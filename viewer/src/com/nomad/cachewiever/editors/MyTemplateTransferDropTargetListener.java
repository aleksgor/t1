/**
 * Created using the GEF Tutorial by
 * Jean-Charles Mammana,
 * Romain Meson,
 * Jonathan Gramain
 *  
 * Modified by Christopher Kebschull
 *  
 */

package com.nomad.cachewiever.editors;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.CreationFactory;

import com.nomad.cachewiever.editors.model.NodeCreationFactory;


public class MyTemplateTransferDropTargetListener extends	TemplateTransferDropTargetListener  {

	public MyTemplateTransferDropTargetListener(EditPartViewer viewer) {
		super(viewer);
	}
	
	@Override 
	protected CreationFactory getFactory(Object template) { 
		return new NodeCreationFactory((Class<?>)template); 
	}

}
