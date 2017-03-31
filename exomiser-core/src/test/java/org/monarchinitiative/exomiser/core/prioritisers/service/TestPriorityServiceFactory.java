/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers.service;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TestPriorityServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(TestPriorityServiceFactory.class);

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

    private static PriorityService setUpPriorityService() {
        logger.info("Creating test priority service - this is static test data");

        OntologyService testOntologyService = setUpOntologyService();

        logger.info("To the following models:");
        ModelService testModelService = setUpModelService();

        logger.info("Associated with genes:");
        logger.info("    Entrez:2263 - FGFR2");
        logger.info("    Entrez:4920 - ROR2");
        logger.info("    Entrez:341640 - FREM2");

        List<Disease> diseases = TestPrioritiserDataFileReader.readDiseaseData("src/test/resources/prioritisers/disease-models");
        TestDiseaseDao testDiseaseDao = new TestDiseaseDao(diseases);

        return new PriorityService(testModelService, new PhenotypeMatchService(testOntologyService), testDiseaseDao);
    }

    private static OntologyService setUpOntologyService() {
        Map<String, PhenotypeTerm> hpPhenotypesTerms = new HashMap<>();
        hpPhenotypesTerms.put("HP:0010055", PhenotypeTerm.of("HP:0010055", "Broad hallux"));
        hpPhenotypesTerms.put("HP:0001363", PhenotypeTerm.of("HP:0001363", "Craniosynostosis"));
        hpPhenotypesTerms.put("HP:0001156", PhenotypeTerm.of("HP:0001156", "Brachydactyly syndrome"));
        hpPhenotypesTerms.put("HP:0011304", PhenotypeTerm.of("HP:0011304", "Broad thumb"));

        logger.info("This data links {} phenotypes:", hpPhenotypesTerms.size());
        hpPhenotypesTerms.values().forEach(term ->logger.info("    {} - {}", term.getId(), term.getLabel()));

        logger.info("Via cross-species phenotype mappings:");
        List<PhenotypeMatch> hpHpMappings = TestPrioritiserDataFileReader.readOntologyMatchData("src/test/resources/prioritisers/hp-hp-mappings");
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
