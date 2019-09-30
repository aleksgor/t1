package com.nomad.pm.generator;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.Condition;
import com.nomad.model.Criteria;
import com.nomad.model.CriteriaItem;
import com.nomad.model.Field;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ModelDescription;
import com.nomad.model.Relation;
import com.nomad.model.RequestType;
import com.nomad.model.SortField;
import com.nomad.model.criteria.AbstractCriteria;
import com.nomad.model.criteria.StatisticElement;
import com.nomad.pm.exception.SysPmException;
import com.nomad.server.DataDefinitionService;
import com.mysql.jdbc.StringUtils;

public class CommonSqlGenerator implements SqlGenerator {
    protected static Logger LOGGER = LoggerFactory.getLogger(CommonSqlGenerator.class);

    private String tablePrefix = "t_";
    private final String fieldPrefix = "f_";
    private String relationPrefix = "r_";
    private String sqlSelect = "";
    private String sqlFrom = "";
    private String sqlWhere = "";
    private String sqlParameters = "";
    private String sqlOrder = "";
    private final DataDefinitionService dataDefinitionService;
    private FullField fields = null;
    private FullField fieldsSelect = null;
    private int fieldCounter = 0;
    private String errorMessage = "";
    private boolean error = false;
    private final boolean noAlias;
    private String statisticSelect;
    private String groupSelect;
    private final List<StatisticElement> statisticElements = new ArrayList<>();
    private final List<StatisticElement> groupElements = new ArrayList<>();
    private String groupBy;
    private String groupOrderBy;

    private ModelDescription table;

    private List<ParameterField> parameters = new LinkedList<>();

    @Override
    public List<ParameterField> getParameters() {
        return parameters;
    }

    @Override
    public String getGroupOrderBy() {
        return groupOrderBy;
    }

    @Override
    public String getGroupBy() {
        return groupBy;
    }

    @Override
    public List<StatisticElement> getGroupElements() {
        return groupElements;
    }

    @Override
    public List<StatisticElement> getStatisticElements() {
        return statisticElements;
    }

    @Override
    public String getStatisticSelect() {
        return statisticSelect;
    }

    @Override
    public String getGroupSelect() {
        return groupSelect;
    }

    public CommonSqlGenerator(final DataDefinitionService dataDefinitionService, boolean noAlias) {
        this.dataDefinitionService = dataDefinitionService;
        this.noAlias = noAlias;
    }

    @Override
    public String getSqlSelect() {
        return sqlSelect;
    }

    @Override
    public String getSqlFrom() {
        return sqlFrom;
    }

    @Override
    public String getSqlOrder() {
        if (sqlOrder.length() > 0) {
            return " ORDER BY " + sqlOrder;
        }
        return "";
    }

    @Override
    public String getSqlWhere() {
        if (sqlWhere.length() > 0) {
            return " WHERE " + sqlWhere;
        }
        return "";
    }

    /*
     * public void addField(Table table, Field field) { if (fields.size() > 0) {
     * sqlSelect += ", "; } fields.add(new FullField(table, field)); }
     */
    public String getFrom() {
        return sqlFrom;
    }

    public String getSql() {
        return sqlSelect;
    }

    public FullField getField() {
        return fields;
    }

    @Override
    public String getSqlParameters() {
        return sqlParameters;
    }

    public void deleteModel(final Identifier identifier) throws SysPmException {
        final ModelDescription table = dataDefinitionService.getModelDescription(identifier.getModelName());
        fields = new FullField(table);
        sqlFrom = table.getDataBaseName();

        for (final Field field : table.getPrimaryKeyFields()) {
            if (sqlWhere.length() > 1) {
                sqlWhere += " AND ";
            }
            sqlWhere += field.getDataBaseName() + "= ? ";
            parameters.add(new ParameterField(field.getRequestType(), getObject(identifier, field.getName())));
        }
    }

    @Override
    public void setIdentifiers(final Collection<Identifier> identifiers) throws SysPmException {
        table = dataDefinitionService.getModelDescription(identifiers.iterator().next().getModelName());
        fieldsSelect = addFieldsForSelect(table, tablePrefix + table.getModelName(), true);
        fields = new FullField(table);
        sqlFrom += table.getDataBaseName() + " " + tablePrefix + table.getModelName();

        if (identifiers.size() == 1) {
            for (final Field field : table.getPrimaryKeyFields()) {
                if (sqlWhere.length() > 1) {
                    sqlWhere += " AND ";
                }
                sqlWhere += tablePrefix + table.getModelName() + "." + field.getDataBaseName() + "= ? ";
                fields.getFields().add(field);
                parameters.add(new ParameterField(field.getRequestType(), getObject(identifiers.iterator().next(), field.getName())));
            }
        } else if (table.getPrimaryKeyFields().size() == 1) {
            List<String> wh = new ArrayList<>(identifiers.size());
            Field field = table.getPrimaryKeyFields().iterator().next();
            for (Identifier identifier : identifiers) {
                wh.add(" ? ");
                parameters.add(new ParameterField(field.getRequestType(), getObject(identifier, field.getName())));
                fields.getFields().add(field);
            }

            if (!StringUtils.isEmptyOrWhitespaceOnly(sqlWhere)) {
                sqlWhere += " AND ";
            }
            sqlWhere += tablePrefix + table.getModelName() + "." + field.getDataBaseName() + " IN (" + wh.stream().collect(Collectors.joining(", ")) + ")";
        } else {
            List<String> wh = new ArrayList<>(identifiers.size());
            for (Identifier identifier : identifiers) {
                String oneKey = "";
                for (final Field field : table.getPrimaryKeyFields()) {
                    if (!StringUtils.isEmptyOrWhitespaceOnly(oneKey)) {
                        oneKey += " AND ";
                    }
                    oneKey += tablePrefix + table.getModelName() + "." + field.getDataBaseName() + "= ? ";
                    parameters.add(new ParameterField(field.getRequestType(), getObject(identifier, field.getName())));
                    fields.getFields().add(field);
                }
                wh.add(oneKey);
            }
            if (!StringUtils.isEmptyOrWhitespaceOnly(sqlWhere)) {
                sqlWhere += " AND ";
            }
            sqlWhere += "(" + wh.stream().collect(Collectors.joining(" OR ")) + ")";
        }

    }

    @Override
    public void updateModel(final Model model) throws SysPmException {
        table = dataDefinitionService.getModelDescription(model.getModelName());
        fields = new FullField(table);
        sqlFrom = table.getDataBaseName();
        int fieldCounter = 0;

        for (final Field field : table.getFields().values()) {
            if (fieldCounter > 0) {
                sqlSelect += ", ";
            }
            sqlSelect += field.getDataBaseName() + "=?";
            fields.getFields().add(field);
            fieldCounter++;
        }

        for (final Field field : table.getPrimaryKeyFields()) {
            if (sqlWhere.length() > 1) {
                sqlWhere += " AND ";
            }
            fields.getFields().add(field);
            sqlWhere += field.getDataBaseName() + "= ? ";
        }
    }



    @Override
    public void addModel(final Model model) throws SysPmException {
        ModelDescription table = getTable(model.getModelName());

        fields = new FullField(table);
        for (final Field field : table.getFields().values()) {
            if (fieldCounter > 0) {
                sqlSelect += ", ";
            }
            sqlSelect += field.getDataBaseName();
            fields.getFields().add(field);

            if (sqlParameters.length() > 0) {
                sqlParameters += ",";
            }
            sqlParameters += "? ";

            fieldCounter++;
        }
        if (sqlFrom.length() > 2) {
            sqlFrom += ", ";
        }
        sqlFrom += table.getDataBaseName();
    }


    @Override
    public void setValues(final Model model, PreparedStatement statement) throws SysPmException {
        parameters.clear();
        for (final Field field : fields.getFields()) {
            parameters.add(new ParameterField(field.getRequestType(), getObject(model, field.getName())));
        }
        setStatements(statement);
    }

    @Override
    public void setValues(Collection<Identifier> identifiers, PreparedStatement statement) throws SysPmException {
        parameters.clear();
        int counPk=-1;
        Iterator<Identifier> iterator =  identifiers.iterator();
        Identifier currentId=null;
        for (final Field field : fields.getFields()) {
            if(counPk<=0){
                counPk=table.getPrimaryKeyFields().size();
                currentId= iterator.next();
            }
            counPk--;
            parameters.add(new ParameterField(field.getRequestType(), getObject(currentId, field.getName())));
        }
        setStatements(statement);
    }
    
    private ModelDescription getTable(final String modelName ) {
        if (table == null) {
            table = dataDefinitionService.getModelDescription(modelName);
        }
        return table;

    }
    private void addRelation(final Relation relation, final boolean loadBlob) {
        final ModelDescription childTable = dataDefinitionService.getModelDescription(relation.getChildrenModel());
        final FullField childFullField = new FullField(childTable);
        childFullField.setField(relation.getFieldName());
        addFieldsForSelect(childTable, relationPrefix + relation.getName(), loadBlob);
        fields.getTables().add(childFullField);
    }

    private void addFor(final Relation relation, final ModelDescription mainTable, final Set<String> addedRelation) {
        final ModelDescription childTable = dataDefinitionService.getModelDescription(relation.getChildrenModel());
        if (addedRelation.contains(relation.getName())) {
            return;
        }

        addedRelation.add(relation.getName());
        boolean first = true;

        switch (relation.getJoin()) {
        case INNER:
            sqlFrom += " INNER JOIN " + childTable.getDataBaseName() + " " + relationPrefix + relation.getName() + " ON ";
            first = true;
            for (final Condition relationItem : relation.getConditions()) {
                final Field parentField = mainTable.getField(relationItem.getParentFieldName());
                final Field childrenField = childTable.getField(relationItem.getChildFieldName());
                if (!first) {
                    sqlFrom += " AND ";
                }
                sqlFrom += tablePrefix + mainTable.getModelName() + "." + parentField.getDataBaseName() + " = " + relationPrefix + relation.getName() + "."
                        + childrenField.getDataBaseName();
            }
            break;
        case LEFT_OUTER:
            sqlFrom += " LEFT OUTER JOIN " + childTable.getDataBaseName() + " " + relationPrefix + relation.getName() + " ON ";
            first = true;
            for (final Condition relationItem : relation.getConditions()) {
                final Field parentField = mainTable.getField(relationItem.getParentFieldName());
                final Field childrenField = childTable.getField(relationItem.getChildFieldName());
                if (!first) {
                    sqlFrom += " AND ";
                }
                sqlFrom += tablePrefix + mainTable.getModelName() + "." + parentField.getDataBaseName() + " = " + relationPrefix + relation.getName() + "."
                        + childrenField.getDataBaseName();
            }
            break;
        case RIGHT_OUTER:
            sqlFrom += " RIGHT OUTER JOIN " + childTable.getDataBaseName() + " " + relationPrefix + relation.getName() + " ON ";
            first = true;
            for (final Condition relationItem : relation.getConditions()) {
                final Field parentField = mainTable.getField(relationItem.getParentFieldName());
                final Field childrenField = childTable.getField(relationItem.getChildFieldName());
                if (!first) {
                    sqlFrom += " AND ";
                }
                sqlFrom += tablePrefix + mainTable.getModelName() + "." + parentField.getDataBaseName() + " = " + relationPrefix + relation.getName() + "."
                        + childrenField.getDataBaseName();
            }
            break;
        case FULL_OUTER:
            sqlFrom += " FULL OUTER JOIN " + childTable.getDataBaseName() + " " + relationPrefix + relation.getName() + " ON ";
            first = true;
            for (final Condition relationItem : relation.getConditions()) {
                final Field parentField = mainTable.getField(relationItem.getParentFieldName());
                final Field childrenField = childTable.getField(relationItem.getChildFieldName());
                if (!first) {
                    sqlFrom += " AND ";
                }
                sqlFrom += tablePrefix + mainTable.getModelName() + "." + parentField.getDataBaseName() + " = " + relationPrefix + relation.getName() + "."
                        + childrenField.getDataBaseName();
            }
            break;
        case COLLECTION:
            break;
        default:
            break;

        }

    }

    @Override
    public void setCriteria(final Criteria<? extends Model> criteria) throws SysPmException {

        setCriteria(criteria, false);

    }

    @Override
    public void setCriteriaForId(final Criteria<? extends Model> criteria) throws SysPmException {
        setCriteria(criteria, true);
    }

    @Override
    public String getFullDataBaseName(String modelName, String fieldName) {
        final ModelDescription mainTable = dataDefinitionService.getModelDescription(modelName);
        if (mainTable != null) {
            Field field = mainTable.getField(fieldName);
            if (field != null) {
                return tablePrefix + mainTable.getModelName() + "." + field.getDataBaseName();
            } else {
                LOGGER.error(" field:" + modelName + " not found in model: " + modelName);
            }
        } else {
            LOGGER.error(" model:" + modelName + " not found. ");
        }
        return null;
    }

    private void setCriteria(final Criteria<? extends Model> criteria, final boolean onlyIdentifier) throws SysPmException {
        final Set<String> addedRelation = new HashSet<>();
        final ModelDescription mainTable =getTable(criteria.getModelName());
        fields = new FullField(mainTable);
        if (mainTable == null) {
            error = true;
            errorMessage = "Model not found:'" + criteria.getModelName() + "'";
            throw new SysPmException("no description for model :"+criteria.getModelName() +" in file:"+dataDefinitionService.getFileName());
        }
        // fieldsSelect fields
        if (onlyIdentifier) {
            fieldsSelect = addFieldForId(mainTable, tablePrefix + mainTable.getModelName(), criteria.isBinaryLoad());
        } else {
            fieldsSelect = addFieldsForSelect(mainTable, tablePrefix + mainTable.getModelName(), criteria.isBinaryLoad());

        }
        if (noAlias) {
            sqlFrom += mainTable.getDataBaseName();
        } else {
            sqlFrom += mainTable.getDataBaseName() + " " + tablePrefix + mainTable.getModelName();
        }

        for (final String relationSource : criteria.getRelationsLoad()) {
            final Relation relation = mainTable.getRelationByName(relationSource);
            if (relation != null) {
                addFor(relation, mainTable, addedRelation);
                if(!onlyIdentifier){
                    addRelation(relation, criteria.isBinaryLoad());
                }
            } else if (!onlyIdentifier) {
                error = true;
                errorMessage = "Relation:'" + relationSource + "' not found in table:'" + mainTable.getModelName() + "'";
                return;
            }
        }
        // connect Criteria data
        for (final CriteriaItem criteriaItem : criteria.getCriteria()) {
            for (final String relationName : getRelationsName(criteriaItem)) {
                final Relation relation = mainTable.getRelationByName(relationName);
                if (!addedRelation.contains(relation.getName())) {
                    addFor(relation, mainTable, addedRelation);

                }
            }
        }

        for (final CriteriaItem criteriaItem : criteria.getCriteria()) {
            if (criteriaItem.getFieldValue() != null || RequestType.GROUP.equals(criteriaItem.getFieldType())) {
                if (sqlWhere.length() > 1) {
                    sqlWhere += " AND ";
                }

                sqlWhere += parseCriteriaElement(criteriaItem);
            }
            if (RequestType.CRITERIA.equals(criteriaItem.getFieldType())) {// criteria
                if (sqlWhere.length() > 1) {
                    sqlWhere += " AND ";
                }
                sqlWhere += getSqlByCriteriaIn(criteria.getModelName(), criteriaItem);
            }

        }

        // order

        for (final SortField sortField : criteria.getOrder()) {
            if (sqlOrder.length() > 1) {
                sqlOrder += ", ";
            }
            final String relationName = sortField.getRelationName();
            if (relationName == null) {

                final Field field = mainTable.getField(sortField.getFieldName());
                if (field == null) {
                    errorMessage += "field:'" + sortField.getFieldName() + "' not found in:'" + mainTable.getModelName() + "'";
                    error = true;
                    return;
                }
                sqlOrder += tablePrefix + mainTable.getModelName() + "." + field.getDataBaseName();
            } else {
                final Relation relation = mainTable.getRelationByName(relationName);
                addFor(relation, mainTable, addedRelation);
                final ModelDescription relationTable = dataDefinitionService.getModelDescription(relation.getChildrenModel());
                sqlOrder += relationPrefix + relation.getName() + "." + relationTable.getField(sortField.getFieldName()).getDataBaseName();
            }
            if (SortField.Order.DESC.equals(sortField.getOrder())) {
                sqlOrder += " DESC";
            }
        }
        // statistic
        String statisticSelect = "";
        if (criteria.isStatisticRequest()) {
            Set<StatisticElement> statisticElements = criteria.getStatistics();
            for (StatisticElement statisticElement : statisticElements) {
                if (statisticElement.getFunction() == null) {
                    LOGGER.error("incorrect statistic function:" + statisticElement + " Element ignored");
                } else {
                    String fromPart;
                    switch (statisticElement.getFunction()) {
                    case COUNT:
                        fromPart = getCountFunction(getFullDataBaseName(statisticElement.getModelName(), statisticElement.getFieldName()));
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        this.statisticElements.add(statisticElement);
                        break;
                    case SUM:
                        fromPart = getSumFunction(getFullDataBaseName(statisticElement.getModelName(), statisticElement.getFieldName()));
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        if (fromPart != null) {
                            this.statisticElements.add(statisticElement);
                        }
                        break;
                    case AVG:
                        fromPart = getAverageFunction(getFullDataBaseName(statisticElement.getModelName(), statisticElement.getFieldName()));
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        if (fromPart != null) {
                            this.statisticElements.add(statisticElement);
                        }
                        break;
                    case MAX:
                        fromPart = getMaxFunction(getFullDataBaseName(statisticElement.getModelName(), statisticElement.getFieldName()));
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        if (fromPart != null) {
                            this.statisticElements.add(statisticElement);
                        }
                        break;
                    case MIN:
                        fromPart = getMinFunction(getFullDataBaseName(statisticElement.getModelName(), statisticElement.getFieldName()));
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        if (fromPart != null) {
                            this.statisticElements.add(statisticElement);
                        }
                        break;
                    }
                }
            }
            this.statisticSelect = statisticSelect;
        }

        if (criteria.isGroupingRequest()) {
            statisticSelect = "";
            groupBy = "";

            Set<String> groupFields = new LinkedHashSet<>();
            Set<StatisticElement> groupElements = criteria.getGroups();
            for (StatisticElement groupElement : groupElements) {
                if (groupElement.getFunction() == null) {
                    LOGGER.error("incorrect statistic function:" + groupElement + " Element ignored");
                } else {
                    String statisticDataBaseName = getFullDataBaseName(groupElement.getModelName(), groupElement.getFieldName());
                    if (!groupElement.isFieldOnly()) {
                        groupFields.add(statisticDataBaseName);
                    }

                    String fromPart;
                    switch (groupElement.getFunction()) {
                    case COUNT:
                        fromPart = getCountFunction(statisticDataBaseName);
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        this.groupElements.add(groupElement);
                        break;
                    case SUM:
                        fromPart = getSumFunction(statisticDataBaseName);
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        if (fromPart != null) {
                            this.groupElements.add(groupElement);
                        }
                        break;
                    case AVG:
                        fromPart = getAverageFunction(statisticDataBaseName);
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        if (fromPart != null) {
                            this.groupElements.add(groupElement);
                        }
                        break;
                    case MAX:
                        fromPart = getMaxFunction(statisticDataBaseName);
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        if (fromPart != null) {
                            this.groupElements.add(groupElement);
                        }
                        break;
                    case MIN:
                        fromPart = getMinFunction(statisticDataBaseName);
                        statisticSelect = commaConcatenation(statisticSelect, fromPart);
                        if (fromPart != null) {
                            this.groupElements.add(groupElement);
                        }
                        break;
                    }

                }
            }

            groupSelect = groupFields.stream().collect(Collectors.joining(",")).concat(",").concat(statisticSelect);
            groupBy = groupFields.stream().collect(Collectors.joining(","));

            groupOrderBy = groupFields.stream().collect(Collectors.joining(","));
            groupCount = groupFields.size();
            if (groupBy != null && groupBy.length() > 0) {
                groupBy = " GROUP BY " + groupBy;
            }
        }

        // grouping

    }

    private int groupCount = 0;

    @Override
    public int getGroupCount() {
        return groupCount;
    }

    protected String getCountFunction(String field) {
        if (field == null || field.length() == 0) {
            field = "*";
        }
        return "COUNT(" + field + ")";
    }

    protected String getSumFunction(String field) {
        if (field == null || field.length() == 0) {
            return null;
        }
        return "SUM(" + field + ")";
    }

    protected String getAverageFunction(String field) {
        if (field == null || field.length() == 0) {
            return null;
        }
        return "AVG(" + field + ")";
    }

    protected String getMinFunction(String field) {
        if (field == null || field.length() == 0) {
            return null;
        }
        return "MIN(" + field + ")";
    }

    protected String getMaxFunction(String field) {
        if (field == null || field.length() == 0) {
            return null;
        }
        return "MAX(" + field + ")";
    }

    private String commaConcatenation(String input, String part) {
        if (input == null || input.length() == 0) {
            return part;
        }
        if (part == null || part.length() == 0) {
            return input;
        }
        return input + ", " + part;
    }

    private List<String> getRelationsName(final CriteriaItem criteriaItem) {
        if (criteriaItem.getFieldName() != null) {
            final String[] data = criteriaItem.getFieldName().split("\\.");
            if (data.length > 1) {
                final List<String> result = new ArrayList<>(data.length - 1);
                for (int i = 0; i < data.length - 1; i++) {
                    result.add(data[i]);
                }
                return result;
            }
        }
        return Collections.<String> emptyList();
    }

    private FullField addFieldsForSelect(final ModelDescription table, final String tablePrefix, final boolean loadBlob) {
        final FullField fullField = new FullField(table);
        for (final Field field : table.getFields().values()) {
            if (!(RequestType.BLOB.equals(field.getRequestType()) || RequestType.CLOB.equals(field.getRequestType())) || loadBlob) {
                if (sqlSelect.length() > 0) {
                    sqlSelect += ", ";
                }
                if (noAlias) {
                    sqlSelect += field.getDataBaseName();
                } else {
                    sqlSelect += tablePrefix + "." + field.getDataBaseName() + " AS " + fieldPrefix + fieldCounter;
                }
                fullField.getFields().add(field);
                fieldCounter++;
            }
        }

        return fullField;
    }

    private FullField addFieldForId(final ModelDescription table, final String tablePrefix, final boolean loadBlob) {
        final FullField fullField = new FullField(table);
        for (final Field field : table.getFields().values()) {
            if (field.isIdentifier()) {
                if (!(RequestType.BLOB.equals(field.getRequestType()) || RequestType.CLOB.equals(field.getRequestType())) || loadBlob) {
                    if (sqlSelect.length() > 0) {
                        sqlSelect += ", ";
                    }
                    if (noAlias) {
                        sqlSelect += field.getDataBaseName();
                    } else {
                        sqlSelect += tablePrefix + "." + field.getDataBaseName() + " AS " + fieldPrefix + fieldCounter;
                    }
                    fullField.getFields().add(field);
                    fieldCounter++;
                }
            }
        }

        return fullField;
    }

    private Object getObject(final Object id, final String name) throws SysPmException {
        try {
            return new PropertyDescriptor(name, id.getClass()).getReadMethod().invoke(id);
        } catch (final Throwable e) {
            throw new SysPmException(e.getMessage(), e);
        }

    }

    protected String parseCriteriaElement(final CriteriaItem criteriaElement) {
        String result = "";
        boolean found = false;
        LOGGER.debug("parseCriteriaElement: {}", criteriaElement);
        if (RequestType.LIST.equals(criteriaElement.getFieldType())) {// string
            if (criteriaElement.getCondition() == Criteria.Condition.IN) {
                if (criteriaElement.getValueList().size() > 0) {
                    result = getDataBaseName(criteriaElement) + " IN " + getStringForIn(criteriaElement.getValueList());
                } else {
                    result = result + " 1=2 ";
                }
            }
            found = false;

        }
        if (RequestType.STRING.equals(criteriaElement.getFieldType())) // string
        {
            if (Criteria.Condition.LIKE_LEFT.equals(criteriaElement.getCondition())) {
                if (Criteria.Case.UPPER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), ("%" + criteriaElement.getFieldValue().toString()).toUpperCase()));
                    result = result + " upper(" + getDataBaseName(criteriaElement) + ") LIKE ?";

                } else if (Criteria.Case.LOWER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), ("%" + criteriaElement.getFieldValue().toString()).toLowerCase()));
                    result = result + " lower(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.NO_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), "%" + criteriaElement.getFieldValue().toString()));
                    result = result + getDataBaseName(criteriaElement) + " LIKE ?";
                }
                found = false;
            }
            if (criteriaElement.getCondition() == Criteria.Condition.LIKE_RIGHT) {
                // ret = ret + getdbName(cr) + " LIKE ?";
                if (Criteria.Case.UPPER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), (criteriaElement.getFieldValue().toString() + "%").toUpperCase()));
                    result = result + " upper(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.LOWER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), (criteriaElement.getFieldValue().toString() + "%").toLowerCase()));
                    result = result + " lower(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.NO_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), criteriaElement.getFieldValue().toString() + "%"));
                    result = result + getDataBaseName(criteriaElement) + " LIKE ?";
                }
                found = false;
            }
            if (criteriaElement.getCondition() == Criteria.Condition.LIKE_ALL) {
                if (Criteria.Case.UPPER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), ("%" + criteriaElement.getFieldValue().toString() + "%").toUpperCase()));
                    result = result + " upper(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                }
                if (Criteria.Case.LOWER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), ("%" + criteriaElement.getFieldValue().toString() + "%").toLowerCase()));
                    result = result + " lower(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.NO_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), "%" + criteriaElement.getFieldValue().toString() + "%"));
                    result = result + getDataBaseName(criteriaElement) + " LIKE ?";
                }
                found = false;
            }
            if (criteriaElement.getCondition() == Criteria.Condition.NOT_LIKE) {
                if (Criteria.Case.UPPER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), ("%" + criteriaElement.getFieldValue().toString() + "%").toUpperCase()));
                    result = result + "NOT upper(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.LOWER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), ("%" + criteriaElement.getFieldValue().toString() + "%").toLowerCase()));
                    result = result + "NOT lower(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                }
                if (Criteria.Case.NO_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), "%" + criteriaElement.getFieldValue().toString() + "%"));
                    result = result + " NOT " + getDataBaseName(criteriaElement) + " LIKE ?";
                }
                found = false;
            }
            if (criteriaElement.getCondition() == Criteria.Condition.EQ_MASK) {
                if (Criteria.Case.UPPER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters
                            .add(new ParameterField(criteriaElement.getFieldType(), translateMask((criteriaElement.getFieldValue().toString()).toUpperCase())));
                    result = result + " upper(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.LOWER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters
                            .add(new ParameterField(criteriaElement.getFieldType(), translateMask((criteriaElement.getFieldValue().toString()).toLowerCase())));
                    result = result + " lower(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.NO_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), translateMask(criteriaElement.getFieldValue().toString())));
                    result = result + getDataBaseName(criteriaElement) + " LIKE ?";
                }
                found = false;
            }

            if (criteriaElement.getCondition() == Criteria.Condition.EQ_MASK_RIGHT) {
                if (Criteria.Case.UPPER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(),
                            translateMask(((criteriaElement.getFieldValue() + "*").toString()).toUpperCase())));
                    result = result + " upper(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.LOWER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(),
                            translateMask(((criteriaElement.getFieldValue() + "*").toString()).toLowerCase())));
                    result = result + " lower(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.NO_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), translateMask((criteriaElement.getFieldValue() + "*").toString())));
                    result = result + getDataBaseName(criteriaElement) + " LIKE ?";
                }
                found = false;
            }
            if (criteriaElement.getCondition() == Criteria.Condition.EQ_MASK_LEFT) {
                if (Criteria.Case.UPPER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(),
                            translateMask((("*" + criteriaElement.getFieldValue().toString())).toUpperCase())));
                    result = result + " upper(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                } else if (Criteria.Case.LOWER_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(),
                            translateMask((("*" + criteriaElement.getFieldValue().toString())).toLowerCase())));
                    result = result + " lower(" + getDataBaseName(criteriaElement) + ") LIKE ?";
                }
                if (Criteria.Case.NO_CASE.equals(criteriaElement.getCaseValue())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), translateMask(("*" + criteriaElement.getFieldValue().toString()))));
                    result = result + getDataBaseName(criteriaElement) + " LIKE ?";
                }
                found = false;
            }

        }
        if (RequestType.STRING.equals(criteriaElement.getFieldType()) || RequestType.DOUBLE.equals(criteriaElement.getFieldType())
                || RequestType.DATE.equals(criteriaElement.getFieldType()) || RequestType.INT.equals(criteriaElement.getFieldType())
                || RequestType.LONG.equals(criteriaElement.getFieldType())) {
            if (criteriaElement.getCondition() == Criteria.Condition.EQ) {
                if (RequestType.STRING.equals(criteriaElement.getFieldType())) {
                    if (Criteria.Case.UPPER_CASE.equals(criteriaElement.getCaseValue())) {
                        parameters.add(new ParameterField(criteriaElement.getFieldType(), (criteriaElement.getFieldValue().toString()).toUpperCase()));
                        result = result + " upper(" + getDataBaseName(criteriaElement) + ") = ?";
                    }
                    if (Criteria.Case.LOWER_CASE.equals(criteriaElement.getCaseValue())) {
                        parameters.add(new ParameterField(criteriaElement.getFieldType(), (criteriaElement.getFieldValue().toString()).toLowerCase()));
                        result = result + " lower(" + getDataBaseName(criteriaElement) + ") = ?";
                    }
                    if (Criteria.Case.NO_CASE.equals(criteriaElement.getCaseValue())) {
                        parameters.add(new ParameterField(criteriaElement.getFieldType(), criteriaElement.getFieldValue().toString()));
                        result = result + getDataBaseName(criteriaElement) + " = ?";
                    }
                } else {
                    result = result + getDataBaseName(criteriaElement) + " = ?";
                    found = true;
                }
            }
            if (criteriaElement.getCondition() == Criteria.Condition.NE) {
                if (RequestType.STRING.equals(criteriaElement.getFieldType())) {
                    if (Criteria.Case.UPPER_CASE.equals(criteriaElement.getCaseValue())) {
                        parameters.add(new ParameterField(criteriaElement.getFieldType(), (criteriaElement.getFieldValue().toString()).toUpperCase()));
                        result = result + " NOT " + " upper(" + getDataBaseName(criteriaElement) + ") = ?";
                    } else if (Criteria.Case.LOWER_CASE.equals(criteriaElement.getCaseValue())) {
                        parameters.add(new ParameterField(criteriaElement.getFieldType(), (criteriaElement.getFieldValue().toString()).toLowerCase()));
                        result = result + " NOT " + " lower(" + getDataBaseName(criteriaElement) + ") = ?";
                    } else if (Criteria.Case.NO_CASE.equals(criteriaElement.getCaseValue())) {
                        parameters.add(new ParameterField(criteriaElement.getFieldType(), criteriaElement.getFieldValue().toString()));
                        result = result + " NOT " + getDataBaseName(criteriaElement) + " = ?";
                    }
                } else

                {
                    result = result + " NOT " + getDataBaseName(criteriaElement) + " = ?";
                    found = true;
                }
            }

            if (Criteria.Condition.GT.equals(criteriaElement.getCondition())) {
                result = result + getDataBaseName(criteriaElement) + " > ?";
                found = true;
            }
            if (Criteria.Condition.LT.equals(criteriaElement.getCondition())) {
                result = result + getDataBaseName(criteriaElement) + " < ?";
                found = true;
            }
            if (Criteria.Condition.GE.equals(criteriaElement.getCondition())) {
                result = result + getDataBaseName(criteriaElement) + " >= ?";
                found = true;
            }
            if (Criteria.Condition.LE.equals(criteriaElement.getCondition())) {
                result = result + getDataBaseName(criteriaElement) + " <= ?";
                found = true;
            }
            if (Criteria.Condition.IS_NULL.equals(criteriaElement.getCondition())) {
                result = result + getDataBaseName(criteriaElement) + " IS NULL";
            }
            if (Criteria.Condition.NOT_IS_NULL.equals(criteriaElement.getCondition())) {
                result = result + " not " + getDataBaseName(criteriaElement) + " IS NULL";
            }
            // if (cr.getCondition() == ISNOTNULL) { ret = ret + "NOT " +
            // cr.getDbName(reg) + " IS NULL"; fp = false; }
            if (found) {
                if (RequestType.STRING.equals(criteriaElement.getFieldType())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), criteriaElement.getFieldValue().toString()));
                }
                if (RequestType.DOUBLE.equals(criteriaElement.getFieldType())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), criteriaElement.getFieldValue()));
                }
                if (RequestType.DATE.equals(criteriaElement.getFieldType())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), criteriaElement.getFieldValue()));
                }
                if (RequestType.INT.equals(criteriaElement.getFieldType())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), criteriaElement.getFieldValue()));
                }
                if (RequestType.LONG.equals(criteriaElement.getFieldType())) {
                    parameters.add(new ParameterField(criteriaElement.getFieldType(), criteriaElement.getFieldValue()));
                }
            }
        }

        if (RequestType.GROUP.equals(criteriaElement.getFieldType())) // group
        {
            final Criteria.Concatenation concatenation = criteriaElement.getConcatenation();
            if (concatenation != null) {
                if (criteriaElement.getCriteria().size() > 0) {
                    result += " ( ";
                    result += addCriteria(criteriaElement.getCriteria(), concatenation);
                    result += ")";
                }
            }
        }

        return result;
    }

    protected String getStringForIn(final List<Object> objects) {
        String result = "(";
        for (final Object object : objects) {
            if (result.length() > 1) {
                result += ",";
            }
            if (object.getClass().getName().indexOf("String") >= 0) {// String
                                                                     // !!
                result += "'" + object.toString() + "'";
            } else {
                // not String
                result += object.toString();
            }
        }
        return result + ")";
    }

    protected String getDataBaseName(final CriteriaItem item) {
        final ModelDescription table = dataDefinitionService.getModelDescription(item.getModelName());
        if (table == null) {

        }
        final String[] pathToField = item.getFieldName().split("\\.");
        if (pathToField.length > 1) { // relation !

            final String lastFieldName = pathToField[pathToField.length - 1];
            final String relationName = pathToField[pathToField.length - 2];
            final Relation relation = table.getRelationByName(relationName);
            if (relation == null) {
                LOGGER.error(" relation not found for {} table:{}", table.getModelName() + " for CRITERIA:" + item);
            }
            final ModelDescription childTable = dataDefinitionService.getModelDescription(relation.getChildrenModel());
            if (childTable == null) {
                LOGGER.error(" Table not found for model: {} in ce:{}", relation.getParentModel(), item);
            }
            final Field f = childTable.getField(lastFieldName);
            if (noAlias) {
                return f.getDataBaseName();
            } else {
                return relationPrefix + relation.getName() + "." + f.getDataBaseName();
            }

        } else {
            final Field f = table.getField(item.getFieldName());
            if (f == null) {
                errorMessage += "Field:'" + item.getFieldName() + "' not found in model:'" + table.getModelName() + "'";
                error = true;
                return "";
            }
            return tablePrefix + table.getModelName() + "." + f.getDataBaseName();
        }

    }

    protected String getSqlByCriteriaIn(final String modelName, final CriteriaItem criteriaItem) throws SysPmException {

        final ModelDescription mainTable = dataDefinitionService.getModelDescription(modelName);
        final ModelDescription modelFromCriteria = dataDefinitionService.getModelDescription(criteriaItem.getModelName());

        final Relation r = mainTable.getRelationByName(criteriaItem.getFieldName());

        if (r == null) {
            return "";
        }

        String relationName = "";
        for (final Condition relationItem : r.getConditions()) {
            if (relationName.length() > 0) {
                relationName += " AND ";
            }
            relationName += tablePrefix + r.getParentModel() + "." + mainTable.getField(relationItem.getParentFieldName()).getDataBaseName() + " = " + "ch_"
                    + r.getChildrenModel() + "." + modelFromCriteria.getField(relationItem.getChildFieldName()).getDataBaseName();
        }
        LOGGER.debug("srel:{}", relationName);
        final CommonSqlGenerator generator = new CommonSqlGenerator(dataDefinitionService, noAlias);
        generator.relationPrefix = "ch_";
        generator.tablePrefix = "ch_";
        generator.setCriteria(criteriaItem.getCriteriaForIn());

        String sqlWhere = generator.getSqlWhere();
        generator.parameters.addAll(parameters);
        parameters = generator.parameters;

        if (sqlWhere.length() == 0) {
            sqlWhere += " WHERE ";
        } else {
            sqlWhere += " AND ";
        }
        sqlWhere += relationName;
        final String sql = "(select count(*) FROM " + generator.getFrom() + sqlWhere + ")>0 ";
        LOGGER.debug("swh1: {}", sql);
        return sql;

    }

    protected String translateMask(String s) {
        if (s == null) {
            return null;
        }
        s = s.replace("*", "%");
        s = s.replace("?", "_");
        return s;
    }

    protected List<Relation> getListTables(final AbstractCriteria<? extends Model> criteria) {

        final List<Relation> result = new ArrayList<>();
        for (final CriteriaItem criteriaItem : criteria.getCriteria()) {
            getListTables(criteriaItem, result);
        }
        return result;
    }

    protected void getListTables(final CriteriaItem criteria, final List<Relation> tables) {

        for (final CriteriaItem criteriaItem : criteria.getCriteria()) {
            getListTables(criteriaItem, tables);
        }
    }

    protected List<Relation> getListModelFromCriteria(final AbstractCriteria<? extends Model> criteria) {
        final ModelDescription mainTable = dataDefinitionService.getModelDescription(criteria.getModelName());
        final List<Relation> tables = new ArrayList<>();
        for (final CriteriaItem criteriaElement : criteria.getCriteria()) {
            getListTables(criteriaElement, tables);
        }

        for (final String relationName : criteria.getRelationsLoad()) {

            final Relation relations = mainTable.getRelationByName(relationName);

            if (relations == null) {
                LOGGER.error(" relation not found for {}", relationName);
            } else {
                if (!tables.contains(relations)) {
                    tables.add(relations);
                }
            }
        }

        return tables;
    }

    protected String addCriteria(final List<CriteriaItem> criteriaElements, final Criteria.Concatenation concatenation) {
        String result = "";
        for (final CriteriaItem criteriaItem : criteriaElements) {

            if (result.length() > 0) {
                if (Criteria.Concatenation.AND.equals(concatenation)) {
                    result = result + " AND ";
                }
                if (Criteria.Concatenation.OR.equals(concatenation)) {
                    result = result + " OR ";
                }
            }
            result += parseCriteriaElement(criteriaItem);
        }
        return result;
    }

    @Override
    public void setStatements(final PreparedStatement statement) {

        int i = 0;
        for (final ParameterField param : parameters) {
            i++;
            try {
                switch (param.getType()) {
                case STRING:
                    final String s = (String) param.getValue();
                    if (s == null) {
                        statement.setNull(i, Types.CHAR);
                    } else {
                        statement.setString(i, s);
                    }
                    break;
                case DOUBLE:
                    final Double d = (Double) param.getValue();
                    statement.setDouble(i, d.doubleValue());
                    break;
                case DATE:
                    final Object o = param.getValue();
                    final java.util.Date date = (java.util.Date) o;
                    if (date == null) {
                        statement.setNull(i, Types.DATE);
                    } else {
                        statement.setTimestamp(i, new Timestamp(date.getTime()));
                    }
                    break;
                case INT:
                    final Integer in = (Integer) param.getValue();
                    statement.setInt(i, in.intValue());
                    break;
                case LONG:
                    final Long l = (Long) param.getValue();
                    statement.setLong(i, l.longValue());
                    break;
                case FLOAT:
                    final Float floatValue = (Float) param.getValue();
                    statement.setFloat(i, floatValue.floatValue());
                    break;
                case CLOB:
                    final String stringValue = (String) param.getValue();
                    if (stringValue != null) {
                        try {
                            final ByteArrayInputStream byteArray = new ByteArrayInputStream(stringValue.getBytes("UTF-8"));
                            statement.setBinaryStream(i, byteArray, byteArray.available());
                        } catch (final UnsupportedEncodingException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    } else {
                        statement.setNull(i, Types.LONGVARBINARY);
                    }
                    break;
                case BLOB:
                    final byte[] bytes = (byte[]) param.getValue();
                    if (bytes != null) {
                        // Blob blob = new
                        statement.setObject(i, bytes);
                    } else {
                        statement.setNull(i, Types.LONGVARBINARY);
                    }
                    break;

                default:
                    break;
                }

            } catch (final SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }

    @Override
    public <T extends Model> T getObjectFromResultSet(final ResultSet resultSet) throws SysPmException {

        return getObjectFromResultSet(resultSet, fieldsSelect, 1);
    }

    private <T extends Model> T getObjectFromResultSet(final ResultSet resultSet, final FullField fullField, int index) throws SysPmException {
        if (fullField == null) {
            return null;
        }
        try {
            final ModelDescription table = fullField.getTable();

            final Class<?> modelClass = Class.forName(table.getClazz());

            final Class<?> identifierClass = Class.forName(table.getClassId());
            @SuppressWarnings("unchecked")
            final T result = (T) modelClass.newInstance();

            final Identifier identifier = (Identifier) identifierClass.newInstance();
            for (final Field filed : fullField.getFields()) {
                final Object value = getData(filed, index, resultSet);
                index++;
                new PropertyDescriptor(filed.getName(), modelClass).getWriteMethod().invoke(result, value);
                if (filed.isIdentifier()) {
                    new PropertyDescriptor(filed.getName(), identifierClass).getWriteMethod().invoke(identifier, value);
                }
            }
            result.setIdentifier(identifier);
            for (final FullField field : fullField.getTables()) {
                final Model child = getObjectFromResultSet(resultSet, field, index);

                if (field.getField() == null) {
                    new PropertyDescriptor(field.getTable().getModelName(), result.getClass()).getWriteMethod().invoke(result, child);
                } else {
                    new PropertyDescriptor(field.getField(), result.getClass()).getWriteMethod().invoke(result, child);
                }

            }
            return result;
        } catch (final Throwable e) {
            throw new SysPmException(e.getMessage(), e);
        }
    }

    @Override
    public Identifier getIdentifierFromResultSet(final ResultSet resultSet) throws SysPmException {
        if (fieldsSelect == null) {
            return null;
        }
        int index = 1;
        try {
            final ModelDescription table = fieldsSelect.getTable();
            final Class<?> identifierClass = Class.forName(table.getClassId());
            final Identifier result = (Identifier) identifierClass.newInstance();
            for (final Field filed : fieldsSelect.getFields()) {

                if (filed.isIdentifier()) {
                    final Object value = getData(filed, index, resultSet);
                    index++;
                    new PropertyDescriptor(filed.getName(), identifierClass).getWriteMethod().invoke(result, value);
                }

            }
            return result;
        } catch (final Throwable e) {
            throw new SysPmException(e.getMessage(), e);
        }
    }

    // ALTER DATABASE test SET bytea_output = 'escape';
    private Object getData(final Field filed, final int index, final ResultSet resultSet) throws SQLException, IOException {
        switch (filed.getRequestType()) {
        case BLOB:

            return resultSet.getBytes(index);

        case CLOB:
            return resultSet.getBytes(index);
        case DATE:
            final Timestamp timeStamp = resultSet.getTimestamp(index);
            if (timeStamp != null) {
                return new Date(timeStamp.getTime());
            }
            return null;
        case DOUBLE:
            return resultSet.getDouble(index);
        case FLOAT:
            return resultSet.getFloat(index);
        case INT:
            return resultSet.getInt(index);
        case LONG:
            return resultSet.getLong(index);
        case STRING:
            return resultSet.getString(index);
        case UNDEFINED:
            return resultSet.getObject(index);
        default:
            break;
        }
        return null;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean isError() {
        return error;
    }

    @Override
    public String toString() {
        return "CommonSqlGenerator [fieldPrefix=" + fieldPrefix + ", relationPrefix=" + relationPrefix + ", sqlSelect=" + sqlSelect + ", sqlFrom=" + sqlFrom
                + ", sqlWhere=" + sqlWhere + ", sqlParameters=" + sqlParameters + ", sqlOrder=" + sqlOrder + ", dataDefinitionService=" + dataDefinitionService
                + ", fields=" + fields + ", fieldCounter=" + fieldCounter + ", errorMessage=" + errorMessage + ", error=" + error + ", noAlias=" + noAlias
                + ", statisticSelect=" + statisticSelect + ", groupSelect=" + groupSelect + ", statisticElements=" + statisticElements + ", groupElements="
                + groupElements + ", groupBy=" + groupBy + ", parameters=" + parameters + ", groupCount=" + groupCount + "]";
    }

}
