/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeDataResolver {

    private final String versionAssemblyPrefix;
    private final Path assemblyDataDirectory;

    public GenomeDataResolver(GenomeProperties genomeProperties, Path exomiserDataDirectory) {
        this.versionAssemblyPrefix = String.format("%s_%s", genomeProperties.getDataVersion(), genomeProperties.getAssembly());
        this.assemblyDataDirectory = findAssemblyDataDirectory(versionAssemblyPrefix, genomeProperties, exomiserDataDirectory);
    }

    private Path findAssemblyDataDirectory(String versionDir, GenomeProperties genomeProperties, Path exomiserDataDirectory) {

        Path assemblyDataDir = genomeProperties.getDataDirectory();

        if (assemblyDataDir == null) {
            return exomiserDataDirectory.resolve(versionDir).toAbsolutePath();
        } else {
            return assemblyDataDir.toAbsolutePath();
        }
    }

    public String getVersionAssemblyPrefix() {
        return versionAssemblyPrefix;
    }

    public Path getGenomeAssemblyDataPath() {
        return assemblyDataDirectory;
    }

    public Path resolveAbsoluteResourcePath(String fileResource) {
        return resolveAbsoluteResourcePath(Paths.get(fileResource));
    }

    public Path resolveAbsoluteResourcePath(Path fileResourcePath) {
        if (fileResourcePath.isAbsolute()) {
            return fileResourcePath;
        }
        return assemblyDataDirectory.resolve(fileResourcePath).toAbsolutePath();
    }
}

