/**
 * Created using the GEF Tutorial by
 * Jean-Charles Mammana,
 * Romain Meson,
 * Jonathan Gramain
 *  
 * Modified by Christopher Kebschull
 *  
 */

package com.nomad.cachewiever.editors.editpolicy;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import com.nomad.cachewiever.editors.commands.ServerChangeLayoutCommand;
import com.nomad.cachewiever.editors.model.command.AbstractLayoutCommand;
import com.nomad.cachewiever.editors.part.ServerPart;

public class AppEditLayoutPolicy extends XYLayoutEditPolicy {

  @Override
  protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
    AbstractLayoutCommand command = null;
    if (child instanceof ServerPart) {
      command = new ServerChangeLayoutCommand();
    }
    if(command==null){
      return null;
    }
    command.setModel(child.getModel());
    command.setConstraint((Rectangle) constraint);
    return command;
  }
  protected Command getCreateCommand(CreateRequest request) {
    return null;
  }
  /*
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    if (request.getType() == REQ_CREATE && getHost() instanceof EnterprisePart) {
      ServerCreateCommand cmd = new ServiceCreateCommand();
      cmd.setEntreprise(getHost().getModel());
      cmd.setService(request.getNewObject());

      Rectangle constraint = (Rectangle) getConstraintFor(request);
      constraint.x = (constraint.x < 0) ? 0 : constraint.x;
      constraint.y = (constraint.y < 0) ? 0 : constraint.y;
      cmd.setLayout(constraint);
      return cmd;
    } else if (request.getType() == REQ_CREATE && getHost() instanceof ServicePart) {
      EmployeCreateCommand cmd = new EmployeCreateCommand();
      cmd.setService(getHost().getModel());
      cmd.setEmploye(request.getNewObject());

      Rectangle constraint = (Rectangle) getConstraintFor(request);
      constraint.x = (constraint.x < 0) ? 0 : constraint.x;
      constraint.y = (constraint.y < 0) ? 0 : constraint.y;
      constraint.width = (constraint.width <= 0) ? EmployeFigure.EMPLOYE_FIGURE_DEFWIDTH : constraint.width;
      constraint.height = (constraint.height <= 0) ? EmployeFigure.EMPLOYE_FIGURE_DEFHEIGHT : constraint.height;
      cmd.setLayout(constraint);
      return cmd; 
    }
    return null;
  }
*/
  protected Point getLayoutOrigin() {
    IFigure container = getLayoutContainer();
    LayoutManager lmanager = container.getLayoutManager();
    if (lmanager instanceof XYLayout) {
      return getXYLayout().getOrigin(getLayoutContainer());
    }
    
    int cicle =10;
    while(cicle>0){
      cicle--;
      container=container.getParent();
      if(container==null){
        return new Point(0,0);
      }
      lmanager = container.getLayoutManager();
      if(lmanager==null){
        return new Point(0,0);
        
      }
      if (lmanager instanceof XYLayout) {
        return ((XYLayout)lmanager).getOrigin(getLayoutContainer());
      }
    }
    return new Point(0,0);

  }

}
