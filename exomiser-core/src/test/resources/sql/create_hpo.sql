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
    hp_term character varying(200),
    hp_id_hit character varying(10),
    hp_hit_term character varying(200),
    simj double precision,
    ic double precision,
    score double precision,
    lcs_id character varying(10),
    lcs_term character varying(200)
);

