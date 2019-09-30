package com.nomad.cachewiever.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.nomad.cachewiever.views.cacheview.MemoryChartView;
import com.nomad.cachewiever.views.chartview.ChartView;
import com.nomad.cachewiever.views.modelchartview.ModelChartView;
import com.nomad.model.ServerModel;

public class AppData {

  private final static Map<String, ServerModel> servers = new HashMap<String, ServerModel>();
  private final static Map<String, TimerContent> timers = new HashMap<String, TimerContent>();

  public static Map<String, ServerModel> getServers() {
    return servers;
  }

  public static void sendMessage(Object data, int action) {
    Event e = new Event();
    e.data = data;
    Shell[] ash = Display.getDefault().getShells();
    for (int i = 0; i < ash.length; i++)
      ash[i].notifyListeners(action, e);

  }

  public static List<IViewReference> getIViewReferences(String viewId) {
    List<IViewReference> result = new ArrayList<IViewReference>();
    IWorkbench wb = PlatformUI.getWorkbench();
    IWorkbenchPage wbPage = wb.getActiveWorkbenchWindow().getActivePage();
    IViewReference allParts[] = wbPage.getViewReferences();
    for (int i = 0; i < allParts.length; i++) {
      if (allParts[i].getId().equals(viewId)) {
        result.add(allParts[i]);
      }
    }
    return result;
  }

  
  public static class TimerContent{
    public Timer timer=null;
    public MemoryChartView memoryChartView;
    public ModelChartView modelChartView;
    public ChartView chartView;
  }


  public static Map<String, TimerContent> getTimers() {
    return timers;
  }
  
}
