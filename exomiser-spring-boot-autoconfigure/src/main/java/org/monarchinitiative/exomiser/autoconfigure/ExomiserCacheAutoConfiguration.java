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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(ExomiserProperties.class)
public class ExomiserCacheAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserCacheAutoConfiguration.class);

    //TODO: move this bean to the phenotype package? Can we have 2 @EnableCaching @Configurations?
    @Bean
    public CacheResolver modelCacheResolver() {
        NamedCacheResolver modelCacheResolver = new NamedCacheResolver();
        //these guys are relatively small, always accessed and never grow so were using a ConcurrentMap to store them.
        modelCacheResolver.setCacheNames(Collections.singletonList("models"));
        modelCacheResolver.setCacheManager(new ConcurrentMapCacheManager());
        return modelCacheResolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManager cacheManager(ExomiserProperties exomiserProperties) {
        String cacheOption = exomiserProperties.getCache();
        //see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/cache.html for how this works
        CacheManager cacheManager;
        List<String> cacheNames = new ArrayList<>();
        switch (cacheOption) {
            case "none":
                cacheManager = noOpCacheManager();
                break;
            case "mem":
                cacheManager = new ConcurrentMapCacheManager("pathogenicity", "frequency", "diseaseHp", "diseases", "hpo", "mpo", "zpo", "cadd", "remm", "local");
                cacheNames.addAll(cacheManager.getCacheNames());
                break;
            case "ehcache":
                cacheManager = ehCacheCacheManager();
                cacheNames.addAll(Arrays.asList(ehCacheCacheManager().getCacheManager().getCacheNames()));
                break;
            default:
                String message = String.format("Unrecognised value '%s' for exomiser cache option. Please choose 'none', 'mem' or 'ehcache'.", cacheOption);
                logger.error(message);
                throw new ExomiserAutoConfigurationException(message);
        }
        logger.info("Set up {} caches: {}", cacheOption, cacheNames);
        return cacheManager;
    }

    private NoOpCacheManager noOpCacheManager() {
        logger.info("Caching disabled.");
        return new NoOpCacheManager();
    }

    private EhCacheCacheManager ehCacheCacheManager() {
        return new EhCacheCacheManager(ehCacheManager().getObject());
    }

    @Bean
    @ConditionalOnMissingBean
    public Resource ehCacheConfig() {
        return new ClassPathResource("ehcache.xml");
    }

    @Lazy
    @Bean
    public EhCacheManagerFactoryBean ehCacheManager() {
        Resource ehCacheConfig = ehCacheConfig();
        logger.info("Loading ehcache.xml from {}", ehCacheConfig.getDescription());

        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        ehCacheManagerFactoryBean.setConfigLocation(ehCacheConfig);
        return ehCacheManagerFactoryBean;
    }
}
