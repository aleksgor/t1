package com.nomad.pm.test.models;

import java.math.BigInteger;

import com.nomad.model.Identifier;

public class ChildId implements Identifier {

    private static String MODELNAME = "Child";
    private long id;

    public ChildId(long id) {
        super();
        this.id = id;
    }

    public ChildId() {
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
        ChildId other = (ChildId) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChildId [id=" + id + "]";
    }

    @Override
    public void setGeneratedId(BigInteger newCode) {
        id = newCode.longValue();
    }


}
