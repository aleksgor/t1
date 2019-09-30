package com.nomad.model;

import java.util.ArrayList;
import java.util.List;

import com.nomad.model.saveserver.SaveClientModel;
import com.nomad.model.saveserver.SaveServerModel;

public class SaveServerModelImpl extends CommonServerModelImpl implements SaveServerModel {

    private long sessionTimeout=1000;

    private final List<SaveClientModel>mirrors= new ArrayList<>();

    @Override
    public List<SaveClientModel> getMirrors() {
        return mirrors;
    }

    @Override
    public long getSessionTimeout() {
        return sessionTimeout;
    }

    @Override
    public void setSessionTimeout(final long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (sessionTimeout ^ (sessionTimeout >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SaveServerModelImpl other = (SaveServerModelImpl) obj;
        if (sessionTimeout != other.sessionTimeout)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SaveServerModelImpl ["+super.toString()+ "]";
    }




}
