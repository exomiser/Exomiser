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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class GenomeDataResolverTest {

    private final Path exomiserDataDirectory = Paths.get("src/test/resources/data");
    private static final String DATA_VERSION = "1710";
    private static final String ASSEMBLY_VERSION = "hg19";
    private static final String BUILD_VERSION = DATA_VERSION + "_" + ASSEMBLY_VERSION;
    private final Path BUILD_DATA_PATH = exomiserDataDirectory.resolve(BUILD_VERSION);


    @Test
    void testNoAssemblyDataDirDefined() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getGenomeAssemblyDataPath(), equalTo(BUILD_DATA_PATH.toAbsolutePath()));
    }

    @Test
    void testAssemblyDataDirOverridesDefault() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");
        genomeProperties.setDataDirectory("src/test/resources/user-defined");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getGenomeAssemblyDataPath(), equalTo(Paths.get("src/test/resources/user-defined")
                .toAbsolutePath()));
    }

    @Test
    void getAssemblyVersion() throws Exception {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getVersionAssemblyPrefix(), equalTo(BUILD_VERSION));
    }

    @Test
    void testResolveAbsoluteResourcePathFromRelativePath() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");
        genomeProperties.setDataDirectory("src/test/resources/data");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        Path relativeFilePath = Paths.get("genome.h2.db");
        assertThat(instance.resolveAbsoluteResourcePath(relativeFilePath), equalTo(Paths.get("src/test/resources/data/genome.h2.db")
                .toAbsolutePath()));
    }

    @Test
    void testResolveAbsoluteResourcePathFromAbsolutePath() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");
        genomeProperties.setDataDirectory("src/test/resources/data");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        Path absoluteFilePath = Paths.get("src/test/resources/user-defined/genome.h2.db").toAbsolutePath();
        assertThat(instance.resolveAbsoluteResourcePath(absoluteFilePath), equalTo(Paths.get("src/test/resources/user-defined/genome.h2.db")
                .toAbsolutePath()));
    }

    @Test
    void testResolveAbsoluteResourcePathFromString() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");
        genomeProperties.setDataDirectory("src/test/resources/user-defined");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        assertThat(instance.resolveAbsoluteResourcePath("genome.h2.db"), equalTo(Paths.get("src/test/resources/user-defined/genome.h2.db")
                .toAbsolutePath()));
    }

    @Test
    void testResolveAbsolutePathOrNullIfEmpty() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);

        assertThat(instance.resolvePathOrNullIfEmpty(null), nullValue());
        assertThat(instance.resolvePathOrNullIfEmpty(""), nullValue());

        assertThat(instance.resolvePathOrNullIfEmpty(BUILD_VERSION + "_transcripts_ensembl.ser"),
                equalTo(BUILD_DATA_PATH.resolve(BUILD_VERSION + "_transcripts_ensembl.ser").toAbsolutePath()));
    }

    @Test
    void testGetTranscriptPathDefault() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getTranscriptFilePath(), equalTo(BUILD_DATA_PATH.resolve(BUILD_VERSION + "_transcripts_ensembl.ser")
                .toAbsolutePath()));
    }

    @Test
    void testGetTranscriptPathRefseq() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");
        genomeProperties.setTranscriptSource("refseq");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getTranscriptFilePath(), equalTo(BUILD_DATA_PATH.resolve(BUILD_VERSION + "_transcripts_refseq.ser")
                .toAbsolutePath()));
    }

    @Test
    void testGetVariantsMvStorePath() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getVariantsMvStorePath(), equalTo(BUILD_DATA_PATH.resolve(BUILD_VERSION + "_variants.mv.db")
                .toAbsolutePath()));
    }

    @Test
    void testGetClinVarMvStorePath() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getClinVarMvStorePath(), equalTo(BUILD_DATA_PATH.resolve(BUILD_VERSION + "_clinvar.mv.db")
                .toAbsolutePath()));
    }

    @Test
    void testGetGenomeDbPath() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");

        GenomeDataResolver instance = new GenomeDataResolver(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getGenomeDbPath(), equalTo(BUILD_DATA_PATH.resolve(BUILD_VERSION + "_genome")
                .toAbsolutePath()));
    }
}