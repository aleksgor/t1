package com.nomad.cachewiever.editors.part;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.NodeEditPart;

import com.nomad.cachewiever.editors.figure.SaveClientFigure;
import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.SaveClientNode;


public class SaveClientPart extends CommonPart implements NodeEditPart{
  @Override
  protected IFigure createFigure() {
    IFigure figure = new SaveClientFigure((SaveClientNode) getModel());
    return figure;
  }

  
  protected void refreshVisuals() {
    SaveClientFigure figure = (SaveClientFigure) getFigure();
    SaveClientNode model = (SaveClientNode) getModel();
    figure.setLayout(model.getLayout());
  }


  public List<Connection> getModelSourceConnections() {
    return ((SaveClientNode)getModel()).getSourceConnectionsArray();
  }
  
  public List<Connection> getModelTargetConnections() {
    return ((SaveClientNode)getModel()).getTargetConnectionsArray();
  }



}