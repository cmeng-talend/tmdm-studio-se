// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.repository.core.impl.datamodel;

import java.util.List;

import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.mdm.repository.core.IServerObjectRepositoryType;
import org.talend.mdm.repository.core.impl.AbstractContentProvider;
import org.talend.mdm.repository.utils.RepositoryResourceUtil;
import org.talend.mdm.webservice.WSDataModel;

import com.amalto.workbench.models.TreeObject;

/**
 * DOC hbhong class global comment. Detailled comment <br/>
 * 
 */
public class DataModelContentProvider extends AbstractContentProvider {

    @Override
    protected List<IRepositoryViewObject> getViewObjFromSystemFolder(Item parentItem) {
        return RepositoryResourceUtil.findViewObjectsByType(IServerObjectRepositoryType.TYPE_DATAMODEL, parentItem,
                TreeObject.DATA_MODEL);
    }

    public Class<?> getWSObjectClass() {
        return WSDataModel.class;
    }
}
