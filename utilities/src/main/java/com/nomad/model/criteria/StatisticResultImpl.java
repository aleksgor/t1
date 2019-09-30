package com.nomad.model.criteria;

import java.util.ArrayList;
import java.util.Collection;

import com.nomad.model.Identifier;
import com.nomad.model.Model;

public class StatisticResultImpl<T extends Model> implements StatisticResult<T> {

    private Collection<StatisticElement> statistics;
    private Collection<StatisticElement> groups;
    private Collection<T> resultList = new ArrayList<>();
    private long startPosition = 0;
    private int pageSize = -1;
    private long countAllRow = 0;
    private Collection<Identifier> identifiers = new ArrayList<>();

    public StatisticResultImpl() {
    }
    public StatisticResultImpl(Collection<T> resultList) {
        super();
        this.resultList = resultList;
        countAllRow = resultList.size();
    }

    @Override
    public Collection<StatisticElement> getGroups() {
        return groups;
    }

    @Override
    public void setGroups(Collection<StatisticElement> groups) {
        this.groups = groups;
    }
    @Override
    public Collection<StatisticElement> getStatistics() {
        return statistics;
    }

    @Override
    public void setStatistics(Collection<StatisticElement> statistics) {
        this.statistics = statistics;
    }

    @Override
    public Collection<T> getResultList() {
        return resultList;
    }

    @Override
    public void setResultList(Collection<T> resultList) {
        this.resultList = resultList;
    }


    @Override
    public long getStartPosition() {
        return startPosition;
    }

    @Override
    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public long getCountAllRow() {
        return countAllRow;
    }

    @Override
    public void setCountAllRow(long countAllRow) {
        this.countAllRow = countAllRow;
    }

    @Override
    public Collection<Identifier> getIdentifiers() {
        return identifiers;
    }

    @Override
    public void setIdentifiers(Collection<Identifier> identifiers) {
        this.identifiers = identifiers;
    }
    @Override
    public String toString() {
        return "StatisticResultImpl [statistics=" + statistics + ", groups=" + groups + ", resultList=" + resultList + ", startPosition=" + startPosition + ", pageSize="
                + pageSize + ", countAllRow=" + countAllRow + ", identifiers=" + identifiers + "]";
    }


}
