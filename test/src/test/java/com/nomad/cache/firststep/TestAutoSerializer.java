package com.nomad.cache.firststep;

import java.util.Date;

import org.junit.BeforeClass;

import com.nomad.cache.test.model.MainTestModel;
//import static org.junit.Assert.*;
import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.exception.SystemException;
import com.nomad.server.DataDefinitionService;

public class TestAutoSerializer {

    private final String name="askfalkvbasdvb,asd cgaksjdhg caskjehfgaksjeh fqslkjfgslkhgv asdjrhgvqjr hfjrewhfg ajskdhvcaskjdcvaksjdvckasjDVSDKVJSKDVSKVVBJHghggjhgkjhgkjhgkfgk";
    @BeforeClass
    public static void init() throws Exception  {
        final DataDefinitionService dataDefinitionService = new DataDefinitionServiceImpl(null, "model.xml", null);
        dataDefinitionService.start();
    }
    @org.junit.Test
    public void test1() throws SystemException{

        final Date date=new Date();

        final MainTestModel test= new MainTestModel();
        test.setChildId(1);
        test.setDate(date);
        test.setId(8);
        test.setName(name);

    }
}
