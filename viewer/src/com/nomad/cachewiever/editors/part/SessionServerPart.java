package com.nomad.cachewiever.editors.part;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.NodeEditPart;

import com.nomad.cachewiever.editors.figure.SessionServerFigure;
import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.SessionServerNode;


public class SessionServerPart extends CommonPart implements NodeEditPart{
  @Override
  protected IFigure createFigure() {
    IFigure figure = new SessionServerFigure((SessionServerNode) getModel());
    return figure;
  }

  
  protected void refreshVisuals() {
    SessionServerFigure figure = (SessionServerFigure) getFigure();
    SessionServerNode model = (SessionServerNode) getModel();
    figure.setLayout(model.getLayout());
  }


  public List<Connection> getModelSourceConnections() {
    return ((SessionServerNode)getModel()).getSourceConnectionsArray();
  }
  
  public List<Connection> getModelTargetConnections() {
    return ((SessionServerNode)getModel()).getTargetConnectionsArray();
  }



}