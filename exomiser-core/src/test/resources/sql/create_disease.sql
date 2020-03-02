DROP TABLE IF EXISTS disease;

CREATE TABLE disease
(
    disease_id   VARCHAR(20) NOT NULL,
    omim_gene_id VARCHAR(20),
    diseasename  CHARACTER VARYING(2056),
    gene_id      INTEGER     NOT NULL,
    type         CHARACTER(1),
    inheritance  CHARACTER VARYING(2)
);
