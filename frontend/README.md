# Frontend Development Guide

This directory contains the Angular frontend for the Contact Record Keeping Application.

## Local Development

Install dependencies:

```bash
npm ci
```

Start the dev server:

```bash
npm start
```

Open:

```text
http://localhost:4200
```

## Build

Create a production build:

```bash
npm run build
```

Build artifacts are generated under:

```text
dist/contact-list-app
```

## Tests

Run frontend tests:

```bash
npm test
```

## Environment Behavior

- development uses `src/environments/environment.ts`
- production uses `src/environments/environment.prod.ts`

Production builds use `/api` as the backend base path so the frontend can sit behind Kubernetes ingress and nginx proxying.

## Container Build

The frontend container is built with:

- [frontend/Dockerfile](/home/ninad/Projects/Contact-Record-Keeping-Application/frontend/Dockerfile)

It:

1. builds the Angular app
2. serves the static files with nginx
3. proxies `/api/*` to the backend service

nginx config:

- [frontend/nginx/default.conf](/home/ninad/Projects/Contact-Record-Keeping-Application/frontend/nginx/default.conf)
