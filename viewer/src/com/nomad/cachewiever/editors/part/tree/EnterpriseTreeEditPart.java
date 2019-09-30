/**
 * Created using the GEF Tutorial by
 * Jean-Charles Mammana,
 * Romain Meson,
 * Jonathan Gramain
 *  
 * Modified by Christopher Kebschull
 *  
 */

package com.nomad.cachewiever.editors.part.tree;

import java.beans.PropertyChangeEvent;
import java.util.List;

import com.nomad.cachewiever.editors.model.RootObject;
import com.nomad.cachewiever.editors.model.Node;


public class EnterpriseTreeEditPart extends AppAbstractTreeEditPart {

	protected List<Node> getModelChildren() { 
		return ((RootObject)getModel()).getChildrenArray(); 
	} 
	
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals(Node.PROPERTY_ADD)) 
			refreshChildren(); 
		if(evt.getPropertyName().equals(Node.PROPERTY_REMOVE)) 
			refreshChildren(); 
	}
}
