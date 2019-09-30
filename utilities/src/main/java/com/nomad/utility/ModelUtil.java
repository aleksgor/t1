package com.nomad.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public class ModelUtil {
    public static  Map<Identifier, Model> convertToMap(Collection<? extends Model> models) {
        Map<Identifier, Model> result = new HashMap<>(models.size(), 1);
        for (Model model : models) {
            result.put(model.getIdentifier(), model);
        }
        return result;
    }

    public static Collection<Identifier> getIdentifiers(Collection<? extends Model> models) {
        return (Collection<Identifier>) models.stream().parallel().map(Model::getIdentifier).collect(Collectors.toCollection(ArrayList::new));
    }

}
