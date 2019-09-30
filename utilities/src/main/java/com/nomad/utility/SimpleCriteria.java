package com.nomad.utility;

import com.nomad.model.CriteriaItem;
import com.nomad.model.Model;
import com.nomad.model.criteria.AbstractCriteria;


public class SimpleCriteria extends AbstractCriteria<Model> {
    private String modelName;

    @Override
    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public void setFieldValue(Object data){ 
        if(!getCriteria().isEmpty()){
            for (CriteriaItem item : getCriteria()) {
                item.setFieldValue(data);
            }
        }
    }

}
