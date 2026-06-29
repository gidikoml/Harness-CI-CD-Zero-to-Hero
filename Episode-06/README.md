# Episode 6: Continuous Delivery to Kubernetes

## 🎯 Goal
Deploy your application to Kubernetes using Harness CD.
Like delivering a pizza from the kitchen (CI) to the customer's door (Production).

---

## 📚 Topics Covered

### 1. What is Continuous Delivery (CD)?

```
CI (What we built so far):
  Code → Test → Build → Docker Image → Push to Registry
  = You made the pizza 🍕

CD (What we build now):
  Docker Image → Deploy to Server → Users can access it
  = You delivered the pizza to the customer 🚗💨
```

**Think of it like Amazon:**
```
CI  = Warehouse packs the box
CD  = Delivery truck brings it to your house
You = Happy customer using the new feature
```

---

### 2. Kubernetes Deployment Concepts

```
┌─────────────────────────────────────────────────────────┐
│  KUBERNETES BASICS (Super Simple)                        │
│                                                          │
│  Pod         = A running copy of your app               │
│  Deployment  = Manages your pods (creates, updates)     │
│  Service     = A door to reach your pods (like a URL)   │
│  Namespace   = A folder to organize things              │
│  ConfigMap   = Settings for your app (non-secret)       │
│  Secret      = Passwords/keys for your app              │
│                                                          │
│  ANALOGY:                                               │
│  Namespace  = A floor in an apartment building          │
│  Deployment = An apartment (can have multiple rooms)    │
│  Pod        = A room where your app lives               │
│  Service    = The apartment's doorbell                   │
│  ConfigMap  = Instructions on the fridge                 │
│  Secret     = The safe in the bedroom                   │
└─────────────────────────────────────────────────────────┘
```

---

### 3. Kubernetes Manifests for Our App

#### Namespace
```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: harness-course
  labels:
    app: harness-course
```

#### ConfigMap
```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: harness-course
data:
  APP_ENV: "production"
  APP_PORT: "8080"
  LOG_LEVEL: "info"
  APP_NAME: "Harness Course App"
```

#### Secret
```yaml
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: harness-course
type: Opaque
stringData:
  DB_PASSWORD: "super-secret-password"
  API_KEY: "my-api-key-12345"
```

#### Deployment
```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: harness-course-app
  namespace: harness-course
  labels:
    app: harness-course-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: harness-course-app
  template:
    metadata:
      labels:
        app: harness-course-app
    spec:
      containers:
        - name: app
          image: <+artifact.image>
          ports:
            - containerPort: 8080
          env:
            - name: APP_ENV
              valueFrom:
                configMapKeyRef:
                  name: app-config
                  key: APP_ENV
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: app-secrets
                  key: DB_PASSWORD
          resources:
            requests:
              memory: "256Mi"
              cpu: "250m"
            limits:
              memory: "512Mi"
              cpu: "500m"
          readinessProbe:
            httpGet:
              path: /health
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
          livenessProbe:
            httpGet:
              path: /health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
```

#### Service
```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: harness-course-app
  namespace: harness-course
spec:
  type: LoadBalancer
  selector:
    app: harness-course-app
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
```

---

### 4. Harness CD Concepts

```
┌─────────────────────────────────────────────────────────┐
│  HARNESS CD BUILDING BLOCKS                              │
│                                                          │
│  Service      = WHAT you're deploying (your app)        │
│  Environment  = WHERE you're deploying (dev/prod)       │
│  Infrastructure = HOW to reach the target (K8s cluster) │
│  Execution    = The steps to deploy                      │
│                                                          │
│  ANALOGY:                                               │
│  Service      = The pizza you made                      │
│  Environment  = Customer's neighborhood (dev/staging)    │
│  Infrastructure = The exact address                      │
│  Execution    = Drive there, ring bell, hand it over    │
└─────────────────────────────────────────────────────────┘
```

---

### 5. Deployment Strategies

```
┌─────────────────────────────────────────────────────────┐
│  1. ROLLING DEPLOYMENT                                   │
│  ═══════════════════════                                 │
│                                                          │
│  Replace pods ONE BY ONE                                 │
│                                                          │
│  Before: [v1] [v1] [v1] [v1]                           │
│  Step 1:  [v2] [v1] [v1] [v1]  ← 1 pod updated        │
│  Step 2:  [v2] [v2] [v1] [v1]  ← 2 pods updated       │
│  Step 3:  [v2] [v2] [v2] [v1]  ← 3 pods updated       │
│  After:   [v2] [v2] [v2] [v2]  ← All done!            │
│                                                          │
│  ✅ Zero downtime                                        │
│  ✅ Simple                                               │
│  ❌ Both versions run at same time (briefly)            │
│  Best for: Most applications                            │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  2. BLUE-GREEN DEPLOYMENT                                │
│  ════════════════════════                                │
│                                                          │
│  Run 2 environments, switch traffic instantly            │
│                                                          │
│  BLUE (current):  [v1] [v1] [v1]  ← Users here         │
│  GREEN (new):     [v2] [v2] [v2]  ← Testing here       │
│                                                          │
│  Ready? SWITCH!                                          │
│                                                          │
│  BLUE (old):      [v1] [v1] [v1]  ← Nobody here        │
│  GREEN (current): [v2] [v2] [v2]  ← Users here now!    │
│                                                          │
│  ✅ Instant rollback (switch back to Blue)              │
│  ✅ Zero downtime                                        │
│  ❌ Needs 2x resources                                  │
│  Best for: Critical applications                        │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  3. CANARY DEPLOYMENT                                    │
│  ════════════════════                                    │
│                                                          │
│  Send small % of traffic to new version first           │
│                                                          │
│  Step 1: 10% traffic → [v2]    90% traffic → [v1]      │
│  Step 2: 25% traffic → [v2]    75% traffic → [v1]      │
│  Step 3: 50% traffic → [v2]    50% traffic → [v1]      │
│  Step 4: 100% traffic → [v2]   Done!                    │
│                                                          │
│  ✅ Least risk (only small % of users see issues)       │
│  ✅ Can monitor between steps                           │
│  ❌ More complex                                        │
│  Best for: High-traffic applications                    │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  4. RECREATE (Big Bang)                                  │
│  ════════════════════                                    │
│                                                          │
│  Kill everything, deploy new                             │
│                                                          │
│  Before: [v1] [v1] [v1]                                │
│  Step 1:  [ ] [ ] [ ]     ← All killed (DOWNTIME!)     │
│  After:  [v2] [v2] [v2]   ← All new                    │
│                                                          │
│  ✅ Simple, clean                                        │
│  ❌ HAS DOWNTIME                                        │
│  Best for: Development environments only                │
└─────────────────────────────────────────────────────────┘
```

---

### 6. Rollback

```
WHAT: Go back to the previous working version
WHEN: Something goes wrong with the new version
HOW:  Harness does it automatically or you can trigger manually

┌─────────────────────────────────────────────┐
│  AUTOMATIC ROLLBACK                          │
│                                              │
│  Deploy v2 → Health check fails             │
│           → Harness sees failure             │
│           → Automatically deploys v1 back   │
│           → Users never noticed! ✅          │
│                                              │
│  MANUAL ROLLBACK                             │
│                                              │
│  Deploy v2 → Users report bugs              │
│           → You click "Rollback" in Harness │
│           → v1 is back instantly            │
└─────────────────────────────────────────────┘
```

---

## 🖥️ Demo: Deploy Java/Spring Boot to Kubernetes

### Step 1: Create a Service in Harness

1. Go to Project → **Services** → **+ New Service**
2. Fill in:
   - Name: `harness-course-app`
   - Deployment Type: **Kubernetes**
3. Under **Service Definition**:
   - Manifests: Add Kubernetes manifests from Git
   - Artifacts: Docker Hub image

**Service YAML:**
```yaml
service:
  name: harness-course-app
  identifier: harness_course_app
  serviceDefinition:
    type: Kubernetes
    spec:
      manifests:
        - manifest:
            identifier: k8s_manifests
            type: K8sManifest
            spec:
              store:
                type: Github
                spec:
                  connectorRef: github_connector
                  repoName: harness-cicd-sample-app
                  branch: main
                  paths:
                    - k8s/
              skipResourceVersioning: false
      artifacts:
        primary:
          primaryArtifactRef: dockerhub_image
          sources:
            - identifier: dockerhub_image
              spec:
                connectorRef: dockerhub_connector
                imagePath: yourusername/harness-course-app
                tag: <+input>
              type: DockerRegistry
```

### Step 2: Create an Environment

1. Go to **Environments** → **+ New Environment**
2. Fill in:
   - Name: `development`
   - Type: **Pre-Production**

3. Create another:
   - Name: `production`
   - Type: **Production**

### Step 3: Create Infrastructure Definition

1. Inside the `development` environment:
2. **+ New Infrastructure** → **Kubernetes**
3. Fill in:
   - Name: `dev-k8s-cluster`
   - Connector: `k8s-connector`
   - Namespace: `harness-course-dev`

### Step 4: Create CD Pipeline

```yaml
# .harness/cd-pipeline.yaml
pipeline:
  name: Deploy to Kubernetes
  identifier: deploy_to_k8s
  projectIdentifier: harness_course
  orgIdentifier: learning

  stages:
    - stage:
        name: Deploy to Dev
        identifier: deploy_dev
        type: Deployment
        spec:
          deploymentType: Kubernetes
          service:
            serviceRef: harness_course_app
            serviceInputs:
              serviceDefinition:
                type: Kubernetes
                spec:
                  artifacts:
                    primary:
                      primaryArtifactRef: dockerhub_image
                      sources:
                        - identifier: dockerhub_image
                          type: DockerRegistry
                          spec:
                            tag: <+input>
          environment:
            environmentRef: development
            deployToAll: false
            infrastructureDefinitions:
              - identifier: dev_k8s_cluster
          execution:
            steps:
              - step:
                  name: Rolling Deployment
                  identifier: rolling
                  type: K8sRollingDeploy
                  timeout: 10m
                  spec:
                    skipDryRun: false
                    pruningEnabled: false
            rollbackSteps:
              - step:
                  name: Rollback
                  identifier: rollback
                  type: K8sRollingRollback
                  timeout: 10m
                  spec:
                    pruningEnabled: false
```

### Step 5: Run the Deployment!

1. Click **Run** on the pipeline
2. Enter the image tag (from your CI build)
3. Watch Harness:
   - Connect to your cluster
   - Apply the Kubernetes manifests
   - Wait for pods to be healthy
   - Show you the deployment status

```
Expected Output:
════════════════
✅ Namespace created
✅ ConfigMap applied
✅ Secret applied
✅ Deployment applied (3 replicas)
✅ Service created
✅ All pods healthy
✅ Deployment successful!
```

---

### Step 6: Verify the Deployment

```bash
# Check pods are running
kubectl get pods -n harness-course-dev

# Expected:
# NAME                                  READY   STATUS    RESTARTS   AGE
# harness-course-app-xxx-aaa            1/1     Running   0          1m
# harness-course-app-xxx-bbb            1/1     Running   0          1m
# harness-course-app-xxx-ccc            1/1     Running   0          1m

# Check the service
kubectl get svc -n harness-course-dev

# Access the app
curl http://<EXTERNAL-IP>/
# Output: Hello from Harness CI/CD Course!

curl http://<EXTERNAL-IP>/health
# Output: OK
```

---

## 🖥️ Demo: Blue-Green Deployment

```yaml
# Change execution strategy to Blue-Green
execution:
  steps:
    - step:
        name: Blue-Green Deploy
        identifier: bg_deploy
        type: K8sBGSwapServices
        timeout: 10m
        spec:
          skipDryRun: false
    - step:
        name: Swap Primary and Stage
        identifier: swap
        type: K8sBGSwapServices
        timeout: 10m
        spec: {}
  rollbackSteps:
    - step:
        name: BG Rollback
        identifier: bg_rollback
        type: K8sBGSwapServices
        timeout: 10m
        spec: {}
```

---

## 🖥️ Demo: Canary Deployment

```yaml
# Canary deployment strategy
execution:
  steps:
    - step:
        name: Canary 25%
        identifier: canary_25
        type: K8sCanaryDeploy
        timeout: 10m
        spec:
          instanceSelection:
            type: Count
            spec:
              count: 1
          skipDryRun: false
    - step:
        name: Verify Canary
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
    - step:
        name: Canary Full
        identifier: canary_full
        type: K8sCanaryDeploy
        timeout: 10m
        spec:
          instanceSelection:
            type: Percentage
            spec:
              percentage: 100
          skipDryRun: false
    - step:
        name: Canary Delete
        identifier: canary_delete
        type: K8sCanaryDelete
        timeout: 10m
        spec: {}
    - step:
        name: Rolling Deploy (Full)
        identifier: rolling
        type: K8sRollingDeploy
        timeout: 10m
        spec:
          skipDryRun: false
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
```

---

## ✅ Episode 6 Checklist

- [ ] Understand what CD means (deliver the app to users)
- [ ] Know Kubernetes basics (Pod, Deployment, Service, etc.)
- [ ] Created Kubernetes manifests for our app
- [ ] Understand Harness CD concepts (Service, Environment, Infrastructure)
- [ ] Know all 4 deployment strategies and when to use each
- [ ] Understand rollback (automatic + manual)
- [ ] Created a Service in Harness
- [ ] Created Environments (dev + production)
- [ ] Created an Infrastructure Definition
- [ ] Built and ran a CD pipeline
- [ ] Successfully deployed to Kubernetes
- [ ] Verified the deployment works

---

## 📝 Key Takeaways

1. **CD = Deliver your app to users** (CI builds it, CD delivers it)
2. **Rolling** = Safe default (zero downtime, one at a time)
3. **Blue-Green** = Instant switch + instant rollback
4. **Canary** = Safest (test with small % of users first)
5. **Always have rollback steps** in your pipeline
6. **Health checks** are critical (readiness + liveness probes)

---

> 🎬 Next Episode: [Episode 7 - Helm, Amazon EKS & Amazon ECS Deployment](../Episode-07/README.md)
