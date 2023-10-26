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

package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class Acmg2015ClassifierTest {

    private final Acmg2015Classifier instance = new Acmg2015Classifier();

    private AcmgEvidence parseAcmgEvidence(String criteria) {
        AcmgEvidence.Builder acmgEvidenceBuilder = AcmgEvidence.builder();
        for (String criterion : criteria.split(" ")) {
            AcmgCriterion acmgCriterion = AcmgCriterion.valueOf(criterion);
            acmgEvidenceBuilder.add(acmgCriterion);
        }
        return acmgEvidenceBuilder.build();
    }

    @ParameterizedTest
    @CsvSource({
//      (i)  1 Very strong (PVS1)
//            AND  (a)  ≥1 Strong (PS1–PS4)
            "PVS1 PS1",
            "PVS1 PS2",
            "PVS1 PS3",
            "PVS1 PS4",
            "PVS1 PS1 PS2",
            "PVS1 PS1 PS2 PS3",
            "PVS1 PS1 PM1 PP1",
//            OR  (b)  ≥2 Moderate (PM1–PM6)
            "PVS1 PM1 PM2",
            "PVS1 PM1 PM2 PM3",
            "PVS1 PM1 PM2 PM3 PM4",
            "PVS1 PM1 PM2 PP1",
//            OR   (c)   1 Moderate (PM1–PM6) and 1 supporting (PP1–PP5)
            "PVS1 PM1 PP1",
            "PVS1 PM1 PP1 PP2",
//            OR  (d)  ≥2 Supporting (PP1–PP5)
            "PVS1 PP1 PP2",
            "PVS1 PP1 PP2 PP3",
    })
    void classifiesPathogenic(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.PATHOGENIC));
    }

    @ParameterizedTest
    @CsvSource({
//            (i)   1 Very strong (PVS1) AND 1 moderate (PM1–PM6)
            "PVS1 PM1",
            "PVS1 PP1", // updated rule https://clinicalgenome.org/site/assets/files/5182/pm2_-_svi_recommendation_-_approved_sept2020.pdf
//            OR (ii)   1 Strong (PS1–PS4) AND 1–2 moderate (PM1–PM6)
            "PS1 PM1 PM2",
//            OR (iii)   1 Strong (PS1–PS4) AND ≥2 supporting (PP1–PP5)
            "PS1 PP1 PP2",
            "PS1 PP1 PP2 PP3",
//            OR (iv)  ≥3 Moderate (PM1–PM6)
            "PM1 PM2 PM3",
//            OR (v)   2 Moderate (PM1–PM6) AND ≥2 supporting (PP1–PP5)
            "PM1 PM2 PP1 PP2",
            "PM1 PM2 PP1 PP2 PP3",
            "PM1 PM2 PP1 PP2 PP3 PP4",
//            OR (vi)   1 Moderate (PM1–PM6) AND ≥4 supporting (PP1–PP5)
            "PM1 PP1 PP2 PP3 PP4",
            "PM1 PP1 PP2 PP3 PP4 PP5",
    })
    void classifiesLikelyPathogenic(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.LIKELY_PATHOGENIC));
    }

    @ParameterizedTest
    @CsvSource({
//            (i)  1 Stand-alone (BA1)
            "BA1",
            "BA1 BS1 BP1",
//            OR (ii)  ≥2 Strong (BS1–BS4)
            "BS1 BS2",
            "BS1 BS2 BS3"
    })
    void classifiesBenign(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.BENIGN));
    }

    @ParameterizedTest
    @CsvSource({
//            (i)   1 Strong (BS1–BS4) and 1 supporting (BP1–BP7)
            "BS1 BP1",
//            OR (ii)  ≥2 Supporting (BP1–BP7)
            "BP1 BP2",
            "BP1 BP2 BP3",
    })
    void classifiesLikelyBenign(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.LIKELY_BENIGN));
    }

    @ParameterizedTest
    @CsvSource({
//            (i)  Other criteria shown above are not met
//            "PVS1 PP1", - Changed to LP in https://clinicalgenome.org/site/assets/files/5182/pm2_-_svi_recommendation_-_approved_sept2020.pdf
            "PS1 PP1", // VUS_Hot
            "PM1 PM2 PP1", // VUS_Hot
//            "PM1 PP1 PP2 PP3", // VUS_Hot - classified as LP
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
            "BS1 BP1 PVS1 PS1",
            "BA1 PVS1 PM1",
            "BP1 BP2 BP3 PM1 PP1 PP2 PP3 PP4",
    })
    void classifiesUncertain(String criteria) {
        AcmgEvidence acmgEvidence = parseAcmgEvidence(criteria);
        assertThat(instance.classify(acmgEvidence), equalTo(AcmgClassification.UNCERTAIN_SIGNIFICANCE));
    }

}