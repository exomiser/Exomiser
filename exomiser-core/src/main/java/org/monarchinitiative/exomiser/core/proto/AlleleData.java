package org.monarchinitiative.exomiser.core.proto;

/**
 * Utility class for creating {@link AlleleProto} instances without the builder verbosity.
 *
 * @since 14.0.0
 */
public class AlleleData {

    private AlleleData() {
    }

    public static AlleleProto.Frequency frequencyOf(AlleleProto.FrequencySource frequencySource, float freq) {
        if (freq > 100f) {
            throw new IllegalArgumentException(frequencySource + " AF=" + freq + " must be less than 100%!");
        }
        return AlleleProto.Frequency.newBuilder().setFrequencySource(frequencySource).setFrequency(freq).build();
    }

    public static AlleleProto.Frequency frequencyOf(AlleleProto.FrequencySource frequencySource, int ac, int an) {
        if (ac > an) {
            throw new IllegalArgumentException(frequencySource + " AC=" + ac + " must be less than or equal to AN=" + an);
        }
        return AlleleProto.Frequency.newBuilder().setFrequencySource(frequencySource).setAc(ac).setAn(an).build();
    }

    public static AlleleProto.Frequency frequencyOf(AlleleProto.FrequencySource frequencySource, int ac, int an, int hom) {
        if (ac > an) {
            throw new IllegalArgumentException(frequencySource + " AC=" + ac + " must be less than or equal to AN=" + an);
        }
        if (hom > ac) {
            throw new IllegalArgumentException(frequencySource + " HOM=" + hom + " must be less than or equal to AC=" + ac);
        }
        return AlleleProto.Frequency.newBuilder().setFrequencySource(frequencySource).setAc(ac).setAn(an).setHom(hom).build();
    }

    public static AlleleProto.PathogenicityScore pathogenicityScoreOf(AlleleProto.PathogenicitySource pathSource, float score) {
        return AlleleProto.PathogenicityScore.newBuilder().setPathogenicitySource(pathSource).setScore(score).build();
    }

}
