DROP TABLE IF EXISTS hpo;

CREATE TABLE hpo(
    id       CHAR(10) PRIMARY KEY,
    lcname   VARCHAR(256)
);

CREATE INDEX hpoidx ON hpo(id);

--- HP-HP mappings
DROP TABLE IF EXISTS hp_hp_mappings;

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

-- Obsolete and alternate id mappings
DROP TABLE IF EXISTS hp_alt_ids;

CREATE TABLE hp_alt_ids (
    alt_id     char(10) PRIMARY KEY,
    primary_id char(10)
);

CREATE INDEX hpaltidx ON hp_alt_ids (alt_id);

