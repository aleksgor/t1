package com.nomad.cachewiever.views.serverview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.nomad.cachewiever.utility.Action;
//import com.nomad.model.ColleagueModel;
import com.nomad.cachewiever.views.model.Server;
import com.nomad.cachewiever.views.serverview.action.AddServerAction;
import com.nomad.cachewiever.views.serverview.action.ImportAction;
import com.nomad.cachewiever.views.serverview.action.DeleteAction;
import com.nomad.cachewiever.views.serverview.action.EditAction;
import com.nomad.cachewiever.views.serverview.action.OpenMemoryChartAction;
import com.nomad.cachewiever.views.serverview.action.OpenModelChartAction;
import com.nomad.io.serializer.MessageHeaderAssemblerV1;
import com.nomad.model.ServerModel;

public class ServerView extends ViewPart {
  public static final String ID = "com.nomad.CacheWiever.ServerView";
  private Shell shell;
  private TableViewer viewer;
  private final ArrayList<Server> servers = new ArrayList<Server>();

  @SuppressWarnings("unchecked")
  @Override
  public void init(IViewSite site, IMemento memento) throws PartInitException {

    super.init(site, memento);
    MessageHeaderAssemblerV1 assembler = new MessageHeaderAssemblerV1();
    if (memento != null) {
      String s = memento.getString("SERVERS");
      if (s != null) {
        try {
          ByteArrayInputStream inp = new ByteArrayInputStream(s.getBytes("UTF-8"));
          ArrayList<Server> ss=  (ArrayList<Server>) assembler.getObject(inp);
          for (Server server : ss) {
            servers.add(server);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void saveState(IMemento memento) {
    super.saveState(memento);
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    MessageHeaderAssemblerV1 assembler = new MessageHeaderAssemblerV1();
    try {
      assembler.storeObject(servers, data);
      memento.putString("SERVERS", data.toString("UTF-8"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * The content provider class is responsible for providing objects to the
   * view. It can wrap existing objects in adapters or simply return objects
   * as-is. These objects may be sensitive to the current input of the view, or
   * ignore it and always show the same content (like Task List, for example).
   */
  public class ViewContentProvider implements IStructuredContentProvider {
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
      return servers.toArray();
    }
  }

  public class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
    public String getColumnText(Object obj, int index) {
      Server server = (Server) obj;
      String name = server.getServerModel().getServerName();
      if (name == null) {
        name = "";
      } else {
        name += ": ";
      }
      return name + server.getManagerHost() + ":" + server.getManagerPort() ;
    }

    public Image getColumnImage(Object obj, int index) {
      return getImage(obj);
    }

    public Image getImage(Object obj) {
      return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
    }
  }

  /**
   * This is a callback that will allow us to create the viewer and initialize
   * it.
   */
  public void createPartControl(Composite parent) {
    shell = new Shell(parent.getDisplay());
    viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    viewer.setContentProvider(new ViewContentProvider());
    viewer.setLabelProvider(new ViewLabelProvider());
    // Provide the input to the ContentProvider
    viewer.setInput(servers);

    AddServerAction shpa = new AddServerAction(this);
    shell.addListener(Action.Connect.getCode(), shpa);

    // mouse

    // This is new code
    // First we create a menu Manager
    MenuManager menuManager = new MenuManager();
    menuManager.add(new ImportAction(this));
    menuManager.add(new EditAction(this));
    menuManager.add(new DeleteAction(this));
    menuManager.add(new OpenMemoryChartAction(this));
    menuManager.add(new OpenModelChartAction(this));

    menuManager.addMenuListener(new IMenuListener() {

      @Override
      public void menuAboutToShow(IMenuManager manager) {
        IContributionItem[] items = manager.getItems();
        for (IContributionItem iContributionItem : items) {
          iContributionItem.update(IAction.ENABLED);
        }
      }
    });

    Menu menu = menuManager.createContextMenu(viewer.getTable());
    // Set the MenuManager
    viewer.getTable().setMenu(menu);
    getSite().registerContextMenu(menuManager, viewer);
    // Make the selection available
    getSite().setSelectionProvider(viewer);

  }

  /**
   * Passing the focus request to the viewer's control.
   */
  public void setFocus() {
    viewer.getControl().setFocus();
  }

  public void addServer(String host, int port) {
    Server server = new Server(new ServerModel());
    server.setManagerHost(host);
    server.setManagerPort(port);
    servers.add(server);
    viewer.refresh();
  }

  public ArrayList<Server> getServers() {
    return servers;
  }

  public Server getColleagueModel() {
    int index = viewer.getTable().getSelectionIndex();
    Object o = viewer.getElementAt(index);
    return (Server) o;
  }

  public void refresh() {
    viewer.refresh();
  }

}