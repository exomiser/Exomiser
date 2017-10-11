INSERT INTO pathogenicity VALUES
--CHROMOSOME, position, REF, ALT, SIFT, POLYPHEN, MUT_TASTER, CADD, CADD_RAW
-- tests for null scores
(1, 1, 'A', 'T', null, 0.998, 1.0, 23.7, 4.452675),
(1, 2, 'A', 'T', 0.0, null, 1.0, 23.7, 4.452675),
(1, 3, 'A', 'T', 0.0, 0.998, null, 23.7, 4.452675),
(1, 4, 'A', 'T', 0.0, 0.998, 1.0, null, 4.452675),
-- single allele with multiple rows but with varying best scores
(1, 5, 'A', 'T', 0.0, 0.998, 1.0, null, 4.452675),
(1, 5, 'A', 'T', 1.0, 0.001, 1.0, 23.7, 4.452675),
-- multiple alleles - some real data
(10, 123256215, 'T', 'A', 0.0, 0.998, 1.0, 23.9, 4.467705),
(10, 123256215, 'T', 'C', 0.0, 0.998, 1.0, 25.0, 4.589662),
(10, 123256215, 'T', 'G', 0.0, 0.998, 1.0, 23.7, 4.452675);
