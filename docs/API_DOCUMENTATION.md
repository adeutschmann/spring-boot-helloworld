# API Documentation

This document provides comprehensive documentation for all endpoints available in the Spring Boot HelloWorld application.

## Overview

The HelloWorld API provides a simple REST endpoint demonstrating Spring Boot's functional web framework using RouterFunction and handler patterns. The API follows RESTful principles and returns JSON responses.

### Base URL
```
http://localhost:8080
```

### API Version
- **Current Version**: 1.0
- **Spring Boot Version**: 3.5.5
- **Java Version**: 21

### Content Types
- **Request**: `application/json`
- **Response**: `application/json`

## Authentication

Currently, the API does not require authentication for the `/hello` endpoint. Future versions may implement:
- JWT Token-based authentication
- OAuth 2.0 integration
- API key authentication

## Endpoints

### GET /hello

Returns a simple greeting message in JSON format.

#### Request

**HTTP Method:** `GET`  
**URL:** `/hello`  
**Content-Type:** `application/json`

**Headers:**
| Header | Required | Description | Example |
|--------|----------|-------------|---------|
| Accept | No | Content type expected in response | `application/json` |
| User-Agent | No | Client identification | `curl/7.68.0` |

**Query Parameters:**
| Parameter | Type | Required | Description | Default | Example |
|-----------|------|----------|-------------|---------|---------|
| None | - | - | This endpoint accepts no query parameters | - | - |

**Path Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| None | - | - | This endpoint has no path parameters | - |

#### Response

**Success Response:**

**Status Code:** `200 OK`  
**Content-Type:** `application/json`

```json
{
  "message": "Hello World"
}
```

**Response Schema:**
```json
{
  "type": "object",
  "properties": {
    "message": {
      "type": "string",
      "description": "Greeting message",
      "example": "Hello World"
    }
  },
  "required": ["message"]
}
```

**Response Headers:**
| Header | Description | Example |
|--------|-------------|---------|
| Content-Type | Response content type | `application/json` |
| Content-Length | Size of response body | `27` |
| Date | Response timestamp | `Sat, 24 Aug 2025 10:30:00 GMT` |

#### Error Responses

**400 Bad Request**
```json
{
  "timestamp": "2025-08-24T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request format",
  "path": "/hello"
}
```

**500 Internal Server Error**
```json
{
  "timestamp": "2025-08-24T10:30:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/hello"
}
```

#### Examples

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/hello" \
     -H "Accept: application/json"
```

**Response:**
```json
{
  "message": "Hello World"
}
```

**JavaScript Fetch Example:**
```javascript
fetch('http://localhost:8080/hello', {
  method: 'GET',
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

**Python Requests Example:**
```python
import requests

url = "http://localhost:8080/hello"
headers = {
    "Accept": "application/json"
}

response = requests.get(url, headers=headers)
data = response.json()
print(data)
```

**Java Example (using RestTemplate):**
```java
RestTemplate restTemplate = new RestTemplate();
String url = "http://localhost:8080/hello";

HttpHeaders headers = new HttpHeaders();
headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
HttpEntity<?> entity = new HttpEntity<>(headers);

ResponseEntity<Map> response = restTemplate.exchange(
    url, HttpMethod.GET, entity, Map.class);

Map<String, String> responseBody = response.getBody();
System.out.println(responseBody.get("message"));
```

## Health Check Endpoints

### GET /actuator/health

Returns the health status of the application.

#### Request

**HTTP Method:** `GET`  
**URL:** `/actuator/health`

#### Response

**Success Response:**
```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250790436864,
        "free": 123456789012,
        "threshold": 10485760,
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

#### Example

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/actuator/health"
```

### GET /actuator/health/liveness

Kubernetes liveness probe endpoint.

#### Response
```json
{
  "status": "UP"
}
```

### GET /actuator/health/readiness

Kubernetes readiness probe endpoint.

#### Response
```json
{
  "status": "UP"
}
```

## Metrics Endpoints

### GET /actuator/metrics

Returns available metrics for monitoring.

#### Response
```json
{
  "names": [
    "hello_requests_total",
    "hello_response_time",
    "jvm.memory.used",
    "jvm.gc.memory.allocated",
    "system.cpu.usage",
    "process.uptime"
  ]
}
```

### GET /actuator/prometheus

Returns metrics in Prometheus format for scraping.

#### Response
```
# HELP hello_requests_total Total number of hello requests
# TYPE hello_requests_total counter
hello_requests_total{endpoint="hello"} 42.0

# HELP hello_response_time Response time for hello endpoint
# TYPE hello_response_time summary
hello_response_time_count{} 42.0
hello_response_time_sum{} 0.125
```

## Error Handling

### Standard Error Response Format

All error responses follow a consistent format:

```json
{
  "timestamp": "2025-08-24T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error description",
  "path": "/api/endpoint",
  "details": {
    "field": "Additional context if applicable"
  }
}
```

### HTTP Status Codes

| Status Code | Description | When it occurs |
|-------------|-------------|----------------|
| 200 | OK | Successful GET request |
| 400 | Bad Request | Invalid request format or parameters |
| 401 | Unauthorized | Authentication required (if implemented) |
| 403 | Forbidden | Access denied (if authorization implemented) |
| 404 | Not Found | Endpoint does not exist |
| 405 | Method Not Allowed | HTTP method not supported for endpoint |
| 500 | Internal Server Error | Unexpected server error |
| 503 | Service Unavailable | Service temporarily unavailable |

## Rate Limiting

Currently, no rate limiting is implemented. For production deployments, consider implementing:
- Request rate limiting (e.g., 100 requests per minute per IP)
- Burst protection
- API key-based rate limiting

## CORS (Cross-Origin Resource Sharing)

The application supports CORS for web browser requests:

**Allowed Origins:** All origins (`*`) - should be restricted in production  
**Allowed Methods:** `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`  
**Allowed Headers:** `Content-Type`, `Authorization`, `X-Requested-With`  
**Max Age:** 3600 seconds

## Security Considerations

### Input Validation
- All input parameters are validated for type and format
- SQL injection protection (when database is used)
- XSS prevention through proper output encoding

### Security Headers
The following security headers are included in responses:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Referrer-Policy: strict-origin-when-cross-origin`

## Versioning Strategy

### Current Approach
- URL-based versioning (future): `/api/v1/hello`
- Header-based versioning (future): `Accept: application/vnd.api+json;version=1`
- Backward compatibility maintained for existing endpoints

### Migration Path
When new versions are introduced:
1. New endpoints will include version in URL
2. Old endpoints remain functional with deprecation warnings
3. Documentation will clearly indicate deprecated endpoints
4. Minimum 6-month deprecation period before removal

## Testing the API

### Integration Tests

**Test Suite Location:** `src/test/java/ch/adeutschmanndev/helloworld/`

**Key Test Classes:**
- `HelloworldApplicationTests.java` - Integration tests
- `HelloWorldHandlerTest.java` - Unit tests for handlers
- `HelloWorldRouterTest.java` - Router configuration tests

### Manual Testing

**Postman Collection:**
```json
{
  "info": {
    "name": "Spring Boot HelloWorld API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Hello World",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Accept",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/hello",
          "host": ["{{baseUrl}}"],
          "path": ["hello"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    }
  ]
}
```

### Load Testing

**Example k6 Script:**
```javascript
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 0 },
  ],
};

export default function() {
  let response = http.get('http://localhost:8080/hello');
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
    'contains message': (r) => r.json('message') === 'Hello World',
  });
}
```

## Container API Access

### Docker/Podman Deployment

When running in containers, the API is accessible on the exposed port:

```bash
# Run container
podman run -p 8080:8080 spring-boot-helloworld:latest

# Test endpoint
curl http://localhost:8080/hello
```

### Kubernetes Deployment

```yaml
apiVersion: v1
kind: Service
metadata:
  name: hello-service
spec:
  selector:
    app: spring-boot-helloworld
  ports:
  - port: 80
    targetPort: 8080
```

**Access via Service:**
```bash
kubectl port-forward service/hello-service 8080:80
curl http://localhost:8080/hello
```

## Monitoring and Observability

### Application Metrics

The API exposes metrics for monitoring:
- Request count per endpoint
- Response time percentiles
- Error rates
- JVM metrics (memory, GC, threads)

### Distributed Tracing

Future versions will include:
- Jaeger/Zipkin integration
- Request correlation IDs
- Cross-service tracing

### Logging

Request/response logging includes:
- Request ID for correlation
- HTTP method and path
- Response status and duration
- User agent and IP address

## Future Enhancements

### Planned Features
- Additional endpoints for CRUD operations
- Database integration with JPA
- Caching support with Redis
- Message queue integration
- Real-time updates with WebSocket

### API Evolution
- GraphQL endpoint
- gRPC support for high-performance scenarios
- Async processing capabilities
- Batch operation support

## Support and Contact

### Documentation Resources
- [Functional Web Framework Guide](FUNCTIONAL_WEB_FRAMEWORK.md)
- [Local Development Setup](LOCAL_DEVELOPMENT.md)
- [Testing Strategy](TESTING_STRATEGY.md)

### Troubleshooting
For common issues and solutions, refer to the [Local Development Setup](LOCAL_DEVELOPMENT.md#troubleshooting) guide.

### Contributing
See the main [README.md](../README.md#contributing) for contribution guidelines.

---

**API Documentation Version:** 1.0  
**Last Updated:** August 24, 2025  
**Generated for:** Spring Boot HelloWorld v0.0.1-SNAPSHOT
