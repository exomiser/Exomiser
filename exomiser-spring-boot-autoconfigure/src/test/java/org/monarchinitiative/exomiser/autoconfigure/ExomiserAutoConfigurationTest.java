/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatchService;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserAutoConfigurationTest extends AbstractAutoConfigurationTest {

    // n.b there are issues with the @PreDestroy shutdown hooks for classes in the GenomeAnalysisServiceConfigurer using the MVStore
    // so all the relevant beans are being tested in one go
    @Test
    public void testAutoConfiguration() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg19.data-version=1710", "exomiser.hg38.data-version=1710", "exomiser.phenotype.data-version=1710");
        Exomiser exomiser = (Exomiser) context.getBean("exomiser");
        assertThat(exomiser, instanceOf(Exomiser.class));

        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = (GenomeAnalysisServiceProvider) context.getBean("genomeAnalysisServiceProvider");
        assertThat(genomeAnalysisServiceProvider, instanceOf(GenomeAnalysisServiceProvider.class));
        assertThat(genomeAnalysisServiceProvider.hasServiceFor(GenomeAssembly.HG19), is(true));
        assertThat(genomeAnalysisServiceProvider.hasServiceFor(GenomeAssembly.HG38), is(true));

        PhenotypeMatchService phenotypeMatchService = (PhenotypeMatchService) context.getBean("phenotypeMatchService");
        assertThat(phenotypeMatchService, instanceOf(PhenotypeMatchService.class));
    }

    @Configuration
    @ImportAutoConfiguration(value = ExomiserAutoConfiguration.class)
    protected static class EmptyConfiguration {

        @Bean
        public CacheManager noOpCacheManager() {
            return new NoOpCacheManager();
        }
    }

}