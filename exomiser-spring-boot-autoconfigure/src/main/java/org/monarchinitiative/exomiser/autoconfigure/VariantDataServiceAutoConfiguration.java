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

package org.monarchinitiative.exomiser.autoconfigure;

import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.genome.dao.ErrorThrowingTabixDataSource;
import org.monarchinitiative.exomiser.core.genome.dao.TabixDataSource;
import org.monarchinitiative.exomiser.core.genome.dao.TabixReaderAdaptor;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@ConditionalOnClass(VariantDataService.class)
@EnableConfigurationProperties(ExomiserProperties.class)
public class VariantDataServiceAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(VariantDataServiceAutoConfiguration.class);

    /**
     * Optional full system path to CADD InDels.tsv.gz and InDels.tsv.gz.tbi file pair.
     * These can be downloaded from http://cadd.gs.washington.edu/download - v1.3 has been tested.
     * <p>
     * Default is empty and will return no data.
     *
     * @return
     */
    @Bean
    public TabixDataSource caddInDelTabixDataSource(ExomiserProperties properties) {
        String caddInDelPath = properties.getCaddInDelPath();
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
    @Bean
    public TabixDataSource caddSnvTabixDataSource(ExomiserProperties properties) {
        String caddSnvPath = properties.getCaddSnvPath();
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
    @Bean
    public TabixDataSource remmTabixDataSource(ExomiserProperties properties) {
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
    @Bean
    public TabixDataSource localFrequencyTabixDataSource(ExomiserProperties properties) {
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

}
