/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.config;

import de.charite.compbio.exomiser.cli.Main;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
import de.charite.compbio.exomiser.core.dao.DefaultFrequencyDao;
import de.charite.compbio.exomiser.core.dao.DefaultPathogenicityDao;
import de.charite.compbio.exomiser.core.dao.PathogenicityDao;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantEvaluationDataService;
import de.charite.compbio.exomiser.core.filters.FilterFactory;
import de.charite.compbio.exomiser.core.filters.SparseVariantFilterRunner;
import de.charite.compbio.exomiser.core.Exomiser;
import de.charite.compbio.exomiser.core.dao.DefaultDiseaseDao;
import de.charite.compbio.exomiser.core.dao.DiseaseDao;
import de.charite.compbio.exomiser.core.dao.HumanPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.MousePhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.OntologyDao;
import de.charite.compbio.exomiser.core.dao.ZebraFishPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.factories.VariantAnnotator;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.ModelService;
import de.charite.compbio.exomiser.core.prioritisers.util.ModelServiceImpl;
import de.charite.compbio.exomiser.core.prioritisers.util.OntologyService;
import de.charite.compbio.exomiser.core.prioritisers.util.OntologyServiceImpl;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import de.charite.compbio.jannovar.io.JannovarDataSerializer;
import de.charite.compbio.jannovar.io.SerializationException;

import de.charite.compbio.exomiser.core.writers.ResultsWriterFactory;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

/**
 * Provides configuration details from the settings.properties file located in
 * the classpath.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@Import({DataSourceConfig.class, CommandLineOptionsConfig.class, CacheConfig.class})
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
        String dataDirValue = getValueOfProperty("dataDir");
        Path dataPath = jarFilePath().resolve(dataDirValue);
        logger.debug("Root data source directory set to: {}", dataPath.toAbsolutePath());

        return dataPath;
    }

    @Bean
    public Path ucscFilePath() {
        String ucscFileNameValue = getValueOfProperty("ucscFileName");
        Path ucscFilePath = dataPath().resolve(ucscFileNameValue);
        logger.debug("UCSC data file: {}", ucscFilePath.toAbsolutePath());
        return ucscFilePath;
    }

    @Bean
    public Path phenixDataDirectory() {
        String phenixDataDirValue = getValueOfProperty("phenomizerDataDir");
        Path phenixDataDirectory = dataPath().resolve(phenixDataDirValue);
        logger.debug("phenixDataDirectory: {}", phenixDataDirectory.toAbsolutePath());
        return phenixDataDirectory;
    }

    @Bean
    public Path hpoOntologyFilePath() {
        String hpoOntologyFileValue = getValueOfProperty("hpoOntologyFile");
        Path hpoOntologyFilePath = phenixDataDirectory().resolve(hpoOntologyFileValue);
        logger.debug("hpoOntologyFilePath: {}", hpoOntologyFilePath.toAbsolutePath());
        return hpoOntologyFilePath;
    }

    @Bean
    public Path hpoAnnotationFilePath() {
        String hpoAnnotationFileValue = getValueOfProperty("hpoAnnotationFile");
        Path hpoAnnotationFilePath = phenixDataDirectory().resolve(hpoAnnotationFileValue);
        logger.debug("hpoAnnotationFilePath: {}", hpoAnnotationFilePath.toAbsolutePath());
        return hpoAnnotationFilePath;
    }

    /**
     * This takes a few seconds to de-serialise. Would be better to be eager in
     * a web-app, but lazy on the command-line as then the input parameters can
     * be checked before doing this.
     *
     * @return
     */
    @Bean
    @Lazy
    public VariantAnnotator variantAnnotator() {
        try {
            return new VariantAnnotator(new JannovarDataSerializer(ucscFilePath().toString()).load());
        } catch (SerializationException e) {
            throw new RuntimeException("Could not load Jannovar data from " + ucscFilePath(), e);
        }
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
        String randomWalkFileNameValue = getValueOfProperty("randomWalkFileName");
        Path randomWalkFilePath = dataPath().resolve(randomWalkFileNameValue);

        String randomWalkIndexFileNameValue = getValueOfProperty("randomWalkIndexFileName");
        Path randomWalkIndexFilePath = dataPath().resolve(randomWalkIndexFileNameValue);

        return new DataMatrix(randomWalkFilePath.toString(), randomWalkIndexFilePath.toString(), true);
    }

    @Bean
    public FrequencyDao frequencyDao() {
        return new DefaultFrequencyDao();
    }

    @Bean
    public PathogenicityDao pathogenicityDao() {
        return new DefaultPathogenicityDao();
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
    PriorityService priorityService() {
        return new PriorityService();
    }
    
    @Bean
    ModelService modelService() {
        return new ModelServiceImpl();
    }
    
    @Bean
    OntologyService ontologyService() {
        return new OntologyServiceImpl();
    }
    
    @Bean
    DiseaseDao diseaseDao() {
        return new DefaultDiseaseDao();
    }
           
    @Bean
    HumanPhenotypeOntologyDao humanPhenotypeOntologyDao() {
        return new HumanPhenotypeOntologyDao();
    }
    
    @Bean
    MousePhenotypeOntologyDao mousePhenotypeOntologyDao() {
        return new MousePhenotypeOntologyDao();
    }
    
    @Bean
    ZebraFishPhenotypeOntologyDao zebraFishPhenotypeOntologyDao() {
        return new ZebraFishPhenotypeOntologyDao();
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
    public SparseVariantFilterRunner sparseVariantFilterer() {
        return new SparseVariantFilterRunner();
    }

    @Bean
    public VariantEvaluationDataService variantEvaluationDataService() {
        return new VariantEvaluationDataService();
    }
       
    @Bean
    public TemplateEngine templateEngine() {
        TemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setPrefix("html/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(true);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        
        return templateEngine;
    }
    
    @Bean
    public ResultsWriterFactory resultsWriterFactory() {
        return new ResultsWriterFactory();
    }
    
    protected String getValueOfProperty(String property) throws PropertyNotFoundException {
        String value = env.getProperty(property);
        if (value == null) {
            throw new PropertyNotFoundException(String.format("Property '%s' not present in application.properties", property));
        }
        return value;
    }

    public class PropertyNotFoundException extends RuntimeException {

        public PropertyNotFoundException(String message) {
            super(message);
        }
    }
}
