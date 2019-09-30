package com.nomad.model;

import java.io.Serializable;
import java.math.BigInteger;

public interface Identifier extends Serializable{

    String getModelName();

    void setGeneratedId(BigInteger newCode);
}
