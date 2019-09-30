package com.nomad.cachewiever.editors.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.ListenerNode;
import com.nomad.cachewiever.utility.CacheColorConstants;
import com.nomad.model.ListenerModel;

public class ListenerFigure extends Figure {
  private ListenerNode listener;
  private Label text;
  public ListenerFigure(ListenerNode listener) {
    this.listener = listener;

    ToolbarLayout layout = new ToolbarLayout();

    setLayoutManager(layout);

    
    text = new Label("Listener: port:" + listener.getListener().getPort() + " threads:" + listener.getListener().getThreads());
    text.setLabelAlignment(PositionConstants.LEFT);
    text.setTextAlignment(PositionConstants.LEFT);

    text.setToolTip(new Label("port:" + listener.getListener().getPort() + " threads:" + listener.getListener().getThreads() + " protocol version:"
        + listener.getListener().getProtocolVersion()));
    setBackgroundColor(listener.getListener().getStatus()==0?CacheColorConstants.red:CacheColorConstants.green);
    add(text);

    setBorder(new LineBorder(1));
    setOpaque(true);
  }

  public void setLayout(Rectangle rect) {
    text.setText("Listener: port:" + listener.getListener().getPort() + " threads:" + listener.getListener().getThreads());
    text.setToolTip(new Label("port:" + listener.getListener().getPort() + " threads:" + listener.getListener().getThreads() + " protocol version:"
        + listener.getListener().getProtocolVersion()));
    setBackgroundColor(listener.getListener().getStatus()==0?CacheColorConstants.red:CacheColorConstants.green);
    setBorder(new LineBorder(1));
    getParent().setConstraint(this, rect);
  }
  public ListenerModel getListenerModel(){
    return listener.getListener();
  }
}