package com.bvs.aadhar.service;

import com.bvs.aadhar.dto.AadharData;
import com.bvs.aadhar.exception.InvalidXmlException;
import com.bvs.aadhar.exception.XmlProcessingTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.*;

@Slf4j
@Service
public class AadharXmlService {

    @Value("${aadhar.xml.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${aadhar.xml.processing-timeout:5000}") // 5 seconds default
    private long processingTimeout;

    private final ExecutorService executorService;

    public AadharXmlService() {
        // Create a thread pool for XML processing with timeout support
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public AadharData parseAadharXml(MultipartFile file) {
        validateFile(file);
        
        try {
            byte[] xmlContent = file.getBytes();
            
            // Process XML with timeout to prevent CPU exhaustion
            Future<AadharData> future = executorService.submit(() -> parseXmlContent(xmlContent));
            
            try {
                return future.get(processingTimeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                log.error("XML processing timed out after {} ms", processingTimeout);
                throw new XmlProcessingTimeoutException("XML processing took too long and was cancelled");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InvalidXmlException("XML processing was interrupted", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof InvalidXmlException) {
                    throw (InvalidXmlException) cause;
                }
                throw new InvalidXmlException("Error processing XML", cause);
            }
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new InvalidXmlException("Error reading the uploaded file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidXmlException("File is empty or null");
        }

        if (file.getSize() > maxFileSize) {
            throw new InvalidXmlException(String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize));
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        if (filename == null || !filename.toLowerCase().endsWith(".xml")) {
            throw new InvalidXmlException("File must be an XML file");
        }

        // Additional content type validation
        if (contentType != null && !contentType.contains("xml") && !contentType.contains("octet-stream")) {
            throw new InvalidXmlException("Invalid file type. Expected XML file");
        }
    }

    private AadharData parseXmlContent(byte[] xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            // Security: Disable external entity processing to prevent XXE attacks
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlContent));
            document.getDocumentElement().normalize();

            return extractAadharData(document);
            
        } catch (ParserConfigurationException | SAXException e) {
            log.error("Error parsing XML: {}", e.getMessage());
            throw new InvalidXmlException("Invalid or corrupt XML file", e);
        } catch (IOException e) {
            log.error("IO error during XML parsing: {}", e.getMessage());
            throw new InvalidXmlException("Error reading XML content", e);
        }
    }

    private AadharData extractAadharData(Document document) {
        Element root = document.getDocumentElement();
        
        // This is a generic implementation. Adjust based on actual Aadhar XML structure
        String uid = getElementText(document, "uid");
        String name = getElementText(document, "name");
        String dob = getElementText(document, "dob");
        String gender = getElementText(document, "gender");
        String address = getElementText(document, "address");

        // If standard tags don't exist, check for common Aadhar XML attribute patterns
        if (uid == null && root.hasAttribute("uid")) {
            uid = root.getAttribute("uid");
        }
        if (name == null && root.hasAttribute("name")) {
            name = root.getAttribute("name");
        }

        return AadharData.builder()
                .uid(uid != null ? uid : "N/A")
                .name(name != null ? name : "N/A")
                .dob(dob != null ? dob : "N/A")
                .gender(gender != null ? gender : "N/A")
                .address(address != null ? address : "N/A")
                .build();
    }

    private String getElementText(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
