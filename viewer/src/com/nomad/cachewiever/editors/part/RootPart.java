/**
 * Created using the GEF Tutorial by
 * Jean-Charles Mammana,
 * Romain Meson,
 * Jonathan Gramain
 *  
 * Modified by Christopher Kebschull
 *  
 */

package com.nomad.cachewiever.editors.part;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.swt.SWT;

import com.nomad.cachewiever.editors.editpolicy.AppDeletePolicy;
import com.nomad.cachewiever.editors.editpolicy.AppEditLayoutPolicy;
import com.nomad.cachewiever.editors.figure.RootFigure;
import com.nomad.cachewiever.editors.model.RootObject;
import com.nomad.cachewiever.editors.model.Node;


public class RootPart extends AppAbstractEditPart {

	@Override
	protected IFigure createFigure() {
		IFigure figure = new RootFigure();
		ConnectionLayer connLayer = (ConnectionLayer)getLayer(LayerConstants.CONNECTION_LAYER);
		connLayer.setAntialias(SWT.ON);
		connLayer.setConnectionRouter(new ShortestPathConnectionRouter(figure));
		return figure;
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new AppEditLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE,new AppDeletePolicy());
	}
	
	protected void refreshVisuals () {
		
	}

	public List<Node> getModelChildren() {
		return ((RootObject)getModel()).getChildrenArray();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(Node.PROPERTY_LAYOUT)) refreshVisuals();
		if (evt.getPropertyName().equals(Node.PROPERTY_ADD)) refreshChildren(); 
		if (evt.getPropertyName().equals(Node.PROPERTY_REMOVE)) refreshChildren();
		if (evt.getPropertyName().equals(Node.PROPERTY_RENAME)) refreshVisuals();
		if (evt.getPropertyName().equals(RootObject.PROPERTY_CAPITAL)) refreshVisuals();
	}
}
