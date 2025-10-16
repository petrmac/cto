# Docker Build and Publishing Guide

This project uses Google Jib Maven Plugin for building Docker images without requiring a Docker daemon.

## Building Docker Image

### Build to tarball (local testing, no Docker required)
```bash
mvn clean package jib:buildTar
```
This creates `target/jib-image.tar` (single-platform for current architecture).

### Build to Docker daemon (requires Docker installed)
```bash
mvn clean package jib:dockerBuild
```

### Build and push to registry (no Docker required)
```bash
# Single platform (default)
mvn clean package jib:build

# Multi-platform (amd64 + arm64)
mvn clean package jib:build -Pmultiplatform
```

## Docker Image Configuration

- **Base Image**: eclipse-temurin:21-jre-alpine
- **Multi-architecture support**: amd64, arm64 (with `-Pmultiplatform` profile)
- **Registry**: ghcr.io/conrad-ccp
- **Image name**: comline-edge
- **Tags**: `${version}`, `latest`
- **Image size**: ~115MB

## JVM Configuration

The container is configured with:
- Initial heap: 256MB (-Xms256m)
- Maximum heap: 512MB (-Xmx512m)
- Garbage Collector: G1GC
- Port: 8080

## Running the Container

```bash
docker run -p 8080:8080 \
  -e COMLINE_BASE_URL=https://ctofinder.comline-shop.de/4DCGI/direct \
  -e COMLINE_CUSTOMER_NUMBER=your-customer-number \
  -e COMLINE_PASSWORD=your-password \
  ghcr.io/conrad-ccp/comline-edge:latest
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `COMLINE_BASE_URL` | ComLine API base URL | https://ctofinder.comline-shop.de/4DCGI/direct |
| `COMLINE_CUSTOMER_NUMBER` | Customer number | 15017319 |
| `COMLINE_PASSWORD` | API password | (from config) |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | - |

## OpenAPI Schema

The OpenAPI schema is included in the Docker image at:
- Container path: `/app/resources/openapi/comline-api.yaml`
- JAR path: `META-INF/openapi/comline-api.yaml`

## Publishing Artifacts

### Maven Artifacts (JAR, Sources, Javadoc)
```bash
mvn clean deploy
```

This publishes:
- Main JAR with compiled classes
- Sources JAR with source code
- Javadoc JAR with API documentation
- Generated OpenAPI stubs
- OpenAPI schema in META-INF

### Docker Image
```bash
mvn clean package jib:build
```

### Both Artifacts and Docker Image
```bash
mvn clean deploy jib:build
```

## GitHub Packages Authentication

To publish to GitHub Packages, configure your `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

## Container Labels

The Docker image includes OCI labels:
- `org.opencontainers.image.title`: ComLine Edge Service
- `org.opencontainers.image.description`: ComLine API Edge Service
- `org.opencontainers.image.version`: Project version
- `org.opencontainers.image.vendor`: Conrad CCP

## Health Check

The application exposes health endpoints via Spring Actuator:
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Prometheus: `http://localhost:8080/actuator/prometheus`
