package com.nomad.statistic;

import java.util.Date;
import java.util.Map;

public interface StatisticPoint {

    Date getDate();

    void setDate(Date date);

    StatisticData getStatistic();

    Map<String, String> getParameters();

    void setStatisticData(StatisticData data);

}
