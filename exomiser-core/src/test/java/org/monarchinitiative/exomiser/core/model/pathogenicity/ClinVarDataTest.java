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

package org.monarchinitiative.exomiser.core.model.pathogenicity;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ClinSig;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ClinVarDataTest {

    @Test
    void testEmptyBuilder() {
        ClinVarData instance = ClinVarData.builder().build();
        assertThat(instance, equalTo(ClinVarData.empty()));
        assertThat(instance.variationId(), equalTo(""));
        assertThat(instance.primaryInterpretation(), equalTo(ClinSig.NOT_PROVIDED));
        assertThat(instance.secondaryInterpretations(), equalTo(Collections.emptySet()));
        assertThat(instance.reviewStatus(), equalTo(ClinVarData.ReviewStatus.NO_ASSERTION_PROVIDED));
        assertThat(instance.includedAlleles(), equalTo(Collections.emptyMap()));
    }

    @Test
    void testBuilderWithValues() {
        String alleleId = "12345";
        String variationId = "23456";
        String geneSymbol = "GENE1";
        VariantEffect variantEffect = VariantEffect.MISSENSE_VARIANT;
        ClinSig clinSig = ClinSig.PATHOGENIC;
        Set<ClinSig> secondaryInterpretations = EnumSet.of(ClinSig.RISK_FACTOR, ClinSig.ASSOCIATION);
        Map<String, ClinSig> included = Map.of("54321", ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC);
        ClinVarData instance = ClinVarData.builder()
                .variationId(variationId)
                .geneSymbol(geneSymbol)
                .variantEffect(variantEffect)
                .primaryInterpretation(clinSig)
                .secondaryInterpretations(secondaryInterpretations)
                .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS)
                .includedAlleles(included)
                .build();

        assertThat(instance.variationId(), equalTo(variationId));
        assertThat(instance.geneSymbol(), equalTo(geneSymbol));
        assertThat(instance.variantEffect(), equalTo(variantEffect));
        assertThat(instance.primaryInterpretation(), equalTo(clinSig));
        assertThat(instance.secondaryInterpretations(), equalTo(secondaryInterpretations));
        assertThat(instance.reviewStatus(), equalTo(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS));
        assertThat(instance.includedAlleles(), equalTo(included));
        System.out.println(instance);
    }

    @Test
    void testStringValue() {
        String alleleId = "12345";
        ClinSig clinSig = ClinSig.PATHOGENIC;
        Set<ClinSig> secondaryInterpretations = EnumSet.of(ClinSig.RISK_FACTOR, ClinSig.ASSOCIATION);
        Map<String, ClinSig> included = Map.of("54321", ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC);
        ClinVarData instance = ClinVarData.builder()
                .primaryInterpretation(clinSig)
                .secondaryInterpretations(secondaryInterpretations)
                .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS)
                .includedAlleles(included)
                .build();
        assertThat(instance.toString(), containsString("PATHOGENIC"));
    }

    @Test
    void testStarRating() {
        assertThat(starRating(""), equalTo(0));
        assertThat(starRating("other"), equalTo(0));
        assertThat(starRating("criteria_provided,_single_submitter"), equalTo(1));
        assertThat(starRating("criteria_provided,_conflicting_interpretations"), equalTo(1));
        assertThat(starRating("criteria_provided,_multiple_submitters,_no_conflicts"), equalTo(2));
        assertThat(starRating("reviewed_by_expert_panel"), equalTo(3));
        assertThat(starRating("practice_guideline"), equalTo(4));
    }

    private int starRating(String clinRevStat) {
        return ClinVarData.ReviewStatus.parseReviewStatus(clinRevStat).starRating();
    }

    @Test
    void testIsSecondaryAssociationRiskFactorOrOther() {
        assertThat(isSecondaryAssociationRiskFactorOrOther(), is(false));
        assertThat(isSecondaryAssociationRiskFactorOrOther(ClinSig.DRUG_RESPONSE), is(false));
        assertThat(isSecondaryAssociationRiskFactorOrOther(ClinSig.OTHER), is(true));
        assertThat(isSecondaryAssociationRiskFactorOrOther(ClinSig.ASSOCIATION), is(true));
        assertThat(isSecondaryAssociationRiskFactorOrOther(ClinSig.RISK_FACTOR), is(true));
        assertThat(isSecondaryAssociationRiskFactorOrOther(ClinSig.PROTECTIVE), is(true));
        assertThat(isSecondaryAssociationRiskFactorOrOther(ClinSig.AFFECTS), is(true));
    }

    private boolean isSecondaryAssociationRiskFactorOrOther(ClinSig... secondaryInterpretations) {
        return ClinVarData.builder()
                .secondaryInterpretations(Set.of(secondaryInterpretations))
                .build()
                .isSecondaryAssociationRiskFactorOrOther();
    }

    @Test
    void hgvsC() {
        ClinVarData instance = ClinVarData.builder()
                .hgvsCdna("c.12345A>G")
                .build();
        assertThat(instance.hgvsCdna(), equalTo("c.12345A>G"));
    }

    @Test
    void hgvsP() {
        ClinVarData instance = ClinVarData.builder()
                .hgvsProtein("p.(Ser123Gly)")
                .build();
        assertThat(instance.hgvsProtein(), equalTo("p.(Ser123Gly)"));
    }

    @Test
    void conflictingInterpretations() {
        Map<ClinSig, Integer> conflictingInterpretations = Map.of(ClinSig.PATHOGENIC, 3, ClinSig.UNCERTAIN_SIGNIFICANCE, 1);
        ClinVarData instance = ClinVarData.builder()
                .conflictingInterpretationCounts(conflictingInterpretations)
                .build();
        assertThat(instance.conflictingInterpretationCounts(), equalTo(conflictingInterpretations));
    }

    @Test
    void toBuilder() {
        ClinVarData instance = ClinVarData.builder()
                .variationId("12345")
                .geneSymbol("GENE1")
                .primaryInterpretation(ClinSig.LIKELY_PATHOGENIC)
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .hgvsCdna("c.12345A>G")
                .hgvsProtein("p.(Ser123Gly)")
                .secondaryInterpretations(EnumSet.of(ClinSig.RISK_FACTOR, ClinSig.ASSOCIATION))
                .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                .conflictingInterpretationCounts(Map.of(ClinSig.PATHOGENIC, 3, ClinSig.UNCERTAIN_SIGNIFICANCE, 1))
                .includedAlleles(Map.of("54321", ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC))
                .build();
        assertThat(instance.toBuilder().build(), equalTo(instance));
    }
}