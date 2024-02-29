/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantDataService;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.MALE;
import static org.monarchinitiative.exomiser.core.model.Pedigree.justProband;

/**
 * This is not a unit test - it is a full integration test of the ACMG assignment process
 */
class AcmgAssignmentCalculatorTest {

    @Test
    void calculatePathAcmgAssignments() {
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        TranscriptAnnotation startLostAnnotation = TranscriptAnnotation.builder()
                .geneSymbol("PTEN")
                .accession("ENST00000371953.7")
                .variantEffect(VariantEffect.START_LOST)
                .rankType(TranscriptAnnotation.RankType.EXON)
                .rank(1)
                .rankTotal(9)
                .build();

        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                .variantEffect(VariantEffect.START_LOST)
                .annotations(List.of(startLostAnnotation))
                .contributingModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .pathogenicityData(PathogenicityData.of(ClinVarData.builder()
                                .primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC)
                                .reviewStatus(ClinVarData.ReviewStatus.REVIEWED_BY_EXPERT_PANEL)
                                .build(),
                        PathogenicityScore.of(PathogenicitySource.REVEL, 1.0f), PathogenicityScore.of(PathogenicitySource.MVP, 1.0f)))
                .filterResults(FilterResult.pass(FilterType.FREQUENCY_FILTER), FilterResult.pass(FilterType.PATHOGENICITY_FILTER))
                .build();

        Disease cowdenSyndrome = Disease.builder()
                .diseaseId("OMIM:158350")
                .diseaseName("COWDEN SYNDROME 1; CWS1")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .diseaseType(Disease.DiseaseType.DISEASE)
                .build();
        OmimPriorityResult omimPriorityResult = new OmimPriorityResult(12345, "PTEN", 1.0, List.of(cowdenSyndrome), Map.of());
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(1.0, cowdenSyndrome, List.of()));

        Gene gene = new Gene("PTEN", 12345);
        gene.addVariant(variantEvaluation);
        gene.addPriorityResult(omimPriorityResult);

        AcmgEvidence acmgEvidence = AcmgEvidence.builder()
                .add(AcmgCriterion.PVS1)
                .add(AcmgCriterion.PM2, AcmgCriterion.Evidence.SUPPORTING)
                .add(AcmgCriterion.PP3, AcmgCriterion.Evidence.STRONG)
                .add(AcmgCriterion.PP4)
                .add(AcmgCriterion.PP5, AcmgCriterion.Evidence.VERY_STRONG)
                .build();
        AcmgAssignment acmgAssignment = AcmgAssignment.of(variantEvaluation, gene.getGeneIdentifier(), ModeOfInheritance.AUTOSOMAL_DOMINANT, cowdenSyndrome, acmgEvidence, AcmgClassification.PATHOGENIC);

        AcmgEvidenceAssigner acmgEvidenceAssigner = new Acmg2015EvidenceAssigner("proband", justProband("proband", MALE), TestVariantDataService.stub());
        AcmgAssignmentCalculator instance = new AcmgAssignmentCalculator(acmgEvidenceAssigner, new Acgs2020Classifier());
        List<AcmgAssignment> acmgAssignments = instance.calculateAcmgAssignments(ModeOfInheritance.AUTOSOMAL_DOMINANT, gene, List.of(variantEvaluation), compatibleDiseaseMatches);
        assertThat(acmgAssignments, equalTo(List.of(acmgAssignment)));
    }

    @Test
    void calculateVusAcmgAssignments() {
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN, 0.5f)))
                .pathogenicityData(PathogenicityData.of(PathogenicityScore.of(PathogenicitySource.REVEL, 0.5f)))
                .contributingModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .filterResults(FilterResult.pass(FilterType.FREQUENCY_FILTER), FilterResult.pass(FilterType.PATHOGENICITY_FILTER))
                .build();

        Disease cowdenSyndrome = Disease.builder()
                .diseaseId("OMIM:158350")
                .diseaseName("COWDEN SYNDROME 1; CWS1")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .diseaseType(Disease.DiseaseType.DISEASE)
                .build();
        OmimPriorityResult omimPriorityResult = new OmimPriorityResult(12345, "PTEN", 1.0, List.of(cowdenSyndrome), Map.of());
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));

        Gene gene = new Gene("PTEN", 12345);
        gene.addVariant(variantEvaluation);
        gene.addPriorityResult(omimPriorityResult);

        AcmgEvidence acmgEvidence = AcmgEvidence.empty();
        AcmgAssignment acmgAssignment = AcmgAssignment.of(variantEvaluation, gene.getGeneIdentifier(), ModeOfInheritance.AUTOSOMAL_DOMINANT, cowdenSyndrome, acmgEvidence, AcmgClassification.UNCERTAIN_SIGNIFICANCE);

        AcmgEvidenceAssigner acmgEvidenceAssigner = new Acmg2015EvidenceAssigner("proband", justProband("proband", MALE), TestVariantDataService.stub());
        AcmgAssignmentCalculator instance = new AcmgAssignmentCalculator(acmgEvidenceAssigner, new Acgs2020Classifier());
        List<AcmgAssignment> acmgAssignments = instance.calculateAcmgAssignments(ModeOfInheritance.AUTOSOMAL_DOMINANT, gene, List.of(variantEvaluation), compatibleDiseaseMatches);
        assertThat(acmgAssignments, equalTo(List.of(acmgAssignment)));
    }

    @Test
    void calculateBenignAcmgAssignments() {
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/127667/ - BA1: Allele frequency of 0.0142 (1.42%, 23/1618 alleles) in the African subpopulation of the gnomAD cohort.
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89622915, "A", "G")
                .geneSymbol("PTEN")
                .variantEffect(VariantEffect.SYNONYMOUS_VARIANT)
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN, 50.0f)))
                .pathogenicityData(PathogenicityData.of(PathogenicityScore.of(PathogenicitySource.REVEL, 0.0f), PathogenicityScore.of(PathogenicitySource.MVP, 0.0f)))
                .contributingModes(Set.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .filterResults(FilterResult.fail(FilterType.FREQUENCY_FILTER), FilterResult.pass(FilterType.PATHOGENICITY_FILTER))
                .build();

        Disease cowdenSyndrome = Disease.builder()
                .diseaseId("OMIM:158350")
                .diseaseName("COWDEN SYNDROME 1; CWS1")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .diseaseType(Disease.DiseaseType.DISEASE)
                .build();
        OmimPriorityResult omimPriorityResult = new OmimPriorityResult(12345, "PTEN", 1.0, List.of(cowdenSyndrome), Map.of());
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));

        Gene gene = new Gene("PTEN", 12345);
        gene.addVariant(variantEvaluation);
        gene.addPriorityResult(omimPriorityResult);

        AcmgEvidence acmgEvidence = AcmgEvidence.builder()
                .add(AcmgCriterion.BA1)
                .build();
        AcmgAssignment acmgAssignment = AcmgAssignment.of(variantEvaluation, gene.getGeneIdentifier(), ModeOfInheritance.AUTOSOMAL_DOMINANT, cowdenSyndrome, acmgEvidence, AcmgClassification.BENIGN);

        AcmgEvidenceAssigner acmgEvidenceAssigner = new Acmg2015EvidenceAssigner("proband", justProband("proband", MALE), TestVariantDataService.stub());
        AcmgAssignmentCalculator instance = new AcmgAssignmentCalculator(acmgEvidenceAssigner, new Acmg2020PointsBasedClassifier());
        List<AcmgAssignment> acmgAssignments = instance.calculateAcmgAssignments(ModeOfInheritance.AUTOSOMAL_DOMINANT, gene, List.of(variantEvaluation), compatibleDiseaseMatches);
        assertThat(acmgAssignments, equalTo(List.of(acmgAssignment)));
    }
}