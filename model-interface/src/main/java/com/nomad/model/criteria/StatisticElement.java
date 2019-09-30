package com.nomad.model.criteria;

import java.util.Set;

public interface StatisticElement {


    String getModelName();

    void setModelName(String modelName);

    String getFieldName();

    void setFieldName(String fieldName);

    AggregateFunction getFunction();

    void setFunction(AggregateFunction function);

    String getDescription();

    void setDescription(String description);

    Object getValue();

    StatisticElement setValue(Object value);

    StatisticElement getCopy();

    Double getDoubleValue();

    StatisticElement getChild(StatisticElement template);

    Set<StatisticElement> getChildren();

    void setFieldOnly(boolean fieldOnly);

    boolean isFieldOnly();

}
