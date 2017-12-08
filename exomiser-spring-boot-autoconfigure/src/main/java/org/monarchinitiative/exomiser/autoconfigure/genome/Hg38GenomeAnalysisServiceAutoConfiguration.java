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

import org.monarchinitiative.exomiser.autoconfigure.DataDirectoryAutoConfiguration;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService;
import org.monarchinitiative.exomiser.core.genome.dao.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@Import({DataDirectoryAutoConfiguration.class})
@ConditionalOnProperty({"exomiser.hg38.data-version"})
@EnableConfigurationProperties(Hg38GenomeProperties.class)
public class Hg38GenomeAnalysisServiceAutoConfiguration extends GenomeAnalysisServiceConfigurer {

    public Hg38GenomeAnalysisServiceAutoConfiguration(Hg38GenomeProperties hg38GenomeProperties, Path exomiserDataDirectory) {
        super(hg38GenomeProperties, exomiserDataDirectory);
    }

    @Bean("hg38genomeAnalysisService")
    public GenomeAnalysisService genomeAnalysisService() {
        return buildGenomeAnalysisService();
    }

    @Bean("hg38defaultFrequencyDao")
    @Override
    public FrequencyDao defaultFrequencyDao() {
        return super.defaultFrequencyDao();
    }

    @Bean("hg38pathogenicityDao")
    @Override
    public PathogenicityDao pathogenicityDao() {
        return super.pathogenicityDao();
    }

    @Bean("hg38localFrequencyDao")
    @Override
    public FrequencyDao localFrequencyDao() {
        return new LocalFrequencyDao(localFrequencyTabixDataSource());
    }

    @Bean("hg38remmDao")
    @Override
    public RemmDao remmDao() {
        return new RemmDao(remmTabixDataSource());
    }

    @Bean("hg38caddDao")
    @Override
    public CaddDao caddDao() {
        return new CaddDao(caddInDelTabixDataSource(), caddSnvTabixDataSource());
    }
}
