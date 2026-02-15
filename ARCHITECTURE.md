# Docker Parallel Execution Architecture

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Your Test Machine                       │
│                    (Windows/Linux/Mac)                      │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  Docker Network                             │
│              (selenium-network bridge)                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │          Selenium Hub (Port 4444)                   │  │
│  │  - Receives test requests                           │  │
│  │  - Distributes to available nodes                   │  │
│  │  - Manages session pool                             │  │
│  │  - Tracks node capabilities                         │  │
│  └──────────────────────────────────────────────────────┘  │
│         ▲                    ▲                    ▲          │
│         │                    │                    │          │
│    ┌────┴─────┐    ┌────────┴────┐    ┌─────────┴─────┐  │
│    │           │    │             │    │               │  │
│    ▼           ▼    ▼             ▼    ▼               ▼  │
│ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐   │
│ │Chrome  │ │Chrome  │ │Chrome  │ │Firefox │ │Firefox │   │
│ │ Node 1 │ │ Node 2 │ │ Node 3 │ │ Node 1 │ │ Node 2 │   │
│ │        │ │        │ │        │ │        │ │        │   │
│ │VNC:    │ │VNC:    │ │VNC:    │ │VNC:    │ │VNC:    │   │
│ │7900    │ │7901    │ │7902    │ │7903    │ │7904    │   │
│ └────────┘ └────────┘ └────────┘ └────────┘ └────────┘   │
│                                                             │
│  ▲ Each node supports 2 concurrent sessions               │
│  ▲ Total capacity: 10 concurrent browser instances        │
│  ▲ Each has 2GB shared memory for stability               │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                           ▲
                           │
          ┌────────────────┴────────────────┐
          │                                 │
          ▼                                 ▼
  ┌──────────────┐              ┌──────────────────┐
  │ Maven Test   │              │ Management       │
  │ Execution    │              │ Scripts          │
  │              │              │                  │
  │ mvn clean    │              │ docker-parallel  │
  │ test         │              │ (.ps1 / .sh)     │
  └──────────────┘              └──────────────────┘
```

## Test Execution Flow

```
1. START TEST SUITE
        ▼
2. Read config.properties
   (execution.mode=DOCKER)
        ▼
3. DriverFactory.createDriver()
        ▼
4. Connect to Grid Hub
   (http://localhost:4444)
        ▼
5. Hub finds available node
        ▼
6. Browser session created
   on selected node
        ▼
7. Test executes in parallel
   with other tests
        ▼
8. Browser session closed
   on node
        ▼
9. REPEAT for next test
        ▼
10. All threads complete
        ▼
11. Generate reports
```

## Thread Distribution Pattern

```
TestNG Suite Configuration
(testng-docker-parallel.xml)

<suite parallel="tests" thread-count="5">

  ┌─────────────────────────────────────────┐
  │       Test 1 ─────► Thread 1            │
  │       Test 2 ─────► Thread 2            │
  │       Test 3 ─────► Thread 3   ─────┐   │
  │       Test 4 ─────► Thread 4        │   │
  │       Test 5 ─────► Thread 5        │   │
  │                                     │   │
  │   All 5 tests run at the same time  │   │
  │   Each thread gets its own          │   │
  │   WebDriver instance (ThreadLocal)  │   │
  └─────────────────────────────────────────┘
           │
           ▼
    Request Session
         ▼
    Selenium Grid Hub
         ▼
    ┌─────────────────────────┐
    │ Chrome Node 1 ─ 2 slots │
    │ Chrome Node 2 ─ 2 slots │
    │ Chrome Node 3 ─ 2 slots │
    │ Firefox Node 1─ 2 slots │
    │ Firefox Node 2─ 2 slots │
    └─────────────────────────┘
         (10 slots total)
         
    Distribution:
    Thread 1 → Chrome Node 1 (slot 1)
    Thread 2 → Chrome Node 1 (slot 2)
    Thread 3 → Chrome Node 2 (slot 1)
    Thread 4 → Chrome Node 2 (slot 2)
    Thread 5 → Chrome Node 3 (slot 1)
```

## Configuration Files Relationship

```
config.properties
       ▼
   ┌───────────────────┐
   │ execution.mode=   │
   │   DOCKER          │
   │ docker.hub.url=   │
   │   http://...      │
   │ browser=CHROME    │
   └───────────────────┘
           ▼
  DriverFactory.createDriver()
           ▼
      ┌─────────────┐
      │ Docker Mode?│
      └──────┬──────┘
             │
             ▼
  RemoteWebDriver(gridUrl)
             ▼
  Selenium Grid (localhost:4444)
             ▼
       Available Node
             ▼
     Browser Session Created
```

## Data Flow for Parallel Tests

```
┌─────────────────────────────────────────────────────────────┐
│                   Test Execution                            │
│              (5 threads running in parallel)                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ @BeforeMethod (per thread)                                 │
│      │                                                      │
│      ├─ Read config.properties                             │
│      ├─ Create DriverFactory                               │
│      └─ Get WebDriver via DriverManager.getDriver()        │
│             (stored in ThreadLocal)                        │
│                                                             │
│ Test Method (per thread)                                   │
│      │                                                      │
│      ├─ Interact with driver                               │
│      ├─ Assert results                                     │
│      └─ Call ScreenshotUtil if needed                      │
│                                                             │
│ @AfterMethod (per thread)                                  │
│      │                                                      │
│      ├─ Call VideoRecorderUtil.stop()                      │
│      └─ DriverManager.quitDriver()                         │
│             (removes from ThreadLocal)                     │
│                                                             │
│ ALL THREADS COMPLETE → Generate Report                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## Node Health Status

```
Selenium Hub - Status Dashboard
http://localhost:4444

Grid Status
├── Total Sessions: 5/10 available
│
├── Chrome Node 1 (Ready)
│   ├── Max Sessions: 2
│   ├── Active: 2
│   ├── Available: 0
│   └── VNC: localhost:7900
│
├── Chrome Node 2 (Ready)
│   ├── Max Sessions: 2
│   ├── Active: 2
│   ├── Available: 0
│   └── VNC: localhost:7901
│
├── Chrome Node 3 (Ready)
│   ├── Max Sessions: 2
│   ├── Active: 1
│   ├── Available: 1
│   └── VNC: localhost:7902
│
├── Firefox Node 1 (Ready)
│   ├── Max Sessions: 2
│   ├── Active: 0
│   ├── Available: 2
│   └── VNC: localhost:7903
│
└── Firefox Node 2 (Ready)
    ├── Max Sessions: 2
    ├── Active: 0
    ├── Available: 2
    └── VNC: localhost:7904

Current Load: 5 sessions running on 10 available
Utilization: 50%
```

## Docker Container Lifecycle

```
1. START CONTAINERS
   docker-compose up -d
        ▼
2. INITIALIZATION
   Hub starts ─────┐
                  ├─► All nodes register
   Chrome 1 starts┤
   Chrome 2 starts┤
   Chrome 3 starts├─► Ready to receive tests
   Firefox 1 ─────┤
   Firefox 2 ─────┘
        ▼
3. READY STATE
   ├─ Hub listening on :4444
   ├─ All nodes connected
   ├─ VNC servers active
   └─ Ready for test requests
        ▼
4. EXECUTION
   Tests request sessions
        ▼
5. TEARDOWN
   Tests complete
        ▼
6. STOP CONTAINERS
   docker-compose down
        ▼
7. CLEANUP
   Volumes removed
   Containers stopped
```

## Performance Timeline

```
SEQUENTIAL EXECUTION
├─ Test 1: 3 min
├─ Test 2: 3 min
├─ Test 3: 2 min
├─ Test 4: 2 min
└─ Test 5: 2 min
   ────────────────── TOTAL: 12 minutes ─────

PARALLEL EXECUTION (5 threads)
├─ Test 1 ─┐
├─ Test 2 ─┤
├─ Test 3 ─├─ CONCURRENT ─ 3 minutes
├─ Test 4 ─┤
└─ Test 5 ─┘
   ────── TOTAL: 3 minutes ─────

IMPROVEMENT: 4x faster (12 min → 3 min)
```

## Port Mapping Overview

```
Local Machine          Docker Container
─────────────         ─────────────────

4444 ◄────────────────► 4444 (Selenium Hub)
4442 ◄────────────────► 4442 (Event Bus Pub)
4443 ◄────────────────► 4443 (Event Bus Sub)

7900 ◄────────────────► 7900 (Chrome Node 1 VNC)
7901 ◄────────────────► 7900 (Chrome Node 2 VNC)
7902 ◄────────────────► 7900 (Chrome Node 3 VNC)
7903 ◄────────────────► 7900 (Firefox Node 1 VNC)
7904 ◄────────────────► 7900 (Firefox Node 2 VNC)

All traffic isolated in docker bridge network
```

## ThreadLocal Driver Management

```
┌──────────────────────────────────────────────┐
│          ThreadLocal<WebDriver>             │
├──────────────────────────────────────────────┤
│                                              │
│  Thread 1 ─────► WebDriver Instance 1        │
│  Thread 2 ─────► WebDriver Instance 2        │
│  Thread 3 ─────► WebDriver Instance 3        │
│  Thread 4 ─────► WebDriver Instance 4        │
│  Thread 5 ─────► WebDriver Instance 5        │
│                                              │
│  No cross-thread interference               │
│  Each thread has isolated driver             │
│  Automatic cleanup via DriverManager         │
│                                              │
└──────────────────────────────────────────────┘
```

## Fallback Mechanism

```
Application Startup
        ▼
DriverFactory.createDriver()
        ▼
Is execution.mode == "DOCKER"?
   ├─ YES ─► Try connecting to docker.hub.url
   │           ├─ SUCCESS ─► Use RemoteWebDriver
   │           │
   │           └─ FAILURE ─► Log warning
   │                │
   │                └─► Fall back to LOCAL
   │
   └─ NO ──► Check other modes (LAMBDATEST, etc.)
   │           ├─ SUCCESS ─► Use appropriate driver
   │           │
   │           └─ FAILURE ─► Fall back to LOCAL
   │
   └─ FALLBACK ─► Use LocalWebDriver (WebDriverManager)
                  ├─ WebDriverManager.chromedriver()
                  └─ new ChromeDriver(options)
```

## Summary

```
YOUR FRAMEWORK SETUP
───────────────────

                    Docker Containers
                    ────────────────
                    1 Hub
                    3 Chrome Nodes
                    2 Firefox Nodes
                    ─────────────────
                    10 Parallel Slots

              ▲
              │ (via HTTP/JSON Wire Protocol)
              │
        Test Execution
        ────────────
        5 Threads
        TestNG Parallel
        Suite Configuration

              ▲
              │ (uses config.properties)
              │
        DriverFactory
        ─────────────
        Docker Mode Enabled
        Graceful Fallback
        Cross-Platform Support
```

---

**This architecture enables high-speed parallel test execution while maintaining test isolation and stability.**

