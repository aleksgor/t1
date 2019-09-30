package com.nomad.client;


import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticResult;

public class CriteriaResult<T extends Model> extends AbstractResult {
    StatisticResult<T> result;

    public CriteriaResult(StatisticResult<T> result) {
        super();
        this.result = result;
    }

    public StatisticResult<T> getResult() {
        return result;
    }

    public void setResult(StatisticResult<T> result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "CriteriaResult [result=" + result + "]";
    }

}
