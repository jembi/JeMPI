CREATE TABLE IF NOT EXISTS configuration (
    id SERIAL PRIMARY KEY,
    key VARCHAR(255) NOT NULL,
    json JSON NOT NULL,
    dateCreated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    dateUpdated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create an index on the key column for faster lookups
CREATE INDEX IF NOT EXISTS idx_configuration_key ON configuration(key);

-- INSERT INTO configuration (key, json) VALUES
-- ('config', pg_read_file('/app/conf_system/config.json')::json);