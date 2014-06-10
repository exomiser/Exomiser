/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.config;

import de.charite.compbio.exomiser.filter.FilterFactory;
import de.charite.compbio.exomiser.priority.PriorityFactory;
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
@Import({DataSourceConfig.class, CommandLineOptionsConfig.class})
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

    /**
     * This is critical for the application to run as it points to the data 
     * directory where all the required resources are found. Without this being 
     * correctly set, the application will fail.
     * 
     * @return 
     */
    @Bean
    public Path dataPath() {
        Path dataPath = Paths.get(dataDir);
        logger.info("Root data source directory set to: {}", dataPath.toAbsolutePath());

        return dataPath;
    }

    @Bean
    public Path ucscFilePath() {
        Path ucscFilePath = dataPath().resolve(env.getProperty("ucscFileName"));
        logger.info("UCSC data file: {}", ucscFilePath.toAbsolutePath());
        return ucscFilePath;
    }
    
    @Bean
    public Path phenomizerDataDirectory() {
        Path phenomizerDataDirectory = dataPath().resolve(env.getProperty("phenomizerDataDir"));
        logger.info("phenomizerDataDirectory: {}", phenomizerDataDirectory.toAbsolutePath());
        return phenomizerDataDirectory;
    }
    
    @Bean
    public Path hpoOntologyFilePath() {
        Path hpoOntologyFilePath = phenomizerDataDirectory().resolve(env.getProperty("hpoOntologyFile"));
        logger.info("hpoOntologyFilePath: {}", hpoOntologyFilePath.toAbsolutePath());
        return hpoOntologyFilePath;
    }
    
    @Bean
    public Path hpoAnnotationFilePath() {
        Path hpoAnnotationFilePath = phenomizerDataDirectory().resolve(env.getProperty("hpoAnnotationFile"));
        logger.info("hpoAnnotationFilePath: {}", hpoAnnotationFilePath.toAbsolutePath());
        return hpoAnnotationFilePath;
    }

    /**
     * This takes a few seconds to de-serealise. Would be better to be eager in a web-app, 
     * but lazy on the command-line as then the input parameters can be checked before doing this. 
     * @return 
     */
    @Bean
    @Lazy
    public VariantAnnotator variantAnnotator() {
        Map<Byte, Chromosome> chromosomeMap = ChromosomeMapFactory.deserializeKnownGeneData(ucscFilePath());
        return new VariantAnnotator(chromosomeMap);
    }

//    
    /**
     * This needs a lot of RAM and is slow to create from the randomWalkFile, so it's set as lazy use on the command-line.
     * @return 
     */
    @Bean
    @Lazy
    public DataMatrix randomWalkMatrix() {
        Path randomWalkFilePath = dataPath().resolve(env.getProperty("randomWalkFileName"));
        Path randomWalkIndexFilePath = dataPath().resolve(env.getProperty("randomWalkIndexFileName"));

        return new DataMatrix(randomWalkFilePath.toString(), randomWalkIndexFilePath.toString(), true);
    }
    
    @Bean
    public FilterFactory filterFactory() {
        return new FilterFactory();
    }
    
    @Bean
    public PriorityFactory priorityFactory() {
        return new PriorityFactory();
    }
}
