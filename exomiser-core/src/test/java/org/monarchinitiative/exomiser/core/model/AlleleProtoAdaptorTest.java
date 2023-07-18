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
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.KG, 4, 1000))
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.TOPMED, 200, 400000))
                .build();
        assertThat(AlleleProtoAdaptor.toFrequencyData(alleleProperties),
                equalTo(FrequencyData.of(
                    Frequency.of(FrequencySource.THOUSAND_GENOMES, 0.4f),
                    Frequency.of(FrequencySource.TOPMED, 0.05f))
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
    public void testToPathDataMcap() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.M_CAP, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(M_CAP, 0.7f))));
    }

    @Test
    public void testToPathDataMpc() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.MPC, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(MPC, 0.7f))));
    }

    @Test
    public void testToPathDataMvp() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.MVP, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(MVP, 0.7f))));
    }

    @Test
    public void testToPathDataPrimateAi() {
        AlleleProperties alleleProperties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.PRIMATE_AI, 0.7f))
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(PathogenicityScore.of(PRIMATE_AI, 0.7f))));
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
                .setAlleleId("12345")
                .setPrimaryInterpretation(ClinVar.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .addSecondaryInterpretations(ClinVar.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC)
                .addSecondaryInterpretations(ClinVar.ClinSig.UNCERTAIN_SIGNIFICANCE)
                .addSecondaryInterpretations(ClinVar.ClinSig.BENIGN_OR_LIKELY_BENIGN)
                .addSecondaryInterpretations(ClinVar.ClinSig.BENIGN)
                .putIncludedAlleles("54321", ClinVar.ClinSig.ASSOCIATION)
                .setReviewStatus("conflicting evidence")
                .build();

        ClinVarData expected = ClinVarData.builder()
                .alleleId("12345")
                .primaryInterpretation(ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .secondaryInterpretations(EnumSet.of(
                        ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC,
                        ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE,
                        ClinVarData.ClinSig.BENIGN_OR_LIKELY_BENIGN,
                        ClinVarData.ClinSig.BENIGN))
                .includedAlleles(Map.of("54321", ClinVarData.ClinSig.ASSOCIATION))
                .reviewStatus("conflicting evidence")
                .build();

        assertThat(AlleleProtoAdaptor.toClinVarData(clinVar), equalTo(expected));

        AlleleProperties alleleProperties = AlleleProperties.newBuilder().setClinVar(clinVar).build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(expected)));
    }
}