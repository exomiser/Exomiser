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

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleKey;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;

import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleConverterTest {

    private static final ClinVarData CLINVAR_DATA = ClinVarData.builder()
            .alleleId("12334")
            .primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC)
            .secondaryInterpretations(EnumSet.of(ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE))
            .reviewStatus("yada-yada")
            .includedAlleles(ImmutableMap.of("455645", ClinVarData.ClinSig.LIKELY_PATHOGENIC))
            .build();

    private static final Allele ALLELE = makeAllele();

    private static final AlleleKey ALLELE_KEY = AlleleKey.newBuilder()
            .setChr(1)
            .setPosition(2345)
            .setRef("A")
            .setAlt("C")
            .build();

    private static final AlleleProperties.ClinVar PROTO_CLINVAR = AlleleProperties.ClinVar.newBuilder()
            .setAlleleId("12334")
            .setPrimaryInterpretation(AlleleProperties.ClinVar.ClinSig.PATHOGENIC)
            .addSecondaryInterpretations(AlleleProperties.ClinVar.ClinSig.UNCERTAIN_SIGNIFICANCE)
            .addSecondaryInterpretations(AlleleProperties.ClinVar.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
            .setReviewStatus("yada-yada")
            .putIncludedAlleles("455645", AlleleProperties.ClinVar.ClinSig.LIKELY_PATHOGENIC)
            .build();

    private static final AlleleProperties ALLELE_PROPERTIES = AlleleProperties.newBuilder()
            .setRsId("rs678910")
            .putProperties("EXAC_AFR", 0.00056f)
            .setClinVar(PROTO_CLINVAR)
            .build();

    private static Allele makeAllele() {
        Allele allele = new Allele(1, 2345, "A", "C");
        allele.setRsId("rs678910");
        allele.getValues().put(AlleleProperty.EXAC_AFR, 0.00056f);
        allele.setClinVarData(CLINVAR_DATA);
        return allele;
    }

    @Test
    public void convertKey() {
        assertThat(AlleleConverter.toAlleleKey(ALLELE), equalTo(ALLELE_KEY));
    }

    @Test
    public void convertProperties() {
        assertThat(AlleleConverter.toAlleleProperties(ALLELE), equalTo(ALLELE_PROPERTIES));
    }

    @Test
    public void mergeProperties() {
        AlleleProperties toMerge = AlleleProperties.newBuilder().putProperties("POLYPHEN", 1f).build();

        AlleleProperties expected = AlleleProperties.newBuilder()
                .setRsId("rs678910")
                .putProperties("EXAC_AFR", 0.00056f)
                .putProperties("POLYPHEN", 1f)
                .setClinVar(PROTO_CLINVAR)
                .build();

        assertThat(AlleleConverter.mergeProperties(ALLELE_PROPERTIES, toMerge), equalTo(expected));
    }

    @Test
    public void mergePropertiesUsesOriginalRsidWhenPresent() {
        AlleleProperties toMerge = AlleleProperties.newBuilder()
                .setRsId("Shouldn't be present in merged")
                .putProperties("POLYPHEN", 1f).build();

        AlleleProperties expected = AlleleProperties.newBuilder()
                .setRsId("rs678910")
                .putProperties("EXAC_AFR", 0.00056f)
                .putProperties("POLYPHEN", 1f)
                .setClinVar(PROTO_CLINVAR)
                .build();

        assertThat(AlleleConverter.mergeProperties(ALLELE_PROPERTIES, toMerge), equalTo(expected));
    }

    @Test
    public void mergePropertiesUsesNewRsidIfOriginalAbsent() {

        AlleleProperties original = AlleleProperties.newBuilder()
                .putProperties("EXAC_AFR", 0.00056f)
                .build();

        AlleleProperties toMerge = AlleleProperties.newBuilder()
                .setRsId("rs45789")
                .putProperties("POLYPHEN", 1f).build();

        AlleleProperties expected = AlleleProperties.newBuilder()
                .setRsId("rs45789")
                .putProperties("EXAC_AFR", 0.00056f)
                .putProperties("POLYPHEN", 1f)
                .build();

        assertThat(AlleleConverter.mergeProperties(original, toMerge), equalTo(expected));
    }

    @Test
    public void convertClinVar() {
        assertThat(AlleleConverter.toProtoClinVar(CLINVAR_DATA), equalTo(PROTO_CLINVAR));
    }
}