# 🚀 Deploy Python App — Harness Cloud + Docker Hub

## What We Built

```
python-project/
├── app.py              ← Flask web app
├── test_app.py         ← Unit tests
├── requirements.txt    ← Dependencies
├── Dockerfile          ← Docker image
└── .harness/
    └── pipeline-docker.yaml  ← Harness pipeline
```

---

## Prerequisites

1. Harness account with credit card verified (no charge)
2. GitHub connector created (account level)
3. Docker Hub connector created (account level)
4. Secret `docker-hub-password` created (Docker Hub token)
5. Variable `docker_username` created (Docker Hub username)

---

## Step 2: Push Code to GitHub

```bash
git add .
git commit -m "Episode 2: Python project with Docker Hub push"
git push origin master
```

---

## Step 3: Create Pipeline in Harness

1. Pipelines → Import from Git
2. Connector: Github
3. Repo: Harness-CI-CD-Zero-to-Hero
4. Branch: master
5. YAML Path: Episode-02/python-project/.harness/pipeline-docker.yaml
6. Click Import

---

## Step 4: Run Pipeline

1. Click Run
2. Branch: master
3. Click Run Pipeline
4. Watch 5 steps execute:
   - Install Dependencies ✅
   - Run Tests ✅
   - Run App ✅
   - Build and Push to Docker Hub ✅
   - Verify Push ✅

---

## Expected Output

Step 2 (Run Tests):
```
test_app.py::test_home PASSED
test_app.py::test_health PASSED
test_app.py::test_info PASSED
=== All 3 Tests Passed! ===
```

Step 3 (Run App):
```
{'message': 'Hello from Harness CI/CD Course!', 'episode': 2, ...}
{'status': 'healthy'}
=== App Verified! ===
```

Step 5 (Verify Push):
```
=========================================
  PIPELINE COMPLETE!
  Image pushed to Docker Hub:
  yaswanth111/python-harness-app:1
  yaswanth111/python-harness-app:latest
  View: https://hub.docker.com/r/yaswanth111/python-harness-app
=========================================
```

---

## After Pipeline — Check Docker Hub

Open browser: https://hub.docker.com/r/YOUR-USERNAME/python-harness-app

You will see your image there with tags `1` and `latest`.

---

## Run Locally (to test before pushing)

```bash
cd Episode-02/python-project
pip install -r requirements.txt
pytest test_app.py -v
python app.py
# Open browser: http://localhost:5000
```
