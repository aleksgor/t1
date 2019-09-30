package com.nomad.cachewiever.editors.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;

import com.nomad.model.ServerModel;

public class ShowMemoryStatisticAction extends CommonEmptyAction implements IAction {
  public ShowMemoryStatisticAction(ServerModel server){
  } 
  
  public static final String ID="ShowMemoryStatisticAction";
 

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
    
    return "show memory statistic ";
  }

  @Override
  public void runWithEvent(Event event) {
  }

}
