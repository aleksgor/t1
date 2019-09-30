package com.nomad.model.update;


public interface UpdateItem {
    enum Operation {
        INCREMENT, DECREMENT, DECREMENT_BY, MULTIPLY, DIVIDE, DIVIDE_BY
    }

    String getFieldName();

    void setFieldName(String fieldName);

    String getValue();

    void setValue(String value);

    Operation getOperation();

    void setOperation(Operation operation);

}
