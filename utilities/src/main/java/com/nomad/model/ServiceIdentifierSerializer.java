package com.nomad.model;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class ServiceIdentifierSerializer implements Serializer<ServiceIdentifier> {

  public void write(MessageOutputStream out, ServiceIdentifier data) throws IOException,SystemException {
    
    out.writeObject(data.getOriginal());
  
  }

  public ServiceIdentifier read(MessageInputStream input) throws IOException ,SystemException{
    Identifier original=(Identifier) input.readObject();
    return new ServiceIdentifier(original);
  }

}
