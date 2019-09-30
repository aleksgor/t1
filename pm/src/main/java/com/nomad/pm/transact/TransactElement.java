package com.nomad.pm.transact;

import java.io.Serializable;

import com.nomad.model.Model;

//TODO <? extends Model>
public class TransactElement implements Serializable
{
    public enum Operation{
        INSERT_MODEL, UPDATE_MODEL, DELETE_MODEL,SELECT_MODEL
    }

    private Model oldModel;
    private Model newModel;

    private Operation operation;

    public TransactElement(Model model, Operation operation)
    {
        oldModel = model;
        this.operation = operation;
    }
    public TransactElement(Model oldModel, Model newModel, Operation operation)
    {
        this.oldModel = oldModel;
        this.newModel=newModel;
        this.operation = operation;
    }

    public Model getNewModel() {
        return newModel;
    }

    public void setNewModel(Model newModel) {
        this.newModel = newModel;
    }

    public Model getOldModel() {
        return oldModel;
    }

    public void setOldModel(Model oldModel) {
        this.oldModel = oldModel;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @Override
    public String toString(){
        return " TransactElement:{ oldModel:"+oldModel+
                " newModel:"+newModel+" operation:"+operation;

    }
}
