package com.nomad.model;

import java.util.Date;

public class MemoryInfo {
  private long freeMemory ;
  private long maxMemory;
  private long totalMemory;
  private Date date;
  
  public Date getDate() {
    return date;
  }
  public void setDate(Date date) {
    this.date = date;
  }
  public long getFreeMemory() {
    return freeMemory;
  }
  public void setFreeMemory(long freeMemory) {
    this.freeMemory = freeMemory;
  }
  public long getMaxMemory() {
    return maxMemory;
  }
  public void setMaxMemory(long maxMemory) {
    this.maxMemory = maxMemory;
  }
  public long getTotalMemory() {
    return totalMemory;
  }
  public void setTotalMemory(long totalMemory) {
    this.totalMemory = totalMemory;
  }
  @Override
  public String toString() {
    return "MomoryInfo [freeMemory=" + freeMemory + ", maxMemory=" + maxMemory + ", totalMemory=" + totalMemory + "]";
  }

  
}
