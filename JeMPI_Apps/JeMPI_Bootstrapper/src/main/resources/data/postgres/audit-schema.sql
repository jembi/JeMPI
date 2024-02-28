CREATE TABLE IF NOT EXISTS audit_trail (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    insertedAt     TIMESTAMP    NOT NULL DEFAULT now(),
    createdAt      TIMESTAMP    NOT NULL,
    interactionID  VARCHAR(64),
    goldenID       VARCHAR(64),
    event          VARCHAR(256),
    eventData      VARCHAR(256),
    CONSTRAINT PKEY_AUDIT_TRAIL PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_gid ON audit_trail(goldenID);
CREATE INDEX IF NOT EXISTS idx_iid ON audit_trail(interactionID);
