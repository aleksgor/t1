package com.nomad.cachewiever.utility;

public enum Action {
  Connect(1111), RefreshChart(1112);

  int code;

  private Action(int code) {
    this.code = code;
  }
  public int getCode(){
    return code;
  }
}
