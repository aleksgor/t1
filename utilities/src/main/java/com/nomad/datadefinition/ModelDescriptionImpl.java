package com.nomad.datadefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nomad.model.Field;
import com.nomad.model.ModelDescription;
import com.nomad.model.Relation;

/**
 * @author alexgor Date: 10.01.2005 Time: 11:36:05
 */
public class ModelDescriptionImpl implements ModelDescription, Serializable {

    private String modelName = "";
    private String dataBaseName = "";
    private String classId = "";
    private String clazz = "";
    private String extend = "";
    private String parentObject = "";
    private final Map<String, Field> fields = new HashMap<>(); // content

    private final Map<String, Relation> relations = new HashMap<>();
    private final List<Field> primaryKeyFields = new ArrayList<>();

    @Override
    public String getParentObject() {
        return parentObject;
    }

    @Override
    public void setParentObject(String parentObject) {
        if (parentObject != null)
            if (parentObject.length() == 0)
                parentObject = null;
        this.parentObject = parentObject;
    }

    @Override
    public String getExtend() {
        return extend;
    }

    @Override
    public void setExtend(String extend) {
        this.extend = extend;
    }

    @Override
    public Map<String, Relation> getRelations() {
        return relations;
    }

    @Override
    public Map<String, Field> getFields() {
        return fields;
    }

 

    @Override
    public String getClassId() {
        return classId;
    }

    @Override
    public void setClassId(String classId) {
        this.classId = classId;
    }

    @Override
    public String getDataBaseName() {
        return dataBaseName;
    }

    @Override
    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public void setModelName(String nameModel) {
        this.modelName = nameModel;
    }

    @Override
    public String getClazz() {
        return clazz;
    }

    @Override
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    @Override
    public Field getField(String name) {
        return fields.get(name);

    }

    @Override
    public Field getFieldByTableName(String name) {
        for (Field field : fields.values()) {
            if (field.getDataBaseName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public List<Field> getPrimaryKeyFields() {
        return primaryKeyFields;
    }

    @Override
    public void addField(Field f) {
        fields.put(f.getName(), f);
        if (f.isIdentifier()) {
            primaryKeyFields.add(f);
        }
    }

    @Override
    public List<Field> getListFields() {
        return new ArrayList<>(fields.values());
    }

    @Override
    public String toString() {
        return "Table{ classId='" + classId + "'" + ", nameModel='" + modelName + "'" + ", dataBaseName='" + dataBaseName + "'" + ", fields=" + fields
                + ", relations=" + relations + "}";
    }

    @Override
    public int getCountFields() {
        return fields.size();
    }

    @Override
    public void setData(ModelDescription description) {
        setModelName(description.getModelName());
        setDataBaseName(description.getDataBaseName());
        setClazz(description.getClazz());
        setClassId(description.getClassId());
        setExtend(description.getExtend());

        fields.putAll(description.getFields());
        relations.putAll(description.getRelations());
        primaryKeyFields.addAll(description.getPrimaryKeyFields());

    }
    
  


    @Override
    public Relation getRelationByName(String relationName) {
        return relations.get(relationName);
    }

}
