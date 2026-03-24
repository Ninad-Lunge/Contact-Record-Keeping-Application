# Kubernetes Deployment Guide

This document covers the full Kubernetes deployment flow for the Contact Record Keeping Application.

It is written for the manifests in this repository and supports two common setups:

- local development with `kind`
- a multi-node Kubernetes cluster such as a `kubeadm` cluster running on Multipass VMs

## Architecture

The deployed application has four main parts:

1. `frontend` Deployment
2. `backend` Deployment
3. `postgres` Deployment with a PersistentVolumeClaim
4. `Ingress` routing external traffic to the frontend

Traffic flow:

1. Client reaches the Kubernetes `Ingress`
2. `Ingress` sends traffic to the `frontend` Service
3. frontend nginx proxies `/api/*` to the internal `backend` Service
4. backend connects to the internal `postgres` Service

This keeps the backend private inside the cluster and avoids production browser CORS issues.

## Repository Layout

Relevant files:

- `k8s/namespace.yaml`
- `k8s/postgres.yaml`
- `k8s/postgres-secret.example.yaml`
- `k8s/backend-configmap.yaml`
- `k8s/backend-secret.example.yaml`
- `k8s/backend.yaml`
- `k8s/frontend.yaml`
- `k8s/ingress.yaml`
- `k8s/kustomization.yaml`
- `backend/Dockerfile`
- `frontend/Dockerfile`
- `kind/kind-config.yaml`

## What The Manifests Expect

Before deploying, the cluster must provide:

- a working Kubernetes context in `kubectl`
- a default `StorageClass` or an explicit storage class you will use for PostgreSQL
- an `nginx` `IngressClass`
- access to the frontend and backend container images

If any of those are missing, the deployment will fail even if the YAML applies cleanly.

## Common Prerequisites

Check the cluster first:

```bash
kubectl get nodes -o wide
kubectl get storageclass
kubectl get ingressclass
```

If `kubectl get storageclass` returns no resources, install a storage provisioner before deploying PostgreSQL.

If `kubectl get ingressclass` does not show `nginx`, install `ingress-nginx` before applying the app manifests.

## Option 1: kind Deployment

This is the simplest local path for WSL or a single-machine development environment.

### 1. Create the cluster

```bash
kind create cluster --name contact-record --config kind/kind-config.yaml --wait 2m
kubectl cluster-info --context kind-contact-record
```

### 2. Install ingress-nginx for kind

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.14.3/deploy/static/provider/kind/deploy.yaml
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s
```

### 3. Build local images

From the repository root:

```bash
docker build -t contact-record-backend:local ./backend
docker build -t contact-record-frontend:local ./frontend
```

### 4. Load images into kind

```bash
kind load docker-image contact-record-backend:local --name contact-record
kind load docker-image contact-record-frontend:local --name contact-record
```

### 5. Create secrets

```bash
cp k8s/postgres-secret.example.yaml /tmp/postgres-secret.yaml
cp k8s/backend-secret.example.yaml /tmp/backend-secret.yaml
```

Edit both files, then apply:

```bash
kubectl apply -f /tmp/postgres-secret.yaml
kubectl apply -f /tmp/backend-secret.yaml
```

### 6. Deploy the app

```bash
kubectl apply -k k8s/
```

### 7. Verify rollout

```bash
kubectl get all -n contact-record
kubectl get pvc -n contact-record
kubectl get ingress -n contact-record
kubectl rollout status deployment/postgres -n contact-record
kubectl rollout status deployment/backend -n contact-record
kubectl rollout status deployment/frontend -n contact-record
```

### 8. Open the application

The ingress host in this repository is:

```text
http://contact-record.localtest.me
```

`localtest.me` resolves to `127.0.0.1`, which makes it convenient for local ingress testing with `kind`.

## Option 2: Multipass Or kubeadm-Style Cluster

For a real multi-node cluster running on VMs, there are three additional concerns:

- persistent storage
- image distribution
- external ingress access

### 1. Install a storage provisioner

If the cluster has no default storage class, install one before deploying PostgreSQL.

A simple option for labs and local VM clusters is `local-path-provisioner`:

```bash
kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.34/deploy/local-path-storage.yaml
kubectl get storageclass
```

If `local-path` is not the default:

```bash
kubectl patch storageclass local-path \
  -p '{"metadata":{"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

### 2. Install ingress-nginx

Using Helm:

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --create-namespace
```

Verify it:

```bash
kubectl get pods -n ingress-nginx
kubectl get svc -n ingress-nginx
kubectl get ingressclass
```

### 3. Choose how ingress will be exposed

For VM-based clusters, `Ingress` alone is not enough. You must decide how the ingress controller is reached from outside the cluster.

Common options:

- `MetalLB` plus an ingress `LoadBalancer` Service
- `NodePort` and access through a node IP and high port
- a reverse proxy or external load balancer in front of the cluster

For a clean local lab setup, `MetalLB` is usually the better option.

### 4. Push images to a registry

The default manifests use local image names:

- `contact-record-backend:local`
- `contact-record-frontend:local`

That works for `kind`, but not for separate VM nodes.

Build and push images to a registry reachable by all nodes:

```bash
docker build -t YOUR_REGISTRY/contact-record-backend:v1 ./backend
docker build -t YOUR_REGISTRY/contact-record-frontend:v1 ./frontend

docker push YOUR_REGISTRY/contact-record-backend:v1
docker push YOUR_REGISTRY/contact-record-frontend:v1
```

Then update:

- `k8s/backend.yaml`
- `k8s/frontend.yaml`

Replace the `image:` values with your pushed image names.

### 5. Create secrets

```bash
cp k8s/postgres-secret.example.yaml /tmp/postgres-secret.yaml
cp k8s/backend-secret.example.yaml /tmp/backend-secret.yaml
```

Edit the files and set:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`

Then apply them:

```bash
kubectl apply -f /tmp/postgres-secret.yaml
kubectl apply -f /tmp/backend-secret.yaml
```

### 6. Apply the manifests

```bash
kubectl apply -k k8s/
```

### 7. Verify rollout

```bash
kubectl get all -n contact-record
kubectl get pvc -n contact-record
kubectl get ingress -n contact-record
kubectl rollout status deployment/postgres -n contact-record
kubectl rollout status deployment/backend -n contact-record
kubectl rollout status deployment/frontend -n contact-record
```

### 8. Route the hostname

The current ingress host is `contact-record.localtest.me`.

For a VM cluster, you may want to:

- keep that hostname and map it to the ingress IP in `/etc/hosts`
- change the ingress host in `k8s/ingress.yaml`
- use a real DNS name if the cluster will be shared

## Secrets And Configuration

### Non-secret configuration

`k8s/backend-configmap.yaml` contains non-secret runtime configuration such as:

- backend port
- datasource URL
- JPA settings
- allowed CORS origins

### Secret configuration

`k8s/backend-secret.example.yaml` contains:

- backend database username
- backend database password
- JWT secret
- JWT expiration

`k8s/postgres-secret.example.yaml` contains:

- database name
- database username
- database password

Never commit the edited secret files to the repository.

## Deployment Commands

These are the main commands you will use repeatedly:

```bash
kubectl apply -k k8s/
kubectl get all -n contact-record
kubectl get pvc -n contact-record
kubectl get ingress -n contact-record
kubectl logs -n contact-record deploy/backend
kubectl logs -n contact-record deploy/frontend
kubectl logs -n contact-record deploy/postgres
kubectl describe pod -n contact-record -l app=backend
kubectl describe pod -n contact-record -l app=frontend
kubectl describe pod -n contact-record -l app=postgres
```

## Updating The Application

When you make code changes:

1. rebuild the image
2. push or load the image
3. update the image tag in the manifest if needed
4. apply the manifests again

Example:

```bash
docker build -t YOUR_REGISTRY/contact-record-backend:v2 ./backend
docker push YOUR_REGISTRY/contact-record-backend:v2

kubectl apply -k k8s/
kubectl rollout status deployment/backend -n contact-record
```

## Rollback

If a deployment fails after a rollout:

```bash
kubectl rollout undo deployment/backend -n contact-record
kubectl rollout undo deployment/frontend -n contact-record
```

## Troubleshooting

### PostgreSQL pod is Pending

Likely causes:

- no default `StorageClass`
- provisioner not installed
- PVC cannot be bound

Check:

```bash
kubectl get storageclass
kubectl get pvc -n contact-record
kubectl describe pvc postgres-data -n contact-record
```

### Backend pod cannot connect to PostgreSQL

Check:

```bash
kubectl logs -n contact-record deploy/backend
kubectl logs -n contact-record deploy/postgres
kubectl get secret postgres-secret -n contact-record
kubectl get secret backend-secret -n contact-record
```

### Frontend is up but API calls fail

Check:

```bash
kubectl logs -n contact-record deploy/frontend
kubectl logs -n contact-record deploy/backend
kubectl describe ingress -n contact-record
```

### Ingress exists but the app is not reachable

Check:

```bash
kubectl get svc -n ingress-nginx
kubectl get ingress -n contact-record
kubectl describe ingress -n contact-record
```

On VM clusters, this usually means ingress is not exposed outside the cluster yet.

### Pods are crashing after start

Check:

```bash
kubectl get pods -n contact-record
kubectl describe pod -n contact-record -l app=backend
kubectl describe pod -n contact-record -l app=frontend
kubectl describe pod -n contact-record -l app=postgres
```

## Production Notes

This repository is suitable as a local and lab deployment baseline. For production, you should still plan for:

- managed PostgreSQL instead of in-cluster PostgreSQL
- external secrets management
- TLS certificates and HTTPS
- image scanning and signed releases
- CI/CD pipelines
- observability and alerting
- backup and restore procedures
- database migrations with Flyway or Liquibase
- environment-specific overlays or Helm packaging

## GitHub Actions CI/CD

This repository includes two GitHub Actions workflows:

- `.github/workflows/build-images.yml`
- `.github/workflows/deploy-k8s.yml`

### What the pipeline does

`build-images.yml` on pull requests to `main`:

- validates the Kubernetes manifests with `kubectl kustomize`
- runs backend tests
- builds the frontend

`build-images.yml` on pushes to `main` and manual runs:

- runs the same validation
- builds and pushes Docker images to Docker Hub

`deploy-k8s.yml`:

- runs automatically after a successful `Build Images` workflow
- can also be started manually
- deploys the selected image tags to the Kubernetes cluster

### Why the deploy workflow uses a self-hosted runner

Your Multipass cluster is on a private network, typically with node IPs like `10.x.x.x`.

GitHub-hosted runners usually cannot reach that network directly. Because of that, the deploy workflow is designed to run on a self-hosted GitHub Actions runner installed on the Multipass `master` node or another machine that already has:

- network access to the cluster
- `kubectl`
- a working kubeconfig context

### Required GitHub repository secrets

Add these in `GitHub -> Settings -> Secrets and variables -> Actions`:

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`

`DOCKERHUB_TOKEN` should be a Docker Hub access token, not your Docker password.

### What must already exist on the cluster

The workflow applies the manifests and updates the deployment image tags, but it does not create your real application secrets for you.

Before the deploy job can succeed, the cluster must already have:

- `backend-secret`
- `postgres-secret`
- storage provisioner
- ingress controller

### Self-hosted runner setup on the master node

On the Multipass master VM:

1. Log in to GitHub
2. Open the repository
3. Go to `Settings -> Actions -> Runners`
4. Click `New self-hosted runner`
5. Choose `Linux` and `x64`
6. Run the provided installation commands on the master VM

Make sure the runner machine also has:

```bash
kubectl get nodes
```

working successfully under the same user that runs the GitHub Actions runner service.

### Deployment behavior

The deploy workflow does this:

1. checks out the repository
2. runs `kubectl apply -k k8s/`
3. updates the backend image with `kubectl set image`
4. updates the frontend image with `kubectl set image`
5. waits for PostgreSQL, backend, and frontend rollouts

This means you can continue keeping environment config in the manifests while using immutable image tags from CI.

### Important note about manifests

Your checked-in manifests may still contain fixed image tags such as `v1`.

That is fine because the deploy workflow overwrites the deployed image tags at runtime with:

- `DOCKERHUB_USERNAME/contact-record-backend:${GITHUB_SHA}`
- `DOCKERHUB_USERNAME/contact-record-frontend:${GITHUB_SHA}`

For manual deploys, `deploy-k8s.yml` also accepts an `image_tag` input so you can roll out:

- `latest`
- a specific Git commit SHA tag
- any other tag you pushed intentionally

### First-time bootstrap before CI/CD

Before relying on the pipeline, do this once manually:

1. install storage
2. install ingress
3. create `backend-secret`
4. create `postgres-secret`
5. confirm `kubectl apply -k k8s/` works from the master node

After that, GitHub Actions can take over image build and rollout.

## References

- Kubernetes ingress-nginx deployment docs: https://kubernetes.github.io/ingress-nginx/deploy/
- ingress-nginx bare-metal guidance: https://kubernetes.github.io/ingress-nginx/deploy/baremetal/
- Rancher local-path-provisioner: https://github.com/rancher/local-path-provisioner
- kind quick start: https://kind.sigs.k8s.io/docs/user/quick-start/
