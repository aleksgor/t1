package com.nomad.model;

import java.io.Serializable;

/**
 * @author alexgor
 *         Date: 06.01.2005
 *         Time: 14:14:42
 */
public interface SortField extends Serializable{

    public enum Order{
        ASC,DESC
    }

    Order getOrder();

    void setOrder(Order order);

    void reverseOrder();

    String getFieldName();

    void setFieldName(String fieldName);

    String getRelationName();

    void setRelationName(String relationName);

}
