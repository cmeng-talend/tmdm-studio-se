// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.2_01, construire R40)
// Generated source version: 1.1.2

package com.amalto.workbench.webservices;


public class WSSynchronizationPlanStatus {
    protected com.amalto.workbench.webservices.WSSynchronizationPlanStatusCode wsStatusCode;
    protected java.lang.String statusMessage;
    protected java.util.Calendar lastRunStarted;
    protected java.util.Calendar lastRunStopped;
    
    public WSSynchronizationPlanStatus() {
    }
    
    public WSSynchronizationPlanStatus(com.amalto.workbench.webservices.WSSynchronizationPlanStatusCode wsStatusCode, java.lang.String statusMessage, java.util.Calendar lastRunStarted, java.util.Calendar lastRunStopped) {
        this.wsStatusCode = wsStatusCode;
        this.statusMessage = statusMessage;
        this.lastRunStarted = lastRunStarted;
        this.lastRunStopped = lastRunStopped;
    }
    
    public com.amalto.workbench.webservices.WSSynchronizationPlanStatusCode getWsStatusCode() {
        return wsStatusCode;
    }
    
    public void setWsStatusCode(com.amalto.workbench.webservices.WSSynchronizationPlanStatusCode wsStatusCode) {
        this.wsStatusCode = wsStatusCode;
    }
    
    public java.lang.String getStatusMessage() {
        return statusMessage;
    }
    
    public void setStatusMessage(java.lang.String statusMessage) {
        this.statusMessage = statusMessage;
    }
    
    public java.util.Calendar getLastRunStarted() {
        return lastRunStarted;
    }
    
    public void setLastRunStarted(java.util.Calendar lastRunStarted) {
        this.lastRunStarted = lastRunStarted;
    }
    
    public java.util.Calendar getLastRunStopped() {
        return lastRunStopped;
    }
    
    public void setLastRunStopped(java.util.Calendar lastRunStopped) {
        this.lastRunStopped = lastRunStopped;
    }
}
