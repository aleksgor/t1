package com.nomad.pm.test.criteria;


import com.nomad.model.criteria.AbstractCriteria;
import com.nomad.pm.test.models.MainClass;
import com.nomad.pm.test.models.TestId;

public class TestCriteria extends AbstractCriteria<MainClass> {

    public static String ID = "mainId";
    public static String NAME = "mainName";
    public static String DATE = "mainDate";
    public static String CHILDNAME = "name";
    public static String fakeName = "fakeName";

    public static String RELATIONR = "rChild";
    public static String RELATIONS = "sChild";
    public static String RELATIONO = "oChild";
    public static String fakeRelation = "fakeRelation";
    public static String fakechildName = RELATIONR + ".fakechildName";

    public static String CHILD_ID = RELATIONR + "." + ChildCriteria.ID;
    public static String CHILD_NAME = RELATIONR + "." + ChildCriteria.NAME;

    public static String SECOND_CHILD_ID = RELATIONS + "." + ChildCriteria.ID;
    public static String SECOND_CHILD_NAME = RELATIONS + "." + ChildCriteria.NAME;

    public static String THIRD_CHILD_ID = RELATIONO + "." + ChildCriteria.ID;
    public static String THIRD_CHILD_NAME = RELATIONO + "." + ChildCriteria.NAME;

    public TestCriteria() {
        super();
    }


    @Override
    public String getModelName() {
        return new TestId().getModelName();
    }


}
