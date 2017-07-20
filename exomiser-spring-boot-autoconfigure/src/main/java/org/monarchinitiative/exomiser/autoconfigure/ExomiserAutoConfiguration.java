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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisFactory;
import org.monarchinitiative.exomiser.core.genome.dao.ErrorThrowingTabixDataSource;
import org.monarchinitiative.exomiser.core.genome.dao.TabixDataSource;
import org.monarchinitiative.exomiser.core.genome.dao.TabixReaderAdaptor;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrixIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@EnableCaching
@ConditionalOnClass({Exomiser.class, AnalysisFactory.class})
@EnableConfigurationProperties(ExomiserProperties.class)
@ComponentScan("org.monarchinitiative.exomiser.core")
public class ExomiserAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserAutoConfiguration.class);

    @Autowired
    private ExomiserProperties properties;

    /**
     * This is critical for the application to run as it points to the data
     * directory where all the required resources are found. Without this being
     * correctly set, the application will fail.
     *
     * @return
     */
    @Bean
    public Path exomiserDataDirectory() {
        Path dataPath = Paths.get(properties.getDataDirectory());
        logger.info("Exomiser data directory set to: {}", dataPath.toAbsolutePath());
        return dataPath;
    }

    private Path resolveRelativeToDataDir(String fileName) {
        return exomiserDataDirectory().resolve(fileName);
    }

    @Bean
    public Path exomiserWorkingDirectory() {
        Path workingDir = Paths.get(getWorkingDir());
        logger.info("Exomiser working directory set to: {}", workingDir.toAbsolutePath());
        return workingDir;
    }

    private String getWorkingDir() {
        if (properties.getWorkingDirectory() != null) {
            return properties.getWorkingDirectory();
        }
        String tempDirectory = System.getProperty("java.io.tmpdir");
        return new File(tempDirectory, "exomiser-data").getAbsolutePath();
    }

    //Variant analysis configuration
    @Bean
    public Path transcriptFilePath() {
        String transcriptFileNameValue = properties.getTranscriptDataFileName();
        Path transcriptFilePath = resolveRelativeToDataDir(transcriptFileNameValue);
        logger.debug("Transcript data file: {}", transcriptFilePath.toAbsolutePath());
        return transcriptFilePath;
    }

    /**
     * This takes a few seconds to de-serialise. Can be overridden by defining your own bean.
     */
    @Lazy
    @Bean
    @ConditionalOnMissingBean
    public JannovarData jannovarData(Path transcriptFilePath) {
        try {
            return new JannovarDataSerializer(transcriptFilePath.toString()).load();
        } catch (SerializationException e) {
            throw new ExomiserAutoConfigurationException("Could not load Jannovar data from " + transcriptFilePath, e);
        }
    }

    /**
     * Optional full system path to CADD InDels.tsv.gz and InDels.tsv.gz.tbi file pair.
     * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    @Lazy
    @Bean
    public TabixDataSource caddInDelTabixDataSource() {
        String caddInDelPath = properties.getCaddInDelPath();
        logTabixPathIfNotEmpty("Reading CADD InDel file from:", caddInDelPath);
        return getTabixDataSourceOrDefaultForProperty(caddInDelPath, PathogenicitySource.CADD.name());
    }

    /**
     * Optional full system path to CADD whole_genome_SNVs.tsv.gz and whole_genome_SNVs.tsv.gz.tbi file pair.
     * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    @Lazy
    @Bean
    public TabixDataSource caddSnvTabixDataSource() {
        String caddSnvPath = properties.getCaddSnvPath();
        logTabixPathIfNotEmpty("Reading CADD snv file from:", caddSnvPath);
        return getTabixDataSourceOrDefaultForProperty(caddSnvPath, PathogenicitySource.CADD.name());
    }

    /**
     * Optional full system path to REMM remmData.tsv.gz and remmData.tsv.gz.tbi file pair.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    @Lazy
    @Bean
    public TabixDataSource remmTabixDataSource() {
        String remmPath = properties.getRemmPath();
        logTabixPathIfNotEmpty("Reading REMM data file from:", remmPath);
        return getTabixDataSourceOrDefaultForProperty(remmPath, PathogenicitySource.REMM.name());
    }

    /**
     * Optional full system path to local frequency .tsv.gz and .tsv.gz.tbi file pair.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    @Lazy
    @Bean
    public TabixDataSource localFrequencyTabixDataSource() {
        String localFrequencyPath = properties.getLocalFrequencyPath();
        logTabixPathIfNotEmpty("Reading LOCAL frequency file from:", localFrequencyPath);
        return getTabixDataSourceOrDefaultForProperty(localFrequencyPath, FrequencySource.LOCAL.name());
    }

    private void logTabixPathIfNotEmpty(String prefixMessage, String tabixPath) {
        if (!tabixPath.isEmpty()) {
            logger.info("{} {}", prefixMessage, tabixPath);
        }
    }

    private TabixDataSource getTabixDataSourceOrDefaultForProperty(String pathToTabixGzFile, String dataSourceName) {
        String tabixGzPathValue = pathToTabixGzFile;
        if (tabixGzPathValue.isEmpty()) {
            logger.warn("Data for {} is not configured. THIS WILL LEAD TO ERRORS IF REQUIRED DURING ANALYSIS. Check the application.properties is pointing to a valid file.", dataSourceName);
            String message = "Data for " + dataSourceName + " is not configured. Check the application.properties is pointing to a valid file.";
            return new ErrorThrowingTabixDataSource(message);
        }
        TabixReader tabixReader;
        try {
            tabixReader = new TabixReader(tabixGzPathValue);
        } catch (IOException e) {
            throw new ExomiserAutoConfigurationException(tabixGzPathValue + " file not found. Please check exomiser properties file points to a valid tabix .gz file.", e);
        }
        return new TabixReaderAdaptor(tabixReader);
    }

    //Prioritiser configuration

    @Bean
    public Path phenixDataDirectory() {
        String phenixDataDirValue = properties.getPhenixDataDir();
        Path phenixDataDirectory = resolveRelativeToDataDir(phenixDataDirValue);
        logger.debug("phenixDataDirectory: {}", phenixDataDirectory.toAbsolutePath());
        return phenixDataDirectory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "hpoOboFilePath")
    public Path hpoOboFilePath() {
        String hpoFileName = properties.getHpoFileName();
        Path hpoFilePath = phenixDataDirectory().resolve(hpoFileName);
        logger.debug("hpoOboFilePath: {}", hpoFilePath.toAbsolutePath());
        return hpoFilePath;
    }

    @Bean
    @ConditionalOnMissingBean(name = "hpoAnnotationFilePath")
    public Path hpoAnnotationFilePath() {
        String hpoAnnotationFileValue = properties.getHpoAnnotationFile();
        Path hpoAnnotationFilePath = phenixDataDirectory().resolve(hpoAnnotationFileValue);
        logger.debug("hpoAnnotationFilePath: {}", hpoAnnotationFilePath.toAbsolutePath());
        return hpoAnnotationFilePath;
    }

    /**
     * This needs a lot of RAM and is slow to create from the randomWalkFile, so
     * it's set as lazy use on the command-line.
     *
     * @return
     */
    @Lazy
    @Bean
    @ConditionalOnMissingBean(name = "randomWalkMatrix")
    public DataMatrix randomWalkMatrix() {
        String randomWalkFileNameValue = properties.getRandomWalkFileName();
        Path randomWalkFilePath = resolveRelativeToDataDir(randomWalkFileNameValue);

        String randomWalkIndexFileNameValue = properties.getRandomWalkIndexFileName();
        Path randomWalkIndexFilePath = resolveRelativeToDataDir(randomWalkIndexFileNameValue);

        return DataMatrixIO.loadDataMatrix(randomWalkFilePath.toString(), randomWalkIndexFilePath.toString(), true);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        HikariDataSource dataSource;

        dataSource = new HikariDataSource(h2Config());

        logger.info("DataSource using maximum of {} database connections", dataSource.getMaximumPoolSize());
        logger.info("Returning a new {} DataSource pool to URL {} user: {}", dataSource.getPoolName(), dataSource.getJdbcUrl(), dataSource.getUsername());
        return dataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public HikariConfig h2Config() {

        ExomiserProperties.H2 h2 = properties.getH2();

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(determineH2Url(h2));
        config.setUsername(h2.getUser());
        config.setPassword(h2.getPassword());
        config.setMaximumPoolSize(h2.getMaxConnections());
        config.setPoolName("exomiser-H2");

        return config;
    }

    private String determineH2Url(ExomiserProperties.H2 h2) {
        //the data path is the default place for the exomiser H2 database to be found.
        if (h2.getDirectory().isEmpty()) {
            logger.info("H2 path not set. Using default data path: {}", exomiserDataDirectory());
            return resolveH2UrlPathPlaceholder(h2.getUrl(), exomiserDataDirectory().toAbsolutePath().toString());
        } else {
            logger.info("Using user defined H2 path: {}", h2.getDirectory());
            return resolveH2UrlPathPlaceholder(h2.getUrl(), h2.getDirectory());
        }
    }

    private String resolveH2UrlPathPlaceholder(String h2Url, String h2AbsolutePath) {
        return h2Url.replace("${h2Path}", h2AbsolutePath);
    }

    @Bean
    public CacheResolver modelCacheResolver() {
        NamedCacheResolver modelCacheResolver = new NamedCacheResolver();
        //these guys are relatively small, always accessed and never grow so were using a ConcurrentMap to store them.
        modelCacheResolver.setCacheNames(Arrays.asList("models"));
        modelCacheResolver.setCacheManager(new ConcurrentMapCacheManager());
        return modelCacheResolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        String cacheOption = properties.getCache();
        //see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/cache.html for how this works
        CacheManager cacheManager;
        List<String> cacheNames = new ArrayList<>();
        switch (cacheOption) {
            case "none":
                cacheManager = noOpCacheManager();
                break;
            case "mem":
                cacheManager = new ConcurrentMapCacheManager("pathogenicity", "frequency", "diseaseHp", "diseases","hpo", "mpo", "zpo", "cadd", "remm");
                cacheNames.addAll(cacheManager.getCacheNames());
                break;
            case "ehcache":
                cacheManager = ehCacheCacheManager();
                cacheNames.addAll(Arrays.asList(ehCacheCacheManager().getCacheManager().getCacheNames()));
                break;
            default:
                String message = String.format("Unrecognised value '%s' for exomiser cache option. Please choose 'none', 'mem' or 'ehcache'.", cacheOption);
                logger.error(message);
                throw new ExomiserAutoConfigurationException(message);
        }
        logger.info("Set up {} caches: {}", cacheOption, cacheNames);
        return cacheManager;
    }

    private NoOpCacheManager noOpCacheManager() {
        logger.info("Caching disabled.");
        return new NoOpCacheManager();
    }

    private EhCacheCacheManager ehCacheCacheManager() {
        return new EhCacheCacheManager(ehCacheManager().getObject());
    }

    @Bean
    @ConditionalOnMissingBean
    public Resource ehCacheConfig() {
        return new ClassPathResource("ehcache.xml");
    }

    @Lazy
    @Bean
    public EhCacheManagerFactoryBean ehCacheManager() {
        Resource ehCacheConfig = ehCacheConfig();
        logger.info("Loading ehcache.xml from {}", ehCacheConfig.getDescription());

        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        ehCacheManagerFactoryBean.setConfigLocation(ehCacheConfig);
        return ehCacheManagerFactoryBean;
    }

}
