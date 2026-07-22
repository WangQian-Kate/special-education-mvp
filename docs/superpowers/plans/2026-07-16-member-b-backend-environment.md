# Member B Backend Environment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Configure the approved Spring Boot development environment and produce a tested minimal backend exposing `GET /health`.

**Architecture:** A Maven-built Spring Boot 4.1.0 application uses a conventional controller/service/mapper/entity structure. MyBatis 4.0.0 provides the database access layer, MySQL 8.0 provides persistence, and secrets enter through environment variables rather than version control.

**Tech Stack:** Windows 11, Oracle JDK 25.0.2, Apache Maven 3.9.16, Spring Boot 4.1.0, MyBatis Spring Boot Starter 4.0.0, MySQL 8.0.45, JUnit/MockMvc, Apifox, Git 2.55.0.

## Global Constraints

- Keep the existing JDK 25 and `JAVA_HOME=C:\develop-java\JDK`; install JDK 21 only if a verified build incompatibility occurs.
- Install Maven 3.9.16 under `C:\Users\Asus\Documents\Codex\tools\apache-maven-3.9.16` and add its `bin` directory to the user PATH.
- Replace the stale MySQL PATH entry with `C:\Program Files\MySQL\MySQL Server 8.0\bin`.
- Do not reset the MySQL root password and do not expose any database password in Git or chat output.
- Keep the `MySQL80` Windows service set to Manual; start it only for development.
- Use lowerCamelCase in Java/JSON and snake_case in MySQL.
- Do not add Docker, Redis, Spring Security, Lombok, MyBatis-Plus, Flyway, cloud deployment, tunneling, WeChat login, or AI SDKs.
- Default the backend to localhost. Do not add firewall rules or LAN/public listeners in this plan.

---

### Task 1: Initialize Git and preserve the approved design

**Files:**
- Existing: `docs/superpowers/specs/2026-07-16-member-b-backend-environment-design.md`
- Existing: `docs/superpowers/plans/2026-07-16-member-b-backend-environment.md`
- Create: `.git/`

**Interfaces:**
- Consumes: the user-approved design document.
- Produces: a Git repository with `main` as the initial branch and a `develop` branch for integration.

- [ ] **Step 1: Configure the default branch and initialize the repository**

```powershell
git config --global init.defaultBranch main
git init -b main
```

Expected: `Initialized empty Git repository` and `git branch --show-current` prints `main`.

- [ ] **Step 2: Commit the design and implementation plan**

```powershell
git add docs/superpowers/specs/2026-07-16-member-b-backend-environment-design.md docs/superpowers/plans/2026-07-16-member-b-backend-environment.md
git commit -m "docs: define member B backend environment"
```

Expected: one root commit containing both documents.

- [ ] **Step 3: Create the integration branch and return to main**

```powershell
git branch develop
git branch --list
```

Expected: both `main` and `develop` are listed, with `main` selected.

---

### Task 2: Install and verify Maven 3.9.16

**Files:**
- Download: `work/downloads/apache-maven-3.9.16-bin.zip`
- Download: `work/downloads/apache-maven-3.9.16-bin.zip.sha512`
- Create: `C:\Users\Asus\Documents\Codex\tools\apache-maven-3.9.16\`
- Modify: user environment variables `MAVEN_HOME` and `Path`.

**Interfaces:**
- Consumes: existing JDK 25 through `JAVA_HOME`.
- Produces: `mvn` available from a fresh shell and using JDK 25.

- [ ] **Step 1: Download the Maven binary and official checksum**

```powershell
New-Item -ItemType Directory -Force work\downloads | Out-Null
curl.exe -L --fail https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip -o work\downloads\apache-maven-3.9.16-bin.zip
curl.exe -L --fail https://dlcdn.apache.org/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip.sha512 -o work\downloads\apache-maven-3.9.16-bin.zip.sha512
```

Expected: both downloads complete without an HTTP error.

- [ ] **Step 2: Verify the SHA-512 checksum**

```powershell
$expected=(Get-Content work\downloads\apache-maven-3.9.16-bin.zip.sha512 -Raw).Trim().Split()[0].ToUpperInvariant()
$actual=(Get-FileHash work\downloads\apache-maven-3.9.16-bin.zip -Algorithm SHA512).Hash
if($actual -ne $expected){ throw "Maven SHA-512 mismatch" }
```

Expected: exit code 0 and no mismatch exception.

- [ ] **Step 3: Extract Maven to the shared Codex tools directory**

```powershell
Expand-Archive -LiteralPath work\downloads\apache-maven-3.9.16-bin.zip -DestinationPath C:\Users\Asus\Documents\Codex\tools -Force
```

Expected: `C:\Users\Asus\Documents\Codex\tools\apache-maven-3.9.16\bin\mvn.cmd` exists.

- [ ] **Step 4: Set user environment variables without duplicating PATH entries**

```powershell
$mavenHome='C:\Users\Asus\Documents\Codex\tools\apache-maven-3.9.16'
[Environment]::SetEnvironmentVariable('MAVEN_HOME',$mavenHome,'User')
$userPath=[Environment]::GetEnvironmentVariable('Path','User')
$entries=@($userPath -split ';' | Where-Object { $_ -and $_ -ne "$mavenHome\bin" })
[Environment]::SetEnvironmentVariable('Path',(($entries + "$mavenHome\bin") -join ';'),'User')
```

Expected: user `MAVEN_HOME` equals the installation directory and user PATH contains its `bin` once.

- [ ] **Step 5: Verify Maven and Java together**

```powershell
& 'C:\Users\Asus\Documents\Codex\tools\apache-maven-3.9.16\bin\mvn.cmd' --version
```

Expected: Apache Maven 3.9.16 and Java version 25.0.2.

---

### Task 3: Repair MySQL command access and start the development service

**Files:**
- Modify: user PATH.
- Existing executable: `C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`.
- Existing service: `MySQL80`.

**Interfaces:**
- Consumes: the existing MySQL Server installation and existing root credential.
- Produces: `mysql` available from a fresh shell, `MySQL80` running, and TCP 3306 listening.

- [ ] **Step 1: Replace the stale MySQL PATH entry**

```powershell
$mysqlBin='C:\Program Files\MySQL\MySQL Server 8.0\bin'
$userPath=[Environment]::GetEnvironmentVariable('Path','User')
$entries=@($userPath -split ';' | Where-Object { $_ -and $_ -ne 'C:\MySQL\MySQL_Server 8.0\bin' -and $_ -ne $mysqlBin })
[Environment]::SetEnvironmentVariable('Path',(($entries + $mysqlBin) -join ';'),'User')
```

Expected: the stale path is absent and the real MySQL `bin` path appears exactly once.

- [ ] **Step 2: Start the existing MySQL service without changing its start type**

```powershell
Start-Service MySQL80
Get-Service MySQL80 | Select-Object Name,Status,StartType
```

Expected: `Status` is `Running` and `StartType` remains `Manual`.

- [ ] **Step 3: Verify the executable and listening port**

```powershell
& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' --version
Get-NetTCPConnection -LocalPort 3306 -State Listen
```

Expected: MySQL 8.0.45 and at least one listener on TCP 3306.

- [ ] **Step 4: Verify credentials without exposing them**

Open the existing saved local connection in MySQL Workbench. Run exactly:

```sql
SELECT VERSION() AS mysql_version, CURRENT_USER() AS authenticated_user;
```

Expected: one result row. If Workbench requests a password that is not known, stop database credential work and report the blocker; do not reset the password.

---

### Task 4: Install and verify Apifox

**Files:**
- Install: current Windows x64 Apifox release from `https://apifox.com/`.

**Interfaces:**
- Consumes: the official Apifox Windows installer.
- Produces: an installed Apifox desktop client that can create a local project and send HTTP requests.

- [ ] **Step 1: Download the Windows installer from the official Apifox site**

Open `https://apifox.com/`, select the Windows x64 desktop download, and retain the installer in the system Downloads folder.

Expected: a signed Windows installer downloaded from the official Apifox domain.

- [ ] **Step 2: Verify the installer signature and install for the current user**

Use Windows file properties to confirm the digital signature is valid and names Apifox or its official publisher, then run the installer with default current-user settings.

Expected: Apifox appears in Installed Apps and starts successfully.

- [ ] **Step 3: Create a local API project**

Create a project named `special-ed-assistant`, set the local environment base URL to `http://localhost:8080`, and do not enable cloud sharing.

Expected: a local project with the base URL saved.

---

### Task 5: Create the minimal Spring Boot project scaffold

**Files:**
- Create: `.gitignore`
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/specialed/assistant/AssistantApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-dev.yml`
- Create: `backend/README.md`
- Create directories: `backend/src/main/java/com/specialed/assistant/{controller,service,mapper,entity,dto,config,exception}`
- Create directories: `backend/src/main/resources/mapper`, `backend/sql`

**Interfaces:**
- Consumes: Maven 3.9.16, JDK 25, MySQL 8.0.
- Produces: a compilable Spring Boot application scaffold; no `/health` endpoint yet.

- [ ] **Step 1: Add repository ignore rules**

```gitignore
.idea/
*.iml
target/
*.log
.env
application-local.yml
work/
```

- [ ] **Step 2: Create `backend/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.0</version>
        <relativePath/>
    </parent>
    <groupId>com.specialed</groupId>
    <artifactId>assistant-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>assistant-backend</name>
    <description>Backend for the special education assistant MVP</description>
    <properties>
        <java.version>25</java.version>
        <mybatis-spring-boot.version>4.0.0</mybatis-spring-boot.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>${mybatis-spring-boot.version}</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Create the application entry point**

```java
package com.specialed.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AssistantApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssistantApplication.class, args);
    }
}
```

- [ ] **Step 4: Create common and development configuration**

`backend/src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: assistant-backend
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
server:
  address: 127.0.0.1
  port: ${SERVER_PORT:8080}
```

`backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/special_ed_assistant?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai}
    username: ${DB_USERNAME:special_ed_app}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

- [ ] **Step 5: Add exact local startup instructions to `backend/README.md`**

```markdown
# Assistant Backend

## Prerequisites

- JDK 25
- Maven 3.9.16
- MySQL 8.0

## Required environment variables

Set `DB_PASSWORD` to the password of the local `special_ed_app` MySQL user. Optional variables are `DB_URL`, `DB_USERNAME`, `SPRING_PROFILES_ACTIVE`, and `SERVER_PORT`.

## Verify

Run `mvn test`, then `mvn spring-boot:run`. The health endpoint is `GET http://localhost:8080/health`.
```

- [ ] **Step 6: Compile the scaffold and generate Maven Wrapper**

```powershell
Set-Location backend
mvn -DskipTests compile
mvn wrapper:wrapper -Dmaven=3.9.16
```

Expected: build success and `mvnw.cmd` exists.

- [ ] **Step 7: Commit the scaffold**

```powershell
git add .gitignore backend
git commit -m "build: initialize Spring Boot backend"
```

---

### Task 6: Implement `GET /health` with test-driven development

**Files:**
- Create: `backend/src/test/java/com/specialed/assistant/controller/HealthControllerTest.java`
- Create after RED: `backend/src/main/java/com/specialed/assistant/controller/HealthController.java`

**Interfaces:**
- Consumes: the Spring Boot web scaffold from Task 5.
- Produces: `GET /health` returning HTTP 200 and JSON `{"status":"UP"}`.

- [ ] **Step 1: Write the failing MockMvc test before the controller exists**

```java
package com.specialed.assistant.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class HealthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsUpStatus() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
```

- [ ] **Step 2: Run the focused test and verify RED**

```powershell
Set-Location backend
.\mvnw.cmd -Dtest=HealthControllerTest test
```

Expected: test failure because `/health` returns 404 rather than 200.

- [ ] **Step 3: Add the minimal controller**

```java
package com.specialed.assistant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

```powershell
.\mvnw.cmd -Dtest=HealthControllerTest test
```

Expected: one test passes with zero failures and zero errors.

- [ ] **Step 5: Run the complete test suite**

```powershell
.\mvnw.cmd test
```

Expected: build success with all tests passing.

- [ ] **Step 6: Commit the tested endpoint**

```powershell
git add src/main/java/com/specialed/assistant/controller/HealthController.java src/test/java/com/specialed/assistant/controller/HealthControllerTest.java
git commit -m "feat: add health endpoint"
```

---

### Task 7: Create local database assets and verify the running system

**Files:**
- Create: `backend/sql/schema.sql`
- Create: `backend/sql/seed.sql`
- Modify: local user environment variable `DB_PASSWORD`; never write its value to a file or Git.

**Interfaces:**
- Consumes: a working MySQL Workbench connection and the tested Spring Boot project.
- Produces: database `special_ed_assistant`, application account `special_ed_app`, a running backend, and an Apifox health-check result.

- [ ] **Step 1: Add the versioned database schema file**

```sql
CREATE DATABASE IF NOT EXISTS special_ed_assistant
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

- [ ] **Step 2: Add the deterministic seed file**

```sql
USE special_ed_assistant;
SELECT 'seed-ready' AS status;
```

- [ ] **Step 3: Create the application user from MySQL Workbench without exposing its password**

In MySQL Workbench, open **Administration → Users and Privileges → Add Account**. Set Login Name to `special_ed_app`, Limit to Hosts Matching to `localhost`, Authentication Type to `Standard`, and enter a new strong local-only password directly into both password boxes. Under **Schema Privileges**, add `special_ed_assistant` and grant `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE`, `ALTER`, `INDEX`, and `REFERENCES`; then click **Apply**.

Expected: Workbench shows the saved `special_ed_app@localhost` account and its schema privileges. Do not paste the real password into chat, terminal history, SQL files, or Git.

- [ ] **Step 4: Save the same secret as a user environment variable**

In a local PowerShell prompt, assign the secret interactively and set it without printing it:

```powershell
$secret=Read-Host 'DB_PASSWORD' -AsSecureString
$plain=[System.Net.NetworkCredential]::new('', $secret).Password
[Environment]::SetEnvironmentVariable('DB_PASSWORD',$plain,'User')
$plain=$null
```

Expected: user `DB_PASSWORD` exists; its value is not displayed.

- [ ] **Step 5: Run the application with the current-process secret**

```powershell
$env:DB_PASSWORD=[Environment]::GetEnvironmentVariable('DB_PASSWORD','User')
Set-Location backend
.\mvnw.cmd spring-boot:run
```

Expected: embedded server starts on `127.0.0.1:8080` without datasource errors.

- [ ] **Step 6: Verify the endpoint independently**

```powershell
$response=Invoke-RestMethod http://127.0.0.1:8080/health
if($response.status -ne 'UP'){ throw 'Health endpoint did not return UP' }
```

Expected: exit code 0.

- [ ] **Step 7: Verify through Apifox**

Create `GET /health` under the local Apifox project, send the request to `{{baseUrl}}/health`, and save an assertion that HTTP status equals 200 and JSON path `$.status` equals `UP`.

Expected: both assertions pass.

- [ ] **Step 8: Commit database assets and verify repository hygiene**

```powershell
git add backend/sql/schema.sql backend/sql/seed.sql
git commit -m "db: add initial database scripts"
git status --short
git grep -n -I -E "DB_PASSWORD=|IDENTIFIED BY '[^']+'|jdbc:mysql://.*:[^/]+@" -- . ':!docs/superpowers/plans/*'
```

Expected: clean working tree and no secret matches.

---

### Task 8: Final verification

**Files:**
- Verify only; no planned file changes.

**Interfaces:**
- Consumes: all previous tasks.
- Produces: evidence that the machine and project satisfy the approved design.

- [ ] **Step 1: Open a fresh shell and verify tool versions**

```powershell
java --version
mvn --version
git --version
mysql --version
```

Expected: Java 25.0.2, Maven 3.9.16, Git 2.55.0, and MySQL 8.0.45.

- [ ] **Step 2: Verify MySQL without changing service configuration**

```powershell
Get-Service MySQL80 | Select-Object Name,Status,StartType
Get-NetTCPConnection -LocalPort 3306 -State Listen
```

Expected: Running, Manual, and a TCP 3306 listener.

- [ ] **Step 3: Run a clean project verification**

```powershell
Set-Location backend
.\mvnw.cmd clean test
```

Expected: build success, zero test failures, zero test errors.

- [ ] **Step 4: Verify Git structure and cleanliness**

```powershell
git branch --list
git log --oneline --decorate -5
git status --short
```

Expected: `main` and `develop` exist, the environment/scaffold/health/database commits are present, and the working tree is clean.
