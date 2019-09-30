package com.nomad.message;


public interface RawMessage  {

    MessageHeader getHeader();

    void setHeader(MessageHeader header);

    Result getResult();

    void setResult(Result result);

    byte[] getMessage();

    void setMessage(byte[] message);

}
