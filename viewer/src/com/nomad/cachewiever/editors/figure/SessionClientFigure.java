package com.nomad.cachewiever.editors.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.SessionClientNode;

public class SessionClientFigure extends Figure {
  private SessionClientNode listener;
  private Label text;
  public SessionClientFigure(SessionClientNode model) {
    this.listener = model;

    ToolbarLayout layout = new ToolbarLayout();

    setLayoutManager(layout);

    text = new Label("Session Client port:" + model.getSessionClientModel().getPort() + " threads:" + model.getSessionClientModel().getThreads());
    text.setLabelAlignment(PositionConstants.LEFT);
    text.setTextAlignment(PositionConstants.LEFT);

    text.setToolTip(new Label("port:" + model.getSessionClientModel().getPort() + " threads:" + model.getSessionClientModel().getThreads()));

    add(text);
    setBackgroundColor(model.getColor());
    setBorder(new LineBorder(1));
    setOpaque(true);
  }

  public void setLayout(Rectangle rect) {
    text.setText("Session Client port:" + listener.getSessionClientModel().getPort() + " threads:" + listener.getSessionClientModel().getThreads());
    text.setToolTip(new Label("port:" + listener.getSessionClientModel().getPort() + " threads:" + listener.getSessionClientModel().getThreads() ));
    setBackgroundColor(listener.getColor());
    setBorder(new LineBorder(1));
    getParent().setConstraint(this, rect);
  }
}