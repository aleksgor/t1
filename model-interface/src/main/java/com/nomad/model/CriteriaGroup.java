package com.nomad.model;

import java.util.List;

public interface CriteriaGroup {


    CriteriaGroup addCriteriaItem(CriteriaItem item);

    List<CriteriaItem> getCriteria();


}
