package eu.domibus.extension.validator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.util.Base64;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import com.helger.commons.error.list.IErrorList;
import com.helger.schematron.pure.SchematronResourcePure;
import com.helger.schematron.svrl.SVRLHelper;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import com.helger.schematron.pure.model.PSSchema;
import com.helger.schematron.pure.errorhandler.LoggingPSErrorHandler;
import com.helger.schematron.svrl.SVRLFailedAssert;

// import com.helger.schematron.SchematronValidator;
// import com.helger.schematron.ValidationResult;
// import com.helger.schematron.ValidationReport;
// import com.helger.schematron.pure.SchematronResourcePure;
// import com.helger.schematron.pure.errorhandler.LoggingPSErrorHandler;
// import com.helger.schematron.pure.model.PSSchema;
// import com.helger.schematron.pure.preprocess.PSPreprocessor;
// import com.helger.xml.serialize.read.DOMReader;
// import com.helger.xml.serialize.write.XMLWriter;

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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
                "ValidateUserMessage: Calling ExampleUserMessageValidatorSpi.validateUserMessage method from the domibus-validation-extension");
        // throw new UserMessageValidatorSpiException("The validation didn't pass for the validateUserMessage");
       

        // PayloadInfoDTO
        PayloadInfoDTO payloadInfo = userMessage.getPayloadInfo();

        if (payloadInfo == null) {
            LOGGER.info("ValidateUserMessage: PayloadInfoDTO is null");
        } else {
            Set<PartInfoDTO> partInfos = payloadInfo.getPartInfo();

            // PartInfoDTO
            for (PartInfoDTO partInfo : partInfos) {
                DataHandler payloadDataHandler = partInfo.getPayloadDatahandler();

                if (payloadDataHandler != null) {
                    try (InputStream payloadStream = payloadDataHandler.getInputStream()) {
                        String mimeType = partInfo.getMime();
                        LOGGER.info("ValidateUserMessage: Processing payload with MIME type: " + mimeType);

                        validatePayload(payloadStream, mimeType);
                    } catch (IOException e) {
                        LOGGER.error("ValidateUserMessage: Error reading payload", e);
                        throw new UserMessageValidatorSpiException("Error reading payload", e);
                    }
                } else {
                    LOGGER.warn("ValidateUserMessage: No payload available for this PartInfoDTO");
                }

                // if (payloadDataHandler != null) {
                //     try (InputStream payloadStream = payloadDataHandler.getInputStream()) {
                //         // Process the InputStream as needed
                //         // String mimeType = partInfo.getMime();
                //         // LOGGER.info("VALIDATEUSERMESSAGE: Processing payload with MIME type: " + mimeType);

                //         // Example: Reading payload as a String (if it is text-based)
                //         String payloadContent = new BufferedReader(new InputStreamReader(payloadStream))
                //                 .lines()
                //                 .collect(Collectors.joining("\n"));

                //         LOGGER.info("VALIDATEUSERMESSAGE: Payload Content: " + payloadContent);

                //         // Parse the XML content
                //         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                //         DocumentBuilder builder = factory.newDocumentBuilder();
                //         Document document = builder.parse(new ByteArrayInputStream(payloadContent.getBytes()));                 

                //         // Process the XML document
                //         // Get and validate the root element
                //         Element rootElement = document.getDocumentElement();
                        
                //         if (rootElement == null) {
                //             LOGGER.error("ValidateUserMessage: The root element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the root element name
                //         String rootElementValue = document.getDocumentElement().getNodeName();
                //         LOGGER.info("ValidateUserMessage: Root element of the XML: " + rootElement);

                //         if (!rootElementValue.equals("XHE")) {
                //             LOGGER.error("ValidateUserMessage: The root element is not XHE");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the XHEVersionID element
                //         Element xheVersionIdElement = (Element) document.getElementsByTagName("xhb:XHEVersionID").item(0);

                //         if (xheVersionIdElement == null) {
                //             LOGGER.error("ValidateUserMessage: The XHEVersionID element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the value of the XHEVersionID element
                //         String xheVersionIdValue = document.getElementsByTagName("xhb:XHEVersionID").item(0).getTextContent();
                //         LOGGER.info("ValidateUserMessage: XHEVersionID value: " + xheVersionIdValue);

                //         if (!xheVersionIdValue.equals("1.0")) {
                //             LOGGER.error("ValidateUserMessage: The XHEVersionID value is not 1.0");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the CustomizationID element
                //         Element customizationIdElement = (Element) document.getElementsByTagName("xhb:CustomizationID").item(0);

                //         if (customizationIdElement == null) {
                //             LOGGER.error("ValidateUserMessage: The CustomizationID element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the value of the CustomizationID element
                //         String customizationIdValue = customizationIdElement.getTextContent();
                //         LOGGER.info("ValidateUserMessage: CustomizationID value: " + customizationIdValue);

                //         if (!customizationIdValue.equals("http://docs.oasis-open.org/bdxr/ns/XHE/1/ExchangeHeaderEnvelope::XHE##dbnalliance-envelope-1.0")) {
                //             LOGGER.error("ValidateUserMessage: The CustomizationID value is not http://docs.oasis-open.org/bdxr/ns/XHE/1/ExchangeHeaderEnvelope::XHE##dbnalliance-envelope-1.0");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the schemeID value of the CustomizationID element
                //         String schemeIdValue = customizationIdElement.getAttribute("schemeID");
                //         LOGGER.info("ValidateUserMessage: CustomizationID schemeID value: " + schemeIdValue);

                //         if (!schemeIdValue.equals("bdx-docid-qns")) {
                //             LOGGER.error("ValidateUserMessage: The CustomizationID schemeID value is not bdx-docid-qns");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the ProfileID element
                //         Element profileIdElement = (Element) document.getElementsByTagName("xhb:ProfileID").item(0);

                //         if (profileIdElement == null) {
                //             LOGGER.error("ValidateUserMessage: The ProfileID element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }
                        
                //         // Get and validate the value of the ProfileID element
                //         String profileIdValue = profileIdElement.getTextContent();
                //         LOGGER.info("ValidateUserMessage: ProfileID value: " + profileIdValue);

                //         if (!profileIdValue.equals("dbnalliance-envelope-1.0")) {
                //             LOGGER.error("ValidateUserMessage: The ProfileID value is not dbnalliance-envelope-1.0");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the Header element
                //         Element headerElement = (Element) document.getElementsByTagName("xha:Header").item(0);

                //         if (headerElement == null) {
                //             LOGGER.error("ValidateUserMessage: The Header element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the ID element of the Header element
                //         Element headerIdElement = (Element) headerElement.getElementsByTagName("xhb:ID").item(0);

                //         if (headerIdElement == null) {
                //             LOGGER.error("ValidateUserMessage: The Header ID element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }                        

                //         // Get and validate the CreationDateTime element of the Header element
                //         Element creationDateTimeElement = (Element) headerElement.getElementsByTagName("xhb:CreationDateTime").item(0);

                //         if (creationDateTimeElement == null) {
                //             LOGGER.error("ValidateUserMessage: The Header CreationDateTime element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the FromParty element of the Header element
                //         Element fromPartyElement = (Element) headerElement.getElementsByTagName("xha:FromParty").item(0);

                //         if (fromPartyElement == null) {
                //             LOGGER.error("ValidateUserMessage: The FromParty element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }
                        
                //         // Get and validate the PartyIdentification element of the FromParty element
                //         Element fromPartyIdentificationElement = (Element) fromPartyElement.getElementsByTagName("xha:PartyIdentification").item(0);

                //         if (fromPartyIdentificationElement == null) {
                //             LOGGER.error("ValidateUserMessage: The FromParty PartyIdentification element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the ID element of the PartyIdentification element
                //         Element fromPartyIdElement = (Element) fromPartyIdentificationElement.getElementsByTagName("xhb:ID").item(0);

                //         if (fromPartyIdElement == null) {
                //             LOGGER.error("ValidateUserMessage: The FromParty PartyID element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // TODO: Validate the From Party ID against DBNAlliance Policy for Using Identifiers
                //         // String fromPartyIdValue = fromPartyIdElement.getTextContent();

                //         // TODO: Validate the From Party scheme against DBNAlliance Policy for Using Identifiers
                //         // String fromPartyIdSchemeID = fromPartyIdElement.getAttribute("schemeID");

                //         // Get and compare the values of the Header ID and FromParty ID elements
                //         String headerIdValue = headerIdElement.getTextContent();
                //         String fromPartyIdValue = fromPartyIdElement.getTextContent();
                //         LOGGER.info("ValidateUserMessage: Header ID value: " + headerIdValue);
                //         LOGGER.info("ValidateUserMessage: FromParty ID value: " + fromPartyIdValue);

                //         if (headerIdValue.equals(fromPartyIdValue)) {
                //             LOGGER.error("ValidateUserMessage: The Header ID FromParty ID values must be unique");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the ToParty element of the Header element
                //         Element toPartyElement = (Element) headerElement.getElementsByTagName("xha:ToParty").item(0);

                //         if (toPartyElement == null) {
                //             LOGGER.error("ValidateUserMessage: The ToParty element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the PartyIdentification element of the ToParty element
                //         Element toPartyIdentificationElement = (Element) toPartyElement.getElementsByTagName("xha:PartyIdentification").item(0);

                //         if (toPartyIdentificationElement == null) {
                //             LOGGER.error("ValidateUserMessage: The ToParty PartyIdentification element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the ID element of the PartyIdentification element
                //         Element toPartyIdElement = (Element) toPartyIdentificationElement.getElementsByTagName("xhb:ID").item(0);

                //         if (toPartyIdElement == null) {
                //             LOGGER.error("ValidateUserMessage: The ToParty PartyID element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // TODO: Validate the To Party ID against DBNAlliance Policy for Using Identifiers
                //         // String toPartyIdValue = toPartyIdElement.getTextContent();

                //         // TODO: Validate the To Party ID scheme against DBNAlliance Policy for Using Identifiers
                //         // String toPartyIdSchemeID = toPartyIdElement.getAttribute("schemeID");

                //         // Get and validate the Payloads element
                //         Element payloadsElement = (Element) document.getElementsByTagName("xha:Payloads").item(0);

                //         if (payloadsElement == null) {
                //             LOGGER.error("ValidateUserMessage: The Payloads element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the Payload element of the Payloads element
                //         Element payloadElement = (Element) payloadsElement.getElementsByTagName("xha:Payload").item(0);

                //         if (payloadElement == null) {
                //             LOGGER.error("ValidateUserMessage: The Payload element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // TODO: Get and validate the ID element of the Payload element
                //         // Element payloadIdElement = (Element) payloadElement.getElementsByTagName("xhb:ID").item(0);

                //         // if (payloadIdElement == null) {
                //         //     LOGGER.error("ValidateUserMessage: The Payload ID element is missing");
                //         //     throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         // }

                //         // Get and validate the ContentTypeCode element of the Payload element
                //         Element contentTypeCodeElement = (Element) payloadElement.getElementsByTagName("xhb:ContentTypeCode").item(0);

                //         if (contentTypeCodeElement == null) {
                //             LOGGER.error("ValidateUserMessage: The ContentTypeCode element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the value of the ContentTypeCode element
                //         String contentTypeCodeValue = contentTypeCodeElement.getTextContent();
                //         LOGGER.info("ValidateUserMessage: ContentTypeCode value: " + contentTypeCodeValue);

                //         if (!contentTypeCodeValue.equals("application/xml")) {
                //             LOGGER.error("ValidateUserMessage: The ContentTypeCode value is not application/xml");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the listID value of the ContentTypeCode element if it is set
                //         String contentTypeCodeListIdValue = contentTypeCodeElement.getAttribute("listID");
                //         LOGGER.info("ValidateUserMessage: ContentTypeCode listID value: " + contentTypeCodeListIdValue);

                //         if (!contentTypeCodeListIdValue.isEmpty() && !contentTypeCodeListIdValue.equals("MIME")) {
                //             LOGGER.error("ValidateUserMessage: The ContentTypeCode listID value is not MIME");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the CustomizationID element of the ContentTypeCode element
                //         Element payloadCustomizationIdElement = (Element) payloadElement.getElementsByTagName("xhb:CustomizationID").item(0);

                //         if (payloadCustomizationIdElement == null) {
                //             LOGGER.error("ValidateUserMessage: The ContentTypeCode CustomizationID element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // TODO: Get and validate the schemeID value of the CustomizationID element
                //         // String payloadCustomizationIdSchemeIdValue = payloadCustomizationIdElement.getAttribute("schemeID");
                //         // LOGGER.info("ValidateUserMessage: ContentTypeCode CustomizationID schemeID value: " + payloadCustomizationIdSchemeIdValue);

                //         // if (payloadCustomizationIdElement != null && payloadCustomizationIdSchemeIdValue.isEmpty()) {
                //         //     LOGGER.error("ValidateUserMessage: The ContentTypeCode CustomizationID schemeID value is missing");
                //         //     throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         // }

                //         // Get and validate the ProfileID element
                //         Element payloadProfileIdElement = (Element) payloadElement.getElementsByTagName("xhb:ProfileID").item(0);

                //         if (payloadProfileIdElement == null) {
                //             LOGGER.error("ValidateUserMessage: The Payload ProfileID element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the InstanceEncryptionIndicator element
                //         Element instanceEncryptionIndicatorElement = (Element) payloadElement.getElementsByTagName("xhb:InstanceEncryptionIndicator").item(0);

                //         if (instanceEncryptionIndicatorElement == null) {
                //             LOGGER.error("ValidateUserMessage: The InstanceEncryptionIndicator element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the value of the InstanceEncryptionIndicator element
                //         boolean instanceEncryptionIndicatorValue = Boolean.parseBoolean(instanceEncryptionIndicatorElement.getTextContent());
                //         LOGGER.info("ValidateUserMessage: InstanceEncryptionIndicator value: " + instanceEncryptionIndicatorValue);

                //         // Get and validate the InstanceEncryptionMethod element if the InstanceEncryptionIndicator value equals true
                //         if (instanceEncryptionIndicatorValue) {
                //             Element instanceEncryptionMethodElement = (Element) payloadElement.getElementsByTagName("xhb:InstanceEncryptionMethod").item(0);

                //             if (instanceEncryptionMethodElement == null) {
                //                 LOGGER.error("ValidateUserMessage: The InstanceEncryptionMethod element is missing");
                //                 throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //             }
                //         }

                //         // Get and validate the InstanceHashValue element
                //         Element instanceHashValueElement = (Element) payloadElement.getElementsByTagName("xhb:InstanceHashValue").item(0);

                //         if (instanceHashValueElement != null) {
                //             LOGGER.error("ValidateUserMessage: InstanceHashValue element cannot be present");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // Get and validate the PayloadContent element
                //         Element payloadContentElement = (Element) payloadElement.getElementsByTagName("xha:PayloadContent").item(0);

                //         if (payloadContentElement == null) {
                //             LOGGER.error("ValidateUserMessage: The PayloadContent element is missing");
                //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                //         }

                //         // TODO: Review this validation
                //         validatePayloadContent(payloadContentElement);

                //     } catch (IOException | ParserConfigurationException | SAXException e) {
                //         LOGGER.error("ValidateUserMessage: Error reading payload", e);
                //     }
                // } else {
                //     LOGGER.warn("ValidateUserMessage: No payload available for this PartInfoDTO");
                // }                
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

    private String loadSchematronFile(String fileName) throws IOException {
        LOGGER.info("LoadSchematronFile: Calling ExampleUserMessageValidatorSpi.loadSchematronFile method from the domibus-validation-extension");
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public void validatePayloadContent(Element payloadContentElement) throws UserMessageValidatorSpiException {
        LOGGER.info("ValidatePayloadContent: Calling ExampleUserMessageValidatorSpi.validatePayloadContent method from the domibus-validation-extension");
        try {
            // Convert the payload content element to a string
            Element invoiceElement = (Element) payloadContentElement.getElementsByTagName("Invoice").item(0);
            LOGGER.info("ValidatePayloadContent: Invoice value: " + invoiceElement);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(invoiceElement), new StreamResult(writer));
            String payloadContent = writer.toString();

            // Load the Schematron file content
            LOGGER.info("ValidatePayloadContent: Loading Schematron file content");
            String schematronContent = loadSchematronFile("DBNA_Core_Invoice_Profile_1.0_Minimum.sch");
            //LOGGER.info("ValidatePayloadContent: Schematron content: " + schematronContent);

            // Validate the payload content using the Schematron file
            LOGGER.info("ValidatePayloadContent: Validating payload content with Schematron");
            // validateWithSchematron(payloadContent, "DBNA_Core_Invoice_Profile_1.0_Minimum.sch");         
            // validateWithSchematron(payloadContent, schematronContent);

            if (isXmlContent(payloadContentElement)) {
                validateXmlContent(payloadContentElement);
            } else if (isTextContent(payloadContentElement)) {
                validateTextContent(payloadContentElement);
            } else if (isBinaryContent(payloadContentElement)) {
                validateBinaryContent(payloadContentElement);
            } else {
                throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
            }
        } catch (TransformerConfigurationException e) {
            LOGGER.error("ValidatePayloadContent: Transformer configuration error", e);
            throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
        } catch (TransformerException e) {
            LOGGER.error("ValidatePayloadContent: Transformer error", e);
            throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
        } catch (IOException e) {
            LOGGER.error("ValidatePayloadContent: IO error", e);
            throw new UserMessageValidatorSpiException("Error: Unable to load Schematron file.");
        }
    }

    private boolean isXmlContent(Element payloadContentElement) {
        // Implement logic to determine if the content is XML
        // For example, check if it has child elements
        return payloadContentElement.getElementsByTagName("*").getLength() > 0;
    }

    private boolean isTextContent(Element payloadContentElement) {
        // Implement logic to determine if the content is textual
        // For example, check if it contains only text nodes
        return payloadContentElement.getTextContent().matches("[\\s\\S]*");
    }

    private boolean isBinaryContent(Element payloadContentElement) {
        // Implement logic to determine if the content is binary
        // For example, check if it is Base64 encoded
        try {
            Base64.getDecoder().decode(payloadContentElement.getTextContent());
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error: The binary content is not Base64 encoded.  Exception: " + e);
            return false;
        }
    }

    private void validateXmlContent(Element payloadContentElement) throws UserMessageValidatorSpiException {
        // Check for exactly one apex element
        // NodeList childNodes = payloadContentElement.getChildNodes();
        // int elementCount = 0;
        // for (int i = 0; i < childNodes.getLength(); i++) {
        //     if (childNodes.item(i) instanceof Element) {
        //         elementCount++;
        //     }
        // }

        // LOGGER.info("ValidateXmlContent: Number of child elements: " + elementCount);
        // LOGGER.info("ValidateXmlContent: payloadContentElement: " + payloadContentElement);

        // if (elementCount != 1) {
        //     LOGGER.error("Error: XML content must have exactly one apex element");
        //     throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
        // }
    
        // Check for UTF-8 encoding
        String encoding = payloadContentElement.getOwnerDocument().getXmlEncoding();
        if (encoding == null || !encoding.equalsIgnoreCase(StandardCharsets.UTF_8.name())) {
            LOGGER.error("Error: XML content must be UTF-8 encoded");
            throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
        }
    
        // Check for schema validation (assuming schema validation logic is implemented)
        // validateSchema(payloadContentElement);
    }

    private void validateTextContent(Element payloadContentElement) throws UserMessageValidatorSpiException {
       // Check if the content is encoded according to XML text encoding rules
       String textContent = payloadContentElement.getTextContent();
       if (!textContent.matches("[\\s\\S]*")) {    
            LOGGER.error("Error: Text content must be encoded according to XML text encoding rules");    
            throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
        }
    }

    private void validateBinaryContent(Element payloadContentElement) throws UserMessageValidatorSpiException {
        // Check if the content is Base64 encoded
        try {
            Base64.getDecoder().decode(payloadContentElement.getTextContent());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error: Binary content must be Base64 encoded.  Exception: " + e);
            throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
        }
    }

    private void validateSchema(Element payloadContentElement) throws UserMessageValidatorSpiException {
        // Implement schema validation logic
        // This is a placeholder for actual schema validation
        // For example, using a schema validator
    }

    // private void validateWithSchematron(String xmlContent, String schematronContent) throws UserMessageValidatorSpiException {
    // LOGGER.info("ValidateWithSchematron: Calling ExampleUserMessageValidatorSpi.validateWithSchematron method from the domibus-validation-extension");
    //     try {
    //         // Compile the Schematron schema to XSLT
    //         TransformerFactory factory = TransformerFactory.newInstance();
    //         Source schematronSource = new StreamSource(new StringReader(schematronContent));
    //         Transformer transformer = factory.newTransformer(schematronSource);
    
    //         // Transform the XML content using the compiled Schematron XSLT
    //         Source xmlSource = new StreamSource(new StringReader(xmlContent));
    //         StringWriter resultWriter = new StringWriter();
    //         Result result = new StreamResult(resultWriter);
    //         transformer.transform(xmlSource, result);
    
    //         // Check the result for validation errors
    //         String validationResult = resultWriter.toString();
    //         if (validationResult.contains("failed-assert")) {
    //             LOGGER.error("ValidateWithSchematron: Schematron validation failed: " + validationResult);
    //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
    //         }
    //     } catch (TransformerConfigurationException e) {
    //         LOGGER.error("ValidateWithSchematron: Transformer configuration error", e);
    //         throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
    //     } catch (TransformerException e) {
    //         LOGGER.error("ValidateWithSchematron: Transformer error", e);
    //         throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
    //     }
    // }

    // private void validateWithSchematron(String xmlContent, String schematronContent) throws UserMessageValidatorSpiException {
    //     LOGGER.info("ValidateWithSchematron: Calling ExampleUserMessageValidatorSpi.validateWithSchematron method from the domibus-validation-extension");
    //     try {
    //         // Load the XSLT file from the classpath
    //         InputStream xsltInputStream = getClass().getClassLoader().getResourceAsStream("iso_svrl_for_xslt2.xsl");
    //         if (xsltInputStream == null) {
    //             throw new UserMessageValidatorSpiException("Error: XSLT file not found in classpath");
    //         }
    
    //         // Transform the Schematron schema to XSLT
    //         TransformerFactory factory = TransformerFactory.newInstance();
    //         Source schematronSource = new StreamSource(new StringReader(schematronContent));
    //         StringWriter xsltWriter = new StringWriter();
    //         Transformer transformer = factory.newTransformer(new StreamSource(xsltInputStream));
    //         transformer.transform(schematronSource, new StreamResult(xsltWriter));
    //         String xsltContent = xsltWriter.toString();
    
    //         // Compile the transformed XSLT
    //         Transformer xsltTransformer = factory.newTransformer(new StreamSource(new StringReader(xsltContent)));
    
    //         // Transform the XML content using the compiled Schematron XSLT
    //         Source xmlSource = new StreamSource(new StringReader(xmlContent));
    //         StringWriter resultWriter = new StringWriter();
    //         Result result = new StreamResult(resultWriter);
    //         xsltTransformer.transform(xmlSource, result);
    
    //         // Check the result for validation errors
    //         String validationResult = resultWriter.toString();
    //         if (validationResult.contains("failed-assert")) {
    //             LOGGER.error("ValidateWithSchematron: Schematron validation failed: " + validationResult);
    //             throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
    //         }
    //     } catch (TransformerConfigurationException e) {
    //         LOGGER.error("ValidateWithSchematron: Transformer configuration error", e);
    //         throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
    //     } catch (TransformerException e) {
    //         LOGGER.error("ValidateWithSchematron: Transformer error", e);
    //         throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
    //     }
    // }

    // private void validateWithSchematron(String payloadContent, String schematronContent) throws UserMessageValidatorSpiException {
    //     // try (StringReader schematronReader = new StringReader(schematronContent);
    //     //      StringReader payloadReader = new StringReader(payloadContent)) {

    //     try (StringReader schematronReader = new StringReader(removeBOM(schematronContent));
    //      StringReader payloadReader = new StringReader(removeBOM(payloadContent))) {
            
    //         LOGGER.info("ValidateWithSchematron: Calling ExampleUserMessageValidatorSpi.validateWithSchematron method from the domibus-validation-extension");

    //         // Parse the Schematron file
    //         Source schematronSource = new StreamSource(schematronReader);
    //         LOGGER.info("ValidateWithSchematron: Schematron source: " + schematronSource);

    //         // Create a Schematron Validator
    //         SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    //         LOGGER.info("ValidateWithSchematron: Schema factory: " + factory);
    //         Schema schema = factory.newSchema(schematronSource);
    //         LOGGER.info("ValidateWithSchematron: Schema: " + schema);
    //         Validator validator = schema.newValidator();
    //         LOGGER.info("ValidateWithSchematron: Validator: " + validator);

    //         // Validate the payload content
    //         Source payloadSource = new StreamSource(payloadReader);
    //         LOGGER.info("ValidateWithSchematron: Payload source: " + payloadSource);
    //         validator.validate(payloadSource);
    //         LOGGER.info("ValidateWithSchematron: Schematron validation passed.");

    //     } catch (SAXException e) {
    //         LOGGER.error("Schematron validation failed due to XML error", e);
    //         throw new UserMessageValidatorSpiException("Error: Invalid payload according to Schematron rules.", e);
    //     } catch (IOException e) {
    //         LOGGER.error("IO error during Schematron validation", e);
    //         throw new UserMessageValidatorSpiException("Error: Unable to validate payload due to IO issues.", e);
    //     }
    // }

    // public void validateWithSchematron(String payloadContent, String schematronContent) throws UserMessageValidatorSpiException {
    //     try (StringReader schematronReader = new StringReader(removeBOM(schematronContent));
    //          StringReader payloadReader = new StringReader(removeBOM(payloadContent))) {

    //         LOGGER.info("ValidateWithSchematron: Calling ExampleUserMessageValidatorSpi.validateWithSchematron method from the domibus-validation-extension");

    //         LOGGER.debug("ValidateWithSchematron: Payload conent: " + payloadContent);
    //         LOGGER.debug("ValidateWithSchematron: Schematron content: " + schematronContent);

    //         // Load the Schematron rules
    //         // SchematronResourcePure schematron = SchematronResourcePure.fromString(schematronContent);
    //         // if (!schematron.isValidSchematron()) {
    //         //     throw new UserMessageValidatorSpiException("Invalid Schematron rules.");
    //         // }            

    //         // IErrorList errorList = schematron.getSchematronValidity(new LoggingPSErrorHandler()).getAllErrors();
    //         // if (errorList.containsAtLeastOneError()) {
    //         //     LOGGER.error("ValidateWithSchematron: Invalid Schematron rules");
    //         //     errorList.forEach(error -> LOGGER.error("Schematron validation error: " + error.getAsString(Locale.ENGLISH)));
    //         //     throw new UserMessageValidatorSpiException("Invalid Schematron rules.");
    //         // }

    //         SchematronResourcePure schematron = SchematronResourcePure.fromString(schematronContent, StandardCharsets.UTF_8);
    //         LOGGER.info("ValidateWithSchematron: Schematron rules: " + schematron);

    //         if (!schematron.isValidSchematron()) {
    //             LOGGER.error("ValidateWithSchematron: Invalid Schematron rules");
    //             // IErrorList errorList = schematron.getErrorList();
    //             // errorList.forEach(error -> LOGGER.error("Schematron validation error: " + error.getAsString(Locale.ENGLISH)));
    //             throw new UserMessageValidatorSpiException("Invalid Schematron rules.");
    //         }

    //         // Validate the payload content
    //         SchematronOutputType validationResult = schematron.applySchematronValidationToSVRL(new StreamSource(payloadReader));
    //         LOGGER.info("ValidateWithSchematron: Schematron validation result: " + validationResult);
    //         if (!SVRLHelper.getAllFailedAssertions(validationResult).isEmpty()) {
    //             LOGGER.error("Schematron validation failed: " + SVRLHelper.getAllFailedAssertions(validationResult));
    //             throw new UserMessageValidatorSpiException("Error: Invalid payload according to Schematron rules.");
    //         }

    //         LOGGER.info("ValidateWithSchematron: Schematron validation passed.");

    //     } catch (IOException e) {
    //         LOGGER.error("IO error during Schematron validation", e);
    //         throw new UserMessageValidatorSpiException("Error: Unable to validate payload due to IO issues.", e);
    //     } catch (Exception e) {
    //         LOGGER.error("Schematron validation failed", e);
    //         throw new UserMessageValidatorSpiException("Error: Schematron validation failed.", e);
    //     }
    // }

    public void validateWithSchematron(String payloadContent, String schematronContent) throws UserMessageValidatorSpiException {
        try {
            LOGGER.info("ValidateWithSchematron: Calling ExampleUserMessageValidatorSpi.validateWithSchematron method from the domibus-validation-extension");
            // LOGGER.info("ValidateWithSchematron: Payload content: " + payloadContent);
            SchematronResourcePure schematron = SchematronResourcePure.fromString(schematronContent, StandardCharsets.UTF_8);

            if (!schematron.isValidSchematron()) {
                LOGGER.error("ValidateWithSchematron: Invalid Schematron rules");
                // IErrorList errorList = schematron.getErrorList();
                // errorList.forEach(error -> LOGGER.error("Schematron validation error: " + error.getAsString(Locale.ENGLISH)));
                throw new UserMessageValidatorSpiException("Invalid Schematron rules.");
            }

            StringReader payloadReader = new StringReader(removeBOM(payloadContent));
            SchematronOutputType validationOutput = schematron.applySchematronValidationToSVRL(new StreamSource(payloadReader));

            List<SVRLFailedAssert> failedAssertions = SVRLHelper.getAllFailedAssertions(validationOutput);

            if (!failedAssertions.isEmpty()) {
                StringBuilder errors = new StringBuilder("XML validation failed with the following errors:\n");
                for (SVRLFailedAssert failedAssert : failedAssertions) {
                    errors.append("- ").append(failedAssert.getText()).append("\n");
                }
                //throw new Exception(errors.toString());
                LOGGER.error("Schematron validation failed: " + errors.toString());
                throw new UserMessageValidatorSpiException("Error: Invalid payload according to Schematron rules.");
            }
        
            LOGGER.info("ValidateWithSchematron: Schematron validation passed.");

        } catch (Exception e) {
            LOGGER.error("Schematron validation failed", e);
            throw new UserMessageValidatorSpiException("Error: Schematron validation failed.", e);
        }       
    }
    
    private String removeBOM(String content) {
        if (content.startsWith("\uFEFF")) {
            return content.substring(1);
        }
        return content;
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString().trim();  // Trim to remove extra newline
    }
    
    @Override
    public void validatePayload(InputStream payload, String mimeType) throws UserMessageValidatorSpiException {
        LOGGER.info("ValidatePayload: Calling ExampleUserMessageValidatorSpi.validatePayload method from the domibus-validation-extension");

        try {
            // Read the payload content
            // String payloadContent = new BufferedReader(new InputStreamReader(payload, StandardCharsets.UTF_8))
            //         .lines()
            //         .collect(Collectors.joining("\n"));

            String payloadContent = readInputStream(payload) + ">";
            LOGGER.info("ValidatePayload: Payload Content: " + payloadContent);

            // Parse the XML content
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(payloadContent.getBytes(StandardCharsets.UTF_8)));

            // Process the XML document
            Element rootElement = document.getDocumentElement();
                        
            if (rootElement == null) {
                LOGGER.error("ValidatePayload: The root element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the root element name
            String rootElementValue = document.getDocumentElement().getNodeName();
            LOGGER.info("ValidatePayload: Root element of the XML: " + rootElement);

            if (!rootElementValue.contains("XHE")) {
                LOGGER.error("ValidatePayload: The root element is not XHE");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the XHEVersionID element
            Element xheVersionIdElement = (Element) document.getElementsByTagName("xhb:XHEVersionID").item(0);

            if (xheVersionIdElement == null) {
                LOGGER.error("ValidatePayload: The XHEVersionID element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the value of the XHEVersionID element
            String xheVersionIdValue = document.getElementsByTagName("xhb:XHEVersionID").item(0).getTextContent();
            LOGGER.info("ValidatePayload: XHEVersionID value: " + xheVersionIdValue);

            if (!xheVersionIdValue.equals("1.0")) {
                LOGGER.error("ValidatePayload: The XHEVersionID value is not 1.0");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the CustomizationID element
            Element customizationIdElement = (Element) document.getElementsByTagName("xhb:CustomizationID").item(0);

            if (customizationIdElement == null) {
                LOGGER.error("ValidatePayload: The CustomizationID element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the value of the CustomizationID element
            String customizationIdValue = customizationIdElement.getTextContent();
            LOGGER.info("ValidatePayload: CustomizationID value: " + customizationIdValue);

            if (!customizationIdValue.equals("http://docs.oasis-open.org/bdxr/ns/XHE/1/ExchangeHeaderEnvelope::XHE##dbnalliance-envelope-1.0")) {
                LOGGER.error("ValidatePayload: The CustomizationID value is not http://docs.oasis-open.org/bdxr/ns/XHE/1/ExchangeHeaderEnvelope::XHE##dbnalliance-envelope-1.0");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the schemeID value of the CustomizationID element
            String schemeIdValue = customizationIdElement.getAttribute("schemeID");
            LOGGER.info("ValidatePayload: CustomizationID schemeID value: " + schemeIdValue);

            if (!schemeIdValue.equals("bdx-docid-qns")) {
                LOGGER.error("ValidatePayload: The CustomizationID schemeID value is not bdx-docid-qns");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the ProfileID element
            Element profileIdElement = (Element) document.getElementsByTagName("xhb:ProfileID").item(0);

            if (profileIdElement == null) {
                LOGGER.error("ValidatePayload: The ProfileID element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }
            
            // Get and validate the value of the ProfileID element
            String profileIdValue = profileIdElement.getTextContent();
            LOGGER.info("ValidatePayload: ProfileID value: " + profileIdValue);

            if (!profileIdValue.equals("dbnalliance-envelope-1.0")) {
                LOGGER.error("ValidatePayload: The ProfileID value is not dbnalliance-envelope-1.0");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the Header element
            Element headerElement = (Element) document.getElementsByTagName("xha:Header").item(0);

            if (headerElement == null) {
                LOGGER.error("ValidatePayload: The Header element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the ID element of the Header element
            Element headerIdElement = (Element) headerElement.getElementsByTagName("xhb:ID").item(0);

            if (headerIdElement == null) {
                LOGGER.error("ValidatePayload: The Header ID element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }                        

            // Get and validate the CreationDateTime element of the Header element
            Element creationDateTimeElement = (Element) headerElement.getElementsByTagName("xhb:CreationDateTime").item(0);

            if (creationDateTimeElement == null) {
                LOGGER.error("ValidatePayload: The Header CreationDateTime element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the FromParty element of the Header element
            Element fromPartyElement = (Element) headerElement.getElementsByTagName("xha:FromParty").item(0);

            if (fromPartyElement == null) {
                LOGGER.error("ValidatePayload: The FromParty element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }
            
            // Get and validate the PartyIdentification element of the FromParty element
            Element fromPartyIdentificationElement = (Element) fromPartyElement.getElementsByTagName("xha:PartyIdentification").item(0);

            if (fromPartyIdentificationElement == null) {
                LOGGER.error("ValidatePayload: The FromParty PartyIdentification element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the ID element of the PartyIdentification element
            Element fromPartyIdElement = (Element) fromPartyIdentificationElement.getElementsByTagName("xhb:ID").item(0);

            if (fromPartyIdElement == null) {
                LOGGER.error("ValidatePayload: The FromParty PartyID element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // TODO: Validate the From Party ID against DBNAlliance Policy for Using Identifiers
            // String fromPartyIdValue = fromPartyIdElement.getTextContent();

            // TODO: Validate the From Party scheme against DBNAlliance Policy for Using Identifiers
            // String fromPartyIdSchemeID = fromPartyIdElement.getAttribute("schemeID");

            // Get and compare the values of the Header ID and FromParty ID elements
            String headerIdValue = headerIdElement.getTextContent();
            String fromPartyIdValue = fromPartyIdElement.getTextContent();
            LOGGER.info("ValidatePayload: Header ID value: " + headerIdValue);
            LOGGER.info("ValidatePayload: FromParty ID value: " + fromPartyIdValue);

            if (headerIdValue.equals(fromPartyIdValue)) {
                LOGGER.error("ValidatePayload: The Header ID FromParty ID values must be unique");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the ToParty element of the Header element
            Element toPartyElement = (Element) headerElement.getElementsByTagName("xha:ToParty").item(0);

            if (toPartyElement == null) {
                LOGGER.error("ValidatePayload: The ToParty element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the PartyIdentification element of the ToParty element
            Element toPartyIdentificationElement = (Element) toPartyElement.getElementsByTagName("xha:PartyIdentification").item(0);

            if (toPartyIdentificationElement == null) {
                LOGGER.error("ValidatePayload: The ToParty PartyIdentification element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the ID element of the PartyIdentification element
            Element toPartyIdElement = (Element) toPartyIdentificationElement.getElementsByTagName("xhb:ID").item(0);

            if (toPartyIdElement == null) {
                LOGGER.error("ValidatePayload: The ToParty PartyID element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // TODO: Validate the To Party ID against DBNAlliance Policy for Using Identifiers
            // String toPartyIdValue = toPartyIdElement.getTextContent();

            // TODO: Validate the To Party ID scheme against DBNAlliance Policy for Using Identifiers
            // String toPartyIdSchemeID = toPartyIdElement.getAttribute("schemeID");

            // Get and validate the Payloads element
            Element payloadsElement = (Element) document.getElementsByTagName("xha:Payloads").item(0);

            if (payloadsElement == null) {
                LOGGER.error("ValidatePayload: The Payloads element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the Payload element of the Payloads element
            Element payloadElement = (Element) payloadsElement.getElementsByTagName("xha:Payload").item(0);

            if (payloadElement == null) {
                LOGGER.error("ValidatePayload: The Payload element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // TODO: Get and validate the ID element of the Payload element
            // Element payloadIdElement = (Element) payloadElement.getElementsByTagName("xhb:ID").item(0);

            // if (payloadIdElement == null) {
            //     LOGGER.error("ValidatePayload: The Payload ID element is missing");
            //     throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            // }

            // Get and validate the ContentTypeCode element of the Payload element
            Element contentTypeCodeElement = (Element) payloadElement.getElementsByTagName("xhb:ContentTypeCode").item(0);

            if (contentTypeCodeElement == null) {
                LOGGER.error("ValidatePayload: The ContentTypeCode element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the value of the ContentTypeCode element
            String contentTypeCodeValue = contentTypeCodeElement.getTextContent();
            LOGGER.info("ValidatePayload: ContentTypeCode value: " + contentTypeCodeValue);

            if (!contentTypeCodeValue.equals("application/xml")) {
                LOGGER.error("ValidatePayload: The ContentTypeCode value is not application/xml");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the listID value of the ContentTypeCode element if it is set
            String contentTypeCodeListIdValue = contentTypeCodeElement.getAttribute("listID");
            LOGGER.info("ValidatePayload: ContentTypeCode listID value: " + contentTypeCodeListIdValue);

            if (!contentTypeCodeListIdValue.isEmpty() && !contentTypeCodeListIdValue.equals("MIME")) {
                LOGGER.error("ValidatePayload: The ContentTypeCode listID value is not MIME");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the CustomizationID element of the ContentTypeCode element
            Element payloadCustomizationIdElement = (Element) payloadElement.getElementsByTagName("xhb:CustomizationID").item(0);

            if (payloadCustomizationIdElement == null) {
                LOGGER.error("ValidatePayload: The ContentTypeCode CustomizationID element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // TODO: Get and validate the schemeID value of the CustomizationID element
            // String payloadCustomizationIdSchemeIdValue = payloadCustomizationIdElement.getAttribute("schemeID");
            // LOGGER.info("ValidatePayload: ContentTypeCode CustomizationID schemeID value: " + payloadCustomizationIdSchemeIdValue);

            // if (payloadCustomizationIdElement != null && payloadCustomizationIdSchemeIdValue.isEmpty()) {
            //     LOGGER.error("ValidatePayload: The ContentTypeCode CustomizationID schemeID value is missing");
            //     throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            // }

            // Get and validate the ProfileID element
            Element payloadProfileIdElement = (Element) payloadElement.getElementsByTagName("xhb:ProfileID").item(0);

            if (payloadProfileIdElement == null) {
                LOGGER.error("ValidatePayload: The Payload ProfileID element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the InstanceEncryptionIndicator element
            Element instanceEncryptionIndicatorElement = (Element) payloadElement.getElementsByTagName("xhb:InstanceEncryptionIndicator").item(0);

            if (instanceEncryptionIndicatorElement == null) {
                LOGGER.error("ValidatePayload: The InstanceEncryptionIndicator element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the value of the InstanceEncryptionIndicator element
            boolean instanceEncryptionIndicatorValue = Boolean.parseBoolean(instanceEncryptionIndicatorElement.getTextContent());
            LOGGER.info("ValidatePayload: InstanceEncryptionIndicator value: " + instanceEncryptionIndicatorValue);

            // Get and validate the InstanceEncryptionMethod element if the InstanceEncryptionIndicator value equals true
            if (instanceEncryptionIndicatorValue) {
                Element instanceEncryptionMethodElement = (Element) payloadElement.getElementsByTagName("xhb:InstanceEncryptionMethod").item(0);

                if (instanceEncryptionMethodElement == null) {
                    LOGGER.error("ValidatePayload: The InstanceEncryptionMethod element is missing");
                    throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
                }
            }

            // Get and validate the InstanceHashValue element
            Element instanceHashValueElement = (Element) payloadElement.getElementsByTagName("xhb:InstanceHashValue").item(0);

            if (instanceHashValueElement != null) {
                LOGGER.error("ValidatePayload: InstanceHashValue element cannot be present");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Get and validate the PayloadContent element
            Element payloadContentElement = (Element) payloadElement.getElementsByTagName("xha:PayloadContent").item(0);

            if (payloadContentElement == null) {
                LOGGER.error("ValidatePayload: The PayloadContent element is missing");
                throw new UserMessageValidatorSpiException("Error: The XHE Envelope is invalid.");
            }

            // Validate the payload content according to the specified rules
            validatePayloadContent(rootElement);

        } catch (Exception e) {
            LOGGER.error("ValidatePayload: Error validating payload", e);
            throw new UserMessageValidatorSpiException("Error validating payload", e);
        }
    }
}
