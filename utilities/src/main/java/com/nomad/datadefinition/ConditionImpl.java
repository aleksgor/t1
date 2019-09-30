package com.nomad.datadefinition;

import com.nomad.model.Condition;

public class ConditionImpl implements Condition{

  private String parentFieldName;
  
  private String childFieldName;

  public String getParentFieldName() {
    return parentFieldName;
  }

  public void setParentFieldName(String parentFieldName) {
    this.parentFieldName = parentFieldName;
  }

  public String getChildFieldName() {
    return childFieldName;
  }

  public void setChildFieldName(String childFieldName) {
    this.childFieldName = childFieldName;
  }


  @Override
  public String toString() {
    return "RelationItem [parentFieldName=" + parentFieldName + ", childFieldName=" + childFieldName  + "]";
  }

}
