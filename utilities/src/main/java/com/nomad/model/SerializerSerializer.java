package com.nomad.model;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class SerializerSerializer implements Serializer<com.nomad.model.SerializerImpl> {

  public void write(MessageOutputStream out, com.nomad.model.SerializerImpl data) throws IOException {
    out.writeString(data.getClazz());
    out.writeString(data.getSerializerClazz());
    
  }

  public com.nomad.model.SerializerImpl read(MessageInputStream input) throws IOException {
    com.nomad.model.SerializerImpl result = new com.nomad.model.SerializerImpl();
    
    result.setClazz(input.readString());
    result.setSerializerClazz(input.readString());
    return result;
  }
 
  
}
