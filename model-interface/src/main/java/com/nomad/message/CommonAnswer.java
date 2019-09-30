package com.nomad.message;

public interface CommonAnswer {
  /**
   *  0 means "ok", negative result- error, positive - message executed successfully with warning.
   * @return
   */
  
    int getResultCode();
}
