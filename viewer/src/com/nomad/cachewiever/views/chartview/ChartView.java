package com.nomad.cachewiever.views.chartview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider.UpdateMode;
import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
import org.csstudio.swt.xygraph.figures.Trace.TraceType;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.linearscale.AbstractScale.LabelSide;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.ViewPart;

import com.nomad.cachewiever.utility.CacheColorConstants;
import com.nomad.cachewiever.utility.OpenViewUtilite;
import com.nomad.cachewiever.utility.StatisticDataComparator;
import com.nomad.cachewiever.views.model.Server;
import com.nomad.model.MemoryInfo;
import com.nomad.server.CacheServerConstants;
import com.nomad.statistic.StatisticData;
import com.nomad.statistic.StatisticPoint;

public class ChartView extends ViewPart {
  public static final String ID = "com.nomad.cachewiever.views.ChartView";
  private XYGraph xyGraph;
  private final static int min = 0;
  private final static int avg = 1;
  private final static int max = 2;
  private final static int count = 3;
  private Axis lcounterAxis;
  private Axis ltmAxis;

  private CircularBufferDataProvider timeDataProvider;

  private CircularBufferDataProvider maxMemoryDataProvider;
  private CircularBufferDataProvider freeMemoryDataProvider;
  private CircularBufferDataProvider totalMemoryDataProvider;

  // models
  final private Map<String, CircularBufferDataProvider> modelDataProviders = new HashMap<String, CircularBufferDataProvider>();
  final private Map<String, CircularBufferDataProvider[]> connectPooldataProviders = new HashMap<String, CircularBufferDataProvider[]>();
  final private Map<String, CircularBufferDataProvider[]> listenerdataProviders = new HashMap<String, CircularBufferDataProvider[]>();
  // listener statistic
  private Axis mcAxis;
  private Server server;

  @Override
  public void saveState(IMemento memento) {

    super.saveState(null);
  }

  /**
   * This is a callback that will allow us to create the viewer and initialize
   * it.
   */
  public void createPartControl(Composite parent) {
    // use LightweightSystem to create the bridge between SWT and draw2D
    final LightweightSystem lws = new LightweightSystem(new Canvas(parent, SWT.NONE));

    // create a new XY Graph.
    xyGraph = new XYGraph();
    xyGraph.primaryXAxis.setRange(new Range(0, 250));
    xyGraph.primaryXAxis.setDateEnabled(true);
    xyGraph.primaryXAxis.setAutoScale(true);
    xyGraph.primaryYAxis.setAutoScale(true);
    xyGraph.primaryYAxis.setShowMajorGrid(true);
    xyGraph.primaryXAxis.setAutoScaleThreshold(0);
    xyGraph.primaryYAxis.setFormatPattern("##,###");
    xyGraph.primaryYAxis.setTitle("Memoy (kb)");
    xyGraph.primaryXAxis.setTitle("Date");
    xyGraph.setTitle("Memory info ");
    Color black = new Color(null, 0, 0, 0);
    xyGraph.getPlotArea().setBackgroundColor(black);

    mcAxis = new Axis("Model count", true);
    mcAxis.setTickLableSide(LabelSide.Primary);
    mcAxis.setAutoScale(true);
    xyGraph.addAxis(mcAxis);

    ltmAxis = new Axis("request time", true);
    ltmAxis.setTickLableSide(LabelSide.Secondary);
    ltmAxis.setAutoScale(true);
    xyGraph.addAxis(ltmAxis);

    lcounterAxis = new Axis("request count", true);
    lcounterAxis.setTickLableSide(LabelSide.Secondary);
    lcounterAxis.setAutoScale(true);
    xyGraph.addAxis(lcounterAxis);

    maxMemoryDataProvider = getCircularBufferDataProvider("Max memory", xyGraph.primaryYAxis, CacheColorConstants.blue);
    freeMemoryDataProvider = getCircularBufferDataProvider("free memory", xyGraph.primaryYAxis, CacheColorConstants.green);
    totalMemoryDataProvider = getCircularBufferDataProvider("total  memory", xyGraph.primaryYAxis, CacheColorConstants.red);

    // set it as the content of LightwightSystem
    lws.setContents(xyGraph);

    // create a trace data provider, which will provide the data to the trace.

    timeDataProvider = getCircularBufferDataProvider("", xyGraph.primaryYAxis, CacheColorConstants.black);
    xyGraph.primaryXAxis.setDateEnabled(true);

  }

  private CircularBufferDataProvider getCircularBufferDataProvider(String title, Axis yAxis, Color color) {
    return getCircularBufferDataProvider(title, yAxis, color, PointStyle.POINT);
  }

  private CircularBufferDataProvider getCircularBufferDataProvider(String title, Axis yAxis, Color color, PointStyle pointType) {
    CircularBufferDataProvider result = new CircularBufferDataProvider(true);
    result.setBufferSize(500);
    result.setUpdateMode(UpdateMode.X_AND_Y);

    Trace trace = new Trace(title, xyGraph.primaryXAxis, yAxis, result);

    trace.setDataProvider(result);
    trace.setTraceType(TraceType.SOLID_LINE);
    trace.setLineWidth(1);
    trace.setPointStyle(pointType);
    trace.setPointSize(4);
    trace.setTraceColor(color);

    // add the trace to xyGraph
    xyGraph.addTrace(trace);
    return result;
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  public void setFocus() {

  }

  public void setServer(Server server) {
    this.server = server;
    setContentDescription(server.getServerModel().getHost() + ":" + server.getServerModel().getManagementPort());
  }

  /**
   * 
   * @param modelCountData
   *          models info
   * @param mi
   *          memory info
   */
  public synchronized void addData(long currentTime, Map<String, Integer> modelCountData, MemoryInfo mi, List<StatisticPoint> poolstatistics,
      List<StatisticPoint> listenerStatistic) {

    long currenttime = System.currentTimeMillis();
    if (modelCountData != null) {
      Set<Entry<String, Integer>> set = modelCountData.entrySet();

      for (Entry<String, Integer> entry : set) {
        CircularBufferDataProvider dataProvider = modelDataProviders.get(entry.getKey());
        if (dataProvider == null) {
          dataProvider = getCircularBufferDataProvider(entry.getKey(), mcAxis, CacheColorConstants.lightGray);
          modelDataProviders.put(entry.getKey(), dataProvider);
        }
        dataProvider.setCurrentYData(entry.getValue(), currenttime);

      }
    }
    if (listenerStatistic != null) {
      for (StatisticPoint stat : listenerStatistic) {
        String key = stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.command) + ":"
            + stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.modelName);

        CircularBufferDataProvider[] dataProvider = listenerdataProviders.get(key);
        if (dataProvider == null) {
          Color color = CacheColorConstants.getOrangeColor(listenerdataProviders.size());

          dataProvider = new CircularBufferDataProvider[4];
          dataProvider[min] = getCircularBufferDataProvider("min " + stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.command) + " "
              + stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.modelName), ltmAxis, color, PointStyle.CIRCLE);
          dataProvider[avg] = getCircularBufferDataProvider("avg" + stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.command) + " "
              + stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.modelName), ltmAxis, color, PointStyle.BAR);
          dataProvider[max] = getCircularBufferDataProvider("max" + stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.command) + " "
              + stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.modelName), ltmAxis, color, PointStyle.DIAMOND);
          dataProvider[count] = getCircularBufferDataProvider("count" + stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.command)
              + " " + stat.getParameters().get(CacheServerConstants.Statistic.ListenerProperties.modelName), lcounterAxis, color, PointStyle.SQUARE);
          listenerdataProviders.put(key, dataProvider);
        }

        List<StatisticData> collectors = new ArrayList<StatisticData>(stat.getAllStatisticData());
        Collections.sort(collectors, new StatisticDataComparator());

        for (StatisticData statisticCollector : collectors) {
          if (statisticCollector.getDate() > 10) {
            dataProvider[min].setCurrentYData(statisticCollector.getMin(), statisticCollector.getDate());
            dataProvider[max].setCurrentYData(statisticCollector.getMax(), statisticCollector.getDate());
            dataProvider[avg].setCurrentYData(statisticCollector.getAverage(), statisticCollector.getDate());
            dataProvider[count].setCurrentYData(statisticCollector.getCount(), statisticCollector.getDate());
          }
        }

      }

    }
    if (poolstatistics != null) {
      for (StatisticPoint storeConnectionPoolModel : poolstatistics) {

        String host = storeConnectionPoolModel.getParameters().get(CacheServerConstants.Statistic.StoreConnectonPoolProperties.host);
        String port = storeConnectionPoolModel.getParameters().get(CacheServerConstants.Statistic.StoreConnectonPoolProperties.port);
        String key = host + ":" + port;
        CircularBufferDataProvider[] dataProvider = connectPooldataProviders.get(key);
        if (dataProvider == null) {
          dataProvider = new CircularBufferDataProvider[4];
          Color color = CacheColorConstants.getCyanColor(connectPooldataProviders.size());
          dataProvider[min] = getCircularBufferDataProvider("min" + host + ":" + port, ltmAxis, color, PointStyle.CIRCLE);
          dataProvider[avg] = getCircularBufferDataProvider("avg" + host + ":" + port, ltmAxis, color, PointStyle.BAR);
          dataProvider[max] = getCircularBufferDataProvider("max" + host + ":" + port, ltmAxis, color, PointStyle.DIAMOND);
          dataProvider[count] = getCircularBufferDataProvider("count" + host + ":" + port, lcounterAxis, color, PointStyle.SQUARE);
          connectPooldataProviders.put(key, dataProvider);
        }
        List<StatisticData> collectors = new ArrayList<StatisticData>(storeConnectionPoolModel.getAllStatisticData());
        Collections.sort(collectors, new StatisticDataComparator());

        for (StatisticData statisticCollector : collectors) {
          if (statisticCollector.getDate() > 10) {
            dataProvider[min].setCurrentYData(statisticCollector.getMin(), statisticCollector.getDate());
            dataProvider[max].setCurrentYData(statisticCollector.getMax(), statisticCollector.getDate());
            dataProvider[avg].setCurrentYData(statisticCollector.getAverage(), statisticCollector.getDate());
            dataProvider[count].setCurrentYData(statisticCollector.getCount(), statisticCollector.getDate());
          }
        }
      }
    }
    freeMemoryDataProvider.setCurrentYData(mi.getFreeMemory() / 1024, currenttime);
    maxMemoryDataProvider.setCurrentYData(mi.getMaxMemory() / 1024, currenttime);
    totalMemoryDataProvider.setCurrentYData(mi.getTotalMemory() / 1024, currenttime);

    timeDataProvider.setCurrentYData(0, currentTime);

  }

  public void dispose() {
    super.dispose();
    OpenViewUtilite.closeView(server, this);
  }

}