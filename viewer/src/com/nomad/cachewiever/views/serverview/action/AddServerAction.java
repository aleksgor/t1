package com.nomad.cachewiever.views.serverview.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.nomad.cachewiever.views.serverview.ServerView;

public class AddServerAction extends Action implements Listener {

  private ServerView view;

  public AddServerAction(ServerView view) {
    this.view = view;
  }

  public void handleEvent(Event event) {
    try {
      if (event.data != null) {
        String[] data = (String[]) event.data;
        view.addServer(data[0], Integer.parseInt(data[1]));
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
