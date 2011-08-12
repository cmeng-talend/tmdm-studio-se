// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2011 Talend �C www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.mdm.repository.ui.navigator;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.navigator.CommonNavigator;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.mdm.repository.utils.RepositoryResourceUtil;

/**
 * DOC hbhong class global comment. Detailled comment <br/>
 * 
 */
public class MDMRepositoryView extends CommonNavigator {

    @Override
    public void createPartControl(Composite aParent) {
        super.createPartControl(aParent);
        initInput();
    }

    /**
     * DOC hbhong Comment method "initInput".
     */
    private void initInput() {
        IRepositoryViewObject[] categoryViewObjects = RepositoryResourceUtil.getCategoryViewObjects();

        getCommonViewer().setInput(categoryViewObjects);
        // getCommonViewer().addFilter(filter);
    }

}