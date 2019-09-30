package com.nomad.cachewiever.editors.part;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.NodeEditPart;

import com.nomad.cachewiever.editors.figure.SessionClientFigure;
import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.SessionClientNode;


public class SessionClientPart extends CommonPart implements NodeEditPart{
  @Override
  protected IFigure createFigure() {
    IFigure figure = new SessionClientFigure((SessionClientNode) getModel());
    return figure;
  }

  
  protected void refreshVisuals() {
    SessionClientFigure figure = (SessionClientFigure) getFigure();
    SessionClientNode model = (SessionClientNode) getModel();
    figure.setLayout(model.getLayout());
  }


  public List<Connection> getModelSourceConnections() {
    return ((SessionClientNode)getModel()).getSourceConnectionsArray();
  }
  
  public List<Connection> getModelTargetConnections() {
    return ((SessionClientNode)getModel()).getTargetConnectionsArray();
  }



}