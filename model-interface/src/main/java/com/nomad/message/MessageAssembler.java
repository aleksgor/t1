package com.nomad.message;

import java.io.InputStream;
import java.io.OutputStream;

import com.nomad.exception.SystemException;

public interface MessageAssembler {

  void storeObject(Object object, OutputStream output) throws  SystemException;

  Object getObject(InputStream is) throws  SystemException;

  long getInBytes();

  long getOutBytes();

  void reset();

}
