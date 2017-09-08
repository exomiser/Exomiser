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
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ExomiserCacheAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    public void cachingDisabledByDefault() {
        load(ExomiserCacheAutoConfiguration.class, "");
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames().isEmpty(), is(true));
    }

    @Test(expected = BeanCreationException.class)
    public void cachingThrowsExceptionWhenNameNotRecognised() {
        load(ExomiserCacheAutoConfiguration.class, "exomiser.cache=wibble");
    }

    @Test
    public void cachingCanBeDisabledExplicitly() {
        load(ExomiserCacheAutoConfiguration.class, "exomiser.cache=none");
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames().isEmpty(), is(true));
    }

    @Test
    public void cachingInMemCanBeDefined() {
        load(ExomiserCacheAutoConfiguration.class, "exomiser.cache=mem");
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames(), hasItems("pathogenicity", "frequency", "diseaseHp", "diseases", "hpo", "mpo", "zpo", "cadd", "remm"));
    }

    @Test
    public void cachingEhCacheCanBeDefined() {
        load(ExomiserCacheAutoConfiguration.class, "exomiser.cache=ehcache");
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames(), hasItems("pathogenicity", "frequency", "diseaseHp", "diseases", "hpo", "mpo", "zpo", "cadd", "remm"));
    }

    @Test
    public void cachingCanBeOverridden() {
        load(BeanOverrideConfiguration.class, "exomiser.cache=ehcache");
        CacheManager cache = context.getBean(CacheManager.class);
        assertThat(cache.getCacheNames(), hasItems("wibble"));
    }

    @ImportAutoConfiguration(ExomiserCacheAutoConfiguration.class)
    protected static class BeanOverrideConfiguration {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("wibble");
        }
    }
}