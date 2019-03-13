/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import org.apache.commons.vfs2.FileName;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbNsfp4AlleleArchive extends AbstractAlleleArchive {

    public DbNsfp4AlleleArchive(Path archivePath) {
        super(archivePath, "zip", "gz");
    }

    @Override
    public Stream<String> lines() {
        // this needs to be abstract too as it needs a separate implementation for dbNSFP4
        ArchiveFileReader archiveFileReader = new DbNsfp4ArchiveFileReader(this);
        return archiveFileReader.lines();
    }

    private class DbNsfp4ArchiveFileReader extends ArchiveFileReader {

        public DbNsfp4ArchiveFileReader(AlleleArchive alleleArchive) {
            super(alleleArchive);
        }

        @Override
        boolean isWanted(FileName fileObjectName) {
            // wanted:
            // dbNSFP4.0b1a_variant.chr1.gz
            // dbNSFP4.0b1a_variant.chrM.gz
            //
            // not wanted:
            // dbNSFP4.0b1_gene.complete.gz
            return fileObjectName.getExtension().startsWith(dataFileFormat) && fileObjectName.getBaseName().contains("_variant.chr");
        }
    }
}
