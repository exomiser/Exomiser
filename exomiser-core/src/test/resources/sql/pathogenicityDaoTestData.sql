DROP TABLE variant IF EXISTS;

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
    phylop double precision,
    cadd double precision,
    cadd_raw double precision 
);

insert into variant values (10, 123256215, 'T', 'G', null, null, null, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0);
