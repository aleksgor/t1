package com.nomad.cache.test.model;

import java.math.BigInteger;

import com.nomad.model.Identifier;

public class PriceId implements Identifier {

    public static String MODEL_NAME = "Price";
    private long id;

    public PriceId(long id) {
        super();
        this.id = id;
    }

    public PriceId() {
        super();
    }

    @Override
    public String getModelName() {
        return MODEL_NAME;
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
        PriceId other = (PriceId) obj;
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
