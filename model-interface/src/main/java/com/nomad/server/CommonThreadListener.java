package com.nomad.server;

import java.io.IOException;

import com.nomad.statistic.StatisticCollector;

public interface CommonThreadListener {

    void stop() throws InterruptedException, IOException;

    StatisticCollector getStatistic();

}
