package com.nomad.cache.commonclientserver;

import com.nomad.message.Body;
import com.nomad.message.FullMessage;
import com.nomad.message.MessageHeader;
import com.nomad.message.Result;

public class FullMessageImpl implements FullMessage {

    private MessageHeader header;
    private Body message;
    private Result result = null;

    public FullMessageImpl() {
        super();
    }

    @Override
    public Result getResult() {
        return result;
    }

    @Override
    public void setResult(Result result) {
        this.result = result;
    }

    public FullMessageImpl(MessageHeader header, Body message) {
        super();
        this.header = header;
        this.message = message;
    }

    public FullMessageImpl(MessageHeader header, Body body, Result result) {
        this(header, body);
        this.result = result;
    }

    @Override
    public MessageHeader getHeader() {
        return header;
    }

    @Override
    public void setHeader(MessageHeader header) {
        this.header = header;
    }

    @Override
    public Body getBody() {
        return message;
    }

    @Override
    public void setMessage(Body message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "FullMessageImpl [header=" + header + ", message=" + message + ", result=" + result + "]";
    }

}
