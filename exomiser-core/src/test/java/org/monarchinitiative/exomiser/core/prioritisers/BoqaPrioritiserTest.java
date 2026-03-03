package org.monarchinitiative.exomiser.core.prioritisers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatchService;
import org.monarchinitiative.exomiser.core.phenotype.dao.HumanPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.MousePhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.dao.ZebraFishPhenotypeOntologyDao;
import org.monarchinitiative.exomiser.core.phenotype.service.HpoIdChecker;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyServiceImpl;
import org.monarchinitiative.exomiser.core.prioritisers.config.TestDataSourceConfig;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DefaultDiseaseDao;
import org.monarchinitiative.exomiser.core.prioritisers.service.ModelServiceImpl;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.p2gx.boqa.core.Counter;
import org.p2gx.boqa.core.PatientData;
import org.p2gx.boqa.core.algorithm.BoqaCounts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        BoqaPrioritiserTest.BoqaPriotiserTestConfig.class,
        TestDataSourceConfig.class,
        PriorityFactoryImpl.class,
        PriorityFactoryTestConfig.class,
        PriorityService.class,
        OntologyServiceImpl.class,
        PhenotypeMatchService.class,
        ModelServiceImpl.class,
        DefaultDiseaseDao.class,
        HumanPhenotypeOntologyDao.class,
        MousePhenotypeOntologyDao.class,
        ZebraFishPhenotypeOntologyDao.class,
        HpoIdChecker.class
})
@Sql(scripts = {
        "file:src/test/resources/sql/create_hpo.sql",
        "file:src/test/resources/sql/create_disease.sql",
        "file:src/test/resources/sql/create_disease_hp.sql",
        "file:src/test/resources/sql/create_entrez2sym.sql",
        "file:src/test/resources/sql/diseaseDaoTestData.sql",
        "file:src/test/resources/sql/humanPhenotypeOntologyDaoTestData.sql"
})
class BoqaPrioritiserTest {

    @Configuration
    static class BoqaPriotiserTestConfig {
        @Bean
        Counter counter() {
            return new BoqaCounterStub();
        }
    }

    @Autowired
    private PriorityService priorityService;
    @Autowired
    private Counter counter;

    private List<String> patientHpoIds() {
        return List.of(
                "HP:0010055",
                "HP:0001363",
                "HP:0001156",
                "HP:0011304");
    }

    private List<Gene> buildGenes() {
        return List.of(
                new Gene("FGFR2", 2263),
                new Gene("ROR2", 4920),
                new Gene("FREM2", 341640),
                new Gene("ZNF738", 148203)
        );
    }


    @Disabled("Non-functional and useless")
    @Test
    void testPrioritise() {
        BoqaPrioritiser instance = new BoqaPrioritiser(priorityService, counter);
        List<String> patientPhenotypes = patientHpoIds();
        List<Gene> genes = buildGenes();
        List<BoqaPriorityResult> results = instance.prioritise(patientPhenotypes, genes).peek(System.out::println).toList();
    }

}