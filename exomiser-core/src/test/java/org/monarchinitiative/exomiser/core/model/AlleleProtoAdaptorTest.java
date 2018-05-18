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

package org.monarchinitiative.exomiser.core.model;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.AlleleProperties;
import org.monarchinitiative.exomiser.core.proto.AlleleProto.ClinVar;

import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleProtoAdaptorTest {

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
        AlleleProperties alleleProperties = AlleleProperties.newBuilder().setClinVar(clinVar).build();


        ClinVarData expected = ClinVarData.builder()
                .alleleId("12345")
                .primaryInterpretation(ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .secondaryInterpretations(EnumSet.of(
                        ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC,
                        ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE,
                        ClinVarData.ClinSig.BENIGN_OR_LIKELY_BENIGN,
                        ClinVarData.ClinSig.BENIGN))
                .includedAlleles(ImmutableMap.of("54321", ClinVarData.ClinSig.ASSOCIATION))
                .reviewStatus("conflicting evidence")
                .build();
        assertThat(AlleleProtoAdaptor.toPathogenicityData(alleleProperties), equalTo(PathogenicityData.of(expected)));
    }
}