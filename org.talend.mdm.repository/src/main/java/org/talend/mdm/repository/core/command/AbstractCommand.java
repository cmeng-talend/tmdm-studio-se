// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.repository.core.command;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IMemento;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.mdm.repository.model.mdmproperties.MDMServerObjectItem;

/**
 * DOC hbhong class global comment. Detailled comment
 */
public abstract class AbstractCommand implements ICommand {

    protected IRepositoryViewObject viewObject;

    public IRepositoryViewObject getViewObject() {
        return this.viewObject;
    }

    protected String commandId;

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    protected String objName;

    protected String lastName;

    public String getObjName() {
        return this.objName;
    }

    public String getObjLastName() {
        return this.lastName;
    }

    public AbstractCommand(IRepositoryViewObject viewObject) {
        init(viewObject);
    }

    public AbstractCommand() {
    }

    public abstract int getCommandType();

    public String getCommandId() {
        return commandId;
    }

    public abstract IStatus execute(Object params, IProgressMonitor monitor);

    public void restoreState(IMemento aMemento) {
        commandId = aMemento.getString(PROP_ID);
        this.objName = aMemento.getString(PROP_OBJ_NAME);
        this.lastName = aMemento.getString(PROP_LAST_OBJ_NAME);

    }

    public void saveState(IMemento aMemento) {
        aMemento.putString(PROP_ID, commandId);
        aMemento.putInteger(PROP_TYPE, getCommandType());
        aMemento.putString(PROP_OBJ_NAME, objName);
        aMemento.putString(PROP_LAST_OBJ_NAME, lastName);
    }

    public void init(IRepositoryViewObject viewObj) {
        this.viewObject = viewObj;
        this.commandId = viewObject.getId();
        //
        Item item = viewObj.getProperty().getItem();
        if (item != null) {
            if (item instanceof MDMServerObjectItem) {
                objName = ((MDMServerObjectItem) item).getMDMServerObject().getName();
                lastName = objName;
            } else if (item instanceof ProcessItem) {
                objName = viewObj.getLabel();
                lastName = objName;
            }
        }
    }

    public void init() {
    }

    public void init(String id, Object param) {
        this.commandId = id;
        if (param instanceof String) {
            this.objName = (String) param;
            lastName = objName;
        }
        if (param instanceof String[]) {
            String[] names = (String[]) param;
            lastName = names[1];
            objName = names[0];
        }
    }

    public void updateViewObject(IRepositoryViewObject viewObj) {
        this.viewObject = viewObj;
    }

}