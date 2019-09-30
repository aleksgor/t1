package com.nomad.message;

public interface FullMessage {
    MessageHeader getHeader();

    void setHeader(MessageHeader header);

    Result getResult();

    void setResult(Result result);

    Body getBody();

    void setMessage(Body message);

}
