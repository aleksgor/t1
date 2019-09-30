package com.nomad.cachewiever.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.nomad.cachewiever.editors.CacheEditor;
import com.nomad.cachewiever.editors.MyEditorInput;
import com.nomad.cachewiever.file.LoadCache;
import com.nomad.cachewiever.views.model.Server;
import com.nomad.model.ServerModel;

public class OpenCasheHandler extends AbstractHandler {

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    // File f = new File("/opt/t1/server1.xml");
    IWorkbench wb = PlatformUI.getWorkbench();
    IWorkbenchPage wbPage = wb.getActiveWorkbenchWindow().getActivePage();

    FileDialog fileDialog = new FileDialog(wb.getActiveWorkbenchWindow().getShell());
    fileDialog.setText("Select File");
    fileDialog.setFilterExtensions(new String[] { "*.xml" });
    // Put in a readable name for the filter
    // fileDialog.setFilterNames(new String[] { "Textfiles(*.txt)" });
    String selected = fileDialog.open();
    if (selected != null) {
      try {
        LoadCache load = new LoadCache();
        List<ServerModel> servers = load.parseServerList(new File(selected));
        List<Server> result = new ArrayList<Server>(servers.size());
        for (ServerModel serverModel : servers) {
          Server s = new Server(serverModel);
          s.setX(getProperty("x", serverModel));
          s.setY(getProperty("y", serverModel));
          s.setH(getProperty("h", serverModel));
          s.setW(getProperty("w", serverModel));

          result.add(s);
        }
        CacheEditor editor = (CacheEditor) wbPage.getActiveEditor();
        if (editor == null) {
          editor = (CacheEditor) wbPage.openEditor(new MyEditorInput("Cashe"), CacheEditor.ID);
        }
        wbPage.activate(editor);
        editor.addServers(result);

      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
    return null;
  }

  private int getProperty(String propertyName, ServerModel serverModel) {
    String propValue = serverModel.getProperties().getProperty(propertyName);
    if (propValue != null) {
      try {
        int result = Integer.parseInt(propValue);
        serverModel.getProperties().remove(propertyName);
        return result;
      } catch (NumberFormatException e) {
        ;
      }
    }
    return 0;
  }
}
