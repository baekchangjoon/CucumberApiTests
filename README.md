![Main](https://github.com/baekchangjoon/CucumberApiTests/actions/workflows/maven.yml/badge.svg?branch=main)
[![codecov](https://codecov.io/gh/baekchangjoon/CucumberApiTests/graph/badge.svg?token=23K2OUH1PQ)](https://codecov.io/gh/baekchangjoon/CucumberApiTests)

# Cucumber API Tests

This project is a **Cucumber-based API testing project** that:
- Sets up the test environment (**H2 Database**, **WireMock**),
- Performs API testing (**Cucumber**),
- Analyzes test results (**HtmlParser**).

It is implemented as a **Command Line Interface (CLI) program**.

## Project Structure

```
📂 src/main/java/com/example
├── CucumberApiTests.java        # Main CLI execution file
├── CucumberStarter.java         # Command for executing Cucumber tests
├── HtmlParser.java              # Cucumber HTML result analyzer
├── MemoryDBStarter.java         # H2 DB execution manager
│   ├── H2StartCommand.java      # Command to start H2 DB
│   ├── H2StopCommand.java       # Command to stop H2 DB
├── WireMockStarter.java         # WireMock execution manager
│   ├── WireMockStartCommand.java # Command to start WireMock
│   ├── WireMockStopCommand.java # Command to stop WireMock
```

## Execution Guide

### 1. Running and Stopping H2 Database

H2 Database can be started on multiple ports.

#### (1) Start H2 DB
```sh
java -jar ApiTest.jar MemoryDBStarter start --ports=33306,33307
```
- `--ports` : Ports on which the H2 database will be started (Default: `33306,33307,33308,33309`)

#### (2) Stop H2 DB
```sh
java -jar ApiTest.jar MemoryDBStarter stop
```

### 2. Running and Stopping WireMock

WireMock runs an API mocking server.

#### (1) Start WireMock
```sh
java -jar ApiTest.jar WireMockStarter start --config=wiremock-config.json --dburl=jdbc:h2:mem:mockdb
```
- `--config` : Path to the WireMock configuration file (Required)
- `--dburl` : Database URL used by WireMock (Required)
- `--dbusername` : Database username (Default: `sa`)
- `--dbpassword` : Database password (Default: `1234`)

#### (2) Stop WireMock
```sh
java -jar ApiTest.jar WireMockStarter stop
```

### 3. Running Cucumber API Tests

⚠️ **This feature is incomplete.**  
An API server to be tested is required. Currently, only sample feature files exist, and their execution will result in failures.

To run Cucumber tests, use the following command:

```sh
java -jar ApiTest.jar CucumberStarter --target-db-url=jdbc:h2:mem:testdb \
                                    --target-db-username=sa \
                                    --target-db-password=1234 \
                                    --test-db-url=jdbc:h2:mem:cucumberdb \
                                    --test-db-username=sa \
                                    --test-db-password=1234
```

Options:
- `--target-db-url` : Target service's database URL (Default: environment variable `TARGET_DB_URL`)
- `--target-db-username` : Target database username (Default: `sa`)
- `--target-db-password` : Target database password (Default: `1234`)
- `--test-db-url` : Cucumber test database URL (Default: environment variable `TEST_DB_URL`)
- `--test-db-username` : Test database username (Default: `sa`)
- `--test-db-password` : Test database password (Default: `1234`)

### 4. Analyzing Cucumber HTML Results

You can analyze the Cucumber test execution results and generate reports.

```sh
java -jar ApiTest.jar HtmlParser -f path/to/cucumber.html --csv --html
```
- `-f` : Path to the Cucumber HTML result file (Required)
- `--csv` : Save results in CSV format
- `--html` : Save results in HTML format

---

## Technology Stack

- **Java 11+**
- **PicoCLI** (CLI support)
- **Cucumber** (API test automation)
- **H2 Database** (In-memory database)
- **WireMock** (API mocking server)
- **HTML Parser** (Test result analysis)

---

This project is designed to automate API testing and simplify environment setup.  
For any questions, please create an [issue](https://github.com/baekchangjoon/CucumberApiTests/issues).