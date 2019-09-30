package com.nomad.model;

public interface BlockClientModel {
  
  public int getTimeOut() ;

  public void setTimeOut(int timeout) ;

  public int getPort();

  public void setPort(int port);

  public int getThreads();

  public void setThreads(int threads);

  public String getHost();

  public void setHost(String host);
}
