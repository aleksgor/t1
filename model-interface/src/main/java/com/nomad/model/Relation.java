package com.nomad.model;

import java.util.List;


public interface Relation {

    String getChildrenModel();

    void setChildrenModel(String childrenModel);

    String getParentModel();

    void setParentModel(String parentModel);

    String getName();

    void setName(String name);

    List<Condition> getConditions();

    Join getJoin();

    void setJoin(Join join);

    String getFieldName();

    void setFieldName(String fieldName);
}
