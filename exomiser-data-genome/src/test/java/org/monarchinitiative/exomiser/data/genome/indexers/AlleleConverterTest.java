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

package org.monarchinitiative.exomiser.data.genome.indexers;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.EnumSet;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleConverterTest {

    private static final AlleleKey ALLELE_KEY = AlleleKey.newBuilder()
            .setChr(1)
            .setPosition(2345)
            .setRef("A")
            .setAlt("C")
            .build();

    private static final ClinVarData CLINVAR_DATA = ClinVarData.builder()
            .variationId("12334")
            .primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC)
            .secondaryInterpretations(EnumSet.of(ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE))
            .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
            .includedAlleles(Map.of("455645", ClinVarData.ClinSig.LIKELY_PATHOGENIC))
            .build();

    private static final ClinVar PROTO_CLINVAR = ClinVar.newBuilder()
            .setVariationId("12334")
            .setPrimaryInterpretation(ClinVar.ClinSig.PATHOGENIC)
            .addSecondaryInterpretations(ClinVar.ClinSig.UNCERTAIN_SIGNIFICANCE)
            .addSecondaryInterpretations(ClinVar.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
            .setReviewStatus(ClinVar.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
            .putIncludedAlleles("455645", ClinVar.ClinSig.LIKELY_PATHOGENIC)
            .build();

    private static final AlleleProto.Frequency GNOMAD_E_AFR_FREQUENCY = AlleleProto.Frequency.newBuilder()
            .setFrequencySource(AlleleProto.FrequencySource.GNOMAD_E_AFR)
            .setAc(1)
            .setAn(50000)
            .build();

    private static final AlleleProto.PathogenicityScore POLYPHEN_SCORE = AlleleProto.PathogenicityScore.newBuilder()
            .setPathogenicitySource(AlleleProto.PathogenicitySource.POLYPHEN)
            .setScore(1f)
            .build();

    private static final AlleleProperties ALLELE_PROPERTIES = AlleleProperties.newBuilder()
            .setRsId("rs678910")
            .addFrequencies(GNOMAD_E_AFR_FREQUENCY)
            .setClinVar(PROTO_CLINVAR)
            .build();

    private static Allele makeAllele() {
        Allele allele = new Allele(1, 2345, "A", "C");
        allele.setRsId("rs678910");
        allele.addFrequency(GNOMAD_E_AFR_FREQUENCY);
        allele.addPathogenicityScore(POLYPHEN_SCORE);
        allele.setClinVarData(CLINVAR_DATA);
        return allele;
    }

    private static final Allele ALLELE = makeAllele();

    @Test
    public void convertKey() {
        assertThat(AlleleConverter.toAlleleKey(ALLELE), equalTo(ALLELE_KEY));
    }

    @Test
    public void convertProperties() {
        AlleleProperties expected = ALLELE_PROPERTIES.toBuilder().addPathogenicityScores(POLYPHEN_SCORE).build();
        assertThat(AlleleConverter.toAlleleProperties(ALLELE), equalTo(expected));
    }

    @Test
    public void mergeProperties() {
        AlleleProperties toMerge = AlleleProperties.newBuilder()
                .addPathogenicityScores(POLYPHEN_SCORE)
                .build();

        AlleleProperties expected = AlleleProperties.newBuilder()
                .setRsId("rs678910")
                .addFrequencies(GNOMAD_E_AFR_FREQUENCY)
                .addPathogenicityScores(POLYPHEN_SCORE)
                .setClinVar(PROTO_CLINVAR)
                .build();

        assertThat(AlleleConverter.mergeProperties(ALLELE_PROPERTIES, toMerge), equalTo(expected));
    }

    @Test
    public void mergePropertiesDoesntDuplicateFrequencies() {
        AlleleProperties toMerge = AlleleProperties.newBuilder()
                .addFrequencies(GNOMAD_E_AFR_FREQUENCY)
                .build();

        AlleleProperties expected = AlleleProperties.newBuilder()
                .setRsId("rs678910")
                .addFrequencies(GNOMAD_E_AFR_FREQUENCY)
                .setClinVar(PROTO_CLINVAR)
                .build();

        assertThat(AlleleConverter.mergeProperties(ALLELE_PROPERTIES, toMerge), equalTo(expected));
    }

    @Test
    public void mergePropertiesIncludesNonDuplicatedPathScores() {
        AlleleProperties original = ALLELE_PROPERTIES.toBuilder()
                .addPathogenicityScores(POLYPHEN_SCORE)
                .build();

        AlleleProperties toMerge = AlleleProperties.newBuilder()
                .addPathogenicityScores(POLYPHEN_SCORE)
                .build();

        AlleleProperties expected = AlleleProperties.newBuilder()
                .setRsId("rs678910")
                .addFrequencies(GNOMAD_E_AFR_FREQUENCY)
                .addPathogenicityScores(POLYPHEN_SCORE)
                .setClinVar(PROTO_CLINVAR)
                .build();

        assertThat(AlleleConverter.mergeProperties(original, toMerge), equalTo(expected));
    }

    @Test
    public void mergePropertiesUsesOriginalRsidWhenPresent() {
        AlleleProperties toMerge = AlleleProperties.newBuilder()
                .setRsId("Shouldn't be present in merged")
                .addPathogenicityScores(POLYPHEN_SCORE)
                .build();

        AlleleProperties expected = AlleleProperties.newBuilder()
                .setRsId("rs678910")
                .addFrequencies(GNOMAD_E_AFR_FREQUENCY)
                .addPathogenicityScores(POLYPHEN_SCORE)
                .setClinVar(PROTO_CLINVAR)
                .build();

        assertThat(AlleleConverter.mergeProperties(ALLELE_PROPERTIES, toMerge), equalTo(expected));
    }

    @Test
    public void mergePropertiesUsesNewRsidIfOriginalAbsent() {

        AlleleProperties original = AlleleProperties.newBuilder()
                .addFrequencies(GNOMAD_E_AFR_FREQUENCY)
                .build();

        AlleleProperties toMerge = AlleleProperties.newBuilder()
                .setRsId("rs45789")
                .addPathogenicityScores(POLYPHEN_SCORE)
                .build();

        AlleleProperties expected = AlleleProperties.newBuilder()
                .setRsId("rs45789")
                .addFrequencies(GNOMAD_E_AFR_FREQUENCY)
                .addPathogenicityScores(POLYPHEN_SCORE)
                .build();

        assertThat(AlleleConverter.mergeProperties(original, toMerge), equalTo(expected));
    }

    @Test
    public void convertClinVar() {
        assertThat(AlleleConverter.toProtoClinVar(CLINVAR_DATA), equalTo(PROTO_CLINVAR));
    }

    @Test
    public void convertClinVarConflictingInterpretationCounts() {
        ClinVarData clinVarData = ClinVarData.builder()
                .conflictingInterpretationCounts(Map.of(ClinVarData.ClinSig.PATHOGENIC, 3, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE, 2))
                .build();

        ClinVar expected = ClinVar.newBuilder()
                .putAllClinSigCounts(Map.of("PATHOGENIC", 3, "UNCERTAIN_SIGNIFICANCE", 2))
                .build();

        assertThat(AlleleConverter.toProtoClinVar(clinVarData), equalTo(expected));
    }
}