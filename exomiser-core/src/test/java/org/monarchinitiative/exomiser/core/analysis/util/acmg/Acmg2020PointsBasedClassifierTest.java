package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Acmg2020PointsBasedClassifierTest {


    private final AcmgEvidenceClassifier instance = new Acmg2020PointsBasedClassifier();

    private AcmgEvidence parseAcmgEvidence(String criteria) {
        AcmgEvidence.Builder acmgEvidenceBuilder = AcmgEvidence.builder();
        for (String criterion : criteria.split(" ")) {
            String[] criteriaModifier = criterion.split("_");
            AcmgCriterion acmgCriterion = AcmgCriterion.valueOf(criteriaModifier[0]);
            if (criteriaModifier.length == 2) {
                AcmgCriterion.Evidence evidence = AcmgCriterion.Evidence.parseValue(criteriaModifier[1]);
                acmgEvidenceBuilder.add(acmgCriterion, evidence);
            } else {
                acmgEvidenceBuilder.add(acmgCriterion);
            }
        }
        return acmgEvidenceBuilder.build();
    }

    @ParameterizedTest
    @CsvSource({
            "PVS1 PP5_VeryStrong",
//      (a)  1 Very strong (PVS1) AND
//            ≥1 Strong (PS1–PS4) OR
            "PVS1 PS1",
            "PVS1 PS2",
            "PVS1 PS3",
            "PVS1 PS4",
            "PVS1 PS1 PS2",
            "PVS1 PS1 PS2 PS3",
//            ≥1 moderate (PM1–PM6) OR
            "PVS1 PM1",
            "PVS1 PM1 PM2",
            "PVS1 PM1 PM2 PM3",
            "PVS1 PM1 PM2 PM3 PM4",
            "PVS1 PM1 PM2 PP1",
//            ≥2 supporting (PP1–PP5)
            "PVS1 PP1 PP2",
            "PVS1 PP1 PP2 PP3",
//      (b)  ≥3 Strong (PS1-PS4)
            "PS1 PS2 PS3",
//      (c)  2 Strong (PS1-PS4) AND
//            ≥1 moderate (PM1–PM6) OR
            "PS1 PS2 PM1",
//            ≥2 supporting (PP1–PP5)
            "PS1 PS2 PP1 PP2",
//      (d)  1 Strong (PS1-PS4) AND
//            ≥3 moderate (PM1–PM6) OR
            "PS1 PM1 PM2 PM3",
//            ≥2 moderate (PM1–PM6) AND ≥2 supporting (PP1–PP5) OR
            "PS1 PM1 PM2 PP1 PP2",
//            ≥1 moderate (PM1–PM6) AND ≥4 supporting (PP1–PP5)
            "PS1 PM1 PP1 PP2 PP3 PP4",
    })
    void classifiesPathogenic(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.PATHOGENIC));
    }

    @ParameterizedTest
    @CsvSource({
            "PVS1 PP1",
//      (c)  >=2 Strong
            "PS1 PS2",
            "PS1 PS2 PP1",
//      (b)  1 Strong (PS1–PS4) AND
//             1–2 moderate (PM1–PM6) OR
            "PS1 PM1 PM2",
            "PS1 PM1",
//             ≥2 supporting (PP1–PP5)
            "PS1 PP1 PP2",
            "PS1 PP1 PP2 PP3",
//      (c)  ≥3 Moderate (PM1–PM6) OR
            "PM1 PM2 PM3",
//           2 Moderate (PM1–PM6) AND ≥2 supporting (PP1–PP5) OR
            "PM1 PM2 PP1 PP2",
            "PM1 PM2 PP1 PP2 PP3",
            "PM1 PM2 PP1 PP2 PP3 PP4",
//           1 Moderate (PM1–PM6) AND ≥4 supporting (PP1–PP5)
            "PM1 PP1 PP2 PP3 PP4",
            "PM1 PP1 PP2 PP3 PP4 PP5",
            //PM2, PP3_Strong, PP4, PP5_Strong
    })
    void classifiesLikelyPathogenic(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.LIKELY_PATHOGENIC));
    }

    @ParameterizedTest
    @CsvSource({
//            (i)  Other criteria shown above are not met
            "PS1 PP1", // VUS_Hot
            "PM1 PM2 PP1", // VUS_Hot
            "PM1 PP1 PP2 PP3", // VUS_Hot - classified as LP
            "PS1", // VUS_Warm
            "PM1 PM2", // VUS_Warm
            "PM1 PP1 PP2", // VUS_Warm
            "PP1 PP2 PP3 PP4", // VUS_Warm
            "PM1 PP2", // VUS_Tepid
            "PP1 PP2 PP3", // VUS_Warm
            "PM1", // VUS_Cool
            "PP1 PP2", // VUS_Warm
            "PP1", // VUS_Cold
//            OR  (ii)   the criteria for benign and pathogenic are contradictory
            "PS1 BP1",
//            "BS1 BP1 PVS1 PS1", // This is LP (12P, 5B = 7 points)
//            "BA1 PVS1 PM1", // is this possible?
            "BP1 BP2 BP3 PM1 PP1 PP2 PP3 PP4",
            "PP1 BP1",
    })
    void classifiesUncertain(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.UNCERTAIN_SIGNIFICANCE));
    }

    @ParameterizedTest
    @CsvSource({
//            (i)   1 Strong (BS1–BS4) and 1 supporting (BP1–BP7)
            "BS1 BP1",
//            OR (ii)  ≥2 Supporting (BP1–BP7)
            "BP1 BP2",
            "BP4",
            "BP4_Moderate", // This is possible
            "BP1 BP2 BP3",
            "BP4_Moderate BP6",
    })
    void classifiesLikelyBenign(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.LIKELY_BENIGN));
    }

    @ParameterizedTest
    @CsvSource({
//            (i)  1 Stand-alone (BA1)
            "BA1",
            "BA1 BS1 BP1",
//            OR (ii)  ≥2 Strong (BS1–BS4)
            "BS1 BS2",
            "BS1 BS2 BS3",
            "PS3 PS4 PP1_Supporting PM3 PP4 BA1" // would be a warm VUS (4 points) if not for the BA1 benign hard-filter
    })
    void classifiesBenign(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.BENIGN));
    }

    @Test
    void noEvidenceClassifiesUnknown() {
        assertThat(instance.classify(AcmgEvidence.empty()), equalTo(AcmgClassification.UNCERTAIN_SIGNIFICANCE));
    }


    @ParameterizedTest
    @CsvSource({
            "11, PATHOGENIC",
            "10, PATHOGENIC",
            "9, LIKELY_PATHOGENIC",
            "8, LIKELY_PATHOGENIC",
            "7, LIKELY_PATHOGENIC",
            "6, LIKELY_PATHOGENIC",
            "5, UNCERTAIN_SIGNIFICANCE",
            "4, UNCERTAIN_SIGNIFICANCE",
            "3, UNCERTAIN_SIGNIFICANCE",
            "2, UNCERTAIN_SIGNIFICANCE",
            "1, UNCERTAIN_SIGNIFICANCE",
            "0, UNCERTAIN_SIGNIFICANCE",
            "-1, LIKELY_BENIGN",
            "-2, LIKELY_BENIGN",
            "-3, LIKELY_BENIGN",
            "-4, LIKELY_BENIGN",
            "-5, LIKELY_BENIGN",
            "-6, LIKELY_BENIGN",
            "-7, BENIGN",
            "-8, BENIGN",
    })
    void testPointsClassification(int points, AcmgClassification acmgClassification) {
        assertThat(new Acmg2020PointsBasedClassifier().classification(points), equalTo(acmgClassification));
    }

}