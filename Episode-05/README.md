# Episode 5: Advanced CI & DevSecOps

## 🎯 Goal
Make your CI pipeline FAST (caching, parallel builds) and SECURE (security scanning).
Like adding turbo boost AND a security alarm to your car.

---

## 📚 Topics Covered

### 1. Caching (Make Builds FAST)

**Problem Without Caching:**
```
Every build downloads ALL dependencies from scratch:

Build 1: Download 500MB of dependencies (5 minutes)
Build 2: Download 500MB of dependencies (5 minutes) ← SAME files!
Build 3: Download 500MB of dependencies (5 minutes) ← SAME files!

Total wasted time: 10 minutes per day × 365 days = 60+ HOURS/year wasted!
```

**Solution With Caching:**
```
Build 1: Download 500MB of dependencies (5 minutes) → Save to cache
Build 2: Load from cache (10 seconds) ✅
Build 3: Load from cache (10 seconds) ✅

Saved: 60+ hours per year!
```

---

#### Maven Cache

```yaml
- step:
    type: RestoreCacheS3
    name: Restore Maven Cache
    identifier: restore_cache
    spec:
      connectorRef: aws_connector
      region: us-east-1
      bucket: my-build-cache
      key: maven-{{ checksum "pom.xml" }}
      archiveFormat: Tar
      failIfKeyNotFound: false

# ... your build steps ...

- step:
    type: SaveCacheS3
    name: Save Maven Cache
    identifier: save_cache
    spec:
      connectorRef: aws_connector
      region: us-east-1
      bucket: my-build-cache
      key: maven-{{ checksum "pom.xml" }}
      sourcePaths:
        - /root/.m2/repository
      archiveFormat: Tar
```

#### npm Cache

```yaml
key: npm-{{ checksum "package-lock.json" }}
sourcePaths:
  - node_modules
```

#### Gradle Cache

```yaml
key: gradle-{{ checksum "build.gradle" }}
sourcePaths:
  - /root/.gradle/caches
```

#### Go Cache

```yaml
key: go-{{ checksum "go.sum" }}
sourcePaths:
  - /root/go/pkg/mod
```

#### Python Cache

```yaml
key: pip-{{ checksum "requirements.txt" }}
sourcePaths:
  - /root/.cache/pip
```

---

### 2. Matrix Builds (Test Multiple Versions)

```
Problem: Your app should work with Java 11, 17, and 21.
Testing one at a time = 3 separate pipelines = messy

Solution: Matrix Build = Test ALL versions in parallel!

┌───────────────────────────────────────────────┐
│  Matrix Build                                  │
│                                                │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐      │
│  │ Java 11 │  │ Java 17 │  │ Java 21 │      │
│  │  Test   │  │  Test   │  │  Test   │      │
│  │  Build  │  │  Build  │  │  Build  │      │
│  └────┬────┘  └────┬────┘  └────┬────┘      │
│       │             │             │           │
│       └─────────────┼─────────────┘           │
│                     ▼                          │
│              All passed? ✅                    │
│              Continue pipeline                 │
└───────────────────────────────────────────────┘
```

**Pipeline YAML:**
```yaml
- stage:
    name: Build and Test
    identifier: build_test
    type: CI
    spec:
      execution:
        steps:
          - step:
              type: Run
              name: Test
              identifier: test
              spec:
                image: maven:<+matrix.mavenVersion>
                command: mvn test
              strategy:
                matrix:
                  mavenVersion:
                    - "3.8-eclipse-temurin-11"
                    - "3.9-eclipse-temurin-17"
                    - "3.9-eclipse-temurin-21"
                  maxConcurrency: 3
```

---

### 3. Parallel Execution

```
SEQUENTIAL (Slow):
Step 1 (2 min) → Step 2 (2 min) → Step 3 (2 min) = 6 minutes total

PARALLEL (Fast):
Step 1 (2 min) ─┐
Step 2 (2 min) ─┼─ = 2 minutes total!
Step 3 (2 min) ─┘
```

**Pipeline YAML for Parallel Steps:**
```yaml
execution:
  steps:
    - parallel:
        - step:
            type: Run
            name: Unit Tests
            identifier: unit_tests
            spec:
              command: mvn test -pl module1
        - step:
            type: Run
            name: Integration Tests
            identifier: integration_tests
            spec:
              command: mvn test -pl module2
        - step:
            type: Run
            name: Lint Check
            identifier: lint
            spec:
              command: mvn checkstyle:check
```

---

### 4. Triggers

#### Git Push Trigger
```yaml
# Runs pipeline when code is pushed to main
trigger:
  name: On Push to Main
  identifier: push_main
  type: Webhook
  spec:
    type: Github
    spec:
      type: Push
      spec:
        connectorRef: github_connector
        repoName: harness-cicd-sample-app
        actions: []
      payloadConditions:
        - key: targetBranch
          operator: Equals
          value: main
```

#### Pull Request Trigger
```yaml
# Runs pipeline when PR is opened
trigger:
  name: On Pull Request
  identifier: on_pr
  type: Webhook
  spec:
    type: Github
    spec:
      type: PullRequest
      spec:
        connectorRef: github_connector
        repoName: harness-cicd-sample-app
        actions:
          - Open
          - Synchronize
```

#### Cron Trigger (Scheduled)
```yaml
# Runs every night at midnight
trigger:
  name: Nightly Build
  identifier: nightly
  type: Scheduled
  spec:
    type: Cron
    spec:
      expression: "0 0 * * *"
```

---

### 5. Security Scanning (DevSecOps)

```
DevSecOps = Development + Security + Operations

Traditional:
  Code → Build → Deploy → Security team finds issues (TOO LATE!)

DevSecOps:
  Code → Security Scan → Build → Security Scan → Deploy
         ↑ Find issues EARLY = Cheaper to fix!

┌─────────────────────────────────────────────────┐
│  SECURITY TOOLS WE USE:                          │
│                                                   │
│  1. SonarQube  → Code quality & bugs            │
│  2. Gitleaks   → Secrets accidentally in code   │
│  3. Trivy      → Vulnerabilities in Docker image│
│  4. OWASP      → Vulnerable dependencies        │
└─────────────────────────────────────────────────┘
```

---

#### Trivy (Container Image Scanner)

```
What Trivy Does:
  Scans your Docker image for known vulnerabilities (CVEs)

Example finding:
  ⚠️ CRITICAL: log4j-2.14.0 has Remote Code Execution vulnerability
  Fix: Upgrade to log4j-2.17.1
```

**Pipeline Step:**
```yaml
- step:
    type: AquaTrivy
    name: Trivy Scan
    identifier: trivy_scan
    spec:
      mode: orchestration
      config: default
      target:
        type: container
        detection: auto
      advanced:
        log:
          level: info
      privileged: true
      image:
        type: docker_v2
        name: <+pipeline.variables.docker_repo>
        tag: <+pipeline.sequenceId>
      sbom:
        format: spdx-json
```

---

#### SonarQube (Code Quality)

```
What SonarQube Does:
  - Finds bugs in your code
  - Finds security vulnerabilities
  - Checks code style
  - Measures test coverage
  - Gives a quality "grade"

Example findings:
  🐛 BUG: Null pointer possible on line 42
  🔒 SECURITY: SQL injection risk on line 88
  💩 CODE SMELL: Method too long (200 lines)
```

**Pipeline Step:**
```yaml
- step:
    type: Run
    name: SonarQube Scan
    identifier: sonarqube
    spec:
      connectorRef: dockerhub_connector
      image: sonarsource/sonar-scanner-cli:latest
      shell: Sh
      command: |
        sonar-scanner \
          -Dsonar.projectKey=harness-course-app \
          -Dsonar.sources=src/main \
          -Dsonar.tests=src/test \
          -Dsonar.host.url=<+variable.sonarqube_url> \
          -Dsonar.login=<+secrets.getValue("sonarqube_token")> \
          -Dsonar.java.binaries=target/classes
      envVariables:
        SONAR_HOST_URL: <+variable.sonarqube_url>
```

---

#### Gitleaks (Secret Detection)

```
What Gitleaks Does:
  Finds passwords/keys accidentally committed to Git

Example findings:
  🔑 LEAK: AWS Access Key found in config.py line 15
  🔑 LEAK: Database password found in .env line 3
  🔑 LEAK: API token found in README.md line 22
```

**Pipeline Step:**
```yaml
- step:
    type: Run
    name: Gitleaks Secret Scan
    identifier: gitleaks
    spec:
      connectorRef: dockerhub_connector
      image: zricethezav/gitleaks:latest
      shell: Sh
      command: |
        echo "=== Scanning for secrets ==="
        gitleaks detect --source=. --report-format=json --report-path=gitleaks-report.json
        echo "=== No secrets found! ==="
```

---

#### OWASP Dependency Check

```
What OWASP Does:
  Checks if your libraries/dependencies have known vulnerabilities

Example findings:
  ⚠️ HIGH: spring-core-5.3.0 has CVE-2022-22965 (Spring4Shell)
  ⚠️ MEDIUM: jackson-databind-2.11.0 has deserialization issue
  Fix: Update your pom.xml to newer versions
```

**Pipeline Step:**
```yaml
- step:
    type: Run
    name: OWASP Dependency Check
    identifier: owasp
    spec:
      connectorRef: dockerhub_connector
      image: owasp/dependency-check:latest
      shell: Sh
      command: |
        /usr/share/dependency-check/bin/dependency-check.sh \
          --scan . \
          --format HTML \
          --format JSON \
          --out /harness/dependency-check-report \
          --project "harness-course-app"
```

---

## 🖥️ Complete Secure CI Pipeline

```yaml
# .harness/secure-ci-pipeline.yaml
pipeline:
  name: Secure CI Pipeline
  identifier: secure_ci_pipeline
  projectIdentifier: harness_course
  orgIdentifier: learning

  properties:
    ci:
      codebase:
        connectorRef: github_connector
        repoName: harness-cicd-sample-app
        build: <+input>

  stages:
    - stage:
        name: Build Test and Scan
        identifier: build_test_scan
        type: CI
        spec:
          cloneCodebase: true
          infrastructure:
            type: KubernetesDirect
            spec:
              connectorRef: k8s_connector
              namespace: harness-builds
          caching:
            enabled: true
            paths:
              - /root/.m2/repository
          execution:
            steps:
              # Secret Detection FIRST
              - step:
                  type: Run
                  name: Gitleaks Scan
                  identifier: gitleaks
                  spec:
                    image: zricethezav/gitleaks:latest
                    command: |
                      gitleaks detect --source=. -v
                      echo "No secrets found ✅"

              # Run tests
              - step:
                  type: Run
                  name: Unit Tests
                  identifier: tests
                  spec:
                    image: maven:3.9-eclipse-temurin-17
                    command: mvn test

              # SonarQube + OWASP in parallel
              - parallel:
                  - step:
                      type: Run
                      name: SonarQube Scan
                      identifier: sonar
                      spec:
                        image: sonarsource/sonar-scanner-cli:latest
                        command: |
                          sonar-scanner \
                            -Dsonar.projectKey=harness-course \
                            -Dsonar.sources=src/main \
                            -Dsonar.host.url=$SONAR_URL \
                            -Dsonar.login=$SONAR_TOKEN
                        envVariables:
                          SONAR_URL: <+variable.sonarqube_url>
                          SONAR_TOKEN: <+secrets.getValue("sonarqube_token")>
                  - step:
                      type: Run
                      name: OWASP Check
                      identifier: owasp
                      spec:
                        image: owasp/dependency-check:latest
                        command: |
                          /usr/share/dependency-check/bin/dependency-check.sh \
                            --scan . --format JSON --out ./reports

              # Build and Push Docker Image
              - step:
                  type: BuildAndPushDockerRegistry
                  name: Build and Push
                  identifier: docker_push
                  spec:
                    connectorRef: dockerhub_connector
                    repo: <+pipeline.variables.docker_repo>
                    tags:
                      - <+pipeline.sequenceId>
                      - latest

              # Scan the Docker Image
              - step:
                  type: AquaTrivy
                  name: Trivy Image Scan
                  identifier: trivy
                  spec:
                    mode: orchestration
                    config: default
                    target:
                      type: container
                      detection: auto
                    privileged: true
                    image:
                      type: docker_v2
                      name: <+pipeline.variables.docker_repo>
                      tag: <+pipeline.sequenceId>

  variables:
    - name: docker_repo
      type: String
      value: "yourusername/harness-course-app"
```

---

### Pipeline Flow Visualization

```
┌──────────────────────────────────────────────────┐
│  SECURE CI PIPELINE                               │
│                                                    │
│  ┌──────────────┐                                 │
│  │ Gitleaks     │  ← Find secrets in code         │
│  └──────┬───────┘                                 │
│         ▼                                          │
│  ┌──────────────┐                                 │
│  │ Unit Tests   │  ← Make sure code works         │
│  └──────┬───────┘                                 │
│         ▼                                          │
│  ┌──────────────┐  ┌──────────────┐              │
│  │ SonarQube   │  │ OWASP Check  │  ← PARALLEL  │
│  │ (code bugs) │  │ (bad deps)   │              │
│  └──────┬───────┘  └──────┬───────┘              │
│         └─────────┬────────┘                      │
│                   ▼                                │
│  ┌──────────────────────┐                         │
│  │ Docker Build & Push  │  ← Create image         │
│  └──────────┬───────────┘                         │
│             ▼                                      │
│  ┌──────────────┐                                 │
│  │ Trivy Scan   │  ← Scan image for vulns        │
│  └──────────────┘                                 │
│                                                    │
│  Result: Secure, tested, production-ready image!  │
└──────────────────────────────────────────────────┘
```

---

## ✅ Episode 5 Checklist

- [ ] Understand caching and how it speeds up builds
- [ ] Know cache keys for Maven, npm, Gradle, Go, Python
- [ ] Understand matrix builds for multi-version testing
- [ ] Know how to run steps in parallel
- [ ] Created Git Push trigger
- [ ] Created Pull Request trigger
- [ ] Understand what each security tool does
- [ ] Added Trivy scanning to pipeline
- [ ] Added SonarQube scanning to pipeline
- [ ] Added Gitleaks scanning to pipeline
- [ ] Added OWASP scanning to pipeline
- [ ] Pipeline runs security scans automatically

---

## 📝 Key Takeaways

1. **Cache = Speed** (save hours of download time)
2. **Matrix = Test multiple versions** simultaneously
3. **Parallel = Do multiple things at once** (faster pipeline)
4. **Triggers = Automatic** (push code → pipeline runs)
5. **Security scanning should be EARLY** in the pipeline (shift left)
6. **4 Security Tools**: Gitleaks (secrets), SonarQube (code), OWASP (deps), Trivy (images)

---

> 🎬 Next Episode: [Episode 6 - Continuous Delivery to Kubernetes](../Episode-06/README.md)
