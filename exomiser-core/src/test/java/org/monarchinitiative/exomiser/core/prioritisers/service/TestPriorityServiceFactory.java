/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.prioritisers.service;

import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatchService;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.phenotype.service.TestOntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.dao.TestDiseaseDao;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestPriorityServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(TestPriorityServiceFactory.class);

    static {
        logger.info("Creating test ontology and priority services - this is static test data");
    }
    public static final OntologyService TEST_ONTOLOGY_SERVICE = setUpOntologyService();

    public static final PriorityService TEST_SERVICE = setUpPriorityService();
    public static final PriorityService STUB_SERVICE = setUpStubPriorityService();


    private static PriorityService setUpStubPriorityService() {
        ModelService stubModelService = new TestModelService(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        OntologyService stubOntologyService = TestOntologyService.builder()
                .setHpIdPhenotypeTerms(Collections.emptyMap())
                .setHumanHumanMappings(Collections.emptyMap())
                .setHumanMouseMappings(Collections.emptyMap())
                .setHumanFishMappings(Collections.emptyMap())
                .build();

        return new PriorityService(stubModelService, new PhenotypeMatchService(stubOntologyService), null);
    }

    private static OntologyService setUpOntologyService() {
        List<PhenotypeMatch> hpHpMappings = TestPrioritiserDataFileReader.readOntologyMatchData("src/test/resources/prioritisers/hp-hp-mappings");

        Map<String, PhenotypeTerm> hpPhenotypesTerms = hpHpMappings.stream()
                .map(PhenotypeMatch::getQueryPhenotype)
                .distinct()
                .collect(Collectors.toConcurrentMap(PhenotypeTerm::getId, Function.identity()));

        logger.info("This data links {} phenotypes:", hpPhenotypesTerms.size());
        hpPhenotypesTerms.values().forEach(term ->logger.info("    {} - {}", term.getId(), term.getLabel()));

        logger.info("Via cross-species phenotype mappings:");
        logger.info("    hp-hp: " + hpHpMappings.size());

        List<PhenotypeMatch> hpMpMappings = TestPrioritiserDataFileReader.readOntologyMatchData("src/test/resources/prioritisers/hp-mp-mappings");
        logger.info("    hp-mp: " + hpMpMappings.size());

        List<PhenotypeMatch> hpZpMappings = TestPrioritiserDataFileReader.readOntologyMatchData("src/test/resources/prioritisers/hp-zp-mappings");
        logger.info("    hp-zp: " + hpZpMappings.size());

        return TestOntologyService.builder()
                .setHpIdPhenotypeTerms(hpPhenotypesTerms)
                .setHumanHumanMappings(createPhenotypeMap(hpHpMappings))
                .setHumanMouseMappings(createPhenotypeMap(hpMpMappings))
                .setHumanFishMappings(createPhenotypeMap(hpZpMappings))
                .build();
    }

    private static PriorityService setUpPriorityService() {
        logger.info("To the following models:");
        ModelService testModelService = setUpModelService();

        logger.info("Associated with genes:");
        testModelService.getHumanGeneDiseaseModels().stream()
                .map(geneModel -> GeneIdentifier.builder()
                        .entrezId(geneModel.getEntrezGeneId().toString())
                        .geneSymbol(geneModel.getHumanGeneSymbol())
                        .build())
                .distinct()
                .forEach(geneIdentifier -> logger.info("    Entrez:{} - {}", geneIdentifier.getEntrezId(), geneIdentifier
                        .getGeneSymbol()));

        List<Disease> diseases = TestPrioritiserDataFileReader.readDiseaseData("src/test/resources/prioritisers/disease-models");
        TestDiseaseDao testDiseaseDao = new TestDiseaseDao(diseases);

        return new PriorityService(testModelService, new PhenotypeMatchService(TestPriorityServiceFactory.TEST_ONTOLOGY_SERVICE), testDiseaseDao);
    }

    private static Map<PhenotypeTerm, List<PhenotypeMatch>> createPhenotypeMap(List<PhenotypeMatch> crossOntologyMappings) {
        return crossOntologyMappings.parallelStream().collect(Collectors.groupingBy(PhenotypeMatch::getQueryPhenotype));
    }

    private static ModelService setUpModelService() {
        List<GeneModel> diseaseModels = TestPrioritiserDataFileReader.readDiseaseModelData("src/test/resources/prioritisers/disease-models");
        logger.info("    Disease Models: " + diseaseModels.size());

        List<GeneModel> mouseModels = TestPrioritiserDataFileReader.readOrganismData("src/test/resources/prioritisers/mouse-models");
        logger.info("    Mouse Models: " + mouseModels.size());

        List<GeneModel> fishModels = TestPrioritiserDataFileReader.readOrganismData("src/test/resources/prioritisers/fish-models");
        logger.info("    Fish Models: {}", fishModels.size());

        return new TestModelService(diseaseModels, mouseModels, fishModels);
    }
}
