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

package org.monarchinitiative.exomiser.autoconfigure.genome;

import de.charite.compbio.jannovar.data.JannovarData;
import org.h2.mvstore.MVStore;
import org.monarchinitiative.exomiser.autoconfigure.DataDirectoryAutoConfiguration;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.genome.GenomeDataService;
import org.monarchinitiative.exomiser.core.genome.VariantAnnotator;
import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.genome.dao.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import jakarta.annotation.PreDestroy;
import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@Import({DataDirectoryAutoConfiguration.class})
@PropertySource("classpath:application-default.properties")
@PropertySource("classpath:application-default-jdbc.properties")
@ConditionalOnProperty({"exomiser.hg19.data-version"})
@EnableConfigurationProperties(Hg19GenomeProperties.class)
public class Hg19GenomeAnalysisServiceAutoConfiguration extends GenomeAnalysisServiceConfigurer {

    public Hg19GenomeAnalysisServiceAutoConfiguration(Hg19GenomeProperties hg19GenomeProperties, Path exomiserDataDirectory) {
        super(hg19GenomeProperties, exomiserDataDirectory);
    }

    @Bean("hg19jannovarData")
    public JannovarData jannovarData() {
        return jannovarData;
    }

    @Bean("hg19mvStore")
    public MVStore mvStore() {
        return allelesMvStore;
    }

    /**
     * Only one instance of an MVStore can access the store on disk at a time in a single JVM. This prevents tests failing
     * when the store hasn't been properly closed.
     */
    @PreDestroy
    public synchronized void closeMvStore() {
        allelesMvStore.close();
    }

    @Bean("hg19clinVarStore")
    public MVStore clinVarStore() {
        return clinVarMvStore;
    }

    @PreDestroy
    public synchronized void closeClinVarMvStore() {
        clinVarMvStore.close();
    }

    @Bean("hg19variantAnnotator")
    @Override
    public VariantAnnotator variantAnnotator() {
        return super.buildVariantAnnotator();
    }

    @Bean("hg19variantDataService")
    @Override
    public VariantDataService variantDataService() {
        return super.buildVariantDataService();
    }

    @Bean("hg19genomeDataService")
    @Override
    public GenomeDataService genomeDataService() {
        return super.buildGenomeDataService();
    }

    @Bean("hg19VariantWhiteList")
    @Override
    public VariantWhiteList variantWhiteList() {
        return genomeDataSourceLoader.getVariantWhiteList();
    }

    //These require Spring to manage the caching and are called by buildVariantDataService
    @Bean("hg19genomeAnalysisService")
    @Override
    public GenomeAnalysisService genomeAnalysisService() {
        return buildGenomeAnalysisService();
    }

    @Bean("hg19allelePropertiesDao")
    @Override
    public AllelePropertiesDao allelePropertiesDao() {
        return new AllelePropertiesDaoMvStore(genomeDataSourceLoader.getAllelePropsMvStore());
    }

    @Bean("hg19localFrequencyDao")
    @Override
    public FrequencyDao localFrequencyDao() {
        return new LocalFrequencyDao(genomeDataSourceLoader.getLocalFrequencyTabixDataSource());
    }

    @Bean("hg19remmDao")
    @Override
    public RemmDao remmDao() {
        return new RemmDao(genomeDataSourceLoader.getRemmTabixDataSource());
    }

    @Bean("hg19caddDao")
    @Override
    public CaddDao caddDao() {
        return new CaddDao(genomeDataSourceLoader.getCaddIndelTabixDataSource(), genomeDataSourceLoader.getCaddSnvTabixDataSource());
    }

    @Bean("hg19testPathDao")
    @Override
    public PathogenicityDao testPathScoreDao() {
        return new TestPathogenicityScoreDao(genomeDataSourceLoader.getTestPathogenicityTabixDataSource());
    }

    @Bean("hg19svFrequencyDao")
    @Override
    public FrequencyDao svFrequencyDao() {
        return new SvFrequencyDao(genomeDataSource);
    }

    @Bean("hg19svPathogenicityDao")
    @Override
    public PathogenicityDao svPathogenicityDao() {
        return new SvPathogenicityDao(genomeDataSource);
    }
}
