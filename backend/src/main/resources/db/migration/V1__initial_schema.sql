-- Garba Connect – Phase 2 initial schema (PostgreSQL)

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
-- Enum types
-- ---------------------------------------------------------------------------

CREATE TYPE user_gender AS ENUM (
    'MALE',
    'FEMALE',
    'OTHER',
    'PREFER_NOT_TO_SAY'
);

CREATE TYPE user_role AS ENUM (
    'USER',
    'ADMIN',
    'MODERATOR'
);

CREATE TYPE user_status AS ENUM (
    'ACTIVE',
    'PENDING_VERIFICATION',
    'SUSPENDED',
    'DELETED'
);

CREATE TYPE connection_request_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'DECLINED',
    'CANCELLED'
);

CREATE TYPE message_read_status AS ENUM (
    'SENT',
    'DELIVERED',
    'READ'
);

CREATE TYPE notification_type AS ENUM (
    'CONNECTION_REQUEST_RECEIVED',
    'CONNECTION_REQUEST_ACCEPTED',
    'NEW_MESSAGE'
);

CREATE TYPE report_status AS ENUM (
    'OPEN',
    'UNDER_REVIEW',
    'RESOLVED',
    'DISMISSED'
);

-- ---------------------------------------------------------------------------
-- Users
-- ---------------------------------------------------------------------------

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(120) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    gender          user_gender NOT NULL DEFAULT 'PREFER_NOT_TO_SAY',
    age             SMALLINT NOT NULL,
    role            user_role NOT NULL DEFAULT 'USER',
    status          user_status NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_lower_chk CHECK (email = lower(email)),
    CONSTRAINT users_age_chk CHECK (age >= 13 AND age <= 120)
);

CREATE UNIQUE INDEX idx_users_email ON users (email);

-- ---------------------------------------------------------------------------
-- User profiles (1:1)
-- ---------------------------------------------------------------------------

CREATE TABLE user_profiles (
    user_id                 UUID PRIMARY KEY,
    profile_image_url       VARCHAR(2048),
    phone                   VARCHAR(32),
    location                VARCHAR(255),
    height_cm               NUMERIC(5, 2),
    complexion              VARCHAR(64),
    bio                     TEXT,
    favorite_garba_style    VARCHAR(128),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT user_profiles_height_chk CHECK (
        height_cm IS NULL OR (height_cm >= 50 AND height_cm <= 300)
    )
);

CREATE UNIQUE INDEX idx_user_profiles_phone
    ON user_profiles (phone)
    WHERE phone IS NOT NULL;

CREATE INDEX idx_user_profiles_location ON user_profiles (location);

-- ---------------------------------------------------------------------------
-- Connection requests
-- ---------------------------------------------------------------------------

CREATE TABLE connection_requests (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id       UUID NOT NULL,
    receiver_id     UUID NOT NULL,
    status          connection_request_status NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_connection_requests_sender
        FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_connection_requests_receiver
        FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT connection_requests_distinct_users_chk CHECK (sender_id <> receiver_id)
);

CREATE INDEX idx_connection_requests_receiver_status
    ON connection_requests (receiver_id, status);

CREATE INDEX idx_connection_requests_sender_status
    ON connection_requests (sender_id, status);

CREATE UNIQUE INDEX idx_connection_requests_one_pending_pair
    ON connection_requests (sender_id, receiver_id)
    WHERE status = 'PENDING';

-- ---------------------------------------------------------------------------
-- Accepted connections (canonical unordered pair)
-- ---------------------------------------------------------------------------

CREATE TABLE connections (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_a_id       UUID NOT NULL,
    user_b_id       UUID NOT NULL,
    connected_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_connections_user_a
        FOREIGN KEY (user_a_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_connections_user_b
        FOREIGN KEY (user_b_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT connections_canonical_pair_chk CHECK (user_a_id < user_b_id),
    CONSTRAINT connections_distinct_users_chk CHECK (user_a_id <> user_b_id)
);

CREATE UNIQUE INDEX idx_connections_pair ON connections (user_a_id, user_b_id);

CREATE INDEX idx_connections_user_a ON connections (user_a_id);
CREATE INDEX idx_connections_user_b ON connections (user_b_id);

-- ---------------------------------------------------------------------------
-- Conversations (one per connection)
-- ---------------------------------------------------------------------------

CREATE TABLE conversations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    connection_id   UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_conversations_connection
        FOREIGN KEY (connection_id) REFERENCES connections (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_conversations_connection ON conversations (connection_id);

-- ---------------------------------------------------------------------------
-- Messages
-- ---------------------------------------------------------------------------

CREATE TABLE messages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id     UUID NOT NULL,
    sender_id           UUID NOT NULL,
    receiver_id         UUID NOT NULL,
    body                TEXT NOT NULL,
    read_status         message_read_status NOT NULL DEFAULT 'SENT',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations (id) ON DELETE RESTRICT,
    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_messages_receiver
        FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT messages_body_not_blank_chk CHECK (length(trim(body)) > 0),
    CONSTRAINT messages_distinct_participants_chk CHECK (sender_id <> receiver_id)
);

CREATE INDEX idx_messages_conversation_created
    ON messages (conversation_id, created_at DESC);

CREATE INDEX idx_messages_receiver_unread
    ON messages (receiver_id, read_status)
    WHERE read_status <> 'READ';

-- ---------------------------------------------------------------------------
-- Notifications
-- ---------------------------------------------------------------------------

CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    type            notification_type NOT NULL,
    payload         JSONB NOT NULL DEFAULT '{}'::jsonb,
    read            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT
);

CREATE INDEX idx_notifications_user_feed
    ON notifications (user_id, read, created_at DESC);

-- ---------------------------------------------------------------------------
-- Abuse reports
-- ---------------------------------------------------------------------------

CREATE TABLE reports (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id         UUID NOT NULL,
    reported_user_id    UUID NOT NULL,
    reason              VARCHAR(128) NOT NULL,
    description         TEXT,
    status              report_status NOT NULL DEFAULT 'OPEN',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_reports_reporter
        FOREIGN KEY (reporter_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_reports_reported_user
        FOREIGN KEY (reported_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT reports_distinct_users_chk CHECK (reporter_id <> reported_user_id)
);

CREATE INDEX idx_reports_reported_status ON reports (reported_user_id, status);
CREATE INDEX idx_reports_reporter ON reports (reporter_id, created_at DESC);

-- ---------------------------------------------------------------------------
-- Blocked users (directed block graph)
-- ---------------------------------------------------------------------------

CREATE TABLE blocked_users (
    blocker_id      UUID NOT NULL,
    blocked_id      UUID NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (blocker_id, blocked_id),
    CONSTRAINT fk_blocked_users_blocker
        FOREIGN KEY (blocker_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_blocked_users_blocked
        FOREIGN KEY (blocked_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT blocked_users_distinct_chk CHECK (blocker_id <> blocked_id)
);

CREATE INDEX idx_blocked_users_blocked ON blocked_users (blocked_id);
