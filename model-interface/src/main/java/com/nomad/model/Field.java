package com.nomad.model;

public interface Field {


    enum Type {
        type_string,
        type_byte,
        type_int ,
        type_short,
        type_long ,
        type_float ,
        type_double ,
        type_date ,
        type_boolean,
        type_binary
    };

    int getLength();

    void setLength(int length);

    int getOrder();

    void setOrder(int order);

    boolean isIdentifier();

    void setIdentifier(boolean identifier);

    int getSelected();

    void setSelected(int selected);

    String getName();

    void setName(String name);

    RequestType getRequestType();

    void setRequestType(RequestType type);

    String getDataBaseName();

    void setDataBaseName(String dataBaseName);

    int getSqlType();

    void setSqlType(int sqlType);

    boolean isNumber();
}
