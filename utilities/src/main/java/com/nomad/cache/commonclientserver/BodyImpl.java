package com.nomad.cache.commonclientserver;

import java.util.Collection;

import com.nomad.message.Body;
import com.nomad.message.Request;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.criteria.StatisticResultImpl;
import com.nomad.model.update.UpdateRequest;

public class BodyImpl implements Body {

    private Request request;
    private StatisticResult<? extends Model> response;

    public BodyImpl(final Collection<Model> models, final Collection<Identifier> identifiers, final Criteria<? extends Model> criteria) {
        super();
        request = new RequestImpl(models, identifiers, criteria);
    }


    public BodyImpl(UpdateRequest updateRequest) {
        super();
        request = new RequestImpl(updateRequest);
    }

    public BodyImpl(Collection<UpdateRequest> updateRequests) {
        super();
        request = new RequestImpl(updateRequests);
    }

    public BodyImpl(StatisticResult<? extends Model> response) {
        super();
        this.response = response;
    }

    public BodyImpl(Request request) {
        super();
        this.request = request;
    }

    public BodyImpl() {

    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    public void setRequest(Request request) {
        this.request = request;
    }

    @Override
    public StatisticResult<? extends Model> getResponse() {
        return response;
    }

    @Override
    public void setResponse(StatisticResult<? extends Model> response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "BodyImpl [request=" + request + ", response=" + response + "]";
    }


    @Override
    public void cleanRequest() {
        request = new RequestImpl();
        
    }


    @Override
    public void cleanResponce() {
        response= new StatisticResultImpl<>();
        
    }



}
