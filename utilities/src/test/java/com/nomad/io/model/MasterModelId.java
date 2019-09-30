package com.nomad.io.model;

import java.io.Serializable;
import java.math.BigInteger;

import com.nomad.model.Identifier;

public class MasterModelId implements Identifier, Serializable {

    private static String MODELNAME = "MasterModel";
    private long id;

    public MasterModelId(long id) {
        super();
        this.id = id;
    }

    public MasterModelId() {
        super();
    }

    @Override
    public String getModelName() {
        return MODELNAME;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MasterModelId other = (MasterModelId) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TestId[ id:" + id + "]";
    }

    @Override
    public void setGeneratedId(BigInteger newCode) {
        id = newCode.longValue();
    }

}
