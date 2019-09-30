package com.nomad.cachewiever.editors.part;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import com.nomad.cachewiever.editors.editpolicy.AppConnectionPolicy;
import com.nomad.cachewiever.editors.editpolicy.AppDeletePolicy;
import com.nomad.cachewiever.editors.editpolicy.AppEditLayoutPolicy;
import com.nomad.cachewiever.editors.editpolicy.AppRenamePolicy;
import com.nomad.cachewiever.editors.figure.ServerFigure;
import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.editors.model.ServerNode;
                        
public class ServerPart extends AppAbstractEditPart implements NodeEditPart {
  @Override
  protected IFigure createFigure() {
    IFigure figure = new ServerFigure((ServerNode) getModel());
    return figure;
  }

  @Override
  protected void createEditPolicies() {
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new AppEditLayoutPolicy());
    installEditPolicy(EditPolicy.COMPONENT_ROLE,new AppDeletePolicy());
    installEditPolicy(EditPolicy.NODE_ROLE, new AppRenamePolicy());
    installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new AppConnectionPolicy());
  }

  public List<Node> getModelChildren() {
    return ((Node) getModel()).getChildrenArray();
  }

  protected void refreshVisuals() {

    ServerNode model = (ServerNode) getModel();
    GraphicalEditPart parent = (GraphicalEditPart) getParent();
    parent.setLayoutConstraint(this, getFigure(), model.getLayout());
    ServerFigure figure = (ServerFigure) getFigure();
    figure.setLayout(model.getLayout());

  }

  protected void addChildVisual(EditPart childEditPart, int index) {
    IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
    getContentPane().add(child, index + 2);

  }


  public List<Connection> getModelSourceConnections() {
    return ((ServerNode)getModel()).getSourceConnectionsArray();
  }
  
  public List<Connection> getModelTargetConnections() {
    return ((ServerNode)getModel()).getTargetConnectionsArray();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(Node.PROPERTY_LAYOUT)) refreshVisuals();
    if (evt.getPropertyName().equals(Node.PROPERTY_ADD)) refreshChildren(); 
    if (evt.getPropertyName().equals(Node.PROPERTY_REMOVE)) refreshChildren();
    if (evt.getPropertyName().equals(Node.PROPERTY_RENAME)) refreshVisuals();
    if (evt.getPropertyName().equals(ServerNode.PROPERTY_CLEANER_TIMER)) refreshVisuals();
    if (evt.getPropertyName().equals(ServerNode.PROPERTY_COMMAND_PORT)) refreshVisuals();
    if (evt.getPropertyName().equals(ServerNode.PROPERTY_HOST)) refreshVisuals();
    if (evt.getPropertyName().equals(ServerNode.PROPERTY_MANAGEMENT_PORT)) refreshVisuals();
    if (evt.getPropertyName().equals(ServerNode.PROPERTY_NAME)) refreshVisuals();
    if (evt.getPropertyName().equals(ServerNode.PROPERTY_SESSION_TIMEOUT)) refreshVisuals();
    if (evt.getPropertyName().equals(ServerNode.PROPERTY_THREADS)) refreshVisuals();
    if (evt.getPropertyName().equals(Node.SOURCE_CONNECTION)) refreshSourceConnections();
    if (evt.getPropertyName().equals(Node.TARGET_CONNECTION)) refreshTargetConnections();
    if (evt.getPropertyName().equals(Node.PROPERTY_CHANGE)) refreshVisuals();

  }

  @Override
  public ConnectionAnchor getSourceConnectionAnchor(
      ConnectionEditPart connection) {
    return new ChopboxAnchor(getFigure());
  }

  @Override
  public ConnectionAnchor getSourceConnectionAnchor(Request request) {
    return new ChopboxAnchor(getFigure());
  }

  @Override
  public ConnectionAnchor getTargetConnectionAnchor(
      ConnectionEditPart connection) {
    return new ChopboxAnchor(getFigure());
  }

  @Override
  public ConnectionAnchor getTargetConnectionAnchor(Request request) {
    return new ChopboxAnchor(getFigure());
  }
}