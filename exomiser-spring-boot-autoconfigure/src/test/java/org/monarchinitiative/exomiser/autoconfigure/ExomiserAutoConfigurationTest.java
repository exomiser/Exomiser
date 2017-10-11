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

import org.junit.Test;
import org.mockito.Mockito;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.genome.*;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ExomiserAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    public void testExomiserIsAutoConfigured() throws Exception {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        Exomiser exomiser = (Exomiser) context.getBean("exomiser");
        assertThat(exomiser, instanceOf(Exomiser.class));
    }

    @Test
    public void testGenomeAnalysisServiceProviderIsAutoConfigured() throws Exception {
        load(EmptyConfiguration.class, TEST_DATA_ENV);
        GenomeAnalysisServiceProvider genomeAnalysisServiceProvider = (GenomeAnalysisServiceProvider) context.getBean("genomeAnalysisServiceProvider");
        assertThat(genomeAnalysisServiceProvider, instanceOf(GenomeAnalysisServiceProvider.class));
    }

    @Configuration
    @ImportAutoConfiguration(value = ExomiserAutoConfiguration.class)
    protected static class EmptyConfiguration {

        @Bean
        public GenomeAnalysisService genomeAnalysisService() {
            return new GenomeAnalysisServiceImpl(GenomeAssembly.HG19, Mockito.mock(GenomeAnalysisService.class), Mockito
                    .mock(VariantDataService.class), Mockito.mock(VariantFactory.class));
        }

        @Bean
        public DataSource phenotypeDataSource() {
            return Mockito.mock(DataSource.class);
        }
    }

}