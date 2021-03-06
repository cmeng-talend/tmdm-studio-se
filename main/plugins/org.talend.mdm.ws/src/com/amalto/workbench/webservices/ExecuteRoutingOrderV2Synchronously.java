
package com.amalto.workbench.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for executeRoutingOrderV2Synchronously complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="executeRoutingOrderV2Synchronously">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://www.talend.com/mdm}WSExecuteRoutingOrderV2Synchronously" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "executeRoutingOrderV2Synchronously", propOrder = {
    "arg0"
})
public class ExecuteRoutingOrderV2Synchronously {

    protected WSExecuteRoutingOrderV2Synchronously arg0;

    /**
     * Default no-arg constructor
     * 
     */
    public ExecuteRoutingOrderV2Synchronously() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public ExecuteRoutingOrderV2Synchronously(final WSExecuteRoutingOrderV2Synchronously arg0) {
        this.arg0 = arg0;
    }

    /**
     * Gets the value of the arg0 property.
     * 
     * @return
     *     possible object is
     *     {@link WSExecuteRoutingOrderV2Synchronously }
     *     
     */
    public WSExecuteRoutingOrderV2Synchronously getArg0() {
        return arg0;
    }

    /**
     * Sets the value of the arg0 property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSExecuteRoutingOrderV2Synchronously }
     *     
     */
    public void setArg0(WSExecuteRoutingOrderV2Synchronously value) {
        this.arg0 = value;
    }

}
