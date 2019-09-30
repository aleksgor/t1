package com.nomad.cache.commonclientserver;

import com.nomad.message.MessageHeader;
import com.nomad.message.RawMessage;
import com.nomad.message.Result;

public class RawMessageImpl implements RawMessage {

    private MessageHeader header;
    private byte[] body;
    private Result result = null;

    public RawMessageImpl() {
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

    public RawMessageImpl(MessageHeader header, byte[] body) {
        super();
        this.header = header;
        this.body = body;
    }

    public RawMessageImpl(MessageHeader header, byte[] body, Result result) {
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
    public byte[] getMessage() {
        return body;
    }

    @Override
    public void setMessage(byte[] message) {
        body = message;
    }

    @Override
    public String toString() {
        return "FullMessageImpl [header=" + header + ", message=" + body + ", result=" + result + "]";
    }

}
