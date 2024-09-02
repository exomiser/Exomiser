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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantDataService;
import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.*;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.FEMALE;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.MALE;
import static org.monarchinitiative.exomiser.core.model.Pedigree.justProband;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ClinSig.*;

class Acmg2015EvidenceAssignerTest {


    private Acmg2015EvidenceAssigner acmgEvidenceAssigner(String probandId, Pedigree pedigree) {
        return new Acmg2015EvidenceAssigner(probandId, pedigree, TestVariantDataService.stub());
    }

    private Acmg2015EvidenceAssigner acmgEvidenceAssigner(String probandId, Pedigree pedigree, Map<Variant, ClinVarData> expectedClinVarData) {
        return new Acmg2015EvidenceAssigner(probandId, pedigree, TestVariantDataService.builder().expectedClinVarData(expectedClinVarData).build());
    }

    @Test
    void throwsExceptionWithMismatchedIds() {
        assertThrows(IllegalArgumentException.class, () -> acmgEvidenceAssigner("Zaphod", justProband("Ford", MALE)));
    }

    @ParameterizedTest
    @CsvSource({
            // nonsense, middle-exon NMD predicted
            "STOP_LOST, 2, VERY_STRONG",
            "STOP_GAINED, 2, VERY_STRONG",
            "FRAMESHIFT_ELONGATION, 2, VERY_STRONG",
            "FRAMESHIFT_TRUNCATION, 2, VERY_STRONG",
            "FRAMESHIFT_VARIANT, 2, VERY_STRONG",
            // nonsense, last exon - NMD escape
            "STOP_LOST, 5, STRONG",
            "STOP_GAINED, 5, STRONG",
            "FRAMESHIFT_ELONGATION, 5, STRONG",
            "FRAMESHIFT_TRUNCATION, 5, STRONG",
            "FRAMESHIFT_VARIANT, 5, STRONG",
            // splice donor/acceptor site, middle-exon NMD predicted
            "SPLICE_DONOR_VARIANT, 2, VERY_STRONG",
            "SPLICE_ACCEPTOR_VARIANT, 2, VERY_STRONG",
            // splice donor/acceptor site, last exon - NMD escape
            "SPLICE_DONOR_VARIANT, 5, STRONG",
            "SPLICE_ACCEPTOR_VARIANT, 5, STRONG",
            // transcript ablation / exon loss
            "TRANSCRIPT_ABLATION, 2, VERY_STRONG", // sufficient as a STAND_ALONE in the case of HI genes and no conflicting evidence
            "EXON_LOSS_VARIANT, 2, VERY_STRONG", // NMD
            "EXON_LOSS_VARIANT, 5, STRONG", // NMD escape
            // start lost
            "START_LOST, 1, MODERATE",
    })
    void testAssignsPVS1(VariantEffect variantEffect, int exonNumber, AcmgCriterion.Evidence evidence) {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        // requires variant to be on a transcript predicted to undergo NMD in a LoF-intolerant gene for full PVS1
        TranscriptAnnotation transcriptAnnotation = TranscriptAnnotation.builder()
                .variantEffect(variantEffect)
                .rankType(TranscriptAnnotation.RankType.EXON)
                .rank(exonNumber)
                .rankTotal(5)
                .build();
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(variantEffect)
                .annotations(List.of(transcriptAnnotation))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PVS1, evidence).build()));
    }

    @ParameterizedTest
    @CsvSource({
            "MALE, AUTOSOMAL_DOMINANT, AUTOSOMAL_DOMINANT, true",
            "FEMALE, AUTOSOMAL_DOMINANT, AUTOSOMAL_DOMINANT, true",
            "UNKNOWN, AUTOSOMAL_DOMINANT, AUTOSOMAL_DOMINANT, true",

            "MALE, AUTOSOMAL_RECESSIVE, AUTOSOMAL_RECESSIVE, true",
            "FEMALE, AUTOSOMAL_RECESSIVE, AUTOSOMAL_RECESSIVE, true",
            "UNKNOWN, AUTOSOMAL_RECESSIVE, AUTOSOMAL_RECESSIVE, true",

            "MALE, AUTOSOMAL_DOMINANT, AUTOSOMAL_RECESSIVE, false",
            "FEMALE, AUTOSOMAL_DOMINANT, AUTOSOMAL_RECESSIVE, false",
            "UNKNOWN, AUTOSOMAL_DOMINANT, AUTOSOMAL_RECESSIVE, false",

            "MALE, X_RECESSIVE, X_RECESSIVE, true",
            "FEMALE, X_RECESSIVE, X_RECESSIVE, true",
            "UNKNOWN, X_RECESSIVE, X_RECESSIVE, true",

            "MALE, X_DOMINANT, X_DOMINANT, true",
            "FEMALE, X_DOMINANT, X_DOMINANT, true",
            "UNKNOWN, X_DOMINANT, X_DOMINANT, true",
    })
    void testAssignsPVS1(Pedigree.Individual.Sex probandSex, InheritanceMode diseaseInheritanceMode, ModeOfInheritance modeOfInheritance, boolean expectPvs1) {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", probandSex));
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        TranscriptAnnotation transcriptAnnotation = TranscriptAnnotation.builder()
                .variantEffect(VariantEffect.SPLICE_DONOR_VARIANT)
                .rankType(TranscriptAnnotation.RankType.EXON)
                .rank(1)
                .rankTotal(5)
                .build();
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(VariantEffect.SPLICE_DONOR_VARIANT)
                .annotations(List.of(transcriptAnnotation))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(diseaseInheritanceMode).diseaseType(Disease.DiseaseType.DISEASE).build();
        List<Disease> knownDiseases = diseaseInheritanceMode.isCompatibleWith(modeOfInheritance) ? List.of(cowdenSyndrome) : List.of();
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, modeOfInheritance, List.of(variantEvaluation), knownDiseases, List.of());
        AcmgEvidence expected = expectPvs1 ? AcmgEvidence.builder().add(PVS1).build() : AcmgEvidence.empty();
        assertThat(acmgEvidence, equalTo(expected));
    }

    @ParameterizedTest
    @CsvSource({
            "MISSENSE_VARIANT, p.(Ala42Thr), p.(Ala42Thr), PATHOGENIC, 3, PS1, STRONG",
            "MISSENSE_VARIANT, p.(Ala42Thr), p.(Ala42Thr), PATHOGENIC, 2, PS1, STRONG",
            "MISSENSE_VARIANT, p.(Ala42Thr), p.(Ala42Thr), PATHOGENIC, 1, PS1, MODERATE",
            "MISSENSE_VARIANT, p.(Ala42Thr), p.(Ala42Thr), PATHOGENIC, 0, PS1, SUPPORTING",

            "MISSENSE_VARIANT, p.(Ala42Thr), p.(Ala42Cys), PATHOGENIC, 3, PM5, MODERATE",
            "MISSENSE_VARIANT, p.(Ala42Thr), p.(Ala42Cys), PATHOGENIC, 2, PM5, MODERATE",
            "MISSENSE_VARIANT, p.(Ala42Thr), p.(Ala42Cys), PATHOGENIC, 1, PM5, SUPPORTING",
            "MISSENSE_VARIANT, p.(Ala42Thr), p.(Ala42Cys), PATHOGENIC, 0, PM5, SUPPORTING",
    })
    void testAssignsPS1orPM5(VariantEffect variantEffect, String hgvsP, String clinVarHgvsP, ClinVarData.ClinSig clinSig, int clinVarStarRating, AcmgCriterion acmgCriterion, AcmgCriterion.Evidence evidence) {
        ClinVarData clinVarData = ClinVarData.builder()
                .primaryInterpretation(clinSig)
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .reviewStatus(starRatingToReviewStatus(clinVarStarRating))
                .hgvsProtein(clinVarHgvsP)
                .build();
        Variant overlapping = TestFactory.variantBuilder(10, 89624227, "A", "C").build();

        VariantDataService variantDataService = TestVariantDataService.builder().put(overlapping, clinVarData).build();

        TranscriptAnnotation transcriptAnnotation = TranscriptAnnotation.builder()
                .variantEffect(variantEffect)
                .hgvsProtein(hgvsP)
                .build();
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("GENE")
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(variantEffect)
                .annotations(List.of(transcriptAnnotation))
                .build();

        Acmg2015EvidenceAssigner instance = new Acmg2015EvidenceAssigner("proband", justProband("proband", MALE), variantDataService);

        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(acmgCriterion, evidence).build()));
    }

    private ClinVarData.ReviewStatus starRatingToReviewStatus(int clinVarStarRating) {
        return switch (clinVarStarRating) {
            case 4 -> ClinVarData.ReviewStatus.PRACTICE_GUIDELINE;
            case 3 -> ClinVarData.ReviewStatus.REVIEWED_BY_EXPERT_PANEL;
            case 2 -> ClinVarData.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS;
            case 1 -> ClinVarData.ReviewStatus.CRITERIA_PROVIDED_SINGLE_SUBMITTER;
            default -> ClinVarData.ReviewStatus.NO_ASSERTION_CRITERIA_PROVIDED;
        };
    }

    @ParameterizedTest
    @CsvSource({
            // requires 4 or more missense P/LP variants in the local region
            "1, 3, 0, 0, 0, PM1, MODERATE",
            "3, 3, 0, 0, 0, PM1, MODERATE",
            "0, 4, 0, 0, 0, PM1, MODERATE",
            "0, 4, 3, 0, 0, PM1, MODERATE",
            "4, 0, 3, 0, 0, PM1, MODERATE",
            "0, 4, 4, 0, 0, PM1, SUPPORTING",
            "4, 0, 4, 0, 0, PM1, SUPPORTING",
            "0, 0, 0, 0, 0, BP1, SUPPORTING", // Here BP1 is acting as a null marker, results should be empty
            "0, 0, 4, 0, 0, BP1, SUPPORTING", // Here BP1 is acting as a null marker, results should be empty
            "0, 0, 4, 1, 0, BP1, SUPPORTING", // Here BP1 is acting as a null marker, results should be empty
            "4, 0, 4, 1, 0, BP1, SUPPORTING", // Here BP1 is acting as a null marker, results should be empty
            "4, 0, 0, 1, 1, BP1, SUPPORTING", // Here BP1 is acting as a null marker, results should be empty
    })
    void assignPM1(int p, int lp, int vus, int lb, int b, AcmgCriterion expectedCriteria, Evidence expectedEvidence) {
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("GENE")
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .annotations(List.of(TranscriptAnnotation.builder()
                        .build()))
                .build();

        TestVariantDataService.Builder variantDataServiceBuilder = TestVariantDataService.builder();

        int[] categoryCounts = {p, lp, vus, lb, b};
        ClinVarData.ClinSig[] categories = {PATHOGENIC, LIKELY_PATHOGENIC, UNCERTAIN_SIGNIFICANCE, LIKELY_BENIGN, BENIGN};
        int currentPos = variantEvaluation.start();
        for (int i = 0; i < categoryCounts.length; i++) {
            for (int j = 0; j < categoryCounts[i]; j++) {
                Variant variant = TestFactory.variantBuilder(10, ++currentPos, "A", "C").build();
                ClinVarData clinVarData = ClinVarData.builder()
                        .primaryInterpretation(categories[i])
                        .variantEffect(VariantEffect.MISSENSE_VARIANT)
                        .build();
                variantDataServiceBuilder.put(variant, clinVarData);
            }
        }
        TestVariantDataService variantDataService = variantDataServiceBuilder.build();
        Acmg2015EvidenceAssigner instance = new Acmg2015EvidenceAssigner("proband", justProband("proband", MALE), variantDataService);

        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
        AcmgEvidence expected = expectedCriteria == BP1 ? AcmgEvidence.empty() : AcmgEvidence.builder().add(expectedCriteria, expectedEvidence).build();
        assertThat(acmgEvidence, equalTo(expected));
    }


    @Test
    void testAssignsPS2() {
        Individual proband = Individual.builder().id("proband").motherId("mother").fatherId("father").sex(MALE).status(Status.AFFECTED).build();
        Individual mother = Individual.builder().id("mother").sex(FEMALE).status(Status.UNAFFECTED).build();
        Individual father = Individual.builder().id("father").sex(MALE).status(Status.UNAFFECTED).build();
        Pedigree pedigree = Pedigree.of(proband, mother, father);
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", pedigree);
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                // n.b. PTEN is a haploinsufficient gene
                .geneSymbol("PTEN")
                // n.b. has frequency data - will not trigger PM2
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f)))
                // n.b. missense variant - will not trigger PVS1
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(SampleGenotypes.of(
                        SampleData.of("proband", SampleGenotype.het()),
                        SampleData.of("mother", SampleGenotype.homRef()),
                        SampleData.of("father", SampleGenotype.homRef())
                ))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        // n.b. low phenotype score - will not trigger PP4
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), compatibleDiseaseMatches);

        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PS2).build()));
    }

    @Test
    void testAssignsPS2_hasFamilyHistory() {
        Individual proband = Individual.builder().id("proband").motherId("mother").fatherId("father").sex(MALE).status(Status.AFFECTED).build();
        Individual mother = Individual.builder().id("mother").sex(FEMALE).status(Status.UNAFFECTED).build();
        Individual father = Individual.builder().id("father").sex(MALE).status(Status.UNAFFECTED).build();
        Pedigree pedigree = Pedigree.of(proband, mother, father);
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", pedigree);
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                // n.b. PTEN is a haploinsufficient gene
                .geneSymbol("PTEN")
                // n.b. has frequency data - will not trigger PM2
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f)))
                // n.b. missense variant - will not trigger PVS1
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(SampleGenotypes.of(
                        SampleData.of("proband", SampleGenotype.het()),
                        SampleData.of("mother", SampleGenotype.het()), // Unaffected mother has same genotype - can't be PS2
                        SampleData.of("father", SampleGenotype.homRef())
                ))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        // n.b. low phenotype score - will not trigger PP4
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), compatibleDiseaseMatches);

        assertThat(acmgEvidence, equalTo(AcmgEvidence.empty()));
    }

    @Test
    void testAssignsPM2AutosomalDominant() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", Pedigree.empty());
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 12345, "A", "G")
                // n.b. missing frequency data - will trigger PM2
                .frequencyData(FrequencyData.empty())
                .build();
        // Requires variant to be in gene associated with a disorder in order that any ACMG criteria can be applied
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_RECESSIVE).diseaseType(Disease.DiseaseType.DISEASE).build();

        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());

        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PM2, Evidence.SUPPORTING).build()));
    }

    @Test
    void testAssignsPM2AutosomalDominantAllowsPresenceOfLocalFrequency() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", Pedigree.empty());
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 12345, "A", "G")
                // n.b. missing frequency data APART FROM LOCAL - will trigger PM2
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.LOCAL, 0.019f)))
                .build();
        // Requires variant to be in gene associated with a disorder in order that any ACMG criteria can be applied
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_RECESSIVE).diseaseType(Disease.DiseaseType.DISEASE).build();

        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());

        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PM2, Evidence.SUPPORTING).build()));
    }

    @Test
    void testAssignsPM2AutosomalRecessive() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", Pedigree.empty());
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 12345, "A", "G")
                // n.b. low frequency for AR - will trigger PM2
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.GNOMAD_E_EAS, 0.009f)))
                .build();

        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_RECESSIVE, List.of(variantEvaluation), List.of(), List.of());

        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PM2, Evidence.SUPPORTING).build()));
    }

    @Test
    void testVariantNeedNotBeInGeneWithKnownDiseaseAssociationForAcmgCriteriaToBeAssigned() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", Pedigree.empty());
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(1, 12345, "A", "G")
                // n.b. missing frequency data - should trigger PM2
                .frequencyData(FrequencyData.of())
                .build();
        // Not required that a variant is in gene associated with a disorder in order that any ACMG criteria can be applied
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(), List.of());

        AcmgEvidence expected = AcmgEvidence.builder().add(PM2, Evidence.SUPPORTING).build();
        assertThat(acmgEvidence, equalTo(expected));
    }

    @Test
    void testAssignsPM3() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", null);
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89000000, "A", "G")
                // n.b. PTEN is a haploinsufficient gene
                .geneSymbol("PTEN")
                // n.b. has frequency data - will not trigger PM2
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f)))
                // n.b. missense variant - will not trigger PVS1
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(SampleGenotypes.of(
                        SampleData.of("proband", SampleGenotype.parseGenotype("0|1"))
                ))
                .build();

        VariantEvaluation pathogenic = TestFactory.variantBuilder(10, 89624227, "A", "G")
                // n.b. PTEN is a haploinsufficient gene
                .geneSymbol("PTEN")
                // n.b. start loss variant - will trigger PVS1
                .variantEffect(VariantEffect.START_LOST)
                .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(PATHOGENIC).reviewStatus(ClinVarData.ReviewStatus.REVIEWED_BY_EXPERT_PANEL).build()))
                .sampleGenotypes(SampleGenotypes.of(
                        SampleData.of("proband", SampleGenotype.parseGenotype("1|0"))
                ))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_RECESSIVE).diseaseType(Disease.DiseaseType.DISEASE).build();
        // n.b. low phenotype score - will not trigger PP4
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_RECESSIVE, List.of(variantEvaluation, pathogenic), List.of(cowdenSyndrome), compatibleDiseaseMatches);

        AcmgEvidence expected = AcmgEvidence.builder().add(AcmgCriterion.PM3).build();
        assertThat(acmgEvidence, equalTo(expected));
    }

    @Test
    void testAssignsBP2_InCisWithPathAR() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", Pedigree.empty());
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89000000, "A", "G")
                // n.b. has frequency data - will not trigger PM2
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f)))
                .sampleGenotypes(SampleGenotypes.of(
                        SampleData.of("proband", SampleGenotype.parseGenotype("0|1"))
                ))
                .build();

        VariantEvaluation pathogenic = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(PATHOGENIC).reviewStatus(ClinVarData.ReviewStatus.REVIEWED_BY_EXPERT_PANEL).build()))
                .sampleGenotypes(SampleGenotypes.of(
                        SampleData.of("proband", SampleGenotype.parseGenotype("0|1"))
                ))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_RECESSIVE).diseaseType(Disease.DiseaseType.DISEASE).build();
        // n.b. low phenotype score - will not trigger PP4
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_RECESSIVE, List.of(variantEvaluation, pathogenic), List.of(cowdenSyndrome), compatibleDiseaseMatches);

        AcmgEvidence expected = AcmgEvidence.builder().add(AcmgCriterion.BP2).build();
        assertThat(acmgEvidence, equalTo(expected));
    }

    @Test
    void testAssignsBP2_InTransWithPathAD() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", Pedigree.empty());
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89000000, "A", "G")
                // n.b. PTEN is a haploinsufficient gene
                .geneSymbol("PTEN")
                // n.b. has frequency data - will not trigger PM2
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f)))
                // n.b. missense variant - will not trigger PVS1
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(SampleGenotypes.of(
                        SampleData.of("proband", SampleGenotype.parseGenotype("0|1"))
                ))
                .build();

        VariantEvaluation pathogenic = TestFactory.variantBuilder(10, 89624227, "A", "G")
                // n.b. PTEN is a haploinsufficient gene
                .geneSymbol("PTEN")
                // n.b. start loss variant - will trigger PVS1
                .variantEffect(VariantEffect.START_LOST)
                .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(PATHOGENIC).reviewStatus(ClinVarData.ReviewStatus.REVIEWED_BY_EXPERT_PANEL).build()))
                .sampleGenotypes(SampleGenotypes.of(
                        SampleData.of("proband", SampleGenotype.parseGenotype("1|0"))
                ))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        // n.b. low phenotype score - will not trigger PP4
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation, pathogenic), List.of(cowdenSyndrome), compatibleDiseaseMatches);

        AcmgEvidence expected = AcmgEvidence.builder().add(AcmgCriterion.BP2).build();
        assertThat(acmgEvidence, equalTo(expected));
    }

    @Test
    void testAssignsPM4() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("MUC6")
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(VariantEffect.STOP_LOST)
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PM4).build()));
    }

    @Test
    void testAssignsPM4_NotAssignedPM4WhenPVS1Present() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));

        TranscriptAnnotation startLostAnnotation = TranscriptAnnotation.builder()
                .geneSymbol("PTEN")
                .accession("ENST00000371953.7")
                .variantEffect(VariantEffect.START_LOST)
                .rankType(TranscriptAnnotation.RankType.EXON)
                .rank(1)
                .rankTotal(9)
                .build();

        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                // haploinsufficient gene
                .geneSymbol("PTEN")
                .annotations(List.of(startLostAnnotation)) // prevent PM4 as PVS1 already triggered
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(VariantEffect.STOP_LOST)
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PVS1).build()));
    }

    @Nested
    class ComputationalEvidence {

        @Test
        void testAssignsPP3() {
            Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .pathogenicityData(PathogenicityData.of(
                            PathogenicityScore.of(PathogenicitySource.REVEL, 1.0f),
                            PathogenicityScore.of(PathogenicitySource.MVP, 1.0f)
                    ))
                    .variantEffect(VariantEffect.MISSENSE_VARIANT)
                    .build();
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
            assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PP3, Evidence.STRONG).build()));
        }

        @ParameterizedTest
        @CsvSource({
                "MVP, 1.0f, , ",
                "REVEL, 1.0f, PP3, STRONG",
                "CADD, 0.0f, , ",
                "REVEL, 0.0f, BP4, VERY_STRONG"
        })
        void testAssignsPP3BP4_singleScoreIsInsufficientUnlessItsRevel(PathogenicitySource pathogenicitySource, float pathogenicityScore, AcmgCriterion acmgCriterion, Evidence evidence) {
            Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .pathogenicityData(PathogenicityData.of(PathogenicityScore.of(pathogenicitySource, pathogenicityScore)))
                    .variantEffect(VariantEffect.MISSENSE_VARIANT)
                    .build();
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());

            AcmgEvidence expected = acmgCriterion == null ? AcmgEvidence.empty() : AcmgEvidence.builder().add(acmgCriterion, evidence).build();
            assertThat(acmgEvidence, equalTo(expected));
        }

        @Test
        void testAssignsPP3_majorityMustBePath() {
            Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .pathogenicityData(PathogenicityData.of(
                            PathogenicityScore.of(PathogenicitySource.POLYPHEN, 1.0f),
                            PathogenicityScore.of(PathogenicitySource.MVP, 1.0f),
                            PathogenicityScore.of(PathogenicitySource.MUTATION_TASTER, 0.0f)
                    ))
                    .variantEffect(VariantEffect.MISSENSE_VARIANT)
                    .build();
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
            assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PP3).build()));
        }

        @Test
        void testPP3andPM4_majorityMustBePathOrBenign() {
            Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .pathogenicityData(PathogenicityData.of(
                            PathogenicityScore.of(PathogenicitySource.POLYPHEN, 1.0f),
                            PathogenicityScore.of(PathogenicitySource.MVP, 1.0f),
                            PathogenicityScore.of(PathogenicitySource.SIFT, 1.0f),
                            PathogenicityScore.of(PathogenicitySource.MUTATION_TASTER, 0.0f)
                    ))
                    .variantEffect(VariantEffect.MISSENSE_VARIANT)
                    .build();
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
            assertThat(acmgEvidence, equalTo(AcmgEvidence.empty()));
        }

        @Test
        void testAssignsBP4() {
            Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .pathogenicityData(PathogenicityData.of(
                            PathogenicityScore.of(PathogenicitySource.POLYPHEN, 0.0f),
                            PathogenicityScore.of(PathogenicitySource.MVP, 0.0f)
                    ))
                    .variantEffect(VariantEffect.MISSENSE_VARIANT)
                    .build();
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
            assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.BP4).build()));
        }

        @Test
        void testAssignsBP4_majorityMustBeBenign() {
            Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .pathogenicityData(PathogenicityData.of(
                            PathogenicityScore.of(PathogenicitySource.POLYPHEN, 0.0f),
                            PathogenicityScore.of(PathogenicitySource.MVP, 0.0f),
                            PathogenicityScore.of(PathogenicitySource.MUTATION_TASTER, 1.0f)
                    ))
                    .variantEffect(VariantEffect.MISSENSE_VARIANT)
                    .build();
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
            assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.BP4).build()));
        }

        @ParameterizedTest
        @CsvSource({
                "0.932, PP3, STRONG",
                "0.773, PP3, MODERATE",
                "0.644f, PP3, SUPPORTING",

                "0.290f, BP4, SUPPORTING",
                "0.183f, BP4, MODERATE",
                "0.016f, BP4, STRONG",
                "0.003f, BP4, VERY_STRONG",
        })
        public void testRevelOverridesAllOtherScores(float revelScore, AcmgCriterion acmgCriterion, Evidence evidence) {
            Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .pathogenicityData(PathogenicityData.of(
                            // REVEL, if present, should be used as the sole means of classifying the PP3/BP4
                            PathogenicityScore.of(PathogenicitySource.REVEL, revelScore),
                            PathogenicityScore.of(PathogenicitySource.MVP, 0.0f),
                            PathogenicityScore.of(PathogenicitySource.POLYPHEN, 1.0f),
                            PathogenicityScore.of(PathogenicitySource.SIFT, 0.0f),
                            PathogenicityScore.of(PathogenicitySource.MUTATION_TASTER, 1.0f)
                    ))
                    .variantEffect(VariantEffect.MISSENSE_VARIANT)
                    .build();
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
            assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(acmgCriterion, evidence).build()));
        }
    }

    // PP4
    @ParameterizedTest
    @CsvSource({
            "0.51, SUPPORTING",
            "0.7, MODERATE",
    })
    void testAssignsPP4(double phenotypeScore, Evidence evidence) {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        // High phenotype match triggers - PP4
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(phenotypeScore, cowdenSyndrome, List.of()));

        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), compatibleDiseaseMatches);
        AcmgEvidence expected = evidence == null ? AcmgEvidence.empty() : AcmgEvidence.builder().add(PP4, evidence).build();
        assertThat(acmgEvidence, equalTo(expected));
    }

    @Nested
    class ClinicalEvidence {

        @ParameterizedTest
        @CsvSource(
                delimiter = ';',
                value = {
                        "criteria provided, single submitter; SUPPORTING",
                        "criteria provided, multiple submitters, no conflicts; STRONG",
                        "reviewed by expert panel; VERY_STRONG",
                        "practice guideline; VERY_STRONG",
                })
        void testAssignsPP5(String reviewStatus, AcmgCriterion.Evidence evidence) {
            Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", Pedigree.empty());
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89000000, "A", "G")
                    // n.b. PTEN is a haploinsufficient gene
                    .geneSymbol("PTEN")
                    // n.b. has frequency data - will not trigger PM2
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f)))
                    // n.b. missense variant - will not trigger PVS1
                    .variantEffect(VariantEffect.MISSENSE_VARIANT)
                    .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(PATHOGENIC).reviewStatus(ClinVarData.ReviewStatus.parseReviewStatus(reviewStatus)).build()))
                    .build();

            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            // n.b. low phenotype score - will not trigger PP4
            List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), compatibleDiseaseMatches);

            assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(PP5, evidence).build()));
        }

        @ParameterizedTest
        @CsvSource(
                delimiter = ';',
                value = {
                        "criteria provided, single submitter; SUPPORTING",
                        "criteria provided, multiple submitters, no conflicts; STRONG",
                        "reviewed by expert panel; VERY_STRONG",
                        "practice guideline; VERY_STRONG",
                })
        void testAssignsBP6(String reviewStatus, AcmgCriterion.Evidence evidence) {
            Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", Pedigree.empty());
            // https://www.ncbi.nlm.nih.gov/clinvar/variation/127667/
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89622915, "A", "G")
                    // n.b. PTEN is a haploinsufficient gene
                    .geneSymbol("PTEN")
                    // n.b. has frequency data - will not trigger PM2
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AFRICAN_INC_AFRICAN_AMERICAN, 1.42f)))
                    // n.b. missense variant - will not trigger PVS1
                    .variantEffect(VariantEffect.MISSENSE_VARIANT)
                    .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(BENIGN).reviewStatus(ClinVarData.ReviewStatus.parseReviewStatus(reviewStatus)).build()))
                    .build();

            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            // n.b. low phenotype score - will not trigger PP4
            List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), compatibleDiseaseMatches);

            assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(BP6, evidence).build()));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "ESP_AA, true",
            "THOUSAND_GENOMES, true",
            "TOPMED, true",
            "UK10K, true",
            "GNOMAD_E_ASJ, false",
            "GNOMAD_E_FIN, false",
            "GNOMAD_E_OTH, false",
            "GNOMAD_G_AMI, false",
            "GNOMAD_G_MID, false",
            "GNOMAD_E_AFR, true",
            "GNOMAD_G_AMR, true",
            "GNOMAD_G_NFE, true",
    })
    void testAssignsBA1(FrequencySource frequencySource, boolean expected) {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                // high allele freq IN A GNOMAD NON-FOUNDER POP - triggers BA1 assignment
                .frequencyData(FrequencyData.of(Frequency.of(frequencySource, 5.0f)))
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(), List.of());
        assertThat(acmgEvidence.hasCriterion(AcmgCriterion.BA1), equalTo(expected));
    }

    @Test
    void testDoesntAssignBA1ForException() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
        // NM_004004.6:c.109G>A  https://www.ncbi.nlm.nih.gov/clinvar/variation/17023/
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(3, 128598490, "C", "CTAAG")
                .geneSymbol("GJB2")
                // high allele freq - triggers BA1 assignment but this is an exception
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.GNOMAD_G_EAS, 9.444445f)))
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        // Causes non-syndromic hearing loss (ClinVar 3* reviewed by expert panel)
        Disease deafness = Disease.builder().diseaseId("OMIM:220290").diseaseName("DEAFNESS, AUTOSOMAL RECESSIVE 1A; DFNB1A")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_RECESSIVE).diseaseType(Disease.DiseaseType.DISEASE).build();

        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_RECESSIVE, List.of(variantEvaluation), List.of(deafness), List.of());
        assertThat(acmgEvidence, equalTo(AcmgEvidence.empty()));
    }

    @Test
    void testAssignsBS4() {
        Individual proband = Individual.builder().id("proband").motherId("mother").fatherId("father").sex(MALE).status(Status.AFFECTED).build();
        Individual mother = Individual.builder().id("mother").sex(FEMALE).status(Status.AFFECTED).build();
        Individual father = Individual.builder().id("father").sex(MALE).status(Status.UNAFFECTED).build();
        Pedigree pedigree = Pedigree.of(proband, mother, father);
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", pedigree);
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                // n.b. PTEN is a haploinsufficient gene
                .geneSymbol("PTEN")
                // n.b. has frequency data - will not trigger PM2
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f)))
                // n.b. missense variant - will not trigger PVS1
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .sampleGenotypes(SampleGenotypes.of(
                        SampleData.of("proband", SampleGenotype.het()),
                        SampleData.of("mother", SampleGenotype.homRef()), // Affected mother has different genotype - can't be PS2
                        SampleData.of("father", SampleGenotype.homRef())
                ))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        // n.b. low phenotype score - will not trigger PP4
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), compatibleDiseaseMatches);

        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(BS4).build()));
    }

    @Nested
    class SplicingEvidence {

        @ParameterizedTest
        @CsvSource(value = {
                "SPLICE_ACCEPTOR_VARIANT, VERY_STRONG, '', -",
                "SPLICE_ACCEPTOR_VARIANT, VERY_STRONG, SPLICE_ACCEPTOR_VARIANT|PATHOGENIC, SUPPORTING",
                "SPLICE_ACCEPTOR_VARIANT, VERY_STRONG, SPLICE_REGION_VARIANT|PATHOGENIC, -",
                "SPLICE_ACCEPTOR_VARIANT, VERY_STRONG, SPLICE_REGION_VARIANT|LIKELY_PATHOGENIC, SUPPORTING",
                "SPLICE_ACCEPTOR_VARIANT, VERY_STRONG, SPLICE_REGION_VARIANT|LIKELY_PATHOGENIC, SUPPORTING",
                "SPLICE_ACCEPTOR_VARIANT, VERY_STRONG, SPLICE_REGION_VARIANT|UNCERTAIN_SIGNIFICANCE:SPLICE_REGION_VARIANT|LIKELY_PATHOGENIC, SUPPORTING",

                "SPLICE_ACCEPTOR_VARIANT, STRONG, SPLICE_ACCEPTOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_ACCEPTOR_VARIANT, MODERATE, SPLICE_ACCEPTOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_ACCEPTOR_VARIANT, SUPPORTING, SPLICE_ACCEPTOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_DONOR_VARIANT, STRONG, SPLICE_ACCEPTOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_DONOR_VARIANT, MODERATE, SPLICE_ACCEPTOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_DONOR_VARIANT, SUPPORTING, SPLICE_ACCEPTOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_ACCEPTOR_VARIANT, STRONG, SPLICE_DONOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_ACCEPTOR_VARIANT, MODERATE, SPLICE_DONOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_ACCEPTOR_VARIANT, SUPPORTING, SPLICE_DONOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_DONOR_VARIANT, STRONG, SPLICE_DONOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_DONOR_VARIANT, MODERATE, SPLICE_DONOR_VARIANT|PATHOGENIC, STRONG",
                "SPLICE_DONOR_VARIANT, SUPPORTING, SPLICE_DONOR_VARIANT|PATHOGENIC, STRONG",

                "SPLICE_DONOR_VARIANT, STRONG, SPLICE_REGION_VARIANT|PATHOGENIC, MODERATE",
                "SPLICE_DONOR_VARIANT, MODERATE, SPLICE_REGION_VARIANT|PATHOGENIC, MODERATE",
                "SPLICE_DONOR_VARIANT, SUPPORTING, SPLICE_REGION_VARIANT|PATHOGENIC, MODERATE",

                "SPLICE_DONOR_VARIANT, STRONG, SPLICE_REGION_VARIANT|LIKELY_PATHOGENIC, SUPPORTING",
                "SPLICE_DONOR_VARIANT, MODERATE, SPLICE_REGION_VARIANT|LIKELY_PATHOGENIC, SUPPORTING",
                "SPLICE_DONOR_VARIANT, SUPPORTING, SPLICE_REGION_VARIANT|LIKELY_PATHOGENIC, SUPPORTING",

                "SPLICE_DONOR_VARIANT, VERY_STRONG, SPLICE_REGION_VARIANT|UNCERTAIN_SIGNIFICANCE, -",
                "SPLICE_DONOR_VARIANT, VERY_STRONG, MISSENSE_VARIANT|PATHOGENIC, -",
                "SPLICE_DONOR_VARIANT, STRONG, SPLICE_REGION_VARIANT|UNCERTAIN_SIGNIFICANCE, -",
                "SPLICE_DONOR_VARIANT, MODERATE, SPLICE_REGION_VARIANT|UNCERTAIN_SIGNIFICANCE, -",
                "SPLICE_DONOR_VARIANT, SUPPORTING, SPLICE_REGION_VARIANT|UNCERTAIN_SIGNIFICANCE, -",
        }, nullValues = {"-"})
        void testAssignSpliceAcceptorDonorPS1(VariantEffect variantEffect, Evidence pvs1Evidence, String clinVarVariants, Evidence ps1Evidence) {
            // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
            // requires variant to be on a transcript predicted to undergo NMD in a LoF-intolerant gene for full PVS1

            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89693009, "G", "C")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .variantEffect(variantEffect)
//                    .annotations(List.of(transcriptAnnotation))
                    .build();
            Map<Variant, ClinVarData> expectedClinVarData = parseExpectedClinvarData(variantEvaluation, clinVarVariants);
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();

            VariantDataService testVariantDataService = TestVariantDataService.builder().expectedClinVarData(expectedClinVarData).build();
            AcmgEvidence.Builder acmgEvidenceBuilder = AcmgEvidence.builder()
                    .add(PVS1, pvs1Evidence);
            AcmgSpliceEvidenceAssigner.assignSpliceEvidence(acmgEvidenceBuilder, variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(cowdenSyndrome), testVariantDataService);

            AcmgEvidence.Builder expected = AcmgEvidence.builder().add(PVS1, pvs1Evidence);
            if (ps1Evidence != null) {
                expected.add(PS1, ps1Evidence);
            }
            assertThat(acmgEvidenceBuilder.build(), equalTo(expected.build()));
        }

        @ParameterizedTest
        @CsvSource(value = {
                "SPLICE_REGION_VARIANT, 0.0, '', BP4",
                "SPLICE_REGION_VARIANT, 0.09, '', BP4",
                "SPLICE_REGION_VARIANT, 0.1, '', ",
                "SPLICE_REGION_VARIANT, 0.2, SPLICE_ACCEPTOR_VARIANT|PATHOGENIC, PP3",
                "SPLICE_REGION_VARIANT, 0.2, SPLICE_REGION_VARIANT|PATHOGENIC, PP3 PS1_Strong",
                "SPLICE_REGION_VARIANT, 0.2, SPLICE_REGION_VARIANT|LIKELY_PATHOGENIC, PP3 PS1_Moderate",
                // different variant location
                "SPLICE_REGION_VARIANT, 0.2, SPLICE_REGION_VARIANT|PATHOGENIC, PP3 PS1_Moderate",
                "SPLICE_REGION_VARIANT, 0.2, SPLICE_REGION_VARIANT|LIKELY_PATHOGENIC, PP3 PS1_Supporting",

        })
        void testAssignNonDonorAcceptorSpliceRegionPS1(VariantEffect variantEffect, float spliceAiScore, String clinVarVariants, String acmgEvidence) {
            // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
            // requires variant to be on a transcript predicted to undergo NMD in a LoF-intolerant gene for full PVS1

            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89693009, "G", "C")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .pathogenicityData(PathogenicityData.of(PathogenicityScore.of(PathogenicitySource.SPLICE_AI, spliceAiScore)))
                    .variantEffect(variantEffect)
                    .build();
            Map<Variant, ClinVarData> expectedClinVarData = parseExpectedClinvarData(variantEvaluation, clinVarVariants);
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();

            VariantDataService testVariantDataService = TestVariantDataService.builder().expectedClinVarData(expectedClinVarData).build();
            AcmgEvidence.Builder acmgEvidenceBuilder = AcmgEvidence.builder();
            AcmgSpliceEvidenceAssigner.assignSpliceEvidence(acmgEvidenceBuilder, variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(cowdenSyndrome), testVariantDataService);

            AcmgEvidence expected = AcmgEvidence.parseAcmgEvidence(acmgEvidence);
            assertThat(acmgEvidenceBuilder.build(), equalTo(expected));
        }

        @ParameterizedTest
        @CsvSource(value = {
                "INTRON_VARIANT, c.300+7A>C, 0.7, ",
                "SYNONYMOUS_VARIANT, c.300A>C, 0.3, ",
                "SPLICE_REGION_VARIANT, c.300+7A>C, 0.8, PP3",
                "SPLICE_REGION_VARIANT, c.300+7A>C, 0.1, ",
                "SPLICE_REGION_VARIANT, c.300+6A>C, 0.0, BP4",
                "SPLICE_REGION_VARIANT, c.300+7A>C, 0.0, BP4 BP7",
                "SPLICE_REGION_VARIANT, c.400-20A>C, 0.0, BP4",
                "SPLICE_REGION_VARIANT, c.400-21A>C, 0.0, BP4 BP7",
                "SPLICE_REGION_VARIANT, c.400-22A>C, 0.0, BP4 BP7",
        })
        void testSilentAndIntronicBP7(VariantEffect variantEffect, String hgvsc, float spliceAiScore, String acmgEvidence) {
            TranscriptAnnotation transcriptAnnotation = TranscriptAnnotation.builder()
                    .variantEffect(variantEffect)
                    .hgvsCdna(hgvsc)
                    .build();
            VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                    .geneSymbol("PTEN")
                    .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                    .pathogenicityData(PathogenicityData.of(PathogenicityScore.of(PathogenicitySource.SPLICE_AI, spliceAiScore)))
                    .variantEffect(variantEffect)
                    .annotations(List.of(transcriptAnnotation))
                    .build();
            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            AcmgEvidence.Builder acmgEvidenceBuilder = AcmgEvidence.builder();
            AcmgSpliceEvidenceAssigner.assignSpliceEvidence(acmgEvidenceBuilder, variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(cowdenSyndrome), TestVariantDataService.stub());

            AcmgEvidence expected = AcmgEvidence.parseAcmgEvidence(acmgEvidence);
            assertThat(acmgEvidenceBuilder.build(), equalTo(expected));
        }

        private Map<Variant, ClinVarData> parseExpectedClinvarData(VariantEvaluation variantEvaluation, String clinVarVariants) {
            // SPLICE_ACCEPTOR_VARIANT|PATHOGENIC:SPLICE_REGION_VARIANT|LIKELY_PATHOGENIC:MISSENSE_VARIANT|UNKNOWN_SIGNIFICANCE
            if (clinVarVariants.isEmpty()) {
                return Map.of();
            }
            Map<Variant, ClinVarData> clinVarData = new LinkedHashMap<>();
            String[] clinVars = clinVarVariants.split(":");
            for (int i = 0; i < clinVars.length; i++) {
                String[] clinVarInfo = clinVars[i].split("\\|");
                Variant variant = VariantEvaluation.builder().variant(variantEvaluation.contig(), variantEvaluation.strand(), variantEvaluation.coordinates().extend(+i, -i), "A", "T").build();
                ClinVarData clinVar = ClinVarData.builder()
                        .variantEffect(VariantEffect.valueOf(clinVarInfo[0]))
                        .primaryInterpretation(ClinVarData.ClinSig.valueOf(clinVarInfo[1]))
                        .build();
                clinVarData.put(variant, clinVar);
            }
            return clinVarData;
        }

    }
}