--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.9
-- Dumped by pg_dump version 9.3.1
-- Started on 2013-12-05 14:38:16

--
-- TOC entry 176 (class 1259 OID 16513)
-- Name: frequency; Type: TABLE; Schema: public; Owner: -
--


--
-- TOC entry 174 (class 1259 OID 16482)
-- Name: hp_mp_mappings; Type: TABLE; Schema: public; Owner: -
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

--
-- TOC entry 164 (class 1259 OID 16403)
-- Name: hpo; Type: TABLE; Schema: public; Owner: -
-- this looks like a bad idea - 
--

DROP TABLE IF EXISTS hpo;

CREATE TABLE hpo (
  id       CHAR(10) PRIMARY KEY,
  lcname   VARCHAR(256)
);

CREATE INDEX hpoidx
  ON hpo (id);

-- Obsolete and alternate id mappings
DROP TABLE IF EXISTS hp_alt_ids;

CREATE TABLE hp_alt_ids (
  alt_id     char(10) PRIMARY KEY,
  primary_id char(10)
);

CREATE INDEX hpaltidx ON hp_alt_ids (alt_id);

--
-- TOC entry 1850 (class 1259 OID 16468)
-- Name: hpo_term; Type: INDEX; Schema: public; Owner: -
--

-- CREATE INDEX hpo_term ON hpo (hpo_term);

--
-- TOC entry 165 (class 1259 OID 16406)
-- Name: human2fish_orthologs; Type: TABLE; Schema: public; Owner: -
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

--
-- TOC entry 1858 (class 2606 OID 16456)
-- Name: omim_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

--ALTER TABLE disease
--    ADD CONSTRAINT omim_pkey PRIMARY KEY (gene_id, disease_id, omim_gene_id);

CREATE INDEX disease1
  ON disease (gene_id, disease_id);

--
-- TOC entry 1864 (class 2606 OID 16460)
-- Name: variant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

--

CREATE INDEX hp_id
  ON hp_mp_mappings (hp_id);

CREATE INDEX hp_id2
  ON hp_hp_mappings (hp_id);

CREATE INDEX hp_id3
  ON hp_zp_mappings (hp_id);

--
-- TOC entry 1853 (class 1259 OID 16469)
-- Name: human_gene_symbol; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX human_gene_symbol
  ON human2mouse_orthologs (human_gene_symbol);

CREATE INDEX entrez_id
  ON human2mouse_orthologs (entrez_id);
--
-- TOC entry 1851 (class 1259 OID 16470)
-- Name: human_gene_symbol_2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX human_gene_symbol_2
  ON human2fish_orthologs (human_gene_symbol);
CREATE INDEX entrez_id_2
  ON human2fish_orthologs (entrez_id);

--
-- TOC entry 1854 (class 1259 OID 16471)
-- Name: mgi_gene_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mgi_gene_id
  ON human2mouse_orthologs (mgi_gene_id);

--
-- TOC entry 1852 (class 1259 OID 16475)
-- Name: zfin_gene_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX zfin_gene_id
  ON human2fish_orthologs (zfin_gene_id);


CREATE INDEX mgi_gene_id_4
  ON mgi_mp (mgi_gene_id);
CREATE INDEX zfin_gene_id_4
  ON zfin_zp (zfin_gene_id);
CREATE INDEX disease_id_3
  ON disease_hp (disease_id);
-- Completed on 2013-12-05 14:38:16

--
-- PostgreSQL database dump complete
--

