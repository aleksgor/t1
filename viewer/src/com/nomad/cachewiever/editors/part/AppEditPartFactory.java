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

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import com.nomad.cachewiever.editors.model.CommandPluginNode;
import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.DataSourceNode;
import com.nomad.cachewiever.editors.model.RootObject;
import com.nomad.cachewiever.editors.model.ListenerNode;
import com.nomad.cachewiever.editors.model.SaveClientNode;
import com.nomad.cachewiever.editors.model.SaveServerNode;
import com.nomad.cachewiever.editors.model.ServerNode;
import com.nomad.cachewiever.editors.model.SessionClientNode;
import com.nomad.cachewiever.editors.model.SessionServerNode;
import com.nomad.cachewiever.editors.model.StoreModelNode;


public class AppEditPartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		
		if (model instanceof RootObject) {
			part = new RootPart();
		} else if (model instanceof Connection) {
			part = new ConnectionPart();
    } else if (model instanceof ServerNode) {
      part = new ServerPart();
    } else if (model instanceof ListenerNode) {
      part = new ListenerPart();
    } else if (model instanceof DataSourceNode) {
      part = new DataDourcePart();
    } else if (model instanceof StoreModelNode) {
      part = new StoreModelPart();
    } else if (model instanceof CommandPluginNode) {
      part = new CommandPluginPart();
    } else if (model instanceof SaveServerNode) {
      part = new SaveServerPart();
    } else if (model instanceof SaveClientNode) {
      part = new SaveClientPart();
    } else if (model instanceof SessionServerNode) {
      part = new SessionServerPart();
    } else if (model instanceof SessionClientNode) {
      part = new SessionClientPart();
		}
		part.setModel(model);
		return part;
	}

}
