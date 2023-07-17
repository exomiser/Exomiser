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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import org.monarchinitiative.exomiser.core.genome.jannovar.TranscriptSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class used for resolving files in the genome data directories e.g. 1909_hg19.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeDataResolver {

    private static final Logger logger = LoggerFactory.getLogger(GenomeDataResolver.class);

    private final GenomeProperties genomeProperties;

    private final String versionAssemblyPrefix;
    private final Path assemblyDataDirectory;

    public GenomeDataResolver(GenomeProperties genomeProperties, Path exomiserDataDirectory) {
        this.genomeProperties = genomeProperties;

        this.versionAssemblyPrefix = String.format("%s_%s", genomeProperties.getDataVersion(), genomeProperties.getAssembly());
        this.assemblyDataDirectory = resolveAssemblyDataDirectory(versionAssemblyPrefix, exomiserDataDirectory);
        logger.debug("Created resource resolver for release {} (transcript-source={})", versionAssemblyPrefix, genomeProperties
                .getTranscriptSource());
    }

    private Path resolveAssemblyDataDirectory(String versionAssemblyPrefix, Path exomiserDataDirectory) {
        Path assemblyDataDir = genomeProperties.getDataDirectory();
        if (assemblyDataDir == null) {
            return exomiserDataDirectory.resolve(versionAssemblyPrefix).toAbsolutePath();
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

    @Nullable
    public Path resolvePathOrNullIfEmpty(String fileResource) {
        if (fileResource == null || fileResource.isEmpty()) {
            return null;
        }
        return resolveAbsoluteResourcePath(fileResource);
    }

    public Path getTranscriptFilePath() {
        TranscriptSource transcriptSource = genomeProperties.getTranscriptSource();
        //e.g 1710_hg19_transcripts_ucsc.ser
        String transcriptFileNameValue = String.format("%s_transcripts_%s.ser", versionAssemblyPrefix, transcriptSource
                .toString());
        return assemblyDataDirectory.resolve(transcriptFileNameValue);
    }

    public Path getVariantsMvStorePath() {
        String mvStoreFileName = String.format("%s_variants.mv.db", versionAssemblyPrefix);
        return resolveAbsoluteResourcePath(mvStoreFileName);
    }

    public Path getClinVarMvStorePath() {
        String clinvarDataVersion = genomeProperties.getClinVarDataVersion();
        String fileVersion = clinvarDataVersion.isEmpty() ? versionAssemblyPrefix : clinvarDataVersion + "_" + genomeProperties.getAssembly();
        String mvStoreFileName = String.format("%s_clinvar.mv.db", fileVersion);
        return resolveAbsoluteResourcePath(mvStoreFileName);
    }

    public Path getGenomeDbPath() {
        //omit the .h2.db extensions
        String dbFileName = String.format("%s_genome", versionAssemblyPrefix);
        return resolveAbsoluteResourcePath(dbFileName);
    }
}

