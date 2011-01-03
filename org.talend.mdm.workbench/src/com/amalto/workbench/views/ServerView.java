package com.amalto.workbench.views;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.talend.mdm.commmon.util.core.CommonUtil;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.workbench.actions.AServerViewAction;
import com.amalto.workbench.actions.BrowseRevisionAction;
import com.amalto.workbench.actions.BrowseViewAction;
import com.amalto.workbench.actions.CopyXObjectAction;
import com.amalto.workbench.actions.DeleteJobAction;
import com.amalto.workbench.actions.DeleteXObjectAction;
import com.amalto.workbench.actions.DuplicateXObjectAction;
import com.amalto.workbench.actions.EditXObjectAction;
import com.amalto.workbench.actions.GenerateJobDefaultTransformerAction;
import com.amalto.workbench.actions.GenerateJobDefaultTriggerAction;
import com.amalto.workbench.actions.ImportTISJobAction;
import com.amalto.workbench.actions.NewCategoryAction;
import com.amalto.workbench.actions.NewXObjectAction;
import com.amalto.workbench.actions.PasteXObjectAction;
import com.amalto.workbench.actions.RefreshAllServerAction;
import com.amalto.workbench.actions.RefreshXObjectAction;
import com.amalto.workbench.actions.RenameXObjectAction;
import com.amalto.workbench.actions.ServerLoginAction;
import com.amalto.workbench.actions.ServerRefreshAction;
import com.amalto.workbench.availablemodel.AvailableModelUtil;
import com.amalto.workbench.availablemodel.IAvailableModel;
import com.amalto.workbench.dialogs.ErrorExceptionDialog;
import com.amalto.workbench.editors.XObjectBrowser;
import com.amalto.workbench.editors.XObjectEditor;
import com.amalto.workbench.export.ExportItemsAction;
import com.amalto.workbench.export.ImportItemsAction;
import com.amalto.workbench.image.EImage;
import com.amalto.workbench.image.ImageCache;
import com.amalto.workbench.models.IXObjectModelListener;
import com.amalto.workbench.models.TreeObject;
import com.amalto.workbench.models.TreeObjectTransfer;
import com.amalto.workbench.models.TreeParent;
import com.amalto.workbench.providers.ServerTreeContentProvider;
import com.amalto.workbench.providers.ServerTreeLabelProvider;
import com.amalto.workbench.providers.XObjectBrowserInput;
import com.amalto.workbench.providers.XtentisServerObjectsRetriever;
import com.amalto.workbench.utils.IConstants;
import com.amalto.workbench.utils.LocalTreeObjectRepository;
import com.amalto.workbench.utils.PasswordUtil;
import com.amalto.workbench.utils.UserInfo;
import com.amalto.workbench.utils.Util;
import com.amalto.workbench.utils.WorkbenchClipboard;
import com.amalto.workbench.utils.XtentisException;
import com.amalto.workbench.webservices.WSGetCurrentUniverse;
import com.amalto.workbench.webservices.WSLogout;
import com.amalto.workbench.webservices.WSUniverse;
import com.amalto.workbench.webservices.XtentisPort;

/**
 * The view allowing administration of the "+IConstants.TALEND+" Server
 * 
 * @author Bruno Grieder
 * 
 */
public class ServerView extends ViewPart implements IXObjectModelListener {

    public static final String VIEW_ID = "org.talend.mdm.workbench.views.ServerView";

    protected TreeViewer viewer;

    protected DrillDownAdapter drillDownAdapter;

    protected Action loginAction;

    protected Action logoutAction;

    protected Action newXObjectAction;

    protected Action renameXObjectAction;

    protected Action editXObjectAction;

    protected Action deleteXObjectAction;

    protected Action serverRefreshAction;

    protected Action refreshAllServerAction;

    // protected Action serverInitAction;
    protected Action browseViewAction;

    protected Action copyAction;

    protected Action pasteAction;

    protected Action duplicateAction;

    protected Action exportAction;

    protected Action importAction;

    protected Action newCategoryAction;

    // test for NewUserAction

    private ServerTreeContentProvider contentProvider;

    private ArrayList<TreeObject> dndTreeObjs = new ArrayList<TreeObject>();

    private int dragType = -1;

    // private static String f = System.getProperty("user.dir")+"/mdm_workbench_config.xml";
    private static String f = Platform.getInstanceLocation().getURL().getPath() + "/mdm_workbench_config.xml";

    private BrowseRevisionAction browseRevisionAction;

    /**********************************************************************************
     * The VIEW
     * 
     **********************************************************************************/

    /**
     * The constructor.
     */
    public ServerView() {
    }

    public static ServerView show() {
        IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(VIEW_ID);
        if (part == null) {
            try {
                part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(VIEW_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return (ServerView) part;
    }

    public TreeParent getRoot() {
        return contentProvider.getInvisibleRoot();
    }

    public java.util.List<XtentisPort> getPorts() {
        java.util.List<XtentisPort> ports = new ArrayList<XtentisPort>();
        XtentisPort port = null;
        try {
            TreeObject[] servers = contentProvider.getInvisibleRoot().getChildren();
            for (TreeObject server : servers) {
                if (server instanceof TreeParent) {
                    if (!(((TreeParent) server).getChildren().length == 1 && ((TreeParent) server).getChildren()[0]
                            .getDisplayName().equalsIgnoreCase("Pending...")))
                        ports.add(Util.getPort(server));
                }
            }
        } catch (XtentisException e) {
            e.printStackTrace();
        }
        return ports;
    }

    public java.util.List<TreeParent> getServers() {
        java.util.List<TreeParent> servs = new ArrayList<TreeParent>();
        TreeObject[] servers = contentProvider.getInvisibleRoot().getChildren();
        for (TreeObject server : servers) {
            if (server instanceof TreeParent) {
                if (!(((TreeParent) server).getChildren().length == 1 && ((TreeParent) server).getChildren()[0].getDisplayName()
                        .equalsIgnoreCase("Pending..."))) {
                    servs.add((TreeParent) server);
                }
            }
        }
        return servs;
    }

    private DragSource createTreeDragSource() {
        DragSource dragSource = new DragSource(viewer.getTree(), DND.DROP_MOVE | DND.DROP_COPY);
        dragSource.setTransfer(new Transfer[] { TreeObjectTransfer.getInstance() });
        dragSource.addDragListener(new DragSourceListener() {

            IStructuredSelection dndSelection = null;

            public void dragStart(DragSourceEvent event) {
                dragType = -1;
                dndSelection = (IStructuredSelection) viewer.getSelection();
                event.doit = dndSelection.size() > 0;
                dndTreeObjs.clear();
                for (Iterator<TreeObject> iter = dndSelection.iterator(); iter.hasNext();) {
                    TreeObject xobject = iter.next();
                    dndTreeObjs.add(xobject);
                    if ((dragType != -1 && dragType != xobject.getType()) || xobject.getType() == TreeObject.CATEGORY_FOLDER
                            || xobject.getParent().getType() == TreeObject.RESOURCES
                            || (LocalTreeObjectRepository.getInstance().isInSystemCatalog(xobject))
                            || (xobject.getServerRoot() == xobject.getParent())) {
                        event.doit = false;
                        break;
                    } else {
                        dragType = xobject.getType();
                    }
                }
            }

            public void dragFinished(DragSourceEvent event) {
                dndSelection = null;
            }

            public void dragSetData(DragSourceEvent event) {
                if (dndSelection == null || dndSelection.size() == 0)
                    return;
                if (!TreeObjectTransfer.getInstance().isSupportedType(event.dataType))
                    return;

                TreeObject[] sourceObjs = new TreeObject[dndSelection.size()];
                int index = 0;
                for (Iterator<TreeObject> iter = dndSelection.iterator(); iter.hasNext();) {
                    TreeObject xobject = iter.next();
                    sourceObjs[index++] = xobject;
                }
                event.data = sourceObjs;
            }
        });

        return dragSource;
    }

    private DropTarget createTreeDropTarget() {
        DropTarget dropTarget = new DropTarget(viewer.getTree(), DND.DROP_MOVE | DND.DROP_COPY);
        dropTarget.setTransfer(new Transfer[] { TreeObjectTransfer.getInstance() });
        dropTarget.addDropListener(new DropTargetAdapter() {

            public void dragEnter(DropTargetEvent event) {

            }

            public void dragLeave(DropTargetEvent event) {

            }

            public void dragOver(DropTargetEvent event) {
                dropTargetValidate(event);
                event.feedback |= DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
            }

            public void drop(DropTargetEvent event) {

                resetTargetTreeObject(event);
                if (dropTargetValidate(event))
                    dropTargetHandleDrop(event);
            }

            private void resetTargetTreeObject(DropTargetEvent event) {
                // Determine the target XObject for the drop
                IStructuredSelection dndSelection = (IStructuredSelection) viewer.getSelection();
                for (Iterator<TreeObject> iter = dndSelection.iterator(); iter.hasNext();) {
                    TreeObject xobject = iter.next();
                    if (!xobject.isXObject() && xobject instanceof TreeParent) {
                        if (dndTreeObjs.indexOf(xobject) >= 0)
                            dndTreeObjs.remove(xobject);
                        TreeParent dir = (TreeParent) xobject;
                        for (TreeObject treeObj : dir.getChildren()) {
                            if (dndTreeObjs.indexOf(treeObj) == -1)
                                dndTreeObjs.add(treeObj);
                        }
                    }
                }
            }
        });

        return dropTarget;
    }

    private boolean dropTargetValidate(DropTargetEvent event) {

        if (event.item == null)
            return false;
        Object obj = event.item.getData();
        if (obj instanceof TreeObject) {
            TreeObject treeObj = (TreeObject) obj;
            if (treeObj.getParent() == null)
                System.out.println(treeObj.getDisplayName());
            int xtentisType = LocalTreeObjectRepository.getInstance().receiveUnCertainTreeObjectType(treeObj);
            if ((treeObj.getType() != dragType && treeObj.getType() != TreeObject.CATEGORY_FOLDER && !(dragType == TreeObject.JOB
                    || dragType == TreeObject.TIS_JOB || dragType == TreeObject.WORKFLOW_PROCESS))
                    || dragType == TreeObject.CATEGORY_FOLDER
                    || dragType == TreeObject.DATA_MODEL_RESOURCE
                    || dragType == TreeObject.DATA_MODEL_TYPES_RESOURCE
                    || dragType == TreeObject.CUSTOM_TYPES_RESOURCE
                    || dragType == TreeObject.PICTURES_RESOURCE
                    || (treeObj.getType() == TreeObject.CATEGORY_FOLDER && xtentisType != dragType && !(dragType == TreeObject.JOB
                            || dragType == TreeObject.TIS_JOB || dragType == TreeObject.WORKFLOW_PROCESS))
                    || (treeObj.getType() == TreeObject.CATEGORY_FOLDER && treeObj.getParent().getType() == dragType && treeObj
                            .getDisplayName().equals("System"))
                    || (LocalTreeObjectRepository.getInstance().isInSystemCatalog(treeObj.getParent()))) {
                event.detail = DND.DROP_NONE;
            } else {
                for (TreeObject tos : dndTreeObjs) {
                    if (tos == obj) {
                        event.detail = DND.DROP_LINK;
                        break;
                    } else {
                        if (tos.getParent().getType() == TreeObject.CATEGORY_FOLDER
                                && tos.getParent().getDisplayName().equals("System")) {
                            event.detail = DND.DROP_NONE;
                        }
                        event.detail = DND.DROP_MOVE;
                    }
                }
            }
        }

        return event.detail != DND.DROP_NONE;
    }

    private void dropTargetHandleDrop(DropTargetEvent event) {
        TreeObject remoteObj = (TreeObject) event.item.getData();
        TreeParent parent = null;
        if (remoteObj instanceof TreeParent)
            parent = (TreeParent) remoteObj;
        else
            parent = remoteObj.getParent();
        if (parent.getParent().getType() == TreeObject.RESOURCES)
            return;
        // only for transfer
        ArrayList<TreeObject> subDdnList = new ArrayList<TreeObject>();

        if (parent != null) {
            for (TreeObject xobj : dndTreeObjs) {
                if (xobj.getServerRoot().getDisplayName().equals(remoteObj.getServerRoot().getDisplayName())) {
                    if (xobj.getParent() != remoteObj.getParent() && remoteObj.isXObject()) {
                        subDdnList.add(xobj);
                    } else if (xobj.getParent() != remoteObj && remoteObj instanceof TreeParent) {
                        subDdnList.add(xobj);
                    }
                    if (xobj.getType() == TreeObject.JOB || xobj.getType() == TreeObject.WORKFLOW_PROCESS)
                        subDdnList.add(xobj);
                }
            }
            dndTreeObjs.removeAll(subDdnList);
        }

        transformCatalog(remoteObj, subDdnList);

        if (!dndTreeObjs.isEmpty()) {
            WorkbenchClipboard.getWorkbenchClipboard().get().clear();
            WorkbenchClipboard.getWorkbenchClipboard().get().addAll(dndTreeObjs);
            ((PasteXObjectAction) pasteAction).setXtentisPort(remoteObj);
            ((PasteXObjectAction) pasteAction).setParent(remoteObj instanceof TreeParent ? (TreeParent) remoteObj : remoteObj
                    .getParent());
            pasteAction.run();
        }
    }

    public void forceAllSiteToRefresh() {
        // TreeObject[] childs = getTreeContentProvider().getInvisibleRoot().getChildren();
        // for (TreeObject child : childs) {
        // (new ServerRefreshAction(this, child.getServerRoot())).run();
        // }
    }

    private void transformCatalog(TreeObject remoteObj, ArrayList<TreeObject> transferList) {
        boolean transform = false;
        TreeParent catalog = remoteObj instanceof TreeParent ? (TreeParent) remoteObj : remoteObj.getParent();
        for (TreeObject theObj : transferList) {
            theObj.getParent().removeChild(theObj);
            catalog.addChild(theObj);
            theObj.setServerRoot(catalog.getServerRoot());
            transform = true;
        }

        if (transform && getTreeContentProvider().getInvisibleRoot().getChildren().length > 1) {
            forceAllSiteToRefresh();
        }
        getViewer().refresh(false);
    }

    protected class DCDragSourceListener implements DragSourceListener {

        private int selected;

        public void dragFinished(DragSourceEvent event) {
            Control control = ((DragSource) event.widget).getControl();
            if ((control instanceof List) && ((event.detail & DND.DROP_MOVE) == DND.DROP_MOVE)) {
                ((List) control).remove(selected);
            }
        }

        public void dragSetData(DragSourceEvent event) {
            Control control = ((DragSource) event.widget).getControl();
            if ((control instanceof List))
                if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
                    this.selected = ((List) control).getSelectionIndex();
                    event.data = ((List) control).getSelection()[0];
                }
        }

        public void dragStart(DragSourceEvent event) {
            Control control = ((DragSource) event.widget).getControl();
            if (control instanceof Tree) {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                if (selection.size() == 0) {
                    event.doit = false;
                }
            }
        }

    }

    protected TreeViewer createTreeViewer(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        return viewer;
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     */
    public void createPartControl(Composite parent) {

        viewer = createTreeViewer(parent);
        drillDownAdapter = new DrillDownAdapter(viewer);
        contentProvider = new ServerTreeContentProvider(this.getSite(), new TreeParent("INVISIBLE ROOT", null, TreeObject._ROOT_,
                null, null));
        setTreeContentProvider(contentProvider);
        viewer.setLabelProvider(new ServerTreeLabelProvider());
        viewer.setSorter(new ViewerSorter() {

            public int category(Object element) {
                if (element instanceof TreeParent) {
                    TreeParent category = (TreeParent) element;
                    if (category.getType() == TreeObject.CATEGORY_FOLDER)
                        return -1;
                }
                return 0;
            }
        });
        viewer.setInput(getViewSite());
        if (getSite().getWorkbenchWindow().getActivePage() != null) {
            getSite().getWorkbenchWindow().getActivePage().addPartListener(new IPartListener2() {

                public void partVisible(IWorkbenchPartReference partRef) {
                    // TODO Auto-generated method stub

                }

                public void partOpened(IWorkbenchPartReference partRef) {
                    // TODO Auto-generated method stub

                }

                public void partInputChanged(IWorkbenchPartReference partRef) {
                    // TODO Auto-generated method stub

                }

                public void partHidden(IWorkbenchPartReference partRef) {
                    // TODO Auto-generated method stub

                }

                public void partDeactivated(IWorkbenchPartReference partRef) {
                    // TODO Auto-generated method stub

                }

                public void partClosed(IWorkbenchPartReference partRef) {
                    // TODO Auto-generated method stub
                    System.gc();
                }

                public void partBroughtToTop(IWorkbenchPartReference partRef) {
                    // TODO Auto-generated method stub

                }

                public void partActivated(IWorkbenchPartReference partRef) {
                    // TODO Auto-generated method stub

                }
            });
        }
        ;
        viewer.getTree().addTreeListener(new TreeListener() {

            public void treeExpanded(TreeEvent event) {
                if (event.item.getData() instanceof TreeParent
                        && ((TreeParent) event.item.getData()).getType() == TreeObject._SERVER_
                        && ((TreeParent) event.item.getData()).getChildrenList().size() == 1) {
                    // ((TreeParent) event.item.getData()).getChildrenList().clear();
                    initServer((TreeParent) event.item.getData());
                }

            }

            public void treeCollapsed(TreeEvent e) {

            }
        });

        createTreeDragSource();
        createTreeDropTarget();
        makeActions();
        hookContextMenu();
        hookDoubleClickAction();
        hookKeyPressAction();
        contributeToActionBars();
        // hookKeyboard();
        initView();
    }

    protected void initServer(TreeParent server) {

        // add by ymli; fix the bug:0012600.made the refresh as a job run underground
        refreshServerRoot(this, server);

    }

    /**
     * @author ymli; fix the bug:0012600. made the refresh as a job run underground
     * @param view
     * @param serverRoot
     * @return
     */
    public boolean refreshServerRoot(final ServerView view, final TreeParent serverRoot) {
        // TODO Auto-generated method stub
        UIJob job = new UIJob("Pending ...") {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                try {

                    Cursor wait = new Cursor(getViewSite().getShell().getDisplay(), SWT.CURSOR_WAIT);
                    viewer.getControl().setCursor(wait);
                    new ServerRefreshAction(view, serverRoot).doRun();
                    viewer.expandToLevel(serverRoot, 1);

                    return Status.OK_STATUS;
                } catch (Exception e) {
                    MessageDialog.openError(view.getSite().getShell(), "Error", e.getLocalizedMessage());
                    viewer.collapseToLevel(serverRoot, 1);
                    return Status.CANCEL_STATUS;
                } finally {
                    viewer.getControl().setCursor(new Cursor(getViewSite().getShell().getDisplay(), SWT.CURSOR_ARROW));
                }
            }
        };
        job.setPriority(Job.LONG);
        job.schedule();

        return true;

    }

    public void initView() {
        SAXReader reader = new SAXReader();

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");
        File file = new File(f);
        if (file.exists()) {
            Document logininfoDocument = null;
            try {
                logininfoDocument = reader.read(file);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            Element root = logininfoDocument.getRootElement();
            // boolean bl = false;
            for (Iterator i = root.elementIterator("properties"); i.hasNext();) {
                Element server = (Element) i.next();
                String url = server.selectSingleNode("url").getText();
                String user = server.selectSingleNode("user").getText();
                String password = PasswordUtil.decryptPassword(server.selectSingleNode("password").getText());
                String universe = server.selectSingleNode("universe").getText();
                if (!("".equalsIgnoreCase(url) || "".equalsIgnoreCase(user) || "".equalsIgnoreCase(password)))
                    initServerTreeParent(url, user, password, universe);
                // initServerTree(url, user, password, universe);
            }
        }
        // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // DocumentBuilder builder;
        //
        // try {
        // builder = factory.newDocumentBuilder();
        // if(!new File(f).exists())
        // return;
        // else
        // logininfoDocument = builder.parse(new File(f));
        // } catch (SAXException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // } catch (ParserConfigurationException e) {
        // e.printStackTrace();
        // }
        // NodeList properties = logininfoDocument.getElementsByTagName("properties");
        // for (int i = 0; i < properties.getLength(); i++) {
        // Node server = properties.item(i);
        // NodeList serverInfo = server.getChildNodes();
        // for (int j = 0; j < serverInfo.getLength(); j++) {
        // serverInfo.item(j).getNodeName();
        //						
        // }
        // }
    }

    // /**
    // * // fliu add keyboard listener into tree to assist ctrl+c, ctrl+v and del
    // */
    // private void hookKeyboard() {
    // viewer.getControl().addKeyListener(new KeyListener() {
    //
    // public void keyPressed(KeyEvent e) {
    // }
    //
    // public void keyReleased(KeyEvent e) {
    // if (e.keyCode == 'c' && e.stateMask == SWT.CTRL) {
    // copyAction.run();
    // } else if (e.keyCode == 'v' && e.stateMask == SWT.CTRL) {
    // // modifier:fiu see bug 0008905
    // TreeObject xobject = (TreeObject) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
    // ((PasteXObjectAction) pasteAction).setXtentisPort(xobject);
    // ((PasteXObjectAction) pasteAction).setParent(xobject instanceof TreeParent ? (TreeParent) xobject : xobject
    // .getParent());
    // pasteAction.run();
    // } else if (e.keyCode == SWT.DEL) {
    // deleteXObjectAction.run();
    // }
    // }
    // });
    // }

    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu");
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                ServerView.this.fillContextMenu(manager);
            }
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        manager.add(loginAction);
        /*
         * manager.add(new Separator()); manager.add(logoutAction);
         */
    }

    protected void fillContextMenu(IMenuManager manager) {
        TreeObject xobject = (TreeObject) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
        try {
            XtentisPort port = Util.getPort(xobject);
            if (port == null)
                return;

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (xobject == null) {
            manager.add(loginAction);
        } else {
            // available models

            switch (xobject.getType()) {
            case TreeObject._SERVER_:
                manager.add(loginAction);
                manager.add(logoutAction);
                manager.add(serverRefreshAction);
                manager.add(importAction);
                manager.add(exportAction);

                if (!WorkbenchClipboard.getWorkbenchClipboard().isEmpty())
                    manager.add(pasteAction);

                break;

            case TreeObject._ACTION_:
                manager.add((Action) xobject.getWsObject());
                break;
            case TreeObject.SUBSCRIPTION_ENGINE:
                manager.add(browseViewAction);
                break;
            case TreeObject.CUSTOM_TYPE:
            case TreeObject.CUSTOM_TYPES_RESOURCE:
                // manager.add(uploadCustomTypeAction);
                break;
            case TreeObject.SERVICE_CONFIGURATION:
            case TreeObject.RESOURCES:
            case TreeObject.DATA_MODEL_RESOURCE:
            case TreeObject.DATA_MODEL_TYPES_RESOURCE:
                break;
            case TreeObject.PICTURES_RESOURCE:
                manager.add(exportAction);
                manager.add(importAction);
                break;
            case TreeObject.DATA_CLUSTER:
                if (xobject.isXObject()) {
                    manager.add(browseViewAction);
                }

            case TreeObject.ROLE:
            case TreeObject.VIEW:
            default:
                if (xobject.getType() != TreeObject.CATEGORY_FOLDER && xobject.getType() != TreeObject.BUILT_IN_CATEGORY_FOLDER) {
                    manager.add(exportAction);
                    manager.add(importAction);
                }
                if (xobject.getType() == TreeObject.VIEW && xobject.isXObject()) {
                    manager.add(browseViewAction);
                }
                if (xobject.getType() == TreeObject.JOB_REGISTRY) {
                    manager.add(new ImportTISJobAction());
                    manager.add(new RefreshXObjectAction(ServerView.show(), xobject));
                }
                if (xobject.getType() == TreeObject.JOB) {
                    manager.add(new DeleteJobAction());
                    manager.add(new GenerateJobDefaultTransformerAction());
                    manager.add(new GenerateJobDefaultTriggerAction());
                }
                int type = LocalTreeObjectRepository.getInstance().receiveUnCertainTreeObjectType(xobject);
                if (!LocalTreeObjectRepository.getInstance().isInSystemCatalog(xobject)
                        && xobject.getType() != TreeObject.WORKFLOW_PROCESS && xobject.getType() != TreeObject.JOB
                        && xobject.getType() != TreeObject.WORKFLOW && xobject.getType() != TreeObject.JOB_REGISTRY) {
                    if (type != TreeObject.ROLE && xobject.getType() != TreeObject.RESOURCES
                            && xobject.getType() != TreeObject.DATA_MODEL_RESOURCE
                            && xobject.getType() != TreeObject.DATA_MODEL_TYPES_RESOURCE
                            && xobject.getType() != TreeObject.CUSTOM_TYPES_RESOURCE
                            && xobject.getType() != TreeObject.PICTURES_RESOURCE
                            && xobject.getType() != TreeObject.BUILT_IN_CATEGORY_FOLDER
                            && xobject.getType() != TreeObject.EVENT_MANAGEMENT)
                        manager.add(newXObjectAction);
                    // edit by ymli; fix the bug:0012191; if the object is DATA_CLUSTER, refused to rename.
                    // if(!(xobject instanceof TreeParent))
                    if (!(xobject instanceof TreeParent) && xobject.getType() != TreeObject.DATA_CLUSTER)
                        manager.add(renameXObjectAction);
                }

                if (Util.IsEnterPrise() && Util.hasUniverse(xobject))
                    manager.add(browseRevisionAction);

                // if(xobject.getType()!=TreeObject.WORKFLOW_PROCESS && xobject.getType()!=TreeObject.JOB &&
                // Util.hasTags(xobject))
                // manager.add(versionAction);

                if (xobject.getType() != TreeObject.WORKFLOW_PROCESS && xobject.getType() != TreeObject.JOB
                        && xobject.isXObject() && !XSystemObjects.isExist(xobject.getType(), xobject.getDisplayName())) {
                    manager.add(editXObjectAction);
                    manager.add(deleteXObjectAction);
                    manager.add(copyAction);
                    manager.add(duplicateAction);
                } else if (xobject.getType() != TreeObject.EVENT_MANAGEMENT && xobject.getType() != TreeObject.JOB_REGISTRY
                        && xobject.getType() != TreeObject.JOB && xobject.getType() != TreeObject.BUILT_IN_CATEGORY_FOLDER
                        && LocalTreeObjectRepository.getInstance().isInSystemCatalog(xobject) == false) {
                    manager.add(newCategoryAction);
                }

                if (xobject.getType() == TreeObject.CATEGORY_FOLDER
                        && LocalTreeObjectRepository.getInstance().isInSystemCatalog(xobject) == false) {
                    manager.add(deleteXObjectAction);
                }
                if (!WorkbenchClipboard.getWorkbenchClipboard().isEmpty()) {
                    // modifier:fiu see bug 0008905
                    TreeObject remoteObj = (TreeObject) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                    ((PasteXObjectAction) pasteAction).setXtentisPort(remoteObj);
                    ((PasteXObjectAction) pasteAction).setParent(remoteObj instanceof TreeParent ? (TreeParent) remoteObj
                            : remoteObj.getParent());
                    manager.add(pasteAction);
                }

            }

            java.util.List<IAvailableModel> availablemodels = AvailableModelUtil.getAvailableModels();
            for (IAvailableModel model : availablemodels) {
                model.fillContextMenu(xobject, manager);
            }
        }
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
        // Other plug-ins can contribute there actions here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(loginAction);
        manager.add(new Separator());
        manager.add(refreshAllServerAction);
        manager.add(new Separator());
        manager.add(new Separator());
        drillDownAdapter.addNavigationActions(manager);
    }

    private void makeActions() {
        loginAction = new ServerLoginAction(this);

        logoutAction = new Action() {

            public void run() {
                TreeParent serverRoot = (TreeParent) ((IStructuredSelection) ServerView.this.viewer.getSelection())
                        .getFirstElement();

                final String universe = serverRoot.getUniverse();
                final String username = serverRoot.getUsername();
                final String password = serverRoot.getPassword();
                final String endpointAddress = serverRoot.getEndpointAddress();

                TreeParent root = serverRoot.getParent();

                LocalTreeObjectRepository.getInstance().switchOffListening();
                LocalTreeObjectRepository.getInstance().setLazySaveStrategy(false, (TreeParent) serverRoot);
                // add by ymli; fix the bug:0011948:
                // All the tabs related to an MDM server connection should go away when loging out
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                int length = page.getEditors().length;
                String version = "";
                String tabEndpointAddress = "";
                String unserName = null;
                int j = 0;
                for (int i = 0; i < length; i++) {
                    IEditorPart part = page.getEditors()[i - j];
                    if (part instanceof XObjectBrowser) {
                        version = ((TreeObject) ((XObjectBrowserInput) part.getEditorInput()).getModel()).getUniverse();
                        tabEndpointAddress = ((TreeObject) ((XObjectBrowserInput) part.getEditorInput()).getModel())
                                .getEndpointAddress();
                        unserName = ((TreeObject) ((XObjectBrowserInput) part.getEditorInput()).getModel()).getUsername();
                    } else if (part instanceof XObjectEditor) {
                        version = ((XObjectEditor) part).getInitialXObject().getServerRoot().getUniverse();
                        tabEndpointAddress = ((XObjectEditor) part).getInitialXObject().getServerRoot().getEndpointAddress();
                        unserName = ((XObjectEditor) part).getInitialXObject().getServerRoot().getUsername();
                    }
                    if (serverRoot.getUniverse().equals(version) && endpointAddress.equals(tabEndpointAddress)
                            && serverRoot.getUsername().equals(unserName)) {
                        page.closeEditor(part, false);
                        j++;
                    }
                }

                serverRoot.getParent().removeChild(serverRoot);
                ServerView.this.viewer.refresh();

                // attempt logout on the server side
                ServerView.this.viewer.getControl().getDisplay().syncExec(new Runnable() {

                    public void run() {
                        try {
                            Util.getPort(new URL(endpointAddress), universe, username, password).logout(new WSLogout());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                deleteReserved(endpointAddress, username, universe);
            }
        };

        logoutAction.setText("Logout");
        logoutAction.setToolTipText("Logout From the " + IConstants.TALEND + " Server");
        logoutAction.setImageDescriptor(ImageCache.getImage(EImage.LOGOUT.getPath()));

        editXObjectAction = new EditXObjectAction(this);
        newXObjectAction = new NewXObjectAction(this);
        renameXObjectAction = new RenameXObjectAction(this);
        browseRevisionAction = new BrowseRevisionAction(this);
        deleteXObjectAction = new DeleteXObjectAction(this);
        serverRefreshAction = new ServerRefreshAction(this);
        refreshAllServerAction = new RefreshAllServerAction(this);
        // serverInitAction = new ServerInitAction(this);
        browseViewAction = new BrowseViewAction(this);
        copyAction = new CopyXObjectAction(this);
        pasteAction = new PasteXObjectAction(this);
        duplicateAction = new DuplicateXObjectAction(this);

        exportAction = new ExportItemsAction(this);
        importAction = new ImportItemsAction(this);
        newCategoryAction = new NewCategoryAction(this);

    }

    private void hookDoubleClickAction() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                ISelection selection = ServerView.this.getViewer().getSelection();
                TreeObject xo = (TreeObject) ((IStructuredSelection) selection).getFirstElement();
                if (xo.getType() == TreeObject._ACTION_) {
                    Class<?> actionClass = (Class<?>) xo.getWsKey();
                    try {
                        AServerViewAction action = (AServerViewAction) actionClass.newInstance();
                        action.setServerView(ServerView.this);
                        action.run();
                    } catch (Exception ex) {
                        MessageDialog.openError(viewer.getControl().getShell(), "Error", "Unable to run action");
                    }
                    return;
                }// if action
                if (xo.getType() == TreeObject._SERVER_) {
                    if (((TreeParent) xo).getChildrenList().size() == 1)
                        initServer((TreeParent) xo);
                }
                if (xo.getType() == TreeObject.WORKFLOW)
                    return;
                if (xo.getWsObject() instanceof Action) {
                    ((Action) xo.getWsObject()).run();
                    return;
                }
                if (xo.getType() == TreeObject.SUBSCRIPTION_ENGINE || (xo.getType() == TreeObject.DATA_CLUSTER && xo.isXObject())
                        || xo.getType() == TreeObject.WORKFLOW_PROCESS)
                    browseViewAction.run();
                else
                    editXObjectAction.run();
            }
        });
    }

    protected void initServerContent(TreeObject xo) {
        String universe = "";
        String username = "";
        String password = "";
        if (xo.getWsObject().toString().contains("/")) {
            universe = xo.getWsObject().toString().split("/")[0];
            username = xo.getWsObject().toString().split("/")[1].split(":")[0];
            password = xo.getWsObject().toString().split("/")[1].split(":")[1];
        } else {
            username = xo.getWsObject().toString().split(":")[0];
            password = xo.getWsObject().toString().split(":")[1];
        }
        initServerTree(xo.getWsKey().toString(), username, password, universe);
        // xo.getServerRoot().getParent().removeChild(xo.getServerRoot());
        viewer.expandToLevel(xo, 1);
    }

    private void hookKeyPressAction() {
        viewer.getTree().addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {

                ISelection selection = ServerView.this.getViewer().getSelection();
                TreeObject xo = (TreeObject) ((IStructuredSelection) selection).getFirstElement();

                // delete
                if ((e.stateMask == 0) && (e.keyCode == SWT.DEL)) {

                    switch (xo.getType()) {

                    case TreeObject.JOB:
                        new DeleteJobAction().run();

                        break;
                    default:
                        // MessageDialog.openError(getSite().getShell(),
                        // "Error", "Unknown " + IConstants.TALEND
                        // + " Object Type: " + xo.getType());
                        deleteXObjectAction.run();
                        return;
                    }// switch

                }

            }

            public void keyReleased(KeyEvent e) {

                if (e.keyCode == 'c' && e.stateMask == SWT.CTRL) {
                    copyAction.run();
                } else if (e.keyCode == 'v' && e.stateMask == SWT.CTRL) {
                    // modifier:fiu see bug 0008905
                    TreeObject xobject = (TreeObject) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                    ((PasteXObjectAction) pasteAction).setXtentisPort(xobject);
                    ((PasteXObjectAction) pasteAction).setParent(xobject instanceof TreeParent ? (TreeParent) xobject : xobject
                            .getParent());
                    pasteAction.run();
                    // } else if (e.keyCode == SWT.DEL) {
                    // deleteXObjectAction.run();
                }

            }

        });

    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     * Access to the Tree Model
     * 
     */
    public ServerTreeContentProvider getTreeContentProvider() {
        return (ServerTreeContentProvider) viewer.getContentProvider();
    }

    public void setTreeContentProvider(ServerTreeContentProvider treeContentProvider) {
        ServerTreeContentProvider oldProvider = (ServerTreeContentProvider) this.viewer.getContentProvider();
        if (oldProvider != null)
            oldProvider.removeListener(this);

        viewer.setContentProvider(treeContentProvider);
        treeContentProvider.addListener(this);
    }

    public void handleEvent(int type, TreeObject parent, TreeObject child) {
        // System.out.println("VIEWER HANDLE EVENT "+type+" Parent:  "+(parent==
        // null
        // ?" no parent":parent.getDisplayName())+"-->"+child.getDisplayName());

        switch (type) {
        case IXObjectModelListener.NEED_REFRESH:
            new ServerRefreshAction(this, (TreeParent) child).run();
            break;
        }

        this.viewer.refresh(false);
    }

    public TreeViewer getViewer() {
        return viewer;
    }

    public void setViewer(TreeViewer viewer) {
        this.viewer = viewer;
    }

    public void dispose() {
        ServerTreeContentProvider oldProvider = (ServerTreeContentProvider) this.viewer.getContentProvider();
        if (oldProvider != null)
            oldProvider.removeListener(this);

        super.dispose();
    }

    public void initServerTreeParent(String url, String username, String password, String universe) {
        TreeParent serverRoot = new TreeParent(url, null, TreeObject._SERVER_, url, ("".equals(universe) ? "" : universe + "/")
                + username + ":" + (password == null ? "" : password));
        UserInfo user = new UserInfo();
        WSUniverse wUuniverse = null;
        XtentisPort port;
        try {
            port = Util.getPort(new URL(url), universe, username, password);
            wUuniverse = port.getCurrentUniverse(new WSGetCurrentUniverse());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (XtentisException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        user.setUsername(username);
        user.setPassword(password);
        user.setServerUrl(url);
        user.setUniverse(universe);
        user.setWsUuniverse(wUuniverse);

        serverRoot.setUser(user);
        if ("".equalsIgnoreCase(universe))
            universe = "HEAD";
        serverRoot.setDisplayName(url + " [" + universe + "] " + username);
        TreeObject obj = new TreeObject("Pending...", serverRoot, TreeObject._INVISIBLE, null, null);
        ArrayList list = new ArrayList() {
        };
        list.add(obj);
        serverRoot.setChildren(list);
        TreeParent invisibleRoot = getTreeContentProvider().getInvisibleRoot();
        invisibleRoot.addChild(serverRoot);

        // getViewer().refresh();
    }

    public void initServerTree(String url, String username, String password, String universe) {

        // Remove authenticator dialog
        Authenticator.setDefault(null);

        try {
            XtentisServerObjectsRetriever retriever = new XtentisServerObjectsRetriever(url, username, password, universe, this);
            new ProgressMonitorDialog(this.getSite().getShell()).run(true, true, retriever);

            TreeParent serverRoot = retriever.getServerRoot();
            TreeParent invisibleRoot = getTreeContentProvider().getInvisibleRoot();
            TreeObject[] serverRoots = invisibleRoot.getChildren();

            boolean found = false;
            for (int i = 0; i < serverRoots.length; i++) {
                // aiming add root displayName as unique ID of each server
                if (serverRoots[i].getDisplayName().equalsIgnoreCase(serverRoot.getDisplayName())) {
                    if (serverRoots[i].getWsKey().equals(serverRoot.getWsKey())) {
                        // server & universe already exists --> synchronize
                        if (serverRoots[i].getUser().getUniverse().equalsIgnoreCase(serverRoot.getUser().getUniverse())) {
                            found = true;
                            ((TreeParent) serverRoots[i]).synchronizeWith(serverRoot);
                        }
                    }
                }
            }
            if (!found)
                invisibleRoot.addChild(serverRoot);

            LocalTreeObjectRepository.getInstance().setLazySaveStrategy(false, serverRoot);
            getViewer().refresh();
            getViewer().expandToLevel(serverRoot, 1);
        } catch (InterruptedException ie) {
            return;
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
            deleteReserved(url, username, universe);
            ErrorExceptionDialog.openError(this.getSite().getShell(), "Error", CommonUtil.getErrMsgFromException(ite.getCause()));
        }
    }

    private void deleteReserved(String url, String user, String universe) {
        SAXReader reader = new SAXReader();

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");
        File file = new File(f);
        if (file.exists()) {
            Document logininfoDocument = null;
            try {
                logininfoDocument = reader.read(file);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
            Element root = logininfoDocument.getRootElement();
            deleteServer(root, url, user, universe);
            try {
                XMLWriter writer = new XMLWriter(new FileWriter(f));
                writer.write(logininfoDocument);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteServer(Element root, String url, String user, String universe) {
        java.util.List properties = root.elements("properties");
        for (Iterator iterator = properties.iterator(); iterator.hasNext();) {
            Element ele = (Element) iterator.next();
            if (ele.element("url").getText().equals(url) && ele.element("user").getText().equals(user)
                    && ele.element("universe").getText().equals(universe))
                root.remove(ele);
        }
    }
}