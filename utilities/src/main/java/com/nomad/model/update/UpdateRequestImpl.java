package com.nomad.model.update;

import java.util.ArrayList;
import java.util.Collection;

public class UpdateRequestImpl implements UpdateRequest {
    private String modelName;
    private final Collection<UpdateItem> updateItems= new ArrayList<>();

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }


    @Override
    public Collection<UpdateItem> getUpdateItems() {
        return updateItems;
    }

}
