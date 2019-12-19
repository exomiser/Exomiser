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
