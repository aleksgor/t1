package com.nomad.exception;


public class InvalidModelNameException extends SystemException {


  public InvalidModelNameException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidModelNameException(String message) {
    super(message);
  }

  public InvalidModelNameException(Throwable cause) {
    super(cause);
  }


}
