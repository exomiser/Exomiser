package org.monarchinitiative.exomiser.core.proto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AlleleProtoFormatterTest {

    @Test
    void testFormatAlleleKey() {
        var alleleKey = AlleleProto.AlleleKey.newBuilder().setChr(1).setPosition(23456).setRef("A").setAlt("T").build();
        assertThat(AlleleProtoFormatter.format(alleleKey), equalTo("1-23456-A-T"));
    }

    @Test
    void testFormatAlleleFrequencies() {
        var freq1 = AlleleProto.Frequency.newBuilder().setFrequencySource(AlleleProto.FrequencySource.UK10K)
                .setAc(3)
                .setAn(5500)
                .build();
        var freq2 = AlleleProto.Frequency.newBuilder().setFrequencySource(AlleleProto.FrequencySource.GNOMAD_E_AFR)
                .setAc(5)
                .setAn(15000)
                .setHom(2)
                .build();
        var alleleFrequencies = List.of(freq1, freq2);
        assertThat(AlleleProtoFormatter.formatFrequencies(alleleFrequencies), equalTo("[UK10K=3|5500|0|0.055, GNOMAD_E_AFR=5|15000|2|0.033]"));
    }

    @Test
    void testFormatPathScores() {
        var path1 = AlleleProto.PathogenicityScore.newBuilder()
                .setPathogenicitySource(AlleleProto.PathogenicitySource.REVEL)
                .setScore(0.95f)
                .build();
        var path2 = AlleleProto.PathogenicityScore.newBuilder()
                .setPathogenicitySource(AlleleProto.PathogenicitySource.MVP)
                .setScore(0.90f)
                .build();
        var pathScores = List.of(path1, path2);
        assertThat(AlleleProtoFormatter.formatPathScores(pathScores), equalTo("[REVEL=0.95, MVP=0.9]"));
    }
}