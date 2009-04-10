// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.2_01, construire R40)
// Generated source version: 1.1.2

package com.amalto.workbench.webservices;

import com.sun.xml.rpc.encoding.*;
import com.sun.xml.rpc.encoding.xsd.XSDConstants;
import com.sun.xml.rpc.encoding.literal.*;
import com.sun.xml.rpc.encoding.literal.DetailFragmentDeserializer;
import com.sun.xml.rpc.encoding.simpletype.*;
import com.sun.xml.rpc.encoding.soap.SOAPConstants;
import com.sun.xml.rpc.encoding.soap.SOAP12Constants;
import com.sun.xml.rpc.streaming.*;
import com.sun.xml.rpc.wsdl.document.schema.SchemaConstants;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

public class WSSynchronizationPlanStatus_LiteralSerializer extends LiteralObjectSerializerBase implements Initializable  {
    private static final QName ns1_wsStatusCode_QNAME = new QName("", "wsStatusCode");
    private static final QName ns2_WSSynchronizationPlanStatusCode_TYPE_QNAME = new QName("urn-com-amalto-xtentis-webservice", "WSSynchronizationPlanStatusCode");
    private CombinedSerializer ns2myns2_WSSynchronizationPlanStatusCode__WSSynchronizationPlanStatusCode_LiteralSerializer;
    private static final QName ns1_statusMessage_QNAME = new QName("", "statusMessage");
    private static final QName ns3_string_TYPE_QNAME = SchemaConstants.QNAME_TYPE_STRING;
    private CombinedSerializer ns3_myns3_string__java_lang_String_String_Serializer;
    private static final QName ns1_lastRunStarted_QNAME = new QName("", "lastRunStarted");
    private static final QName ns3_dateTime_TYPE_QNAME = SchemaConstants.QNAME_TYPE_DATE_TIME;
    private CombinedSerializer ns3_myns3_dateTime__java_util_Calendar_DateTimeCalendar_Serializer;
    private static final QName ns1_lastRunStopped_QNAME = new QName("", "lastRunStopped");
    
    public WSSynchronizationPlanStatus_LiteralSerializer(QName type, String encodingStyle) {
        this(type, encodingStyle, false);
    }
    
    public WSSynchronizationPlanStatus_LiteralSerializer(QName type, String encodingStyle, boolean encodeType) {
        super(type, true, encodingStyle, encodeType);
    }
    
    public void initialize(InternalTypeMappingRegistry registry) throws Exception {
        ns2myns2_WSSynchronizationPlanStatusCode__WSSynchronizationPlanStatusCode_LiteralSerializer = (CombinedSerializer)registry.getSerializer("", com.amalto.workbench.webservices.WSSynchronizationPlanStatusCode.class, ns2_WSSynchronizationPlanStatusCode_TYPE_QNAME);
        ns3_myns3_string__java_lang_String_String_Serializer = (CombinedSerializer)registry.getSerializer("", java.lang.String.class, ns3_string_TYPE_QNAME);
        ns3_myns3_dateTime__java_util_Calendar_DateTimeCalendar_Serializer = (CombinedSerializer)registry.getSerializer("", java.util.Calendar.class, ns3_dateTime_TYPE_QNAME);
    }
    
    public Object doDeserialize(XMLReader reader,
        SOAPDeserializationContext context) throws Exception {
        com.amalto.workbench.webservices.WSSynchronizationPlanStatus instance = new com.amalto.workbench.webservices.WSSynchronizationPlanStatus();
        Object member=null;
        QName elementName;
        List values;
        Object value;
        
        reader.nextElementContent();
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_wsStatusCode_QNAME)) {
                member = ns2myns2_WSSynchronizationPlanStatusCode__WSSynchronizationPlanStatusCode_LiteralSerializer.deserialize(ns1_wsStatusCode_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setWsStatusCode((com.amalto.workbench.webservices.WSSynchronizationPlanStatusCode)member);
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns1_wsStatusCode_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_statusMessage_QNAME)) {
                member = ns3_myns3_string__java_lang_String_String_Serializer.deserialize(ns1_statusMessage_QNAME, reader, context);
                instance.setStatusMessage((java.lang.String)member);
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns1_statusMessage_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_lastRunStarted_QNAME)) {
                member = ns3_myns3_dateTime__java_util_Calendar_DateTimeCalendar_Serializer.deserialize(ns1_lastRunStarted_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setLastRunStarted((java.util.Calendar)member);
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns1_lastRunStarted_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        elementName = reader.getName();
        if (reader.getState() == XMLReader.START) {
            if (elementName.equals(ns1_lastRunStopped_QNAME)) {
                member = ns3_myns3_dateTime__java_util_Calendar_DateTimeCalendar_Serializer.deserialize(ns1_lastRunStopped_QNAME, reader, context);
                if (member == null) {
                    throw new DeserializationException("literal.unexpectedNull");
                }
                instance.setLastRunStopped((java.util.Calendar)member);
                reader.nextElementContent();
            } else {
                throw new DeserializationException("literal.unexpectedElementName", new Object[] { ns1_lastRunStopped_QNAME, reader.getName() });
            }
        }
        else {
            throw new DeserializationException("literal.expectedElementName", reader.getName().toString());
        }
        
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        return (Object)instance;
    }
    
    public void doSerializeAttributes(Object obj, XMLWriter writer, SOAPSerializationContext context) throws Exception {
        com.amalto.workbench.webservices.WSSynchronizationPlanStatus instance = (com.amalto.workbench.webservices.WSSynchronizationPlanStatus)obj;
        
    }
    public void doSerialize(Object obj, XMLWriter writer, SOAPSerializationContext context) throws Exception {
        com.amalto.workbench.webservices.WSSynchronizationPlanStatus instance = (com.amalto.workbench.webservices.WSSynchronizationPlanStatus)obj;
        
        if (instance.getWsStatusCode() == null) {
            throw new SerializationException("literal.unexpectedNull");
        }
        ns2myns2_WSSynchronizationPlanStatusCode__WSSynchronizationPlanStatusCode_LiteralSerializer.serialize(instance.getWsStatusCode(), ns1_wsStatusCode_QNAME, null, writer, context);
        ns3_myns3_string__java_lang_String_String_Serializer.serialize(instance.getStatusMessage(), ns1_statusMessage_QNAME, null, writer, context);
        if (instance.getLastRunStarted() == null) {
            throw new SerializationException("literal.unexpectedNull");
        }
        ns3_myns3_dateTime__java_util_Calendar_DateTimeCalendar_Serializer.serialize(instance.getLastRunStarted(), ns1_lastRunStarted_QNAME, null, writer, context);
        if (instance.getLastRunStopped() == null) {
            throw new SerializationException("literal.unexpectedNull");
        }
        ns3_myns3_dateTime__java_util_Calendar_DateTimeCalendar_Serializer.serialize(instance.getLastRunStopped(), ns1_lastRunStopped_QNAME, null, writer, context);
    }
}
