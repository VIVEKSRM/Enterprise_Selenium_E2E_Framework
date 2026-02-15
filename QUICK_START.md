# Quick Start Guide: Docker Parallel Execution

## 5-Minute Setup

### Prerequisites Check
```bash
docker --version          # Should be 20.10+
docker-compose --version  # Should be 1.29+
mvn --version            # Should be 3.6+
java -version            # Should be 17+
```

### Step 1: Start Selenium Grid (Windows PowerShell)
```powershell
# Navigate to project directory
cd D:\Learning_Code_Framework\Selenium-Automation-Framework_New-2026\Enterprise_Selenium_E2E_Framework

# Start all Docker containers
.\docker-parallel.ps1 -Command start
```

### Step 1: Start Selenium Grid (Linux/Mac Bash)
```bash
cd ./Enterprise_Selenium_E2E_Framework
chmod +x docker-parallel.sh
./docker-parallel.sh start
```

### Step 2: Verify Setup
```bash
# Windows PowerShell
.\docker-parallel.ps1 -Command status

# Linux/Mac
./docker-parallel.sh status
```

Expected output: All services showing "Up"

### Step 3: Run Tests in Parallel
```bash
# Windows PowerShell - Test-level parallelization (5 threads)
.\docker-parallel.ps1 -Command test

# Linux/Mac - Test-level parallelization (5 threads)
./docker-parallel.sh test
```

Or for class-level parallelization (10 threads):
```bash
# Windows PowerShell
.\docker-parallel.ps1 -Command test-all

# Linux/Mac
./docker-parallel.sh test-all
```

### Step 4: Monitor Tests (Optional)

Open browser tabs to watch tests execute:
- **Selenium Grid Console**: http://localhost:4444
- **Chrome Node 1**: localhost:7900 (VNC, password: `secret`)
- **Chrome Node 2**: localhost:7901 (VNC)
- **Chrome Node 3**: localhost:7902 (VNC)

### Step 5: Stop Everything
```bash
# Windows PowerShell
.\docker-parallel.ps1 -Command stop

# Linux/Mac
./docker-parallel.sh stop
```

## What Was Set Up

### Docker Infrastructure
- **1 Selenium Hub**: Central dispatcher (port 4444)
- **3 Chrome Nodes**: Parallel execution (ports 7900-7902 for VNC)
- **2 Firefox Nodes**: Parallel execution (ports 7903-7904 for VNC)
- **Total Capacity**: 10 concurrent test sessions

### Framework Updates
1. **DriverFactory.java**: Now supports Docker Selenium Grid mode
2. **config.properties**: Added docker.hub.url configuration
3. **TestNG Suites**: Two parallel execution configurations
   - `testng-docker-parallel.xml` (5 thread count)
   - `testng-docker-all-parallel.xml` (10 thread count)

### Management Scripts
- **docker-parallel.ps1**: PowerShell script for Windows
- **docker-parallel.sh**: Bash script for Linux/Mac

## Common Commands

```bash
# Start Docker
docker-compose -f docker/docker-compose.yml up -d

# Stop Docker
docker-compose -f docker/docker-compose.yml down

# View all services
docker-compose -f docker/docker-compose.yml ps

# View hub logs
docker-compose -f docker/docker-compose.yml logs -f selenium-hub

# Check grid status
curl http://localhost:4444/wd/hub/status

# Run specific test file
mvn clean test -Dsuites=testng-docker-parallel.xml

# Run with custom thread count
mvn clean test -Dsuites=testng-docker-parallel.xml -DthreadCount=8
```

## Expected Performance

**Sequential vs Parallel (Approximate)**
- Sequential execution: 10-15 minutes for full suite
- Parallel (5 threads): 3-5 minutes
- Parallel (10 threads): 2-3 minutes

**Speed-up Factor**: 3-7x faster depending on test design

## Troubleshooting Quick Fixes

### Docker won't start
```bash
# Kill existing containers
docker stop $(docker ps -aq)
docker rm $(docker ps -aq)

# Start fresh
.\docker-parallel.ps1 -Command start  # Windows
./docker-parallel.sh start            # Linux/Mac
```

### Port 4444 already in use
```bash
# Find what's using it
netstat -tuln | grep 4444

# Kill the process or use different port in docker-compose.yml
```

### Tests timeout
```bash
# Check if all nodes are healthy
curl http://localhost:4444/wd/hub/status

# View logs
docker-compose -f docker/docker-compose.yml logs chrome-node-1
```

### Out of memory
```bash
# Increase Docker memory allocation in Docker Desktop settings
# Or reduce concurrent threads in TestNG suite
```

## Next Steps

1. **Customize test suites**: Edit `testng-docker-parallel.xml` to include your specific tests
2. **Adjust thread count**: Modify `thread-count` attribute based on available resources
3. **Add more nodes**: Duplicate node configurations in `docker-compose.yml`
4. **CI/CD Integration**: See `DOCKER_PARALLEL_SETUP.md` for Jenkins/GitHub Actions examples

## Important Files

| File | Purpose |
|------|---------|
| `docker/docker-compose.yml` | Main Selenium Grid configuration |
| `testng-docker-parallel.xml` | Test distribution configuration (5 threads) |
| `testng-docker-all-parallel.xml` | Alternative configuration (10 threads) |
| `src/test/resources/config.properties` | Execution mode configuration |
| `src/main/java/core/driver/DriverFactory.java` | Driver creation logic |
| `docker-parallel.ps1` | Windows management script |
| `docker-parallel.sh` | Linux/Mac management script |

## Getting Help

- Full guide: See `DOCKER_PARALLEL_SETUP.md`
- Selenium Grid docs: https://www.selenium.dev/documentation/grid/
- TestNG docs: https://testng.org/doc/documentation-main.html#parallel-tests
- Docker docs: https://docs.docker.com/

---

**Ready to run fast tests? Start with Step 1 above!**

