/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.MockPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenePriorityScoreCalculatorTest {

    private final GenePriorityScoreCalculator instance = new GenePriorityScoreCalculator();
    // Just a placeholder decent-ish score. The actual value isn't particularly important.
    private static final double PHENOTYPE_SCORE = 0.75;

    private Gene newGene() {
        return TestFactory.newGeneFGFR2();
    }

    private Gene newGeneCompatibleWith(ModeOfInheritance... modes) {
        Gene gene = TestFactory.newGeneFGFR2();
        gene.setCompatibleInheritanceModes(Set.of(modes));
        return gene;
    }

    private void addHiPhiveResultWithScore(Gene gene, double phenotypeScore) {
        PriorityResult hiPhiveResult = hiPhiveResult(gene, phenotypeScore);
        gene.addPriorityResult(hiPhiveResult);
    }

    private PriorityResult hiPhiveResult(Gene gene, double score) {
        return new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, gene.getEntrezGeneID(), gene.getGeneSymbol(), score);
    }

    private void addOmimResultToGene(Gene gene, Map<ModeOfInheritance, Double> scores, InheritanceMode... inheritanceModes) {
        double maxScore = scores.values().stream().max(Comparator.naturalOrder()).orElse(1.0);
        List<Disease> knownDiseases = diseasesCompatibleWith(inheritanceModes);
        PriorityResult omimResult = new OmimPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), maxScore, knownDiseases, scores);
        gene.addPriorityResult(omimResult);
    }

    private List<Disease> diseasesCompatibleWith(InheritanceMode... inheritanceModes) {
        List<Disease> diseaseListBuilder = new ArrayList<>();
        for (InheritanceMode inheritanceMode : inheritanceModes) {
            Disease disease = Disease.builder()
                    .inheritanceMode(inheritanceMode)
                    .build();
            diseaseListBuilder.add(disease);
        }
        return List.copyOf(diseaseListBuilder);
    }

    @Test
    public void emptyGeneWithAnyInheritance() {
        Gene empty = newGene();

        double score = instance.calculateGenePriorityScoreForMode(empty, ModeOfInheritance.ANY);
        assertThat(score, equalTo(0d));
    }

    @Test
    public void prioritisedGeneWithAnyInheritance() {
        Gene gene = newGene();

        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.ANY);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }


    @Test
    public void omimPrioritisedOnlyGeneWithKnownDiseaseCurrentInheritanceMatchesOneDiseaseInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        InheritanceMode[] inheritanceModes = {InheritanceMode.AUTOSOMAL_RECESSIVE, InheritanceMode.AUTOSOMAL_DOMINANT};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.0));
    }

    @Test
    public void prioritisedGeneWithNoKnownDiseasesAnyInheritance() {
        Gene gene = newGene();
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

//        addOmimResultWithCompatibleModes(gene);

        InheritanceMode[] inheritanceModes = {};
        addOmimResultToGene(gene, Collections.emptyMap(), inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.ANY);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithNoKnownDiseasesIncompatibleWithCurrentInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithNoOmimPrioritiserCompatibleWithCurrentInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseasesUnknownInheritance() {
        Gene gene = newGene();
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.UNKNOWN};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseasesAnyInheritance() {
        Gene gene = newGene();
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.AUTOSOMAL_DOMINANT};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.ANY);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    // there are probably too many combinations tested here as this was previously testing the OmimPrioritiser behaviour
    // in conjunction with the rest of the gene scoring. Now it's much simpler internally, but still depends on the output
    // of the OmimPrioritiser
    @Test
    public void prioritisedGeneWithInheritanceModesKnownDiseasesUnknownInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.UNKNOWN};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 1.0);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseCurrentInheritanceMatchesDiseaseAndGeneInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.AUTOSOMAL_DOMINANT};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }


    @Test
    public void prioritisedGeneWithKnownDiseaseCurrentInheritanceNotMatchesGeneInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.AUTOSOMAL_DOMINANT};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseCurrentInheritanceNotMatchesDiseaseInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.AUTOSOMAL_RECESSIVE};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseCurrentInheritanceMatchesOneDiseaseInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.AUTOSOMAL_DOMINANT, InheritanceMode.AUTOSOMAL_RECESSIVE};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double adScore = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(adScore, equalTo(PHENOTYPE_SCORE));

        double arScore = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        assertThat(arScore, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseCurrentInheritanceNotMatchesDiseaseWithUnknownGeneInheritance() {
        // Here we have a gene with a possible dominant or compound het compatible set of variants
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        // This simulates frequent cases where there is an OMIM and Orphanet entry for the same disorder, but Orphanet
        // does not indicate an inheritance mode.
        InheritanceMode[] inheritanceModes = {InheritanceMode.AUTOSOMAL_RECESSIVE, InheritanceMode.UNKNOWN};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5); //does not match disease inheritance pattern
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0); //matches known disease and gene inheritance patterns
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);


        // Under AD the known associated diseases are incompatible so the score should be reduced
        double adScore = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(adScore, equalTo(0.5 * PHENOTYPE_SCORE));

        // Under AR all modes are compatible so we expect the full phenotype score
        double arScore = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        assertThat(arScore, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseasePolygenicInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.POLYGENIC};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseSomaticInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.SOMATIC};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseYlinkedInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.Y_LINKED};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 1.0);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseMitochondrialInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.MITOCHONDRIAL);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.MITOCHONDRIAL};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 1.0);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.MITOCHONDRIAL);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseAdAndArInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseXlinkedInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.X_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.X_LINKED};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.X_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseXlinkedDominantInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.X_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.X_DOMINANT};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.X_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseXlinkedRecessiveInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.X_RECESSIVE);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        InheritanceMode[] inheritanceModes = {InheritanceMode.X_RECESSIVE};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.X_RECESSIVE);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseNoInheritanceModes() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.X_RECESSIVE);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        //this is currently the case with Orphanet
        InheritanceMode[] inheritanceModes = {InheritanceMode.UNKNOWN};
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        addOmimResultToGene(gene, scores, inheritanceModes);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.X_RECESSIVE);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }
}