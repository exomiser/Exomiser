/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli.config;

import de.charite.compbio.exomiser.cli.Main;
import de.charite.compbio.exomiser.core.AnalysisFactory;
import de.charite.compbio.exomiser.core.AnalysisParser;
import de.charite.compbio.exomiser.core.SettingsParser;
import de.charite.compbio.exomiser.core.dao.*;
import de.charite.compbio.exomiser.core.factories.SampleDataFactory;
import de.charite.compbio.exomiser.core.factories.VariantDataServiceImpl;
import de.charite.compbio.exomiser.core.dao.DefaultDiseaseDao;
import de.charite.compbio.exomiser.core.dao.DiseaseDao;
import de.charite.compbio.exomiser.core.dao.HumanPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.MousePhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.dao.ZebraFishPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.factories.VariantAnnotator;
import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactoryImpl;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.ModelService;
import de.charite.compbio.exomiser.core.prioritisers.util.ModelServiceImpl;
import de.charite.compbio.exomiser.core.prioritisers.util.OntologyService;
import de.charite.compbio.exomiser.core.prioritisers.util.OntologyServiceImpl;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;

import de.charite.compbio.exomiser.core.writers.ResultsWriterFactory;
import de.charite.compbio.jannovar.data.JannovarData;
import htsjdk.tribble.readers.TabixReader;
import java.io.IOException;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
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
//TODO: add this and check it works - then remove all the unecessary gubbins below
//@ComponentScan("de.charite.compbio.exomiser")
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
    
    @Lazy
    @Bean
    public TabixReader indelTabixReader() {
        String caddInDelPathValue = getValueOfProperty("caddInDelPath");
        if (caddInDelPathValue.isEmpty()) {
            caddInDelPathValue = dataPath().resolve("InDels.tsv.gz").toString();
        }
        try {
             return new TabixReader(caddInDelPathValue);
        } catch (IOException e) {
            throw new RuntimeException("CADD InDels.tsv.gz file not found.", e);
        }
    }
    
    @Lazy
    @Bean
    public TabixReader snvTabixReader() {
        String caddSnvPathValue = getValueOfProperty("caddSnvPath");
        if (caddSnvPathValue.isEmpty()) {
            caddSnvPathValue = dataPath().resolve("whole_genome_SNVs.tsv.gz").toString();
        }
        try {
             return new TabixReader(caddSnvPathValue);
        } catch (IOException e) {
            throw new RuntimeException("CADD whole_genome_SNVs.tsv.gz file not found.", e);
        }
    }
    
    @Lazy
    @Bean
    public TabixReader ncdsTabixReader() {
        String remmPath = getValueOfProperty("remmPath");
        String ncdsPathValue = dataPath().resolve(remmPath).toString();
        try {
             return new TabixReader(ncdsPathValue);
        } catch (IOException e) {
            throw new RuntimeException("REMM file not found ", e);
        }
    }

    @Bean
    public SettingsParser exomiser() {
        return new SettingsParser(priorityFactory(), variantDataService());
    }

    @Bean
    public AnalysisParser analysisParser() {
        return new AnalysisParser(priorityFactory(), variantDataService());
    }

    /**
     * This takes a few seconds to de-serialise.
     */
    @Bean
    public JannovarData jannovarData() {
        try {
            return new JannovarDataSerializer(ucscFilePath().toString()).load();
        } catch (SerializationException e) {
            throw new RuntimeException("Could not load Jannovar data from " + ucscFilePath(), e);
        }
    }

    @Bean
    public AnalysisFactory analysisFactory() {
        return new AnalysisFactory(sampleDataFactory(), variantDataService(), priorityFactory());
    }

    @Bean
    public VariantFactory variantFactory() {
        JannovarData jannovarData = jannovarData();
        VariantContextAnnotator variantContextAnnotator = new VariantContextAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes());
        VariantAnnotator variantAnnotator = new VariantAnnotator(variantContextAnnotator);
        return new VariantFactory(variantAnnotator);
    }

    @Bean
    public SampleDataFactory sampleDataFactory() {
        return new SampleDataFactory();
    }

    /**
     * This needs a lot of RAM and is slow to create from the randomWalkFile, so
     * it's set as lazy use on the command-line.
     *
     * @return
     */
    @Lazy
    @Bean
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
    
    @Lazy
    @Bean
    public CaddDao caddDao() {
        return new CaddDao(indelTabixReader(), snvTabixReader());
    }
    
    @Lazy
    @Bean
    public NcdsDao ncdsDao() {
        return new NcdsDao(ncdsTabixReader());
    }

    @Bean
    public RegulatoryFeatureDao regulatoryFeatureDao() {
        return new RegulatoryFeatureDao();
    }

    @Bean
    public PriorityFactoryImpl priorityFactory() {
        return new PriorityFactoryImpl();
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
    public VariantDataService variantDataService() {
        return new VariantDataServiceImpl();
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
