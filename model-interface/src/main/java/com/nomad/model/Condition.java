package com.nomad.model;

public interface Condition {
    String getParentFieldName();

    void setParentFieldName(String parentFieldName);

    String getChildFieldName();

    void setChildFieldName(String childFieldName);

}
