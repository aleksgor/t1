package com.nomad.model.criteria;

import java.util.LinkedHashSet;
import java.util.Set;


public class StatisticElementImpl implements StatisticElement {

    private String modelName;
    private String fieldName;
    private AggregateFunction function;
    private String description;
    private Object value;
    private final Set<StatisticElement> children = new LinkedHashSet<>();
    private boolean fieldOnly = false;

    public StatisticElementImpl() {

    }

    public StatisticElementImpl(String modelName, String fieldName, AggregateFunction function) {
        super();
        this.modelName = modelName;
        this.fieldName = fieldName;
        this.function = function;
    }

    public StatisticElementImpl(String modelName, String fieldName, AggregateFunction function, boolean fieldOnly) {
        super();
        this.modelName = modelName;
        this.fieldName = fieldName;
        this.function = function;
        this.fieldOnly = fieldOnly;
    }


    @Override
    public boolean isFieldOnly() {
        return fieldOnly;
    }

    @Override
    public void setFieldOnly(boolean fieldOnly) {
        this.fieldOnly = fieldOnly;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public AggregateFunction getFunction() {
        return function;
    }

    @Override
    public void setFunction(AggregateFunction function) {
        this.function = function;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Double getDoubleValue() {
        if (value == null) {
            return 0.0;
        }
        return new Double(value.toString());
    }

    @Override
    public StatisticElement setValue(Object value) {
        this.value = value;
        return this;
    }


    @Override
    public Set<StatisticElement> getChildren() {
        return children;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime * result + ((function == null) ? 0 : function.hashCode());
        result = prime * result + ((modelName == null) ? 0 : modelName.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StatisticElementImpl other = (StatisticElementImpl) obj;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        if (function != other.function)
            return false;
        if (modelName == null) {
            if (other.modelName != null)
                return false;
        } else if (!modelName.equals(other.modelName))
            return false;

        if (function != null) {
            return true;
        }
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!compareValue(value, other.value))
            return false;
        return true;
    }



    private boolean compareValue(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            Number number1 = (Number) a;
            Number number2 = (Number) b;
            return Double.compare(number1.doubleValue(), number2.doubleValue()) == 0;

        }
        return a.equals(b);
    }

    @Override
    public String toString() {
        return "StatisticElementImpl [modelName=" + modelName + ", fieldName=" + fieldName + ", function=" + function + ", description=" + description + ", value=" + value
                + ", children=" + children + ", fieldOnly=" + fieldOnly + "]";
    }

    @Override
    public StatisticElement getCopy() {
        StatisticElement result = new StatisticElementImpl();
        result.setDescription(description);
        result.setFieldName(fieldName);
        result.setFunction(function);
        result.setModelName(modelName);
        result.setValue(value);
        result.setFieldOnly(fieldOnly);
        return result;
    }

    @Override
    public StatisticElement getChild(StatisticElement template) {
        for (StatisticElement statisticElement : children) {
            if (statisticElement.equals(template)) {
                return statisticElement;
            }
        }
        return null;
    }


}
