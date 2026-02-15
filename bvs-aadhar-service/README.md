# BVS Aadhar Service

A Spring Boot microservice for parsing and validating Aadhar XML files.

## Features

- **XML Parsing**: Parse Aadhar XML files and extract key information (UID, Name, DOB, Gender, Address)
- **File Validation**: Validates file type, size, and format before processing
- **Security**: Protected against XXE (XML External Entity) attacks
- **CPU Protection**: Processing timeout to prevent CPU exhaustion from malicious or complex XML files
- **Error Handling**: Comprehensive error handling with detailed error messages

## API Endpoints

### Parse Aadhar XML
**POST** `/api/aadhar/parse`

Upload an Aadhar XML file for parsing.

**Request:**
- Method: `POST`
- Content-Type: `multipart/form-data`
- Parameter: `file` (XML file)

**Response:**
```json
{
  "status": "success",
  "message": "XML file parsed successfully",
  "data": {
    "uid": "123456789012",
    "name": "John Doe",
    "dob": "01-01-1990",
    "gender": "M",
    "address": "123 Main Street, City, State - 123456"
  }
}
```

**Error Response:**
```json
{
  "status": "failure",
  "message": "Invalid or corrupt XML file",
  "data": null
}
```

### Health Check
**GET** `/api/health`

Check the health status of the service.

**Response:**
```json
{
  "status": "UP",
  "service": "bvs-aadhar-service"
}
```

## Configuration

The service can be configured via `application.properties`:

```properties
# Server port
server.port=8082

# Maximum file size in bytes (default: 10MB)
aadhar.xml.max-file-size=10485760

# Processing timeout in milliseconds (default: 5 seconds)
aadhar.xml.processing-timeout=5000

# Spring multipart configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Security Features

1. **File Size Validation**: Prevents processing of files larger than configured limit
2. **File Type Validation**: Only accepts `.xml` files
3. **XXE Protection**: All external entity processing disabled
4. **Processing Timeout**: Prevents CPU exhaustion from complex XML structures
5. **Corrupt File Detection**: Validates XML structure before processing

## Running the Service

### Build
```bash
./gradlew :bvs-aadhar-service:build
```

### Run
```bash
./gradlew :bvs-aadhar-service:bootRun
```

The service will start on port **8082**.

## Example Usage

### Using cURL
```bash
curl -X POST http://localhost:8082/api/aadhar/parse \
  -F "file=@/path/to/aadhar.xml" \
  -H "Content-Type: multipart/form-data"
```

### Sample Aadhar XML
```xml
<?xml version="1.0" encoding="UTF-8"?>
<PrintLetterBarcodeData>
    <uid>123456789012</uid>
    <name>John Doe</name>
    <dob>01-01-1990</dob>
    <gender>M</gender>
    <address>123 Main Street, City, State - 123456</address>
</PrintLetterBarcodeData>
```

## Error Scenarios

The service handles the following error scenarios:

1. **Empty File**: Returns error if file is empty or null
2. **File Too Large**: Returns error if file exceeds maximum size limit
3. **Wrong File Type**: Returns error if file is not an XML file
4. **Corrupt XML**: Returns error if XML structure is invalid
5. **Processing Timeout**: Returns error if parsing takes too long

## Technology Stack

- Java 21
- Spring Boot 3.3.4
- Spring Web
- Spring Validation
- Lombok
