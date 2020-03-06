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

package org.monarchinitiative.exomiser.data.genome.model.archive;

import java.nio.file.Path;

/**
 * AlleleArchive for dbSNP build 152+. These are named using the RefSeq accession for each assembly e.g.
 * GCF_000001405.25.gz (GRCh37.p12) or GCF_000001405.38.gz (GRCh38.p13). Annoyingly the broke the tradition of 20 years
 * and removed the .vcf part of the file extension so this new and special class was needed to replace the {@link TabixAlleleArchive}
 * <p>
 * https://www.ncbi.nlm.nih.gov/variation/docs/snp2_human_variation_vcf/
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbSnpAlleleArchive extends AbstractAlleleArchive {

    public DbSnpAlleleArchive(Path archivePath) {
        super(archivePath, "gz", "");
    }
}
