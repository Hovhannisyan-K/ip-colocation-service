# IP Colocation Service

A Spring Boot application that provides an endpoint for IP geolocation lookup by wrapping the FreeIPAPI service. It adds
input validation, caching (30-day TTL), timeout/error handling, and returns JSON responses.

## Features

* **REST endpoint**: `GET /ip/{ip}`
* **Input validation**: rejects invalid IP formats
* **External API integration**: calls FreeIPAPI using Spring `WebClient`
* **Timeout**: fails after 5 seconds if the API does not respond
* **Error mapping**: maps 4xx → 400 Bad Request; 5xx → 502 Bad Gateway; others → 500 Internal Server Error
* **In-memory cache**: stores responses per IP for 30 days
* **Unit tests**: covers service logic and controller behaviors

## Prerequisites

* Java 21
* Maven 3.6+
* Internet access (to download Maven dependencies and call FreeIPAPI)

## Getting Started

1. **Clone the repository**

   ```bash
   git clone https://github.com/Hovhannisyan-K/ip-colocation-service
   cd ip-colocation-service
   ```

2. **Configure the base URL**

   The default FreeIPAPI base URL (`https://freeipapi.com/api/json`) is set in `application.properties`. You can
   override it by passing a JVM property:

   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--freeipapi.baseurl=https://freeipapi.com/api/json"
   ```

3. **Build and run**

   ```bash
   mvn clean package
   java -jar target/ip-colocation-service-0.0.1-SNAPSHOT.jar
   ```

4. **Test the endpoint**

   Open your browser or use `curl`:

   ```bash
   curl http://localhost:8080/ip/136.159.0.0
   ```

   Should return:

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

5. **Run unit tests**

   ```bash
   mvn test
   ```

## Assumptions and Notes

* **Public API**: FreeIPAPI requires no API key.
* **Caching**: In-memory, not distributed. Suitable for demo/testing only.
* **Rate limiting**: Not enforced in code; the underlying client is expected to honor 1 request/sec if needed.
* **Extensions**: You can swap in Caffeine or Redis for caching, and add additional providers by implementing a common
  interface.

## Next Steps

* Add CI/CD pipeline (e.g., GitHub Actions) for automated build & test
* Integrate a proper rate-limiter (e.g., Spring Cloud Gateway with `RequestRateLimiter`)
* Replace in-memory cache with a distributed cache for horizontal scalability

---

> Developed as a technical assignment by Karen Hovhannisyan, Clinic Mind.
