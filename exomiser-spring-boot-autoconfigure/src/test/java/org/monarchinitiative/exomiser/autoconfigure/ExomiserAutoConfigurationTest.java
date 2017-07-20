/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.autoconfigure;

import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.HikariConfig;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.monarchinitiative.exomiser.core.genome.dao.ErrorThrowingTabixDataSource;
import org.monarchinitiative.exomiser.core.genome.dao.TabixDataSource;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserAutoConfigurationTest {

    private AnnotationConfigApplicationContext context;

    private static final Path TEST_DATA = Paths.get("src/test/resources/data");
    private static final String TEST_DATA_ENV = "exomiser.data-directory=" + TEST_DATA;

    @After
    public void tearDown() {
        if (this.context != null) {
            this.context.close();
        }
    }

    private void load(Class<?> config, String... environment) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        EnvironmentTestUtils.addEnvironment(applicationContext, environment);
        applicationContext.register(config);
        applicationContext.register(ExomiserAutoConfiguration.class);
        applicationContext.refresh();
        this.context = applicationContext;
    }

    @Test
    public void testDataPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        Path exomiserDataDirectory = (Path) this.context.getBean("exomiserDataDirectory");
        assertThat(exomiserDataDirectory.getFileName(), equalTo(Paths.get("data")));
    }

    @Test
    public void testWorkingDirectoryPathDefaultIsTempDir() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        Path workingDirectory = (Path) this.context.getBean("exomiserWorkingDirectory");
        assertThat(workingDirectory.getFileName(), equalTo(Paths.get("exomiser-data")));
        assertThat(workingDirectory.getParent(), equalTo(Paths.get(System.getProperty("java.io.tmpdir"))));
    }

    @Test
    public void testCanSpecifyWorkingDirectory() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.working-directory=" + TEST_DATA + "/wibble");
        Path workingDirectory = (Path) this.context.getBean("exomiserWorkingDirectory");
        assertThat(workingDirectory.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(workingDirectory.getParent(), equalTo(TEST_DATA));
    }

    @Test
    public void transcriptFilePathIsDefinedRelativeToDataPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.transcript-data-file-name=ucsc.ser");
        Path ucscFilePath = (Path) this.context.getBean("transcriptFilePath");
        assertThat(ucscFilePath.getFileName(), equalTo(Paths.get("ucsc.ser")));
        assertThat(ucscFilePath.getParent(), equalTo(TEST_DATA));
    }

    /**
     * We're testing against empty placeholder files here so we're expecting an error
     */
    @Test(expected = RuntimeException.class)
    public void testJannovarData() {
        load(NoJannovarOverrideConfiguration.class, TEST_DATA_ENV, "exomiser.transcript-data-file-name=ucsc.ser");
        JannovarData jannovarData = (JannovarData) this.context.getBean("jannovarData");
    }

    @Test
    public void testJannovarDataCanBeOverridden() {
        load(BeanOverrideConfiguration.class, TEST_DATA_ENV, "exomiser.transcriptDataFileName=hg19_ucsc.ser");
        JannovarData jannovarData = (JannovarData) this.context.getBean("jannovarData");
        assertThat(jannovarData, not(nullValue()));
    }

    @Test(expected = RuntimeException.class)
    public void loadTabixFileThrowsRuntimeExceptionWhenFileNotFound() {
        String testTabixFilePath = TEST_DATA.resolve("wibble.tsv.gz").toAbsolutePath().toString();
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.caddSnvPath=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddSnvTabixDataSource");
    }

    @Test
    public void loadCaddSnvTabixFromPlaceholderWhenNotDefined() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddSnvTabixDataSource");
        assertThat(tabixDataSource, instanceOf(ErrorThrowingTabixDataSource.class));
    }

    @Test
    public void loadCaddSnvTabixFileFromFullPathWhenDefined() {
        String testTabixFilePath = TEST_DATA.resolve("whole_genome_SNVs.tsv.gz").toAbsolutePath().toString();
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.caddSnvPath=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddSnvTabixDataSource");
        assertThat(tabixDataSource.getSource(), equalTo(testTabixFilePath));
    }

    @Test
    public void loadCaddIndelTabixFromPlaceholderWhenNotDefined() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddInDelTabixDataSource");
        assertThat(tabixDataSource, instanceOf(ErrorThrowingTabixDataSource.class));
    }

    @Test
    public void loadCaddIndelTabixFileFromFullPathWhenDefined() {
        String testTabixFilePath = TEST_DATA.resolve("InDels.tsv.gz").toAbsolutePath().toString();
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.caddInDelPath=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("caddInDelTabixDataSource");
        assertThat(tabixDataSource.getSource(), equalTo(testTabixFilePath));
    }

    @Test
    public void loadRemmTabixFromPlaceholderWhenNotDefined() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("remmTabixDataSource");
        assertThat(tabixDataSource, instanceOf(ErrorThrowingTabixDataSource.class));
    }

    @Test
    public void loadRemmTabixFileFromFullPathWhenDefined() {
        String testTabixFilePath = TEST_DATA.resolve("remmData.tsv.gz").toAbsolutePath().toString();
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.remmPath=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("remmTabixDataSource");
        assertThat(tabixDataSource.getSource(), equalTo(testTabixFilePath));
    }

    @Test
    public void loadLocalFrequencyTabixFromPlaceholderWhenNotDefined() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("localFrequencyTabixDataSource");
        assertThat(tabixDataSource, instanceOf(ErrorThrowingTabixDataSource.class));
    }

    @Test
    public void loadLocalFrequencyTabixFileFromFullPathWhenDefined() {
        String testTabixFilePath = TEST_DATA.resolve("placeholder.tsv.gz").toAbsolutePath().toString();
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.local-frequency-path=" + testTabixFilePath);
        TabixDataSource tabixDataSource = (TabixDataSource) context.getBean("localFrequencyTabixDataSource");
        assertThat(tabixDataSource.getSource(), equalTo(testTabixFilePath));
    }

    @Test
    public void phenixDirectoryDefaultNameIsDefinedRelativeToDataPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        Path phenixDataDirectory = (Path) this.context.getBean("phenixDataDirectory");
        assertThat(phenixDataDirectory.getFileName(), equalTo(Paths.get("phenix")));
        assertThat(phenixDataDirectory.getParent(), equalTo(TEST_DATA));
    }

    @Test
    public void phenixDirectoryIsDefinedRelativeToDataPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.phenixDataDir=wibble");
        Path phenixDataDirectory = (Path) this.context.getBean("phenixDataDirectory");
        assertThat(phenixDataDirectory.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(phenixDataDirectory.getParent(), equalTo(TEST_DATA));
    }


    @Test
    public void hpoFileDefaultIsDefinedRelativeToPhenixPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        Path path = (Path) this.context.getBean("hpoOboFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("hp.obo")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoFileIsDefinedRelativeToPhenixPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hpoFileName=wibble");
        Path path = (Path) this.context.getBean("hpoOboFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoFileBeanCanBeOverridden() {
        load(BeanOverrideConfiguration.class, TEST_DATA_ENV, "exomiser.hpoFileName=wibble");
        Path path = (Path) this.context.getBean("hpoOboFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("hpo.obo")));
        assertThat(path.getParent(), equalTo((Paths.get("/another/data/dir"))));
    }

    @Test
    public void hpoAnnotationFileDefaultIsDefinedRelativeToPhenixPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        Path path = (Path) this.context.getBean("hpoAnnotationFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoAnnotationFileIsDefinedRelativeToDataPath() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hpoAnnotationFile=wibble");
        Path path = (Path) this.context.getBean("hpoAnnotationFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(path.getParent(), equalTo((Path) this.context.getBean("phenixDataDirectory")));
    }

    @Test
    public void hpoAnnotationFileBeanCanBeOverridden() {
        load(BeanOverrideConfiguration.class, TEST_DATA_ENV, "exomiser.hpoAnnotationFile=wibble");
        Path path = (Path) this.context.getBean("hpoAnnotationFilePath");
        assertThat(path.getFileName(), equalTo(Paths.get("hpo.annotations")));
        assertThat(path.getParent(), equalTo((Paths.get("/another/data/dir"))));
    }

    @Test(expected = Exception.class)
    public void randomWalkMatrixDefault() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        DataMatrix dataMatrix = (DataMatrix) context.getBean("randomWalkMatrix");
    }

    @Test
    public void randomWalkMatrixCanBeOverriden() {
        load(BeanOverrideConfiguration.class, TEST_DATA_ENV, "exomiser.randomWalkFileName=wibble", "exomiser.randomWalkIndexFileName=wibbleIndex");
        DataMatrix dataMatrix = (DataMatrix) context.getBean("randomWalkMatrix");
        assertThat(dataMatrix, not(nullValue()));
    }

    @Test
    public void exomiserH2DefaultConfig() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        HikariConfig hikariConfig = (HikariConfig) context.getBean("h2Config");
        assertThat(hikariConfig.getDriverClassName(), equalTo("org.h2.Driver"));
        assertThat(hikariConfig.getMaximumPoolSize(), equalTo(3));
        assertThat(hikariConfig.getUsername(), equalTo("sa"));
        assertThat(hikariConfig.getPassword(), equalTo(""));
        assertThat(hikariConfig.getJdbcUrl(), startsWith("jdbc:h2:file:"+ TEST_DATA.toAbsolutePath()+"/exomiser;"));
    }

    @Test
    public void exomiserH2ConfigUserDefinedH2PathNoUrlDefined() {
        load(EmptyConfiguration.class, TEST_DATA_ENV,
                "exomiser.h2.directory=wibble",
                "exomiser.h2.user=wibble",
                "exomiser.h2.password=wibble",
                "exomiser.h2.max-connections=999");

        HikariConfig hikariConfig = (HikariConfig) context.getBean("h2Config");
        assertThat(hikariConfig.getDriverClassName(), equalTo("org.h2.Driver"));
        assertThat(hikariConfig.getMaximumPoolSize(), equalTo(999));
        assertThat(hikariConfig.getUsername(), equalTo("wibble"));
        assertThat(hikariConfig.getPassword(), equalTo("wibble"));
        assertThat(hikariConfig.getJdbcUrl(), startsWith("jdbc:h2:file:wibble/exomiser;"));
    }

    @Test
    public void exomiserH2ConfigUserDefinedH2PathUrlDefined() {
        load(EmptyConfiguration.class, TEST_DATA_ENV,
                "exomiser.h2.directory=wibble",
                "exomiser.h2.user=wibble",
                "exomiser.h2.password=wibble",
                "exomiser.h2.url=jdbc:h2:mem:exomiser",
                "exomiser.h2.max-connections=999");

        HikariConfig hikariConfig = (HikariConfig) context.getBean("h2Config");
        assertThat(hikariConfig.getDriverClassName(), equalTo("org.h2.Driver"));
        assertThat(hikariConfig.getMaximumPoolSize(), equalTo(999));
        assertThat(hikariConfig.getUsername(), equalTo("wibble"));
        assertThat(hikariConfig.getPassword(), equalTo("wibble"));
        assertThat(hikariConfig.getJdbcUrl(), equalTo("jdbc:h2:mem:exomiser"));
    }

    @Test
    public void exomiserH2ConfigCanBeOveridden() {
        load(H2OverrideConfiguration.class, TEST_DATA_ENV);
        HikariConfig hikariConfig = (HikariConfig) context.getBean("h2Config");
        assertThat(hikariConfig.getDriverClassName(), equalTo("org.h2.Driver"));
        assertThat(hikariConfig.getMaximumPoolSize(), equalTo(3));
        assertThat(hikariConfig.getJdbcUrl(), startsWith("jdbc:h2:mem:exomiser"));
    }

    @Test
    public void dataSource() throws Exception {
        load(H2OverrideConfiguration.class, TEST_DATA_ENV);
        DataSource dataSource = (DataSource) context.getBean("dataSource");
        assertThat(dataSource, not(nullValue()));
        assertThat(dataSource.getConnection().isValid(1), is(true));
    }

    @Test
    public void cachingDisabledByDefault() {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames().isEmpty(), is(true));
    }

    @Test(expected = RuntimeException.class)
    public void cachingThrowsExceptionWhenNameNotRecognised() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.cache=wibble");
    }

    @Test
    public void cachingCanBeDisabledExplicitly() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.cache=none");
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames().isEmpty(), is(true));
    }

    @Test
    public void cachingInMemCanBeDefined() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.cache=mem");
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames(), hasItems("pathogenicity", "frequency", "diseaseHp", "diseases","hpo", "mpo", "zpo", "cadd", "remm"));
    }

    @Test
    public void cachingEhCacheCanBeDefined() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.cache=ehcache");
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames(), hasItems("pathogenicity", "frequency", "diseaseHp", "diseases","hpo", "mpo", "zpo", "cadd", "remm"));
    }

    @Test
    public void cachingCanBeOverridden() {
        load(BeanOverrideConfiguration.class, TEST_DATA_ENV, "exomiser.cache=ehcache");
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames(), hasItems("wibble"));
    }

    @Configuration
    static class NoJannovarOverrideConfiguration {
        /*
         * Mock this otherwise we'll try connecting to a non-existent database.
         */
        @Bean
        public DataSource dataSource() {
            return Mockito.mock(DataSource.class);
        }
    }

    @Configuration
    static class EmptyConfiguration {

        /*
         * Mock this otherwise we'll try connecting to a non-existent database.
         */
        @Bean
        public DataSource dataSource() {
            return Mockito.mock(DataSource.class);
        }

        @Bean
        public JannovarData jannovarData() {
            return new JannovarData(HG19RefDictBuilder.build(), ImmutableList.of());
        }

    }

    @Configuration
    static class BeanOverrideConfiguration extends EmptyConfiguration {

        @Bean
        public JannovarData jannovarData() {
            return new JannovarData(HG19RefDictBuilder.build(), ImmutableList.of());
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
            return Mockito.mock(DataMatrix.class);
        }

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("wibble");
        }

    }

    @Configuration
    static class H2OverrideConfiguration {

        @Bean
        public JannovarData jannovarData() {
            return new JannovarData(HG19RefDictBuilder.build(), ImmutableList.of());
        }

        @Bean
        public HikariConfig h2Config() {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.h2.Driver");
            config.setJdbcUrl("jdbc:h2:mem:exomiser");
            config.setMaximumPoolSize(3);
            config.setPoolName("exomiser-H2-mem");
            return config;
        }
    }

}