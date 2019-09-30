package com.nomad.cachewiever.views.serverview.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.nomad.cachewiever.editors.CacheEditor;
import com.nomad.cachewiever.editors.MyEditorInput;
import com.nomad.cachewiever.views.model.Server;
import com.nomad.cachewiever.views.serverview.ServerView;
import com.nomad.client.ManagerClient;
import com.nomad.message.ManagementMessage;
import com.nomad.model.ManagerCommand;
import com.nomad.model.ServerModel;

public class ImportAction implements IAction {
  ServerView viewer;

  // private static Logger logger =
  // LoggerFactory.getLogger(ConnectAction.class);
  public ImportAction(ServerView viewer) {
    this.viewer = viewer;
  }

  @Override
  public void addPropertyChangeListener(IPropertyChangeListener listener) {

  }

  @Override
  public int getAccelerator() {

    return 0;
  }

  @Override
  public String getActionDefinitionId() {
    return null;
  }

  @Override
  public String getDescription() {

    return "Description";
  }

  @Override
  public ImageDescriptor getDisabledImageDescriptor() {
    return null;
  }

  @Override
  public HelpListener getHelpListener() {
    return null;
  }

  @Override
  public ImageDescriptor getHoverImageDescriptor() {
    return null;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return null;
  }

  @Override
  public IMenuCreator getMenuCreator() {
    return null;
  }

  @Override
  public int getStyle() {
    return IAction.AS_PUSH_BUTTON;
  }

  @Override
  public String getText() {

    return "Import claster configuration";
  }

  @Override
  public String getToolTipText() {

    return "Import claster configuration";
  }

  @Override
  public boolean isChecked() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isHandled() {
    return false;
  }

  @Override
  public void removePropertyChangeListener(IPropertyChangeListener listener) {

  }

  @Override
  public void run() {

  }

  @Override
  public void runWithEvent(Event event) {

    Server server = viewer.getColleagueModel();

    if (server != null) {
      try {
        ManagerClient client = new ManagerClient(server.getManagerHost(), server.getManagerPort());
        ManagementMessage message = client.sendCommand(ManagerCommand.GetServerInfo.toString(), null);
        ServerModel coll = (ServerModel) message.getData();
        if (coll != null) {
          server.setServer(coll);
        }

        viewer.refresh();

        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchPage wbPage = wb.getActiveWorkbenchWindow().getActivePage();
        try {
          CacheEditor editor = (CacheEditor) wbPage.getActiveEditor();
          if (editor == null) {
            editor = (CacheEditor) wbPage.openEditor(new MyEditorInput("Cashe"), CacheEditor.ID);
          }
          wbPage.activate(editor);
          editor.addServer(server);

        } catch (PartInitException e1) {
          e1.printStackTrace();
        }

      } catch (Exception e) {
        e.printStackTrace();
        MessageBox mb = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
        mb.setText("Connect problem");
        mb.setMessage(e.getMessage());
        mb.open();

      }
    }

  }

  @Override
  public void setActionDefinitionId(String id) {

  }

  @Override
  public void setChecked(boolean checked) {

  }

  @Override
  public void setDescription(String text) {

  }

  @Override
  public void setDisabledImageDescriptor(ImageDescriptor newImage) {

  }

  @Override
  public void setEnabled(boolean enabled) {

  }

  @Override
  public void setHelpListener(HelpListener listener) {

  }

  @Override
  public void setHoverImageDescriptor(ImageDescriptor newImage) {

  }

  @Override
  public void setId(String id) {

  }

  @Override
  public void setImageDescriptor(ImageDescriptor newImage) {

  }

  @Override
  public void setMenuCreator(IMenuCreator creator) {

  }

  @Override
  public void setText(String text) {

  }

  @Override
  public void setToolTipText(String text) {

  }

  @Override
  public void setAccelerator(int keycode) {

  }

}
