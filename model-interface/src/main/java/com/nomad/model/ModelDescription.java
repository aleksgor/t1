package com.nomad.model;

import java.util.List;
import java.util.Map;

public interface ModelDescription {

    String getParentObject();

    void setParentObject(String parentObject);

    String getExtend();

    void setExtend(String extend);

    Map<String, Relation> getRelations();

    Map<String, Field> getFields();

    String getClassId();

    void setClassId(String classId);

    String getDataBaseName();

    void setDataBaseName(String dataBaseName);

    String getModelName();

    void setModelName(String nameModel);

    String getClazz();

    void setClazz(String clazz);

    Field getField(String name);

    Field getFieldByTableName(String name);

    List<Field> getPrimaryKeyFields();

    void addField(Field f);

    List<Field> getListFields();

    int getCountFields();

    void setData(ModelDescription t);

    Relation getRelationByName(String relationName);

}
