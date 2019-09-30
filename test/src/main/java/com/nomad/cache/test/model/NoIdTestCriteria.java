package com.nomad.cache.test.model;

import com.nomad.model.criteria.AbstractCriteria;

public class NoIdTestCriteria extends AbstractCriteria<NoIdTestModel> {

    public static String ID="id";
    public static String NAME="name";
    public static String DATE="date";
    public static String CHILDNAME="name";
    public static String CHILD_ID = "childId";
    public static String SECOND_CHILD_ID = "secondChildId";
    public static String MONEY = "money";


    public NoIdTestCriteria() {
        super();
    }
    @Override
    public String getModelName(){
        return new NoIdTestModelId().getModelName();
    }

}
