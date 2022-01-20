/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.autoconfigure.AbstractAutoConfigurationTest;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GenomeAnalysisServiceAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    public void genomeAnalysisServiceProviderFailsWithNoGenomeAnalysisServiceSpecified() {
        BeanCreationException result = assertThrows(BeanCreationException.class, () -> load(EmptyConfiguration.class, TEST_DATA_ENV));
        assertThat(result.getRootCause().getMessage(), equalTo("No org.monarchinitiative.exomiser.core.genome.GenomeAnalysisService instance provided! You must specify at least one exomiser genome assembly data version in the application.properties file."));
    }

    @Test
    public void genomeAnalysisServiceProviderHg19Only() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg19.data-version=1710");

        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = (GenomeAnalysisServiceProvider) context.getBean("genomeAnalysisServiceProvider");
        assertThat(genomeAnalysisServiceProvider.getProvidedAssemblies(), equalTo(Set.of(GenomeAssembly.HG19)));
    }

    @Test
    public void genomeAnalysisServiceProviderHg38Only() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg38.data-version=1710");

        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = (GenomeAnalysisServiceProvider) context.getBean("genomeAnalysisServiceProvider");
        assertThat(genomeAnalysisServiceProvider.getProvidedAssemblies(), equalTo(Set.of(GenomeAssembly.HG38)));
    }

    @Test
    public void genomeAnalysisServiceProviderAllAssemblies() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.hg19.data-version=1710", "exomiser.hg38.data-version=1710");

        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = (GenomeAnalysisServiceProvider) context.getBean("genomeAnalysisServiceProvider");
        assertThat(genomeAnalysisServiceProvider.getProvidedAssemblies(), equalTo(Set.of(GenomeAssembly.HG19, GenomeAssembly.HG38)));
    }

    @Configuration
    @ImportAutoConfiguration(value = GenomeAnalysisServiceAutoConfiguration.class)
    protected static class EmptyConfiguration {
        @Bean
        public CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }
}