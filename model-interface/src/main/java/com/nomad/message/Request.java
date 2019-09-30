package com.nomad.message;

import java.util.Collection;

import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.update.UpdateRequest;

public interface Request {

    public Collection<Model> getModels();

    public void setModels(Collection< Model> models);

    public Collection<Identifier> getIdentifiers();

    public void setIdentifiers(Collection<Identifier> identifiers);

    public Criteria<? extends Model> getCriteria();

    public void setCriteria(Criteria<? extends Model> criteria);

    void setUpdateRequest(Collection<UpdateRequest> updateRequest);

    Collection<UpdateRequest> getUpdateRequest();
}
