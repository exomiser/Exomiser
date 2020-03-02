DROP TABLE IF EXISTS frequency;

CREATE TABLE frequency
(
    chromosome smallint,
    "position" integer,
    ref        character varying(1024),
    alt        character varying(1024),
    rsid       integer,
    dbsnpmaf   double precision,
    espeamaf   double precision,
    espaamaf   double precision,
    espallmaf double precision,
    exacafrmaf double precision,
    exacamrmaf double precision,
    exaceasmaf double precision,
    exacfinmaf double precision,
    exacnfemaf double precision,
    exacothmaf double precision,
    exacsasmaf double precision
);

