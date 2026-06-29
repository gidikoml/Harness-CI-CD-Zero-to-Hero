# Episode 7: Helm, Amazon EKS & Amazon ECS Deployment

## 🎯 Goal
Deploy using Helm charts to AWS EKS and also deploy to AWS ECS.
Like learning to deliver pizza by car (Helm+EKS) AND by drone (ECS).

---

## 📚 Topics Covered

### 1. What is Helm?

```
┌─────────────────────────────────────────────────────────┐
│  HELM = Package Manager for Kubernetes                   │
│                                                          │
│  Think of it like:                                       │
│  • apt-get for Ubuntu                                   │
│  • npm for Node.js                                      │
│  • pip for Python                                       │
│  • BUT for Kubernetes!                                   │
│                                                          │
│  Without Helm:                                           │
│    Deploy 1 app = 5-10 YAML files to manage manually    │
│    Deploy 20 apps = 100-200 YAML files 😫               │
│                                                          │
│  With Helm:                                              │
│    Deploy 1 app = 1 command: helm install myapp          │
│    Deploy 20 apps = 20 commands ✅                       │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

### 2. Helm Chart Structure

```
my-helm-chart/
├── Chart.yaml          ← Chart metadata (name, version)
├── values.yaml         ← Default configuration values
├── templates/          ← Kubernetes YAML templates
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── ingress.yaml
│   ├── hpa.yaml
│   └── _helpers.tpl   ← Reusable template snippets
└── charts/             ← Dependencies (other charts)
```

---

### 3. Chart.yaml

```yaml
# Chart.yaml - The "ID card" of your chart
apiVersion: v2
name: harness-course-app
description: A Helm chart for the Harness CI/CD course application
type: application
version: 1.0.0        # Chart version (changes when chart changes)
appVersion: "1.0.0"   # App version (your actual app version)
maintainers:
  - name: Yaswanth Reddy
    email: your@email.com
```

---

### 4. values.yaml

```yaml
# values.yaml - The "settings panel" of your chart
# Change these values WITHOUT changing the templates!

replicaCount: 3

image:
  repository: yourusername/harness-course-app
  tag: "latest"
  pullPolicy: IfNotPresent

service:
  type: LoadBalancer
  port: 80
  targetPort: 8080

resources:
  requests:
    memory: "256Mi"
    cpu: "250m"
  limits:
    memory: "512Mi"
    cpu: "500m"

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70

ingress:
  enabled: false
  className: "nginx"
  host: app.example.com

env:
  APP_ENV: "production"
  LOG_LEVEL: "info"

health:
  readinessPath: /health
  livenessPath: /health
```

**The MAGIC of values.yaml:**
```
Same chart, different values = Different environments!

values-dev.yaml:
  replicaCount: 1
  image.tag: "dev-latest"

values-staging.yaml:
  replicaCount: 2
  image.tag: "rc-1.2.3"

values-prod.yaml:
  replicaCount: 5
  image.tag: "1.2.3"
```

---

### 5. Templates

#### templates/deployment.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "harness-course-app.fullname" . }}
  labels:
    {{- include "harness-course-app.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.replicaCount }}
  selector:
    matchLabels:
      {{- include "harness-course-app.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "harness-course-app.selectorLabels" . | nindent 8 }}
    spec:
      containers:
        - name: {{ .Chart.Name }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - containerPort: {{ .Values.service.targetPort }}
          env:
            {{- range $key, $value := .Values.env }}
            - name: {{ $key }}
              value: {{ $value | quote }}
            {{- end }}
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
          readinessProbe:
            httpGet:
              path: {{ .Values.health.readinessPath }}
              port: {{ .Values.service.targetPort }}
            initialDelaySeconds: 10
            periodSeconds: 5
          livenessProbe:
            httpGet:
              path: {{ .Values.health.livenessPath }}
              port: {{ .Values.service.targetPort }}
            initialDelaySeconds: 30
            periodSeconds: 10
```

#### templates/service.yaml
```yaml
apiVersion: v1
kind: Service
metadata:
  name: {{ include "harness-course-app.fullname" . }}
  labels:
    {{- include "harness-course-app.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.port }}
      targetPort: {{ .Values.service.targetPort }}
      protocol: TCP
  selector:
    {{- include "harness-course-app.selectorLabels" . | nindent 4 }}
```

#### templates/_helpers.tpl
```yaml
{{- define "harness-course-app.fullname" -}}
{{- .Release.Name }}-{{ .Chart.Name }}
{{- end }}

{{- define "harness-course-app.labels" -}}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version }}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "harness-course-app.selectorLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
```

---

### 6. Helm Commands

```bash
# Install a chart (first time)
helm install my-release ./my-helm-chart

# Upgrade a release (update existing)
helm upgrade my-release ./my-helm-chart

# Install OR upgrade (best practice)
helm upgrade --install my-release ./my-helm-chart

# Use custom values file
helm upgrade --install my-release ./my-helm-chart -f values-prod.yaml

# Override a single value
helm upgrade --install my-release ./my-helm-chart --set image.tag=1.2.3

# Rollback to previous version
helm rollback my-release 1

# List all releases
helm list

# Uninstall
helm uninstall my-release
```

---

### 7. Amazon EKS (Elastic Kubernetes Service)

```
┌─────────────────────────────────────────────────────────┐
│  AMAZON EKS = AWS manages Kubernetes for you            │
│                                                          │
│  Without EKS:                                           │
│    You set up Kubernetes yourself (HARD)                │
│    You manage master nodes (EXPENSIVE)                  │
│    You handle upgrades (SCARY)                          │
│                                                          │
│  With EKS:                                              │
│    AWS manages the control plane ✅                     │
│    You just add worker nodes ✅                         │
│    AWS handles upgrades ✅                              │
│    Built-in security ✅                                 │
│                                                          │
│  Think of EKS like renting an apartment:               │
│    Landlord (AWS) = maintains the building              │
│    You = decorate your apartment (deploy apps)          │
└─────────────────────────────────────────────────────────┘
```

---

### 8. IAM for EKS

```
┌─────────────────────────────────────────────────────────┐
│  IAM ROLES NEEDED                                        │
│                                                          │
│  1. EKS Cluster Role                                    │
│     → Allows EKS to manage AWS resources                │
│     → Policy: AmazonEKSClusterPolicy                    │
│                                                          │
│  2. Node Group Role                                     │
│     → Allows worker nodes to join cluster               │
│     → Policies:                                         │
│       - AmazonEKSWorkerNodePolicy                       │
│       - AmazonEKS_CNI_Policy                            │
│       - AmazonEC2ContainerRegistryReadOnly              │
│                                                          │
│  3. Harness Delegate Role (for deployments)             │
│     → Allows Harness to deploy to EKS                  │
│     → Custom policy with EKS access                    │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

**IAM Policy for Harness (minimum):**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "eks:DescribeCluster",
        "eks:ListClusters",
        "eks:AccessKubernetesApi"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "ecr:GetAuthorizationToken",
        "ecr:BatchCheckLayerAvailability",
        "ecr:GetDownloadUrlForLayer",
        "ecr:BatchGetImage"
      ],
      "Resource": "*"
    }
  ]
}
```

---

### 9. EKS Connector in Harness

```yaml
# How Harness connects to EKS
connector:
  name: eks-connector
  type: Kubernetes
  spec:
    # Option 1: Inherit from Delegate (if delegate runs IN EKS)
    credential:
      type: InheritFromDelegate
      spec:
        delegateSelectors:
          - my-k8s-delegate

    # Option 2: Direct connection with credentials
    credential:
      type: ManualConfig
      spec:
        masterUrl: https://YOUR-EKS-ENDPOINT.eks.amazonaws.com
        auth:
          type: ServiceAccount
          spec:
            serviceAccountTokenSecret: eks-sa-token
```

---

### 10. Amazon ECS (Elastic Container Service)

```
┌─────────────────────────────────────────────────────────┐
│  AMAZON ECS = Run containers WITHOUT Kubernetes         │
│                                                          │
│  EKS = You still deal with Kubernetes concepts          │
│  ECS = AWS handles EVERYTHING, you just provide image   │
│                                                          │
│  ECS Components:                                        │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Cluster     = Group of containers               │    │
│  │  Service     = Keeps your app running            │    │
│  │  Task        = One running copy of your app      │    │
│  │  Task Def    = Blueprint for your container      │    │
│  │  Fargate     = Serverless (no servers to manage!)│    │
│  └─────────────────────────────────────────────────┘    │
│                                                          │
│  Think of ECS like ordering food delivery:              │
│  Task Definition = Your order (what you want)           │
│  Task = The actual food being prepared                   │
│  Service = Auto-reorder when food runs out              │
│  Fargate = Kitchen (you don't manage it)               │
└─────────────────────────────────────────────────────────┘
```

---

### 11. ECS Task Definition

```json
{
  "family": "harness-course-app",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "executionRoleArn": "arn:aws:iam::123456789012:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "app",
      "image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/harness-course-app:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {"name": "APP_ENV", "value": "production"},
        {"name": "LOG_LEVEL", "value": "info"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/harness-course-app",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3
      }
    }
  ]
}
```

---

### 12. ECS Auto Scaling

```
┌─────────────────────────────────────────────────────────┐
│  AUTO SCALING = Automatically add/remove containers     │
│                                                          │
│  Low traffic (night):   [Task1] [Task2]                 │
│  Normal traffic (day):  [Task1] [Task2] [Task3] [Task4] │
│  High traffic (sale):   [Task1] [Task2] ... [Task10]    │
│                                                          │
│  Rules:                                                  │
│  • Min tasks: 2 (always running)                        │
│  • Max tasks: 10 (cost protection)                      │
│  • Scale up when: CPU > 70%                             │
│  • Scale down when: CPU < 30%                           │
└─────────────────────────────────────────────────────────┘
```

---

## 🖥️ Demo: Deploy to EKS with Helm

### Step 1: Create EKS Cluster (if you don't have one)

```bash
# Using eksctl (easiest way)
eksctl create cluster \
  --name harness-course-cluster \
  --region us-east-1 \
  --nodegroup-name workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 3

# Verify
kubectl get nodes
# NAME                           STATUS   ROLES    AGE   VERSION
# ip-192-168-xx-xx.ec2.internal  Ready    <none>   5m    v1.28
# ip-192-168-yy-yy.ec2.internal  Ready    <none>   5m    v1.28
```

### Step 2: Create Helm Chart for Our App

Place the helm chart in your repository:
```
harness-cicd-sample-app/
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
```

### Step 3: Configure Harness Service with Helm

```yaml
service:
  name: harness-course-app-helm
  identifier: harness_course_app_helm
  serviceDefinition:
    type: Kubernetes
    spec:
      manifests:
        - manifest:
            identifier: helm_chart
            type: HelmChart
            spec:
              store:
                type: Github
                spec:
                  connectorRef: github_connector
                  repoName: harness-cicd-sample-app
                  branch: main
                  folderPath: helm/harness-course-app
              chartVersion: ""
              helmVersion: V3
              skipResourceVersioning: false
              valuesPaths:
                - values-prod.yaml
      artifacts:
        primary:
          primaryArtifactRef: ecr_image
          sources:
            - identifier: ecr_image
              spec:
                connectorRef: aws_connector
                region: us-east-1
                imagePath: harness-course-app
                tag: <+input>
              type: Ecr
```

### Step 4: CD Pipeline with Helm

```yaml
pipeline:
  name: Deploy to EKS with Helm
  identifier: deploy_eks_helm
  stages:
    - stage:
        name: Deploy to EKS
        identifier: deploy_eks
        type: Deployment
        spec:
          deploymentType: Kubernetes
          service:
            serviceRef: harness_course_app_helm
          environment:
            environmentRef: production
            infrastructureDefinitions:
              - identifier: eks_production
          execution:
            steps:
              - step:
                  name: Helm Deploy
                  identifier: helm_deploy
                  type: K8sRollingDeploy
                  timeout: 10m
                  spec:
                    skipDryRun: false
            rollbackSteps:
              - step:
                  name: Helm Rollback
                  identifier: helm_rollback
                  type: K8sRollingRollback
                  timeout: 10m
                  spec: {}
```

---

## 🖥️ Demo: Deploy to ECS

### Step 1: Harness ECS Service

```yaml
service:
  name: harness-course-app-ecs
  identifier: harness_course_app_ecs
  serviceDefinition:
    type: ECS
    spec:
      manifests:
        - manifest:
            identifier: task_definition
            type: EcsTaskDefinition
            spec:
              store:
                type: Github
                spec:
                  connectorRef: github_connector
                  repoName: harness-cicd-sample-app
                  branch: main
                  paths:
                    - ecs/task-definition.json
        - manifest:
            identifier: service_definition
            type: EcsServiceDefinition
            spec:
              store:
                type: Github
                spec:
                  connectorRef: github_connector
                  repoName: harness-cicd-sample-app
                  branch: main
                  paths:
                    - ecs/service-definition.json
      artifacts:
        primary:
          primaryArtifactRef: ecr_image
          sources:
            - identifier: ecr_image
              spec:
                connectorRef: aws_connector
                region: us-east-1
                imagePath: harness-course-app
                tag: <+input>
              type: Ecr
```

### Step 2: ECS CD Pipeline

```yaml
pipeline:
  name: Deploy to ECS
  identifier: deploy_ecs
  stages:
    - stage:
        name: Deploy to ECS Fargate
        identifier: deploy_ecs_fargate
        type: Deployment
        spec:
          deploymentType: ECS
          service:
            serviceRef: harness_course_app_ecs
          environment:
            environmentRef: production
            infrastructureDefinitions:
              - identifier: ecs_production
          execution:
            steps:
              - step:
                  name: ECS Rolling Deploy
                  identifier: ecs_deploy
                  type: EcsRollingDeploy
                  timeout: 10m
                  spec: {}
            rollbackSteps:
              - step:
                  name: ECS Rollback
                  identifier: ecs_rollback
                  type: EcsRollingRollback
                  timeout: 10m
                  spec: {}
```

---

## ✅ Episode 7 Checklist

- [ ] Understand what Helm is (package manager for K8s)
- [ ] Know the Helm chart structure
- [ ] Can write Chart.yaml, values.yaml, and templates
- [ ] Know basic Helm commands (install, upgrade, rollback)
- [ ] Understand Amazon EKS and why it exists
- [ ] Know IAM roles needed for EKS
- [ ] Created EKS connector in Harness
- [ ] Understand Amazon ECS concepts (Task, Service, Fargate)
- [ ] Know ECS Task Definition structure
- [ ] Deployed app to EKS using Helm
- [ ] Deployed app to ECS using Fargate
- [ ] Understand auto-scaling in ECS

---

## 📝 Key Takeaways

1. **Helm = Package manager** for Kubernetes (makes deployments repeatable)
2. **values.yaml = The settings** you change per environment
3. **EKS = AWS-managed Kubernetes** (you focus on apps, not cluster management)
4. **ECS = Simpler alternative** to Kubernetes (good for straightforward apps)
5. **Fargate = Serverless containers** (no EC2 instances to manage)
6. **Same app, two platforms** = Flexibility to choose what fits

---

> 🎬 Next Episode: [Episode 8 - Enterprise Security & Governance](../Episode-08/README.md)
