package com.nomad.client;

import java.util.ArrayList;
import java.util.List;

import com.nomad.model.Identifier;

public class IdentifiersResult extends AbstractResult{
    private final List<Identifier> identifiers= new ArrayList<>();

    public List<Identifier> getIdentifiers() {
        return identifiers;
    }


}
