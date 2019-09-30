package com.nomad.cachewiever.editors.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;

import com.nomad.cachewiever.utility.OpenViewUtilite;
import com.nomad.cachewiever.views.model.Server;

public class ShowChartsStatisticAction extends CommonEmptyAction implements IAction {
  private Server server;
  
  public ShowChartsStatisticAction(Server server){
    this.server=server;
  } 
  
  public static final String ID="ShowModelStatisticAction";
 

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
    return "show model statistic ";
  }

  @Override
  public void runWithEvent(Event event) {
    
    try {
      OpenViewUtilite.openChartView(server);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
