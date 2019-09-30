package com.nomad.cachewiever.editors.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.CommandPluginNode;

public class CommandPluginFigure extends Figure {
  private CommandPluginNode commandPlugin;
  private Label text;

  public CommandPluginFigure(CommandPluginNode commandPlugin) {
    this.commandPlugin = commandPlugin;

    ToolbarLayout layout = new ToolbarLayout();

    setLayoutManager(layout);

    text = new Label("class :" + commandPlugin.getCommandPluginModel().getClazz() + " pool sise:"
        + commandPlugin.getCommandPluginModel().getPoolSize());
    text.setToolTip(new Label("class :" + commandPlugin.getCommandPluginModel().getClazz() + " pool sise:"
        + commandPlugin.getCommandPluginModel().getPoolSize()));

    text.setLabelAlignment(PositionConstants.LEFT);
    text.setTextAlignment(PositionConstants.LEFT);

    add(text);
    setBorder(new LineBorder(1));
    setOpaque(true);
  }

  public void setLayout(Rectangle rect) {
    text.setText("class :" + commandPlugin.getCommandPluginModel().getClazz() + " pool sise:"
        + commandPlugin.getCommandPluginModel().getPoolSize());
    text.setToolTip(new Label("class :" + commandPlugin.getCommandPluginModel().getClazz() + " pool sise:"
        + commandPlugin.getCommandPluginModel().getPoolSize()));
    setBorder(new LineBorder(1));
    getParent().setConstraint(this, rect);
  }
}