# Episode 4: Build Your First Enterprise CI Pipeline

## 🎯 Goal
Build a REAL production-ready CI pipeline that builds, tests, and pushes a Docker image.
Like building an assembly line in a factory.

---

## 📚 Topics Covered

### 1. Pipeline Concepts

```
┌─────────────────────────────────────────────────────────┐
│  PIPELINE                                                │
│  ════════                                                │
│                                                          │
│  A Pipeline = A series of steps to build & deploy code   │
│                                                          │
│  Think of it like a RECIPE:                              │
│  1. Get ingredients (Clone code)                         │
│  2. Mix them (Install dependencies)                      │
│  3. Taste test (Run tests)                               │
│  4. Bake (Build application)                             │
│  5. Package (Create Docker image)                        │
│  6. Deliver (Push to registry)                           │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

### 2. Pipeline Structure

```
Pipeline
├── Stage 1: "Build & Test"
│   ├── Step 1: Clone Repository
│   ├── Step 2: Install Dependencies
│   ├── Step 3: Run Unit Tests
│   ├── Step 4: Build Application
│   └── Step 5: Build & Push Docker Image
│
├── Stage 2: "Security Scan" (Episode 5)
│
└── Stage 3: "Deploy" (Episode 6)
```

**Key Terms:**
| Term | What it is | Analogy |
|------|-----------|---------|
| Pipeline | The whole workflow | The entire recipe |
| Stage | A group of related steps | A chapter in the recipe |
| Step | A single action | One instruction |
| Variable | A reusable value | An ingredient amount |
| Expression | Dynamic value | "Add salt to taste" |

---

### 3. Variables & Runtime Inputs

```
VARIABLES (Set once, use everywhere):
═════════
pipeline:
  variables:
    - name: docker_repo
      type: String
      value: "myuser/myapp"
    - name: docker_tag
      type: String
      value: "latest"

RUNTIME INPUTS (Ask user when pipeline runs):
══════════════
pipeline:
  variables:
    - name: environment
      type: String
      value: <+input>    ← This asks "which environment?" every run

EXPRESSIONS (Dynamic values from Harness):
═══════════
<+pipeline.sequenceId>     → Build number (1, 2, 3...)
<+trigger.commitSha>       → Git commit hash
<+codebase.branch>         → Current branch name
<+variable.docker_repo>    → Your variable value
```

---

### 4. Git Experience

```
┌─────────────────────────────────────────────────┐
│  GIT EXPERIENCE                                   │
│  ══════════════                                   │
│                                                   │
│  Store pipeline YAML in Git (not just in Harness) │
│                                                   │
│  Why?                                             │
│  • Version control for pipelines                  │
│  • Code review for pipeline changes              │
│  • Same Git workflow for code AND pipelines       │
│  • Rollback pipeline changes easily              │
│                                                   │
│  Where pipeline lives:                           │
│  your-repo/                                      │
│  └── .harness/                                   │
│      └── my-pipeline.yaml                        │
│                                                   │
└─────────────────────────────────────────────────┘
```

---

### 5. Templates

```
Templates = Reusable pipeline pieces

Example: You have 10 microservices.
All need the same CI pipeline.

WITHOUT Templates:
  Copy-paste pipeline 10 times 😫
  Change one thing? Update all 10 😫😫

WITH Templates:
  Create 1 template
  Use it in all 10 projects 😊
  Change once → All 10 update 😊😊

Template Types:
  • Step Template (reuse a single step)
  • Stage Template (reuse a whole stage)
  • Pipeline Template (reuse entire pipeline)
```

---

## 🖥️ Demo: Build a Production-Ready CI Pipeline

### Our Sample Application

We'll use a Java Spring Boot application:

```
sample-app/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/example/app/
│   │           └── Application.java
│   └── test/
│       └── java/
│           └── com/example/app/
│               └── ApplicationTest.java
├── pom.xml
├── Dockerfile
└── .harness/
    └── ci-pipeline.yaml
```

---

### The Application Code

**Application.java:**
```java
package com.example.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "Hello from Harness CI/CD Course!";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
```

---

### The Dockerfile (Multi-Stage)

```dockerfile
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application (small image)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Why Multi-Stage?**
```
Single Stage:  Image size = 800MB  (includes Maven, JDK, source code)
Multi-Stage:   Image size = 150MB  (only JRE + your app JAR)

Smaller image = Faster deployment = Less money = More secure
```

---

### The CI Pipeline YAML

```yaml
# .harness/ci-pipeline.yaml
pipeline:
  name: Build and Push
  identifier: build_and_push
  projectIdentifier: harness_course
  orgIdentifier: learning
  tags: {}

  properties:
    ci:
      codebase:
        connectorRef: github_connector
        repoName: harness-cicd-sample-app
        build: <+input>

  stages:
    - stage:
        name: Build and Test
        identifier: build_and_test
        type: CI
        spec:
          cloneCodebase: true
          infrastructure:
            type: KubernetesDirect
            spec:
              connectorRef: k8s_connector
              namespace: harness-builds
              automountServiceAccountToken: true
          execution:
            steps:
              # Step 1: Run Unit Tests
              - step:
                  type: Run
                  name: Run Unit Tests
                  identifier: run_tests
                  spec:
                    connectorRef: dockerhub_connector
                    image: maven:3.9-eclipse-temurin-17
                    shell: Sh
                    command: |
                      echo "=== Running Unit Tests ==="
                      mvn test
                      echo "=== Tests Passed! ==="

              # Step 2: Build Application
              - step:
                  type: Run
                  name: Build Application
                  identifier: build_app
                  spec:
                    connectorRef: dockerhub_connector
                    image: maven:3.9-eclipse-temurin-17
                    shell: Sh
                    command: |
                      echo "=== Building Application ==="
                      mvn clean package -DskipTests
                      echo "=== Build Complete! ==="
                      ls -la target/*.jar

              # Step 3: Build and Push Docker Image
              - step:
                  type: BuildAndPushDockerRegistry
                  name: Build and Push to Docker Hub
                  identifier: build_push_dockerhub
                  spec:
                    connectorRef: dockerhub_connector
                    repo: <+pipeline.variables.docker_repo>
                    tags:
                      - <+pipeline.sequenceId>
                      - latest
                    dockerfile: Dockerfile
                    optimize: true

              # Step 4: Build and Push to Amazon ECR
              - step:
                  type: BuildAndPushECR
                  name: Build and Push to ECR
                  identifier: build_push_ecr
                  spec:
                    connectorRef: aws_connector
                    region: us-east-1
                    account: "123456789012"
                    imageName: harness-course-app
                    tags:
                      - <+pipeline.sequenceId>
                      - latest
                    dockerfile: Dockerfile

  variables:
    - name: docker_repo
      type: String
      description: "Docker Hub repository"
      value: "yourusername/harness-course-app"
```

---

### Pipeline Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│  PIPELINE: Build and Push                                │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌─────────────────────────────────────────────────┐    │
│  │  STAGE: Build and Test                           │    │
│  │                                                   │    │
│  │  ┌─────────────┐                                │    │
│  │  │ Clone Repo  │ (automatic with cloneCodebase) │    │
│  │  └──────┬──────┘                                │    │
│  │         ▼                                        │    │
│  │  ┌─────────────┐                                │    │
│  │  │ Unit Tests  │  mvn test                      │    │
│  │  └──────┬──────┘                                │    │
│  │         ▼                                        │    │
│  │  ┌─────────────┐                                │    │
│  │  │ Build App   │  mvn clean package             │    │
│  │  └──────┬──────┘                                │    │
│  │         ▼                                        │    │
│  │  ┌─────────────┐                                │    │
│  │  │ Docker Build│  Build multi-stage image        │    │
│  │  │ & Push      │  Push to Docker Hub + ECR      │    │
│  │  └─────────────┘                                │    │
│  │                                                   │    │
│  └─────────────────────────────────────────────────┘    │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

### Image Tagging Strategy

```
Tags we use:
═══════════
1. <+pipeline.sequenceId>  → Build number (1, 2, 3, 4...)
2. latest                  → Always points to newest build

Why both?
  - "latest" = quick development
  - Build number = production (know exactly which version)

Production Best Practice:
  - NEVER use "latest" in production
  - Always use specific version: myapp:42
  - Why? "latest" can change anytime = unpredictable
```

---

### Docker Buildx (Multi-Platform Builds)

```
Normal Docker Build:
  → Creates image for YOUR machine's architecture only
  → If you build on Mac M1 (ARM), won't work on Linux server (AMD64)

Docker Buildx:
  → Creates image for MULTIPLE architectures
  → Same image works on ARM + AMD64

┌─────────────────────────────────────────┐
│  Buildx builds for:                      │
│  • linux/amd64  (most servers)           │
│  • linux/arm64  (AWS Graviton, Mac M1)   │
└─────────────────────────────────────────┘
```

---

## Step-by-Step: Create Pipeline in Harness UI

### Step 1: Create Pipeline

1. Go to your project → **Pipelines** → **+ Create Pipeline**
2. Name: `Build and Push`
3. Choose: **Inline** (or Remote for Git Experience)
4. Click **Start**

### Step 2: Add CI Stage

1. Click **+ Add Stage** → **Build**
2. Stage Name: `Build and Test`
3. Configure Codebase:
   - Connector: `github-connector`
   - Repository: `harness-cicd-sample-app`
4. Click **Set Up Stage**

### Step 3: Configure Infrastructure

1. Under **Infrastructure**:
   - Type: Kubernetes
   - Connector: `k8s-connector`
   - Namespace: `harness-builds`

### Step 4: Add Steps

1. Click **+ Add Step** → **Run**
   - Name: `Run Unit Tests`
   - Container Registry: `dockerhub-connector`
   - Image: `maven:3.9-eclipse-temurin-17`
   - Command: `mvn test`

2. Click **+ Add Step** → **Run**
   - Name: `Build Application`
   - Container Registry: `dockerhub-connector`
   - Image: `maven:3.9-eclipse-temurin-17`
   - Command: `mvn clean package -DskipTests`

3. Click **+ Add Step** → **Build and Push to Docker Registry**
   - Name: `Push to Docker Hub`
   - Connector: `dockerhub-connector`
   - Repository: `yourusername/harness-course-app`
   - Tags: `<+pipeline.sequenceId>`, `latest`

### Step 5: Run the Pipeline!

1. Click **Save** → Click **Run**
2. Select branch: `main`
3. Click **Run Pipeline**
4. Watch it execute step by step! 🎉

---

## ✅ Episode 4 Checklist

- [ ] Understand Pipeline → Stage → Step hierarchy
- [ ] Know Variables, Runtime Inputs, and Expressions
- [ ] Understand Git Experience (pipeline as code)
- [ ] Know what Templates are and why they matter
- [ ] Created the sample Java application
- [ ] Wrote a multi-stage Dockerfile
- [ ] Built a complete CI pipeline
- [ ] Pipeline pushes to Docker Hub
- [ ] Pipeline pushes to Amazon ECR
- [ ] Understand image tagging strategy

---

## 📝 Key Takeaways

1. **Pipeline = Recipe** with Stages (chapters) and Steps (instructions)
2. **Multi-stage Dockerfile** = Smaller + More secure images
3. **Never use "latest" in production** → Use build numbers
4. **Git Experience** = Pipeline lives in Git (version controlled)
5. **Templates** = Write once, use everywhere

---

> 🎬 Next Episode: [Episode 5 - Advanced CI & DevSecOps](../Episode-05/README.md)
