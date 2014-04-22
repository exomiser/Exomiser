--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.9
-- Dumped by pg_dump version 9.3.1
-- Started on 2013-12-05 14:38:16

-- -- SET statement_timeout = 0;
-- -- SET lock_timeout = 0;
-- -- SET client_encoding = 'UTF8';
-- -- SET standard_conforming_strings = on;
-- -- SET check_function_bodies = false;
-- -- SET client_min_messages = warning;
-- 
-- -- SET search_path = public, pg_catalog;
-- 
-- -- SET default_with_oids = false;

--
-- TOC entry 162 (class 1259 OID 16388)
-- Name: esp; Type: TABLE; Schema: public; Owner: -

-- CREATE SCHEMA EXOMISER;
-- SET SCHEMA EXOMISER;

--
-- TOC entry 163 (class 1259 OID 16391)
-- Name: fish_gene_level_summary; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE fish_gene_level_summary (
    disease_id character varying(20),
    zfin_gene_id character varying(20),
    zfin_gene_symbol character varying(100),
    max_combined_perc double precision
);


--
-- TOC entry 176 (class 1259 OID 16513)
-- Name: frequency; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE frequency (
    chromosome smallint,
    "position" integer,
    ref character varying(1024),
    alt character varying(1024),
    rsid integer,
    dbsnpmaf double precision,
    espeamaf double precision,
    espaamaf double precision,
    espallmaf double precision
);


CREATE TABLE clinvar (
    chromosome SMALLINT,
    position INT,
    id  character varying(20), --This is the RSV accession number
    signif  character varying(200)
);
	
CREATE INDEX cvidx ON clinvar(chromosome,position);


--
-- TOC entry 174 (class 1259 OID 16482)
-- Name: hp_mp_mappings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE hp_mp_mappings (
    mapping_id integer,
    hp_id character varying(10),
    mp_id character varying(10),
    score double precision
);


CREATE TABLE mp(
    mp_id       CHAR(10),
    mp_term VARCHAR(256)
);

CREATE TABLE hp_zp_mappings (
    mapping_id integer,
    hp_id character varying(10),
    zp_id character varying(10),
    score double precision
);


CREATE TABLE zp(
    zp_id       CHAR(10),
    zp_term VARCHAR(256)
);

--
-- TOC entry 164 (class 1259 OID 16403)
-- Name: hpo; Type: TABLE; Schema: public; Owner: -
-- this looks like a bad idea - 
--

CREATE TABLE hpo(
    lcname   VARCHAR(256) PRIMARY KEY,
    id       CHAR(10),
    prefname VARCHAR(256));

CREATE INDEX hpoidx ON hpo(id);

--
-- TOC entry 1850 (class 1259 OID 16468)
-- Name: hpo_term; Type: INDEX; Schema: public; Owner: -
--

-- CREATE INDEX hpo_term ON hpo (hpo_term);

--
-- TOC entry 165 (class 1259 OID 16406)
-- Name: human2fish_orthologs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE human2fish_orthologs (
    zfin_gene_id character varying(40),
    zfin_gene_symbol character varying(100),
    human_gene_symbol character varying(20),
    entrez_id character varying(20)
);


--
-- TOC entry 166 (class 1259 OID 16409)
-- Name: human2mouse_orthologs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE human2mouse_orthologs (
    mgi_gene_id character varying(20),
    mgi_gene_symbol character varying(100),
    human_gene_symbol character varying(20),
    entrez_id integer
);


--
-- TOC entry 167 (class 1259 OID 16412)
-- Name: mgi_mp; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE mgi_mp (
    mgi_gene_id character varying(20),
    mgi_gene_symbol character varying(200),
    mouse_model_id integer,
    mp_id character varying(3000)
);

CREATE TABLE zfin_zp (
    zfin_gene_id character varying(20),
    zfin_gene_symbol character varying(200),
    zfin_model_id integer,
    zp_id character varying(3000)
);

--
-- TOC entry 168 (class 1259 OID 16418)
-- Name: mouse_gene_level_summary; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE mouse_gene_level_summary (
    disease_id character varying(20),
    mgi_gene_id character varying(20),
    mgi_gene_symbol character varying(100),
    max_combined_perc double precision
);


--
-- TOC entry 169 (class 1259 OID 16421)
-- Name: omim; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE omim (
    phenmim VARCHAR(12) NOT NULL,
    genemim VARCHAR(12) NOT NULL,
    diseasename character varying(2056),
    gene_id integer NOT NULL,
    type character(1),
    inheritance CHAR
);


--
-- TOC entry 170 (class 1259 OID 16427)
-- Name: omim2gene; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE omim2gene (
    mimdiseaseid integer NOT NULL,
    mimdiseasename character varying(2056),
    cytoband character varying(64),
    mimgeneid integer,
    entrezgeneid integer,
    genesymbol character varying(64),
    seriesid integer NOT NULL
);


--
-- TOC entry 171 (class 1259 OID 16433)
-- Name: omim_terms; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE omim_terms (
    omim_disease_id character varying(20),
    omim_term character varying(512)
);


--
-- TOC entry 172 (class 1259 OID 16439)
-- Name: phenoseries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE phenoseries (
    seriesid integer NOT NULL,
    name character varying(2056)
);


--
-- TOC entry 173 (class 1259 OID 16445)
-- Name: variant; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE variant (
    chromosome smallint NOT NULL,
    "position" integer NOT NULL,
    ref character(1) NOT NULL,
    alt character(1) NOT NULL,
    aaref character(1),
    aaalt character(1),
    aapos integer,
    sift double precision,
    polyphen double precision,
    mut_taster double precision,
    phylop double precision
);


--CREATE TABLE disease_disease_summary (
--    disease_query character varying(20),
--    disease_hit character varying(20),
--    combined_perc double precision
--);

CREATE TABLE hp_hp_mappings (
    mapping_id integer,
    hp_id character varying(10),
    hp_id_hit character varying(10),
    score double precision
);

CREATE TABLE disease_hp (
    disease_id character varying(20),
    hp_id character varying(3000)
);

CREATE TABLE orphanet (
    orphanumber  character varying(20), 
    entrezgeneid integer not null, 
    diseasename  character varying(2056)
);

CREATE TABLE metadata (
    resource VARCHAR(1024),
    version  VARCHAR(1024)
);

CREATE TABLE entrez2sym (
    entrezID INTEGER PRIMARY KEY,
    symbol VARCHAR(24)
);

CREATE TABLE string (
    entrezA INTEGER,
    entrezB INTEGER,
    score INTEGER,
    PRIMARY KEY(entrezA,entrezB)
);

--
-- TOC entry 1860 (class 2606 OID 16454)
-- Name: omim2gene_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

-- ALTER TABLE ONLY omim2gene
ALTER TABLE omim2gene
    ADD CONSTRAINT omim2gene_pkey PRIMARY KEY (mimdiseaseid, seriesid);


--
-- TOC entry 1858 (class 2606 OID 16456)
-- Name: omim_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE omim
    ADD CONSTRAINT omim_pkey PRIMARY KEY (gene_id, phenmim, genemim);


--
-- TOC entry 1862 (class 2606 OID 16458)
-- Name: phenoseries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE phenoseries
    ADD CONSTRAINT phenoseries_pkey PRIMARY KEY (seriesid);


--
-- TOC entry 1864 (class 2606 OID 16460)
-- Name: variant_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--


-- introduced duplicate rows for variants due to alt transcripts so can't have pkey on these cols now
--ALTER TABLE variant
--    ADD CONSTRAINT variant_pkey PRIMARY KEY (chromosome, "position", ref, alt);


CREATE INDEX variant1 ON variant (chromosome, "position", ref, alt);


-- CREATE INDEX disease_hit ON disease_disease_summary (disease_hit);
-- CREATE INDEX disease_query ON disease_disease_summary (disease_query);

--
-- TOC entry 1867 (class 1259 OID 16519)
-- Name: freqqq; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX freqqq ON frequency (chromosome, "position", ref, alt);


--
-- TOC entry 1865 (class 1259 OID 16485)
-- Name: hp_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX hp_id ON hp_mp_mappings (hp_id);

CREATE INDEX hp_id2 ON hp_hp_mappings (hp_id);

CREATE INDEX hp_id3 ON hp_zp_mappings (hp_id);

--
-- TOC entry 1853 (class 1259 OID 16469)
-- Name: human_gene_symbol; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX human_gene_symbol ON human2mouse_orthologs (human_gene_symbol);

CREATE INDEX entrez_id ON human2mouse_orthologs (entrez_id);
--
-- TOC entry 1851 (class 1259 OID 16470)
-- Name: human_gene_symbol_2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX human_gene_symbol_2 ON human2fish_orthologs (human_gene_symbol);
CREATE INDEX entrez_id_2 ON human2fish_orthologs (entrez_id);

--
-- TOC entry 1854 (class 1259 OID 16471)
-- Name: mgi_gene_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mgi_gene_id ON human2mouse_orthologs (mgi_gene_id);


--
-- TOC entry 1855 (class 1259 OID 16472)
-- Name: mgi_gene_id_2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX mgi_gene_id_2 ON mouse_gene_level_summary (mgi_gene_id);


--
-- TOC entry 1856 (class 1259 OID 16473)
-- Name: disease_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX disease_id ON mouse_gene_level_summary (disease_id);


--
-- TOC entry 1848 (class 1259 OID 16474)
-- Name: disease_id_2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX disease_id_2 ON fish_gene_level_summary (disease_id);


--
-- TOC entry 1852 (class 1259 OID 16475)
-- Name: zfin_gene_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX zfin_gene_id ON human2fish_orthologs (zfin_gene_id);


--
-- TOC entry 1849 (class 1259 OID 16476)
-- Name: zfin_gene_id_2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX zfin_gene_id_2 ON fish_gene_level_summary (zfin_gene_id);


CREATE INDEX mgi_gene_id_4 ON mgi_mp (mgi_gene_id);
CREATE INDEX zfin_gene_id_4 ON zfin_zp (zfin_gene_id);
CREATE INDEX disease_id_3 ON disease_hp (disease_id);
-- Completed on 2013-12-05 14:38:16

--
-- PostgreSQL database dump complete
--

