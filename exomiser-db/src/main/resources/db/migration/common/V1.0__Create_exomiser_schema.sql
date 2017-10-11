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
DROP TABLE IF EXISTS frequency;

CREATE TABLE frequency (
    chromosome smallint,
    "position" integer,
    ref character varying(1024),
    alt character varying(1024),
    rsid integer,
    dbsnpmaf double precision,
    espeamaf double precision,
    espaamaf double precision,
    espallmaf double precision,
    exacafrmaf double precision,
    exacamrmaf double precision,
    exaceasmaf double precision,
    exacfinmaf double precision,
    exacnfemaf double precision,
    exacothmaf double precision,
    exacsasmaf double precision
);

-- DROP TABLE IF EXISTS regulatory_features;
-- 
-- CREATE TABLE regulatory_features (
--     chromosome smallint,
--     start integer,
--     "end" integer,
--     feature_type character varying(200),
--     tissue character varying(200)
-- );

DROP TABLE IF EXISTS REGULATORY_REGIONS;

CREATE TABLE REGULATORY_REGIONS (
    CHROMOSOME SMALLINT,
    START INTEGER,
    "end" INTEGER,
    FEATURE_TYPE VARCHAR(200)
);
CREATE INDEX RR1 ON REGULATORY_REGIONS (CHROMOSOME, START, "end");


DROP TABLE IF EXISTS tad;

create table tad (
    chromosome smallint NOT NULL, 
    start integer not null, 
    "end" integer not null, 
    entrezID INTEGER not null, 
    symbol varchar(24)
);

--
-- TOC entry 173 (class 1259 OID 16445)
-- Name: variant; Type: TABLE; Schema: public; Owner: -
--
DROP TABLE IF EXISTS pathogenicity;

CREATE TABLE pathogenicity (
    chromosome smallint NOT NULL,
    "position" integer NOT NULL,
    ref character(1) NOT NULL,
    alt character(1) NOT NULL,
    sift double precision,
    polyphen double precision,
    mut_taster double precision,
    cadd double precision,
    cadd_raw double precision 
);


DROP TABLE IF EXISTS clinvar;

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
DROP TABLE IF EXISTS hp_mp_mappings;

CREATE TABLE hp_mp_mappings (
    mapping_id integer,
    hp_id character varying(10),
    hp_term character varying(200),
    mp_id character varying(10),
    mp_term character varying(200),
    simJ double precision,
    ic double precision,
    score double precision,
    lcs_id character varying(10),
    lcs_term character varying(150)
);

DROP TABLE IF EXISTS mp;

CREATE TABLE mp(
    mp_id       CHAR(10),
    mp_term VARCHAR(256)
);

DROP TABLE IF EXISTS hp_zp_mappings;

CREATE TABLE hp_zp_mappings (
    mapping_id integer,
    hp_id character varying(10),
    hp_term character varying(200),
    zp_id character varying(10),
    zp_term character varying(200),
    simJ double precision,
    ic double precision,
    score double precision,
    lcs_id character varying(10),
    lcs_term character varying(150)
);

DROP TABLE IF EXISTS zp;

CREATE TABLE zp(
    zp_id       CHAR(10),
    zp_term VARCHAR(256)
);

--
-- TOC entry 164 (class 1259 OID 16403)
-- Name: hpo; Type: TABLE; Schema: public; Owner: -
-- this looks like a bad idea - 
--

DROP TABLE IF EXISTS hpo;

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
DROP TABLE IF EXISTS human2fish_orthologs;

CREATE TABLE human2fish_orthologs (
    zfin_gene_id character varying(40),
    zfin_gene_symbol character varying(100),
    human_gene_symbol character varying(40),
    entrez_id character varying(20)
);


--
-- TOC entry 166 (class 1259 OID 16409)
-- Name: human2mouse_orthologs; Type: TABLE; Schema: public; Owner: -
--
DROP TABLE IF EXISTS human2mouse_orthologs;

CREATE TABLE human2mouse_orthologs (
    mgi_gene_id character varying(20),
    mgi_gene_symbol character varying(100),
    human_gene_symbol character varying(40),
    entrez_id integer
);


--
-- TOC entry 167 (class 1259 OID 16412)
-- Name: mgi_mp; Type: TABLE; Schema: public; Owner: -
--
DROP TABLE IF EXISTS mgi_mp;

CREATE TABLE mgi_mp (
    mgi_gene_id character varying(20),
    mgi_gene_symbol character varying(200),
    mouse_model_id integer,
    mp_id character varying(3000)
);

DROP TABLE IF EXISTS zfin_zp;

CREATE TABLE zfin_zp (
    zfin_gene_id character varying(20),
    zfin_gene_symbol character varying(200),
    zfin_model_id integer,
    zp_id character varying(3000)
);



--
-- TOC entry 169 (class 1259 OID 16421)
-- Name: omim; Type: TABLE; Schema: public; Owner: -
--
DROP TABLE IF EXISTS disease;

CREATE TABLE disease (
    disease_id VARCHAR(20) NOT NULL,
    omim_gene_id VARCHAR(20),
    diseasename character varying(2056),
    gene_id integer NOT NULL,
    type character(1),
    inheritance character(2)
);


--
-- TOC entry 170 (class 1259 OID 16427)
-- Name: omim2gene; Type: TABLE; Schema: public; Owner: -
--
DROP TABLE IF EXISTS omim2gene;

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
DROP TABLE IF EXISTS omim_terms;

CREATE TABLE omim_terms (
    omim_disease_id character varying(20),
    omim_term character varying(512)
);


--
-- TOC entry 172 (class 1259 OID 16439)
-- Name: phenoseries; Type: TABLE; Schema: public; Owner: -
--
DROP TABLE IF EXISTS phenoseries;

CREATE TABLE phenoseries (
    seriesid integer NOT NULL,
    name character varying(2056),
    genecount INTEGER
);


--CREATE TABLE disease_disease_summary (
--    disease_query character varying(20),
--    disease_hit character varying(20),
--    combined_perc double precision
--);
DROP TABLE IF EXISTS hp_hp_mappings;

CREATE TABLE hp_hp_mappings (
    mapping_id integer,
    hp_id character varying(10),
    hp_term character varying(200),
    hp_id_hit character varying(10),
    hp_hit_term character varying(200),
    simJ double precision,
    ic double precision,
    score double precision,
    lcs_id character varying(10),
    lcs_term character varying(150)
);

DROP TABLE IF EXISTS disease_hp;

CREATE TABLE disease_hp (
    disease_id character varying(20),
    hp_id character varying(3000)
);

--CREATE TABLE orphanet (
--    orphanumber  character varying(20), 
--    entrezgeneid integer not null, 
--    diseasename  character varying(2056)
--);
DROP TABLE IF EXISTS metadata;

CREATE TABLE metadata (
    resource VARCHAR(1024),
    version  VARCHAR(1024)
);

DROP TABLE IF EXISTS entrez2sym;

CREATE TABLE entrez2sym (
    entrezID INTEGER PRIMARY KEY,
    symbol VARCHAR(24)
);

DROP TABLE IF EXISTS string;

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

--ALTER TABLE disease
--    ADD CONSTRAINT omim_pkey PRIMARY KEY (gene_id, disease_id, omim_gene_id);

CREATE INDEX disease1 ON disease (gene_id, disease_id);

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


CREATE INDEX path_idx
  ON pathogenicity (chromosome, "position", ref, alt);


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

-- CREATE INDEX e2 ON regulatory_features (chromosome, start, "end");
-- CREATE INDEX e5 ON regulatory_features (chromosome);
-- CREATE INDEX e6 ON regulatory_features (start);
-- CREATE INDEX e7 ON regulatory_features ("end");

create index tad1 on tad (chromosome,start,"end");






--
-- TOC entry 1852 (class 1259 OID 16475)
-- Name: zfin_gene_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX zfin_gene_id ON human2fish_orthologs (zfin_gene_id);




CREATE INDEX mgi_gene_id_4 ON mgi_mp (mgi_gene_id);
CREATE INDEX zfin_gene_id_4 ON zfin_zp (zfin_gene_id);
CREATE INDEX disease_id_3 ON disease_hp (disease_id);
-- Completed on 2013-12-05 14:38:16

--
-- PostgreSQL database dump complete
--

