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

import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@ConditionalOnClass(GenomeAnalysisService.class)
@Import({
        Hg19GenomeAnalysisServiceAutoConfiguration.class,
        Hg38GenomeAnalysisServiceAutoConfiguration.class,
        VariantCacheConfiguration.class})
public class GenomeAnalysisServiceAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GenomeAnalysisServiceAutoConfiguration.class);

    @Bean
    public GenomeAnalysisServiceProvider genomeAnalysisServiceProvider(GenomeAnalysisService... genomeAnalysisServices) {
        Arrays.stream(genomeAnalysisServices)
                .forEach(service -> logger.info("Configured {} genome analysis service", service.getGenomeAssembly()));
        return new GenomeAnalysisServiceProvider(genomeAnalysisServices);
    }

}
