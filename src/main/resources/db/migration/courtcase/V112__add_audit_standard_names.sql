BEGIN;
ALTER TABLE COURT_CASE ADD COLUMN created TIMESTAMP DEFAULT NOW();
ALTER TABLE COURT_CASE ADD COLUMN created_by text;
ALTER TABLE COURT_CASE ADD COLUMN last_updated_by text;

ALTER TABLE COURT ADD COLUMN created TIMESTAMP default NOW();
ALTER TABLE COURT ADD COLUMN last_updated TIMESTAMP;
ALTER TABLE COURT ADD COLUMN created_by text ;
ALTER TABLE COURT ADD COLUMN last_updated_by text;

ALTER TABLE OFFENDER_MATCH_GROUP ADD COLUMN created TIMESTAMP default NOW();
ALTER TABLE OFFENDER_MATCH_GROUP ADD COLUMN last_updated TIMESTAMP;
ALTER TABLE OFFENDER_MATCH_GROUP ADD COLUMN created_by text ;
ALTER TABLE OFFENDER_MATCH_GROUP ADD COLUMN last_updated_by text;

ALTER TABLE OFFENDER_MATCH ADD COLUMN created TIMESTAMP default NOW();
ALTER TABLE OFFENDER_MATCH ADD COLUMN last_updated TIMESTAMP;
ALTER TABLE OFFENDER_MATCH ADD COLUMN created_by text ;
ALTER TABLE OFFENDER_MATCH ADD COLUMN last_updated_by text;

ALTER TABLE OFFENCE ADD COLUMN created TIMESTAMP default NOW();
ALTER TABLE OFFENCE ADD COLUMN last_updated TIMESTAMP;
ALTER TABLE OFFENCE ADD COLUMN created_by text ;
ALTER TABLE OFFENCE ADD COLUMN last_updated_by text;

ALTER TABLE COURT_CASE ADD COLUMN DELETED BOOLEAN not null DEFAULT FALSE;
ALTER TABLE COURT ADD COLUMN DELETED BOOLEAN not null DEFAULT FALSE;
ALTER TABLE OFFENDER_MATCH_GROUP ADD COLUMN DELETED BOOLEAN not null DEFAULT FALSE;
ALTER TABLE OFFENDER_MATCH ADD COLUMN DELETED BOOLEAN not null DEFAULT FALSE;
ALTER TABLE OFFENCE ADD COLUMN DELETED BOOLEAN not null DEFAULT FALSE;

ALTER TABLE OFFENDER_MATCH_GROUP ADD COLUMN VERSION int4 NOT NULL DEFAULT 0;
ALTER TABLE OFFENDER_MATCH ADD COLUMN VERSION int4 NOT NULL DEFAULT 0;
COMMIT;
