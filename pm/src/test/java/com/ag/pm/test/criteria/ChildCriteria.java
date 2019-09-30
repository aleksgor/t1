package com.nomad.pm.test.criteria;

import com.nomad.model.criteria.AbstractCriteria;
import com.nomad.pm.test.models.Child;
import com.nomad.pm.test.models.ChildId;

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
