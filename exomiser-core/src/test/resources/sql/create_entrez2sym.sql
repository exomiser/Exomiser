DROP TABLE IF EXISTS entrez2sym;

CREATE TABLE entrez2sym(
                           entrezID INTEGER PRIMARY KEY,
                           symbol   VARCHAR(100)
);
