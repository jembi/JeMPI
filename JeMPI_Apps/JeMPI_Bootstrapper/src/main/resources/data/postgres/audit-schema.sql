CREATE TABLE IF NOT EXISTS audit_trail (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    insertedAt     TIMESTAMP    NOT NULL DEFAULT now(),
    createdAt      TIMESTAMP    NOT NULL,
    eventType      VARCHAR(256),
    eventData      JSONB,
    CONSTRAINT PKEY_AUDIT_TRAIL PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_eventdata ON audit_trail USING GIN (eventData jsonb_path_ops);
CREATE INDEX IF NOT EXISTS idx_eventtype ON audit_trail(eventType);