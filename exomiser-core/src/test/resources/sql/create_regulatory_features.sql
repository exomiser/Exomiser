DROP TABLE IF EXISTS regulatory_regions;

CREATE TABLE regulatory_regions
(
    chromosome   SMALLINT,
    start        INTEGER,
    "end"        INTEGER,
    feature_type CHARACTER VARYING(200)
);

CREATE INDEX rr1
    ON regulatory_regions (chromosome, start, "end");
