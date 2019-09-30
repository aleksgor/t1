package com.nomad.datadefinition;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.nomad.model.Condition;
import com.nomad.model.Join;
import com.nomad.model.Relation;

/**
 * @author alexgor Date: 10.01.2005 Time: 12:19:53
 */
public class RelationImpl implements Relation, Serializable {

    private String parentModel = null;
    private String childrenModel = null;
    private String fieldName=null;
    private String name = "";
    private Join join = Join.INNER;
    private final List<Condition> conditions = new ArrayList<>();

    @Override
    public String getChildrenModel() {
        return childrenModel;
    }

    @Override
    public void setChildrenModel(String childrenModel) {
        this.childrenModel = childrenModel;
    }

    @Override
    public String getParentModel() {
        return parentModel;
    }

    @Override
    public void setParentModel(String parentModel) {
        this.parentModel = parentModel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<Condition> getConditions() {
        return conditions;
    }


    @Override
    public Join getJoin() {
        return join;
    }

    @Override
    public void setJoin(Join join) {
        this.join = join;
    }

    @Override
    public String getFieldName() {
        if(fieldName==null){
            return childrenModel;
        }
        return fieldName;
    }

    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((childrenModel == null) ? 0 : childrenModel.hashCode());
        result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime * result + ((join == null) ? 0 : join.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parentModel == null) ? 0 : parentModel.hashCode());
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
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
        RelationImpl other = (RelationImpl) obj;
        if (childrenModel == null) {
            if (other.childrenModel != null)
                return false;
        } else if (!childrenModel.equals(other.childrenModel))
            return false;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        if (join != other.join)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parentModel == null) {
            if (other.parentModel != null)
                return false;
        } else if (!parentModel.equals(other.parentModel))
            return false;
        if (conditions == null) {
            if (other.conditions != null)
                return false;
        } else if (!conditions.equals(other.conditions))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RelationImpl [parentModel=" + parentModel + ", childrenModel=" + childrenModel + ", fieldName=" + fieldName + ", name=" + name + ", join=" + join
                + ", conditions=" + conditions + "]";
    }


}
