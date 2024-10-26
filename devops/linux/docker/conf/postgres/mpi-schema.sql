DROP TABLE IF EXISTS encounters;
DROP TABLE IF EXISTS source_id;
DROP TABLE IF EXISTS golden_records;

CREATE EXTENSION IF NOT EXISTS fuzzystrmatch;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
SET pg_trgm.similarity_threshold = 0.5;


CREATE TABLE IF NOT EXISTS golden_records
(
    uid uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    first_name              VARCHAR(100),
    middle_name             VARCHAR(100),
    surname                 VARCHAR(100),
    dob                     VARCHAR(20),
    sex                     VARCHAR(10),
    chiefdom_code           VARCHAR(10),
    cell_phone              VARCHAR(20),
    pin                     VARCHAR(20),
    aux_date_created        timestamp,
    aux_auto_update_enabled BOOLEAN DEFAULT TRUE,
    aux_id                  VARCHAR(50)
);
CREATE INDEX golden_records_pin                     ON golden_records (pin);
CREATE INDEX golden_records_aux_date_created        ON golden_records (aux_date_created);
CREATE INDEX golden_records_first_name_trgm_idx     ON golden_records USING gin(first_name    gin_trgm_ops);
CREATE INDEX golden_records_middle_name_trgm_idx    ON golden_records USING gin(middle_name   gin_trgm_ops);
CREATE INDEX golden_records_surname_trgm_idx        ON golden_records USING gin(surname       gin_trgm_ops);
CREATE INDEX golden_records_dob_trgm_idx            ON golden_records USING gin(dob           gin_trgm_ops);
CREATE INDEX golden_records_chiefdom_code_trgm_idx  ON golden_records USING gin(chiefdom_code gin_trgm_ops);
CREATE INDEX golden_records_cell_phone_trgm_idx     ON golden_records USING gin(cell_phone    gin_trgm_ops);
CREATE INDEX golden_records_pin_trgm_idx            ON golden_records USING gin(pin           gin_trgm_ops);


CREATE TABLE IF NOT EXISTS source_id
(
    uid               uuid PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    facility_code     VARCHAR(50),
    patient_id        VARCHAR(50),
    golden_record_uid uuid,
    CONSTRAINT source_id_constraint_golden_record_uid FOREIGN KEY(golden_record_uid) REFERENCES golden_records(uid)
);

CREATE TABLE IF NOT EXISTS encounters
(
    uid                 uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    first_name          VARCHAR(100),
    middle_name         VARCHAR(100),
    surname             VARCHAR(100),
    dob                 VARCHAR(20),
    sex                 VARCHAR(10),
    chiefdom_code       VARCHAR(10),
    cell_phone          VARCHAR(20),
    pin                 VARCHAR(20),
    golden_record_uid   uuid,
    score               real,
    source_id_uid       uuid,
    aux_date_created    timestamp,
    aux_id              VARCHAR(50),
    CONSTRAINT encounters_constraint_golden_record_uid FOREIGN KEY(golden_record_uid) REFERENCES golden_records(uid),
    CONSTRAINT encounters_constraint_source_id_uid     FOREIGN KEY(source_id_uid)     REFERENCES source_id(uid)
);    


\dt;
