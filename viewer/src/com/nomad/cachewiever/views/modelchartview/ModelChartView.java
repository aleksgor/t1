package com.nomad.cachewiever.views.modelchartview;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider.UpdateMode;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
import org.csstudio.swt.xygraph.figures.Trace.TraceType;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.linearscale.Range;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.ViewPart;

import com.nomad.cachewiever.utility.OpenViewUtilite;
import com.nomad.cachewiever.views.model.Server;

public class ModelChartView extends ViewPart {
  public static final String ID = "com.nomad.cachewiever.views.ModelChartView";
  private XYGraph xyGraph;
  private CircularBufferDataProvider ztraceDataProvider;
  private Map<String, CircularBufferDataProvider> dataProviders = new HashMap<String, CircularBufferDataProvider>();
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
    xyGraph.primaryYAxis.setTitle("Object count");
    xyGraph.primaryXAxis.setTitle("Date");
    xyGraph.setTitle("Object count");
    Color black = new Color(null, 0, 0, 0);
    xyGraph.getPlotArea().setBackgroundColor(black);

    // set it as the content of LightwightSystem
    lws.setContents(xyGraph);

    // create a trace data provider, which will provide the data to the trace.

    ztraceDataProvider = getCircularBufferDataProvider("", black);
    xyGraph.primaryXAxis.setDateEnabled(true);

  }

  private CircularBufferDataProvider getCircularBufferDataProvider(String title, Color color) {
    CircularBufferDataProvider result = new CircularBufferDataProvider(true);
    result.setBufferSize(500);
    result.setUpdateMode(UpdateMode.X_AND_Y);

    Trace trace = new Trace(title, xyGraph.primaryXAxis, xyGraph.primaryYAxis, result);

    trace.setDataProvider(result);
    trace.setTraceType(TraceType.SOLID_LINE);
    trace.setLineWidth(1);
    trace.setPointStyle(PointStyle.POINT);
    trace.setPointSize(2);
    trace.setPointStyle(PointStyle.XCROSS);
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
    this.server=server;
    setContentDescription(server.getServerModel().getHost() + ":" + server.getServerModel().getManagementPort());
  }

  public void addData(Map<String, Integer> data) {
    if (data == null) {
      return;
    }
    Set<Entry<String, Integer>> set = data.entrySet();
    long currenttime = System.currentTimeMillis();

    for (Entry<String, Integer> entry : set) {
      CircularBufferDataProvider dataProvider = dataProviders.get(entry.getKey());
      if (dataProvider == null) {
        dataProvider = getCircularBufferDataProvider(entry.getKey(), new Color(null, 255, 124, 120));
        dataProviders.put(entry.getKey(), dataProvider);
      }
      dataProvider.setCurrentYData(entry.getValue(), currenttime);

    }
    ztraceDataProvider.setCurrentYData(0, currenttime);

  }

  public void dispose() {
    super.dispose();
    OpenViewUtilite.closeView(server, this);
  }

}