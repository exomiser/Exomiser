DROP TABLE IF EXISTS entrez2sym;

CREATE TABLE entrez2sym (
    entrezID integer PRIMARY KEY,
    symbol character varying(24)
);
