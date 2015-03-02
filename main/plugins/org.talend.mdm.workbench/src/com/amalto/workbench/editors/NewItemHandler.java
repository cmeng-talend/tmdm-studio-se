// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.workbench.editors;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.amalto.workbench.dialogs.DOMViewDialog;
import com.amalto.workbench.dialogs.datacontainer.DataContainerDOMViewDialog;
import com.amalto.workbench.i18n.Messages;
import com.amalto.workbench.utils.Util;
import com.amalto.workbench.webservices.TMDMService;
import com.amalto.workbench.webservices.WsDataClusterPK;
import com.amalto.workbench.webservices.WsDataModelPK;
import com.amalto.workbench.webservices.WsDataModelPKArray;
import com.amalto.workbench.webservices.WsPutItem;
import com.amalto.workbench.webservices.WsPutItemWithReport;
import com.amalto.workbench.webservices.WsRegexDataModelPKs;

/**
 * created by liusongbo on 2014-2-19
 */
public class NewItemHandler {

    private static final Log log = LogFactory.getLog(NewItemHandler.class);

    private boolean created = false;

    public static NewItemHandler getNewInstance() {
        return new NewItemHandler();
    }

    public boolean createItemRecord(final TMDMService service, final Shell ashell, final WsDataClusterPK wsDataClusterPK,
            boolean isMaster) {
        if (service == null || wsDataClusterPK == null) {
            throw new IllegalArgumentException();
        }

        final Shell shell = ashell != null ? ashell : Display.getCurrent().getActiveShell();

        try {
            String xml = "<NewItem><NewElement></NewElement></NewItem>"; //$NON-NLS-1$

            WsDataModelPKArray dataModelPKs = service.getDataModelPKs(new WsRegexDataModelPKs("*")); //$NON-NLS-1$
            List<WsDataModelPK> dmPKs = dataModelPKs.getWsDataModelPKs();
            List<String> dataModels = new ArrayList<String>();
            if (dmPKs != null) {
                for (WsDataModelPK pk : dmPKs) {
                    if (!"XMLSCHEMA---".equals(pk.getPk())) { //$NON-NLS-1$
                        dataModels.add(pk.getPk());
                    }
                }
            }
            final DataContainerDOMViewDialog d = new DataContainerDOMViewDialog(shell, service, Util.parse(xml), dataModels,
                    DOMViewDialog.SOURCE_VIEWER, null, isMaster);
            d.addListener(new Listener() {

                public void handleEvent(Event event) {
                    if (event.button == DOMViewDialog.BUTTON_SAVE) {
                        // attempt to save
                        try {
                            WsPutItem putItem = new WsPutItem(false, wsDataClusterPK, "".equals(d.getDataModelName()) ? null //$NON-NLS-1$
                                    : new WsDataModelPK(d.getDataModelName()), d.getXML());
                            WsPutItemWithReport item = new WsPutItemWithReport(d.isBeforeVerification(), "genericUI", putItem);//$NON-NLS-1$
                            if (d.isTriggerProcess()) {
                                service.putItemWithReport(item);
                            } else {
                                service.putItem(putItem);
                            }
                            created = true;

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                            MessageDialog.openError(
                                    shell,
                                    Messages.DataClusterBrowserMainPage_100,
                                    Messages.bind(Messages.DataClusterBrowserMainPage_101,
                                            Util.formatErrorMessage(e.getLocalizedMessage())));
                            return;
                        }
                    }
                    d.close();
                }
            });

            d.setBlockOnOpen(true);
            d.open();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (!Util.handleConnectionException(shell, e, null)) {
                MessageDialog.openError(shell, Messages._Error,
                        Messages.bind(Messages.DataClusterBrowserMainPage_103, e.getLocalizedMessage()));
            }
        }

        return created;
    }
}
