// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.repository.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorReference;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.FolderType;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.mdm.repository.core.AbstractRepositoryAction;
import org.talend.mdm.repository.core.command.CommandManager;
import org.talend.mdm.repository.core.command.ICommand;
import org.talend.mdm.repository.core.service.ContainerCacheService;
import org.talend.mdm.repository.i18n.Messages;
import org.talend.mdm.repository.model.mdmproperties.ContainerItem;
import org.talend.mdm.repository.model.mdmproperties.MDMServerObjectItem;
import org.talend.mdm.repository.model.mdmserverobject.MDMServerObject;
import org.talend.mdm.repository.models.FolderRepositoryObject;
import org.talend.mdm.repository.utils.RepositoryResourceUtil;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;

import com.amalto.workbench.image.EImage;
import com.amalto.workbench.image.ImageCache;

/**
 * DOC hbhong class global comment. Detailled comment
 */
public class RemoveFromRepositoryAction extends AbstractRepositoryAction {

    static Logger log = Logger.getLogger(RemoveFromRepositoryAction.class);

    IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();

    private List<Object> lockedObjs;
    /**
     * DOC hbhong RemoveFromRepositoryAction constructor comment.
     * 
     * @param text
     */
    public RemoveFromRepositoryAction() {
        super(Messages.RemoveFromRepositoryAction_removeFromRepository);
        setImageDescriptor(ImageCache.getImage(EImage.DELETE_OBJ.getPath()));
    }

    @Override
    public String getGroupName() {
        return GROUP_EDIT;
    }

    @Override
    protected boolean needValidateLockedObject() {
        return true;
    }

    protected void doRun() {
        List<Object> selectedObject = getSelectedObject();
        int size = selectedObject.size();
        if (size > 0) {
            if (!MessageDialog.openConfirm(getShell(), Messages.RemoveFromRepositoryAction_Title, Messages.bind(
                    Messages.RemoveFromRepositoryAction_confirm, size, size > 1 ? Messages.RemoveFromRepositoryAction_instances
                            : Messages.RemoveFromRepositoryAction_instance))) {
                return;
            }

        }

        selectedObject.removeAll(lockedObjs);
        for (Object obj : selectedObject) {
            if (obj instanceof IRepositoryViewObject) {
                IRepositoryViewObject viewObj = (IRepositoryViewObject) obj;
                if (isServerObject(viewObj)) {
                    removeServerObject(viewObj);
                } else if (RepositoryResourceUtil.hasContainerItem(obj, FolderType.FOLDER_LITERAL)) {
                    removeFolderObject(viewObj);
                }

            }
        }

        try {
            factory.saveProject(ProjectManager.getInstance().getCurrentProject());
        } catch (PersistenceException e) {
            log.error(e.getMessage(), e);
        }

        commonViewer.refresh();

        if (lockedObjs.size() > 0)
            MessageDialog.openError(getShell(), Messages.AbstractRepositoryAction_lockedObjTitle, getAlertMsg());
    }

    private boolean isServerObject(IRepositoryViewObject viewObj) {
        return viewObj.getProperty().getItem() instanceof MDMServerObjectItem;
    }

    private void removeServerObject(IRepositoryViewObject viewObj) {
        try {
            Item item = viewObj.getProperty().getItem();
            IEditorReference ref = RepositoryResourceUtil.isOpenedInEditor(viewObj);

            if (ref != null) {
                RepositoryResourceUtil.closeEditor(ref, true);
            }
            MDMServerObject serverObj = ((MDMServerObjectItem) item).getMDMServerObject();

            factory.deleteObjectLogical(viewObj);
            CommandManager.getInstance().pushCommand(ICommand.CMD_DELETE, viewObj.getId(), serverObj.getName());
        } catch(BusinessException e) {
            MessageDialog.openError(getShell(), Messages.Common_Error, e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void removeFolderObject(IRepositoryViewObject viewObj) {
        for (IRepositoryViewObject childObj : viewObj.getChildren()) {
            if (childObj instanceof FolderRepositoryObject) {
                removeFolderObject(childObj);
            } else {
                removeServerObject(childObj);
            }
        }
        //
        ContainerItem containerItem = (ContainerItem) viewObj.getProperty().getItem();
        String path = containerItem.getState().getPath();
        ERepositoryObjectType repObjType = containerItem.getRepObjType();

        ContainerCacheService.removeContainer(repObjType, path);

        FolderItem folderItem = factory.getFolderItem(ProjectManager.getInstance().getCurrentProject(), repObjType,
                new Path(path));
        folderItem.getState().setDeleted(true);

    }

    @Override
    protected boolean isLocked() {
        initLockedObjectArray();

        boolean unlocked = false;

        List<Object> selectedObject = getSelectedObject();
        if (selectedObject != null && !selectedObject.isEmpty()) {
            for (Object obj : selectedObject) {
                if (obj instanceof IRepositoryViewObject) {
                    boolean locked = RepositoryResourceUtil.isLockedAndEdited((IRepositoryViewObject) obj);
                    if (locked) {
                        lockedObjs.add(obj);
                        continue;
                    }

                    unlocked = true;
                }
            }
        }

        return !unlocked;
    }

    private void initLockedObjectArray() {
        if (lockedObjs == null)
            lockedObjs = new ArrayList<Object>();
        lockedObjs.clear();
    }

    protected String getAlertMsg() {
        return Messages.bind(Messages.RemoveFromRepositoryAction_AlterLockMsg, lockedObjs.size());
    }

    @Override
    public boolean isVisible(IRepositoryViewObject viewObj) {
        String path = viewObj.getPath();
        if (path != null && path.equalsIgnoreCase("system")) { //$NON-NLS-1$
            return false;
        }
        return true;
    }
}
