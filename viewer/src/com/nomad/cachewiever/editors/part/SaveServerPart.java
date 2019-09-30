package com.nomad.cachewiever.editors.part;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.NodeEditPart;

import com.nomad.cachewiever.editors.figure.SaveServerFigure;
import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.SaveServerNode;


public class SaveServerPart extends CommonPart implements NodeEditPart{
  @Override
  protected IFigure createFigure() {
    IFigure figure = new SaveServerFigure((SaveServerNode) getModel());
    return figure;
  }

  
  protected void refreshVisuals() {
    SaveServerFigure figure = (SaveServerFigure) getFigure();
    SaveServerNode model = (SaveServerNode) getModel();
    figure.setLayout(model.getLayout());
  }


  public List<Connection> getModelSourceConnections() {
    return ((SaveServerNode)getModel()).getSourceConnectionsArray();
  }
  
  public List<Connection> getModelTargetConnections() {
    return ((SaveServerNode)getModel()).getTargetConnectionsArray();
  }



}