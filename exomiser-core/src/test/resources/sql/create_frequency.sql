DROP TABLE FREQUENCY IF EXISTS;

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

