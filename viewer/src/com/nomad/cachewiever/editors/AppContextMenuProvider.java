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

import java.util.List;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

import com.nomad.cachewiever.editors.action.ShowChartsStatisticAction;
import com.nomad.cachewiever.editors.action.ShowConnectStatisticAction;
import com.nomad.cachewiever.editors.action.StartListenerAction;
import com.nomad.cachewiever.editors.action.StopListenerAction;
import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.ServerNode;
import com.nomad.cachewiever.editors.part.ConnectionPart;
import com.nomad.cachewiever.editors.part.ListenerPart;
import com.nomad.cachewiever.editors.part.ServerPart;
import com.nomad.model.ListenerModel;

public class AppContextMenuProvider extends ContextMenuProvider {

  private ActionRegistry actionRegistry;

  public AppContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
    super(viewer);
    setActionRegistry(registry);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void buildContextMenu(IMenuManager menu) {
    IAction action;
    GEFActionConstants.addStandardActionGroups(menu);
    action = getActionRegistry().getAction(ActionFactory.UNDO.getId());
    menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
    action = getActionRegistry().getAction(ActionFactory.REDO.getId());
    menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
    /*
     * action = getActionRegistry().getAction(ActionFactory.COPY.getId());
     * menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action); action =
     * getActionRegistry().getAction(ActionFactory.PASTE.getId());
     * menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
     */
    action = getActionRegistry().getAction(ActionFactory.DELETE.getId());
    menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
    /*
     * action = getActionRegistry().getAction(ActionFactory.RENAME.getId());
     * menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
     */

    List<Object> selected = getViewer().getSelectedEditParts();
    if (selected != null) {
      if (selected.size() == 1) {
        if (selected.get(0) instanceof ConnectionPart) {
          ConnectionPart conEp = (ConnectionPart) selected.get(0);
          action = new ShowConnectStatisticAction(((Connection) conEp.getModel()).getSourceNode().getListener());
          menu.appendToGroup(GEFActionConstants.GROUP_EDIT, action);
        }else   if (selected.get(0) instanceof ServerPart) {
          ServerPart server = (ServerPart) selected.get(0);
//          menu.appendToGroup(GEFActionConstants.GROUP_EDIT, new ShowMemoryStatisticAction(((ServerNode) conEp.getModel()).getServer().getColleague()));
          menu.appendToGroup(GEFActionConstants.GROUP_EDIT,  new ShowChartsStatisticAction(((ServerNode) server.getModel()).getServer()));
        }else   if (selected.get(0) instanceof ListenerPart) {
          ListenerPart listenerPart = (ListenerPart) selected.get(0);
          ListenerModel lst=listenerPart.getListenerModel();
          
          ServerNode server=(ServerNode)listenerPart.getParent().getModel();
          if(lst.getStatus()==1){
            menu.add(  new StopListenerAction(lst,server.getServer().getServerModel(),listenerPart));
          }else{
            menu.add( new StartListenerAction(lst,server.getServer().getServerModel(),listenerPart));
            
          }
        }
        
      }
    }
  }

  private ActionRegistry getActionRegistry() {
    return actionRegistry;
  }

  private void setActionRegistry(ActionRegistry registry) {
    actionRegistry = registry;
  }

}
