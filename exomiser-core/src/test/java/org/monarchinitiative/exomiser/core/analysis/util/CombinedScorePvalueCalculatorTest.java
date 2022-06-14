package org.monarchinitiative.exomiser.core.analysis.util;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhivePriority;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CombinedScorePvalueCalculatorTest {

    @Test
    public void testZeroValueInput() {
        var instance = new CombinedScorePvalueCalculator(0, PriorityType.NONE, new double[]{});
        assertThat(instance.calculatePvalueFromCombinedScore(0.5), equalTo(Double.NaN));
    }

    @Test
    public void testWithRandomScoresConstructor() {
        var instance = CombinedScorePvalueCalculator.withRandomScores(100_000, 50_000);

        assertThat(instance.calculatePvalueFromCombinedScore(1.0), equalTo(0.0));
        assertThat(instance.calculatePvalueFromCombinedScore(0.5), closeTo(0.5, 0.01));
        assertThat(instance.calculatePvalueFromCombinedScore(0.0), equalTo(1.0));

        assertThat(instance.calculatePvalueFromCombinedScore(0.012), closeTo(instance.calculatePvalueFromCombinedScore(0.012), 0.1));
        assertThat(instance.calculatePvalueFromCombinedScore(0.112), closeTo(instance.calculatePvalueFromCombinedScore(0.112), 0.1));
        assertThat(instance.calculatePvalueFromCombinedScore(0.25), closeTo(instance.calculatePvalueFromCombinedScore(0.25), 0.1));
        assertThat(instance.calculatePvalueFromCombinedScore(0.566), closeTo(instance.calculatePvalueFromCombinedScore(0.566), 0.1));
        assertThat(instance.calculatePvalueFromCombinedScore(0.756), closeTo(instance.calculatePvalueFromCombinedScore(0.756), 0.1));
        assertThat(instance.calculatePvalueFromCombinedScore(0.99), closeTo(instance.calculatePvalueFromCombinedScore(0.99), 0.1));
    }

    @Test
    public void testStaticConstructor() {
        Prioritiser<?> prioritiser = new HiPhivePriority(HiPhiveOptions.defaults(), DataMatrix.empty(), TestPriorityServiceFactory.testPriorityService());
        List<String> phenotypicFeatures = TestPriorityServiceFactory.pfeifferSyndromePhenotypes().stream().map(PhenotypeTerm::getId).collect(Collectors.toList());
        List<Gene> genes = TestFactory.buildGenes();
        var instance = CombinedScorePvalueCalculator.of(20_000, prioritiser, phenotypicFeatures, genes);
        assertThat(instance.calculatePvalueFromCombinedScore(0.89), greaterThan(0.0));
    }
}