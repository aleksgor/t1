package com.nomad.cachewiever.editors.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.SessionServerNode;

public class SessionServerFigure extends Figure {
  private SessionServerNode listener;
  private Label text;
  public SessionServerFigure(SessionServerNode model) {
    this.listener = model;

    ToolbarLayout layout = new ToolbarLayout();

    setLayoutManager(layout);

    text = new Label("Session server port:" + model.getSessionServerModel().getPort() + " threads:" + model.getSessionServerModel().getThreads());
    text.setLabelAlignment(PositionConstants.LEFT);
    text.setTextAlignment(PositionConstants.LEFT);

    text.setToolTip(new Label("port:" + model.getSessionServerModel().getPort() + " threads:" + model.getSessionServerModel().getThreads() ));

    add(text);
    setBackgroundColor(model.getColor());
    setBorder(new LineBorder(1));
    setOpaque(true);
  }

  public void setLayout(Rectangle rect) {
    text.setText("Session server port:" + listener.getSessionServerModel().getPort() + " threads:" + listener.getSessionServerModel().getThreads());
    text.setToolTip(new Label("port:" + listener.getSessionServerModel().getPort() + " threads:" + listener.getSessionServerModel().getThreads() ));
    setBackgroundColor(listener.getColor());
    setBorder(new LineBorder(1));
    getParent().setConstraint(this, rect);
  }
}