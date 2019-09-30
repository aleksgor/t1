package com.nomad.model.criteria;


import com.nomad.model.SortField;

public class SortFieldImpl implements SortField {


    private SortField.Order order = SortField.Order.ASC;
    private String fieldName;
    private String relationName;


    public SortFieldImpl()   {
    }

    /**
     * create new SortField
     *
     * @param order     ASC, DESC
     * @param paramName name of feld of model
     * @param mName     name model
     */
    public SortFieldImpl(SortField.Order order, String fieldName, String relationName)
    {
        this.order = order;
        this.fieldName = fieldName;
        this.relationName = relationName;
    }

    @Override
    public SortField.Order getOrder()
    {
        return order;
    }

    @Override
    public void setOrder(SortField.Order order)
    {
        this.order = order;
    }

    @Override
    public void reverseOrder()
    {
        if (SortField.Order.ASC.equals(order))
            order = SortField.Order.DESC;
        else
            order = SortField.Order.ASC;
    }


    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }


    @Override
    public String getRelationName() {
        return relationName;
    }

    @Override
    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    @Override
    public String toString() {
        return "SortFieldImpl [order=" + order + ", fieldName=" + fieldName + ", relationName=" + relationName + "]";
    }

}
