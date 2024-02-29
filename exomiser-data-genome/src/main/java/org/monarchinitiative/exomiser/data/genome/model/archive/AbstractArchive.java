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
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
abstract class AbstractArchive implements Archive {

    private final Path archivePath;
    private final String archiveFormat;
    private final String dataFileFormat;

    /**
     * @param archivePath    path of the original compressed archive file
     * @param archiveFormat  format of the original compressed archive file - tgz, gz or zip
     * @param dataFileFormat extension of the uncompressed data file inside the archive file - usually vcf, but dbNSFP uses .chr[1-22,X,Y,M]
     */
    protected AbstractArchive(Path archivePath, String archiveFormat, String dataFileFormat) {
        this.archivePath = archivePath;
        this.archiveFormat = archiveFormat;
        this.dataFileFormat = dataFileFormat;
    }

    public Path getPath() {
        return archivePath;
    }

    public String getArchiveFileFormat() {
        return archiveFormat;
    }

    @Override
    public String getDataFileFormat() {
        return dataFileFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractArchive that = (AbstractArchive) o;
        return Objects.equals(archivePath, that.archivePath) &&
                Objects.equals(archiveFormat, that.archiveFormat) &&
                Objects.equals(dataFileFormat, that.dataFileFormat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(archivePath, archiveFormat, dataFileFormat);
    }

    @Override
    public String toString() {
        return "Archive{" +
                "archivePath=" + archivePath +
                ", archiveFormat='" + archiveFormat + '\'' +
                ", dataFileFormat='" + dataFileFormat + '\'' +
                '}';
    }
}
