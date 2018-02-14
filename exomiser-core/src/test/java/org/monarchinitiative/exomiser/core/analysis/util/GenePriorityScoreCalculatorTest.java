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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.MockPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.OMIMPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

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
        Gene gene =  TestFactory.newGeneFGFR2();
        gene.setInheritanceModes(ImmutableSet.copyOf(modes));
        return gene;
    }

    private void addHiPhiveResultWithScore(Gene gene, double phenotypeScore) {
        PriorityResult hiPhiveResult = hiPhiveResult(gene, phenotypeScore);
        gene.addPriorityResult(hiPhiveResult);
    }

    private void addOmimResultWithCompatibleModes(Gene gene, InheritanceMode... inheritanceModes) {
        PriorityResult diseasePriorityResult = omimPriorityResult(gene, inheritanceModes);
        gene.addPriorityResult(diseasePriorityResult);
    }

    private PriorityResult hiPhiveResult(Gene gene, double score) {
        return new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, gene.getEntrezGeneID(), gene.getGeneSymbol(), score);
    }

    private PriorityResult omimPriorityResult(Gene gene, InheritanceMode... inheritanceModes) {
        ImmutableList<Disease> knownDiseases = diseasesCompatibleWith(inheritanceModes);
        return new OMIMPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), 0d, knownDiseases);
    }

    private ImmutableList<Disease> diseasesCompatibleWith(InheritanceMode... inheritanceModes) {
        ImmutableList.Builder<Disease> diseaseListBuilder  = ImmutableList.builder();
        for (InheritanceMode inheritanceMode : inheritanceModes) {
            Disease disease = Disease.builder()
                    .inheritanceMode(inheritanceMode)
                    .build();
            diseaseListBuilder.add(disease);
        }
        return diseaseListBuilder.build();
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

        addOmimResultWithCompatibleModes(gene, InheritanceMode.AUTOSOMAL_RECESSIVE, InheritanceMode.AUTOSOMAL_DOMINANT);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.0));
    }

    @Test
    public void prioritisedGeneWithNoKnownDiseasesAnyInheritance() {
        Gene gene = newGene();
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.ANY);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithNoKnownDiseasesIncompatibleWithCurrentInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene);

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

        addOmimResultWithCompatibleModes(gene, InheritanceMode.UNKNOWN);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseasesAnyInheritance() {
        Gene gene = newGene();
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.AUTOSOMAL_DOMINANT);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.ANY);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithInheritanceModesKnownDiseasesUnknownInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.UNKNOWN);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseCurrentInheritanceMatchesDiseaseAndGeneInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.AUTOSOMAL_DOMINANT);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }


    @Test
    public void prioritisedGeneWithKnownDiseaseCurrentInheritanceNotMatchesGeneInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.AUTOSOMAL_DOMINANT);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseCurrentInheritanceNotMatchesDiseaseInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.AUTOSOMAL_RECESSIVE);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseCurrentInheritanceMatchesOneDiseaseInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.AUTOSOMAL_RECESSIVE, InheritanceMode.AUTOSOMAL_DOMINANT);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }


    @Test
    public void prioritisedGeneWithKnownDiseasePolygenicInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.POLYGENIC);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseSomaticInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);


        addOmimResultWithCompatibleModes(gene, InheritanceMode.SOMATIC);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(0.5 * PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseYlinkedInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.Y_LINKED);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseMitochondrialInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.MITOCHONDRIAL);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.MITOCHONDRIAL);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.MITOCHONDRIAL);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseAdAndArInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.AUTOSOMAL_DOMINANT_AND_RECESSIVE);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.AUTOSOMAL_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseXlinkedInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.X_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.X_LINKED);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.X_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseXlinkedDominantInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.X_DOMINANT);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.X_DOMINANT);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.X_DOMINANT);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseXlinkedRecessiveInheritance() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.X_RECESSIVE);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        addOmimResultWithCompatibleModes(gene, InheritanceMode.X_RECESSIVE);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.X_RECESSIVE);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }

    @Test
    public void prioritisedGeneWithKnownDiseaseNoInheritanceModes() {
        Gene gene = newGeneCompatibleWith(ModeOfInheritance.X_RECESSIVE);
        addHiPhiveResultWithScore(gene, PHENOTYPE_SCORE);

        //this is currently the case with Orphanet
        Disease diseaseWithNoSpecifiedInheritanceMode = Disease.builder().diseaseId("ORPHA:12345").build();

        PriorityResult omimResult = new OMIMPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), 0d, ImmutableList.of(diseaseWithNoSpecifiedInheritanceMode));
        gene.addPriorityResult(omimResult);

        double score = instance.calculateGenePriorityScoreForMode(gene, ModeOfInheritance.X_RECESSIVE);
        assertThat(score, equalTo(PHENOTYPE_SCORE));
    }
}