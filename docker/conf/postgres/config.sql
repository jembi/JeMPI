CREATE TABLE IF NOT EXISTS Notification_Type
(
    Id uuid PRIMARY KEY,
    Type VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS Action_Type
(
    Id UUID PRIMARY KEY UNIQUE,
    Type VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS Notification_State
(
    Id UUID PRIMARY KEY UNIQUE,
    State VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS Notification
(
    Id uuid PRIMARY KEY UNIQUE,
    Type_Id uuid,
    Reason VARCHAR(100),
    Created date,
    Reviewd_By uuid,
    Reviewed_At timestamp without time zone,
    State_Id uuid,
    Patient_Id uuid,
    Names VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS Action
(
    Id UUID PRIMARY KEY,
    Notification_Id UUID,
    Action_Type_Id UUID,
    Date date,
    CONSTRAINT FK_Notification
      FOREIGN KEY(Notification_Id) 
	    REFERENCES Notification(Id),
    CONSTRAINT FK_Action_Type
      FOREIGN KEY(Action_Type_Id) 
	    REFERENCES Action_Type(Id)
);

CREATE TABLE IF NOT EXISTS Match
(
    Id UUID PRIMARY KEY,
    Notification_Id UUID,
    Score Numeric,
    Golden_Id VARCHAR(50),
    CONSTRAINT FK_Notification
      FOREIGN KEY(Notification_Id) 
	    REFERENCES Notification(Id)
);

INSERT INTO Notification_State(Id,State)
VALUES ('c2a28016-43b3-49a1-9b5e-32f886090ed2','New');

INSERT INTO Notification_Type(Id,Type)
VALUES ('ebc04be9-4742-4d5a-8049-cb54855e7e3c', 'THRESHOLD');

\dt;