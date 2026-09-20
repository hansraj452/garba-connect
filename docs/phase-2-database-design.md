# Phase 2 – Database Design: Garba Connect

Professional PostgreSQL schema for auth, profiles, social graph, chat, notifications, moderation, and blocking. Implementation lives in `backend/` (Flyway `V1`, JPA entities, Spring Data repositories).

---

## 1. ER diagram

```mermaid
erDiagram
  USERS ||--|| USER_PROFILES : "1:1 owns"
  USERS ||--o{ CONNECTION_REQUESTS : sends
  USERS ||--o{ CONNECTION_REQUESTS : receives
  USERS ||--o{ CONNECTIONS : "participant (canonical pair)"
  CONNECTIONS ||--|| CONVERSATIONS : "1:1 enables"
  CONVERSATIONS ||--o{ MESSAGES : contains
  USERS ||--o{ MESSAGES : sends
  USERS ||--o{ MESSAGES : receives
  USERS ||--o{ NOTIFICATIONS : receives
  USERS ||--o{ REPORTS : files
  USERS ||--o{ REPORTS : "subject of"
  USERS ||--o{ BLOCKED_USERS : blocks
  USERS ||--o{ BLOCKED_USERS : "blocked by"

  USERS {
    uuid id PK
    varchar name
    varchar email UK
    varchar password_hash
    user_gender gender
    smallint age
    user_role role
    user_status status
    timestamptz created_at
  }

  USER_PROFILES {
    uuid user_id PK_FK
    varchar profile_image_url
    varchar phone UK_nullable
    varchar location
    numeric height_cm
    varchar complexion
    text bio
    varchar favorite_garba_style
    timestamptz updated_at
  }

  CONNECTION_REQUESTS {
    uuid id PK
    uuid sender_id FK
    uuid receiver_id FK
    connection_request_status status
    timestamptz created_at
  }

  CONNECTIONS {
    uuid id PK
    uuid user_a_id FK
    uuid user_b_id FK
    timestamptz connected_at
  }

  CONVERSATIONS {
    uuid id PK
    uuid connection_id FK_UK
    timestamptz created_at
  }

  MESSAGES {
    uuid id PK
    uuid conversation_id FK
    uuid sender_id FK
    uuid receiver_id FK
    text body
    message_read_status read_status
    timestamptz created_at
  }

  NOTIFICATIONS {
    uuid id PK
    uuid user_id FK
    notification_type type
    jsonb payload
    boolean read
    timestamptz created_at
  }

  REPORTS {
    uuid id PK
    uuid reporter_id FK
    uuid reported_user_id FK
    text reason
    text description
    report_status status
    timestamptz created_at
  }

  BLOCKED_USERS {
    uuid blocker_id PK_FK
    uuid blocked_id PK_FK
    timestamptz created_at
  }
```

---

## 2. Relationship explanation

| Relationship | Cardinality | Meaning |
|--------------|-------------|---------|
| **Users ↔ User profiles** | 1:1 | Every account has exactly one extended profile row (`user_profiles.user_id` = PK and FK). Auth credentials stay on `users`; discoverable attributes stay on `user_profiles`. |
| **Users ↔ Connection requests** | 1:N (sender), 1:N (receiver) | A user may send many requests (`sender_id`) and receive many (`receiver_id`). Business rule: no duplicate **pending** pair `(sender_id, receiver_id)` enforced by partial unique index. |
| **Users ↔ Connections** | M:N via `connections` | Accepted pairs only. `user_a_id` and `user_b_id` are stored in **canonical order** (`user_a_id < user_b_id`) so each unordered pair appears once. |
| **Connections ↔ Conversations** | 1:1 | When a connection is accepted, one conversation is created (`connection_id` unique on `conversations`). Chat is always scoped to an accepted connection. |
| **Conversations ↔ Messages** | 1:N | Messages belong to one conversation. `sender_id` and `receiver_id` denormalize the two participants for efficient “inbox” and read-status queries without joining through `connections` on every read. |
| **Users ↔ Notifications** | 1:N | Each notification targets one recipient (`user_id`). Payload JSON holds actor ids, request ids, message ids, etc. |
| **Users ↔ Reports** | 1:N (reporter), 1:N (reported) | Abuse reports link reporter and reported user; optional future link to messages. |
| **Users ↔ Blocked users** | M:N (directed) | Composite PK `(blocker_id, blocked_id)`. Block is directional: A blocks B does not imply B blocks A. |

---

## 3. Normalization

Design target: **Third Normal Form (3NF)** with deliberate, documented denormalization where read performance dominates.

### 3NF compliance

- **Users**: No repeating groups; `email` is unique; non-key attributes depend only on `id`.
- **User profiles**: Separate table removes nullable profile columns from `users` and keeps login row narrow (better cache/index behavior).
- **Connection requests / connections**: Request lifecycle (pending/declined) is separate from stable **connections** table—avoids updating wide user rows and clarifies state.
- **Messages**: Depend on `conversation_id`; `sender_id` / `receiver_id` are functionally determined by conversation + sender at insert time but stored for query speed (**controlled denormalization**, not a violation of business integrity if enforced in the service layer).

### Why not embed messages in conversations?

1NF requires atomic message values; storing arrays of messages in JSON would break relational integrity and indexing. Separate `messages` table supports pagination and `(conversation_id, created_at DESC)` indexes.

### Enum types in PostgreSQL

Native `CREATE TYPE ... AS ENUM` gives compact storage, clear constraints, and stable evolution via migrations (add values with `ALTER TYPE ... ADD VALUE`).

---

## 4. Primary keys

| Table | Primary key | Rationale |
|-------|-------------|-----------|
| `users` | `id UUID` | Non-enumerable public ids; safe in APIs and JWT `sub`. Default `gen_random_uuid()`. |
| `user_profiles` | `user_id UUID` | 1:1 with user; natural key avoids surrogate id joins. |
| `connection_requests` | `id UUID` | Multiple historical requests between same pair over time. |
| `connections` | `id UUID` | Referenced by conversations. |
| `conversations` | `id UUID` | Messages reference conversation. |
| `messages` | `id UUID` | Ordered history, notification payloads reference message id. |
| `notifications` | `id UUID` | Inbox pagination. |
| `reports` | `id UUID` | Moderation workflow. |
| `blocked_users` | `(blocker_id, blocked_id)` | Natural composite; one block edge per pair. |

---

## 5. Foreign keys

All FKs use **`ON DELETE RESTRICT`** for graph edges (connections, messages, reports) so data is not silently orphaned. **`ON DELETE CASCADE`** is used only where the child has no meaning without the parent:

- `user_profiles.user_id` → `users.id` **CASCADE** (profile is owned by account).
- `conversations.connection_id` → `connections.id` **CASCADE** (conversation exists only for a connection).

Check constraints:

- `users.age` between 13 and 120 (adjust for product policy).
- `connections.user_a_id < user_b_id`.
- `connection_requests.sender_id <> receiver_id`.
- `blocked_users.blocker_id <> blocked_id`.

---

## 6. Indexes

| Index | Purpose |
|-------|---------|
| `users(email)` UNIQUE | Login lookup. |
| `user_profiles(phone)` UNIQUE WHERE phone IS NOT NULL | Optional phone login / uniqueness. |
| `connection_requests(receiver_id, status)` | Pending inbox for receiver. |
| `connection_requests(sender_id, status)` | Outbound pending list. |
| Unique partial on `(sender_id, receiver_id)` WHERE `status = 'PENDING'` | One active pending request per direction pair. |
| `connections(user_a_id, user_b_id)` UNIQUE | One connection per unordered pair. |
| `conversations(connection_id)` UNIQUE | One chat thread per connection. |
| `messages(conversation_id, created_at DESC)` | Chat history pages. |
| `messages(receiver_id, read_status)` WHERE `read_status <> 'READ'` | Unread counts for recipient. |
| `notifications(user_id, read, created_at DESC)` | Notification feed. |
| `reports(reported_user_id, status)` | Moderation queue. |
| `blocked_users(blocked_id)` | “Who blocked me” / filter discovery. |

---

## 7. Notification types (application mapping)

| `notification_type` | User-facing meaning |
|---------------------|---------------------|
| `CONNECTION_REQUEST_RECEIVED` | Someone sent you a connection request |
| `CONNECTION_REQUEST_ACCEPTED` | Your request was accepted (or you accepted—payload distinguishes) |
| `NEW_MESSAGE` | New chat message while offline or for badge update |

Payload example: `{"actorUserId":"...","connectionRequestId":"...","messageId":"...","preview":"..."}`.

---

## 8. SQL schema location

Flyway migration: `backend/src/main/resources/db/migration/V1__initial_schema.sql`

---

## 9. JPA layer location

| Artifact | Package |
|----------|---------|
| Entities | `com.garbaconnect.domain.entity` |
| Enums | `com.garbaconnect.domain.enums` |
| Repositories | `com.garbaconnect.repository` |

---

## 10. Phase 2 exit criteria

- [x] ER diagram and relationship documentation
- [x] Normalization and PK/FK/index rationale
- [x] PostgreSQL DDL (Flyway V1)
- [x] JPA entities aligned to tables
- [x] Spring Data JPA repository interfaces

Next (Phase 3+): auth services, API DTOs, and discovery geospatial columns if split from `user_profiles.location` text.
