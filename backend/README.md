# Backend Development Guide

This directory contains the Spring Boot backend for the Contact Record Keeping Application.

## Tech Stack

- Java 21
- Spring Boot
- Spring Security with JWT
- Spring Data JPA
- PostgreSQL
- Maven Wrapper

## Local Development

Start the backend locally:

```bash
cd backend
./mvnw spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

API base path:

```text
http://localhost:8080/api
```

## Local Database Setup

The backend expects PostgreSQL.

By default, the application reads its database settings from environment variables, with local defaults defined in:

- [application.properties](/home/ninad/Projects/Contact-Record-Keeping-Application/backend/src/main/resources/application.properties)

Default local values are equivalent to:

- database host: `localhost`
- database port: `5432`
- database name: `contactdb`
- username: `postgres`
- password: `postgres`

### Step 1: Install PostgreSQL

On Ubuntu or WSL:

```bash
sudo apt-get update
sudo apt-get install -y postgresql postgresql-contrib
```

### Step 2: Start PostgreSQL

```bash
sudo systemctl enable postgresql
sudo systemctl start postgresql
```

### Step 3: Create the database

Log into PostgreSQL:

```bash
sudo -u postgres psql
```

Then create the database and user if needed:

```sql
CREATE DATABASE contactdb;
CREATE USER postgres WITH ENCRYPTED PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE contactdb TO postgres;
\q
```

If the `postgres` role already exists, you usually only need:

```sql
ALTER USER postgres WITH ENCRYPTED PASSWORD 'postgres';
CREATE DATABASE contactdb;
GRANT ALL PRIVILEGES ON DATABASE contactdb TO postgres;
```

### Step 4: Verify connection

```bash
psql -h localhost -U postgres -d contactdb
```

## Environment Variables

The backend can be configured through environment variables.

Important ones:

- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `SPRING_JPA_SHOW_SQL`
- `APP_JWT_SECRET`
- `APP_JWT_EXPIRATION`
- `APP_CORS_ALLOWED_ORIGINS`

Example local run:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/contactdb
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=postgres
export APP_JWT_SECRET='replace-with-a-long-random-secret'

./mvnw spring-boot:run
```

## Build

Build the backend JAR:

```bash
./mvnw package
```

## Tests

Run backend tests:

```bash
./mvnw test
```

The tests use:

- [application-test.properties](/home/ninad/Projects/Contact-Record-Keeping-Application/backend/src/test/resources/application-test.properties)

and an H2 in-memory database.

## Docker Image

The backend container is built with:

- [backend/Dockerfile](/home/ninad/Projects/Contact-Record-Keeping-Application/backend/Dockerfile)

Build locally:

```bash
docker build -t contact-record-backend:local ./backend
```

## Kubernetes Database Setup

In Kubernetes, PostgreSQL credentials are provided through:

- [k8s/postgres-secret.example.yaml](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/postgres-secret.example.yaml)
- [k8s/backend-secret.example.yaml](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/backend-secret.example.yaml)

These values must match:

- `POSTGRES_USER` = `SPRING_DATASOURCE_USERNAME`
- `POSTGRES_PASSWORD` = `SPRING_DATASOURCE_PASSWORD`
- `POSTGRES_DB` must match the database name in the datasource URL

The backend ConfigMap provides the in-cluster JDBC URL:

- [k8s/backend-configmap.yaml](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/backend-configmap.yaml)

The PostgreSQL deployment is defined in:

- [k8s/postgres.yaml](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/postgres.yaml)

## Common Commands

Start locally:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

Package:

```bash
./mvnw package
```

Build container:

```bash
docker build -t contact-record-backend:local ./backend
```

## Related Documentation

- Root guide: [README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/README.md)
- Kubernetes deployment: [k8s/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/README.md)
- Multipass setup: [k8s/multipass/README.md](/home/ninad/Projects/Contact-Record-Keeping-Application/k8s/multipass/README.md)
