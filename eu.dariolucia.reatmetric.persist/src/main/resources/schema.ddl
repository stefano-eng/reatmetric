CREATE TABLE OPERATIONAL_MESSAGE_TABLE (
   UniqueId BIGINT NOT NULL,
   GenerationTime TIMESTAMP NOT NULL,
   Id VARCHAR(32) NOT NULL,
   Text VARCHAR(255) NOT NULL,
   Source VARCHAR(32),
   Severity SMALLINT NOT NULL,
   AdditionalData BLOB,
   PRIMARY KEY (UniqueId)
)
-- SEPARATOR
CREATE TABLE EVENT_DATA_TABLE (
   UniqueId BIGINT NOT NULL,
   GenerationTime TIMESTAMP NOT NULL,
   ExternalId INT NOT NULL,
   Name VARCHAR(32) NOT NULL,
   Path VARCHAR(255) NOT NULL,
   Qualifier VARCHAR(255),
   ReceptionTime TIMESTAMP NOT NULL,
   Type VARCHAR(32) NOT NULL,
   Route VARCHAR(32),
   Source VARCHAR(32),
   Severity SMALLINT NOT NULL,
   AdditionalData BLOB,
   PRIMARY KEY (UniqueId)
)
-- SEPARATOR
CREATE TABLE RAW_DATA_TABLE (
   UniqueId BIGINT NOT NULL,
   GenerationTime TIMESTAMP NOT NULL,
   Name VARCHAR(32) NOT NULL,
   ReceptionTime TIMESTAMP NOT NULL,
   Type VARCHAR(32) NOT NULL,
   Route VARCHAR(32),
   Source VARCHAR(32),
   Quality SMALLINT NOT NULL,
   Contents BLOB,
   AdditionalData BLOB,
   PRIMARY KEY (UniqueId)
)
-- SEPARATOR
CREATE TABLE PARAMETER_DATA_TABLE (
   UniqueId BIGINT NOT NULL,
   GenerationTime TIMESTAMP NOT NULL,
   ExternalId INT NOT NULL,
   Name VARCHAR(32) NOT NULL,
   Path VARCHAR(255) NOT NULL,
   EngValue BLOB,
   SourceValue BLOB,
   ReceptionTime TIMESTAMP NOT NULL,
   Route VARCHAR(32),
   Validity SMALLINT NOT NULL,
   AlarmState SMALLINT NOT NULL,
   ContainerId BIGINT,
   AdditionalData BLOB,
   PRIMARY KEY (UniqueId)
)
-- SEPARATOR
CREATE TABLE ALARM_PARAMETER_DATA_TABLE (
   UniqueId BIGINT NOT NULL,
   GenerationTime TIMESTAMP NOT NULL,
   ExternalId INT NOT NULL,
   Name VARCHAR(32) NOT NULL,
   Path VARCHAR(255) NOT NULL,
   CurrentAlarmState SMALLINT NOT NULL,
   CurrentValue BLOB,
   ReceptionTime TIMESTAMP NOT NULL,
   LastNominalValue BLOB,
   LastNominalValueTime TIMESTAMP,
   AdditionalData BLOB,
   PRIMARY KEY (UniqueId)
)