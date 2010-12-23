// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.studio.test.datacontainer;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.talend.mdm.studio.test.TalendSWTBotForMDM;

import com.amalto.workbench.editors.DataClusterBrowserMainPage;
import com.amalto.workbench.editors.XObjectBrowser;

/**
 * DataContainerContentOperationTest is SWTBot test class to test the operation on the records.
 * 
 * DOC rhou class global comment. Detailled comment
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class DataContainerContentOperationTest extends TalendSWTBotForMDM {

    private SWTBotTreeItem dataContainerItem;

    @Before
    public void runBeforeEveryTest() {
        dataContainerItem = serverItem.getNode("Data Container");
        dataContainerItem.expand();
    }

    @After
    public void runAfterEveryTest() {

    }

    @Test
    public void recordEditTest() {
        SWTBotTreeItem node = dataContainerItem.expandNode("System").getNode("PROVISIONING");
        node.doubleClick();
        bot.buttonWithTooltip("Search").click();
        sleep(2);
        XObjectBrowser ep = (XObjectBrowser) bot.activeEditor().getReference().getPart(true);
        Assert.assertNotNull(ep);
        DataClusterBrowserMainPage mainpage = (DataClusterBrowserMainPage) ep.getPage(0);
        Assert.assertNotNull(mainpage);
        Table table = mainpage.getResultsViewer().getTable();
        SWTBotTable conceptBotTree = new SWTBotTable(table);
        Assert.assertNotNull(conceptBotTree);
        conceptBotTree.select(1, 1);
        bot.table().contextMenu("Commit").click();
        SWTBotShell newDataContainerShell = bot.shell("XML Editor/Viewer");
        newDataContainerShell.activate();
    }
}
