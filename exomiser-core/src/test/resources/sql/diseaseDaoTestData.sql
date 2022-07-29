/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

insert into disease
values ('OMIM:101600', 'OMIM:176943', 'Craniofacial-skeletal-dermatologic dysplasia', 2263, 'D', 'D'),
       ('OMIM:101600', 'OMIM:136350', 'Pfeiffer syndrome', 2260, 'D', 'D'),
       ('ORPHA:11111', null, 'Test CNV disease', 2222, 'C', null),
       ('OMIM:123456', null, 'Test unconfirmed disease association', 3333, '?', null),
       ('OMIM:234567', null, 'Test susceptibility disease association', 4444, 'S', null),
       ('OMIM:765432', null, 'Test multi-disease gene disease 1', 5555, 'D', null),
       ('OMIM:765433', null, 'Test multi-disease gene disease 2', 5555, 'D', null);


insert into disease_hp
values ('OMIM:101600',
        'HP:0000174,HP:0000194,HP:0000218,HP:0000238,HP:0000244,HP:0000272,HP:0000303,HP:0000316,HP:0000322,HP:0000324,HP:0000327,HP:0000348,HP:0000431,HP:0000452,HP:0000453,HP:0000470,HP:0000486,HP:0000494,HP:0000508,HP:0000586,HP:0000678,HP:0001156,HP:0001249,HP:0002308,HP:0002676,HP:0002780,HP:0003041,HP:0003070,HP:0003196,HP:0003272,HP:0003307,HP:0003795,HP:0004209,HP:0004322,HP:0004440,HP:0005048,HP:0005280,HP:0005347,HP:0006101,HP:0006110,HP:0009602,HP:0009773,HP:0010055,HP:0010669,HP:0011304'),
       ('ORPHA:11111', 'HP:0000001'),
       ('OMIM:123456', 'HP:0000002'),
       ('OMIM:234567', 'HP:0000002'),
       ('OMIM:765432', 'HP:0000003'),
       ('OMIM:765433', 'HP:0000004');


insert into entrez2sym
VALUES (2263, 'FGFR2'),
       (2260, 'FGFR1'),
       (2222, 'GENE2'),
       (3333, 'GENE3'),
       (4444, 'GENE4'),
       (5555, 'GENE5');