package com.nomad.cachewiever.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.nomad.cachewiever.editors.model.CommandPluginNode;
import com.nomad.cachewiever.editors.model.Connection;
import com.nomad.cachewiever.editors.model.ConnectionCreationFactory;
import com.nomad.cachewiever.editors.model.DataSourceNode;
import com.nomad.cachewiever.editors.model.RootObject;
import com.nomad.cachewiever.editors.model.ListenerNode;
import com.nomad.cachewiever.editors.model.Node;
import com.nomad.cachewiever.editors.model.SaveClientNode;
import com.nomad.cachewiever.editors.model.SaveServerNode;
import com.nomad.cachewiever.editors.model.ServerNode;
import com.nomad.cachewiever.editors.model.SessionClientNode;
import com.nomad.cachewiever.editors.model.SessionServerNode;
import com.nomad.cachewiever.editors.model.StoreModelNode;
import com.nomad.cachewiever.editors.part.AppEditPartFactory;
import com.nomad.cachewiever.editors.part.tree.AppTreeEditPartFactory;
import com.nomad.cachewiever.file.SaveCache;
import com.nomad.cachewiever.views.model.Server;
import com.nomad.client.ManagerClient;
import com.nomad.message.ManagementMessage;
import com.nomad.model.ConnectModel;
import com.nomad.model.CommandPluginModel;
import com.nomad.model.DataSourceModel;
import com.nomad.model.ListenerModel;
import com.nomad.model.ManagerCommand;
import com.nomad.model.ServerModel;
import com.nomad.model.StoreModel;

public class CacheEditor extends GraphicalEditorWithFlyoutPalette implements IPropertyListener {

  private RootObject root;
  private KeyHandler keyHandler;
  public static final String ID = "com.nomad.cachewiever.editors.CacheEditor";
  
  private Map<String, Server> storeAdd = new HashMap<String, Server>();


  public CacheEditor() {
    setEditDomain(new DefaultEditDomain(this));
  }

  private RootObject createEntreprise() {
    RootObject psyEntreprise = new RootObject();

    return psyEntreprise;
  }

  @Override
  protected void initializeGraphicalViewer() {

    GraphicalViewer viewer = getGraphicalViewer();
    if (root == null) {
      root = createEntreprise();
    }
    viewer.setContents(root);
  }

  public void setRoot(RootObject root) {
    this.root = root;
    getGraphicalViewer().setContents(root);
  }

  public void addServers(List<Server> servers) {
    addServersNode(servers);
  }

  @Override
  protected PaletteRoot getPaletteRoot() {
    PaletteRoot root = new PaletteRoot();
    PaletteGroup manipGroup = new PaletteGroup("Manipulation Objects");
    root.add(manipGroup);

    SelectionToolEntry selectionToolEntry = new SelectionToolEntry();
    manipGroup.add(selectionToolEntry);
    manipGroup.add(new MarqueeToolEntry());

    PaletteSeparator sep2 = new PaletteSeparator();
    root.add(sep2);
    PaletteGroup instGroup = new PaletteGroup("Creation d'elemnts");
    root.add(instGroup);
    /*
     * instGroup.add(new CombinedTemplateCreationEntry("Service",
     * "Creation d'un service type", Service.class, new
     * NodeCreationFactory(Service.class),
     * AbstractUIPlugin.imageDescriptorFromPlugin("TutoGEF",
     * "icons/elements_obj.gif"),
     * AbstractUIPlugin.imageDescriptorFromPlugin("TutoGEF",
     * "icons/elements_obj.gif"))); instGroup .add(new
     * CombinedTemplateCreationEntry("Employe", "Creation d'un employe model",
     * Employe.class, new NodeCreationFactory(Employe.class),
     * AbstractUIPlugin.imageDescriptorFromPlugin("TutoGEF",
     * "icons/element.gif"),
     * AbstractUIPlugin.imageDescriptorFromPlugin("TutoGEF",
     * "icons/element.gif")));
     */
    PaletteDrawer connectionElements = new PaletteDrawer("Connecting Elements");
    root.add(connectionElements);

    connectionElements.add(new ConnectionCreationToolEntry("deliver design", "Create Connections", new ConnectionCreationFactory(Connection.CONNECTION_DESIGN),
        AbstractUIPlugin.imageDescriptorFromPlugin("TutoGEF", "icons/arrow_left.gif"), AbstractUIPlugin.imageDescriptorFromPlugin("TutoGEF",
            "icons/arrow_left.gif")));

    connectionElements.add(new ConnectionCreationToolEntry("deliver resources", "Create Connections", new ConnectionCreationFactory(
        Connection.CONNECTION_RESOURCES), AbstractUIPlugin.imageDescriptorFromPlugin("TutoGEF", "icons/arrow_left.gif"), AbstractUIPlugin
        .imageDescriptorFromPlugin("TutoGEF", "icons/arrow_left.gif")));
    connectionElements.add(new ConnectionCreationToolEntry("distribute work packages", "Link Layers", new ConnectionCreationFactory(
        Connection.CONNECTION_WORKPACKAGES), AbstractUIPlugin.imageDescriptorFromPlugin("TutoGEF", "icons/arrow_left.gif"), AbstractUIPlugin
        .imageDescriptorFromPlugin("TutoGEF", "icons/arrow_left.gif")));

    root.setDefaultEntry(selectionToolEntry);
    return root;
  }

  @Override
  protected PaletteViewerProvider createPaletteViewerProvider() {
    return new PaletteViewerProvider(getEditDomain()) {
      protected void configurePaletteViewer(PaletteViewer viewer) {
        super.configurePaletteViewer(viewer);
        viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
      }
    };
  }

  public ScalableFreeformRootEditPart getRootEditPart() {
    if (rootEditPart == null) {
      rootEditPart = new ScalableFreeformRootEditPart();
    }
    return rootEditPart;

  }

  private ScalableFreeformRootEditPart rootEditPart;

  public void addServer(Server server) {

    Collection<Server> servers = getAllservers(server.getServerModel()).values();

    addServersNode(servers);

  }

  public void addServersNode(Collection<Server> servers) {

    int x = 10;
    int y = 10;

    for (Server server : servers) {

      String key = server.getServerModel().getHost() + ":" + server.getServerModel().getManagementPort();
      if (storeAdd.get(key) == null) {
        storeAdd.put(key, server);

        ServerNode serverNode = new ServerNode(server);
        serverNode.addPropertyChangeListener(new EditorPropertyChangeListener());

        if (server.getX() == 0 && server.getY() == 0) {
          serverNode.setLayout(new Rectangle(x, y, 250, 200));
          x += 260;
          if (x > 1500) {
            x = 0;
            y += 220;
          }

        } else {

          serverNode.setLayout(new Rectangle(server.getX(), server.getY(), server.getW(), server.getH()));

        }
        for (ListenerModel listener : server.getServerModel().getListeners()) {
          ListenerNode ln = new ListenerNode(listener);
          ln.addPropertyChangeListener(new EditorPropertyChangeListener());
          serverNode.addChild(ln);
        }

        if (server.getServerModel().getSessionServerModel() != null) {
          SessionServerNode ssm = new SessionServerNode(server.getServerModel().getSessionServerModel());
          serverNode.addChild(ssm);
        }
        if (server.getServerModel().getSessionClientModel() != null) {
          SessionClientNode scm = new SessionClientNode(server.getServerModel().getSessionClientModel());
          serverNode.addChild(scm);
        }

        if (server.getServerModel().getSaveServerModel() != null) {
          SaveServerNode ssm = new SaveServerNode(server.getServerModel().getSaveServerModel());
          serverNode.addChild(ssm);
        }
        if (server.getServerModel().getSaveClientModel() != null) {
          SaveClientNode ssm = new SaveClientNode(server.getServerModel().getSaveClientModel());
          serverNode.addChild(ssm);
        }

        for (Entry<String, DataSourceModel> eds : server.getServerModel().getDataSources().entrySet()) {
          DataSourceNode dsn = new DataSourceNode(eds.getValue());
          dsn.addPropertyChangeListener(new EditorPropertyChangeListener());
          serverNode.addChild(dsn);
        }
        for (StoreModel model : server.getServerModel().getStoreModels()) {
          StoreModelNode dsn = new StoreModelNode(model);
          dsn.addPropertyChangeListener(new EditorPropertyChangeListener());

          serverNode.addChild(dsn);
        }
        for (CommandPluginModel model : server.getServerModel().getCommandPlugins()) {
          CommandPluginNode dsn = new CommandPluginNode(model);
          dsn.addPropertyChangeListener(new EditorPropertyChangeListener());
          serverNode.addChild(dsn);
        }

        root.addChild(serverNode);
      }

    }

    List<Connection> connections = new ArrayList<Connection>();
    for (Node node : root.getChildrenArray()) {
      if (node instanceof ServerNode) {
        ServerNode srv = (ServerNode) node;
        connections.addAll(getConnectChild(srv, root.getChildrenArray()));
      }
    }

    for (Connection connect : connections) {
      connect.connect();
    }

  }

  private Map<String, Server> getAllservers(ServerModel serverModel) {
    Map<String, Server> result = new HashMap<String, Server>();

    result.put(serverModel.getServerName() + ":" + serverModel.getManagementPort(), new Server(serverModel));

    List<ConnectModel> colleages = serverModel.getServers();
    for (ConnectModel colleagueModel : colleages) {
      try {
        ManagerClient client = new ManagerClient(colleagueModel.getServerHost(), colleagueModel.getServerManagementPort());
        ManagementMessage message = client.sendCommand(ManagerCommand.GetServerInfo.toString(), null);
        ServerModel coll = (ServerModel) message.getData();
        result.putAll(getAllservers(coll));
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    return result;
  }

  private ServerNode getServerNode(List<Node> storedServers, String host, int managementPort) {
    for (Node node : storedServers) {
      if (node instanceof ServerNode) {
        ServerNode server = (ServerNode) node;
        ServerModel cl = server.getServer().getServerModel();
        if (cl.getManagementPort() == managementPort && host.equals(cl.getHost())) {
          return server;
        }

      }
    }
    return null;
  }

  private List<Connection> getConnectChild(ServerNode server, List<Node> storedServers) {
    List<Connection> result = new ArrayList<Connection>();
    int managementPort = server.getServer().getServerModel().getManagementPort();
    String managementHost = server.getServer().getServerModel().getHost();

    List<ConnectModel> clientServers = server.getServer().getServerModel().getServers();
    for (ConnectModel serverModel : clientServers) {

      int port = serverModel.getServerPort();
      String host = serverModel.getServerHost();

      for (Node node : storedServers) {
        if (node instanceof ServerNode) {
          ServerNode srv = (ServerNode) node;
          ServerModel srvModel=srv.getServer().getServerModel();
          List<Node> chnodes = srv.getChildrenArray();
          for (Node node2 : chnodes) {
            if (node2 instanceof ListenerNode) {
              ListenerNode listener = (ListenerNode) node2;
              if (host.equals(srvModel.getHost()) && port == listener.getListener().getPort()) {
                Connection connect = new Connection( listener, getServerNode(storedServers, managementHost, managementPort), 1, serverModel);
                result.add(connect);

              }
            }
          }
        }

      }
      /*
       * 
       * if (listener instanceof ListenerNode) {
       * 
       * List<ColleagueModel> children =
       * srv.getServer().getColleague().getClients(); for (ColleagueModel
       * colleagueChildren : children) { for (Node listener : listeners) { if
       * (listener instanceof ListenerNode) { ListenerNode lnode =
       * (ListenerNode) listener;
       * 
       * if
       * (srv.getServer().getColleague().getHost().equals(colleagueChildren.getHost
       * ()) && lnode.getListener().getPort() == colleagueChildren.getPort()) {
       * // if
       * (!(srv.getServer().getColleague().getHost().equals(server.getServer
       * ().getColleague().getHost()) && srv.getServer().getColleague() //
       * .getManagementPort() ==
       * server.getServer().getColleague().getManagementPort())) { Connection
       * connect = new Connection(lnode, srv, 1); result.add(connect); // } }
       * 
       * } } } }
       */
    }
    return result;
  }

  @Override
  protected void configureGraphicalViewer() {
    double[] zoomLevels;
    ArrayList<String> zoomContributions;
    super.configureGraphicalViewer();

    GraphicalViewer viewer = getGraphicalViewer();
    viewer.setEditPartFactory(new AppEditPartFactory());

    ScalableRootEditPart rootEditPart = new ScalableRootEditPart();
    viewer.setRootEditPart(rootEditPart);

    ZoomManager manager = rootEditPart.getZoomManager();
    getActionRegistry().registerAction(new ZoomInAction(manager));
    getActionRegistry().registerAction(new ZoomOutAction(manager));
    // La liste des zooms possible. 1 = 100%
    zoomLevels = new double[] { 0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 10.0, 20.0 };
    manager.setZoomLevels(zoomLevels);

    zoomContributions = new ArrayList<String>();
    zoomContributions.add(ZoomManager.FIT_ALL);
    zoomContributions.add(ZoomManager.FIT_HEIGHT);
    zoomContributions.add(ZoomManager.FIT_WIDTH);
    manager.setZoomLevelContributions(zoomContributions);

    keyHandler = new KeyHandler();
    keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0), getActionRegistry().getAction(ActionFactory.DELETE.getId()));
    keyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0), getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
    keyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT, 0), getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));
    viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.NONE), MouseWheelZoomHandler.SINGLETON);

    viewer.setKeyHandler(keyHandler);
    ContextMenuProvider provider = new AppContextMenuProvider(viewer, getActionRegistry());
    viewer.setContextMenu(provider);

    this.addListenerObject(this);
  }

  public void createActions() {
    super.createActions();
    /*
     * ActionRegistry registry = getActionRegistry(); IAction action = new
     * RenameAction(this); registry.registerAction(action);
     * getSelectionActions().add(action.getId()); action = new
     * CopyNodeAction(this); registry.registerAction(action);
     * getSelectionActions().add(action.getId()); action = new
     * PasteNodeAction(this); registry.registerAction(action);
     * getSelectionActions().add(action.getId());
     */
  }

  @SuppressWarnings("rawtypes")
  public Object getAdapter(Class type) {
    if (type == ZoomManager.class)
      return ((ScalableRootEditPart) getGraphicalViewer().getRootEditPart()).getZoomManager();
    if (type == IContentOutlinePage.class) {
      return new OutlinePage();
    }
    return super.getAdapter(type);
  }

  @Override
  protected void setInput(IEditorInput input) {
    super.setInput(input);

  }

  CommandStack result;

  @Override
  protected CommandStack getCommandStack() {

    if (result == null) {
      result = new CommandStack();
    }
    return result;
  }

  protected class OutlinePage extends ContentOutlinePage {
    private SashForm sash;
    private ScrollableThumbnail thumbnail;
    private DisposeListener disposeListener;

    public OutlinePage() {
      super(new TreeViewer());
    }

    public void createControl(Composite parent) {
      sash = new SashForm(parent, SWT.VERTICAL);
      getViewer().createControl(sash);
      getViewer().setEditDomain(getEditDomain());
      getViewer().setEditPartFactory(new AppTreeEditPartFactory());
      getViewer().setContents(root);
      getSelectionSynchronizer().addViewer(getViewer());

      IActionBars bars = getSite().getActionBars();
      ActionRegistry ar = getActionRegistry();
      bars.setGlobalActionHandler(ActionFactory.COPY.getId(), ar.getAction(ActionFactory.COPY.getId()));
      bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), ar.getAction(ActionFactory.PASTE.getId()));
      Canvas canvas = new Canvas(sash, SWT.BORDER);
      LightweightSystem lws = new LightweightSystem(canvas);
      thumbnail = new ScrollableThumbnail((Viewport) ((ScalableRootEditPart) getGraphicalViewer().getRootEditPart()).getFigure());
      thumbnail.setSource(((ScalableRootEditPart) getGraphicalViewer().getRootEditPart()).getLayer(LayerConstants.PRINTABLE_LAYERS));
      lws.setContents(thumbnail);
      disposeListener = new DisposeListener() {
        public void widgetDisposed(DisposeEvent e) {
          if (thumbnail != null) {
            thumbnail.deactivate();
            thumbnail = null;
          }
        }
      };
      getGraphicalViewer().getControl().addDisposeListener(disposeListener);
    }

    public void init(IPageSite pageSite) {
      super.init(pageSite);
      // On hook les actions de l'editeur sur la toolbar
      IActionBars bars = getSite().getActionBars();
      bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), getActionRegistry().getAction(ActionFactory.UNDO.getId()));
      bars.setGlobalActionHandler(ActionFactory.REDO.getId(), getActionRegistry().getAction(ActionFactory.REDO.getId()));
      bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), getActionRegistry().getAction(ActionFactory.DELETE.getId()));
      bars.updateActionBars();
      // On associe les raccourcis clavier de l'editeur a l'outline
      getViewer().setKeyHandler(keyHandler);
      ContextMenuProvider provider = new AppContextMenuProvider(getViewer(), getActionRegistry());
      getViewer().setContextMenu(provider);

    }

    public Control getControl() {
      return sash;
    }

    public void dispose() {
      getSelectionSynchronizer().removeViewer(getViewer());
      super.dispose();
    }
  }

  private boolean dirty = true;;

  @Override
  public void doSave(IProgressMonitor monitor) {

    SaveCache save = new SaveCache();
    File f = new File("/opt/t1/server1.xml");
    f = new File(f.getAbsolutePath());
    
     save.save(root, f);
    dirty = false;
    firePropertyChange(IEditorPart.PROP_DIRTY);

    /*
     * MessageHeaderAssemblerV1 assembler = new MessageHeaderAssemblerV1();
     * 
     * try { OutputStream data = new FileOutputStream(f);
     * assembler.storeObject(root, data); data.flush(); data.close();
     * 
     * } catch (Exception e) { e.printStackTrace(); }
     */
  }

  @Override
  public void doSaveAs() {
  }

  @Override
  public boolean isDirty() {
    return dirty;
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public void propertyChanged(Object source, int propId) {

  }

  private class EditorPropertyChangeListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
      dirty = true;
      firePropertyChange(IEditorPart.PROP_DIRTY);
    }

  }
}
