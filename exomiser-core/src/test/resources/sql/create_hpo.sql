DROP TABLE hpo IF EXISTS;

CREATE TABLE hpo(
    lcname   VARCHAR(256) PRIMARY KEY,
    id       CHAR(10),
    prefname VARCHAR(256));

CREATE INDEX hpoidx ON hpo(id);

--- HP-HP mappings
DROP TABLE hp_hp_mappings IF EXISTS;

CREATE TABLE hp_hp_mappings (
    mapping_id integer,
    hp_id character varying(10),
    hp_id_hit character varying(10),
    score double precision
);

