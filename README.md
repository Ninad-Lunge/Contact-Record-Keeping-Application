# Contact Record Keeping Application

Angular frontend + Spring Boot backend for authenticated contact management.

For the full Kubernetes deployment guide, see [k8s/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/README.md).

## What Changed For Kubernetes

This repository now includes:

- container images for the frontend and backend
- Kubernetes manifests under `k8s/`
- a `kind` cluster config for local WSL use under `kind/`
- externalized backend config for database credentials, JWT secret, and CORS
- Spring Boot actuator health endpoints for readiness and liveness probes
- frontend production routing through `/api` so only the frontend is exposed publicly

## Architecture

For local Kubernetes, the intended flow is:

1. `Ingress` receives browser traffic on `http://contact-record.localtest.me`
2. `frontend` serves the Angular app
3. frontend nginx proxies `/api/*` to the internal `backend` service
4. `backend` connects to the internal `postgres` service

This keeps the backend private inside the cluster and avoids browser-side CORS complexity in production.

## WSL Kubernetes Setup

Recommended local stack:

- WSL 2
- Docker Desktop with WSL integration enabled
- `kubectl` inside your WSL distro
- `kind` inside your WSL distro
- `ingress-nginx` in the cluster

### 1. Install WSL

From an elevated PowerShell on Windows:

```powershell
wsl --install
```

If WSL is already installed, verify the distro is using WSL 2:

```powershell
wsl -l -v
wsl --set-default-version 2
```

### 2. Install Docker Desktop and enable WSL integration

In Docker Desktop:

1. Settings > General > enable `Use WSL 2 based engine`
2. Settings > Resources > WSL Integration > enable your distro
3. Keep Docker Desktop running before using `kind`

Do not install a separate Docker Engine inside the WSL distro when using Docker Desktop's WSL backend.

### 3. Install kubectl in WSL

Inside Ubuntu or your WSL distro:

```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
kubectl version --client
```

### 4. Install kind in WSL

Inside WSL:

```bash
[ "$(uname -m)" = x86_64 ] && curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.31.0/kind-linux-amd64
[ "$(uname -m)" = aarch64 ] && curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.31.0/kind-linux-arm64
chmod +x ./kind
sudo mv ./kind /usr/local/bin/kind
kind version
```

### 5. Create the cluster

From this repository root in WSL:

```bash
kind create cluster --name contact-record --config kind/kind-config.yaml --wait 2m
kubectl cluster-info --context kind-contact-record
```

### 6. Install ingress-nginx

Apply the official `kind` provider manifest:

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.14.3/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s
```

## Build And Load Images

Build the app images from the repo root:

```bash
docker build -t contact-record-backend:local ./backend
docker build -t contact-record-frontend:local ./frontend
```

Load them into the `kind` cluster:

```bash
kind load docker-image contact-record-backend:local --name contact-record
kind load docker-image contact-record-frontend:local --name contact-record
```

The manifests intentionally use explicit non-`latest` tags and `imagePullPolicy: IfNotPresent` so local image loading works reliably with `kind`.

## Configure Secrets

Do not commit real secrets.

Create working copies of the example secret manifests:

```bash
cp k8s/postgres-secret.example.yaml /tmp/postgres-secret.yaml
cp k8s/backend-secret.example.yaml /tmp/backend-secret.yaml
```

Edit the copies and set:

- a strong PostgreSQL password
- a long random JWT secret
- matching backend database credentials

Then apply them:

```bash
kubectl apply -f /tmp/postgres-secret.yaml
kubectl apply -f /tmp/backend-secret.yaml
```

## Deploy To Kubernetes

Apply the namespace and workload manifests:

```bash
kubectl apply -k k8s/
```

Check rollout status:

```bash
kubectl get pods -n contact-record
kubectl rollout status deployment/backend -n contact-record
kubectl rollout status deployment/frontend -n contact-record
kubectl rollout status deployment/postgres -n contact-record
kubectl get ingress -n contact-record
```

Open the application:

```text
http://contact-record.localtest.me
```

`localtest.me` resolves to `127.0.0.1`, which makes it convenient for local ingress testing.

## Files You Will Use

- `kind/kind-config.yaml`
- `k8s/namespace.yaml`
- `k8s/postgres.yaml`
- `k8s/postgres-secret.example.yaml`
- `k8s/backend-configmap.yaml`
- `k8s/backend-secret.example.yaml`
- `k8s/backend.yaml`
- `k8s/frontend.yaml`
- `k8s/ingress.yaml`
- `backend/Dockerfile`
- `frontend/Dockerfile`

## Best Practice Notes

This repo is now in a reasonable state for local Kubernetes deployment, but "best practice" depends on whether you mean local development or actual production.

### Good local defaults included here

- internal-only backend service
- health probes
- resource requests and limits
- non-root backend container
- externalized secrets
- ingress in front of the app
- persistent volume for PostgreSQL
- multi-stage builds for frontend and backend

### Still recommended before real production

- use a managed PostgreSQL service instead of in-cluster PostgreSQL
- move secrets to an external secret manager
- terminate TLS with real certificates
- add CI/CD that builds, scans, tags, and pushes images
- pin image digests for your own application images
- add database migration tooling such as Flyway or Liquibase
- add observability: metrics, centralized logs, and alerting
- add backup and restore procedures for the database
- consider Helm or Kustomize overlays for environment separation
- add stricter network policies once the cluster networking model is finalized

## Troubleshooting

If the frontend is up but API calls fail:

```bash
kubectl logs -n contact-record deploy/backend
kubectl describe pod -n contact-record -l app=backend
```

If ingress is not routing:

```bash
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
kubectl describe ingress -n contact-record
```

If PostgreSQL fails to start:

```bash
kubectl logs -n contact-record deploy/postgres
kubectl describe pvc postgres-data -n contact-record
```

## Source References

- Microsoft WSL install: https://learn.microsoft.com/en-us/windows/wsl/install
- Docker Desktop WSL 2 backend: https://docs.docker.com/desktop/features/wsl/
- kubectl install on Linux: https://kubernetes.io/docs/tasks/tools/install-kubectl-linux/
- kind quick start: https://kind.sigs.k8s.io/docs/user/quick-start/
- ingress-nginx deployment docs: https://kubernetes.github.io/ingress-nginx/deploy/
