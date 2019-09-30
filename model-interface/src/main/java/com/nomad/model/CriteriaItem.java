package com.nomad.model;

import java.util.List;

import com.nomad.model.Criteria.Condition;

public interface CriteriaItem {

    String getFieldName();

    void setFieldName(String fieldName);

    Object getFieldValue();

    void setFieldValue(Object fieldValue);

    RequestType getFieldType();

    void setFieldType(RequestType fieldType);

    Condition getCondition();

    void setCondition(Condition condition);

    String getModelName();

    void setModelName(String modelName);

    Criteria.Case getCaseValue();

    void setCaseValue(Criteria.Case caseValue);

    List<Object> getValueList();

    List<CriteriaItem> getCriteria();

    void setCriteria(List<CriteriaItem> criteria);

    Criteria.Concatenation getConcatenation();

    void setConcatenation(Criteria.Concatenation concatenation);

    Criteria<? extends Model> getCriteriaForIn();

    void setCriteriaForIn(Criteria<? extends Model> criteriaForIn);

}
