package com.nomad.model.update;

public class UpdateItemImpl implements UpdateItem {

    private String fieldName;
    private String value;
    private Operation operation;

    public UpdateItemImpl(String fieldName, String value, Operation operation) {
        super();
        this.fieldName = fieldName;
        this.value = value;
        this.operation = operation;
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
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

}
