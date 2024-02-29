/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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


--  SV frequency table
DROP TABLE IF EXISTS sv_freq;

CREATE TABLE sv_freq
(
    chromosome    SMALLINT NOT NULL,
    start         INTEGER  NOT NULL,
    "end"         INTEGER  NOT NULL,
    change_length INTEGER  NOT NULL,
    variant_type  CHARACTER VARYING(20),
    dbvar_id      CHARACTER VARYING(20),
    source        CHARACTER VARYING(20),
    source_id     CHARACTER VARYING(40),
    allele_count  INTEGER  NOT NULL,
    allele_number INTEGER  NOT NULL
);

CREATE INDEX sv_freq
    ON sv_freq (chromosome, start, "end");


--  SV pathogenicity table
DROP TABLE IF EXISTS sv_path;

CREATE TABLE sv_path
(
    chromosome    SMALLINT NOT NULL,
    start         INTEGER  NOT NULL,
    "end"         INTEGER  NOT NULL,
    change_length INTEGER  NOT NULL,
    variant_type  CHARACTER VARYING(20),
    dbvar_id      CHARACTER VARYING(20),
    source        CHARACTER VARYING(20),
    rcv_id        CHARACTER VARYING(20),
    variation_id  CHARACTER VARYING(20),
    clin_sig      CHARACTER VARYING(42),
    clin_rev_stat CHARACTER VARYING(55)
);

CREATE INDEX sv_path
    ON sv_path (chromosome, start, "end");