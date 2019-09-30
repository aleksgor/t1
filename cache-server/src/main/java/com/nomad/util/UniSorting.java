package com.nomad.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.core.ProxyProcessing;
import com.nomad.model.Model;
import com.nomad.model.SortField;
import com.nomad.model.SortField.Order;

public class UniSorting implements Comparator<Model> {
    private static Logger LOGGER = LoggerFactory.getLogger(ProxyProcessing.class);

    final List<SortField> sortOrder;

    public UniSorting(final List<SortField> sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(final Model o1, final Model o2) {

        for (final SortField order : sortOrder) {
            final String fieldName = order.getFieldName();
            try {
                final Method method = new PropertyDescriptor(fieldName, o1.getClass()).getReadMethod();
                final Object data1 = method.invoke(o1);
                final Object data2 = method.invoke(o2);
                final int result = compareObjects(data1, data2);
                if (result != 0) {
                    return Order.ASC.equals(order.getOrder())?result:(0-result);
                }
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return 0;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private int compareObjects(final Object o1, final Object o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            final Comparable c1 = (Comparable) o1;
            final Comparable c2 = (Comparable) o2;
            return c1.compareTo(c2);
        }
        if (o1 instanceof Collection && o2 instanceof Collection) {
            final Integer sz1 = ((Collection) o1).size();
            final Integer sz2 = ((Collection) o2).size();
            return sz1.compareTo(sz2);
        }
        if (o1 instanceof byte[] && o2 instanceof byte[]) {
            final Integer sz1 = ((byte[]) o1).length;
            final Integer sz2 = ((byte[]) o2).length;
            return sz1.compareTo(sz2);
        }
        LOGGER.info("Unknown type:" + o1 + " and " + o2 + " for compare");
        return 0;
    }
}
