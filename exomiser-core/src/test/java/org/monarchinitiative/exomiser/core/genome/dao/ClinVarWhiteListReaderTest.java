package org.monarchinitiative.exomiser.core.genome.dao;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.dao.serialisers.MvStoreUtil;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class ClinVarWhiteListReaderTest {

    @Test
    void readVariantWhiteList() {
        try (MVStore clinVarMvStore = new MVStore.Builder().open()) {
            MVMap<AlleleProto.AlleleKey, AlleleProto.ClinVar> alleleKeyClinVarMVMap = MvStoreUtil.openClinVarMVMap(clinVarMvStore);
            AlleleProto.AlleleKey associationKey = AlleleProto.AlleleKey.newBuilder()
                    .setChr(1)
                    .setPosition(100)
                    .setRef("A")
                    .setAlt("C")
                    .build();
            AlleleProto.ClinVar association = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("11111")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.ASSOCIATION)
                    .setReviewStatus("reviewed_by_expert_panel")
                    .build();
            alleleKeyClinVarMVMap.put(associationKey, association);

            AlleleProto.AlleleKey conflictingKey = AlleleProto.AlleleKey.newBuilder()
                    .setChr(1)
                    .setPosition(200)
                    .setRef("A")
                    .setAlt("C")
                    .build();
            AlleleProto.ClinVar conflicting = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("22222")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                    .setReviewStatus("criteria_provided,_conflicting_interpretations")
                    .build();
            alleleKeyClinVarMVMap.put(conflictingKey, conflicting);

            AlleleProto.AlleleKey pathogenicAndRiskFactorKey = AlleleProto.AlleleKey.newBuilder()
                    .setChr(1)
                    .setPosition(300)
                    .setRef("A")
                    .setAlt("C")
                    .build();
            AlleleProto.ClinVar pathogenicAndRiskFactor = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("3333")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.PATHOGENIC)
                    .addSecondaryInterpretations(AlleleProto.ClinVar.ClinSig.RISK_FACTOR)
                    .setReviewStatus("criteria_provided,_multiple submitters,_no_conflicts")
                    .build();
            alleleKeyClinVarMVMap.put(pathogenicAndRiskFactorKey, pathogenicAndRiskFactor);

            AlleleProto.AlleleKey pathogenicAndRiskFactorNoCriteriaKey = AlleleProto.AlleleKey.newBuilder()
                    .setChr(1)
                    .setPosition(310)
                    .setRef("A")
                    .setAlt("C")
                    .build();
            AlleleProto.ClinVar pathogenicAndRiskFactorNoCriteria = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("3333")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.PATHOGENIC)
                    .addSecondaryInterpretations(AlleleProto.ClinVar.ClinSig.RISK_FACTOR)
                    .setReviewStatus("no_assertion_criteria_provided")
                    .build();
            alleleKeyClinVarMVMap.put(pathogenicAndRiskFactorNoCriteriaKey, pathogenicAndRiskFactorNoCriteria);

            AlleleProto.AlleleKey likelyPathNoCriteriaKey = AlleleProto.AlleleKey.newBuilder()
                    .setChr(1)
                    .setPosition(400)
                    .setRef("A")
                    .setAlt("C")
                    .build();
            AlleleProto.ClinVar likelyPathNoCriteria = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("4444")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.LIKELY_PATHOGENIC)
                    .setReviewStatus("no_assertion_criteria_provided")
                    .build();
            alleleKeyClinVarMVMap.put(likelyPathNoCriteriaKey, likelyPathNoCriteria);

            AlleleProto.AlleleKey pathCriteriaProvidedKey = AlleleProto.AlleleKey.newBuilder()
                    .setChr(1)
                    .setPosition(500)
                    .setRef("A")
                    .setAlt("C")
                    .build();
            AlleleProto.ClinVar pathCriteriaProvided = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("5555")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.PATHOGENIC)
                    .setReviewStatus("criteria provided,_single_submitter")
                    .build();
            alleleKeyClinVarMVMap.put(pathCriteriaProvidedKey, pathCriteriaProvided);

            AlleleProto.AlleleKey pathCriteriaProvidedMultipleSubsKey = AlleleProto.AlleleKey.newBuilder()
                    .setChr(1)
                    .setPosition(600)
                    .setRef("A")
                    .setAlt("C")
                    .build();
            AlleleProto.ClinVar pathCriteriaProvidedMultipleSubs = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("6666")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC)
                    .setReviewStatus("criteria provided,_multiple_submitters,_no_conflicts")
                    .build();
            alleleKeyClinVarMVMap.put(pathCriteriaProvidedMultipleSubsKey, pathCriteriaProvidedMultipleSubs);

            AlleleProto.AlleleKey pathExpertReviewPanelKey = AlleleProto.AlleleKey.newBuilder()
                    .setChr(1)
                    .setPosition(700)
                    .setRef("A")
                    .setAlt("C")
                    .build();
            AlleleProto.ClinVar pathExpertReviewPanel = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("7777")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.PATHOGENIC)
                    .setReviewStatus("reviewed_by_expert panel")
                    .build();
            alleleKeyClinVarMVMap.put(pathExpertReviewPanelKey, pathExpertReviewPanel);

            AlleleProto.AlleleKey pathPracticeGuidelineKey = AlleleProto.AlleleKey.newBuilder()
                    .setChr(1)
                    .setPosition(800)
                    .setRef("A")
                    .setAlt("C")
                    .build();
            AlleleProto.ClinVar pathPracticeGuideline = AlleleProto.ClinVar.newBuilder()
                    .setAlleleId("8888")
                    .setPrimaryInterpretation(AlleleProto.ClinVar.ClinSig.PATHOGENIC)
                    .setReviewStatus("practice_guideline")
                    .build();
            alleleKeyClinVarMVMap.put(pathPracticeGuidelineKey, pathPracticeGuideline);

            Set<AlleleProto.AlleleKey> expected = Set.of(pathogenicAndRiskFactorKey, pathCriteriaProvidedKey, pathCriteriaProvidedMultipleSubsKey, pathExpertReviewPanelKey, pathPracticeGuidelineKey);
            Set<AlleleProto.AlleleKey> actual = ClinVarWhiteListReader.readVariantWhiteList(clinVarMvStore);
            assertThat(actual, equalTo(expected));
        }
    }
}