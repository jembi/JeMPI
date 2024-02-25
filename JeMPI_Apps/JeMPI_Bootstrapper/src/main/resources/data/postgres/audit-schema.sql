CREATE TABLE IF NOT EXISTS audit_trail (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    insertedAt     TIMESTAMP    NOT NULL DEFAULT now(),
    createdAt      TIMESTAMP    NOT NULL,
    interactionID  VARCHAR(64),
    goldenID       VARCHAR(64),
    event          VARCHAR(256),
    score          FLOAT DEFAULT -1.0,
    linkingRule    VARCHAR(14),
    CONSTRAINT PKEY_AUDIT_TRAIL PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS idx_gid ON audit_trail(goldenID);
CREATE INDEX IF NOT EXISTS idx_iid ON audit_trail(interactionID);

DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_trail' AND column_name = 'score') THEN 
        ALTER TABLE audit_trail ADD COLUMN score FLOAT;
    END IF; 
END $$;

UPDATE audit_trail
SET score = -1.0
WHERE score IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'audit_trail' AND column_name = 'linkingrule') THEN
        ALTER TABLE audit_trail ADD COLUMN linkingRule VARCHAR(14);
    END IF;
END $$;

UPDATE audit_trail
SET linkingRule = 'UNMATCHED'
WHERE linkingRule IS NULL;

