package com.nomad.store.transaction;


import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.server.transaction.TransactElement;


public class TransactElementImpl implements TransactElement
{

    private Model newModel;
    private Model oldModel;
    private Identifier id;
    private Operation operation;
    private final String sessionId;
    private boolean phase2=false;


    public TransactElementImpl(final Model newModel, final Operation operation, final String sessionId)
    {
        this.newModel=newModel;
        this.operation = operation;
        this.sessionId=sessionId;
    }
    public TransactElementImpl( final Identifier id, final Operation operation,final String sessionId)
    {
        this.id=id;
        this.operation = operation;
        this.sessionId=sessionId;
    }

    @Override
    public Model getNewModel() {
        return newModel;
    }

    @Override
    public void setNewModel(final Model newModel) {
        this.newModel = newModel;
    }


    @Override
    public Operation getOperation() {
        return operation;
    }

    @Override
    public void setOperation(final Operation operation) {
        this.operation = operation;
    }

    @Override
    public Identifier getIdentifier(){
        if(newModel!=null){
            return newModel.getIdentifier();
        }
        return id;
    }

    @Override
    public Model getOldModel() {
        return oldModel;
    }
    @Override
    public void setOldModel(final Model oldModel) {
        this.oldModel = oldModel;
    }
    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean isPhase2() {
        return  phase2;
    }
    @Override
    public void setPhase2(final boolean ph2) {
        phase2 = ph2;
    }
    @Override
    public String toString() {
        return "TransactElementImpl [newModel=" + newModel + ", oldModel=" + oldModel + ", id=" + id + ", operation=" + operation + ", sessionId=" + sessionId + ",  phase2"+phase2;
    }




}
