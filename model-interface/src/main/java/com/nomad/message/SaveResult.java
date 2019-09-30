package com.nomad.message;

import java.util.Collection;

import com.nomad.model.Identifier;

public interface SaveResult extends CommonAnswer {

    Collection<Identifier> getAllowedIds();

    void setAllowedIds(Collection<Identifier> deniedIds);

    void setResultCode(int resultCode);

}
