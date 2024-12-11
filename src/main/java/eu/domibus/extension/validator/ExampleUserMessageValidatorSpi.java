package eu.domibus.extension.validator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;

import javax.activation.DataHandler;

import org.springframework.stereotype.Service;

import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.core.spi.validation.UserMessageValidatorSpiException;
import eu.domibus.ext.domain.AgreementRefDTO;
import eu.domibus.ext.domain.CollaborationInfoDTO;
import eu.domibus.ext.domain.DescriptionDTO;
import eu.domibus.ext.domain.FromDTO;
import eu.domibus.ext.domain.MessageInfoDTO;
import eu.domibus.ext.domain.MessagePropertiesDTO;
import eu.domibus.ext.domain.PartInfoDTO;
import eu.domibus.ext.domain.PartPropertiesDTO;
import eu.domibus.ext.domain.PartyIdDTO;
import eu.domibus.ext.domain.PartyInfoDTO;
import eu.domibus.ext.domain.PayloadInfoDTO;
import eu.domibus.ext.domain.PropertyDTO;
import eu.domibus.ext.domain.SchemaDTO;
import eu.domibus.ext.domain.ServiceDTO;
import eu.domibus.ext.domain.ToDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

@Service
public class ExampleUserMessageValidatorSpi implements UserMessageValidatorSpi {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ExampleUserMessageValidatorSpi.class);

    @Override
    public void validateUserMessage(UserMessageDTO userMessage) throws UserMessageValidatorSpiException {
        LOGGER.info(
                "VALIDATEUSERMESSAGE: Calling ExampleUserMessageValidatorSpi.validateUserMessage method from the domibus-validation-extension");
        // throw new UserMessageValidatorSpiException("The validation didn't pass for the validateUserMessage");
       

        // PayloadInfoDTO
        PayloadInfoDTO payloadInfo = userMessage.getPayloadInfo();

        if (payloadInfo == null) {
            LOGGER.info("VALIDATEUSERMESSAGE: PayloadInfoDTO is null");
        } else {
            Set<PartInfoDTO> partInfos = payloadInfo.getPartInfo();

            // PartInfoDTO
            for (PartInfoDTO partInfo : partInfos) {
                DataHandler payloadDataHandler = partInfo.getPayloadDatahandler();
                LOGGER.info("VALIDATEUSERMESSAGE: payloadDataHandler: " + payloadDataHandler);

                if (payloadDataHandler != null) {
                    try (InputStream payloadStream = payloadDataHandler.getInputStream()) {
                        // Process the InputStream as needed
                        // String mimeType = partInfo.getMime();
                        // LOGGER.info("VALIDATEUSERMESSAGE: Processing payload with MIME type: " + mimeType);

                        // Example: Reading payload as a String (if it is text-based)
                        String payloadContent = new BufferedReader(new InputStreamReader(payloadStream))
                                .lines()
                                .collect(Collectors.joining("\n"));

                        LOGGER.info("VALIDATEUSERMESSAGE: Payload Content: " + payloadContent);

                        // Parse the XML content
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        Document document = builder.parse(new ByteArrayInputStream(payloadContent.getBytes()));

                        // Validate the XML content against the XSD                      
                        String xsdString = "<xsd:schema xmlns=\"http://docs.oasis-open.org/bdxr/ns/XHE/1/ExchangeHeaderEnvelope\" "
        + "xmlns:xha=\"http://docs.oasis-open.org/bdxr/ns/XHE/1/AggregateComponents\" "
        + "xmlns:xhb=\"http://docs.oasis-open.org/bdxr/ns/XHE/1/BasicComponents\" "
        + "xmlns:ext=\"http://docs.oasis-open.org/bdxr/ns/XHE/1/ExtensionComponents\" "
        + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
        + "xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" "
        + "xmlns:ccts=\"urn:un:unece:uncefact:documentation:2\" "
        + "targetNamespace=\"http://docs.oasis-open.org/bdxr/ns/XHE/1/ExchangeHeaderEnvelope\" "
        + "elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" version=\"1.1\">"

        + "<xsd:import namespace=\"http://docs.oasis-open.org/bdxr/ns/XHE/1/AggregateComponents\" schemaLocation=\"https://docs.oasis-open.org/bdxr/xhe/v1.0/os/xsd/fragments/XHE-AggregateComponents-1.0.xsd\"/>"
        + "<xsd:import namespace=\"http://docs.oasis-open.org/bdxr/ns/XHE/1/BasicComponents\" schemaLocation=\"https://docs.oasis-open.org/bdxr/xhe/v1.0/os/xsd/fragments/XHE-BasicComponents-1.0.xsd\"/>"
        + "<xsd:import namespace=\"http://docs.oasis-open.org/bdxr/ns/XHE/1/ExtensionComponents\" schemaLocation=\"https://docs.oasis-open.org/bdxr/xhe/v1.0/os/xsd/fragments/XHE-ExtensionComponents-1.0.xsd\"/>"
        + "<xsd:import namespace=\"http://www.w3.org/2000/09/xmldsig#\" schemaLocation=\"https://docs.oasis-open.org/bdxr/xhe/v1.0/os/xsd/fragments/XHE-xmldsig1-schema-1.0.xsd\"/>"

        + "<xsd:element name=\"XHE\" type=\"XHEType\">"
        + "<xsd:annotation>"
        + "<xsd:documentation>This element MUST be conveyed as the root element in any instance document based on this Schema expression</xsd:documentation>"
        + "</xsd:annotation>"
        + "</xsd:element>"

        + "<xsd:complexType name=\"XHEType\">"
        + "<xsd:annotation>"
        + "<xsd:documentation>"
        + "<ccts:Component>"
        + "<ccts:CEFACTNames>"
        + "<ccts:DictionaryEntryName>XHE_ Envelope. Details</ccts:DictionaryEntryName>"
        + "</ccts:CEFACTNames>"
        + "<ccts:OASISNames>"
        + "<ccts:ComponentType>ABIE</ccts:ComponentType>"
        + "<ccts:DictionaryEntryName>XHE. Details</ccts:DictionaryEntryName>"
        + "<ccts:Definition>The Exchange Header Envelope</ccts:Definition>"
        + "<ccts:ObjectClass>XHE</ccts:ObjectClass>"
        + "</ccts:OASISNames>"
        + "</ccts:Component>"
        + "</xsd:documentation>"
        + "</xsd:annotation>"
        + "<xsd:sequence>"
        // + "<xsd:element ref=\"ext:XHEExtensions\" minOccurs=\"0\" maxOccurs=\"1\">"
        // + "<xsd:annotation>"
        // + "<xsd:documentation>A container for all extensions present in the document.</xsd:documentation>"
        // + "</xsd:annotation>"
        // + "</xsd:element>"

        // + "<xsd:element ref=\"xhb:XHEVersionID\" minOccurs=\"1\" maxOccurs=\"1\">"
        // + "<xsd:annotation>"
        // + "<xsd:documentation>"
        // + "<ccts:Component>"
        // + "<ccts:CEFACTNames>"
        // + "<ccts:DictionaryEntryName>XHE_ Envelope. Version. Identifier</ccts:DictionaryEntryName>"
        // + "</ccts:CEFACTNames>"
        // + "<ccts:OASISNames>"
        // + "<ccts:ComponentType>BBIE</ccts:ComponentType>"
        // + "<ccts:DictionaryEntryName>XHE. XHE Version Identifier. Identifier</ccts:DictionaryEntryName>"
        // + "<ccts:Definition>The version of the specific envelope model in use.</ccts:Definition>"
        // + "<ccts:Cardinality>1</ccts:Cardinality>"
        // + "<ccts:ObjectClass>XHE</ccts:ObjectClass>"
        // + "<ccts:PropertyTerm>XHE Version Identifier</ccts:PropertyTerm>"
        // + "<ccts:RepresentationTerm>Identifier</ccts:RepresentationTerm>"
        // + "<ccts:DataType>Identifier. Type</ccts:DataType>"
        // + "</ccts:OASISNames>"
        // + "</ccts:Component>"
        // + "</xsd:documentation>"
        // + "</xsd:annotation>"
        // + "</xsd:element>"

        // + "<xsd:element ref=\"xhb:CustomizationID\" minOccurs=\"0\" maxOccurs=\"1\">"
        // + "<xsd:annotation>"
        // + "<xsd:documentation>"
        // + "<ccts:Component>"
        // + "<ccts:CEFACTNames>"
        // + "<ccts:DictionaryEntryName>XHE_ Envelope. Customization. Identifier</ccts:DictionaryEntryName>"
        // + "</ccts:CEFACTNames>"
        // + "<ccts:OASISNames>"
        // + "<ccts:ComponentType>BBIE</ccts:ComponentType>"
        // + "<ccts:DictionaryEntryName>XHE. Customization Identifier. Identifier</ccts:DictionaryEntryName>"
        // + "<ccts:Definition>The identification of a customization or use of the envelope model.</ccts:Definition>"
        // + "<ccts:Cardinality>0..1</ccts:Cardinality>"
        // + "<ccts:ObjectClass>XHE</ccts:ObjectClass>"
        // + "<ccts:PropertyTerm>Customization Identifier</ccts:PropertyTerm>"
        // + "<ccts:RepresentationTerm>Identifier</ccts:RepresentationTerm>"
        // + "<ccts:DataType>Identifier. Type</ccts:DataType>"
        // + "</ccts:OASISNames>"
        // + "</ccts:Component>"
        // + "</xsd:documentation>"
        // + "</xsd:annotation>"
        // + "</xsd:element>"

        // + "<xsd:element ref=\"ds:Signature\" minOccurs=\"0\" maxOccurs=\"unbounded\">"
        // + "<xsd:annotation>"
        // + "<xsd:documentation>This is an optional set of digital signatures as defined by the W3C specification.</xsd:documentation>"
        // + "</xsd:annotation>"
        // + "</xsd:element>"
        + "</xsd:sequence>"
        + "</xsd:complexType>"
        + "</xsd:schema>";



                        

                        // SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        // Schema schema;
                        // try (StringReader xsdReader = new StringReader(xsdString)) {
                        //     schema = schemaFactory.newSchema(new StreamSource(xsdReader));
                        // }                   
                        // Validator validator = schema.newValidator();

                        // try {
                        //     validator.validate(new DOMSource(document));
                        //     LOGGER.info("VALIDATEUSERMESSAGE: XML is valid.");
                        // } catch (SAXException e) {
                        //     LOGGER.error("VALIDATEUSERMESSAGE: XML is not valid.", e);
                        //     throw new UserMessageValidatorSpiException("VALIDATEUSERMESSAGE: The XHE validation didn't pass.");
                        // }                        

                        // Process the XML document as needed
                        // Get the root element
                        String rootElement = document.getDocumentElement().getNodeName();
                        LOGGER.info("VALIDATEUSERMESSAGE: Root element of the XML: " + rootElement);

                        if (!rootElement.equals("XHE")) {
                            LOGGER.error("VALIDATEUSERMESSAGE: The root element is not XHE");
                            throw new UserMessageValidatorSpiException("VALIDATEUSERMESSAGE: The XHE validation didn't pass.");
                        }

                        // Get and validate the value of the XHEVersionID element
                        String xheVersionIdValue = document.getElementsByTagName("xhb:XHEVersionID").item(0).getTextContent();
                        // String xheVersionIdValue = document.getElementsByTagNameNS("http://docs.oasis-open.org/bdxr/ns/XHE/1/BasicComponents", "XHEVersionID").item(0).getTextContent();
                        LOGGER.info("VALIDATEUSERMESSAGE: XHEVersionID value: " + xheVersionIdValue);

                        if (!xheVersionIdValue.equals("1.0")) {
                            LOGGER.error("VALIDATEUSERMESSAGE: The XHEVersionID value is not 1.0");
                            throw new UserMessageValidatorSpiException("VALIDATEUSERMESSAGE: The XHE validation didn't pass.");
                        }

                        // Get and validate the value of the CustomizationID element
                        Element customizationIdElement = (Element) document.getElementsByTagName("xhb:CustomizationID").item(0);
                        String customizationIdValue = customizationIdElement.getTextContent();
                        LOGGER.info("VALIDATEUSERMESSAGE: CustomizationID value: " + customizationIdValue);

                        if (!customizationIdValue.equals("http://docs.oasis-open.org/bdxr/ns/XHE/1/ExchangeHeaderEnvelope::XHE##dbnalliance-envelope-1.0")) {
                            LOGGER.error("VALIDATEUSERMESSAGE: The CustomizationID value is not http://docs.oasis-open.org/bdxr/ns/XHE/1/ExchangeHeaderEnvelope::XHE##dbnalliance-envelope-1.0");
                            throw new UserMessageValidatorSpiException("VALIDATEUSERMESSAGE: The XHE validation didn't pass.");
                        }

                        // Get and validate the schemeID value of the CustomizationID element
                        String schemeIdValue = customizationIdElement.getAttribute("schemeID");
                        LOGGER.info("VALIDATEUSERMESSAGE: CustomizationID schemeID value: " + schemeIdValue);

                        if (!schemeIdValue.equals("bdx-docid-qns")) {
                            LOGGER.error("VALIDATEUSERMESSAGE: The CustomizationID schemeID value is not bdx-docid-qns");
                            throw new UserMessageValidatorSpiException("VALIDATEUSERMESSAGE: The XHE validation didn't pass.");
                        }

                        // Get and validate the value of the ProfileID element
                        String profileIdValue = document.getElementsByTagName("xhb:ProfileID").item(0).getTextContent();
                        LOGGER.info("VALIDATEUSERMESSAGE: ProfileID value: " + profileIdValue);

                        if (!profileIdValue.equals("dbnalliance-envelope-1.0")) {
                            LOGGER.error("VALIDATEUSERMESSAGE: The ProfileID value is not dbnalliance-envelope-1.0");
                            throw new UserMessageValidatorSpiException("VALIDATEUSERMESSAGE: The XHE validation didn't pass.");
                        }

                        // Get and validate the value of the Header element
                        Element headerElement = (Element) document.getElementsByTagName("xha:Header").item(0);

                        if (headerElement == null) {
                            LOGGER.error("VALIDATEUSERMESSAGE: The Header element is missing");
                            throw new UserMessageValidatorSpiException("VALIDATEUSERMESSAGE: The XHE validation didn't pass.");
                        }

                        Element headerIdElement = (Element) headerElement.getElementsByTagName("xhb:ID").item(0);


                    } catch (IOException | ParserConfigurationException | SAXException e) {
                        LOGGER.error("VALIDATEUSERMESSAGE: Error reading payload", e);
                    }
                } else {
                    LOGGER.warn("VALIDATEUSERMESSAGE: No payload available for this PartInfoDTO");
                }                
            }
        }        

        // InputStream emptyInputStream = new InputStream() {
        // @Override
        // public int read() {
        // return -1;
        // }
        // }; // new ByteArrayInputStream(new byte[0]);

        // validatePayload(emptyInputStream, "application/xml");
        // if (payload.contains("error")) {
        // throw new UserMessageValidatorSpiException("The validation didn't pass for
        // the validateUserMessage");
        // }
    }

    @Override
    public void validatePayload(InputStream payload, String mimeType) throws UserMessageValidatorSpiException {
        LOGGER.info(
                "VALIDATEPAYLOAD: Calling ExampleUserMessageValidatorSpi.validatePayload method from the domibus-validation-extension");
        // throw new UserMessageValidatorSpiException("The validation didn't pass for
        // the payload");
    }
}
