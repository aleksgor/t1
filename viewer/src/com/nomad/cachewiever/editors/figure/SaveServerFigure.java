package com.nomad.cachewiever.editors.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.SaveServerNode;

public class SaveServerFigure extends Figure {
  private SaveServerNode listener;
  private Label text;
  public SaveServerFigure(SaveServerNode model) {
    this.listener = model;

    ToolbarLayout layout = new ToolbarLayout();

    setLayoutManager(layout);

    text = new Label("SaveServer port:" + model.getSaveServerModel().getPort() + " threads:" + model.getSaveServerModel().getThreads());
    text.setLabelAlignment(PositionConstants.LEFT);
    text.setTextAlignment(PositionConstants.LEFT);

    text.setToolTip(new Label("port:" + model.getSaveServerModel().getPort() + " threads:" + model.getSaveServerModel().getThreads() ));

    add(text);
    setBackgroundColor(model.getColor());
    setBorder(new LineBorder(1));
    setOpaque(true);
  }

  public void setLayout(Rectangle rect) {
    text.setText("SaveServer port:" + listener.getSaveServerModel().getPort() + " threads:" + listener.getSaveServerModel().getThreads());
    text.setToolTip(new Label("port:" + listener.getSaveServerModel().getPort() + " threads:" + listener.getSaveServerModel().getThreads() ));
    setBackgroundColor(listener.getColor());
    setBorder(new LineBorder(1));
    getParent().setConstraint(this, rect);
  }
}