package com.nomad.pm.generator;

import com.nomad.model.RequestType;

public class ParameterField {

    private RequestType type;
    private Object value;

    public ParameterField(RequestType type, Object value) {
        super();
        this.type = type;
        this.value = value;
    }

    public RequestType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ParamField [type=" + type + ", value=" + value + "]";
    }

}
