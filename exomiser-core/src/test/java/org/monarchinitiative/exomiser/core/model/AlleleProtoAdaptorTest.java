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

package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;

import java.util.EnumSet;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.*;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleProtoAdaptorTest {

    @Test
    public void generateAlleleKey() {
        Variant variant = TestFactory.variantBuilder(1, 12345, "A", "T")
                .build();

        AlleleProto.AlleleKey expected = AlleleProto.AlleleKey.newBuilder()
                .setChr(1)
                .setPosition(12345)
                .setRef("A")
                .setAlt("T")
                .build();

        assertThat(AlleleProtoAdaptor.toAlleleKey(variant), equalTo(expected));
    }

    @Test
    public void testToFreqData() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                // 100f * (ac / (float) an);
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.GNOMAD_E_ASJ, 4, 1000))
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.GNOMAD_E_EAS, 200, 400000, 1))
                .build();
        assertThat(AlleleProtoAdaptor.toFrequencyData(alleleProperties),
                equalTo(FrequencyData.of(
                    Frequency.of(FrequencySource.GNOMAD_E_ASJ, 4, 1000, 0),
                    Frequency.of(FrequencySource.GNOMAD_E_EAS, 200, 400000, 1))
                )
        );
    }

    @Test
    public void testToFreqDataNoAnType() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                // 100f * (ac / (float) an);
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.KG, 0.001f))
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.TOPMED, 0.002f))
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.GNOMAD_E_EAS, 200, 400000, 1))
                .build();
        assertThat(AlleleProtoAdaptor.toFrequencyData(alleleProperties),
                equalTo(FrequencyData.of(
                        Frequency.of(FrequencySource.THOUSAND_GENOMES, 0.001f),
                        Frequency.of(FrequencySource.TOPMED, 0.002f),
                        Frequency.of(FrequencySource.GNOMAD_E_EAS, 200, 400000, 1))
                        )
        );
    }

    @Test
    public void testToPathDataSift() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.SIFT, 0.2f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(SIFT, 0.2f))));
    }

    @Test
    public void testToPathDataPolyphen() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.POLYPHEN, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(POLYPHEN, 0.7f))));
    }

    @Test
    public void testToPathDataMutationTaster() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.MUTATION_TASTER, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(MUTATION_TASTER, 0.7f))));
    }

    @Test
    public void testToPathDataRevel() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.REVEL, 0.2f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(REVEL, 0.2f))));
    }

    @Test
    public void testToPathDataMvp() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.MVP, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(MVP, 0.7f))));
    }

    @Test
    public void testToPathDataSpliceAi() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.SPLICE_AI, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(SPLICE_AI, 0.7f))));
    }

    @Test
    public void testToPathDataRemm() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.REMM, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(REMM, 0.7f))));
    }

    @Test
    public void testToPathDataCadd() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.CADD, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(CADD, 0.7f))));
    }

    @Test
    public void parseClinVarDataDefaultInstanceReturnsEmpty() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder().build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void parseClinVarData() {

        ClinVar clinVar = ClinVar.newBuilder()
                .setVariationId("12345")
                .setPrimaryInterpretation(ClinVar.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .addSecondaryInterpretations(ClinVar.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC)
                .addSecondaryInterpretations(ClinVar.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .addSecondaryInterpretations(ClinVar.ClinSig.BENIGN_OR_LIKELY_BENIGN)
                .addSecondaryInterpretations(ClinVar.ClinSig.BENIGN)
                .putIncludedAlleles("54321", ClinVar.ClinSig.ASSOCIATION)
                .setReviewStatus(ClinVar.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                .putAllClinSigCounts(Map.of(ClinVar.ClinSig.PATHOGENIC.toString(), 5, ClinVar.ClinSig.UNCERTAIN_SIGNIFICANCE.toString(), 1))
                .build();

        ClinVarData expected = ClinVarData.builder()
                .variationId("12345")
                .primaryInterpretation(ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .secondaryInterpretations(EnumSet.of(
                        ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC,
                        ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE,
                        ClinVarData.ClinSig.BENIGN_OR_LIKELY_BENIGN,
                        ClinVarData.ClinSig.BENIGN))
                .includedAlleles(Map.of("54321", ClinVarData.ClinSig.ASSOCIATION))
                .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                .conflictingInterpretationCounts(Map.of(ClinVarData.ClinSig.PATHOGENIC, 5, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE, 1))
                .build();

        assertThat(AlleleProtoAdaptor.toClinVarData(clinVar), equalTo(expected));

        AlleleProperties alleleProperties = AlleleProperties.newBuilder().setClinVar(clinVar).build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(expected)));
    }

    @Test
    public void parseClinVarAdditionalAnnotationData() {

        ClinVar clinVar = ClinVar.newBuilder()
                .setVariantEffect(AlleleProto.VariantEffect.MISSENSE_VARIANT)
                .setHgvsCdna("c.12345A>C")
                .setHgvsProtein("p.Arg123Lys")
                .build();

        ClinVarData actual = AlleleProtoAdaptor.toClinVarData(clinVar);
        assertThat(actual.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
        assertThat(actual.getHgvsCdna(), equalTo("c.12345A>C"));
        assertThat(actual.getHgvsProtein(), equalTo("p.Arg123Lys"));
    }

    @Test
    public void convertClinVarConflictingInterpretationCounts() {

        ClinVar clinVar = ClinVar.newBuilder()
                .putAllClinSigCounts(Map.of("PATHOGENIC", 3, "UNCERTAIN_SIGNIFICANCE", 2))
                .build();

        ClinVarData actual = AlleleProtoAdaptor.toClinVarData(clinVar);
        assertThat(actual.getConflictingInterpretationCounts(),
                equalTo(Map.of(ClinVarData.ClinSig.PATHOGENIC, 3, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE, 2)));
    }
}