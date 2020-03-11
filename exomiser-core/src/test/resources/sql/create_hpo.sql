DROP TABLE IF EXISTS hpo;

CREATE TABLE hpo
(
    id     CHAR(10) PRIMARY KEY,
    lcname VARCHAR(256)
);

CREATE INDEX hpoidx
    ON hpo (id);

/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

--- HP-HP mappings
DROP TABLE IF EXISTS hp_hp_mappings;

CREATE TABLE hp_hp_mappings
(
    mapping_id  INTEGER,
    hp_id       CHARACTER VARYING(10),
    hp_term     CHARACTER VARYING(200),
    hp_id_hit   CHARACTER VARYING(10),
    hp_hit_term CHARACTER VARYING(200),
    simj        DOUBLE PRECISION,
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

