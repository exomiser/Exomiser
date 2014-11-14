DROP TABLE disease IF EXISTS ;

CREATE TABLE disease (
    disease_id VARCHAR(20) NOT NULL,
    omim_gene_id VARCHAR(20),
    diseasename character varying(2056),
    gene_id integer NOT NULL,
    type character(1),
    inheritance CHAR
);
