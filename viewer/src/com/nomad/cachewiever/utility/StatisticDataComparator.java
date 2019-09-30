package com.nomad.cachewiever.utility;

import java.util.Comparator;

import com.nomad.statistic.StatisticData;

public class StatisticDataComparator implements Comparator<StatisticData>{

  @Override
  public int compare(StatisticData arg0, StatisticData arg1) {
    if(arg0.getDate()>arg1.getDate()){
      return 1;
    }
    if(arg0.getDate()<arg1.getDate()){
      return -1;
    }
    return 0;
  }

 

}
