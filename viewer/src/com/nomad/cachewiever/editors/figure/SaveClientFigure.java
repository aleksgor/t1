package com.nomad.cachewiever.editors.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.SaveClientNode;

public class SaveClientFigure extends Figure {
  private SaveClientNode listener;
  private Label text;
  public SaveClientFigure(SaveClientNode model) {
    this.listener = model;

    ToolbarLayout layout = new ToolbarLayout();

    setLayoutManager(layout);

    text = new Label("Save Client port:" + model.getSaveClientModel().getPort() + " threads:" + model.getSaveClientModel().getThreads());
    text.setLabelAlignment(PositionConstants.LEFT);
    text.setTextAlignment(PositionConstants.LEFT);

    text.setToolTip(new Label("port:" + model.getSaveClientModel().getPort() + " threads:" + model.getSaveClientModel().getThreads() ));

    add(text);
    setBackgroundColor(model.getColor());
    setBorder(new LineBorder(1));
    setOpaque(true);
  }

  public void setLayout(Rectangle rect) {
    text.setText("Save Client port:" + listener.getSaveClientModel().getPort() + " threads:" + listener.getSaveClientModel().getThreads());
    text.setToolTip(new Label("port:" + listener.getSaveClientModel().getPort() + " threads:" + listener.getSaveClientModel().getThreads() ));
    setBackgroundColor(listener.getColor());
    setBorder(new LineBorder(1));
    getParent().setConstraint(this, rect);
  }
}