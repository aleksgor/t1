package com.nomad.message;

import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticResult;

public interface Body {

    StatisticResult<? extends Model> getResponse();

    void setResponse(StatisticResult<? extends Model> response);

    Request getRequest();

    void setRequest(Request request);
    
    void cleanRequest();
    
    void cleanResponce();

}
