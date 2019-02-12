/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure.phenotype;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.autoconfigure.AbstractAutoConfigurationTest;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatchService;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PhenotypeMatchServiceAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    public void testAutoConfiguresPhenotypeMatchService() {
        load(EmptyConfiguration.class, TEST_DATA_ENV, "exomiser.phenotype.data-version=1710");
        PhenotypeMatchService phenotypeMatchService = (PhenotypeMatchService) context.getBean("phenotypeMatchService");
        assertThat(phenotypeMatchService, instanceOf(PhenotypeMatchService.class));
    }

    @Configuration
    @ImportAutoConfiguration(classes = {PrioritiserAutoConfiguration.class} )
    protected static class EmptyConfiguration {

        @Bean
        public CacheManager cacheManager() {
            return new NoOpCacheManager();
        }

    }
}