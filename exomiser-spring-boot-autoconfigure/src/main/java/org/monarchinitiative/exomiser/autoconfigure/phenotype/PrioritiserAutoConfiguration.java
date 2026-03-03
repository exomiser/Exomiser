/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.autoconfigure.phenotype;

import org.monarchinitiative.exomiser.autoconfigure.DataDirectoryAutoConfiguration;
import org.monarchinitiative.exomiser.autoconfigure.UndefinedDataDirectoryException;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrixIO;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.annotations.io.hpo.DiseaseDatabase;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoader;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaderOptions;
import org.monarchinitiative.phenol.annotations.io.hpo.HpoDiseaseLoaders;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.p2gx.boqa.core.Counter;
import org.p2gx.boqa.core.DiseaseData;
import org.p2gx.boqa.core.algorithm.BoqaSetCounter;
import org.p2gx.boqa.core.diseases.DiseaseDataPhenolIngest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@ConditionalOnClass(PriorityFactory.class)
@ConditionalOnProperty("exomiser.phenotype.data-version")
@Import({DataDirectoryAutoConfiguration.class, PhenotypeMatchServiceAutoConfiguration.class, PhenotypeCacheConfiguration.class})
@EnableConfigurationProperties(PhenotypeProperties.class)
@ComponentScan("org.monarchinitiative.exomiser.core.prioritisers")
public class PrioritiserAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(PrioritiserAutoConfiguration.class);

    private final PhenotypeProperties phenotypeProperties;
    private final Path phenotypeDataDirectory;

    public PrioritiserAutoConfiguration(PhenotypeProperties phenotypeProperties, Path exomiserDataDirectory) {
        logger.debug("Configuring prioritisers for version {}", phenotypeProperties.getDataVersion());
        this.phenotypeProperties = phenotypeProperties;
        this.phenotypeDataDirectory = determinePhenotypeDataDirectory(phenotypeProperties, exomiserDataDirectory);
    }

    private Path determinePhenotypeDataDirectory(PhenotypeProperties phenotypeProperties, Path exomiserDataDirectory) {
        Path originalPath = phenotypeProperties.getDataDirectory();
        if (originalPath == null) {
            logger.debug("exomiser.phenotype.data-directory not defined - searching for candidate...");
            String version = phenotypeProperties.getDataVersion();
            if (version.isEmpty()) {
                logger.debug("Searching for phenotype data releases in {}", exomiserDataDirectory);
                //try searching for directories ending in "_phenotype", return the latest version based on the directory name
                List<Path> phenotypeDataDirectories = listPhenotypeDataDirectories(exomiserDataDirectory);
                if (phenotypeDataDirectories.isEmpty()) {
                    //otherwise return the root exomiser data directory - this could be a legacy setup.
                    logger.debug("Unable to find any phenotype data releases.");
                }
                Path latestPhenotypeDataDir = phenotypeDataDirectories.stream().sorted(Comparator.reverseOrder())
                        .findFirst()
                        .orElse(exomiserDataDirectory);
                logger.debug("Using phenotype data directory: {}", latestPhenotypeDataDir);
                return latestPhenotypeDataDir;

            } else {
                logger.debug("Searching based on phenotype.data-version {}", version);
                String directoryName = String.format("%s_phenotype", version);
                Path automaticallyDefinedPath = exomiserDataDirectory.resolve(directoryName);
                logger.debug("exomiser.phenotype.data-directory defined as: {}", automaticallyDefinedPath);
                return automaticallyDefinedPath;
            }
        }
        logger.debug("exomiser.phenotype.data-directory defined as: {}", originalPath);
        return originalPath;
    }

    private List<Path> listPhenotypeDataDirectories(Path dir) {
        List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (entry.toString().endsWith("_phenotype")) {
                    logger.debug("Found phenotype data directory {}", entry);
                    result.add(entry);
                }
            }
        } catch (DirectoryIteratorException | IOException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw new UndefinedDataDirectoryException("Unable to read/find valid phenotype data directory");
        }
        return result;
    }


    @Bean
    public Path phenotypeDataDirectory() {
        logger.debug("Phenotype data directory {}", phenotypeDataDirectory);
        return phenotypeDataDirectory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "phenixDataDir")
    public Path phenixDataDirectory() {
        String phenixDataDirValue = phenotypeProperties.getPhenixDataDir();
        Path phenixDataDirectory = phenotypeDataDirectory().resolve(phenixDataDirValue);
        logger.debug("phenixDataDirectory: {}", phenixDataDirectory.toAbsolutePath());
        return phenixDataDirectory;
    }

    @Bean
    @ConditionalOnMissingBean(name = "hpoOboFilePath")
    public Path hpoOboFilePath() {
        String hpoFileName = phenotypeProperties.getHpoFileName();
        Path hpoFilePath = phenixDataDirectory().resolve(hpoFileName);
        logger.debug("hpoOboFilePath: {}", hpoFilePath.toAbsolutePath());
        return hpoFilePath;
    }

    @Bean
    @ConditionalOnMissingBean(name = "hpoAnnotationFilePath")
    public Path hpoAnnotationFilePath() {
        String hpoAnnotationFileValue = phenotypeProperties.getHpoAnnotationFile();
        Path hpoAnnotationFilePath = phenixDataDirectory().resolve(hpoAnnotationFileValue);
        logger.debug("hpoAnnotationFilePath: {}", hpoAnnotationFilePath.toAbsolutePath());
        return hpoAnnotationFilePath;
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean(name = "hpoOntology")
    public Ontology hpoOntology() {
        Path hpoFilePath = phenotypeDataDirectory().resolve("hp.json");
        Ontology hpoOntology = OntologyLoader.loadOntology(hpoFilePath.toFile());
        logger.debug("Ontology loaded successfully from {}", hpoFilePath);
        return hpoOntology;
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean(name = "boqaCounter")
    Counter boqaCounter(Ontology hpoOntology) {
        // Parse disease-HPO associations into DiseaseData object
        Path hpoaFilePath = phenotypeDataDirectory().resolve("phenotype.hpoa");
        logger.debug("Importing disease phenotype associations from file: {} ...", hpoaFilePath);
        DiseaseData diseaseData;
        try {
            //diseaseData = DiseaseDataParser.parseDiseaseDataFromHpoa(hpoaFilePath);
            Set<DiseaseDatabase> diseaseDatabase = Set.of("OMIM").stream()
                    .map(DiseaseDatabase::fromString)
                    .collect(Collectors.toSet());
            HpoDiseaseLoaderOptions options = HpoDiseaseLoaderOptions.of(diseaseDatabase,false, 100);
            HpoDiseaseLoader loader = HpoDiseaseLoaders.defaultLoader(hpoOntology(), options);
            HpoDiseases diseases = loader.load(hpoaFilePath);
            diseaseData = DiseaseDataPhenolIngest.of(hpoOntology(), diseases);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        logger.debug("Disease data parsed from {}", hpoaFilePath);

        // n.b. the OMIM entries in the Exomiser database are a subset of the entire HPOA as there are approximately
        // 1950 OMIM entries without a confirmed gene association
//        List<Disease> diseases = priorityService.getAllDiseaseData();
//        DiseaseData exomiserDiseaseData = new BoqaPrioritiser.ExomiserDiseaseData(diseases);

        // Initialize Counter
        var counter = new BoqaSetCounter(diseaseData, hpoOntology);

        logger.debug("Initialized BoqaSetCounter with {} diseases.", diseaseData.size());
        return counter;
    }

    /**
     * This needs a lot of RAM and is slow to create from the randomWalkFile, so
     * it's set as lazy use on the command-line.
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(name = "randomWalkMatrix")
    public DataMatrix randomWalkMatrix() {
        String randomWalkFileNameValue = phenotypeProperties.getRandomWalkFileName();
        Path randomWalkFilePath = phenotypeDataDirectory().resolve(randomWalkFileNameValue);

        //backwards compatibility - pre 10.0.0 did not have a .mv
        if (randomWalkFileNameValue.endsWith(".gz")){
            String randomWalkIndexFileNameValue = phenotypeProperties.getRandomWalkIndexFileName();
            Path randomWalkIndexFilePath = phenotypeDataDirectory().resolve(randomWalkIndexFileNameValue);
            return DataMatrixIO.loadInMemoryDataMatrixFromFile(randomWalkFilePath.toString(), randomWalkIndexFilePath.toString(), true);
        }
        if (phenotypeProperties.isRandomWalkPreload()) {
            logger.info("Pre-loading in-memory random-walk matrix from {}", randomWalkFilePath);
            return DataMatrixIO.loadInMemoryDataMatrix(randomWalkFilePath);
        }
        logger.debug("Using off-heap random-walk matrix from {}", randomWalkFilePath);
        return DataMatrixIO.loadOffHeapDataMatrix(randomWalkFilePath);
    }

}
