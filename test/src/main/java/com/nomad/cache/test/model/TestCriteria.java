package com.nomad.cache.test.model;


import com.nomad.model.criteria.AbstractCriteria;

public class TestCriteria extends AbstractCriteria<MainTestModel>{

    public static String ID="id";
    public static String NAME="name";
    public static String DATE="date";
    public static String CHILDNAME="name";
    public static String CHILD_ID = "childId";
    public static String SECOND_CHILD_ID = "secondChildId";
    public static String MONEY = "money";
    public static String PRICE_RELATION="priceRelation";
    public static String CHILD_1_RELATION = "rChild";
    public static String CHILD_2_RELATION = "sChild";


    public TestCriteria(){
        super();
    }
    @Override
    public String getModelName(){
        return new MainTestModelId().getModelName();
    }

}
