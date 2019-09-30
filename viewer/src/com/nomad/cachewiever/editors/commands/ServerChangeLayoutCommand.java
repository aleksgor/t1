package com.nomad.cachewiever.editors.commands;

import org.eclipse.draw2d.geometry.Rectangle;

import com.nomad.cachewiever.editors.model.ServerNode;
import com.nomad.cachewiever.editors.model.command.AbstractLayoutCommand;

public class ServerChangeLayoutCommand extends AbstractLayoutCommand {
  private ServerNode model;
  private Rectangle layout;
  private Rectangle oldLayout;

  public void execute() {
    model.setLayout(layout);
  }

  public void setConstraint(Rectangle rect) {
    this.layout = rect;
  }

  public void setModel(Object model) {
    this.model = (ServerNode) model;
  }

  public void undo() {
    this.model.setLayout(this.oldLayout);

  }
}