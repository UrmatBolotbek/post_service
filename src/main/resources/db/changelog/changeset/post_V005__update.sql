ALTER TABLE comment
    ADD COLUMN verified boolean DEFAULT false NOT NULL;

ALTER TABLE comment
    ADD COLUMN vision boolean DEFAULT true NOT NULL;
