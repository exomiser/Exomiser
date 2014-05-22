/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.config;

import de.charite.compbio.exomiser.dao.FrequencyTriageDAO;
import de.charite.compbio.exomiser.priority.util.DataMatrix;
import de.charite.compbio.exomiser.util.ChromosomeMapFactory;
import de.charite.compbio.exomiser.util.VariantAnnotator;
import jannovar.reference.Chromosome;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

/**
 * Provides configuration details from the settings.properties file located in
 * the classpath.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@Import(DataSourceConfig.class)
@PropertySource({"classpath:settings.properties"})
public class MainConfig {

    private static final Logger logger = LoggerFactory.getLogger(MainConfig.class);

    @Autowired
    Environment env;

    @Value("${dataDir}")
    private String dataDir;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public Path dataPath() {
        Path dataPath = Paths.get(dataDir);
        logger.info("Root data source directory set to: {}", dataPath.toAbsolutePath());

        return dataPath;
    }

    @Bean
    public Path ucscDataPath() {
        return dataPath().resolve(env.getProperty("ucscFileName"));
    }

    @Bean
    public VariantAnnotator variantAnnotator() {
        Map<Byte, Chromosome> chromosomeMap = ChromosomeMapFactory.deserializeKnownGeneData(ucscDataPath());
        return new VariantAnnotator(chromosomeMap);
    }

//    This needs a lot of RAM so it's disabled for the time being... 
    @Bean
    @Lazy
    public DataMatrix randomWalkMatrix() {
        Path randomWalkFilePath = dataPath().resolve(env.getProperty("randomWalkFileName"));
        Path randomWalkIndexFilePath = dataPath().resolve(env.getProperty("randomWalkIndexFileName"));

        return new DataMatrix(randomWalkFilePath.toString(), randomWalkIndexFilePath.toString(), true);
    }
}
