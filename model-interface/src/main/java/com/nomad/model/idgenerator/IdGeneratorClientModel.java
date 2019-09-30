package com.nomad.model.idgenerator;

import java.util.Set;

import com.nomad.model.CommonClientModel;

public interface IdGeneratorClientModel extends CommonClientModel{

    Set<String> getModelNames();

}
