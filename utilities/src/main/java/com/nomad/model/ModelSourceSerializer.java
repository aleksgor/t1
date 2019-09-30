package com.nomad.model;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class ModelSourceSerializer implements Serializer<ModelSource> {

  public void write(MessageOutputStream out, ModelSource data) throws IOException,SystemException {
    out.writeObject(data.getDataSourceModel());
    out.writeObject( data.getStoreModel());
  
  }

  public ModelSource read(MessageInputStream input) throws IOException,SystemException {
    ModelSource result = new ModelSource();
    result.setDataSourceModel((DataSourceModelImpl) input.readObject());
    result.setStoreModel((StoreModelImpl) input.readObject());
    
    return result;
  }

}
