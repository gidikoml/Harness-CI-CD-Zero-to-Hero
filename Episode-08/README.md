# Episode 8: Enterprise Security & Governance

## 🎯 Goal
Add enterprise-grade security: secret management, approval gates, and policy enforcement.
Like adding locks, security guards, and rules to a bank vault.

---

## 📚 Topics Covered

### 1. Why Enterprise Security Matters

```
┌─────────────────────────────────────────────────────────┐
│  WITHOUT Security & Governance:                          │
│                                                          │
│  Junior dev → Pushes directly to production 💀          │
│  Password in code → Gets leaked on GitHub 💀            │
│  No approval → Broken code reaches users 💀            │
│  No policies → Everyone does whatever they want 💀     │
│                                                          │
├─────────────────────────────────────────────────────────┤
│  WITH Security & Governance:                             │
│                                                          │
│  Junior dev → Must get approval first ✅                │
│  Password → Stored in vault, never in code ✅           │
│  Deployment → Requires manager approval ✅              │
│  Policies → Enforce rules automatically ✅              │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

### 2. Secret Managers

```
┌─────────────────────────────────────────────────────────┐
│  SECRET MANAGERS = Where you store sensitive values      │
│                                                          │
│  Harness Built-in   → Good for getting started          │
│  AWS Secrets Manager → Best for AWS-heavy teams         │
│  HashiCorp Vault    → Best for multi-cloud/enterprise   │
│  Azure Key Vault    → Best for Azure teams              │
│  GCP Secret Manager → Best for GCP teams               │
│                                                          │
│  Think of it like choosing where to keep your money:    │
│  • Under mattress (in code)      → TERRIBLE ❌         │
│  • Home safe (Harness built-in)  → OK for small stuff  │
│  • Bank vault (Vault/AWS SM)     → Enterprise-grade ✅ │
└─────────────────────────────────────────────────────────┘
```

---

### 3. AWS Secrets Manager

```
┌─────────────────────────────────────────────────────────┐
│  AWS SECRETS MANAGER                                     │
│                                                          │
│  What: AWS service to store and manage secrets          │
│  Cost: ~$0.40 per secret per month                      │
│                                                          │
│  Features:                                               │
│  • Automatic rotation (change passwords automatically)  │
│  • Encryption at rest (AES-256)                         │
│  • Audit trail (who accessed what, when)                │
│  • Fine-grained access (IAM policies)                   │
│  • Cross-region replication                             │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

**Setup in Harness:**
```yaml
# Connect Harness to AWS Secrets Manager
secretManager:
  name: aws-secrets-manager
  type: AwsSecretManager
  spec:
    connectorRef: aws_connector
    region: us-east-1
    secretNamePrefix: harness/
    default: true    # Make this the default secret manager
```

**Store a secret:**
```bash
# Using AWS CLI
aws secretsmanager create-secret \
  --name "harness/docker-password" \
  --description "Docker Hub access token" \
  --secret-string "dckr_pat_xxxxxxxxxxxx"
```

**Use in Harness pipeline:**
```yaml
# Reference the secret
env:
  DOCKER_PASSWORD: <+secrets.getValue("docker-password")>
```

---

### 4. HashiCorp Vault

```
┌─────────────────────────────────────────────────────────┐
│  HASHICORP VAULT                                         │
│                                                          │
│  What: Industry-standard secret management              │
│  Why:  Works with ANY cloud (multi-cloud)               │
│                                                          │
│  Features:                                               │
│  • Dynamic secrets (create on-demand, expire auto)      │
│  • Encryption as a Service                              │
│  • Leasing (secrets auto-expire)                        │
│  • Revocation (kill secrets instantly)                  │
│  • Audit logging                                         │
│                                                          │
│  ANALOGY:                                               │
│  Vault is like a smart bank:                            │
│  • Gives you a temporary key to a safe deposit box     │
│  • Key expires after 1 hour                             │
│  • Logs every time you open the box                    │
│  • Can instantly lock you out if needed                │
└─────────────────────────────────────────────────────────┘
```

**Vault Setup:**
```bash
# Start Vault (development mode)
vault server -dev

# Store a secret
vault kv put secret/harness/db-password value="super-secret-123"

# Read a secret
vault kv get secret/harness/db-password
```

**Connect Harness to Vault:**
```yaml
secretManager:
  name: hashicorp-vault
  type: Vault
  spec:
    vaultUrl: https://vault.company.com
    authToken: <vault-token>  # Or use AppRole auth
    secretEngineName: secret
    secretEngineVersion: 2
    renewalIntervalMinutes: 60
    default: false
```

---

### 5. Kubernetes Secrets (In-Cluster)

```yaml
# For secrets that live in the K8s cluster itself
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: harness-course
type: Opaque
data:
  # Values must be base64 encoded
  DB_PASSWORD: c3VwZXItc2VjcmV0LTEyMw==    # super-secret-123
  API_KEY: bXktYXBpLWtleS0xMjM0NQ==          # my-api-key-12345

# Create from command line:
# kubectl create secret generic app-secrets \
#   --from-literal=DB_PASSWORD=super-secret-123 \
#   --from-literal=API_KEY=my-api-key-12345 \
#   -n harness-course
```

---

### 6. Encrypted Variables

```
┌─────────────────────────────────────────────────────────┐
│  ENCRYPTED VARIABLES                                     │
│  ═══════════════════                                     │
│                                                          │
│  Regular Variable:                                       │
│    Name: "app_name"                                     │
│    Value: "my-app"     ← Visible to everyone            │
│                                                          │
│  Encrypted Variable:                                     │
│    Name: "db_password"                                  │
│    Value: "********"   ← Hidden, stored in secret mgr   │
│                                                          │
│  When to use what:                                       │
│  • Regular Variable → docker_repo, app_name, region     │
│  • Encrypted/Secret → passwords, tokens, API keys       │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

### 7. Approval Gates

```
┌─────────────────────────────────────────────────────────┐
│  APPROVAL GATES                                          │
│  ═══════════════                                         │
│                                                          │
│  What: A "checkpoint" that PAUSES the pipeline           │
│  Why:  Human must approve before continuing              │
│  When: Before deploying to production                    │
│                                                          │
│  Flow:                                                   │
│  CI Pipeline → Build ✅ → Tests ✅                      │
│      ↓                                                   │
│  CD Pipeline → Deploy to Dev ✅                         │
│      ↓                                                   │
│  ⏸️  APPROVAL GATE → "Deploy to Prod?"                  │
│      ↓                                                   │
│  Manager clicks "Approve" ✅                            │
│      ↓                                                   │
│  Deploy to Production 🚀                                │
│                                                          │
│  Types in Harness:                                       │
│  1. Manual Approval     → Someone clicks "Approve"      │
│  2. Jira Approval       → Jira ticket must be approved  │
│  3. ServiceNow Approval → ServiceNow change request     │
│  4. Custom Approval     → Any webhook/API call          │
└─────────────────────────────────────────────────────────┘
```

---

### 8. Manual Approval

```yaml
# In your pipeline
- step:
    type: HarnessApproval
    name: Approve Production Deploy
    identifier: approve_prod
    timeout: 24h
    spec:
      approvalMessage: |
        Please review and approve deployment to PRODUCTION.
        
        Image: <+pipeline.variables.docker_repo>:<+pipeline.sequenceId>
        Triggered by: <+pipeline.triggeredBy.name>
        
        Changes:
        - <+pipeline.variables.change_description>
      includePipelineExecutionHistory: true
      approvers:
        minimumCount: 1
        disallowPipelineExecutor: true    # Person who started can't approve
        userGroups:
          - senior_devops
          - team_leads
      approverInputs:
        - name: risk_assessment
          defaultValue: ""
```

**What the approver sees:**
```
┌─────────────────────────────────────────────────────────┐
│  ⏸️  APPROVAL REQUIRED                                   │
│                                                          │
│  Pipeline: Build and Deploy                              │
│  Stage: Deploy to Production                             │
│                                                          │
│  Message:                                                │
│  Please review and approve deployment to PRODUCTION.     │
│  Image: myuser/myapp:42                                  │
│  Triggered by: junior-developer@company.com              │
│                                                          │
│  [✅ APPROVE]    [❌ REJECT]                             │
│                                                          │
│  Comment: ___________________                            │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

### 9. Jira Approval

```yaml
- step:
    type: JiraApproval
    name: Jira Change Approval
    identifier: jira_approval
    timeout: 72h
    spec:
      connectorRef: jira_connector
      issueKey: <+pipeline.variables.jira_ticket>
      approvalCriteria:
        type: KeyValues
        spec:
          matchAnyCondition: false
          conditions:
            - key: Status
              operator: equals
              value: Approved
      rejectionCriteria:
        type: KeyValues
        spec:
          matchAnyCondition: true
          conditions:
            - key: Status
              operator: equals
              value: Rejected
            - key: Status
              operator: equals
              value: Cancelled
```

**How it works:**
```
1. Developer creates Jira ticket: "Deploy v1.2.3 to production"
2. Pipeline starts → Hits Jira Approval step
3. Pipeline WAITS until Jira ticket status = "Approved"
4. Manager reviews ticket → Changes status to "Approved"
5. Pipeline continues → Deploys to production
```

---

### 10. OPA (Open Policy Agent)

```
┌─────────────────────────────────────────────────────────┐
│  OPA = Automatic Rule Enforcement                        │
│                                                          │
│  Think of OPA like automated security guards:            │
│                                                          │
│  Guard 1: "No deployment on Friday after 5 PM"          │
│  Guard 2: "Production must have at least 3 replicas"    │
│  Guard 3: "Docker images must come from our ECR only"   │
│  Guard 4: "All pipelines must have security scan"       │
│                                                          │
│  These rules run AUTOMATICALLY.                          │
│  No human needed to enforce them.                        │
│  Break a rule? Pipeline is BLOCKED.                      │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

### 11. OPA Policies (Rego Language)

#### Policy 1: No Friday Deployments
```rego
# no_friday_deploys.rego
package pipeline

deny[msg] {
  day := time.weekday(time.now_ns())
  day == "Friday"
  input.pipeline.stages[_].type == "Deployment"
  msg := "Production deployments are not allowed on Fridays!"
}
```

#### Policy 2: Minimum Replicas
```rego
# min_replicas.rego
package pipeline

deny[msg] {
  stage := input.pipeline.stages[_]
  stage.type == "Deployment"
  stage.spec.environment.type == "Production"
  
  # Check if replicas < 3
  replicas := stage.spec.execution.steps[_].spec.instances
  replicas < 3
  
  msg := sprintf("Production deployments must have at least 3 replicas. Found: %d", [replicas])
}
```

#### Policy 3: Must Have Approval
```rego
# require_approval.rego
package pipeline

deny[msg] {
  stage := input.pipeline.stages[_]
  stage.type == "Deployment"
  stage.spec.environment.type == "Production"
  
  # Check if approval step exists
  not has_approval_step(stage)
  
  msg := "Production deployments MUST have an approval step!"
}

has_approval_step(stage) {
  stage.spec.execution.steps[_].type == "HarnessApproval"
}
```

#### Policy 4: Only Our ECR Images
```rego
# approved_registries.rego
package pipeline

approved_registries = [
  "123456789012.dkr.ecr.us-east-1.amazonaws.com",
  "123456789012.dkr.ecr.eu-west-1.amazonaws.com"
]

deny[msg] {
  image := input.pipeline.stages[_].spec.service.artifacts.primary.spec.imagePath
  not starts_with_any(image, approved_registries)
  msg := sprintf("Image '%s' is not from an approved registry!", [image])
}

starts_with_any(str, prefixes) {
  startswith(str, prefixes[_])
}
```

---

### 12. Setting Up OPA in Harness

```
Steps:
1. Go to Account Settings → Policies
2. Click + New Policy
3. Write your Rego policy
4. Create a Policy Set (group of policies)
5. Attach Policy Set to:
   - Pipeline (runs when pipeline is saved/run)
   - Connector (runs when connector is created)
   - Secret (runs when secret is created)
```

**Policy Set Configuration:**
```yaml
policySet:
  name: Production Deployment Rules
  identifier: prod_deploy_rules
  type: Pipeline
  action: OnRun    # Check when pipeline RUNS (not just save)
  policies:
    - name: No Friday Deploys
      severity: Error       # Error = blocks pipeline
    - name: Must Have Approval
      severity: Error
    - name: Minimum Replicas
      severity: Warning     # Warning = allow but warn
    - name: Approved Registries
      severity: Error
```

---

## 🖥️ Demo: Build Production Approval Workflow

### Complete Pipeline with Security

```yaml
pipeline:
  name: Enterprise Deployment Pipeline
  identifier: enterprise_deploy
  projectIdentifier: harness_course
  orgIdentifier: learning

  stages:
    # Stage 1: Build (from Episode 5)
    - stage:
        name: Build and Scan
        identifier: build_scan
        type: CI
        spec:
          cloneCodebase: true
          infrastructure:
            type: KubernetesDirect
            spec:
              connectorRef: k8s_connector
              namespace: harness-builds
          execution:
            steps:
              - step:
                  type: Run
                  name: Build and Test
                  identifier: build_test
                  spec:
                    image: maven:3.9-eclipse-temurin-17
                    command: mvn clean package
              - step:
                  type: BuildAndPushECR
                  name: Push to ECR
                  identifier: push_ecr
                  spec:
                    connectorRef: aws_connector
                    region: us-east-1
                    account: "123456789012"
                    imageName: harness-course-app
                    tags:
                      - <+pipeline.sequenceId>

    # Stage 2: Deploy to Dev (no approval needed)
    - stage:
        name: Deploy to Dev
        identifier: deploy_dev
        type: Deployment
        spec:
          deploymentType: Kubernetes
          service:
            serviceRef: harness_course_app
          environment:
            environmentRef: development
            infrastructureDefinitions:
              - identifier: dev_k8s
          execution:
            steps:
              - step:
                  type: K8sRollingDeploy
                  name: Deploy
                  identifier: deploy
                  timeout: 10m
                  spec:
                    skipDryRun: false

    # Stage 3: Approval Gate
    - stage:
        name: Production Approval
        identifier: prod_approval
        type: Approval
        spec:
          execution:
            steps:
              - step:
                  type: HarnessApproval
                  name: Approve for Production
                  identifier: approve
                  timeout: 24h
                  spec:
                    approvalMessage: |
                      🚀 Ready to deploy to PRODUCTION?
                      
                      Build: #<+pipeline.sequenceId>
                      Image: harness-course-app:<+pipeline.sequenceId>
                      Dev deployment: ✅ Successful
                      
                      Please verify in Dev before approving.
                    approvers:
                      minimumCount: 2
                      disallowPipelineExecutor: true
                      userGroups:
                        - engineering_leads
                        - devops_team

    # Stage 4: Deploy to Production (after approval)
    - stage:
        name: Deploy to Production
        identifier: deploy_prod
        type: Deployment
        spec:
          deploymentType: Kubernetes
          service:
            serviceRef: harness_course_app
          environment:
            environmentRef: production
            infrastructureDefinitions:
              - identifier: prod_k8s
          execution:
            steps:
              - step:
                  type: K8sCanaryDeploy
                  name: Canary 25%
                  identifier: canary
                  timeout: 10m
                  spec:
                    instanceSelection:
                      type: Count
                      spec:
                        count: 1
              - step:
                  type: Verify
                  name: Verify Canary
                  identifier: verify
                  timeout: 15m
                  spec:
                    type: Canary
                    spec:
                      sensitivity: HIGH
                      duration: 5m
              - step:
                  type: K8sCanaryDelete
                  name: Delete Canary
                  identifier: canary_delete
                  timeout: 10m
                  spec: {}
              - step:
                  type: K8sRollingDeploy
                  name: Full Rolling Deploy
                  identifier: full_deploy
                  timeout: 10m
                  spec:
                    skipDryRun: false
            rollbackSteps:
              - step:
                  type: K8sRollingRollback
                  name: Rollback
                  identifier: rollback
                  timeout: 10m
                  spec: {}
```

---

### Pipeline Flow Visualization

```
┌──────────────────────────────────────────────────────────┐
│  ENTERPRISE DEPLOYMENT PIPELINE                           │
│                                                           │
│  ┌──────────────┐                                        │
│  │ Build & Scan │  CI: test, build, push image           │
│  └──────┬───────┘                                        │
│         ▼                                                 │
│  ┌──────────────┐                                        │
│  │ Deploy to Dev│  Auto-deploy (no approval)             │
│  └──────┬───────┘                                        │
│         ▼                                                 │
│  ┌──────────────────────────────────────────┐            │
│  │ ⏸️  APPROVAL GATE                         │            │
│  │                                           │            │
│  │  "Ready to deploy to PRODUCTION?"         │            │
│  │  Needs: 2 approvers from leads team       │            │
│  │  Timeout: 24 hours                        │            │
│  │                                           │            │
│  │  [✅ APPROVE]    [❌ REJECT]               │            │
│  └──────────────────────┬───────────────────┘            │
│                          ▼                                │
│  ┌──────────────────────────────────────────┐            │
│  │ Deploy to Production                      │            │
│  │                                           │            │
│  │  Step 1: Canary (25% traffic)            │            │
│  │  Step 2: Verify health (5 min)           │            │
│  │  Step 3: Full rolling deployment         │            │
│  │                                           │            │
│  │  If anything fails → Auto Rollback! 🔄   │            │
│  └──────────────────────────────────────────┘            │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

---

## ✅ Episode 8 Checklist

- [ ] Understand why enterprise security matters
- [ ] Know the different secret manager options
- [ ] Configured AWS Secrets Manager with Harness
- [ ] Understand HashiCorp Vault basics
- [ ] Know when to use encrypted variables vs secrets
- [ ] Understand all approval gate types
- [ ] Added Manual Approval to pipeline
- [ ] Configured Jira Approval (if using Jira)
- [ ] Understand OPA and Policy as Code
- [ ] Can write basic Rego policies
- [ ] Created policy set in Harness
- [ ] Built complete enterprise deployment pipeline
- [ ] Pipeline has: Build → Dev → Approval → Prod flow

---

## 📝 Key Takeaways

1. **Never store secrets in code** → Use a secret manager
2. **AWS Secrets Manager** = Best for AWS teams (auto-rotation)
3. **HashiCorp Vault** = Best for multi-cloud (most features)
4. **Approval Gates** = Human checkpoint before risky actions
5. **OPA Policies** = Automatic rule enforcement (no humans needed)
6. **Defense in Depth** = Multiple layers of security

---

> 🎬 Next Episode: [Episode 9 - GitOps & Observability](../Episode-09/README.md)
