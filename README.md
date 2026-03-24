# Contact Record Keeping Application

Full-stack contact management application with:

- Angular frontend
- Spring Boot backend
- PostgreSQL database
- Kubernetes deployment manifests
- GitHub Actions workflows for image build and Kubernetes deployment

## Repository Structure

- `frontend/`: Angular application
- `backend/`: Spring Boot application
- `k8s/`: Kubernetes manifests and deployment guides
- `.github/`: GitHub Actions workflows and workflow documentation
- `kind/`: local `kind` cluster config

## Documentation Index

Use these documents based on what you are trying to do:

- Kubernetes deployment overview: [k8s/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/README.md)
- Multipass cluster setup and deployment: [k8s/multipass/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/multipass/README.md)
- MetalLB setup: [k8s/external-services/metal-lb/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/external-services/metal-lb/README.md)
- GitHub Actions and self-hosted runner setup: [.github/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/.github/README.md)
- Backend local development and database setup: [backend/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/backend/README.md)
- Frontend local development: [frontend/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/frontend/README.md)

## Application Architecture

Runtime flow:

1. client reaches Kubernetes `Ingress`
2. ingress routes traffic to the `frontend` service
3. frontend nginx proxies `/api/*` to the `backend` service
4. backend connects to PostgreSQL

This keeps the backend internal to the cluster and exposes only the frontend through ingress.

## Local Development

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

### Frontend

```bash
cd frontend
npm ci
npm start
```

The frontend runs on:

```text
http://localhost:4200
```

## Container Images

Build locally:

```bash
docker build -t contact-record-backend:local ./backend
docker build -t contact-record-frontend:local ./frontend
```

For Multipass or any multi-node Kubernetes cluster, use a registry-reachable image name instead of `:local`.

## Kubernetes Notes

This repository already includes:

- Dockerfiles for frontend and backend
- runtime backend configuration via ConfigMap and Secret
- health probes for backend and frontend
- ingress manifest
- PostgreSQL deployment and PVC
- `kind` local-cluster config

## CI/CD Notes

The repository uses two workflows:

- [build-images.yml](/home/ninad/Projects/Contact-Record-Keeping-Application/.github/workflows/build-images.yml)
- [deploy-k8s.yml](/home/ninad/Projects/Contact-Record-Keeping-Application/.github/workflows/deploy-k8s.yml)

Read the full workflow documentation here:

- [.github/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/.github/README.md)

## Recommended Reading Order

If you are starting from scratch:

1. [k8s/multipass/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/multipass/README.md) if you are using Multipass
2. [k8s/external-services/metal-lb/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/external-services/metal-lb/README.md) for ingress IP exposure
3. [k8s/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/README.md) for application deployment
4. [.github/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/.github/README.md) for CI/CD and runner setup
