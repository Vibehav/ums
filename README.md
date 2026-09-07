# User Management Service

This repository contains the User Management Service frontend and backend as
separate applications.

## Project structure

- [`frontend/`](./frontend/) — React and Vite web application. See its
  [documentation](./frontend/Readme.md).
- [`backend/`](./backend/) — Spring Boot REST API, database migrations, and
  local MySQL Docker Compose configuration. See its
  [documentation](./backend/README.md).

## Run locally

Start the API database and backend from `backend/`:

```bash
cd backend
docker compose up -d
./mvnw spring-boot:run
```

In another terminal, start the frontend:

```bash
cd frontend
npm install
npm run dev
```
