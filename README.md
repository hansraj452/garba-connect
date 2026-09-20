# Garba Connect

Production-ready social networking for Garba/Navratri attendees: discover nearby people, send connection requests, and chat after mutual acceptance.

## Tech stack

| Layer | Technology |
|-------|------------|
| Frontend | React (Vite), React Router, Redux Toolkit, Material UI, Axios |
| Backend | Spring Boot, Spring Security, Spring Data JPA, JWT, BCrypt, WebSocket (STOMP) |
| Database | PostgreSQL (Supabase / Neon in production) |
| Storage | Local filesystem (dev); AWS S3 (optional prod) |
| Deploy | Vercel (frontend), Render or AWS (backend) |

## Repository layout

```
garba-connect/
├── docs/                    # Architecture & phase plans
├── frontend/                # Vite + React app (Phase 2+)
├── backend/                 # Spring Boot API (Phase 2+)
└── README.md
```

## Documentation

- **[Phase 1 – Software Architecture](./docs/phase-1-architecture.md)** — system design, flows, security, deployment topology
- **[Phase 2 – Database Design](./docs/phase-2-database-design.md)** — ERD, normalization, Flyway schema, JPA entities

## Phased delivery

| Phase | Focus |
|-------|--------|
| 1 | Architecture (this doc) |
| 2+ | Auth, profiles, discovery, connections, chat, notifications (per phase doc) |
