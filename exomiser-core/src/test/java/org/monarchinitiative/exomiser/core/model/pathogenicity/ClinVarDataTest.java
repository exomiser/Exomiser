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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ClinSig;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ClinVarDataTest {

    @Test
    public void testEmptyBuilder() {
        ClinVarData instance = ClinVarData.builder().build();

        assertThat(instance.getAlleleId(), equalTo(null));
        assertThat(instance.getPrimaryInterpretation(), equalTo(ClinSig.NOT_PROVIDED));
        assertThat(instance.getSecondaryInterpretations(), equalTo(Collections.emptySet()));
        assertThat(instance.getReviewStatus(), equalTo(""));
        assertThat(instance.getIncludedAlleles(), equalTo(Collections.emptyMap()));
    }

    @Test
    public void testBuilderWithValues() {
        String alleleId = "12345";
        ClinSig clinSig = ClinSig.PATHOGENIC;
        Set<ClinSig> secondaryInterpretations = EnumSet.of(ClinSig.RISK_FACTOR, ClinSig.ASSOCIATION);
        String reviewStatus = "multiple_submitters,_no_conflict";
        Map<String, ClinSig> included = ImmutableMap.of("54321", ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC);
        ClinVarData instance = ClinVarData.builder()
                .alleleId(alleleId)
                .primaryInterpretation(clinSig)
                .secondaryInterpretations(secondaryInterpretations)
                .reviewStatus(reviewStatus)
                .includedAlleles(included)
                .build();

        assertThat(instance.getAlleleId(), equalTo(alleleId));
        assertThat(instance.getPrimaryInterpretation(), equalTo(clinSig));
        assertThat(instance.getSecondaryInterpretations(), equalTo(secondaryInterpretations));
        assertThat(instance.getReviewStatus(), equalTo(reviewStatus));
        assertThat(instance.getIncludedAlleles(), equalTo(included));
    }

    @Test
    public void testStringValue() {
        String alleleId = "12345";
        ClinSig clinSig = ClinSig.PATHOGENIC;
        Set<ClinSig> secondaryInterpretations = EnumSet.of(ClinSig.RISK_FACTOR, ClinSig.ASSOCIATION);
        String reviewStatus = "multiple_submitters,_no_conflict";
        Map<String, ClinSig> included = ImmutableMap.of("54321", ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC);
        ClinVarData instance = ClinVarData.builder()
                .alleleId(alleleId)
                .primaryInterpretation(clinSig)
                .secondaryInterpretations(secondaryInterpretations)
                .reviewStatus(reviewStatus)
                .includedAlleles(included)
                .build();
        assertThat(instance.toString(), containsString("PATHOGENIC"));
    }
}