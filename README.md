Enterprise Selenium E2E Framework
=================================

Overview
--------
This is a compact, enterprise-grade Selenium WebDriver TestNG framework scaffolded for end-to-end UI automation. It includes thread-safe WebDriver management, an (optional) self-healing layer for locators, ExtentReports integration, screenshot & video support, and placeholders for JIRA/DB integrations.

Key features
- Java 17 + Maven
- Selenium 4
- TestNG test execution (parallel supported via TestNG XML)
- ExtentReports (HTML) reporting
- ThreadLocal WebDriver management via `DriverManager`
- `DriverFactory` that supports LOCAL Chrome (via WebDriverManager) and remote execution (`LAMBDATEST`) via `RemoteWebDriver`
- Self-healing helpers: `SelfHealingDriver` and `LocatorHealer` (basic heuristic starter)
- Video recording via `ffmpeg` (desktop capture)
- Optional `BaseTest` to initialize/tear down drivers in test lifecycle

Project structure (important files)
- `pom.xml` — Maven dependencies and build plugins
- `src/main/java/core/driver/DriverFactory.java` — creates WebDriver instances
- `src/main/java/core/driver/DriverManager.java` — ThreadLocal driver holder
- `src/main/java/core/healing/SelfHealingDriver.java` — find wrapper + heal fallback
- `src/main/java/core/config/ConfigManager.java` — central config loader (properties + system props)
- `src/main/java/core/listeners/TestListener.java` — TestNG listener for reporting, driver init, screenshots, and video start
- `src/main/java/core/reporting/ExtentManager.java` — ExtentReports wiring
- `src/main/java/core/utils/ScreenshotUtil.java` — screenshot helpers
- `src/main/java/core/video/VideoRecorderUtil.java` — ffmpeg-based recording
- `src/test/resources/config.properties` — default configuration
- `src/test/java/tests` — example tests (FrameworkSmokeTest, E2EFeatureTest, etc.)
- `src/test/java/core/base/BaseTest.java` — optional base test class (per-test setup/teardown)

Quick start — prerequisites
- Java 17 JDK installed and JAVA_HOME set
- Maven installed and on PATH
- For LOCAL Chrome execution: no action (WebDriverManager downloads chromedriver automatically)
- To record videos: `ffmpeg` must be installed and available on PATH (optional)

Configuration
- Primary config file: `src/test/resources/config.properties`.
- Values can be overridden with system properties (e.g., `-Dbrowser=CHROME`).

Important config keys (examples)
- `execution.mode` — LOCAL or LAMBDATEST (default: LOCAL)
- `browser` — CHROME (default)
- `jira.enabled` — ON / OFF
- `lambdatest.url` — remote WebDriver URL (include username:accesskey or use env vars)

Security: credentials
- Do NOT commit real credentials to `config.properties`.
- Use CI secrets or environment variables to provide sensitive values.
  - Example: `-Dlambdatest.username=${LT_USERNAME}` and `-Dlambdatest.accessKey=${LT_ACCESS_KEY}`
- `CredentialUtil` reads `src/test/resources/credentials.json` in this scaffold — replace or remove in your pipeline.

Running tests locally
- Run a single test class (TestNG / Surefire):

```bash
mvn "-Dtest=tests.FrameworkSmokeTest" test
```

- Run the TestNG suite file (if you use `testng-parallel.xml`):

```bash
mvn test -Dsurefire.suiteXmlFiles=src/test/resources/testng-parallel.xml
```

(You can also run via your IDE by launching TestNG tests.)

Headless mode
- Add `-Dheadless=true` to run Chrome in headless mode (useful for CI):

```bash
mvn "-Dtest=tests.FrameworkSmokeTest" test -Dheadless=true
```

CI / GitHub Actions (example)
- Minimal example (create `.github/workflows/ci.yml`):

```yaml
name: CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Run tests
        run: mvn -B test -Dheadless=true
        env:
          LT_USERNAME: ${{ secrets.LT_USERNAME }}
          LT_ACCESS_KEY: ${{ secrets.LT_ACCESS_KEY }}
```

If you prefer to run on a cloud provider like LambdaTest, set `execution.mode=LAMBDATEST` and provide a `lambdatest.url` built from secrets.

How driver initialization works (two options)
1) Listener-based (default in this repo)
- `core.listeners.TestListener` initializes the driver at test start using `DriverFactory.createDriver()` and sets it to `DriverManager`.
- It also starts video recording (best-effort) and captures a screenshot on failure.

2) BaseTest-based (optional)
- `src/test/java/core/base/BaseTest.java` demonstrates per-test setup/teardown.
- Use this if you prefer explicit `@BeforeMethod/@AfterMethod` in the test inheritance model instead of listeners.

Extending the framework
- Add more browsers to `DriverFactory` (FIREFOX, EDGE) and use WebDriverManager to install their binaries.
- Improve `LocatorHealer` to use DOM similarity, attribute scoring, or ML-based predictions.
- Implement full `JiraClient` REST integration and secure auth via env vars or secrets.

Troubleshooting
- SLF4J warning (no-op logger): add a binding like `slf4j-simple` or `logback-classic` to `pom.xml` if you want logging output. This warning is non-fatal.
- `ffmpeg` not found: VideoRecorderUtil will log a start failure; install ffmpeg or disable video recording.
- Driver not initialized (null): Ensure either `TestListener` is registered in TestNG XML or use `BaseTest` as a parent class for tests.
- Duplicate or conflicting `execution.mode` values in `config.properties`: Open and keep a single, correct value.

Developer tips
- To run only one TestNG test method from the CLI:

```bash
mvn -Dtest=tests.FrameworkSmokeTest#frameworkSmoke test
```

- To enable remote execution on LambdaTest, provide an URL like:
  `https://<username>:<accessKey>@hub.lambdatest.com/wd/hub` — prefer building this from secrets.

Next steps I can take (pick one)
- Create a short `CONTRIBUTING.md` with coding conventions and commit hooks.
- Implement full JIRA REST integration (needs auth details and API plan).
- Extend `LocatorHealer` with an improved heuristic (DOM attribute scoring) and add unit tests.
- Add a GitHub Actions workflow file to the repo and a sample CI matrix for browsers.

Which README option did you prefer?
- I created a comprehensive README (includes CI snippet). If you'd prefer a shorter quickstart, I can add that as `README-quick.md`.

Run verification
- I will run `mvn test` now to ensure nothing broke after these additions.

```bash
mvn test
```

If you want me to proceed with any of the next steps (CI workflow, JIRA integration, or LocatorHealer improvements), tell me which and I'll implement it and run quick validations.

# Running TestNG suites from repo root

You can now keep TestNG XML suites directly at the project root (next to `pom.xml`). Example files included at repo root:

- `testng-parallel.xml`
- `testng-all-parallel.xml`
- `testng-sequential.xml`
- `testng-smoke.xml`
- `testng-sanity.xml`
- `testng-regression.xml` (default suite)

Quick run examples (from project root where `pom.xml` lives):

Run the default suite (configured in pom.xml):

```bash
mvn test
```

Run a specific suite explicitly (any suite in repo root):

```bash
mvn -Dsurefire.suiteXmlFiles=testng-all-parallel.xml test
mvn -Dsurefire.suiteXmlFiles=testng-sequential.xml test
mvn -Dsurefire.suiteXmlFiles=testng-smoke.xml test
mvn -Dsurefire.suiteXmlFiles=testng-sanity.xml test
mvn -Dsurefire.suiteXmlFiles=testng-regression.xml test
```

Notes about duplicates
- To avoid confusion we moved the canonical copies of the TestNG suites to the project root. The copies under `src/test/resources/` have been replaced with small redirect notes so you don't accidentally use outdated copies.

Why default is `testng-regression.xml`
- `testng-regression.xml` contains the full test set and is a good default for `mvn test` runs; you can change this in `pom.xml` if you prefer a different default.
