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

package org.monarchinitiative.exomiser.core.genome.dao;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class PathogenicityDaoMvStoreTest extends AllelePropertiesDaoAdapterTest {

    private VariantEvaluation.Builder variantBuilder() {
        return TestFactory.variantBuilder(1, 12345, "A", "T");
    }

    @Test
    public void wrongMapName() throws Exception {
        Variant variant = variantBuilder().build();
        PathogenicityDao instance = newInstanceWithData(Map.of());
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataNoData() throws Exception {
        Variant variant = variantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        PathogenicityDao instance = newInstanceWithData(Map.of());
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataNonMissenseVariant() throws Exception {
        Variant frameShiftVariant = variantBuilder()
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .build();
        PathogenicityDao instance = newInstanceWithData(Map.of());
        assertThat(instance.getPathogenicityData(frameShiftVariant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataNoInfo() throws Exception {
        Variant variant = variantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.getDefaultInstance();

        PathogenicityDao instance = newInstanceWithData(Map.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataNonPathogenicityInfo() throws Exception {
        Variant variant = variantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder()
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.KG, 1, 40000))
                .build();

        PathogenicityDao instance = newInstanceWithData(Map.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.empty()));
    }

    @Test
    public void getPathogenicityDataJustSift() throws Exception {
        Variant variant = variantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder()
                .addFrequencies(AlleleData.frequencyOf(AlleleProto.FrequencySource.KG, 1, 40000))
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.SIFT, 0f))
                .build();

        PathogenicityDao instance = newInstanceWithData(Map.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.of(SiftScore.of(0f))));
    }

    @Test
    public void getPathogenicityDataJustPolyphen() throws Exception {
        Variant variant = variantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.POLYPHEN, 1.0f))
                .build();

        PathogenicityDao instance = newInstanceWithData(Map.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.of(PolyPhenScore.of(1f))));
    }

    @Test
    public void getPathogenicityDataJustMutationTaster() throws Exception {
        Variant variant = variantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        AlleleProperties properties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.MUTATION_TASTER, 1.0f))
                .build();

        PathogenicityDao instance = newInstanceWithData(Map.of(key, properties));
        assertThat(instance.getPathogenicityData(variant), equalTo(PathogenicityData.of(MutationTasterScore.of(1f))));
    }

    @Test
    public void getPathogenicityDataJustClinVar() {
        Variant variant = variantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        ClinVar clinVar = ClinVar.newBuilder().setVariationId("54321").setPrimaryInterpretation(ClinVar.ClinSig.ASSOCIATION).build();
        AlleleProperties properties = AlleleProperties.newBuilder()
                .setClinVar(clinVar)
                .build();

        PathogenicityDao instance = newInstanceWithData(Map.of(key, properties));
        PathogenicityData expected = PathogenicityData.of(ClinVarData.builder()
                .variationId("54321")
                .primaryInterpretation(ClinVarData.ClinSig.ASSOCIATION)
                .build());
        assertThat(instance.getPathogenicityData(variant), equalTo(expected));
    }

    @Test
    public void getPathogenicityDataAll() throws Exception {
        Variant variant = variantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        AlleleKey key = AlleleProtoAdaptor.toAlleleKey(variant);
        ClinVar clinVar = ClinVar.newBuilder()
                .setVariationId("54321")
                .setPrimaryInterpretation(ClinVar.ClinSig.PATHOGENIC)
                .build();
        AlleleProperties properties = AlleleProperties.newBuilder()
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.POLYPHEN, 1.0f))
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.MUTATION_TASTER, 1.0f))
                .addPathogenicityScores(AlleleData.pathogenicityScoreOf(AlleleProto.PathogenicitySource.SIFT, 0.0f))
                .setClinVar(clinVar)
                .build();

        PathogenicityDao instance = newInstanceWithData(Map.of(key, properties));

        PathogenicityData expected = PathogenicityData.of(ClinVarData.builder()
                .variationId("54321")
                .primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC)
                .build(),
                SiftScore.of(0f), PolyPhenScore.of(1f), MutationTasterScore.of(1f));

        assertThat(instance.getPathogenicityData(variant), equalTo(expected));
    }
}