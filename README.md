# IP Colocation Service

A Spring Boot application that provides IP geolocation lookup using external APIs. Features extensible provider
architecture, rate limiting, caching, and concurrent request handling.

## Features

* **REST endpoint**: `GET /ip/{ip}` - Returns geolocation data for any valid IP address
* **Input validation**: Rejects invalid IP formats using Apache Commons Validator
* **External API integration**: Uses FreeIPAPI with Spring WebClient and reactive programming
* **Rate limiting**: 1 request per second using Resilience4j to comply with API limits
* **Concurrency support**: Handles multiple concurrent requests while respecting rate limits
* **Extensible architecture**: Provider pattern allows easy integration of additional IP geolocation services
* **In-memory cache**: Stores responses per IP for 30 days using ConcurrentHashMap
* **Timeout handling**: 5-second timeout with proper error mapping
* **Error mapping**: 4xx → 400 Bad Request; 5xx → 502 Bad Gateway; others → 500 Internal Server Error
* **Comprehensive testing**: Unit tests for controllers, services, and providers

## Architecture

### Provider Pattern

- **IpGeolocationProvider**: Interface for extensibility
- **FreeIpApiProvider**: Implementation for FreeIPAPI service
- **IpLookupService**: Main service with caching and orchestration

### Concurrency & Rate Limiting

- **Resilience4j RateLimiter**: Controls API request flow (1 req/sec)
- **ConcurrentHashMap**: Thread-safe caching for concurrent access
- **Reactive WebClient**: Non-blocking HTTP client with rate limiting integration

## Prerequisites

* Java 21
* Maven 3.6+
* Internet access (to download dependencies and call FreeIPAPI)

## Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/Hovhannisyan-K/ip-colocation-service
   cd ip-colocation-service
   ```

2. **Configure the base URL (optional)**

   The default FreeIPAPI base URL is set in `application.properties`:
   ```properties
   freeipapi.baseurl=https://free.freeipapi.com/api/json
   ```

3. **Build and run**

   ```bash
   mvn clean package
   java -jar target/ip-colocation-service-0.0.1-SNAPSHOT.jar
   ```

   Or run directly with Maven:
   ```bash
   mvn spring-boot:run
   ```

4. **Test the endpoint**

   ```bash
   curl http://localhost:8080/ip/136.159.0.0
   ```

   Expected response:
   ```json
   {
     "ipAddress": "136.159.0.0",
     "continent": "Americas",
     "country": "Canada",
     "region": "Alberta",
     "city": "Calgary",
     "latitude": 51.075153,
     "longitude": -114.12841
   }
   ```

5. **Test concurrent requests**

   The system handles multiple concurrent requests:
   ```bash
   # Send multiple requests simultaneously
   for i in {1..10}; do curl http://localhost:8080/ip/8.8.8.8 & done
   ```

6. **Run unit tests**

   ```bash
   mvn test
   ```

## API Documentation

### Endpoints

#### GET /ip/{ip}

Returns geolocation information for the specified IP address.

**Parameters:**

- `ip` (path): Valid IPv4 address (e.g., `192.168.1.1`)

**Response:**

```json
{
  "ipAddress": "string",
  "continent": "string",
  "country": "string",
  "region": "string",
  "city": "string",
  "latitude": "number",
  "longitude": "number"
}
```

**Error Responses:**

- `400 Bad Request`: Invalid IP format
- `502 Bad Gateway`: External API error
- `500 Internal Server Error`: Unexpected server error

## Technical Implementation

### Rate Limiting

- **Resilience4j**: Implements 1 request per second limit
- **Reactive Integration**: Rate limiter applied to WebClient filter chain
- **Concurrent Handling**: Queues requests when rate limit is exceeded

### Caching Strategy

- **TTL**: 30 days per IP address
- **Thread Safety**: ConcurrentHashMap for concurrent access
- **Memory Efficient**: In-memory cache suitable for moderate traffic

### Error Handling

- **Timeout**: 5-second timeout for external API calls
- **Retry Logic**: Built into Resilience4j rate limiter
- **Graceful Degradation**: Proper error mapping and user-friendly messages

## Extending the Service

### Adding New Providers

1. Implement the `IpGeolocationProvider` interface:
   ```java
   @Component
   public class MyCustomProvider implements IpGeolocationProvider {
       @Override
       public IpInfoResponse getIpInfo(String ip) {
           // Your implementation
       }
       
       @Override
       public String getProviderName() {
           return "MyCustomProvider";
       }
   }
   ```

2. Spring will automatically inject your provider into `IpLookupService`

### Configuration Options

Add to `application.properties`:

```properties
# API Configuration
freeipapi.baseurl=https://free.freeipapi.com/api/json
# Rate Limiting (if needed)
resilience4j.ratelimiter.instances.freeIpApi.limit-for-period=1
resilience4j.ratelimiter.instances.freeIpApi.limit-refresh-period=1s
```

## Assumptions and Design Decisions

* **Public API**: FreeIPAPI requires no authentication
* **In-Memory Caching**: Suitable for single-instance deployments
* **Rate Limiting**: Applied at application level, not distributed
* **IP Validation**: Follows RFC standards (rejects leading zeros)
* **Error Handling**: Distinguishes between client errors and server errors
* **Concurrency**: Designed for high concurrent load while respecting API limits

## Performance Considerations

* **Caching**: Reduces external API calls by 30-day TTL
* **Rate Limiting**: Prevents API quota exhaustion
* **Reactive Programming**: Non-blocking I/O for better resource utilization
* **Concurrent Collections**: Thread-safe operations without synchronization overhead

## Testing

The project includes comprehensive unit tests:

- **Controller Tests**: HTTP endpoint behavior and error handling
- **Service Tests**: Caching logic and exception propagation
- **Provider Tests**: External API integration and rate limiting

Run tests with coverage:

```bash
mvn test jacoco:report
```

## Future Enhancements

* **Distributed Caching**: Redis integration for horizontal scaling
* **Metrics**: Prometheus/Micrometer integration for monitoring
* **Circuit Breaker**: Resilience4j circuit breaker for fault tolerance
* **Multiple Providers**: Failover between different IP geolocation services
* **Database Persistence**: Store frequently accessed IPs in database

---

> Developed as a technical assignment demonstrating Spring Boot, reactive programming, concurrency handling, and clean
> architecture principles.
