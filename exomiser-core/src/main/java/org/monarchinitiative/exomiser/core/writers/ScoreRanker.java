package org.monarchinitiative.exomiser.core.writers;

import java.math.BigDecimal;
import java.math.RoundingMode;

class ScoreRanker {

    /** Positive zero. */
    private static final double POSITIVE_ZERO = 0d;

    private final int scale;

    private int rank = 0;
    private double currentScore = Double.MAX_VALUE;
    private int count = 0;

    ScoreRanker(int scale) {
        this.scale = scale;
    }

    /**
     * Expects scores to be sorted in descending numerical order.
     *
     * @param score a score from a series of descending values where the highest score is ranked first.
     * @return the running rank of the input score
     */
    int rank(double score) {
        double round = round(score, scale, RoundingMode.HALF_EVEN);
        if (round > currentScore) {
            throw new IllegalStateException("Input score " + score + " greater than previous score of " + currentScore+ ". Scores must be provided in reverse numerical order i.e. highest to lowest.");
        }
        count++;
        if (currentScore == round) {
            return rank;
        }
        currentScore = round;
        rank = count;
        return rank;
    }


    /**
     * Rounds the given value to the specified number of decimal places.
     * The value is rounded using the given method which is any method defined
     * in {@link BigDecimal}.
     * If {@code x} is infinite or {@code NaN}, then the value of {@code x} is
     * returned unchanged, regardless of the other parameters.
     *
     * @param x Value to round.
     * @param scale Number of digits to the right of the decimal point.
     * @param roundingMethod Rounding method as defined in {@link BigDecimal}.
     * @return the rounded value.
     * @throws ArithmeticException if {@code roundingMethod} is
     * {@link RoundingMode#UNNECESSARY} and the specified scaling operation
     * would require rounding.
     */
    private static double round(double x, int scale, RoundingMode roundingMethod) {
        try {
            final double rounded = (new BigDecimal(Double.toString(x))
                    .setScale(scale, roundingMethod))
                    .doubleValue();
            // MATH-1089: negative values rounded to zero should result in negative zero
            return rounded == POSITIVE_ZERO ? POSITIVE_ZERO * x : rounded;
        } catch (NumberFormatException ex) {
            if (Double.isInfinite(x)) {
                return x;
            }
            return Double.NaN;
        }
    }
}
