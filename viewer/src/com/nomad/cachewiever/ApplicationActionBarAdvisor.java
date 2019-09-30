package com.nomad.cachewiever;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.

  IWorkbenchAction saveAction;
  IWorkbenchAction importAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

  @Override
  protected void makeActions(IWorkbenchWindow window) {
    super.makeActions(window);
     saveAction = ActionFactory.SAVE.create(window);
    register(saveAction);
    importAction = ActionFactory.IMPORT.create(window);
   register(saveAction);
  }

  
  
  
  @Override
  protected void fillCoolBar(ICoolBarManager coolBar) {
    super.fillCoolBar(coolBar);
    IToolBarManager saveToolbar = new ToolBarManager(SWT.FLAT | SWT.LEFT);
    saveToolbar.add(saveAction);
    saveToolbar.add(importAction);
    
    coolBar.add(new ToolBarContributionItem(saveToolbar, "save"));
  }

  
  

}
