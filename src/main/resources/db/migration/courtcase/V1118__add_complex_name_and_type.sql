BEGIN;
ALTER TABLE court_case ADD COLUMN defendant_type text NOT NULL DEFAULT 'PERSON';
ALTER TABLE COURT_CASE ADD COLUMN NAME JSONB NULL;
COMMIT;