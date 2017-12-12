CREATE TABLE IF NOT EXISTS city_tmp
(
  "id" BIGINT AUTO_INCREMENT PRIMARY KEY,
  "name" VARCHAR(255) NOT NULL,
  "shortCode" VARCHAR(255) NOT NULL,
  "latitude" FLOAT NOT NULL,
  "longitude" FLOAT NOT NULL,
  "country.id" BIGINT NOT NULL,
  "region.id" BIGINT NOT NULL
);

create table IF NOT EXISTS NewObjectId
(
    KeyName varchar(255) not null primary key,
    NextId bigint not null
);
