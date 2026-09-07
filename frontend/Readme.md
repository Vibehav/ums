# User Management System — Frontend

React admin UI for the User Management System. Connects to the Spring Boot backend to perform CRUD operations on user (KYC-style) records.

---

## Tech Stack

| Tool | Purpose |
|---|---|
| React 18 + TypeScript | UI framework |
| Vite | Dev server and bundler |
| React Query (TanStack) | Server state, caching, refetching |
| React Hook Form + Zod | Form handling and validation |
| Axios | HTTP client |

---

## Project Structure

```
frontend/
├── src/
│   ├── api/
│   │   ├── client.ts        # Axios instance pointing to backend
│   │   └── users.ts         # API functions (GET, POST, PATCH, DELETE)
│   ├── components/
│   │   ├── ConfirmDialog.tsx # Delete confirmation modal
│   │   ├── Drawer.tsx        # Slide-in panel for create/edit form
│   │   ├── Pagination.tsx    # Page navigation
│   │   ├── Toast.tsx         # Success/error notifications
│   │   ├── UserForm.tsx      # Create & edit form with validation
│   │   └── UsersTable.tsx    # Users table with actions
│   ├── hooks/
│   │   └── useUsers.ts      # React Query hooks for all API calls
│   ├── types/
│   │   └── user.ts          # TypeScript interfaces
│   ├── App.tsx              # Main page — wires everything together
│   ├── main.tsx             # Entry point
│   └── index.css            # Styles
├── index.html
├── vite.config.ts           # Vite config with backend proxy
├── package.json
└── .env.example
```

---

## Prerequisites

- Node.js v18+
- Backend running at `http://localhost:8080`
- MySQL running (via Docker)

---

## Getting Started

**1. Start MySQL**
```bash
cd ../backend
docker compose up -d
```

**2. Start the backend**
```bash
cd ../backend
./mvnw spring-boot:run
```

**3. Install frontend dependencies**
```bash
cd frontend
npm install
```

**4. Start the dev server**
```bash
npm run dev
```

**5. Open in browser**
```
http://localhost:5173
```

---

## How Frontend Connects to Backend

The Vite dev server proxies all `/api/*` requests to `http://localhost:8080`:

```ts
// vite.config.ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  }
}
```

This means no CORS issues during development. The backend also has a `CorsConfig.java` as a safety net.

## Deploy the frontend

The frontend can be deployed to Vercel, Netlify, or Cloudflare Pages. It is a
static Vite application, so use these settings:

| Setting | Value |
|---|---|
| Build command | `npm run build` |
| Publish directory | `dist` |
| Node.js | 18 or newer |

Before deploying, host the Spring Boot backend at a public HTTPS URL and add
the frontend site's URL to its CORS allowed origins. In the hosting provider's
environment variables, set:

```env
VITE_API_BASE_URL=https://your-backend.example.com
```

The value is compiled into the browser bundle, so it must be the public backend
origin (not `localhost`) and must not contain `/api/v1/users`. Then redeploy
after changing the variable.

---

## API Endpoints Used

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/users?page=0&size=10&search=` | List users with pagination and search |
| `POST` | `/api/v1/users` | Create a new user |
| `PATCH` | `/api/v1/users/{id}` | Partially update a user |
| `DELETE` | `/api/v1/users/{id}` | Soft delete a user |
| `PATCH` | `/api/v1/users/{id}/restore` | Restore a soft-deleted user |

---

## Features

- **List users** — paginated table with all KYC fields
- **Search** — filter by name or email
- **Add user** — slide-in drawer with validated form
- **Edit user** — same form pre-filled with existing data
- **Delete user** — soft delete with confirmation dialog
- **Restore user** — restore soft-deleted users (shown in table)
- **Toast notifications** — success/error feedback on every action
- **Active/Deleted badge** — visual status indicator per row

---

## Form Validation

Validation mirrors the backend `@Valid` constraints exactly:

| Field | Rule |
|---|---|
| Name | Required, max 150 chars |
| Email | Required, valid email, max 254 chars |
| Primary Mobile | Required, 10-digit Indian number (starts with 6–9) |
| Secondary Mobile | Optional, same format |
| Aadhaar | 12 digits |
| PAN | Format `ABCDE1234F` |
| Date of Birth | Required, must be in the past |

---
