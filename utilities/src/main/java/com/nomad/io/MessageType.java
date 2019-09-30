package com.nomad.io;

public enum MessageType {

  
  TYPE_NULL((byte) 0), TYPE_INT((byte) 1), TYPE_FLOAT((byte) 2), TYPE_DOUBLE((byte) 3), TYPE_BIG_DECIMAL((byte) 4), TYPE_DATE_TIME((byte) 5), 
  TYPE_BOOLEAN((byte) 6), TYPE_STRING((byte) 7), TYPE_BYTES((byte) 8), TYPE_LIST((byte) 9), TYPE_MAP((byte) 10), 
  TYPE_SERIALIZABLE( (byte) 11),
  TYPE_MANUAL_SERIALIZABLE((byte) 12), TYPE_LONG((byte) 13),
  TYPE_MODEL((byte) 14),TYPE_IDENTIFIER((byte) 15),
  TYPE_ARRAY_INT((byte) 16), TYPE_ARRAY_STRING((byte) 17),TYPE_ARRAY_LONG((byte) 18), TYPE_SHORT((byte) 19), TYPE_BYTE((byte) 20),
  TYPE_ENUM((byte) 21);
  
  private byte code;
  
  private MessageType(byte cd) {
    code = cd;
  }

  public byte getCode() {
    return code;
  }
}
