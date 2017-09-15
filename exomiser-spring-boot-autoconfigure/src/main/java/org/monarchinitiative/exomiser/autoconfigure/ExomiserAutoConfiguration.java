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

import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisFactory;
import org.monarchinitiative.exomiser.core.analysis.SettingsParser;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.genome.dao.RegulatoryFeatureDao;
import org.monarchinitiative.exomiser.core.genome.dao.TadDao;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@ConditionalOnClass({Exomiser.class, AnalysisFactory.class})
@EnableConfigurationProperties(ExomiserProperties.class)
@Import({DataDirectoryAutoConfiguration.class,
        ExomiserCacheAutoConfiguration.class,
        PhenotypeDataSourceAutoConfiguration.class,
        PrioritiserAutoConfiguration.class,
        VariantDataServiceAutoConfiguration.class,
        TranscriptSourceAutoConfiguration.class})
@ComponentScan({"org.monarchinitiative.exomiser.core"})
public class ExomiserAutoConfiguration {

    @Bean
    public GenomeAnalysisServiceProvider genomeAnalysisServiceProvider(GenomeAnalysisService genomeAnalysisService) {
        return new GenomeAnalysisServiceProvider(genomeAnalysisService);
    }

    //TODO: each GenomeAnalysisService will need to be manually configured as they are identical apart from the data they contain and the path they should be loaded from.
    //TODO: Create a GenomeAnalysisServiceLoader to help loading these?
    @Bean
    public GenomeAnalysisService genomeAnalysisService(GenomeDataService genomeDataService, VariantDataService variantDataService) {
        return new GenomeAnalysisServiceImpl(GenomeAssembly.HG19, genomeDataService, variantDataService);
    }

    @Bean
    public GenomeDataService genomeDataService(GeneFactory geneFactory, RegulatoryFeatureDao regulatoryFeatureDao, TadDao tadDao) {
        return new GenomeDataServiceImpl(geneFactory, regulatoryFeatureDao, tadDao);
    }

    //TODO: This is a hack in order to wire this up in the interim - probably going to remove this class as its no longer used.
    @Bean
    public SettingsParser settingsParser(PriorityFactory priorityFactory, GenomeAnalysisServiceProvider genomeAnalysisServiceProvider) {
        return new SettingsParser(priorityFactory, genomeAnalysisServiceProvider.getDefaultAssemblyAnalysisService());
    }
}
