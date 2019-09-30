package com.nomad.datadefinition;

import java.io.Serializable;

import com.nomad.model.Field;
import com.nomad.model.RequestType;

/**
 * @author alexgor Date: 06.01.2005 Time: 12:50:52
 */
public class FieldImpl implements Field, Serializable {

    private RequestType requestType = RequestType.UNDEFINED;
    private int sqlType = -1;
    private String name = "";
    private String dataBaseName = "";
    private int selected = 0;
    private boolean identifier = false;
    private int order = -1;
    private int length = 0;
    private Type type;

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean isIdentifier() {
        return identifier;
    }

    @Override
    public void setIdentifier(boolean identifier) {
        this.identifier = identifier;
    }

    @Override
    public int getSelected() {
        return selected;
    }

    @Override
    public void setSelected(int selected) {
        this.selected = selected;
    }

    public FieldImpl(String name, String dataBaseName, RequestType type) {
        this.name = name;
        this.requestType = type;
        this.dataBaseName = dataBaseName;
    }

    public FieldImpl(String name, String dataBaseName, RequestType type, int selected) {
        this(name, dataBaseName, type);
        this.selected = selected;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public RequestType getRequestType() {
        return requestType;
    }

    @Override
    public void setRequestType(RequestType type) {
        this.requestType = type;
    }

    @Override
    public String getDataBaseName() {
        return dataBaseName;
    }

    @Override
    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

    @Override
    public int getSqlType() {
        return sqlType;
    }

    @Override
    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean isNumber() {
        if (requestType == null) {
            return false;
        }
        return requestType.isNumber();
    }

    @Override
    public String toString() {
        return "FieldImpl [requestType=" + requestType + ", sqlType=" + sqlType + ", name=" + name + ", dataBaseName=" + dataBaseName + ", selected=" + selected + ", identifier="
                + identifier + ", order=" + order + ", length=" + length + ", type=" + type + "]";
    }

}
