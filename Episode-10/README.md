# Episode 10: Complete Enterprise Project (End-to-End)

## 🎯 Goal
Build the COMPLETE enterprise CI/CD platform combining EVERYTHING from Episodes 1-9.
This is your capstone project. Like building an entire city after learning about buildings.

---

## 🏗️ What We're Building

```
┌──────────────────────────────────────────────────────────┐
│          COMPLETE ENTERPRISE CI/CD PLATFORM               │
│                                                           │
│  GitHub Repository                                        │
│       ⬇️                                                  │
│  Harness CI Pipeline                                      │
│       ⬇️                                                  │
│  SonarQube Code Scan                                      │
│       ⬇️                                                  │
│  Gitleaks Secret Scan                                     │
│       ⬇️                                                  │
│  Trivy Image Scan                                         │
│       ⬇️                                                  │
│  Docker Multi-stage Build                                 │
│       ⬇️                                                  │
│  Push to Amazon ECR                                       │
│       ⬇️                                                  │
│  Deploy to Amazon EKS using Helm                          │
│       ⬇️                                                  │
│  Manual Approval Gate                                     │
│       ⬇️                                                  │
│  Canary Deployment                                        │
│       ⬇️                                                  │
│  Health Verification                                      │
│       ⬇️                                                  │
│  Prometheus & Grafana Monitoring                          │
│       ⬇️                                                  │
│  Slack Notifications                                      │
│       ⬇️                                                  │
│  Automatic Rollback on Failure                            │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

---

## 📋 Prerequisites (Everything from Previous Episodes)

```
✅ Harness account (Episode 1)
✅ Project + Organization setup (Episode 2)
✅ Delegate installed on K8s (Episode 3)
✅ All connectors configured (Episode 3)
✅ Sample Java app with Dockerfile (Episode 4)
✅ Security scanning knowledge (Episode 5)
✅ Kubernetes deployment knowledge (Episode 6)
✅ Helm chart ready (Episode 7)
✅ OPA policies + Approvals (Episode 8)
✅ Monitoring stack installed (Episode 9)
```

---

## 📁 Project Structure

```
harness-cicd-sample-app/
├── src/
│   ├── main/java/com/example/app/
│   │   ├── Application.java
│   │   ├── controller/HealthController.java
│   │   └── controller/HomeController.java
│   └── test/java/com/example/app/
│       └── ApplicationTest.java
├── k8s/
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── deployment.yaml
│   └── service.yaml
├── helm/
│   └── harness-course-app/
│       ├── Chart.yaml
│       ├── values.yaml
│       ├── values-dev.yaml
│       ├── values-prod.yaml
│       └── templates/
│           ├── deployment.yaml
│           ├── service.yaml
│           ├── configmap.yaml
│           ├── hpa.yaml
│           └── _helpers.tpl
├── ecs/
│   ├── task-definition.json
│   └── service-definition.json
├── policies/
│   ├── no-friday-deploys.rego
│   ├── require-approval.rego
│   ├── min-replicas.rego
│   └── approved-registries.rego
├── monitoring/
│   ├── prometheus-rules.yaml
│   ├── grafana-dashboard.json
│   └── alertmanager-config.yaml
├── .harness/
│   ├── ci-pipeline.yaml
│   ├── cd-pipeline.yaml
│   └── enterprise-pipeline.yaml
├── Dockerfile
├── pom.xml
├── sonar-project.properties
└── README.md
```

---

## 🚀 The Complete Enterprise Pipeline

### Pipeline Overview (3 Stages)

```
┌──────────────────────────────────────────────────────────┐
│  STAGE 1: BUILD, TEST & SCAN (CI)                         │
│  ═══════════════════════════════                          │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                │
│  │ Gitleaks │ │  Tests   │ │ SonarQube│  ← Parallel    │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘                │
│       └─────────────┼────────────┘                       │
│                     ▼                                     │
│  ┌──────────────────────────────────────┐                │
│  │ Maven Build + Docker Multi-stage      │                │
│  └──────────────────┬───────────────────┘                │
│                     ▼                                     │
│  ┌──────────┐ ┌──────────┐                              │
│  │ Push ECR │ │  Trivy   │  ← Scan after push          │
│  └──────────┘ └──────────┘                              │
│                                                           │
├──────────────────────────────────────────────────────────┤
│  STAGE 2: DEPLOY TO DEV + APPROVAL                       │
│  ═════════════════════════════════                        │
│  ┌─────────────────────┐                                 │
│  │ Helm Deploy to Dev  │  ← Auto (no approval)          │
│  └──────────┬──────────┘                                 │
│             ▼                                             │
│  ┌─────────────────────┐                                 │
│  │ Verify Dev Health   │  ← Check metrics               │
│  └──────────┬──────────┘                                 │
│             ▼                                             │
│  ┌─────────────────────────────────┐                     │
│  │ ⏸️  MANUAL APPROVAL (2 people)  │                     │
│  └──────────┬──────────────────────┘                     │
│             ▼                                             │
├──────────────────────────────────────────────────────────┤
│  STAGE 3: PRODUCTION DEPLOYMENT                          │
│  ══════════════════════════════                           │
│  ┌─────────────────────┐                                 │
│  │ Canary Deploy (25%) │  ← Small traffic first         │
│  └──────────┬──────────┘                                 │
│             ▼                                             │
│  ┌─────────────────────┐                                 │
│  │ Health Verification │  ← Compare metrics             │
│  └──────────┬──────────┘                                 │
│             ▼                                             │
│  ┌─────────────────────┐                                 │
│  │ Full Rolling Deploy │  ← All traffic                 │
│  └──────────┬──────────┘                                 │
│             ▼                                             │
│  ┌─────────────────────┐                                 │
│  │ Slack Notification  │  ← Team notified               │
│  └─────────────────────┘                                 │
│                                                           │
│  ⚡ If ANYTHING fails → AUTOMATIC ROLLBACK              │
└──────────────────────────────────────────────────────────┘
```

---

## 📝 Complete Pipeline YAML

```yaml
# .harness/enterprise-pipeline.yaml
pipeline:
  name: Enterprise CI/CD Pipeline
  identifier: enterprise_cicd
  projectIdentifier: harness_course
  orgIdentifier: learning
  tags:
    type: enterprise
    environment: production

  properties:
    ci:
      codebase:
        connectorRef: github_connector
        repoName: harness-cicd-sample-app
        build: <+input>

  variables:
    - name: docker_repo
      type: String
      value: "123456789012.dkr.ecr.us-east-1.amazonaws.com/harness-course-app"
    - name: docker_tag
      type: String
      value: <+pipeline.sequenceId>
    - name: helm_release
      type: String
      value: "harness-course-app"
    - name: eks_namespace
      type: String
      value: "harness-course"

  # ══════════════════════════════════════════════
  # STAGE 1: BUILD, TEST & SECURITY SCAN
  # ══════════════════════════════════════════════
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
              automountServiceAccountToken: true
          caching:
            enabled: true
            paths:
              - /root/.m2/repository
          execution:
            steps:
              # === PARALLEL: Security Scans + Tests ===
              - parallel:
                  - step:
                      type: Run
                      name: Gitleaks Secret Scan
                      identifier: gitleaks
                      spec:
                        connectorRef: dockerhub_connector
                        image: zricethezav/gitleaks:latest
                        shell: Sh
                        command: |
                          echo "🔍 Scanning for leaked secrets..."
                          gitleaks detect --source=. -v
                          echo "✅ No secrets found!"

                  - step:
                      type: Run
                      name: Unit Tests
                      identifier: unit_tests
                      spec:
                        connectorRef: dockerhub_connector
                        image: maven:3.9-eclipse-temurin-17
                        shell: Sh
                        command: |
                          echo "🧪 Running unit tests..."
                          mvn test
                          echo "✅ All tests passed!"

                  - step:
                      type: Run
                      name: SonarQube Analysis
                      identifier: sonarqube
                      spec:
                        connectorRef: dockerhub_connector
                        image: sonarsource/sonar-scanner-cli:latest
                        shell: Sh
                        command: |
                          echo "📊 Running SonarQube analysis..."
                          sonar-scanner \
                            -Dsonar.projectKey=harness-course-app \
                            -Dsonar.sources=src/main \
                            -Dsonar.tests=src/test \
                            -Dsonar.host.url=$SONAR_URL \
                            -Dsonar.login=$SONAR_TOKEN \
                            -Dsonar.java.binaries=target/classes
                          echo "✅ SonarQube analysis complete!"
                        envVariables:
                          SONAR_URL: <+variable.sonarqube_url>
                          SONAR_TOKEN: <+secrets.getValue("sonarqube_token")>

              # === OWASP Dependency Check ===
              - step:
                  type: Run
                  name: OWASP Dependency Check
                  identifier: owasp
                  spec:
                    connectorRef: dockerhub_connector
                    image: owasp/dependency-check:latest
                    shell: Sh
                    command: |
                      echo "🔒 Checking dependencies for vulnerabilities..."
                      /usr/share/dependency-check/bin/dependency-check.sh \
                        --scan . \
                        --format JSON \
                        --out ./reports \
                        --project "harness-course-app" \
                        --failOnCVSS 9
                      echo "✅ No critical vulnerabilities!"

              # === Build Application ===
              - step:
                  type: Run
                  name: Maven Build
                  identifier: maven_build
                  spec:
                    connectorRef: dockerhub_connector
                    image: maven:3.9-eclipse-temurin-17
                    shell: Sh
                    command: |
                      echo "🏗️ Building application..."
                      mvn clean package -DskipTests
                      echo "✅ Build successful!"
                      ls -la target/*.jar

              # === Build and Push Docker Image to ECR ===
              - step:
                  type: BuildAndPushECR
                  name: Build and Push to ECR
                  identifier: push_ecr
                  spec:
                    connectorRef: aws_connector
                    region: us-east-1
                    account: "123456789012"
                    imageName: harness-course-app
                    tags:
                      - <+pipeline.sequenceId>
                      - latest
                    dockerfile: Dockerfile
                    optimize: true
                    buildArgs:
                      APP_VERSION: <+pipeline.sequenceId>

              # === Scan the built image ===
              - step:
                  type: AquaTrivy
                  name: Trivy Image Scan
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
                      fail_on_severity: critical
                    privileged: true
                    image:
                      type: docker_v2
                      name: harness-course-app
                      tag: <+pipeline.sequenceId>
```

---

```yaml
    # ══════════════════════════════════════════════
    # STAGE 2: DEPLOY TO DEV + APPROVAL
    # ══════════════════════════════════════════════
    - stage:
        name: Deploy to Development
        identifier: deploy_dev
        type: Deployment
        spec:
          deploymentType: Kubernetes
          service:
            serviceRef: harness_course_app_helm
            serviceInputs:
              serviceDefinition:
                type: Kubernetes
                spec:
                  artifacts:
                    primary:
                      primaryArtifactRef: ecr_image
                      sources:
                        - identifier: ecr_image
                          type: Ecr
                          spec:
                            tag: <+pipeline.sequenceId>
          environment:
            environmentRef: development
            deployToAll: false
            infrastructureDefinitions:
              - identifier: dev_eks_cluster
          execution:
            steps:
              - step:
                  name: Helm Deploy to Dev
                  identifier: helm_dev
                  type: K8sRollingDeploy
                  timeout: 10m
                  spec:
                    skipDryRun: false
              - step:
                  name: Verify Dev Health
                  identifier: verify_dev
                  type: Verify
                  timeout: 10m
                  spec:
                    type: Rolling
                    monitoredService:
                      type: Default
                      spec: {}
                    spec:
                      sensitivity: LOW
                      duration: 3m
            rollbackSteps:
              - step:
                  name: Dev Rollback
                  identifier: dev_rollback
                  type: K8sRollingRollback
                  timeout: 10m
                  spec: {}

    # ══════════════════════════════════════════════
    # STAGE 3: APPROVAL GATE
    # ══════════════════════════════════════════════
    - stage:
        name: Production Approval
        identifier: prod_approval
        type: Approval
        spec:
          execution:
            steps:
              - step:
                  type: HarnessApproval
                  name: Approve Production Deployment
                  identifier: approve_prod
                  timeout: 24h
                  spec:
                    approvalMessage: |
                      🚀 PRODUCTION DEPLOYMENT APPROVAL

                      ═══════════════════════════════════
                      Build Number: #<+pipeline.sequenceId>
                      Image: harness-course-app:<+pipeline.sequenceId>
                      Triggered By: <+pipeline.triggeredBy.name>
                      ═══════════════════════════════════

                      ✅ Security Scans: Passed
                      ✅ Unit Tests: Passed
                      ✅ Dev Deployment: Successful
                      ✅ Dev Health Check: Passed

                      Please verify the dev environment before approving.
                      Dev URL: http://dev.harness-course.internal

                    includePipelineExecutionHistory: true
                    approvers:
                      minimumCount: 2
                      disallowPipelineExecutor: true
                      userGroups:
                        - engineering_leads
                        - senior_devops
                    approverInputs:
                      - name: risk_level
                        defaultValue: "low"
                      - name: change_description
                        defaultValue: ""
```

---

```yaml
    # ══════════════════════════════════════════════
    # STAGE 4: PRODUCTION CANARY DEPLOYMENT
    # ══════════════════════════════════════════════
    - stage:
        name: Deploy to Production
        identifier: deploy_prod
        type: Deployment
        spec:
          deploymentType: Kubernetes
          service:
            serviceRef: harness_course_app_helm
            serviceInputs:
              serviceDefinition:
                type: Kubernetes
                spec:
                  artifacts:
                    primary:
                      primaryArtifactRef: ecr_image
                      sources:
                        - identifier: ecr_image
                          type: Ecr
                          spec:
                            tag: <+pipeline.sequenceId>
          environment:
            environmentRef: production
            deployToAll: false
            infrastructureDefinitions:
              - identifier: prod_eks_cluster
          execution:
            steps:
              # Canary: Deploy to 25% of pods
              - step:
                  name: Canary Deployment (25%)
                  identifier: canary_25
                  type: K8sCanaryDeploy
                  timeout: 10m
                  spec:
                    instanceSelection:
                      type: Count
                      spec:
                        count: 1
                    skipDryRun: false

              # Verify canary health with Prometheus
              - step:
                  name: Verify Canary Health
                  identifier: verify_canary
                  type: Verify
                  timeout: 15m
                  spec:
                    type: Canary
                    monitoredService:
                      type: Default
                      spec: {}
                    spec:
                      sensitivity: HIGH
                      duration: 5m
                      deploymentTag: <+pipeline.sequenceId>

              # Delete canary pods
              - step:
                  name: Delete Canary
                  identifier: canary_delete
                  type: K8sCanaryDelete
                  timeout: 10m
                  spec: {}

              # Full rolling deployment
              - step:
                  name: Full Production Deploy
                  identifier: full_deploy
                  type: K8sRollingDeploy
                  timeout: 15m
                  spec:
                    skipDryRun: false

              # Final health verification
              - step:
                  name: Final Health Check
                  identifier: final_verify
                  type: Verify
                  timeout: 10m
                  spec:
                    type: Rolling
                    monitoredService:
                      type: Default
                      spec: {}
                    spec:
                      sensitivity: MEDIUM
                      duration: 5m

            # === ROLLBACK STEPS ===
            rollbackSteps:
              - step:
                  name: Canary Rollback
                  identifier: canary_rollback
                  type: K8sCanaryDelete
                  timeout: 10m
                  spec: {}
              - step:
                  name: Rolling Rollback
                  identifier: rolling_rollback
                  type: K8sRollingRollback
                  timeout: 10m
                  spec: {}

  # ══════════════════════════════════════════════
  # NOTIFICATIONS
  # ══════════════════════════════════════════════
  notificationRules:
    - name: Slack Notifications
      enabled: true
      pipelineEvents:
        - type: PipelineSuccess
        - type: PipelineFailed
        - type: StageFailed
      notificationMethod:
        type: Slack
        spec:
          webhookUrl: <+secrets.getValue("slack_webhook")>

    - name: Email on Failure
      enabled: true
      pipelineEvents:
        - type: PipelineFailed
      notificationMethod:
        type: Email
        spec:
          recipients:
            - devops-team@company.com
            - oncall@company.com
```

---

## 🖥️ Step-by-Step Implementation

### Step 1: Verify All Prerequisites

```bash
# Check Delegate is running
kubectl get pods -n harness-delegate-ng
# ✅ my-k8s-delegate-xxx Running

# Check monitoring stack
kubectl get pods -n monitoring
# ✅ prometheus-xxx Running
# ✅ grafana-xxx Running

# Check EKS access
kubectl get nodes
# ✅ Nodes are Ready

# Check ECR access
aws ecr describe-repositories --region us-east-1
# ✅ harness-course-app repository exists
```

### Step 2: Create the Pipeline in Harness UI

1. Go to **Pipelines** → **+ Create Pipeline**
2. Name: `Enterprise CI/CD Pipeline`
3. Choose **Remote** (store in Git)
4. Repository: `harness-cicd-sample-app`
5. Path: `.harness/enterprise-pipeline.yaml`

### Step 3: Configure Triggers

```yaml
# Auto-trigger on push to main
trigger:
  name: On Push to Main
  identifier: push_to_main
  type: Webhook
  spec:
    type: Github
    spec:
      type: Push
      spec:
        connectorRef: github_connector
        repoName: harness-cicd-sample-app
      payloadConditions:
        - key: targetBranch
          operator: Equals
          value: main
  inputYaml: |
    pipeline:
      identifier: enterprise_cicd
      properties:
        ci:
          codebase:
            build:
              type: branch
              spec:
                branch: main
```

### Step 4: Configure OPA Policies

Apply all policies from Episode 8:
- No Friday deployments
- Must have approval for production
- Minimum 3 replicas in production
- Only approved ECR registries

### Step 5: Run the Complete Pipeline!

```
1. Push code to main branch
2. Trigger fires → Pipeline starts
3. Watch each stage execute:

   🔍 Gitleaks........... ✅ (12s)
   🧪 Unit Tests......... ✅ (45s)
   📊 SonarQube.......... ✅ (1m 20s)
   🔒 OWASP Check........ ✅ (2m 10s)
   🏗️ Maven Build........ ✅ (1m 5s)
   🐳 Docker Build+Push.. ✅ (2m 30s)
   🛡️ Trivy Scan......... ✅ (45s)
   🚀 Deploy to Dev...... ✅ (1m 50s)
   💚 Dev Health Check... ✅ (3m)
   ⏸️ Approval........... ⏳ (waiting)
   
   [Manager approves] ✅
   
   🐤 Canary (25%)....... ✅ (2m)
   📈 Verify Canary...... ✅ (5m)
   🚀 Full Deploy........ ✅ (3m)
   💚 Final Health....... ✅ (5m)
   📱 Slack Notify....... ✅ (sent!)
   
   ═══════════════════════════════════
   ✅ PIPELINE COMPLETE! Total: ~25 min
   ═══════════════════════════════════
```

---

## 🔄 Testing Rollback

### Simulate a Failure

```bash
# 1. Deploy a "bad" version (one that crashes)
# Modify Application.java to throw an error on /health

# 2. Push to main
git push

# 3. Pipeline runs through CI
# 4. Deploys canary to production
# 5. Verify step detects increased error rate
# 6. AUTOMATIC ROLLBACK triggers!
# 7. Previous healthy version is restored
# 8. Slack notification: "⚠️ Deployment failed, rolled back to v42"
```

---

## 📊 Monitoring Dashboard

After deployment, check Grafana:

```
┌──────────────────────────────────────────────────────────┐
│  GRAFANA - Harness Course App Dashboard                   │
│                                                           │
│  ┌─────────────────────────────────────────────────┐     │
│  │ Request Rate (req/s)                             │     │
│  │ ▁▂▃▅▇█▇▅▃▅▇█▇▅▃▂▁▂▃▅▇ (healthy pattern)       │     │
│  └─────────────────────────────────────────────────┘     │
│                                                           │
│  ┌─────────────────────────────────────────────────┐     │
│  │ Error Rate (%)                                   │     │
│  │ ▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁ (0.01% - excellent!)    │     │
│  └─────────────────────────────────────────────────┘     │
│                                                           │
│  ┌─────────────────────────────────────────────────┐     │
│  │ Response Time P99 (ms)                           │     │
│  │ ▁▁▁▂▂▁▁▁▁▂▁▁▁▁▁▁▁▁▁▁▁ (142ms - good!)         │     │
│  └─────────────────────────────────────────────────┘     │
│                                                           │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐          │
│  │ Pods: 3/3  │ │ CPU: 45%   │ │ Mem: 62%   │          │
│  │    ✅      │ │    ✅      │ │    ✅      │          │
│  └────────────┘ └────────────┘ └────────────┘          │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

---

## 🏆 What You've Built (Complete Summary)

```
┌──────────────────────────────────────────────────────────┐
│  YOUR ENTERPRISE CI/CD PLATFORM                           │
│                                                           │
│  ┌──────────────────────────────────────────────────┐    │
│  │  INFRASTRUCTURE                                   │    │
│  │  • Harness SaaS Platform                         │    │
│  │  • Kubernetes Delegate (HA)                      │    │
│  │  • Amazon EKS Cluster                            │    │
│  │  • Amazon ECR Registry                           │    │
│  │  • Prometheus + Grafana Stack                    │    │
│  └──────────────────────────────────────────────────┘    │
│                                                           │
│  ┌──────────────────────────────────────────────────┐    │
│  │  CI PIPELINE                                      │    │
│  │  • Automated testing (unit + integration)        │    │
│  │  • Security scanning (4 tools)                   │    │
│  │  • Docker multi-stage builds                     │    │
│  │  • Image tagging strategy                        │    │
│  │  • Caching for fast builds                       │    │
│  └──────────────────────────────────────────────────┘    │
│                                                           │
│  ┌──────────────────────────────────────────────────┐    │
│  │  CD PIPELINE                                      │    │
│  │  • Helm-based deployments                        │    │
│  │  • Multi-environment (Dev → Prod)                │    │
│  │  • Canary deployment strategy                    │    │
│  │  • Automated health verification                 │    │
│  │  • Automatic rollback                            │    │
│  └──────────────────────────────────────────────────┘    │
│                                                           │
│  ┌──────────────────────────────────────────────────┐    │
│  │  GOVERNANCE                                       │    │
│  │  • RBAC (who can do what)                        │    │
│  │  • Approval gates (2-person rule)                │    │
│  │  • OPA policies (automated rules)               │    │
│  │  • Secret management (AWS SM / Vault)            │    │
│  │  • Audit trail                                    │    │
│  └──────────────────────────────────────────────────┘    │
│                                                           │
│  ┌──────────────────────────────────────────────────┐    │
│  │  OBSERVABILITY                                    │    │
│  │  • Prometheus metrics collection                 │    │
│  │  • Grafana dashboards                            │    │
│  │  • Alerting rules                                 │    │
│  │  • Slack/Email notifications                     │    │
│  │  • Continuous verification                       │    │
│  └──────────────────────────────────────────────────┘    │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

---

## 🎓 Technologies Mastered

| Category | Technologies |
|----------|-------------|
| **Platform** | Harness CI, Harness CD, Harness GitOps, Harness STO |
| **Containers** | Docker, Docker Buildx, Multi-stage Builds |
| **Orchestration** | Kubernetes, Helm, Amazon EKS, Amazon ECS |
| **Cloud** | AWS (ECR, EKS, ECS, IAM, Secrets Manager) |
| **Security** | Trivy, SonarQube, Gitleaks, OWASP, OPA |
| **Monitoring** | Prometheus, Grafana, Datadog, New Relic |
| **GitOps** | Argo CD, Harness GitOps |
| **Governance** | RBAC, Approval Gates, Policy as Code |
| **Notifications** | Slack, Email, Microsoft Teams |
| **Secrets** | AWS Secrets Manager, HashiCorp Vault |

---

## 💡 Enterprise Best Practices Learned

```
1.  Never store secrets in code → Use secret managers
2.  Never deploy without tests → Shift-left testing
3.  Never skip security scans → DevSecOps by default
4.  Never use "latest" tag in production → Use build numbers
5.  Never deploy directly to prod → Use environments (dev → prod)
6.  Never deploy without approval → Require 2+ approvers
7.  Never deploy all at once → Use canary deployments
8.  Never deploy without monitoring → Continuous verification
9.  Never leave failures unnoticed → Slack/email alerts
10. Never leave broken deployments → Automatic rollback
```

---

## ✅ Final Checklist

- [ ] Complete enterprise pipeline running end-to-end
- [ ] All security scans passing
- [ ] Docker image built and pushed to ECR
- [ ] Dev deployment working with auto health checks
- [ ] Approval gates configured (2-person rule)
- [ ] Production canary deployment working
- [ ] Health verification catching failures
- [ ] Automatic rollback tested
- [ ] Monitoring dashboards showing metrics
- [ ] Slack notifications working
- [ ] OPA policies enforcing rules
- [ ] Triggers auto-starting pipeline on push
- [ ] Full audit trail in Harness

---

## 🎉 Congratulations!

You've built a **complete enterprise-grade CI/CD platform** that:
- Builds and tests code automatically
- Scans for security vulnerabilities
- Deploys safely with canary strategy
- Verifies health with real metrics
- Rolls back automatically on failure
- Notifies the team of everything
- Enforces enterprise governance

**This is exactly how CI/CD works at companies like Netflix, Google, and Amazon.**

You're now ready to implement this at your organization! 🚀

---

> 🎬 Course Complete! Go back to [Course Overview](../README.md)
