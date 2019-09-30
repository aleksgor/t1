package com.nomad.exception;


public class InvalidOperationNameException extends SystemException {


  public InvalidOperationNameException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidOperationNameException(String message) {
    super(message);
  }

  public InvalidOperationNameException(Throwable cause) {
    super(cause);
  }


}
