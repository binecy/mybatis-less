
DROP TABLE subject;
CREATE TABLE subject (
  id      INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  code    VARCHAR(32) NOT NULL UNIQUE ,
  title   VARCHAR(64),
  content VARCHAR(1024),
  author  VARCHAR(16),
  read_count INT ,
  cdt  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);