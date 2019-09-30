package com.nomad.cachewiever.editors.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;

import com.nomad.cachewiever.editors.part.ListenerPart;
import com.nomad.client.ManagerClient;
import com.nomad.message.ManagementMessage;
import com.nomad.message.OperationStartus;
import com.nomad.model.ListenerModel;
import com.nomad.model.ManagerCommand;
import com.nomad.model.ServerModel;

public class StartListenerAction extends CommonEmptyAction implements IAction {
  private ListenerModel listener;
  private ServerModel server;
  private ListenerPart part;

  public StartListenerAction(ListenerModel listener, ServerModel server,ListenerPart part) {
    this.listener = listener;
    this.server = server;
    this.part=part;
  }

  public static final String ID = "StopListenerAction";

  @Override
  public String getDescription() {

    return "Show statistic";
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getText() {
    return "Start listener";
  }

  @Override
  public void runWithEvent(Event event) {

    try {
      ManagerClient client = new ManagerClient(server.getHost(), server.getManagementPort());
      int[] params = { listener.getPort(), 1 };
      ManagementMessage message = client.sendCommand(ManagerCommand.ChangeListenerStatus.toString(), params);
      if (OperationStartus.OK.equals(message.getResult().getOperationStartus())) {
        int newStatus=(Integer) message.getData();
        listener.setStatus(newStatus);
        part.refreshVisuals();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
