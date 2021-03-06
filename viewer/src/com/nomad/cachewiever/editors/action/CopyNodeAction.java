/**
 * Created using the GEF Tutorial by
 * Jean-Charles Mammana,
 * Romain Meson,
 * Jonathan Gramain
 *  
 * Modified by Christopher Kebschull
 *  
 */

package com.nomad.cachewiever.editors.action;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.editors.model.command.CopyNodeCommand;


public class CopyNodeAction extends SelectionAction {
	public CopyNodeAction(IWorkbenchPart part) { 
		super(part);
		// force calculateEnabled() to be called in every context
		setLazyEnablementCalculation(true);
	}
	
	@Override 
	protected void init() {
		super.init(); 
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages(); 
		setText("Copy"); 
		setId(ActionFactory.COPY.getId());
		setHoverImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY)); 
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setEnabled(false);
	}
		
	private Command createCopyCommand(List<Object> selectedObjects) { 
		if (selectedObjects == null || selectedObjects.isEmpty()) {
			return null;
		}
		CopyNodeCommand cmd = new CopyNodeCommand(); 
		Iterator<Object> it = selectedObjects.iterator(); 
		while (it.hasNext()) {
			EditPart ep = (EditPart)it.next(); 
			Node node = (Node)ep.getModel(); 
			if (!cmd.isCopyableNode(node))
				return null; 
			cmd.addElement(node);
		}
		return cmd;
	}
	
	@SuppressWarnings("unchecked")
  @Override 
	protected boolean calculateEnabled() {
		Command cmd = createCopyCommand(getSelectedObjects()); 
		if (cmd == null)
			return false;
		return cmd.canExecute();
	}
	
	@SuppressWarnings("unchecked")
  @Override
	public void run() {
		Command cmd = createCopyCommand(getSelectedObjects());
		if (cmd != null && cmd.canExecute()) {
			cmd.execute();
		}
	}
}