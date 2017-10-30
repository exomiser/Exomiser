/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeData {

    private static final Logger logger = LoggerFactory.getLogger(GenomeData.class);

    private final Path assemblyDataDirectory;

    public GenomeData(GenomeProperties genomeProperties, Path exomiserDataDirectory) {
        this.assemblyDataDirectory = findAssemblyDataDirectory(genomeProperties, exomiserDataDirectory);
    }

    private Path findAssemblyDataDirectory(GenomeProperties genomeProperties, Path exomiserDataDirectory) {
        String versionDir = String.format("%s_%s", genomeProperties.getDataVersion(), genomeProperties.getAssembly());

        String assemblyDataDir = genomeProperties.getDataDirectory();

        if (assemblyDataDir == null || assemblyDataDir.isEmpty()) {
            return exomiserDataDirectory.resolve(versionDir).toAbsolutePath();
        } else {
            return Paths.get(assemblyDataDir).toAbsolutePath();
        }
    }

    public Path getPath() {
        return assemblyDataDirectory;
    }

}
