// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.repository.core.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.ReferenceFileItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.mdm.repository.model.mdmmetadata.MDMServerDef;
import org.talend.mdm.repository.model.mdmproperties.MDMServerObjectItem;
import org.talend.mdm.repository.model.mdmproperties.MdmpropertiesPackage;
import org.talend.mdm.repository.model.mdmserverobject.MDMServerObject;
import org.talend.mdm.repository.plugin.RepositoryPlugin;
import org.talend.mdm.repository.ui.dialogs.consistency.ConfirmConflictMessageDialog;
import org.talend.mdm.repository.ui.dialogs.consistency.ConsistencyConflictDialog;
import org.talend.mdm.repository.ui.preferences.PreferenceConstants;
import org.talend.mdm.repository.utils.DigestUtil;

import com.amalto.workbench.utils.XtentisException;
import com.amalto.workbench.webservices.WSDigestValueKey;
import com.amalto.workbench.webservices.WSDigestValueTimeStamp;
import com.amalto.workbench.webservices.XtentisPort;

/**
 * created by HHB on 2013-7-18 Detailled comment
 * 
 */
public class ConsistencyService {

    public static final int CONFLICT_STRATEGY_DEFAULT = 0;

    public static final int CONFLICT_STRATEGY_OVERWRITE = 1;

    public static final int CONFLICT_STRATEGY_SKIP_DIFFERENCE = 2;

    public static class ConsistencyCheckResult {

        private boolean isCanceled;

        private List<IRepositoryViewObject> toDeployObjects;

        private List<IRepositoryViewObject> toSkipObjects;

        /**
         * user cancel the operation
         */
        public ConsistencyCheckResult() {
            isCanceled = true;
        }

        public ConsistencyCheckResult(Collection<IRepositoryViewObject> viewObjs) {
            this(new LinkedList<IRepositoryViewObject>(viewObjs), Collections.EMPTY_LIST);
        }

        public ConsistencyCheckResult(List<IRepositoryViewObject> deployedObjects, List<IRepositoryViewObject> skippededObjects) {
            this.toDeployObjects = deployedObjects;
            this.toSkipObjects = skippededObjects;
            isCanceled = false;
        }

        public List<IRepositoryViewObject> getToDeployObjects() {
            return this.toDeployObjects;
        }

        public List<IRepositoryViewObject> getToSkipObjects() {
            return this.toSkipObjects;
        }

        public boolean isCanceled() {
            return this.isCanceled;
        }
    }

    enum DigestCalStrategyEnum {
        // compare the object 's resource file
        OBJ_RESOURCE,
        // only compare the reference file
        REF_FILE,
        // same with OBJ_RESOURCE except the property is sorted
        SORTED_OBJ_RESOURCE

    }

    public class SortEMFCopier extends EcoreUtil.Copier {

        @Override
        protected void copyAttribute(EAttribute eAttribute, EObject eObject, EObject copyEObject) {

            if (eObject.eIsSet(eAttribute)) {
                if (FeatureMapUtil.isFeatureMap(eAttribute)) {
                    FeatureMap featureMap = (FeatureMap) eObject.eGet(eAttribute);
                    for (int i = 0, size = featureMap.size(); i < size; ++i) {
                        EStructuralFeature feature = featureMap.getEStructuralFeature(i);
                        if (feature instanceof EReference && ((EReference) feature).isContainment()) {
                            Object value = featureMap.getValue(i);
                            if (value != null) {
                                copy((EObject) value);
                            }
                        }
                    }
                } else if (eAttribute.isMany()) {
                    List<?> source = (List<?>) eObject.eGet(eAttribute);

                    @SuppressWarnings("unchecked")
                    List<Object> target = (List<Object>) copyEObject.eGet(getTarget(eAttribute));
                    if (source.isEmpty()) {
                        target.clear();
                    } else {
                        Object[] array = source.toArray();
                        Arrays.sort(array);
                        for (Object object : array) {
                            target.add(object);
                        }

                    }
                } else {
                    copyEObject.eSet(getTarget(eAttribute), eObject.eGet(eAttribute));
                }
            }

        }

        public <T extends EObject> T duplicate(T eObject) {
            SortEMFCopier copier = new SortEMFCopier();
            EObject result = copier.copy(eObject);
            copier.copyReferences();

            @SuppressWarnings("unchecked")
            T t = (T) result;
            return t;
        }
    }

    private static final String DIGEST_VALUE = "digestValue"; //$NON-NLS-1$

    private static ConsistencyService instance = new ConsistencyService();

    static Logger log = Logger.getLogger(ConsistencyService.class);

    private static final String TIMESTAMP = "timeStamp"; //$NON-NLS-1$

    public static ConsistencyService getInstance() {
        return instance;
    }

    private ConsistencyService() {
    }

    private String calculateDigestValueByEMFResource(Resource resource) {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            resource.save(os, Collections.EMPTY_MAP);
            String digestValue = DigestUtil.encodeByMD5(os.toByteArray());
            return digestValue;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private String calculateDigestValueByObjectResource(Item item) {
        EObject copy = null;
        if (item instanceof ProcessItem) {
            copy = EcoreUtil.copy(((ProcessItem) item).getProcess());
        } else if (item instanceof MDMServerObjectItem) {
            MDMServerObject mdmServerObject = ((MDMServerObjectItem) item).getMDMServerObject();
            copy = EcoreUtil.copy(mdmServerObject);
            // remove server def property
            MDMServerObject copiedMdmObj = (MDMServerObject) copy;
            copiedMdmObj.setLastServerDef(null);
            // restore the timestamp to default
            copiedMdmObj.setTimestamp(0L);

        } else {
            // unsupport to caculate object md5 which not belong to MDM
            throw new UnsupportedOperationException();
        }
        if (copy != null) {
            //
            Resource resource = new XMIResourceImpl();
            resource.getContents().add(copy);
            return calculateDigestValueByEMFResource(resource);
        }
        return null;
    }

    private String calculateDigestValueByRefFile(Item item) {

        IFile file = getReferenceFile(item);
        if (file.exists()) {
            InputStream in = null;
            ByteArrayOutputStream out = null;
            try {
                in = file.getContents();
                out = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                //
                String digestValue = DigestUtil.encodeByMD5(out.toByteArray());
                return digestValue;
            } catch (CoreException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }

        }
        return null;
    }

    private String calculateDigestValueBySortedObject(Item item) {
        if (item.eClass().getClassifierID() == MdmpropertiesPackage.WS_WORKFLOW_ITEM) {

            IFile file = getReferenceFile(item);
            if (file.exists()) {
                try {
                    URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
                    Resource rawResource = new XMIResourceImpl(uri);
                    rawResource.load(Collections.EMPTY_MAP);
                    Resource resource = new XMIResourceImpl();
                    SortEMFCopier copier = new SortEMFCopier();

                    for (EObject next : rawResource.getContents()) {
                        EObject copy = copier.duplicate(next);
                        resource.getContents().add(copy);
                    }
                    return calculateDigestValueByEMFResource(resource);
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

            }
        }
        return null;
    }

    public ConsistencyCheckResult checkConsistency(MDMServerDef serverDef, Collection<IRepositoryViewObject> viewObjs)
            throws XtentisException, RemoteException {
        updateLocalDigestValue(viewObjs);
        Map<IRepositoryViewObject, WSDigestValueTimeStamp> viewObjMap = queryServerDigestValue(serverDef, viewObjs);
        int conflictCount = getConflictCount(viewObjMap);
        if (conflictCount > 0) {
            if (isWarnUserWhenConflict()) {
                ConfirmConflictMessageDialog confirmDialog = new ConfirmConflictMessageDialog(getShell(), conflictCount);
                int returnValue = confirmDialog.open();
                if (returnValue == IDialogConstants.OK_ID) {
                    int strategy = confirmDialog.getStrategy();
                    return getCheckResultByStrategy(strategy, viewObjMap);
                } else if (returnValue == IDialogConstants.DETAILS_ID) {
                    ConsistencyConflictDialog dialog = new ConsistencyConflictDialog(getShell(), conflictCount, viewObjMap);
                    dialog.open();
                    return dialog.getResult();
                } else {
                    return new ConsistencyCheckResult();
                }
            } else {
                int strategy = getConflictStrategy();
                return getCheckResultByStrategy(strategy, viewObjMap);
            }

        } else {
            return new ConsistencyCheckResult(viewObjs);
        }

    }

    private ConsistencyCheckResult getCheckResultByStrategy(int strategy,
            Map<IRepositoryViewObject, WSDigestValueTimeStamp> viewObjMap) {
        List<IRepositoryViewObject> toDeployObjs = new LinkedList<IRepositoryViewObject>();
        List<IRepositoryViewObject> toSkipObjs = new LinkedList<IRepositoryViewObject>();
        for (IRepositoryViewObject viewObj : viewObjMap.keySet()) {
            WSDigestValueTimeStamp dt = viewObjMap.get(viewObj);
            if (dt == null) {
                toDeployObjs.add(viewObj);
            } else {
                Item item = viewObj.getProperty().getItem();
                String localDigestValue = ConsistencyService.getInstance().getLocalDigestValue(item);
                String serverDigestValue = dt.getDigestValue();
                if (localDigestValue.equals(serverDigestValue)) {
                    if (strategy == CONFLICT_STRATEGY_DEFAULT || strategy == CONFLICT_STRATEGY_SKIP_DIFFERENCE) {
                        toSkipObjs.add(viewObj);
                    } else if (strategy == CONFLICT_STRATEGY_OVERWRITE) {
                        toDeployObjs.add(viewObj);
                    }
                } else {
                    if (strategy == CONFLICT_STRATEGY_SKIP_DIFFERENCE) {
                        toSkipObjs.add(viewObj);
                    } else if (strategy == CONFLICT_STRATEGY_DEFAULT || strategy == CONFLICT_STRATEGY_OVERWRITE) {
                        toDeployObjs.add(viewObj);
                    }
                }
            }
        }
        return new ConsistencyCheckResult(toDeployObjs, toSkipObjs);
    }

    public String getDigestValue(Item item) {
        DigestCalStrategyEnum strategy = getDigetValueCaculateStrategy(item);
        switch (strategy) {
        case OBJ_RESOURCE:
            return calculateDigestValueByObjectResource(item);
        case REF_FILE:
            return calculateDigestValueByRefFile(item);
        case SORTED_OBJ_RESOURCE:
            return calculateDigestValueBySortedObject(item);

        }
        return null;
    }

    private DigestCalStrategyEnum getDigetValueCaculateStrategy(Item item) {
        if (item instanceof ProcessItem) {
            return DigestCalStrategyEnum.OBJ_RESOURCE;
        }
        if (item instanceof MDMServerObjectItem) {
            int id = item.eClass().getClassifierID();
            switch (id) {
            case MdmpropertiesPackage.WS_CUSTOM_FORM_ITEM:
            case MdmpropertiesPackage.WS_DATA_MODEL_ITEM:
            case MdmpropertiesPackage.WS_RESOURCE_ITEM:

                return DigestCalStrategyEnum.REF_FILE;
            case MdmpropertiesPackage.WS_WORKFLOW_ITEM:
                return DigestCalStrategyEnum.SORTED_OBJ_RESOURCE;
            default:
                return DigestCalStrategyEnum.OBJ_RESOURCE;
            }
        }
        return DigestCalStrategyEnum.OBJ_RESOURCE;
    }

    public String getLocalDigestValue(Item item) {
        if (item == null) {
            return null;
        }
        if (item instanceof MDMServerObjectItem) {
            return ((MDMServerObjectItem) item).getMDMServerObject().getDigestValue();
        } else if (item instanceof ProcessItem) {
            return (String) item.getProperty().getAdditionalProperties().get(DIGEST_VALUE);
        }
        throw new UnsupportedOperationException();
    }

    public long getLocalTimestamp(Item item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }
        if (item instanceof MDMServerObjectItem) {
            return ((MDMServerObjectItem) item).getMDMServerObject().getTimestamp();
        } else if (item instanceof ProcessItem) {
            Long time = (Long) item.getProperty().getAdditionalProperties().get(TIMESTAMP);
            if (time != null) {
                return time;
            }
            return 0L;
        }
        throw new UnsupportedOperationException();
    }

    private IFile getReferenceFile(Item item) {
        List refResources = item.getReferenceResources();
        if (refResources != null && !refResources.isEmpty()) {
            ReferenceFileItem fileItem = (ReferenceFileItem) refResources.get(0);
            ByteArray content = fileItem.getContent();

            URI uri = content.eResource().getURI();
            if (uri.isPlatformResource()) {
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                IPath path = new Path(uri.toPlatformString(true));
                IFile file = root.getFile(path);
                return file;
            }
        }
        return null;
    }

    private Shell getShell() {
        return Display.getDefault().getActiveShell();
    }

    private int getConflictCount(Map<IRepositoryViewObject, WSDigestValueTimeStamp> map) {
        int total = 0;
        for (IRepositoryViewObject viewObj : map.keySet()) {
            WSDigestValueTimeStamp digestTime = map.get(viewObj);
            if (digestTime != null && digestTime.getDigestValue() != null) {
                String localDigestValue = getLocalDigestValue(viewObj.getProperty().getItem());
                if (localDigestValue != null) {
                    if (!localDigestValue.equals(digestTime.getDigestValue())) {
                        total++;
                    }
                }
            }
        }
        return total;
    }

    public Map<IRepositoryViewObject, WSDigestValueTimeStamp> queryServerDigestValue(MDMServerDef serverDef,
            Collection<IRepositoryViewObject> viewObjIl) throws XtentisException, RemoteException {
        Map<IRepositoryViewObject, WSDigestValueTimeStamp> result = new LinkedHashMap<IRepositoryViewObject, WSDigestValueTimeStamp>();
        XtentisPort port = RepositoryWebServiceAdapter.getXtentisPort(serverDef);
        // TODO remove it
        // int i = 0;
        for (IRepositoryViewObject viewObj : viewObjIl) {
            String type = viewObj.getRepositoryObjectType().getKey();
            String objectName = viewObj.getLabel();
            // TODO uncomment the following
            WSDigestValueTimeStamp digest = port.getDigest(new WSDigestValueKey(type, objectName));
            // construct demo data
            // WSDigestValueTimeStamp digest = null;
            // if (i == 0) {
            // digest = new WSDigestValueTimeStamp("not same value", System.currentTimeMillis());
            // } else if (i == 1) {
            // Item item = viewObj.getProperty().getItem();
            // digest = new WSDigestValueTimeStamp(getDigestValue(item), System.currentTimeMillis() - 100000);
            // } else if (i == 3) {
            // digest = null;
            // i = -1;
            // }
            // i++;
            //
            result.put(viewObj, digest);

        }
        return result;
    }

    private void updateLocalDigestValue(Item item, String digestValue) {
        if (item instanceof MDMServerObjectItem) {
            ((MDMServerObjectItem) item).getMDMServerObject().setDigestValue(digestValue);
        } else if (item instanceof ProcessItem) {
            item.getProperty().getAdditionalProperties().put(DIGEST_VALUE, digestValue);
        }
    }

    public void updateLocalDigestValue(Iterable<IRepositoryViewObject> it) {
        for (IRepositoryViewObject viewObj : it) {
            Item item = viewObj.getProperty().getItem();
            String digestValue = getDigestValue(item);
            updateLocalDigestValue(item, digestValue);
        }
    }

    private void updateLocalTimestamp(Item item, long timestamp) {
        if (item instanceof MDMServerObjectItem) {
            ((MDMServerObjectItem) item).getMDMServerObject().setTimestamp(timestamp);
        } else if (item instanceof ProcessItem) {
            item.getProperty().getAdditionalProperties().put(TIMESTAMP, timestamp);
        }
    }

    private IPreferenceStore getPreferenceStore() {
        return RepositoryPlugin.getDefault().getPreferenceStore();
    }

    public void setWarnUserWhenConflict(boolean isWarn) {
        getPreferenceStore().setValue(PreferenceConstants.P_WARN_USER_HAS_CONFLICT, isWarn);
    }

    public boolean isWarnUserWhenConflict() {
        return getPreferenceStore().getBoolean(PreferenceConstants.P_WARN_USER_HAS_CONFLICT);
    }

    public void setConflictStrategy(int strategy) {
        getPreferenceStore().setValue(PreferenceConstants.P_CONFLICT_STRATEGY, strategy);
    }

    public int getConflictStrategy() {
        return getPreferenceStore().getInt(PreferenceConstants.P_CONFLICT_STRATEGY);
    }

}
