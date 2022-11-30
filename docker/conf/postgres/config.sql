CREATE TABLE IF NOT EXISTS notification_type
(
    id serial PRIMARY KEY,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS action_type
(
    id serial PRIMARY KEY UNIQUE,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS notification_state
(
    id serial PRIMARY KEY UNIQUE,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS notification
(
    id serial PRIMARY KEY UNIQUE,
    type serial,
    created date,
    reviewdBy serial,
    reviewedAt timestamp without time zone,
    state text,
    patientId serial,
    name VARCHAR
);

CREATE TABLE IF NOT EXISTS action
(
    notificationId serial,
    actionTypeId serial,
    date date,
    CONSTRAINT fk_notification
      FOREIGN KEY(notificationId) 
	    REFERENCES notification(id),
    CONSTRAINT fk_action_type
      FOREIGN KEY(actionTypeId) 
	    REFERENCES action_type(id)
);

CREATE TABLE IF NOT EXISTS match
(
    notificationId serial,
    score Float,
    date date,
    CONSTRAINT fk_notification
      FOREIGN KEY(notificationId) 
	    REFERENCES notification(id)
);

\dt;