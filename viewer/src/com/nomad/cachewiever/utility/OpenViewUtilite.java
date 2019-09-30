package com.nomad.cachewiever.utility;

import java.util.Timer;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.nomad.cachewiever.utility.AppData.TimerContent;
import com.nomad.cachewiever.views.cacheview.MemoryChartView;
import com.nomad.cachewiever.views.chartview.ChartView;
import com.nomad.cachewiever.views.model.RefreshTimerTask;
import com.nomad.cachewiever.views.model.Server;
import com.nomad.cachewiever.views.modelchartview.ModelChartView;

public class OpenViewUtilite {

  public static void openMemoryChartView(Server server) throws Exception {
    String key = getKey(server);

    TimerContent timerContent = AppData.getTimers().get(key);

    IWorkbenchPage wbPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    MemoryChartView chv = (MemoryChartView) wbPage.showView(MemoryChartView.ID, key, IWorkbenchPage.VIEW_VISIBLE);
    chv.setServer(server);
    if (timerContent == null) {
      timerContent = new TimerContent();
      timerContent.memoryChartView = chv;
      startTimer(timerContent, server);
      AppData.getTimers().put(key, timerContent);

    } else {
      timerContent.memoryChartView = chv;
      if (timerContent.timer == null) {
        startTimer(timerContent, server);
      }
    }
  }
  public static void openChartView(Server server) throws Exception {
    String key = getKey(server);

    TimerContent timerContent = AppData.getTimers().get(key);

    IWorkbenchPage wbPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    ChartView chv = (ChartView) wbPage.showView(ChartView.ID, key, IWorkbenchPage.VIEW_VISIBLE);
    chv.setServer(server);
    if (timerContent == null) {
      timerContent = new TimerContent();
      timerContent.chartView = chv;
      startTimer(timerContent, server);
      AppData.getTimers().put(key, timerContent);

    } else {
      timerContent.chartView = chv;
      if (timerContent.timer == null) {
        startTimer(timerContent, server);
      }
    }
  }
  
  public static void openModelChartView(Server server) throws Exception {
    String key = getKey(server);

    TimerContent timerContent = AppData.getTimers().get(key);

    IWorkbenchPage wbPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    ModelChartView chv = (ModelChartView) wbPage.showView(ModelChartView.ID, key, IWorkbenchPage.VIEW_VISIBLE);
    chv.setServer(server);
    if (timerContent == null) {
      timerContent = new TimerContent();
      timerContent.modelChartView = chv;
      startTimer(timerContent, server);
      AppData.getTimers().put(key, timerContent);
    } else {
      timerContent.modelChartView = chv;
      if (timerContent.timer == null) {
        startTimer(timerContent, server);
      }
    }
  }

  public static void closeView(Server server, ViewPart view) {
    String key = getKey(server);
    TimerContent tc = AppData.getTimers().get(key);
    if (tc != null) {
      if (view instanceof ModelChartView) {
        tc.modelChartView = null;
      }
      if (view instanceof MemoryChartView) {
        tc.memoryChartView = null;
      }
      if (tc.memoryChartView == null && tc.modelChartView == null) {
        tc.timer.cancel();
        tc.timer = null;
      }
    }
  }

  private static String getKey(Server server) {
    if (server != null) {
      return server.getServerModel().getHost() + " " + server.getServerModel().getManagementPort();
    }
    return "";
  }

  private static void startTimer(TimerContent timerContent, Server server) {
    Timer timer = new Timer();
    timerContent.timer = timer;
    timer.schedule(new RefreshTimerTask(server, timerContent), 0, 4 * 1000);
  }
}
