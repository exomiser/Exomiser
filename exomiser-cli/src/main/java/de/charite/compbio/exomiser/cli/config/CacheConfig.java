/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.config;

import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

/**
 *
 * @author jj8
 */
@EnableCaching
@Configuration
public class CacheConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    
    @Autowired
    private Environment env;

    @Autowired
    Path jarFilePath;
    
    @Bean
    public CacheManager cacheManager() {
        String cacheOption = env.getProperty("cache");
        //see http://docs.spring.io/spring/docs/current/spring-framework-reference/html/cache.html for how this works
        CacheManager cacheManager;
        switch (cacheOption) {
            case "none":
                cacheManager = noOpCacheManager();
                break;
            case "mem":
                cacheManager = new ConcurrentMapCacheManager("pathogenicity", "frequency");
                break;
            case "ehcache":
                cacheManager = ehCacheCacheManager();
                break;
            default:
                cacheManager = noOpCacheManager();
        }
        logger.info("Set up {} caches: {}", cacheOption, cacheManager.getCacheNames());
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
        Path ehCacheConfigFile = jarFilePath.resolve("ehcache.xml");
        Resource ehCachConfig = new PathResource(ehCacheConfigFile);
        logger.info("Loading ehcache.xml from {}", ehCachConfig.getDescription());
        
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        ehCacheManagerFactoryBean.setConfigLocation(ehCachConfig);
        return ehCacheManagerFactoryBean;
    }
    
}
