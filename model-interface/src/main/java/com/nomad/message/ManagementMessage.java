package com.nomad.message;

public interface ManagementMessage extends CommonMessage, CommonAnswer{

    public Result getResult();

    public void setResult(Result result) ;

    public Object getData();

    public void setData(Object data);

    void setCommand(String command);

    String getCommand();

    void setModelName(String modelName);

    String getModelName();

}
