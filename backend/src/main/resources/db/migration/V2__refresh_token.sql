CREATE TABLE refresh_tokens (

                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                token VARCHAR(512) NOT NULL UNIQUE,

                                user_id UUID NOT NULL,

                                expiry_date TIMESTAMPTZ NOT NULL,

                                revoked BOOLEAN DEFAULT FALSE,

                                CONSTRAINT fk_refresh_user
                                    FOREIGN KEY(user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);

CREATE INDEX idx_refresh_user
    ON refresh_tokens(user_id);