/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.web.config;

import de.charite.compbio.exomiser.core.dao.DefaultFrequencyDao;
import de.charite.compbio.exomiser.core.dao.DefaultPathogenicityDao;
import de.charite.compbio.exomiser.core.dao.FrequencyDao;
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
import de.charite.compbio.exomiser.core.dao.ZebraFishPhenotypeOntologyDao;
import de.charite.compbio.exomiser.core.factories.VariantAnnotationsFactory;
import de.charite.compbio.exomiser.core.factories.VariantFactory;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.exomiser.core.prioritisers.util.ModelService;
import de.charite.compbio.exomiser.core.prioritisers.util.ModelServiceImpl;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.exomiser.core.prioritisers.util.OntologyService;
import de.charite.compbio.exomiser.core.prioritisers.util.OntologyServiceImpl;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import java.nio.file.Path;
import java.nio.file.Paths;
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
@PropertySource({"classpath:exomiser.properties"})
@Import(value = {CacheConfig.class, DaoConfig.class})
public class ExomiserConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserConfig.class);

    @Autowired
    private Environment env;

    /**
     * This is critical for the application to run as it points to the data
     * directory where all the required resources are found. Without this being
     * correctly set, the application will fail.
     *
     * @return
     */
    @Bean
    public Path dataPath() {
        Path dataPath = Paths.get(env.getProperty("dataDir"));
        logger.info("Root data source directory set to: {}", dataPath.toAbsolutePath());

        return dataPath;
    }

    @Bean
    public int maxVariants() {
        int maxVariants = Integer.valueOf(env.getProperty("maxVariants"));
        logger.info("Set max variants to {}", maxVariants);
        return maxVariants;
    }

    @Bean
    public int maxGenes() {
        int maxGenes = Integer.valueOf(env.getProperty("maxGenes"));
        logger.info("Set max genes to {}", maxGenes);
        return maxGenes;
    }

    @Bean
    public Path ucscFilePath() {
        Path ucscFilePath = dataPath().resolve(env.getProperty("ucscFileName"));
        logger.info("UCSC data file: {}", ucscFilePath.toAbsolutePath());
        return ucscFilePath;
    }

    @Bean
    public Path phenixDataDirectory() {
        Path phenixDataDirectory = dataPath().resolve(env.getProperty("phenomizerDataDir"));
        logger.info("phenixDataDirectory: {}", phenixDataDirectory.toAbsolutePath());
        return phenixDataDirectory;
    }

    @Bean
    public Path hpoOntologyFilePath() {
        Path hpoOntologyFilePath = phenixDataDirectory().resolve(env.getProperty("hpoOntologyFile"));
        logger.info("hpoOntologyFilePath: {}", hpoOntologyFilePath.toAbsolutePath());
        return hpoOntologyFilePath;
    }

    @Bean
    public Path hpoAnnotationFilePath() {
        Path hpoAnnotationFilePath = phenixDataDirectory().resolve(env.getProperty("hpoAnnotationFile"));
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
    public VariantAnnotationsFactory variantAnnotator() {
        try {
            return new VariantAnnotationsFactory(new JannovarDataSerializer(ucscFilePath().toString()).load());
        } catch (SerializationException e) {
            throw new RuntimeException("Could not load Jannovar data from " + ucscFilePath(), e);
        }
    }

    @Lazy
    @Bean
    public VariantFactory variantFactory() {
        return new VariantFactory(variantAnnotator());
    }
 
    @Lazy
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

    @Bean
    PriorityService priorityService() {
        return new PriorityService();
    }

    @Bean
    ModelService modelService() {
        return new ModelServiceImpl();
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

//cacheable beans
    @Bean
    public FrequencyDao frequencyDao() {
        return new DefaultFrequencyDao();
    }

    @Bean
    public PathogenicityDao pathogenicityDao() {
        return new DefaultPathogenicityDao();
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

}
