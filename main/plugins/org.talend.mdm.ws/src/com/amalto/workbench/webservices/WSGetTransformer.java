
package com.amalto.workbench.webservices;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for WSGetTransformer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WSGetTransformer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="wsTransformerPK" type="{http://www.talend.com/mdm}WSTransformerPK" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSGetTransformer", propOrder = {
    "wsTransformerPK"
})
public class WSGetTransformer {

    protected WSTransformerPK wsTransformerPK;

    /**
     * Default no-arg constructor
     * 
     */
    public WSGetTransformer() {
        super();
    }

    /**
     * Fully-initialising value constructor
     * 
     */
    public WSGetTransformer(final WSTransformerPK wsTransformerPK) {
        this.wsTransformerPK = wsTransformerPK;
    }

    /**
     * Gets the value of the wsTransformerPK property.
     * 
     * @return
     *     possible object is
     *     {@link WSTransformerPK }
     *     
     */
    public WSTransformerPK getWsTransformerPK() {
        return wsTransformerPK;
    }

    /**
     * Sets the value of the wsTransformerPK property.
     * 
     * @param value
     *     allowed object is
     *     {@link WSTransformerPK }
     *     
     */
    public void setWsTransformerPK(WSTransformerPK value) {
        this.wsTransformerPK = value;
    }

}
