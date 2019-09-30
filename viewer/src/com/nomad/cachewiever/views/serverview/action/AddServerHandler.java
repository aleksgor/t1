package com.nomad.cachewiever.views.serverview.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

import com.nomad.cachewiever.form.ServerDialog;
import com.nomad.cachewiever.utility.Action;
import com.nomad.cachewiever.utility.AppData;

public class AddServerHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    
    ServerDialog ssd = new ServerDialog(Display.getCurrent().getActiveShell());
    String [] ret = ssd.open("localhost", 12345);
    AppData.sendMessage(ret, Action.Connect.getCode());
    return ret;
  }

}
