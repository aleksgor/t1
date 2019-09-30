package com.nomad.cache.controller.serverlist;

import java.util.ArrayList;

import org.zkoss.zul.DefaultTreeNode;

public class ServerTreeNode extends DefaultTreeNode<DetailViewModel> {

  public ServerTreeNode(final DetailViewModel data) {
    super(data, new ArrayList<ServerTreeNode>());
  }

  private  String getHost() {
    return getData().getHost();
  }

  private int getPort() {
    return getData().getPort();
  }

  private String getType() {
    return getData().getType();
  }
  private String getName() {
      return getData().getName();
    }

  public  int getCount() {
    return super.getChildren().size();
  }
  private String getDetail() {
      return getData().getDetail();
    }

  public String getTotal(){
      String result ="";
     if(getHost()!=null){
         result+=getHost();
     }
     if(getName()!=null){
         result+=getName();
     }
     if(getType()!=null){
         result+=getType();
     }
     if(getPort()>0){
         result+=":"+getPort();
     }
     if(getDetail()!=null){
         result+=getDetail();
     }
     return result;
  }
  @Override
  public boolean isLeaf() {
      if(getData()==null){
          return false;
      }
    return getData().getServers().isEmpty();
  }
}
