// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation （1.1.2_01，编译版 R40）
// Generated source version: 1.1.2

package com.amalto.workbench.webservices;


public class WSDropItem {
    protected com.amalto.workbench.webservices.WSItemPK wsItemPK;
    protected java.lang.String partPath;
    
    public WSDropItem() {
    }
    
    public WSDropItem(com.amalto.workbench.webservices.WSItemPK wsItemPK, java.lang.String partPath) {
        this.wsItemPK = wsItemPK;
        this.partPath = partPath;
    }
    
    public com.amalto.workbench.webservices.WSItemPK getWsItemPK() {
        return wsItemPK;
    }
    
    public void setWsItemPK(com.amalto.workbench.webservices.WSItemPK wsItemPK) {
        this.wsItemPK = wsItemPK;
    }
    
    public java.lang.String getPartPath() {
        return partPath;
    }
    
    public void setPartPath(java.lang.String partPath) {
        this.partPath = partPath;
    }
}