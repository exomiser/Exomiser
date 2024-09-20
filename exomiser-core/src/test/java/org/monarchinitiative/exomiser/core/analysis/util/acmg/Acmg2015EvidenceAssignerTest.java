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
import org.monarchinitiative.exomiser.core.genome.TestGenomeDataService;
import org.monarchinitiative.exomiser.core.genome.TestVariantDataService;
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

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.*;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.FEMALE;
import static org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex.MALE;
import static org.monarchinitiative.exomiser.core.model.Pedigree.justProband;

class Acmg2015EvidenceAssignerTest {


    private Acmg2015EvidenceAssigner acmgEvidenceAssigner(String probandId, Pedigree pedigree) {
        return new Acmg2015EvidenceAssigner(probandId, pedigree, TestVariantDataService.stub());
    }

    @Test
    void throwsExceptionWithMismatchedIds() {
        assertThrows(IllegalArgumentException.class, () -> acmgEvidenceAssigner("Zaphod", justProband("Ford", MALE)));
    }
    
    @Test
    void testAssignsPVS1() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
        // https://www.ncbi.nlm.nih.gov/clinvar/variation/484600/ 3* PATHOGENIC variant  - reviewed by expert panel
        // requires variant to be on a transcript predicted to undergo NMD in a LoF-intolerant gene for full PVS1
        TranscriptAnnotation transcriptAnnotation = TranscriptAnnotation.builder()
                .variantEffect(VariantEffect.START_LOST)
                .rankType(TranscriptAnnotation.RankType.EXON)
                .rank(1)
                .rankTotal(5)
                .build();
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(VariantEffect.START_LOST)
                .annotations(List.of(transcriptAnnotation))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), List.of());
        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PVS1).build()));
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
                .variantEffect(VariantEffect.START_LOST)
                .rankType(TranscriptAnnotation.RankType.EXON)
                .rank(1)
                .rankTotal(5)
                .build();
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(VariantEffect.START_LOST)
                .annotations(List.of(transcriptAnnotation))
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(diseaseInheritanceMode).diseaseType(Disease.DiseaseType.DISEASE).build();
        List<Disease> knownDiseases = diseaseInheritanceMode.isCompatibleWith(modeOfInheritance) ? List.of(cowdenSyndrome) : List.of();
        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, modeOfInheritance, List.of(variantEvaluation), knownDiseases, List.of());
        AcmgEvidence expected = expectPvs1 ? AcmgEvidence.builder().add(PVS1).build() : AcmgEvidence.empty();
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
                .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC).reviewStatus(ClinVarData.ReviewStatus.REVIEWED_BY_EXPERT_PANEL).build()))
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
                .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC).reviewStatus(ClinVarData.ReviewStatus.REVIEWED_BY_EXPERT_PANEL).build()))
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
                .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC).reviewStatus(ClinVarData.ReviewStatus.REVIEWED_BY_EXPERT_PANEL).build()))
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
        @CsvSource ({
                "MVP, 1.0f, , ",
                "REVEL, 1.0f, PP3, STRONG"
        })
        void testAssignsPP3_singleScoreIsInsufficientUnlessItsRevel(PathogenicitySource pathogenicitySource, float pathogenicityScore, AcmgCriterion acmgCriterion, Evidence evidence) {
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

        @ParameterizedTest
        @CsvSource ({
            "CADD, 0.0f, , ",
            "REVEL, 0.0f, BP4, VERY_STRONG"
        })
        void testAssignsBP4_singleScoreIsInsufficientIfNotRevel(PathogenicitySource pathogenicitySource, float pathogenicityScore, AcmgCriterion acmgCriterion, Evidence evidence) {
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
        @CsvSource ({
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
    @Test
    void testAssignsPP4() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 0.1f))) // prevent PM2 assignment
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
        // High phenotype match triggers - PP4
        List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.6, cowdenSyndrome, List.of()));

        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), compatibleDiseaseMatches);
        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.PP4).build()));
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
                    .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC).reviewStatus(ClinVarData.ReviewStatus.parseReviewStatus(reviewStatus)).build()))
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
                        "reviewed by expert panel; STRONG",
                        "practice guideline; STRONG",
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
                    .pathogenicityData(PathogenicityData.of(ClinVarData.builder().primaryInterpretation(ClinVarData.ClinSig.BENIGN).reviewStatus(ClinVarData.ReviewStatus.parseReviewStatus(reviewStatus)).build()))
                    .build();

            Disease cowdenSyndrome = Disease.builder().diseaseId("OMIM:158350").diseaseName("COWDEN SYNDROME 1; CWS1").inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT).diseaseType(Disease.DiseaseType.DISEASE).build();
            // n.b. low phenotype score - will not trigger PP4
            List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches = List.of(ModelPhenotypeMatch.of(0.5, cowdenSyndrome, List.of()));
            AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(cowdenSyndrome), compatibleDiseaseMatches);

            assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(BP6, evidence).build()));
        }
    }

    @Test
    void testAssignsBA1() {
        Acmg2015EvidenceAssigner instance = acmgEvidenceAssigner("proband", justProband("proband", MALE));
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(10, 89624227, "A", "G")
                .geneSymbol("PTEN")
                // high allele freq - triggers BA1 assignment
                .frequencyData(FrequencyData.of(Frequency.of(FrequencySource.EXAC_AMERICAN, 5.0f)))
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AcmgEvidence acmgEvidence = instance.assignVariantAcmgEvidence(variantEvaluation, ModeOfInheritance.AUTOSOMAL_DOMINANT, List.of(variantEvaluation), List.of(), List.of());
        assertThat(acmgEvidence, equalTo(AcmgEvidence.builder().add(AcmgCriterion.BA1).build()));
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
}