package com.nomad.cachewiever.editors.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;

import com.nomad.model.ListenerModel;

public class ShowConnectStatisticAction extends CommonEmptyAction implements IAction {
  ListenerModel listener ;
  public ShowConnectStatisticAction(ListenerModel listener){
    this.listener=listener;
  } 
  
  public static final String ID="ShowConnectStatisticAction";
 

  @Override
  public String getDescription() {
    
    return "Show statistic";
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getText() {
    
    return "show listener statistic ";
  }

  @Override
  public void runWithEvent(Event event) {
  }

}
