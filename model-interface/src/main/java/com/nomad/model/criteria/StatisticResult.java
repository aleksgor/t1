package com.nomad.model.criteria;

import java.util.Collection;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public interface StatisticResult<T extends Model> {

    Collection<StatisticElement> getStatistics();

    void setStatistics(Collection<StatisticElement> statistics);

    Collection<T> getResultList();

    void setResultList(Collection<T> resultList);

    int getPageSize();

    void setPageSize(int pageSize);

    long getCountAllRow();

    void setCountAllRow(long countAllRow);

    Collection<Identifier> getIdentifiers();

    void setIdentifiers(Collection<Identifier> identifiers);

    long getStartPosition();

    void setStartPosition(long startPosition);

    Collection<StatisticElement> getGroups();

    void setGroups(Collection<StatisticElement> groups);
}
