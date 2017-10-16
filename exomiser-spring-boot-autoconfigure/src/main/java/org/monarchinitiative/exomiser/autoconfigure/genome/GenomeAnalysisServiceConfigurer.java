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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import org.monarchinitiative.exomiser.autoconfigure.ExomiserAutoConfigurationException;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.genome.dao.*;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.nio.file.Path;

/**
 * Acts as a manual version of Spring component discovery and DI. This is required as there can be more than one
 * {@link org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService} present to provide data for different genome
 * assemblies. The {@link org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService} has multiple layers of
 * dependencies and as such is not a simple to construct, hence this class.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public abstract class GenomeAnalysisServiceConfigurer implements GenomeAnalysisServiceConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GenomeAnalysisServiceConfigurer.class);

    private final GenomeProperties genomeProperties;

    private final String filePrefix;
    private final Path assemblyDataDirectory;

    protected final DataSource dataSource;
    private final JannovarData jannovarData;

    public GenomeAnalysisServiceConfigurer(GenomeProperties genomeProperties, Path exomiserDataDirectory) {
        this.genomeProperties = genomeProperties;
        logger.info("Configuring {} GenomeAnalysisService (data-version={}, transcript-source={})", genomeProperties.getAssembly(), genomeProperties
                .getDataVersion(), genomeProperties.getTranscriptSource());
        filePrefix = String.format("%s_%s", genomeProperties.getDataVersion(), genomeProperties.getAssembly());
        //exomiser-cli/data/1710_hg19/ //TODO: provide the path directly?
        assemblyDataDirectory = exomiserDataDirectory.resolve(filePrefix);

        jannovarData = loadJannovarData();
        dataSource = loadGenomeDataSource();

        logger.info("{}", genomeProperties.getDatasource());
    }

    private VariantFactory variantFactory() {
        return new VariantFactoryJannovarImpl(new JannovarVariantAnnotator(genomeProperties.getAssembly(), jannovarData));
    }

    private GenomeDataService genomeDataService() {
        RegulatoryFeatureDao regulatoryFeatureDao = new RegulatoryFeatureDao(dataSource);
        TadDao tadDao = new TadDao(dataSource);
        GeneFactory geneFactory = new GeneFactory(jannovarData);
        return new GenomeDataServiceImpl(geneFactory, regulatoryFeatureDao, tadDao);
    }

    private VariantDataService variantDataService() {
        return VariantDataServiceImpl.builder()
                .defaultFrequencyDao(defaultFrequencyDao())
                .localFrequencyDao(localFrequencyDao())
                .pathogenicityDao(pathogenicityDao())
                .remmDao(remmDao())
                .caddDao(caddDao())
                .build();
    }

    // The protected methods here are exposed so that the concrete sub-classes can call these as a bean method in order that
    // Spring can intercept any caching annotations, but otherwise keep the duplicated GenomeAnalysisServices separate from
    // any autowiring and autoconfiguration which will cause name clashes.
    protected GenomeAnalysisService buildGenomeAnalysisService() {
        return new GenomeAnalysisServiceImpl(genomeProperties.getAssembly(), genomeDataService(), variantDataService(), variantFactory());
    }

    @Override
    public FrequencyDao defaultFrequencyDao() {
        if (genomeProperties.getFrequencyPath().isEmpty()) {
            return new DefaultFrequencyDao(dataSource);
        }
        return new DefaultFrequencyDaoTabix(defaultFrequencyTabixDataSource());
    }

    @Override
    public PathogenicityDao pathogenicityDao() {
        if (genomeProperties.getPathogenicityPath().isEmpty()) {
            return new DefaultPathogenicityDao(dataSource);
        }
        return new DefaultPathogenicityDaoTabix(defaultPathogenicityTabixDataSource());
    }

    protected TabixDataSource defaultFrequencyTabixDataSource() {
        String frequencyPath = genomeProperties.getFrequencyPath();
        logger.info("Reading variant frequency data from tabix {}", frequencyPath);
        return TabixDataSourceLoader.load(frequencyPath);
    }

    protected TabixDataSource defaultPathogenicityTabixDataSource() {
        String pathogenicityPath = genomeProperties.getPathogenicityPath();
        logger.info("Reading variant pathogenicity data from tabix {}", pathogenicityPath);
        return TabixDataSourceLoader.load(pathogenicityPath);
    }

    /**
     * Optional full system path to CADD InDels.tsv.gz and InDels.tsv.gz.tbi file pair.
     * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    protected TabixDataSource caddInDelTabixDataSource() {
        String caddInDelPath = genomeProperties.getCaddInDelPath();
        logTabixPathIfNotEmpty("Reading CADD InDel file from:", caddInDelPath);
        return getTabixDataSourceOrDefaultForProperty(caddInDelPath, "CADD InDel");
    }


    /**
     * Optional full system path to CADD whole_genome_SNVs.tsv.gz and whole_genome_SNVs.tsv.gz.tbi file pair.
     * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    protected TabixDataSource caddSnvTabixDataSource() {
        String caddSnvPath = genomeProperties.getCaddSnvPath();
        logTabixPathIfNotEmpty("Reading CADD snv file from:", caddSnvPath);
        return getTabixDataSourceOrDefaultForProperty(caddSnvPath, "CADD snv");
    }

    /**
     * Optional full system path to REMM remmData.tsv.gz and remmData.tsv.gz.tbi file pair.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    protected TabixDataSource remmTabixDataSource() {
        String remmPath = genomeProperties.getRemmPath();
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
    protected TabixDataSource localFrequencyTabixDataSource() {
        String localFrequencyPath = genomeProperties.getLocalFrequencyPath();
        logTabixPathIfNotEmpty("Reading LOCAL frequency file from:", localFrequencyPath);
        return getTabixDataSourceOrDefaultForProperty(localFrequencyPath, FrequencySource.LOCAL.name());
    }

    private void logTabixPathIfNotEmpty(String prefixMessage, String tabixPath) {
        if (!tabixPath.isEmpty()) {
            logger.info("{} {}", prefixMessage, tabixPath);
        }
    }

    private TabixDataSource getTabixDataSourceOrDefaultForProperty(String pathToTabixGzFile, String dataSourceName) {
        if (pathToTabixGzFile.isEmpty()) {
            logger.warn("Data for {} is not configured. THIS WILL LEAD TO ERRORS IF REQUIRED DURING ANALYSIS. Check the application.properties is pointing to a valid file.", dataSourceName);
            String message = "Data for " + dataSourceName + " is not configured. Check the application.properties is pointing to a valid file.";
            return new ErrorThrowingTabixDataSource(message);
        }
        return TabixDataSourceLoader.load(pathToTabixGzFile);
    }

    //TODO: test this can be overridden in the properties
    private Path transcriptFilePath() {
        TranscriptSource transcriptSource = genomeProperties.getTranscriptSource();
        //e.g 1710_hg19_transcripts_ucsc.ser
        String transcriptFileNameValue = String.format("%s_transcripts_%s.ser", filePrefix, transcriptSource.toString());
        Path transcriptFilePath = assemblyDataDirectory.resolve(transcriptFileNameValue);
        logger.info("Using {} transcript source for {}", transcriptSource, genomeProperties.getAssembly());
        return transcriptFilePath;
    }

    /**
     * This takes a few seconds to de-serialise. Can be overridden by defining your own bean.
     */
    private JannovarData loadJannovarData() {
        Path transcriptFilePath = transcriptFilePath();
        try {
            return new JannovarDataSerializer(transcriptFilePath.toString()).load();
        } catch (SerializationException e) {
            throw new ExomiserAutoConfigurationException("Could not load Jannovar data from " + transcriptFilePath, e);
        }
    }

    private DataSource loadGenomeDataSource() {
        return new HikariDataSource(genomeDataSourceConfig());
    }

    private HikariConfig genomeDataSourceConfig() {
        //omit the .h2.db extensions
        String dbFileName = String.format("%s_exomiser_genome", filePrefix);

        Path dbPath = assemblyDataDirectory.resolve(dbFileName);

        String startUpArgs = ";MODE=PostgreSQL;SCHEMA=EXOMISER;DATABASE_TO_UPPER=FALSE;IFEXISTS=TRUE;AUTO_RECONNECT=TRUE;ACCESS_MODE_DATA=r;";

        String jdbcUrl = String.format("jdbc:h2:file:%s%s", dbPath.toAbsolutePath(), startUpArgs);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(3);
        config.setPoolName(String.format("exomiser-genome-%s-%s", genomeProperties.getAssembly(), genomeProperties.getDataVersion()));
        return config;
    }
}
