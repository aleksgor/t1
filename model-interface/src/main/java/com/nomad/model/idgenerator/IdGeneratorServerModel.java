package com.nomad.model.idgenerator;

import java.util.Map;

import com.nomad.model.CommonServerModel;

public interface IdGeneratorServerModel  extends CommonServerModel{

     int getIncrement();

     void setIncrement(int increment);

     int getTimeOut();

     void setTimeOut(int timeOut);

    Map<String, String> getModelSource();
    
    Map<String, String> getProperties() ;

    void setInvokerClass(String invokerClass);

    String getInvokerClass();

}
