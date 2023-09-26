
CREATE TABLE IF NOT EXISTS em.EntityLog (
  logID               INT           AUTO_INCREMENT PRIMARY KEY,
  id                  VARCHAR(32),
  givenName           VARCHAR(255), 
  givenNamePhonetic   VARCHAR(16),
  familyName          VARCHAR(255),
  familyNamePhonetic  VARCHAR(16),
  genderAtBirth       VARCHAR(16),
  dateOfBirth         VARCHAR(16), 
  city                VARCHAR(255),
  cityPhonetic        VARCHAR(16),
  phoneNumber         VARCHAR(16),
  nationalID          VARCHAR(16)
) ENGINE = INNODB;
