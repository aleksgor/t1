package com.nomad.cache.test.model;

import com.nomad.model.criteria.AbstractCriteria;

public class ChildCriteria extends AbstractCriteria<Child> {


  public static String ID = "id";
  public static String NAME = "name";

  public ChildCriteria() {
    super();
  }

  @Override
public String getModelName() {
    return new ChildId().getModelName();
  }


}
