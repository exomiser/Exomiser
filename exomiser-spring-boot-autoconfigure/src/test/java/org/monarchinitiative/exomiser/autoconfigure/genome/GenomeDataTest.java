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

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeDataTest {

    private final Path exomiserDataDirectory = Paths.get("src/test/resources/data");

    @Test
    public void testNoAssemblyDataDirDefined() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");

        GenomeData instance = new GenomeData(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getPath(), equalTo(Paths.get("src/test/resources/data/1710_hg19").toAbsolutePath()));
    }

    @Test
    public void testAssemblyDataDirOverridesDefault() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");
        genomeProperties.setDataDirectory("src/test/resources/user-defined");

        GenomeData instance = new GenomeData(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getPath(), equalTo(Paths.get("src/test/resources/user-defined").toAbsolutePath()));
    }

    @Test
    public void getAssemblyVersion() throws Exception {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");

        GenomeData instance = new GenomeData(genomeProperties, exomiserDataDirectory);
        assertThat(instance.getVersionAssemblyPrefix(), equalTo("1710_hg19"));
    }

    @Test
    public void testResolveAbsoluteResourcePathFromRelativePath() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");
        genomeProperties.setDataDirectory("src/test/resources/data");

        GenomeData instance = new GenomeData(genomeProperties, exomiserDataDirectory);
        Path relativeFilePath = Paths.get("genome.h2.db");
        assertThat(instance.resolveAbsoluteResourcePath(relativeFilePath), equalTo(Paths.get("src/test/resources/data/genome.h2.db")
                .toAbsolutePath()));
    }

    @Test
    public void testResolveAbsoluteResourcePathFromAbsolutePath() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");
        genomeProperties.setDataDirectory("src/test/resources/data");

        GenomeData instance = new GenomeData(genomeProperties, exomiserDataDirectory);
        Path absoluteFilePath = Paths.get("src/test/resources/user-defined/genome.h2.db").toAbsolutePath();
        assertThat(instance.resolveAbsoluteResourcePath(absoluteFilePath), equalTo(Paths.get("src/test/resources/user-defined/genome.h2.db")
                .toAbsolutePath()));
    }

    @Test
    public void testResolveAbsoluteResourcePathFromString() {
        GenomeProperties genomeProperties = new Hg19GenomeProperties();
        genomeProperties.setDataVersion("1710");
        genomeProperties.setDataDirectory("src/test/resources/user-defined");

        GenomeData instance = new GenomeData(genomeProperties, exomiserDataDirectory);
        assertThat(instance.resolveAbsoluteResourcePath("genome.h2.db"), equalTo(Paths.get("src/test/resources/user-defined/genome.h2.db")
                .toAbsolutePath()));
    }
}