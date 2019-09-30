package com.nomad.model.criteria;

import java.util.ArrayList;
import java.util.List;

import com.nomad.model.CriteriaGroup;
import com.nomad.model.CriteriaItem;

public class CriteriaGroupItem implements CriteriaGroup{

    final private List<CriteriaItem> criteria = new ArrayList<>();

    @Override
    public CriteriaGroupItem addCriteriaItem(final CriteriaItem item){
        criteria.add(item);
        return this;
    }
    @Override
    public List<CriteriaItem> getCriteria() {
        return criteria;
    }


}
