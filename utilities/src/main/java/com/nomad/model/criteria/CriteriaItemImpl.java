package com.nomad.model.criteria;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.nomad.model.Criteria;
import com.nomad.model.Criteria.Condition;
import com.nomad.model.CriteriaItem;
import com.nomad.model.Model;
import com.nomad.model.RequestType;

public class CriteriaItemImpl implements CriteriaItem {
    private String fieldName;
    private Object fieldValue;
    private RequestType fieldType;
    private Condition condition;
    private String modelName;
    private Criteria.Case caseValue = Criteria.Case.NO_CASE;
    private final List<CriteriaItem> criteria = new ArrayList<>();
    private Criteria.Concatenation concatenation = null;
    private Criteria<? extends Model> criteriaForIn=null;


    public CriteriaItemImpl() {

    }
    public CriteriaItemImpl(final String fieldName, final Criteria<? extends Model> criteria) {
        super();
        criteriaForIn=criteria;
        this.fieldName=fieldName;
        fieldType=RequestType.CRITERIA;
    }

    public CriteriaItemImpl(final String fieldName, final Object fieldValue, final RequestType fieldType, final Condition condition) {
        super();
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.fieldType = fieldType;
        this.condition = condition;
    }
    public CriteriaItemImpl(final String fieldName, final int fieldValue, final Condition condition) {
        super();
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        fieldType = RequestType.INT;
        this.condition = condition;
    }
    public CriteriaItemImpl(final String fieldName, final long fieldValue, final Condition condition) {
        super();
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        fieldType = RequestType.LONG;
        this.condition = condition;
    }
    public CriteriaItemImpl(final String fieldName, final String fieldValue, final Condition condition) {
        super();
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        fieldType = RequestType.STRING;
        this.condition = condition;
    }

    public CriteriaItemImpl(final String fieldName, final Date fieldValue, final Condition condition) {
        super();
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        fieldType = RequestType.DATE;
        this.condition = condition;
    }

    public CriteriaItemImpl(final String fieldName, final double fieldValue, final Condition condition) {
        super();
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        fieldType = RequestType.DOUBLE;
        this.condition = condition;
    }
    public CriteriaItemImpl(final String fieldName, final List<Object> fieldValue, final Condition condition) {
        super();
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        fieldType = RequestType.LIST;
        this.condition = condition;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public Object getFieldValue() {
        return fieldValue;
    }

    @Override
    public void setFieldValue(final Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public RequestType getFieldType() {
        return fieldType;
    }

    @Override
    public void setFieldType(final RequestType fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public void setCondition(final Condition condition) {
        this.condition = condition;
    }


    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public void setModelName(final String modelName) {
        this.modelName = modelName;
    }

    @Override
    public Criteria.Case getCaseValue() {
        return caseValue;
    }

    @Override
    public void setCaseValue(final Criteria.Case caseValue) {
        this.caseValue = caseValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> getValueList() {

        if (RequestType.LIST.equals(fieldType)) {
            return (List<Object>) fieldValue;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<CriteriaItem> getCriteria() {
        return criteria;
    }

    @Override
    public void setCriteria(final List<CriteriaItem> criteria) {
        this.criteria.clear();
        this.criteria.addAll(criteria);
    }

    @Override
    public Criteria.Concatenation getConcatenation() {
        return concatenation;
    }

    @Override
    public void setConcatenation(final Criteria.Concatenation concatenation) {
        this.concatenation = concatenation;
    }

    @Override
    public Criteria<? extends Model> getCriteriaForIn() {
        return criteriaForIn;
    }

    @Override
    public void setCriteriaForIn(final Criteria<? extends Model> criteriaForIn) {
        this.criteriaForIn = criteriaForIn;
    }

    @Override
    public String toString() {
        return "CriteriaItemImpl [fieldName=" + fieldName + ", fieldValue=" + fieldValue + ", fieldType=" + fieldType + ", condition=" + condition + ", modelName="
                + modelName
                + ", caseValue=" + caseValue + ", criteria=" + criteria + ", concatenation=" + concatenation + ", criteriaForIn=" + criteriaForIn
                + "]";
    }


}
