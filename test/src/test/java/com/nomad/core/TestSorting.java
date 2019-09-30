package com.nomad.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.nomad.cache.client.CommonTest;
import com.nomad.cache.test.model.MainTestModel;
import com.nomad.cache.test.model.TestCriteria;
import com.nomad.model.SortField.Order;
import com.nomad.model.criteria.SortFieldImpl;
import com.nomad.server.ServerContextImpl;
import com.nomad.utility.SimpleServerContext;

public class TestSorting extends CommonTest {
    @Test
    public void testIdSortASC() throws Exception {
        TestCriteria criteria = new TestCriteria();
        criteria.addSortField(new SortFieldImpl(Order.ASC, TestCriteria.NAME, null));
        criteria.addSortField(new SortFieldImpl(Order.ASC, TestCriteria.ID, null));

        ProxyCacheManagerProcessing proxyProcessing = new ProxyCacheManagerProcessing(
                new SimpleServerContext(), null, null);
        List<MainTestModel> list = new ArrayList<>();
        list.add(getNewTestModel(1, "xxx6"));
        list.add(getNewTestModel(4, "xxx5"));
        list.add(getNewTestModel(2, "xxx5"));
        list.add(getNewTestModel(5, "xxx1"));

        proxyProcessing.sortTest(criteria, list);
        assertEquals(5, list.get(0).getId());
        assertEquals(2, list.get(1).getId());
        assertEquals(4, list.get(2).getId());
        assertEquals(1, list.get(3).getId());
    }

    @Test
    public void testIdSortDesc() throws Exception {
        TestCriteria criteria = new TestCriteria();
        criteria.addSortField(new SortFieldImpl(Order.DESC, TestCriteria.NAME, null));
        criteria.addSortField(new SortFieldImpl(Order.ASC, TestCriteria.ID, null));

        ProxyCacheManagerProcessing proxyProcessing = new ProxyCacheManagerProcessing(
                new ServerContextImpl(), null, null);
        List<MainTestModel> list = new ArrayList<>();
        list.add(getNewTestModel(1, "xxx6"));
        list.add(getNewTestModel(4, "xxx5"));
        list.add(getNewTestModel(2, "xxx5"));
        list.add(getNewTestModel(5, "xxx1"));

        proxyProcessing.sortTest(criteria, list);
        assertEquals(1, list.get(0).getId());
        assertEquals(2, list.get(1).getId());
        assertEquals(4, list.get(2).getId());
        assertEquals(5, list.get(3).getId());
    }
}
