# Episode 1: Introduction to Harness & Enterprise CI/CD

## 🎯 Goal
Understand WHY Harness exists before using it.
Think of it like learning WHY we need a car before learning to drive.

---

## 📚 Topics Covered

### 1. What is CI/CD? (Explained Like You're 5)

Imagine you're building with LEGO blocks:

```
You build something (CODE)
    ⬇️
You check if pieces fit together (CI - Continuous Integration)
    ⬇️
You show it to your friends (CD - Continuous Delivery)
    ⬇️
Your friends can play with it (DEPLOYMENT)
```

**In Real Life:**
- Developer writes code
- Code is automatically tested (CI)
- Code is automatically packaged (CI)
- Code is automatically deployed to servers (CD)
- Users can use the new features

---

### 2. Evolution of CI/CD

```
Era 1: Manual (2000s)
┌─────────────────────────────────────────┐
│  Developer → FTP Upload → Server        │
│  Problem: Human errors, slow, scary     │
└─────────────────────────────────────────┘

Era 2: Jenkins (2011+)
┌─────────────────────────────────────────┐
│  Developer → Jenkins → Server           │
│  Better but: Complex, plugin hell       │
└─────────────────────────────────────────┘

Era 3: Cloud CI/CD (2018+)
┌─────────────────────────────────────────┐
│  Developer → GitHub Actions/GitLab CI   │
│  Good but: Limited for enterprise       │
└─────────────────────────────────────────┘

Era 4: Enterprise Platforms (2020+)
┌─────────────────────────────────────────┐
│  Developer → Harness → Any Cloud        │
│  Built for enterprise, AI-powered       │
└─────────────────────────────────────────┘
```

---

### 3. Problems with Jenkins

| Problem | Explanation |
|---------|-------------|
| Plugin Hell | Need 100+ plugins, they break each other |
| No UI | Groovy scripting is complex |
| No Security | No built-in secrets, scanning |
| No Governance | No approval workflows built-in |
| Scaling Issues | Single server bottleneck |
| No Cloud Native | Not designed for Kubernetes |
| Maintenance | You manage everything yourself |

---

### 4. Why Enterprises Choose Harness

| Feature | Jenkins | Harness |
|---------|---------|---------|
| Setup Time | Weeks | Minutes |
| Pipeline as Code | Groovy (complex) | YAML (simple) |
| Security Scanning | Plugins | Built-in |
| Kubernetes | Plugins | Native |
| Approval Gates | Plugins | Built-in |
| Rollback | Manual scripts | Automatic |
| Cost Management | Nothing | Built-in CCM |
| AI/ML | Nothing | Built-in |

---

### 5. CI vs CD vs GitOps

```
┌──────────────────────────────────────────────────┐
│                                                  │
│  CI (Continuous Integration)                     │
│  ════════════════════════════                    │
│  • Build the code                                │
│  • Run tests                                     │
│  • Create Docker image                           │
│  • Scan for vulnerabilities                      │
│  • Push image to registry                        │
│                                                  │
├──────────────────────────────────────────────────┤
│                                                  │
│  CD (Continuous Delivery/Deployment)             │
│  ════════════════════════════════════            │
│  • Take the image from CI                        │
│  • Deploy to Kubernetes/ECS/VMs                  │
│  • Run health checks                            │
│  • Canary/Blue-Green deployments                 │
│  • Rollback if something breaks                  │
│                                                  │
├──────────────────────────────────────────────────┤
│                                                  │
│  GitOps                                          │
│  ══════                                          │
│  • Git is the single source of truth             │
│  • Change Git → Cluster auto-updates             │
│  • No manual kubectl commands                    │
│  • Uses Argo CD under the hood                   │
│                                                  │
└──────────────────────────────────────────────────┘
```

---

### 6. Harness Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    HARNESS PLATFORM                       │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │  Harness │  │  Harness │  │  Harness │             │
│  │    CI    │  │    CD    │  │  GitOps  │             │
│  └──────────┘  └──────────┘  └──────────┘             │
│                                                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │    FF    │  │   CCM    │  │   STO    │             │
│  │ Feature  │  │  Cloud   │  │ Security │             │
│  │  Flags   │  │  Cost    │  │ Testing  │             │
│  └──────────┘  └──────────┘  └──────────┘             │
│                                                          │
├─────────────────────────────────────────────────────────┤
│                    HARNESS DELEGATE                       │
│         (Runs in YOUR infrastructure)                    │
│                                                          │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Connects Harness Cloud ←→ Your Servers          │    │
│  │  Think of it as a "messenger" between them       │    │
│  └─────────────────────────────────────────────────┘    │
│                                                          │
├─────────────────────────────────────────────────────────┤
│              YOUR INFRASTRUCTURE                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │Kubernetes│  │   AWS    │  │  Azure   │             │
│  └──────────┘  └──────────┘  └──────────┘             │
└─────────────────────────────────────────────────────────┘
```

---

### 7. SaaS vs Self-Managed

| | SaaS (Cloud) | Self-Managed |
|---|---|---|
| Who hosts it? | Harness | You |
| Setup | 2 minutes | Days/Weeks |
| Maintenance | Harness team | Your team |
| Updates | Automatic | Manual |
| Best for | Most companies | Banks, Government |
| Cost | Subscription | License + Infrastructure |
| We use | ✅ This one | |

---

### 8. Harness Modules Overview

```
┌─────────────────────────────────────────────┐
│            HARNESS MODULES                    │
├─────────────────────────────────────────────┤
│                                              │
│  CI   → Build & Test code                   │
│  CD   → Deploy to servers                   │
│  FF   → Feature Flags (toggle features)     │
│  CCM  → Cloud Cost Management               │
│  STO  → Security Testing                    │
│  CE   → Chaos Engineering                   │
│  SRM  → Service Reliability Management      │
│  IDP  → Internal Developer Portal           │
│                                              │
│  In this course we focus on:                │
│  ✅ CI  ✅ CD  ✅ GitOps  ✅ STO           │
└─────────────────────────────────────────────┘
```

---

### 9. Course Roadmap

```
Episode 1:  Learn WHAT Harness is          ← YOU ARE HERE
Episode 2:  Learn HOW Harness is organized
Episode 3:  Connect Harness to your tools
Episode 4:  Build your FIRST CI pipeline
Episode 5:  Add security scanning
Episode 6:  Deploy to Kubernetes
Episode 7:  Deploy to AWS (EKS + ECS)
Episode 8:  Add enterprise security
Episode 9:  GitOps + Monitoring
Episode 10: Build EVERYTHING together
```

---

## 🖥️ Demo: Create Harness Account & Explore Dashboard

### Step 1: Create Free Harness Account

1. Go to: https://app.harness.io/auth/#/signup
2. Sign up with:
   - Google account (easiest) OR
   - GitHub account OR
   - Email + Password
3. Choose "Continuous Integration" when asked what you want to try
4. You now have a **14-day free trial** of all features

### Step 2: Explore the Dashboard

Once logged in, you'll see:

```
┌─────────────────────────────────────────────────┐
│  HARNESS DASHBOARD                               │
├────────┬────────────────────────────────────────┤
│        │                                         │
│  Home  │   Welcome to Harness!                  │
│        │                                         │
│  CI    │   ┌─────────────────────────────────┐  │
│        │   │  Quick Links:                    │  │
│  CD    │   │  • Create Pipeline               │  │
│        │   │  • Create Project                │  │
│  GitOps│   │  • Install Delegate              │  │
│        │   └─────────────────────────────────┘  │
│  STO   │                                         │
│        │   Recent Activity:                      │
│  FF    │   (empty - we haven't done anything)   │
│        │                                         │
│  CCM   │                                         │
│        │                                         │
└────────┴────────────────────────────────────────┘
```

### Step 3: Key Areas to Explore

| Area | What it is | Where to find it |
|------|-----------|------------------|
| Account Settings | Your account config | Top-right gear icon |
| Projects | Organize your work | Left sidebar |
| Delegates | Connectors to your infra | Project Settings |
| Connectors | Links to GitHub, AWS, etc. | Project Settings |
| Pipelines | Your CI/CD workflows | Left sidebar under CI/CD |

---

## ✅ Episode 1 Checklist

- [ ] Understand what CI/CD means
- [ ] Know why Jenkins has problems
- [ ] Know why enterprises choose Harness
- [ ] Understand CI vs CD vs GitOps difference
- [ ] Know Harness architecture (Platform + Delegate)
- [ ] Know the difference between SaaS and Self-Managed
- [ ] Created a free Harness account
- [ ] Explored the Harness dashboard

---

## 📝 Key Takeaways

1. **Harness = Enterprise CI/CD Platform** (like Jenkins but 100x better)
2. **Delegate = Messenger** between Harness cloud and your servers
3. **SaaS = Harness hosts everything** (we use this)
4. **CI = Build & Test**, **CD = Deploy**, **GitOps = Git controls everything**

---

> 🎬 Next Episode: [Episode 2 - Harness Fundamentals](../Episode-02/README.md)
