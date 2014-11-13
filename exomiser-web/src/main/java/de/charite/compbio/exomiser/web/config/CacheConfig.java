/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@EnableCaching
@Configuration
public class CacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    
    @Bean
    public CacheManager cacheManager() {
        //see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/cache.html for how this works        
        return new EhCacheCacheManager(ehCacheManager().getObject());
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManager() {
        Resource ehCachConfig = new ClassPathResource("ehcache.xml");
        logger.info("Loading ehcache.xml from {}", ehCachConfig.getDescription());
        
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        ehCacheManagerFactoryBean.setConfigLocation(ehCachConfig);
        return ehCacheManagerFactoryBean;
    }
    
}
