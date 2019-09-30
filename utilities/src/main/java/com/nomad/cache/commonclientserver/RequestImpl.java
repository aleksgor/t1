package com.nomad.cache.commonclientserver;

import java.util.ArrayList;
import java.util.Collection;

import com.nomad.message.Request;
import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.update.UpdateRequest;

public class RequestImpl implements Request {
    private Collection<Model> models;
    private Collection<Identifier> identifiers;
    private Criteria<? extends Model> criteria;
    private Collection<UpdateRequest> updateRequests;

    public RequestImpl() {
        super();
    }

    public RequestImpl(Collection<Model> models, Collection<Identifier> identifiers, Criteria<? extends Model> criteria) {
        super();
        this.models = models;
        this.identifiers = identifiers;
        this.criteria = criteria;
    }
    public RequestImpl(Collection<UpdateRequest> updateRequests) {
        super();
        this.updateRequests = updateRequests;
    }
    public RequestImpl(UpdateRequest updateRequest) {
        super();
        this.updateRequests = new ArrayList<>();
        this.updateRequests.add(updateRequest);
    }

    @Override
    public Collection< Model> getModels() {
        return models;
    }

    @Override
    public void setModels(Collection<Model> models) {
        this.models = models;
    }

    @Override
    public Collection<Identifier> getIdentifiers() {
        return identifiers;
    }

    @Override
    public void setIdentifiers(Collection<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public Criteria<? extends Model> getCriteria() {
        return criteria;
    }

    @Override
    public void setCriteria(Criteria<? extends Model> criteria) {
        this.criteria = criteria;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((criteria == null) ? 0 : criteria.hashCode());
        result = prime * result + ((identifiers == null) ? 0 : identifiers.hashCode());
        result = prime * result + ((models == null) ? 0 : models.hashCode());
        result = prime * result + ((updateRequests == null) ? 0 : updateRequests.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RequestImpl other = (RequestImpl) obj;
        if (criteria == null) {
            if (other.criteria != null)
                return false;
        } else if (!criteria.equals(other.criteria))
            return false;
        if (identifiers == null) {
            if (other.identifiers != null)
                return false;
        } else if (!identifiers.equals(other.identifiers))
            return false;
        if (models == null) {
            if (other.models != null)
                return false;
        } else if (!models.equals(other.models))
            return false;
        if (updateRequests == null) {
            if (other.updateRequests != null)
                return false;
        } else if (!updateRequests.equals(other.updateRequests))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "RequestImpl [models=" + models + ", identifiers=" + identifiers + ", criteria=" + criteria + ", updateRequest=" + updateRequests + "]";
    }

    @Override
    public Collection<UpdateRequest> getUpdateRequest() {
        return updateRequests;
    }

    @Override
    public void setUpdateRequest(Collection<UpdateRequest> updateRequest) {
        this.updateRequests = updateRequest;
    }


}
