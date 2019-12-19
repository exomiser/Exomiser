DROP TABLE IF EXISTS hpo;

CREATE TABLE hpo
(
    id     CHAR(10) PRIMARY KEY,
    lcname VARCHAR(256)
);

CREATE INDEX hpoidx
    ON hpo (id);

--- HP-HP mappings
DROP TABLE IF EXISTS hp_hp_mappings;

CREATE TABLE hp_hp_mappings
(
    mapping_id  INTEGER,
    hp_id       CHARACTER VARYING(10),
    hp_term     CHARACTER VARYING(200),
    hp_id_hit   CHARACTER VARYING(10),
    hp_hit_term CHARACTER VARYING(200),
    simJ        DOUBLE PRECISION,
    ic          DOUBLE PRECISION,
    score       DOUBLE PRECISION,
    lcs_id      CHARACTER VARYING(20),
    lcs_term    CHARACTER VARYING(150)
);

CREATE INDEX hp_id2
    ON hp_hp_mappings (hp_id);

-- Obsolete and alternate id mappings
DROP TABLE IF EXISTS hp_alt_ids;

CREATE TABLE hp_alt_ids
(
    alt_id     char(10) PRIMARY KEY,
    primary_id char(10)
);

CREATE INDEX hpaltidx
    ON hp_alt_ids (alt_id);

