package com.nomad.exception;


public class InvalidSessionException extends SystemException {


  public InvalidSessionException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidSessionException(String message) {
    super(message);
  }

  public InvalidSessionException(Throwable cause) {
    super(cause);
  }


}
