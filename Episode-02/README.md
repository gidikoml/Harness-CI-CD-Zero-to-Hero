# Episode 2: Harness Fundamentals

## 🎯 Goal
Learn how Harness is organized — like learning the rooms in a house before decorating.

---

## 📚 Topics Covered

### 1. Harness Hierarchy (The Organization Structure)

Think of it like a company:

```
┌─────────────────────────────────────────────────┐
│  ACCOUNT (Your Company)                          │
│  ├── ORGANIZATION (Department)                   │
│  │   ├── PROJECT (Team's Work)                  │
│  │   │   ├── Pipeline 1                         │
│  │   │   ├── Pipeline 2                         │
│  │   │   ├── Connectors                         │
│  │   │   ├── Secrets                            │
│  │   │   └── Services                           │
│  │   │                                           │
│  │   └── PROJECT 2 (Another Team)              │
│  │                                               │
│  └── ORGANIZATION 2 (Another Department)        │
└─────────────────────────────────────────────────┘
```

**Real World Example:**
```
Account: "TechCorp"
├── Organization: "Engineering"
│   ├── Project: "Payment Service"
│   ├── Project: "User Service"
│   └── Project: "Notification Service"
│
└── Organization: "Data Team"
    ├── Project: "Analytics Pipeline"
    └── Project: "ML Models"
```

---

### 2. Account

```
┌─────────────────────────────────────┐
│  ACCOUNT                             │
│  ═══════                             │
│                                      │
│  • Top-level container               │
│  • Created when you sign up          │
│  • Has a unique Account ID           │
│  • Billing happens here              │
│  • Global settings live here         │
│                                      │
│  Think of it as: YOUR ENTIRE COMPANY │
└─────────────────────────────────────┘
```

---

### 3. Organization

```
┌─────────────────────────────────────┐
│  ORGANIZATION                        │
│  ════════════                        │
│                                      │
│  • Groups related projects           │
│  • Has its own settings              │
│  • Can share resources between       │
│    projects in same org              │
│                                      │
│  Think of it as: A DEPARTMENT        │
│  Example: "Engineering", "DevOps"    │
└─────────────────────────────────────┘
```

---

### 4. Project

```
┌─────────────────────────────────────┐
│  PROJECT                             │
│  ═══════                             │
│                                      │
│  • Where all the action happens      │
│  • Contains pipelines, connectors    │
│  • Team members work here            │
│  • Has its own settings              │
│                                      │
│  Think of it as: A TEAM'S WORKSPACE  │
│  Example: "my-java-app"             │
└─────────────────────────────────────┘
```

---

### 5. RBAC (Role-Based Access Control)

**What is RBAC?** = Who can do WHAT in your Harness account

Think of it like a hotel:
```
┌─────────────────────────────────────────────────┐
│  HOTEL ANALOGY                                    │
│                                                   │
│  Guest (Viewer)                                  │
│  → Can look around, but can't change anything    │
│                                                   │
│  Staff (Developer)                               │
│  → Can access rooms, clean, arrange              │
│                                                   │
│  Manager (Admin)                                 │
│  → Can hire/fire staff, change rules             │
│                                                   │
│  Owner (Account Admin)                           │
│  → Can do EVERYTHING including sell the hotel    │
└─────────────────────────────────────────────────┘
```

**In Harness:**
```
Account Admin  → Everything
Org Admin      → Everything in that organization
Project Admin  → Everything in that project
Pipeline Exec  → Can only run pipelines
Viewer         → Can only look, not touch
```

---

### 6. Users

```
How to Add Users:
─────────────────

Account Settings → Access Control → Users → + New User

┌─────────────────────────────────────┐
│  Add User                            │
│                                      │
│  Email: developer@company.com        │
│  Role:  [Project Viewer ▼]          │
│  Project: [my-java-app ▼]           │
│                                      │
│  [Send Invitation]                   │
└─────────────────────────────────────┘
```

---

### 7. Roles

| Role | Can Do |
|------|--------|
| Account Admin | Everything |
| Account Viewer | View everything, change nothing |
| Org Admin | Manage organization |
| Project Admin | Manage project |
| Pipeline Executor | Run pipelines |
| Deployment Viewer | See deployments |

**Custom Roles:** You can create your own!
Example: "Junior Developer" → Can run pipelines but not edit them

---

### 8. Resource Groups

```
Resource Groups = WHAT resources a role can access

Example:
┌─────────────────────────────────────────┐
│  Resource Group: "Production Resources"  │
│                                          │
│  Includes:                               │
│  ✅ Production pipelines                 │
│  ✅ Production environments              │
│  ❌ Development pipelines                │
│  ❌ Development environments             │
│                                          │
│  Assigned to: "Senior DevOps" role       │
└─────────────────────────────────────────┘
```

**RBAC Formula:**
```
WHO (User) + WHAT (Role) + WHERE (Resource Group) = ACCESS
```

---

### 9. API Keys

```
┌─────────────────────────────────────────────┐
│  API KEYS                                     │
│  ════════                                     │
│                                               │
│  What: A password for machines/scripts        │
│  Why:  Automate Harness without the UI        │
│  Where: Account Settings → API Keys           │
│                                               │
│  Types:                                       │
│  • Personal Access Token (PAT)               │
│    → Tied to YOUR account                     │
│    → Use for testing/development              │
│                                               │
│  • Service Account Token (SAT)               │
│    → Not tied to any person                   │
│    → Use for automation/CI systems            │
│                                               │
│  ⚠️  NEVER share API keys or commit to Git!  │
└─────────────────────────────────────────────┘
```

---

### 10. Delegates

```
┌─────────────────────────────────────────────────────┐
│                                                      │
│  HARNESS CLOUD  ←────────→  DELEGATE  ←────→  YOUR  │
│  (SaaS)              │                     SERVERS   │
│                      │                               │
│                      │                               │
│  Think of Delegate as a PHONE between                │
│  Harness and your servers.                           │
│                                                      │
│  Without Delegate = Harness can't reach your stuff   │
│  With Delegate = Harness can deploy, build, etc.     │
│                                                      │
└─────────────────────────────────────────────────────┘
```

(We'll deep-dive into Delegates in Episode 3)

---

### 11. Connectors

```
Connectors = How Harness connects to external tools

┌──────────────┬─────────────────────────┐
│  Connector   │  What it connects to     │
├──────────────┼─────────────────────────┤
│  GitHub      │  Your code repository    │
│  Docker Hub  │  Your Docker images      │
│  AWS         │  Amazon Web Services     │
│  Kubernetes  │  Your K8s cluster        │
│  GCP         │  Google Cloud            │
│  Azure       │  Microsoft Azure         │
└──────────────┴─────────────────────────┘
```

(We'll set these up in Episode 3)

---

### 12. Secrets

```
┌─────────────────────────────────────────┐
│  SECRETS                                  │
│  ═══════                                  │
│                                           │
│  What: Sensitive values (passwords, keys)│
│  Why:  Keep them safe, never in code      │
│  Where: Project Settings → Secrets        │
│                                           │
│  Types:                                   │
│  • Text Secret    → passwords, tokens     │
│  • File Secret    → certificates, keys    │
│  • SSH Key        → server access         │
│                                           │
│  How to use in pipeline:                  │
│  <+secrets.getValue("my-secret")>         │
│                                           │
└─────────────────────────────────────────┘
```

---

### 13. Variables

```
┌─────────────────────────────────────────────┐
│  VARIABLES                                    │
│  ═════════                                    │
│                                               │
│  What: Reusable values (not secret)           │
│  Why:  Don't repeat yourself                  │
│                                               │
│  Levels:                                      │
│  • Account Variable → Available everywhere    │
│  • Org Variable     → Available in that org   │
│  • Project Variable → Available in project    │
│                                               │
│  Example:                                     │
│  Name: "docker_repo"                          │
│  Value: "mycompany/myapp"                     │
│                                               │
│  Use in pipeline:                             │
│  <+variable.docker_repo>                      │
│                                               │
└─────────────────────────────────────────────┘
```

---

## 🖥️ Demo: Build a Complete Harness Project from Scratch

### Step 1: Create an Organization

1. Login to Harness → https://app.harness.io
2. Go to **Account Settings** (gear icon top-right)
3. Click **Organizations** → **+ New Organization**
4. Fill in:
   - Name: `Harness`
   - Description: `My learning organization`
5. Click **Save**

### Step 2: Create a Project

1. Click on your new `learning` organization
2. Click **+ New Project**
3. Fill in:
   - Name: `HarnessCICDZerotoHero`
   - Organization: `learning`
   - Description: `Harness CI/CD course project`
   - Color: Pick any color you like
4. Click **Save**

### Step 3: Add a Secret

**First, create a Docker Hub Access Token:**

1. Open browser → Go to https://app.docker.com/settings/personal-access-tokens
   (OR: Docker Hub → Click avatar top-right → Account Settings → Personal access tokens)
2. Click **Generate new token**
3. Fill in:
   - Token Description: `harness-access`
   - Access permissions: **Read & Write**
4. Click **Generate**
5. COPY THE TOKEN NOW (you cannot see it again!)
   - Token looks like: `dckr_pat_xxxxxxxxxxxxxxxxxxx`
   - Save it somewhere safe temporarily

**Now add it as a Secret in Harness:**

1. Go to Project → **Project Settings** → **Secrets**
2. Click **+ New Secret** → **Text**
3. Fill in:
   - Name: `docker-hub-password`
   - Value: paste your Docker Hub token (`dckr_pat_xxx...`)
4. Click **Save**

### Step 4: Add a Variable

1. Go to Project → **Project Settings** → **Variables**
2. Click **+ New Variable**
3. Fill in:
   - Name: `docker_username`
   - Type: String
   - Value: (your Docker Hub username)
4. Click **Save**

### Step 5: Create Docker Hub Connector

**Connector tells Harness HOW to connect.**

1. Go to **Account Settings** (bottom left) → **Connectors**
2. Click **+ New Connector**
3. Under "Artifact Repositories" → Click **Docker Registry**
4. Fill in:
   - Name: `dockerhub`
   - Provider Type: **Docker Hub**
   - URL: `https://index.docker.io/v2/`
5. Click **Continue**
6. Authentication:
   - Username: your Docker Hub username (e.g. `yaswanth111`)
   - Password: click **Select** → choose your existing secret `docker-hub-password`
7. Click **Continue**
8. Connectivity: **Connect through Harness Platform**
9. Click **Save and Continue**
10. Test Connection → ✅ Success
11. Click **Finish**

### Step 6: Explore RBAC

1. Go to **Project Settings** → **Access Control**
2. See the default roles
3. Try creating a custom role:
   - Name: `Pipeline Runner`
   - Permissions: Only "Pipeline Execute"
4. Click **Save**

---

## ✅ Episode 2 Checklist

- [ ] Understand Account → Organization → Project hierarchy
- [ ] Know what RBAC means and how it works
- [ ] Can explain Users, Roles, and Resource Groups
- [ ] Understand API Keys (PAT vs SAT)
- [ ] Know what Delegates do (messenger)
- [ ] Know what Connectors are (links to tools)
- [ ] Created an Organization
- [ ] Created a Project
- [ ] Added a Secret
- [ ] Added a Variable

---

## 📝 Key Takeaways

1. **Account > Organization > Project** = Company > Department > Team
2. **RBAC** = WHO + WHAT + WHERE = ACCESS
3. **Secrets** = Hidden sensitive values (use `<+secrets.getValue("name")>`)
4. **Variables** = Reusable non-secret values (use `<+variable.name>`)
5. **Delegate** = Messenger between Harness and your servers
6. **Connectors** = Links to GitHub, AWS, Docker, etc.

---

> 🎬 Next Episode: [Episode 3 - Harness Delegate & Connectors](../Episode-03/README.md)
