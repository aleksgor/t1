package com.nomad.model.update;

import java.util.Collection;


public interface UpdateRequest {
    
    public String getModelName();

    public void setModelName(String modelName);

    public Collection<UpdateItem> getUpdateItems();

}
