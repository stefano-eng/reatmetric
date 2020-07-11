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
   Route VARCHAR(48),
   Source VARCHAR(32),
   Severity SMALLINT NOT NULL,
   ContainerId BIGINT,
   Report BLOB,
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
   Route VARCHAR(48),
   Source VARCHAR(32),
   Handler VARCHAR(32) NOT NULL,
   Quality SMALLINT NOT NULL,
   RelatedItem BIGINT,
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
   Route VARCHAR(48),
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
-- SEPARATOR
CREATE TABLE ACTIVITY_OCCURRENCE_DATA_TABLE (
   UniqueId BIGINT NOT NULL,
   GenerationTime TIMESTAMP NOT NULL,
   ExternalId INT NOT NULL,
   Name VARCHAR(32) NOT NULL,
   Path VARCHAR(255) NOT NULL,
   Type VARCHAR(32) NOT NULL,
   Route VARCHAR(48) NOT NULL,
   Source VARCHAR(32),
   Arguments BLOB,
   Properties BLOB,
   AdditionalData BLOB,
   PRIMARY KEY (UniqueId)
)
-- SEPARATOR
CREATE TABLE ACTIVITY_REPORT_DATA_TABLE (
   UniqueId BIGINT NOT NULL,
   GenerationTime TIMESTAMP NOT NULL,
   Name VARCHAR(32) NOT NULL,
   ExecutionTime TIMESTAMP,
   State SMALLINT NOT NULL,
   NextState SMALLINT NOT NULL,
   ReportStatus SMALLINT NOT NULL,
   Result BLOB,
   ActivityOccurrenceId BIGINT REFERENCES ACTIVITY_OCCURRENCE_DATA_TABLE(UniqueId),
   AdditionalData BLOB,
   PRIMARY KEY (UniqueId)
)
-- SEPARATOR
CREATE INDEX PARAMETER_DATA_TABLE_IDX1 ON PARAMETER_DATA_TABLE (GenerationTime ASC)