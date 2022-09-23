--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.9
-- Dumped by pg_dump version 9.3.1
-- Started on 2013-12-05 14:38:16

-- n.b. indexes are created after each bulk insert migration
--
DROP TABLE IF EXISTS hp_mp_mappings;

CREATE TABLE hp_mp_mappings(
                             mapping_id INTEGER,
                             hp_id      CHARACTER VARYING(10),
                             hp_term    CHARACTER VARYING(200),
                             mp_id      CHARACTER VARYING(10),
                             mp_term    CHARACTER VARYING(200),
                             simj       DOUBLE PRECISION,
                             ic         DOUBLE PRECISION,
                             score      DOUBLE PRECISION,
                             lcs_id     CHARACTER VARYING(20),
                             lcs_term   CHARACTER VARYING(150)
);

DROP TABLE IF EXISTS mp;

CREATE TABLE mp (
  mp_id   CHAR(10),
  mp_term VARCHAR(256)
);

DROP TABLE IF EXISTS hp_zp_mappings;

CREATE TABLE hp_zp_mappings
(
  mapping_id INTEGER,
  hp_id      CHARACTER VARYING(10),
  hp_term    CHARACTER VARYING(200),
  zp_id      CHARACTER VARYING(10),
  zp_term    CHARACTER VARYING(200),
  simj       DOUBLE PRECISION,
  ic         DOUBLE PRECISION,
  score      DOUBLE PRECISION,
  lcs_id     CHARACTER VARYING(40),
  lcs_term   CHARACTER VARYING(150)
);

DROP TABLE IF EXISTS zp;

CREATE TABLE zp (
  zp_id   CHAR(10),
  zp_term VARCHAR(256)
);


DROP TABLE IF EXISTS hpo;

CREATE TABLE hpo (
  id       CHAR(10) PRIMARY KEY,
  lcname   VARCHAR(256)
);


-- Obsolete and alternate id mappings
DROP TABLE IF EXISTS hp_alt_ids;

CREATE TABLE hp_alt_ids (
  alt_id     char(10) PRIMARY KEY,
  primary_id char(10)
);

--
DROP TABLE IF EXISTS human2fish_orthologs;

CREATE TABLE human2fish_orthologs (
  zfin_gene_id      CHARACTER VARYING(40),
  zfin_gene_symbol  CHARACTER VARYING(100),
  human_gene_symbol CHARACTER VARYING(40),
  entrez_id         CHARACTER VARYING(20)
);

--
-- TOC entry 166 (class 1259 OID 16409)
-- Name: human2mouse_orthologs; Type: TABLE; Schema: public; Owner: -
--
DROP TABLE IF EXISTS human2mouse_orthologs;

CREATE TABLE human2mouse_orthologs (
  mgi_gene_id       CHARACTER VARYING(20),
  mgi_gene_symbol   CHARACTER VARYING(100),
  human_gene_symbol CHARACTER VARYING(40),
  entrez_id         INTEGER
);

--
-- TOC entry 167 (class 1259 OID 16412)
-- Name: mgi_mp; Type: TABLE; Schema: public; Owner: -
--
DROP TABLE IF EXISTS mgi_mp;

CREATE TABLE mgi_mp (
  mgi_gene_id     CHARACTER VARYING(20),
  mgi_gene_symbol CHARACTER VARYING(200),
  mouse_model_id  CHARACTER VARYING(200),
  mp_id           CHARACTER VARYING(3000)
);

DROP TABLE IF EXISTS zfin_zp;

CREATE TABLE zfin_zp (
  zfin_gene_id     CHARACTER VARYING(40),
  zfin_gene_symbol CHARACTER VARYING(200),
  zfin_model_id    CHARACTER VARYING(200),
  zp_id            CHARACTER VARYING(3000)
);

--
-- TOC entry 169 (class 1259 OID 16421)
-- Name: omim; Type: TABLE; Schema: public; Owner: -
--
DROP TABLE IF EXISTS disease;

CREATE TABLE disease (
                         disease_id   VARCHAR(20) NOT NULL,
                         omim_gene_id VARCHAR(20),
                         diseasename  CHARACTER VARYING(2056),
                         gene_id      INTEGER     NOT NULL,
                         type         CHARACTER(1),
                         inheritance CHARACTER VARYING(2)
);

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

DROP TABLE IF EXISTS disease_hp;

CREATE TABLE disease_hp (
  disease_id CHARACTER VARYING(20),
  hp_id      CHARACTER VARYING(3000)
);

DROP TABLE IF EXISTS metadata;

CREATE TABLE metadata (
  resource VARCHAR(1024),
  version  VARCHAR(1024)
);

DROP TABLE IF EXISTS entrez2sym;

CREATE TABLE entrez2sym
(
  entrezid INTEGER PRIMARY KEY,
  symbol   VARCHAR(100)
);
