DROP TABLE IF EXISTS encounters;
DROP TABLE IF EXISTS source_id;
DROP TABLE IF EXISTS golden_records;

CREATE EXTENSION fuzzystrmatch;

CREATE TABLE IF NOT EXISTS golden_records
(
    uid uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    first_name        VARCHAR(50),
    middle_name       VARCHAR(50),
    surname           VARCHAR(50),
    dob               VARCHAR(50),
    sex               VARCHAR(50),
    chiefdom_code     VARCHAR(10),
    cell_phone        VARCHAR(50),
    pin               VARCHAR(50),
    aux_date_created  timestamp,
    aux_auto_update   BOOLEAN DEFAULT TRUE,
    aux_id            VARCHAR(50)
);

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
    first_name          VARCHAR(50),
    middle_name         VARCHAR(50),
    surname             VARCHAR(50),
    dob                 VARCHAR(50),
    sex                 VARCHAR(50),
    chiefdom_code       VARCHAR(10),
    cell_phone          VARCHAR(50),
    pin                 VARCHAR(50),
    golden_record_uid   uuid,
    score               real,
    source_id_uid       uuid,
    aux_date_created    timestamp,
    aux_id              VARCHAR(50),
    CONSTRAINT encounters_constraint_golden_record_uid FOREIGN KEY(golden_record_uid) REFERENCES golden_records(uid),
    CONSTRAINT encounters_constraint_source_id_uid     FOREIGN KEY(source_id_uid)     REFERENCES source_id(uid)
);    


\dt;
