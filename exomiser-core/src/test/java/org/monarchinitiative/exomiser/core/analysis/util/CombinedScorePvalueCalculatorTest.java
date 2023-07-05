package org.monarchinitiative.exomiser.core.analysis.util;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhivePriority;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CombinedScorePvalueCalculatorTest {

    @Test
    void testNoOpCalculator() {
        var instance = CombinedScorePvalueCalculator.noOpCombinedScorePvalueCalculator();
        assertThat(instance.calculatePvalueFromCombinedScore(0.0), equalTo(1.0));
        assertThat(instance.calculatePvalueFromCombinedScore(0.5), equalTo(1.0));
        assertThat(instance.calculatePvalueFromCombinedScore(1.0), equalTo(1.0));
    }
    @Test
    public void testWithRandomScoresConstructor() {
        var instance = CombinedScorePvalueCalculator.withRandomScores(10_000, 35_000, 250);

        assertThat(instance.calculatePvalueFromCombinedScore(1.0), closeTo(0.0, 0.001));
        assertThat(instance.calculatePvalueFromCombinedScore(0.5), closeTo(0.5, 0.01));
        assertThat(instance.calculatePvalueFromCombinedScore(0.0), equalTo(1.0));

        assertThat(instance.calculatePvalueFromCombinedScore(0.012), closeTo(instance.calculatePvalueFromCombinedScore(0.012), 0.001));
        assertThat(instance.calculatePvalueFromCombinedScore(0.112), closeTo(instance.calculatePvalueFromCombinedScore(0.112), 0.001));
        assertThat(instance.calculatePvalueFromCombinedScore(0.25), closeTo(instance.calculatePvalueFromCombinedScore(0.25), 0.001));
        assertThat(instance.calculatePvalueFromCombinedScore(0.566), closeTo(instance.calculatePvalueFromCombinedScore(0.566), 0.001));
        assertThat(instance.calculatePvalueFromCombinedScore(0.756), closeTo(instance.calculatePvalueFromCombinedScore(0.756), 0.001));
        assertThat(instance.calculatePvalueFromCombinedScore(0.99), closeTo(instance.calculatePvalueFromCombinedScore(0.99), 0.001));
    }

    @Test
    public void testStaticConstructor() {
        Prioritiser<?> prioritiser = new HiPhivePriority(HiPhiveOptions.defaults(), DataMatrix.empty(), TestPriorityServiceFactory.testPriorityService());
        List<String> hpoIds = TestPriorityServiceFactory.pfeifferSyndromePhenotypes().stream().map(PhenotypeTerm::id).collect(Collectors.toList());
        Sample sample = Sample.builder().hpoIds(hpoIds).build();
        List<Gene> genes = TestFactory.buildGenes();
        var instance = CombinedScorePvalueCalculator.of(20_000, prioritiser, sample, genes, genes.size());
        assertThat(instance.calculatePvalueFromCombinedScore(0.89), greaterThan(0.0));
    }

    @Test
    void testZeroValueCombinedScoreHasPvalueOfOne() {
        var instance = CombinedScorePvalueCalculator.withRandomScores(10_000, 25_000, 200);
        assertThat(instance.calculatePvalueFromCombinedScore(0), closeTo(instance.calculatePvalueFromCombinedScore(0.0), 0.001));
        assertThat(instance.calculatePvalueFromCombinedScore(1d), closeTo(instance.calculatePvalueFromCombinedScore(1.0), 0.001));
    }
}