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

insert into frequency values
(10, 123256215, 'T', 'G', null, null, null, null, null),
(10, 123256215, 'T', 'G', 121918506, 0.01, 0.04, 0.03, 0.02);

