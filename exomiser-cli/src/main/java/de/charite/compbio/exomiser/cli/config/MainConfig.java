/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.config;

import de.charite.compbio.exomiser.cli.Main;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.factories.ChromosomeMapFactory;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantEvaluationDataFactory;
import de.charite.compbio.exomiser.core.filter.FilterFactory;
import de.charite.compbio.exomiser.core.filter.SparseVariantFilterer;
import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.Exomiser;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.util.VariantAnnotator;
import de.charite.compbio.exomiser.priority.PriorityFactory;
import de.charite.compbio.exomiser.priority.util.DataMatrix;
import jannovar.reference.Chromosome;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * Provides configuration details from the settings.properties file located in
 * the classpath.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@Import({DataSourceConfig.class, CommandLineOptionsConfig.class})
@PropertySource({"buildversion.properties", "file:${jarFilePath}/application.properties"})
public class MainConfig {

    private static final Logger logger = LoggerFactory.getLogger(MainConfig.class);

    @Autowired
    private Environment env;

    /**
     * Used to find the Path the Main application is running on in order to
     * pick-up the user-configured properties files.
     *
     * @return
     */
    @Bean
    public Path jarFilePath() {
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();

        Path jarFilePath = null;
        try {
            jarFilePath = Paths.get(codeSource.getLocation().toURI()).getParent();
        } catch (URISyntaxException ex) {
            logger.error("Unable to find jar file", ex);
        }
        logger.info("Jar file is running from location: {}", jarFilePath);
        return jarFilePath;
    }

//    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
//        Path jdbcPropertiesPath = jarPath.resolve("jdbc.properties");
//        Path applicationPropertiesPath = jarPath.resolve("application.properties");
//        PathResource[] resources = new PathResource[]{
//                new PathResource(jdbcPropertiesPath),
//                new PathResource(applicationPropertiesPath)};
//                
//        pspc.setLocations(resources);
//        env = new StandardEnvironment();
//        PropertySources propertySources = pspc.getAppliedPropertySources();
//        for (org.springframework.core.env.PropertySource<?> propertySource : propertySources) {
//            logger.info("Adding propertySource {}", propertySource);
//            env.getPropertySources().addFirst(propertySource);        
//        }
//        logger.info(env.getPropertySources().toString());
//        
//        pspc.setEnvironment(env);
//        return pspc;
//    }
    @Bean
    public String buildVersion() {
        return env.getProperty("buildVersion");
    }

    @Bean
    public String buildTimestamp() {
        return env.getProperty("buildTimestamp");
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
        Path dataPath = jarFilePath().resolve(env.getProperty("dataDir"));
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
     * This takes a few seconds to de-serealise. Would be better to be eager in
     * a web-app, but lazy on the command-line as then the input parameters can
     * be checked before doing this.
     *
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
     * This needs a lot of RAM and is slow to create from the randomWalkFile, so
     * it's set as lazy use on the command-line.
     *
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
    public FrequencyDao frequencyDao() {
        return new FrequencyDao();
    }

    @Bean
    public PathogenicityDao pathogenicityDao() {
        return new PathogenicityDao();
    }

    @Bean
    public FilterFactory filterFactory() {
        return new FilterFactory();
    }

    @Bean
    public PriorityFactory priorityFactory() {
        return new PriorityFactory();
    }

    @Bean
    @Lazy
    public SampleDataFactory sampleDataFactory() {
        return new SampleDataFactory();
    }

    @Bean
    public Exomiser exomiser() {
        return new Exomiser();
    }
    
    @Bean 
    public SparseVariantFilterer sparseVariantFilterer() {
        return new SparseVariantFilterer();
    }
    
    @Bean
    public VariantEvaluationDataFactory variantEvaluationDataFactory() {
        Map<String, FrequencyData> frequencyDataCache = new HashMap<>();
        Map<String, PathogenicityData> pathogenicityDataCache = new HashMap<>();
        
        return new VariantEvaluationDataFactory(frequencyDataCache, pathogenicityDataCache);
    }
}
