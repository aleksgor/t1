package com.nomad.cache.commonclientserver;

import com.nomad.message.ManagementMessage;
import com.nomad.message.Result;

public class ManagementMessageImpl implements ManagementMessage {

    private String command;
    private Object data;
    private Result result=null;
    private String modelName;

    public ManagementMessageImpl() {
        super();
    }


    @Override
    public Result getResult() {
        return result;
    }


    @Override
    public void setResult(final Result result) {
        this.result = result;
    }


    public ManagementMessageImpl(final String command, final Object data) {
        super();
        this.command = command;
        this.data=data;
    }
    public ManagementMessageImpl(final String command, final Object data, final Result result) {
        this(command,data);
        this.result = result;
    }


    @Override
    public String getModelName() {
        return modelName;
    }


    @Override
    public void setModelName(final String modelName) {
        this.modelName = modelName;
    }


    @Override
    public Object getData() {
        return data;
    }
    @Override
    public void setData(final Object data) {
        this.data = data;
    }

    @Override
    public int getResultCode() {
        return 0;
    }

    @Override
    public String getCommand() {
        return command;
    }


    @Override
    public void setCommand(final String command) {
        this.command = command;
    }


    @Override
    public String toString() {
        return "ManagementMessageImpl [command=" + command + ", data=" + data + ", result=" + result + ", modelName=" + modelName + "]";
    }



}
