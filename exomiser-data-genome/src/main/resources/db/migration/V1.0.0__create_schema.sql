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


DROP TABLE IF EXISTS tad;

CREATE TABLE tad
(
    chromosome SMALLINT NOT NULL,
    start      INTEGER  NOT NULL,
    "end"      INTEGER  NOT NULL,
    entrezid   INTEGER  NOT NULL,
    symbol     CHARACTER VARYING(24)
);

CREATE INDEX tad
  ON tad (chromosome, start, "end");
