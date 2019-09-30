package com.nomad.model.criteria;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.nomad.exception.SystemException;
import com.nomad.model.Criteria;
import com.nomad.model.CriteriaGroup;
import com.nomad.model.CriteriaItem;
import com.nomad.model.Model;
import com.nomad.model.RequestType;
import com.nomad.model.SortField;

/**
 * base class of all criteria
 * @author alexgor Date: 06.01.2005 Time: 12:38:41
 */
public abstract class AbstractCriteria<T extends Model> implements Criteria<T> {

    private final Set<StatisticElement> statistics = new LinkedHashSet<>();

    private final Set<StatisticElement> grouping = new LinkedHashSet<>();

    private final List<StatisticElement> groupingResult = new ArrayList<>();

    private final Set<String> relationForLoad = new HashSet<>();

    private List<CriteriaItem> criteria = new ArrayList<>();

    protected final List<SortField> order = new ArrayList<>();

    @Override
    public List<StatisticElement> getGroupingResult() {
        return groupingResult;
    }

    /**
     * content String of name model for add model relation
     */
    private boolean calculateCount = true;

    protected long startPosition = 1;

    protected int pageSize = -1;

    /**
     * List of model for return
     */
    protected StatisticResult<T> resultList = null;

    private boolean binaryLoad = true;

    /**
     * flag of select distinct
     */
    private boolean distinct = false;

    private boolean statisticOnly = false;

    public boolean isStatisticOnly() {
        return statisticOnly;
    }

    public void setStatisticOnly(boolean statisticOnly) {
        this.statisticOnly = statisticOnly;
    }

    /**
     * set new List for result
     * @param retList list of Models
     */

    @Override
    public void setResult(final StatisticResult<T> result) {
        this.resultList = result;
    }

    /**
     * distinct select indicator
     * @return true if distinct selected
     */
    @Override
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * set flag distinct
     * @param distinct
     */

    @Override
    public void setDistinct(final boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * default constructor
     */
    public AbstractCriteria() {
        criteria = new ArrayList<>();
        order.clear();
        relationForLoad.clear();
        calculateCount = true;
        startPosition = 1;
        pageSize = -1;
        resultList = null;
        binaryLoad = true;
        distinct = false;

    }

    /**
     * Creates a new instance of Criterion
     */

    public AbstractCriteria(final int startPosition, final int countRow) {
        this();
        setStartPosition(startPosition);
        this.pageSize = countRow;
    }

    /**
     * Creates a new instance of Criterion by template
     */

    public AbstractCriteria(final Criteria<T> criteria) {
        this.relationForLoad.clear();
        this.relationForLoad.addAll(criteria.getRelationsLoad());
        this.criteria = criteria.getCriteria();
        this.startPosition = criteria.getStartPosition();
        this.pageSize = criteria.getPageSize();
        this.order.clear();
        this.order.addAll(criteria.getOrder());
        calculateCount = criteria.isCalculateCount();
        resultList = criteria.getResult();
        binaryLoad = criteria.isBinaryLoad();
        distinct = criteria.isDistinct();

    }

    /**
     * add new model for load
     * @param m Model mast be valid model name
     * @return true if OK or false if name model not valid
     */

    @Override
    public Criteria<T> addRelationLoad(final String relationName) {
        relationForLoad.add(relationName);
        return this;
    }

    @Override
    public boolean isRelationLoad(final String relationName) {

        return relationForLoad.contains(relationName);

    }

    @Override
    public boolean removeRelationLoad(final String relationName) {
        return relationForLoad.remove(relationName);
    }

    @Override
    public Set<String> getRelationsLoad() {
        return relationForLoad;
    }

    @Override
    public abstract String getModelName();

    @Override
    public long getStartPosition() {
        return startPosition;
    }

    @Override
    public void setStartPosition(final long startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(final int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Add String criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    @Override
    public Criteria<T> addCriterion(final String parameterName, final Criteria.Condition condition, final String value) {
        final CriteriaItem item = new CriteriaItemImpl(parameterName, value, RequestType.STRING, condition);
        item.setModelName(this.getModelName());
        criteria.add(item);
        return this;

    }

    /**
     * Add double criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    @Override
    public Criteria<T> addCriterion(final String parameterName, final Criteria.Condition condition, final double value) {
        final CriteriaItem item = new CriteriaItemImpl(parameterName, value, RequestType.DOUBLE, condition);
        item.setModelName(this.getModelName());
        criteria.add(item);

        return this;
    }

    /**
     * Add util.Date criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    @Override
    public Criteria<T> addCriterion(final String parameterName, final Criteria.Condition condition, final Date value) {
        final CriteriaItem item = new CriteriaItemImpl(parameterName, value, RequestType.DATE, condition);
        item.setModelName(this.getModelName());
        criteria.add(item);
        return this;
    }

    /**
     * Add int criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    @Override
    public Criteria<T> addCriterion(final String parameterName, final Criteria.Condition condition, final int value) {
        final CriteriaItem item = new CriteriaItemImpl(parameterName, value, RequestType.INT, condition);
        item.setModelName(this.getModelName());
        criteria.add(item);
        return this;
    }

    /**
     * Add long criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    @Override
    public Criteria<T> addCriterion(final String parameterName, final Criteria.Condition condition, final long value) {
        final CriteriaItem item = new CriteriaItemImpl(parameterName, value, RequestType.LONG, condition);
        item.setModelName(this.getModelName());
        criteria.add(item);
        return this;
    }

    @Override
    public Criteria<T> addCriterion(final String parameterName, final Criteria.Condition condition, final List<Object> value) {
        final CriteriaItem item = new CriteriaItemImpl(parameterName, value, RequestType.LIST, condition);
        item.setModelName(this.getModelName());
        criteria.add(item);
        return this;
    }

    public Criteria<T> addCriterion(final String relationName, final Criteria<? extends Model> criteria) {
        final CriteriaItem item = new CriteriaItemImpl(relationName, criteria);
        item.setModelName(criteria.getModelName());
        this.criteria.add(item);
        return this;
    }

    /**
     * add parameters in SQl "ORDER BY "
     * @param paramName name of parameter
     */
    @Override
    public Criteria<T> addOrderAsc(final String parameterName) {
        order.add(new SortFieldImpl(SortField.Order.ASC, parameterName, null));
        return this;
    }

    @Override
    public Criteria<T> addOrderAsc(final String fieldName, final String relationName) {
        order.add(new SortFieldImpl(SortField.Order.ASC, fieldName, relationName));
        return this;
    }

    /**
     * add parameters in SQl "ORDER BY "
     * @param paramName name of parameter
     */
    @Override
    public Criteria<T> addOrderDesc(final String parameterName) {
        order.add(new SortFieldImpl(SortField.Order.DESC, parameterName, null));
        return this;
    }

    @Override
    public Criteria<T> addOrderDesc(final String fieldName, final String relationName) {
        order.add(new SortFieldImpl(SortField.Order.DESC, fieldName, relationName));
        return this;
    }

    /**
     * count of ctiterion in criterions
     * @return count of ctiterion in criterions
     */
    @Override
    public int count() {
        return criteria.size();
    }

    @Override
    public StatisticResult<T> getResult() {
        return resultList;
    }

    @Override
    public Criteria<T> addORCriterion(final CriteriaGroup criteriaGroup) {
        final CriteriaItem newItem = new CriteriaItemImpl();
        newItem.setFieldType(RequestType.GROUP);
        newItem.setCriteria(criteriaGroup.getCriteria());
        for (final CriteriaItem item : newItem.getCriteria()) {
            item.setModelName(getModelName());
        }
        newItem.setConcatenation(Concatenation.OR);
        criteria.add(newItem);
        return this;
    }

    @Override
    public Criteria<T> addANDCriterion(final CriteriaGroup criteriaGroup) {
        final CriteriaItem newItem = new CriteriaItemImpl();
        newItem.setFieldType(RequestType.GROUP);
        newItem.setCriteria(criteriaGroup.getCriteria());
        newItem.setConcatenation(Concatenation.AND);
        for (final CriteriaItem item : newItem.getCriteria()) {
            item.setModelName(getModelName());
        }
        criteria.add(newItem);
        return this;
    }

    @Override
    public Criteria<T> cleanCriterion() {
        criteria.clear();
        order.clear();
        relationForLoad.clear();

        return this;
    }

    @Override
    public void cleanSort() {
        order.clear();

    }

    protected String formField(final String s, final String modelMName) {
        return modelMName + "." + s;
    }

    public void addSortField(final SortField sortField) {
        if (sortField.getOrder() == SortField.Order.ASC) {
            addOrderAsc(sortField.getFieldName());
        }
        if (sortField.getOrder() == SortField.Order.DESC) {
            addOrderDesc(sortField.getFieldName());
        }
    }

    public void rebuildSorter(final List<SortField> sortFields) {
        cleanSort();
        for (int i = 0; i < sortFields.size(); i++) {
            final SortField filed = sortFields.get(i);
            addSortField(filed);
        }
    }

    @Override
    public List<SortField> getOrder() {
        return order;
    }

    public void setOrder(final ArrayList<SortField> order) {
        this.order.clear();
        this.order.addAll(order);
    }

    @Override
    public boolean isBinaryLoad() {
        return binaryLoad;
    }

    @Override
    public Criteria<T> setBinaryLoad(final boolean binaryLoad) {
        this.binaryLoad = binaryLoad;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized Object clone() throws CloneNotSupportedException, OutOfMemoryError {
        final Criteria<T> criteria = (Criteria<T>) super.clone();
        return criteria;
    }

    @Override
    public boolean isCalculateCount() {
        return calculateCount;
    }

    @Override
    public Criteria<T> setCalculateCount(final boolean calculateCount) {
        this.calculateCount = calculateCount;
        return this;
    }

    @Override
    public List<CriteriaItem> getCriteria() {
        return criteria;
    }

    // statistic (sum, avg, count)
    @Override
    public void addStatisticRequirement(String modelName, String fieldName, AggregateFunction function) {
        statistics.add(new StatisticElementImpl(modelName, fieldName, function));
    }

    @Override
    public void addStatisticRequirement(String fieldName, AggregateFunction function) {
        statistics.add(new StatisticElementImpl(getModelName(), fieldName, function));
    }

    @Override
    public Set<StatisticElement> getStatistics() {
        return statistics;
    }

    @Override
    public boolean isStatisticRequest() {
        return statistics.size() > 0;
    }

    @Override
    public StatisticElement getStatisticElement(String modelName, String fieldName, AggregateFunction function) throws SystemException {
        StatisticElement result = null;
        for (StatisticElement stElement : statistics) {
            if (compare(modelName, stElement.getModelName()) && compare(fieldName, stElement.getFieldName()) && compare(function, stElement.getFunction())) {
                if (result != null) {
                    throw new SystemException("fuzzy result");
                }
                result = stElement;
            }
        }
        return result;
    }

    private boolean compare(Object one, Object second) {
        if (one == null) {
            return second == null;
        }
        return one.equals(second);
    }

    // grouping
    @Override
    public void addGroupRequirement(String modelName, String fieldName, AggregateFunction function) {
        grouping.add(new StatisticElementImpl(modelName, fieldName, function));
    }

    @Override
    public void addGroupRequirement(String fieldName, AggregateFunction function) {
        grouping.add(new StatisticElementImpl(getModelName(), fieldName, function));
    }

    @Override
    public void addGroupSelect(String modelName, String fieldName, AggregateFunction function) {
        grouping.add(new StatisticElementImpl(modelName, fieldName, function, true));
    }

    @Override
    public void addGroupSelect(String fieldName, AggregateFunction function) {
        grouping.add(new StatisticElementImpl(getModelName(), fieldName, function, true));
    }

    @Override
    public Set<StatisticElement> getGroups() {
        return grouping;
    }

    @Override
    public boolean isGroupingRequest() {
        return grouping.size() > 0;
    }

    @Override
    public String toString() {
        return "AbstractCriteria [statistics=" + statistics + ", relationForLoad=" + relationForLoad + ", criterians=" + criteria + ", order=" + order + ", calculateCount="
                + calculateCount + ", startPos=" + startPosition + ", pageSize=" + pageSize + ", resultList=" + resultList + ", binaryLoad=" + binaryLoad + ", distinct="
                + distinct + ", statisticOnly=" + statisticOnly + "]";
    }

}