package org.monarchinitiative.exomiser.cli;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.cli.VariantReanalyser.ReclassificationCandidate;
import org.monarchinitiative.exomiser.cli.VariantReanalyser.ResultRecord;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgClassification;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class VariantReanalyserTest {

    @Test
    void findReclassificationCandidatesHumanPhenoScoreIncrease() {
        ResultRecord originalResult = new ResultRecord(1, "1-12345-A-T", 0.501f, 0.2f, AcmgClassification.UNCERTAIN_SIGNIFICANCE, "");
        Map<String, ResultRecord> original = Map.of(originalResult.variant(), originalResult);

        ResultRecord reanalysedResult = new ResultRecord(1, "1-12345-A-T", 0.69f, 0.4f, AcmgClassification.UNCERTAIN_SIGNIFICANCE, "");
        Map<String, ResultRecord> reanalysed = Map.of(reanalysedResult.variant(), reanalysedResult);

        var expected = List.of(new ReclassificationCandidate(originalResult, reanalysedResult));

        VariantReanalyser instance = new VariantReanalyser(Path.of(""), Path.of(""), Path.of(""));
        assertThat(instance.findReclassificationCandidates(original, reanalysed), equalTo(expected));
    }

    @Test
    void findReclassificationCandidatesAcmgClassificationIncrease() {
        ResultRecord originalResult = new ResultRecord(1, "1-12345-A-T", 0.501f, 0.2f, AcmgClassification.UNCERTAIN_SIGNIFICANCE, "");
        Map<String, ResultRecord> original = Map.of(originalResult.variant(), originalResult);

        ResultRecord reanalysedResult = new ResultRecord(1, "1-12345-A-T", 0.69f, 0.3f, AcmgClassification.PATHOGENIC, "");
        Map<String, ResultRecord> reanalysed = Map.of(reanalysedResult.variant(), reanalysedResult);

        var expected = List.of(new ReclassificationCandidate(originalResult, reanalysedResult));

        VariantReanalyser instance = new VariantReanalyser(Path.of(""), Path.of(""), Path.of(""));
        assertThat(instance.findReclassificationCandidates(original, reanalysed), equalTo(expected));
    }

    @Test
    void reclassificationCandidatePrintUpdate() {
        ResultRecord originalResult = new ResultRecord(20, "1-12345-A-T", 0.501f, 0.2f, AcmgClassification.UNCERTAIN_SIGNIFICANCE, "20\t1-12345-A-T\t0.501\t0.2\tUNCERTAIN_SIGNIFICANCE");

        ResultRecord reanalysedResult = new ResultRecord(1, "1-12345-A-T", 0.69f, 0.3f, AcmgClassification.PATHOGENIC, "1\t1-12345-A-T\t0.69\t0.3\tPATHOGENIC");

        String updateString = new ReclassificationCandidate(originalResult, reanalysedResult).printUpdate();
        String expected = """
                - 20	1-12345-A-T	0.501	0.2	UNCERTAIN_SIGNIFICANCE
                + 1	1-12345-A-T	0.69	0.3	PATHOGENIC
                """;
        assertThat(updateString, equalTo(expected));
    }

    @Test
    void testRun() throws Exception {
        VariantReanalyser instance = new VariantReanalyser(Path.of("/home/hhx640/Documents/exomiser-cli-13.1.0/results/Pfeiffer-hiphive-exome-PASS_ONLY.variants.tsv"), Path.of("/home/hhx640/Documents/exomiser-cli-dev/results/Pfeiffer-hiphive-exome-PASS_ONLY-2.0-freq-filter.variants.tsv"), Path.of(""));
        instance.call();
    }
}