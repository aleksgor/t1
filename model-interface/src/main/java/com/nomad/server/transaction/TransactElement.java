package com.nomad.server.transaction;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public interface TransactElement {

    public enum Operation {
        UPDATE_MODEL, DELETE_MODEL, ADD_MODEL, EMPTY_OPERATION
    }

    Model getNewModel();

    void setNewModel(Model newModel);

    Operation getOperation();

    void setOperation(Operation operation);

    Identifier getIdentifier();

    String getSessionId();

    boolean isPhase2();

    void setPhase2(final boolean phase2);

    Model getOldModel();

    void setOldModel(Model oldModel);

}
