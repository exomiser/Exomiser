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

import org.monarchinitiative.exomiser.autoconfigure.genome.Hg19GenomeAnalysisServiceAutoConfiguration;
import org.monarchinitiative.exomiser.autoconfigure.genome.Hg38GenomeAnalysisServiceAutoConfiguration;
import org.monarchinitiative.exomiser.autoconfigure.phenotype.PrioritiserAutoConfiguration;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.analysis.AnalysisFactory;
import org.monarchinitiative.exomiser.core.analysis.SettingsParser;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@EnableConfigurationProperties({ExomiserProperties.class}) //, Hg19GenomeProperties.class, Hg38GenomeProperties.class
@Import({DataDirectoryAutoConfiguration.class,
        ExomiserCacheAutoConfiguration.class,
        PrioritiserAutoConfiguration.class,
        Hg19GenomeAnalysisServiceAutoConfiguration.class,
        Hg38GenomeAnalysisServiceAutoConfiguration.class
})
@ComponentScan(basePackageClasses = {Exomiser.class}, basePackages = {"org.monarchinitiative.exomiser.core.analysis"})
public class ExomiserAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserAutoConfiguration.class);

    @Bean
    public GenomeAnalysisServiceProvider genomeAnalysisServiceProvider(GenomeAnalysisService... genomeAnalysisServices) {
        return new GenomeAnalysisServiceProvider(genomeAnalysisServices);
    }

    //TODO: This is a hack in order to wire this up in the interim - probably going to remove this class as its no longer used.
    @Bean
    public SettingsParser settingsParser(PriorityFactory priorityFactory, GenomeAnalysisServiceProvider genomeAnalysisServiceProvider) {
        return new SettingsParser(priorityFactory, genomeAnalysisServiceProvider.get(GenomeAssembly.HG19));
    }
}
