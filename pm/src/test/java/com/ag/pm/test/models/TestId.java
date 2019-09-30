package com.nomad.pm.test.models;

import java.io.Serializable;
import java.math.BigInteger;

import com.nomad.model.Identifier;

public class TestId implements Identifier, Serializable {

    private static String MODELNAME = "Test";
    private long mainId;

    public TestId(long id) {
        super();
        this.mainId = id;
    }

    public TestId() {
        super();
    }

    @Override
    public String getModelName() {
        return MODELNAME;
    }

    public long getMainId() {
        return mainId;
    }

    public void setMainId(long mainId) {
        this.mainId = mainId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (mainId ^ (mainId >>> 32));
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
        TestId other = (TestId) obj;
        if (mainId != other.mainId)
            return false;
        return true;
    }


    @Override
    public void setGeneratedId(BigInteger newCode) {
        mainId = newCode.longValue();

    }

}
