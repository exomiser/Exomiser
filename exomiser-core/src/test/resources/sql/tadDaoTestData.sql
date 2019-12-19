INSERT INTO tad (chromosome, start, "end", entrezid, symbol)
VALUES
-- tad - two genes
(1, 770137, 1250137, 9636, 'ISG15'),
(1, 770137, 1250137, 7293, 'TNFRSF4'),
--tad new region
(1, 1250137, 1850140, 8510, 'MMP23B'),
--tad new chr
(2, 30346496, 30906496, 81606, 'LBH'),
(2, 30346496, 30906496, 253558, 'LCLAT1'),
(2, 30346496, 30906496, 51646, 'YPEL5'),
--fictitious overlapping tad end inside previous region
(2, 30346496, 30800000, 11111, 'GENE1'),
--fictitious overlapping tad start inside previous region
(2, 30200000, 30800000, 22222, 'GENE2'),
--fictitious overlapping tad start and end inside previous region
(2, 30100000, 30600000, 33333, 'GENE3'),
(2, 30100000, 30600000, 44444, 'GENE4');


