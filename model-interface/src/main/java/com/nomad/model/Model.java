package com.nomad.model;

import java.io.Serializable;

public interface Model extends Serializable{

    Identifier getIdentifier();

    void setIdentifier(Identifier id);

    String getModelName();

}
