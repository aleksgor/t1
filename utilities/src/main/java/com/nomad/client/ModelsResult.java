package com.nomad.client;

import java.util.ArrayList;
import java.util.List;

import com.nomad.model.Model;

public class ModelsResult extends AbstractResult{
    private final List<Model> models= new ArrayList<>();

    public List<Model> getModels() {
        return models;
    }

}
