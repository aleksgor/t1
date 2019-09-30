package com.nomad.cachewiever.views.model;

import com.nomad.model.ServerModel;

public class Server  {
  private ServerModel server = null;
  private int managerPort = 0;
  private String managerHost = "";

  private int x,y,w,h;

  
  public Server( ServerModel server ){
    this.server=server;
  }
  public ServerModel getServerModel() {
    return server;
  }


  public int getX() {
    return x;
  }
  public void setX(int x) {
    this.x = x;
  }
  public int getY() {
    return y;
  }
  public void setY(int y) {
    this.y = y;
  }
  public int getW() {
    return w;
  }
  public void setW(int w) {
    this.w = w;
  }
  public int getH() {
    return h;
  }
  public void setH(int h) {
    this.h = h;
  }
  public void setServer(ServerModel server) {
    this.server = server;
  }
  public int getManagerPort() {
    return managerPort;
  }
  public void setManagerPort(int managerPort) {
    this.managerPort = managerPort;
  }
  public String getManagerHost() {
    return managerHost;
  }
  public void setManagerHost(String managerHost) {
    this.managerHost = managerHost;
  }

  
 
}
