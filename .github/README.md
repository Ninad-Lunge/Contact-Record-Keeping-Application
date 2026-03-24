# GitHub Actions And Runner Setup

This document explains the GitHub Actions workflows in this repository and how to set up the self-hosted runner used for Kubernetes deployment.

## Workflow Overview

This repository uses two workflows:

- `build-images.yml`
- `deploy-k8s.yml`

They are intentionally separated so image creation and Kubernetes deployment can happen independently.

## Workflow 1: Build Images

Path:

- `.github/workflows/build-images.yml`

### What it does

The workflow:

1. validates Kubernetes manifests with `kubectl kustomize`
2. runs backend tests
3. builds the frontend
4. builds Docker images
5. pushes Docker images to Docker Hub

### When it runs

It runs on:

- pull requests to `main` when `backend/**` or `frontend/**` changes
- pushes to `main` when `backend/**` or `frontend/**` changes
- manual workflow dispatch

### Image tags produced

For push and manual runs, the workflow pushes:

- `DOCKERHUB_USERNAME/contact-record-backend:${GITHUB_SHA}`
- `DOCKERHUB_USERNAME/contact-record-backend:latest`
- `DOCKERHUB_USERNAME/contact-record-frontend:${GITHUB_SHA}`
- `DOCKERHUB_USERNAME/contact-record-frontend:latest`

This gives you:

- immutable commit-based tags for reproducible deployments
- a moving `latest` tag for simple manifest-only redeploys

## Workflow 2: Deploy To Kubernetes

Path:

- `.github/workflows/deploy-k8s.yml`

### What it does

The workflow:

1. checks out the repository
2. verifies `kubectl` access
3. applies `k8s/`
4. updates backend and frontend deployment images
5. waits for rollout

### When it runs

It runs on:

- pushes to `main` when files under `k8s/**` change
- successful completion of the `Build Images` workflow
- manual workflow dispatch

### How image selection works

If triggered by:

- `workflow_run` from `Build Images`
  - it deploys the exact image tag matching `github.event.workflow_run.head_sha`
- `push` for `k8s/**`
  - it deploys `latest`
- manual dispatch
  - it deploys the `image_tag` you provide

That means:

- code changes build new images and then deploy the matching SHA
- manifest-only changes deploy the current `latest`
- manual deployments can roll out any tag you choose

## Why A Self-Hosted Runner Is Required

The Kubernetes cluster is running on Multipass and is typically reachable only from the local VM network or from the master node itself.

GitHub-hosted runners usually cannot reach:

- private `10.x.x.x` VM addresses
- local kubeconfig contexts on your machine
- private cluster networks

Because of that, Kubernetes deployment runs on a self-hosted runner installed on the Multipass `master` node.

## Required Repository Secrets

Add these secrets in:

- `GitHub -> Repository -> Settings -> Secrets and variables -> Actions`

Required secrets:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

Notes:

- `DOCKERHUB_TOKEN` should be a Docker Hub access token
- do not use your Docker Hub account password directly

## Self-Hosted Runner Setup

### Prerequisites on the master node

Before installing the runner, the master VM should already have:

- `kubectl`
- cluster admin access via kubeconfig
- Git installed
- internet access to GitHub

Verify:

```bash
kubectl get nodes
git --version
```

### Install the runner

On the master VM:

1. open the GitHub repository in a browser
2. go to `Settings -> Actions -> Runners`
3. click `New self-hosted runner`
4. choose:
   - `Linux`
   - `x64`
5. run the commands GitHub provides

That usually looks like:

```bash
mkdir actions-runner && cd actions-runner
curl -o actions-runner-linux-x64.tar.gz -L <runner-download-url>
tar xzf ./actions-runner-linux-x64.tar.gz
./config.sh --url https://github.com/<owner>/<repo> --token <token>
./run.sh
```

### Run the runner as a service

Do not leave the runner attached to an interactive shell long-term.

Install and start it as a service:

```bash
sudo ./svc.sh install
sudo ./svc.sh start
```

Check status:

```bash
sudo ./svc.sh status
```

### Important runner requirement

The runner service must run as a user that can execute:

```bash
kubectl get nodes
```

successfully without manual intervention.

If `kubectl` works in your shell but not in the runner, the most common cause is kubeconfig not being available to the runner service account.

## Recommended First-Time Validation

After the runner is installed:

1. manually trigger `Deploy To Kubernetes`
2. verify the workflow can run:
   - `kubectl get nodes`
   - `kubectl apply -k k8s/`
   - `kubectl rollout status ...`

Then:

1. make a small backend or frontend change
2. push to `main`
3. confirm `Build Images` runs
4. confirm `Deploy To Kubernetes` follows automatically

## Common Usage Patterns

### Case 1: Backend or frontend code change

Example:

- Spring Boot code
- Angular code
- Dockerfile changes

What happens:

1. `Build Images` runs
2. new Docker images are pushed
3. `Deploy To Kubernetes` runs automatically
4. the cluster deploys the matching commit SHA image tags

### Case 2: Kubernetes manifest-only change

Example:

- replicas
- resources
- ingress
- configmaps
- services

What happens:

1. `Build Images` does not run
2. `Deploy To Kubernetes` runs on `k8s/**` push
3. the cluster redeploys using the `latest` image tag

### Case 3: Manual rollback or pinned deployment

Run `Deploy To Kubernetes` manually and provide:

- `image_tag=latest`
- or `image_tag=<commit-sha>`

This is useful when:

- you want to redeploy a known good image
- you changed only manifests
- you want to roll back to a previous image

## Troubleshooting

### Build workflow fails on Docker push

Check:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- Docker Hub repository permissions

### Deploy workflow starts but fails on kubectl

Check on the runner machine:

```bash
kubectl get nodes
kubectl config current-context
```

Likely causes:

- kubeconfig missing for the runner user
- wrong context selected
- runner service started under a different user than expected

### Deploy workflow succeeds in GitHub but app does not update

Check:

```bash
kubectl get pods -n contact-record
kubectl describe deployment backend -n contact-record
kubectl describe deployment frontend -n contact-record
kubectl rollout status deployment/backend -n contact-record
kubectl rollout status deployment/frontend -n contact-record
```

### Manifest-only deploy used the wrong image

That usually means `latest` is not the image you expected.

Use manual dispatch with a specific `image_tag` instead of relying on `latest`.

## Operational Notes

- Keep cluster secrets out of GitHub workflows unless you intentionally move to secret management through GitHub or an external secret operator.
- The deploy workflow assumes `backend-secret` and `postgres-secret` already exist in the cluster.
- The deploy workflow updates deployment images at runtime, so your checked-in manifests do not need to carry the newest SHA-based image tag.

## Related Files

- [build-images.yml](/home/ninad/Projects/Contact-Record-Keeping-Application/.github/workflows/build-images.yml)
- [deploy-k8s.yml](/home/ninad/Projects/Contact-Record-Keeping-Application/.github/workflows/deploy-k8s.yml)
- [k8s/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/README.md)
