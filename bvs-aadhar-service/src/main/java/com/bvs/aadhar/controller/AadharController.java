package com.bvs.aadhar.controller;

import com.bvs.aadhar.dto.AadharData;
import com.bvs.aadhar.dto.AadharResponse;
import com.bvs.aadhar.exception.InvalidXmlException;
import com.bvs.aadhar.exception.XmlProcessingTimeoutException;
import com.bvs.aadhar.service.AadharXmlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/aadhar")
@RequiredArgsConstructor
public class AadharController {

    private final AadharXmlService aadharXmlService;

    @PostMapping("/parse")
    public ResponseEntity<AadharResponse> parseAadharXml(@RequestParam("file") MultipartFile file) {
        log.info("Received request to parse Aadhar XML file: {}", file.getOriginalFilename());
        
        try {
            AadharData data = aadharXmlService.parseAadharXml(file);
            AadharResponse response = new AadharResponse("success", "XML file parsed successfully", data);
            return ResponseEntity.ok(response);
        } catch (InvalidXmlException e) {
            log.error("Invalid XML: {}", e.getMessage());
            AadharResponse response = new AadharResponse("failure", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (XmlProcessingTimeoutException e) {
            log.error("Processing timeout: {}", e.getMessage());
            AadharResponse response = new AadharResponse("failure", e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            AadharResponse response = new AadharResponse("failure", "An unexpected error occurred while processing the file", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
