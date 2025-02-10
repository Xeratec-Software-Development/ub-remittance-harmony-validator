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
            }
        }      
    }

    private String loadSchematronFile(String fileName) throws IOException {
        LOGGER.info("LoadSchematronFile: Calling ExampleUserMessageValidatorSpi.loadSchematronFile method from the domibus-validation-extension");
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public void validateXheEnvelope(Element rootElement) throws UserMessageValidatorSpiException {
        try {
            LOGGER.info("ValidateXheEnvelope: Calling ExampleUserMessageValidatorSpi.validateXheEnvelope method from the domibus-validation-extension");
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(rootElement), new StreamResult(writer));
            String rootElementContent = writer.toString();

            // Load the Schematron file content
            LOGGER.info("validateXheEnvelope: Loading Schematron file content");
            String schematronContent = loadSchematronFile("DBNA_XHE.sch");

            // Validate the XHE envelope using the Schematron file
            LOGGER.info("validateXheEnvelope: Validating XHE envelope with Schematron");
            validateWithSchematron(rootElementContent, schematronContent); 

            LOGGER.info("ValidateXheEnvelope: XHE envelope is valid.");
        } catch (TransformerConfigurationException e) {
            LOGGER.error("validateXheEnvelope: Transformer configuration error", e);
            throw new UserMessageValidatorSpiException("Error: The XHE envelope is invalid.");
        } catch (TransformerException e) {
            LOGGER.error("validateXheEnvelope: Transformer error", e);
            throw new UserMessageValidatorSpiException("Error: The XHE envelope is invalid.");
        } catch (IOException e) {
            LOGGER.error("validateXheEnvelope: IO error", e);
            throw new UserMessageValidatorSpiException("Error: Unable to load Schematron file.");
        }
    }

    public void validatePayloadContent(Element payloadContentElement) throws UserMessageValidatorSpiException {
        try {
            LOGGER.info("ValidatePayloadContent: Calling ExampleUserMessageValidatorSpi.validatePayloadContent method from the domibus-validation-extension");
            
            // Initialize the Schematron content
            String schematronContent = null;

            // Initialize the transformer factory and transformer
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();

            // Convert the payload content element to a string
            Element invoiceElement = (Element) payloadContentElement.getElementsByTagName("Invoice").item(0);
            Element creditNoteElement = (Element) payloadContentElement.getElementsByTagName("CreditNote").item(0);
            Element documentElement = (Element) payloadContentElement.getElementsByTagName("Document").item(0);

            // Validate the payload content element
            if (invoiceElement != null) {
                transformer.transform(new DOMSource(invoiceElement), new StreamResult(writer));

                // Get and validate the CustomizationID element
                Element customizationIdElement = (Element) invoiceElement.getElementsByTagName("cbc:CustomizationID").item(0);

                // Check if the CustomizationID element is missing
                if (customizationIdElement == null) {
                    LOGGER.error("ValidatePayload: The CustomizationID element is missing");
                    throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
                }

                // Get and validate the value of the CustomizationID element
                String customizationIdValue = customizationIdElement.getTextContent();
                LOGGER.info("ValidatePayload: CustomizationID value: " + customizationIdValue);

                if (customizationIdValue.equals("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##DBNAlliance-1.0-data-Core")) {
                    LOGGER.info("ValidatePayloadContent: Schematron file: " + "DBNA_Core_Invoice_Profile_1.0_Minimum.sch");
                    schematronContent = loadSchematronFile("DBNA_Core_Invoice_Profile_1.0_Minimum.sch");
                } else if (customizationIdValue.equals("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##DBNAlliance-1.0-data-Extended-embedded-attachments")) {
                    LOGGER.info("ValidatePayloadContent: Schematron file: " + "DBNA Extended Invoice Profile 1.0 Embedded Minimum.sch");
                    schematronContent = loadSchematronFile("DBNA Extended Invoice Profile 1.0 Embedded Minimum.sch");
                } else if (customizationIdValue.equals("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##DBNAlliance-1.0-data-Extended-envelope-attachments")) {
                    LOGGER.info("ValidatePayloadContent: Schematron file: " + "DBNA Extended Invoice Profile 1.0 Envelope Minimum.sch");
                    schematronContent = loadSchematronFile("DBNA Extended Invoice Profile 1.0 Envelope Minimum.sch");
                } else {
                    LOGGER.error("ValidatePayloadContent: The CustomizationID value is invalid");
                    throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
                }
            } else if (creditNoteElement != null) {
                LOGGER.info("ValidatePayloadContent: CreditNote element found");
                transformer.transform(new DOMSource(creditNoteElement), new StreamResult(writer));
                LOGGER.info("ValidatePayloadContent: Schematron file: " + "DBNA Credit Note Profile 1.0 Minimum.sch");
                schematronContent = loadSchematronFile("DBNA Credit Note Profile 1.0 Minimum.sch");
            } else if (documentElement != null) {
                LOGGER.info("ValidatePayloadContent: Document element found");
                transformer.transform(new DOMSource(documentElement), new StreamResult(writer));
                LOGGER.info("ValidatePayloadContent: Schematron file: " + "DBNA REMT.001.001.05.sch");
                schematronContent = loadSchematronFile("DBNA REMT.001.001.05.sch");
            } else {
                LOGGER.error("ValidatePayloadContent: The payload content element is invalid");
                throw new UserMessageValidatorSpiException("Error: The Payload is invalid.");
            }

            // Convert the payload content element to a string
            String payloadContent = writer.toString();

            // Validate the payload content using the Schematron file
            LOGGER.info("ValidatePayloadContent: Validating payload content with Schematron");
            validateWithSchematron(payloadContent, schematronContent);

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

    public void validateWithSchematron(String payloadContent, String schematronContent) throws UserMessageValidatorSpiException {
        try {
            LOGGER.info("ValidateWithSchematron: Calling ExampleUserMessageValidatorSpi.validateWithSchematron method from the domibus-validation-extension");
            
            // Create a Schematron resource from the Schematron content
            SchematronResourcePure schematron = SchematronResourcePure.fromString(schematronContent, StandardCharsets.UTF_8);

            // Validate the Schematron rules
            if (!schematron.isValidSchematron()) {
                LOGGER.error("ValidateWithSchematron: Invalid Schematron rules");
                throw new UserMessageValidatorSpiException("Invalid Schematron rules.");
            }

            // Apply the Schematron validation to the payload content
            StringReader payloadReader = new StringReader(removeBOM(payloadContent));
            SchematronOutputType validationOutput = schematron.applySchematronValidationToSVRL(new StreamSource(payloadReader));
            List<SVRLFailedAssert> failedAssertions = SVRLHelper.getAllFailedAssertions(validationOutput);

            // Check if the validation failed
            if (!failedAssertions.isEmpty()) {
                StringBuilder errors = new StringBuilder("XML validation failed with the following errors:\n");
                for (SVRLFailedAssert failedAssert : failedAssertions) {
                    errors.append("- ").append(failedAssert.getText()).append("\n");
                }
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
        // Read the input stream content
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                stringBuilder.append((char) c);
            }
        }
        return stringBuilder.toString();
    }
    
    @Override
    public void validatePayload(InputStream payload, String mimeType) throws UserMessageValidatorSpiException {
        try {
            LOGGER.info("ValidatePayload: Calling ExampleUserMessageValidatorSpi.validatePayload method from the domibus-validation-extension");

            LOGGER.info("ValidatePayload: Reading the payload content");
            String payloadContent = readInputStream(payload);

            // Parse the XML content
            LOGGER.info("ValidatePayload: Parsing the XML content");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(payloadContent.getBytes(StandardCharsets.UTF_8)));

            // Process the XML document
            LOGGER.info("ValidatePayload: Processing the XML document");
            Element rootElement = document.getDocumentElement();
            LOGGER.info("ValidatePayload: Root element of the XML: " + rootElement);

            // Validate the XHE envelope
            LOGGER.info("ValidatePayload: Validating the XHE envelope");
            validateXheEnvelope(rootElement);

            // Validate the payload content according to the specified rules
            LOGGER.info("ValidatePayload: Validating the payload content");
            validatePayloadContent(rootElement);                     
        } catch (Exception e) {
            LOGGER.error("ValidatePayload: Error validating payload", e);
            throw new UserMessageValidatorSpiException("Error validating payload", e);
        }
    }
}
