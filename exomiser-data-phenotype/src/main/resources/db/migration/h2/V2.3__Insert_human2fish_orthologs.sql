/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

INSERT INTO human2fish_orthologs SELECT *
                                 FROM CSVREAD('${import.path}/human2fishOrthologs.pg',
                                              'zfin_gene_id|zfin_gene_symbol|human_gene_symbol|entrez_id',
                                              'charset=UTF-8 fieldDelimiter='' fieldSeparator=| nullString=NULL');

CREATE INDEX h2fo_zfin_gene_id
    ON human2fish_orthologs (zfin_gene_id);

CREATE INDEX h2fo_entrez_id
    ON human2fish_orthologs (entrez_id);

CREATE INDEX h2fo_human_gene_symbol
    ON human2fish_orthologs (human_gene_symbol);