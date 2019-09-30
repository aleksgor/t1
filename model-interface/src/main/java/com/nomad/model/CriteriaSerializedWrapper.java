package com.nomad.model;

import java.util.List;

/**
 * interface for criteria all criteria
 *
 * @author alexgor Date: 06.01.2005 Time: 12:38:41
 */
public interface CriteriaSerializedWrapper<T extends Model> extends Criteria<T> {

    @Override
    List<CriteriaItem> getCriteria();

    void setCountAllRow(long countRow);

    void setSelectedPosition(int selectedPosition);

}