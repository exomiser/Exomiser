/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure.phenotype;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.autoconfigure.AbstractAutoConfigurationTest;
import org.monarchinitiative.exomiser.autoconfigure.DataDirectoryAutoConfiguration;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PrioritiserAutoConfigurationTest extends AbstractAutoConfigurationTest {

    private static final String DATA_VERSION = "exomiser.phenotype.data-version=1710";

    @Test
    public void doesNotLoadWhenPhenotypeDataVersionIsAbsent() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean("phenotypeDataDirectory"));
    }

    @Test
    public void onlyLoadsWhenPhenotypeDataVersionIsPresent() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION);
        assertThat(context.getBean("phenotypeDataDirectory"), not(nullValue()));
    }

    @Test
    public void canDefinePhenotypeDataDirectory() {
        Path definedDir = TEST_DATA.resolve("1710_phenotype");
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION, "exomiser.phenotype.data-directory=" + definedDir);
        Path phenotypeDataDirectory = (Path) this.context.getBean("phenotypeDataDirectory");
        assertThat(phenotypeDataDirectory, equalTo(definedDir));
    }

    @Test
    public void phenixDirectoryDefaultNameIsDefinedRelativeToPhenotypeDataDirectory() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION);
        Path phenixDataDirectory = (Path) this.context.getBean("phenixDataDirectory");
        assertThat(phenixDataDirectory.getFileName(), equalTo(Paths.get("phenix")));
        assertThat(phenixDataDirectory.getParent(), equalTo((Path) this.context.getBean("phenotypeDataDirectory")));
    }

    @Test
    public void phenixDirectoryIsDefinedRelativeToPhenotypeDataDirectory() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION, "exomiser.phenotype.phenix-data-dir=wibble");
        Path phenixDataDirectory = (Path) this.context.getBean("phenixDataDirectory");
        assertThat(phenixDataDirectory.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(phenixDataDirectory.getParent(), equalTo((Path) this.context.getBean("phenotypeDataDirectory")));
    }


    @Test
    public void hpoFileDefaultIsDefinedRelativeToPhenixPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION);
        Path path = (Path) this.context.getBean("hpoOboFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("hp.obo")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoFileIsDefinedRelativeToPhenixPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION, "exomiser.phenotype.hpoFileName=wibble");
        Path path = (Path) this.context.getBean("hpoOboFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoFileBeanCanBeOverridden() {
        load(UserConfiguration.class, TEST_DATA_ENV, DATA_VERSION, "exomiser.phenotype.hpoFileName=wibble");
        Path path = (Path) this.context.getBean("hpoOboFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("hpo.obo")));
        assertThat(path.getParent(), equalTo((Paths.get("/another/data/dir"))));
    }

    @Test
    public void hpoAnnotationFileDefaultIsDefinedRelativeToPhenixPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION);
        Path path = (Path) this.context.getBean("hpoAnnotationFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoAnnotationFileIsDefinedRelativeToDataPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION, "exomiser.phenotype.hpoAnnotationFile=wibble");
        Path path = (Path) this.context.getBean("hpoAnnotationFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoAnnotationFileBeanCanBeOverridden() {
        load(UserConfiguration.class, TEST_DATA_ENV, DATA_VERSION, "exomiser.phenotype.hpoAnnotationFile=wibble");
        Path path = (Path) this.context.getBean("hpoAnnotationFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("hpo.annotations")));
        assertThat(path.getParent(), equalTo((Paths.get("/another/data/dir"))));
    }

    @Test
    public void randomWalkMatrixDefault() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION);
        DataMatrix dataMatrix = (DataMatrix) context.getBean("randomWalkMatrix");
        assertThat(dataMatrix, not(nullValue()));
        assertThat(dataMatrix.numRows(), equalTo(0));
        assertThat(dataMatrix.numColumns(), equalTo(0));
    }

    @Test
    public void randomWalkMatrixLoadInMemory() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, DATA_VERSION, "exomiser.phenotype.random-walk-preload=true");
        DataMatrix dataMatrix = (DataMatrix) context.getBean("randomWalkMatrix");
        assertThat(dataMatrix, not(nullValue()));
        assertThat(dataMatrix.numRows(), equalTo(0));
        assertThat(dataMatrix.numColumns(), equalTo(0));
    }

    @Test
    public void randomWalkMatrixCanBeOverriden() {
        load(UserConfiguration.class, TEST_DATA_ENV, DATA_VERSION, "exomiser.phenotype.randomWalkFileName=wibble.gz", "exomiser.randomWalkIndexFileName=wibbleIndex.gz");
        DataMatrix dataMatrix = (DataMatrix) context.getBean("randomWalkMatrix");
        assertThat(dataMatrix, not(nullValue()));
    }

    @Configuration
    @ImportAutoConfiguration(PrioritiserAutoConfiguration.class)
    protected static class EmptyConfiguration {

        @Bean
        public CacheManager noOpCacheManager() {
            return new NoOpCacheManager();
        }

    }

    @Configuration
    @Import({DataDirectoryAutoConfiguration.class, EmptyConfiguration.class})
    protected static class UserConfiguration {

        @Bean
        public Path phenixDataDirectory() {
            return Paths.get("/another/data/dir/");
        }

        @Bean
        public Path hpoOboFilePath() {
            return Paths.get("/another/data/dir/hpo.obo");
        }

        @Bean
        public Path hpoAnnotationFilePath() {
            return Paths.get("/another/data/dir/hpo.annotations");
        }

        @Bean
        public DataMatrix randomWalkMatrix() {
            return DataMatrix.empty();
        }
    }
}