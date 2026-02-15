# Docker Parallel Execution Setup Guide

## Overview
This guide explains how to run your Selenium automation tests in parallel using Docker containers with Selenium Grid.

## Architecture
- **Selenium Hub**: Central node that manages test distribution
- **Chrome Nodes**: 3 nodes for parallel Chrome browser execution (6 concurrent sessions)
- **Firefox Nodes**: 2 nodes for parallel Firefox browser execution (4 concurrent sessions)
- **Total Parallel Capacity**: Up to 10 concurrent test sessions

## Prerequisites
- Docker and Docker Compose installed
- Maven 3.6+
- Java 17+
- At least 8GB RAM available for Docker

## Files Created/Modified

### New Files:
1. **testng-docker-parallel.xml** - Test suite for parallel execution with 5 thread-count
2. **testng-docker-all-parallel.xml** - Test suite for class-level parallel execution with 10 thread-count
3. **Dockerfile** - Docker image for running tests
4. **docker/docker-compose-full.yml** - Full docker-compose with health checks

### Modified Files:
1. **docker/docker-compose.yml** - Updated with multiple nodes and networking
2. **src/test/resources/config.properties** - Added Docker Selenium Grid configuration
3. **src/main/java/core/driver/DriverFactory.java** - Added Docker execution mode support

## Quick Start

### Step 1: Start Selenium Grid in Docker

```bash
# Navigate to project root
cd Enterprise_Selenium_E2E_Framework

# Start Selenium Grid with all nodes
docker-compose -f docker/docker-compose.yml up -d

# Verify all containers are running
docker-compose -f docker/docker-compose.yml ps
```

Expected output should show all services as "running":
- selenium-hub
- chrome-node-1, chrome-node-2, chrome-node-3
- firefox-node-1, firefox-node-2

### Step 2: Update Configuration

Ensure your `config.properties` is set to Docker mode:
```properties
execution.mode=DOCKER
docker.hub.url=http://localhost:4444
browser=CHROME
```

### Step 3: Run Tests in Parallel

**Option A: Using TestNG parallel suite (Recommended)**
```bash
mvn clean test -Dsuites=testng-docker-parallel.xml
```

**Option B: Using all-parallel suite (Class-level parallelization)**
```bash
mvn clean test -Dsuites=testng-docker-all-parallel.xml
```

**Option C: Run with Maven Surefire**
```bash
mvn clean test -DthreadCount=5 -Dparallel=tests
```

## Parallel Execution Options

### testng-docker-parallel.xml
- **Parallel Type**: tests
- **Thread Count**: 5
- **Distribution**: Each test runs in separate thread
- **Best For**: Different test scenarios that need isolation

### testng-docker-all-parallel.xml
- **Parallel Type**: classes
- **Thread Count**: 10
- **Distribution**: All test classes run in parallel
- **Best For**: Maximum parallelization across multiple test classes

## Accessing VNC (Live Test Observation)

Each Selenium node runs a VNC server. You can watch tests execute in real-time:

```bash
# Chrome Node 1: localhost:7900 (password: secret)
# Chrome Node 2: localhost:7901
# Chrome Node 3: localhost:7902
# Firefox Node 1: localhost:7903
# Firefox Node 2: localhost:7904
```

### Using VNC Client:
1. Install VNC viewer (e.g., TightVNC, RealVNC)
2. Connect to `localhost:7900` (for chrome-node-1)
3. Enter password: `secret`

### Using Browser (noVNC):
```bash
# Install and run noVNC
docker run -d -p 6080:6080 --link selenium-hub:hub \
  -e DISPLAY_WIDTH=1920 -e DISPLAY_HEIGHT=1080 \
  consol/novnc:latest
# Access: http://localhost:6080
```

## Checking Grid Status

```bash
# View Grid console
# Open in browser: http://localhost:4444

# Check node status via command line
curl http://localhost:4444/wd/hub/status | jq
```

## Docker Compose Commands

```bash
# Start all services in background
docker-compose -f docker/docker-compose.yml up -d

# View logs
docker-compose -f docker/docker-compose.yml logs -f

# View logs from specific service
docker-compose -f docker/docker-compose.yml logs -f selenium-hub

# Stop all services
docker-compose -f docker/docker-compose.yml down

# Stop and remove volumes
docker-compose -f docker/docker-compose.yml down -v

# Restart a specific service
docker-compose -f docker/docker-compose.yml restart chrome-node-1
```

## Performance Optimization

### Increase Parallel Capacity

Edit `docker-compose.yml` and increase node instances:

```yaml
chrome-node-1:
  environment:
    - SE_NODE_MAX_SESSIONS=4  # Increase from 2
    - NODE_MAX_INSTANCES=4
```

Add more nodes:
```yaml
chrome-node-4:
  image: selenium/node-chrome:4.21
  container_name: chrome-node-4
  depends_on:
    - selenium-hub
  environment:
    - SE_EVENT_BUS_HOST=selenium-hub
    - SE_EVENT_BUS_PUBLISH_PORT=4442
    - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
    - SE_NODE_MAX_SESSIONS=2
    - SE_NODE_SESSION_TIMEOUT=300
    - NODE_MAX_INSTANCES=2
  ports:
    - "7905:7900"
  shm_size: 2gb
  networks:
    - selenium-network
```

### Memory Configuration

```yaml
# Increase shared memory for each node (reduce crashes)
shm_size: 4gb  # Increase from 2gb

# In pom.xml or via Maven properties
-Xmx2G  # Increase JVM heap size
```

## Troubleshooting

### Issue: Connection refused to localhost:4444

**Solution**:
```bash
# Check if containers are running
docker-compose -f docker/docker-compose.yml ps

# Check logs
docker-compose -f docker/docker-compose.yml logs selenium-hub

# Ensure ports are not in use
netstat -tuln | grep 4444

# Start containers again
docker-compose -f docker/docker-compose.yml restart
```

### Issue: Tests timeout or hang

**Solution**:
```bash
# Increase session timeout in docker-compose.yml
- SE_NODE_SESSION_TIMEOUT=600  # Increase from 300

# Check node resource usage
docker stats

# Increase shm_size if memory-related
shm_size: 4gb
```

### Issue: Port already in use

**Solution**:
```bash
# Find what's using the port
lsof -i :4444

# Or kill existing containers
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)

# Start fresh
docker-compose -f docker/docker-compose.yml up -d
```

### Issue: Firefox nodes not working

**Solution**: Firefox nodes are optional. If not needed, comment them out in docker-compose.yml or increase Chrome nodes instead.

## Integration with CI/CD

### Jenkins Example
```groovy
pipeline {
    agent any
    stages {
        stage('Start Docker Grid') {
            steps {
                sh 'docker-compose -f docker/docker-compose.yml up -d'
                sh 'sleep 10'  // Wait for grid to be ready
            }
        }
        stage('Run Parallel Tests') {
            steps {
                sh 'mvn clean test -Dsuites=testng-docker-parallel.xml'
            }
        }
        stage('Generate Report') {
            steps {
                sh 'mvn allure:report'
            }
        }
    }
    post {
        always {
            sh 'docker-compose -f docker/docker-compose.yml down -v'
        }
    }
}
```

### GitHub Actions Example
```yaml
name: Parallel Tests on Docker

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up Docker
        run: |
          docker-compose -f docker/docker-compose.yml up -d
          sleep 10
      
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      
      - name: Run parallel tests
        run: mvn clean test -Dsuites=testng-docker-parallel.xml
      
      - name: Cleanup
        run: docker-compose -f docker/docker-compose.yml down -v
```

## Best Practices

1. **Always wait for Grid to be ready** before running tests (5-10 seconds)
2. **Use meaningful test method names** for easier debugging
3. **Implement proper test isolation** to avoid cross-test dependencies
4. **Monitor resource usage** with `docker stats`
5. **Use descriptive thread names** for better logging
6. **Separate test data** when running tests in parallel
7. **Implement proper cleanup** in @AfterMethod to avoid resource leaks
8. **Use TestNG listeners** for better reporting and synchronization

## Advanced Configuration

### Custom Selenium Hub Options

Edit `docker-compose.yml` environment for selenium-hub:

```yaml
selenium-hub:
  environment:
    - SE_SESSION_REQUEST_TIMEOUT=600
    - SE_SESSION_RETRY_INTERVAL=5
    - SE_MAX_SESSION=10
    - SE_BROWSER_SESSION_TIMEOUT=300
```

### Node-level Customization

```yaml
chrome-node-1:
  environment:
    - SE_VNC_NO_SOCKET=true
    - SE_DISABLE_TRACING=false
    - SE_NEW_SESSION_WAIT_TIMEOUT=30000
```

## Performance Metrics

With the current setup:
- **Expected Throughput**: 10 concurrent test sessions
- **Average Test Duration**: 5-10 minutes for full suite
- **Grid Startup Time**: ~15-20 seconds
- **Typical Speed-up**: 3-5x faster compared to sequential execution

## Cleanup and Shutdown

```bash
# Graceful shutdown
docker-compose -f docker/docker-compose.yml down

# Complete cleanup (remove volumes and images)
docker-compose -f docker/docker-compose.yml down -v
docker image rm $(docker images 'selenium/*' -q)
```

## Support and Additional Resources

- Selenium Grid Documentation: https://www.selenium.dev/documentation/grid/
- TestNG Parallel Execution: https://testng.org/doc/documentation-main.html#parallel-tests
- Docker Documentation: https://docs.docker.com/

