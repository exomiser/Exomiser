/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@EnableCaching
@Configuration
public class CacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    
    @Autowired
    private Environment env;

    @Bean
    public CacheManager cacheManager() {
        String cacheOption = env.getProperty("cache");
        //see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/cache.html for how this works
        CacheManager cacheManager;
        List<String> cacheNames = new ArrayList<>();
        switch (cacheOption) {
            case "none":
                cacheManager = noOpCacheManager();
                break;
            case "mem":
                cacheManager = new ConcurrentMapCacheManager("pathogenicity", "frequency", "diseaseHp", "diseases","hpo", "mpo", "zpo");
                cacheNames.addAll(cacheManager.getCacheNames());
                break;
            case "ehcache":
                cacheManager = ehCacheCacheManager();
                cacheNames.addAll(Arrays.asList(ehCacheCacheManager().getCacheManager().getCacheNames()));
                break;
            default:
                cacheManager = noOpCacheManager();
        }
        logger.info("Set up {} caches: {}", cacheOption, cacheNames);
        return cacheManager;
    }

    public NoOpCacheManager noOpCacheManager() {
        logger.info("Caching disabled.");
        return new NoOpCacheManager();
    }


    public EhCacheCacheManager ehCacheCacheManager() {
        EhCacheCacheManager ehCacheCacheManager = new EhCacheCacheManager(ehCacheManager().getObject());
        return ehCacheCacheManager;
    }

    @Lazy
    @Bean//(destroyMethod = "shutdown")
    public EhCacheManagerFactoryBean ehCacheManager() {
        Resource ehCachConfig = new ClassPathResource("ehcache.xml");
        logger.info("Loading ehcache.xml from {}", ehCachConfig.getDescription());
        
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        ehCacheManagerFactoryBean.setConfigLocation(ehCachConfig);
        return ehCacheManagerFactoryBean;
    }
    
}
