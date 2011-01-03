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
package org.talend.mdm.studio.test.storedprocedure;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.talend.mdm.studio.test.TalendSWTBotForMDM;

/**
 * 
 * DOC rhou class global comment. Detailled comment
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class StoredProcedureItemOperationTest extends TalendSWTBotForMDM {

    private SWTBotTreeItem spParentItem;

    private SWTBotTreeItem spItem;

    @Before
    public void runBeforeEveryTest() {
        spParentItem = serverItem.getNode("Stored Procedure [HEAD]");
        spParentItem.expand();

        spParentItem.contextMenu("New").click();
        SWTBotShell newstoredProcedureShell = bot.shell("New Stored Procedure");
        newstoredProcedureShell.activate();
        SWTBotText text = bot.textWithLabel("Enter a name for the new Instance:");
        text.setText("TestStoredProcedure");
        bot.buttonWithTooltip("OK").click();
        spItem = spParentItem.getNode("TestStoredProcedure");
        Assert.assertNotNull(spItem);
        sleep(2);
    }

    @Test
    public void storedProcedureEditTest() {
        SWTBotMenu editMenu = spItem.contextMenu("Edit");
        sleep();
        editMenu.click();
    }

    @Test
    public void storedProcedureCopyTest() {
        SWTBotMenu editMenu = spItem.contextMenu("Copy");
        editMenu.click();
        sleep();
        spItem.contextMenu("Paste").click();
        SWTBotShell pastestoredProcedureShell = bot.shell("Pasting instance TestStoredProcedure");
        pastestoredProcedureShell.activate();
        bot.text("CopyOfTestStoredProcedure").setText("PasteStoredProcedure");
        bot.button("OK").click();
        SWTBotTreeItem pasteNode = spParentItem.getNode("PasteStoredProcedure");
        Assert.assertNotNull(pasteNode);
        sleep(2);
    }

    @Test
    public void storedProcedureDuplicateTest() {
        SWTBotMenu duplicateMenu = spItem.contextMenu("Duplicate");
        duplicateMenu.click();
        SWTBotShell shell = bot.shell("Pasting instance TeststoredProcedure");
        shell.activate();
        bot.text("CopyOfTestStoredProcedure").setText("DuplicateStoredProcedure");
        sleep();
        bot.button("OK").click();
        SWTBotTreeItem duplicateNode = spParentItem.getNode("DuplicateStoredProcedure");
        Assert.assertNotNull(duplicateNode);
        sleep(2);

    }

    @Test
    public void storedProcedureRenameTest() {
        SWTBotMenu renameMenu = spItem.contextMenu("Rename");
        sleep();
        renameMenu.click();
        SWTBotShell renameShell = bot.shell("Rename");
        renameShell.activate();
        bot.textWithLabel("Please enter a new name").setText("RenameStoredProcedure");
        bot.button("OK").click();
        sleep();
        Assert.assertNull(spParentItem.getNode("TestStoredProcedure"));
        Assert.assertNotNull(spParentItem.getNode("RenameStoredProcedure"));
        sleep(2);
    }

    @After
    public void runAfterEveryTest() {
        spParentItem.getNode("RenameStoredProcedure").contextMenu("Delete").click();
        sleep();
        bot.button("OK").click();
        sleep();

        spParentItem.getNode("PasteStoredProcedure").contextMenu("Delete").click();
        sleep();
        bot.button("OK").click();
        sleep();

        spParentItem.getNode("DuplicateStoredProcedure").contextMenu("Delete").click();
        sleep();
        bot.button("OK").click();
        sleep();
    }
}