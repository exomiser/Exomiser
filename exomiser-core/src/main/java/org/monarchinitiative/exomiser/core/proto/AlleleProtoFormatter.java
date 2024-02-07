package org.monarchinitiative.exomiser.core.proto;

import org.monarchinitiative.exomiser.core.model.frequency.Frequency;

import java.text.NumberFormat;
import java.util.List;

/**
 * Provides a simple string representation of {@link AlleleProto} classes. From native proto these are multi-line
 * json-like strings which don't display well in logs so this class is here to help provide a more succinct output.
 * @since 14.0.0
 */
public class AlleleProtoFormatter {

    private static final NumberFormat numberFormat = NumberFormat.getInstance();


    private AlleleProtoFormatter() {
        numberFormat.setMinimumFractionDigits(3);
        numberFormat.setMaximumFractionDigits(3);
    }

    public static String format(AlleleProto.AlleleKey alleleKey) {
        return alleleKey.getChr() + "-" + alleleKey.getPosition() + "-" + alleleKey.getRef() + "-" + alleleKey.getAlt();
    }

    public static String format(AlleleProto.AlleleProperties alleleProperties) {
        String clinVarString = "";
        if (alleleProperties.hasClinVar()) {
            AlleleProto.ClinVar clinVar = alleleProperties.getClinVar();
            clinVarString = "clinVar={" + "primaryInterpretation=" + clinVar.getPrimaryInterpretation() + ", alleleId=" + clinVar.getAlleleId() + ", reviewStatus=" + clinVar.getReviewStatus() + "}, ";
        }
        String rsId = "";
        if (!alleleProperties.getRsId().isEmpty()) {
            rsId = "rsId=" + alleleProperties.getRsId() + ", ";
        }
        return rsId + clinVarString + "freq=" + formatFrequencies(alleleProperties.getFrequenciesList()) + ", path=" + formatPathScores(alleleProperties.getPathogenicityScoresList());
    }

    public static String formatFrequencies(List<AlleleProto.Frequency> frequencies) {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (int i = 0; i < frequencies.size(); i++) {
            AlleleProto.Frequency frequency = frequencies.get(i);
            stringBuilder.append(formatFrequency(frequency));
            if (i < frequencies.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public static String formatFrequency(AlleleProto.Frequency frequency) {
        return frequency.getFrequencySource()
                + "=" +
                frequency.getAc()
                + "|" +
                frequency.getAn()
                + "|" +
                frequency.getHom()
                + "|" +
                numberFormat.format(frequency.getAn() == 0 ? frequency.getFrequency() : Frequency.percentageFrequency(frequency.getAc(), frequency.getAn()));
    }

    public static String formatPathScores(List<AlleleProto.PathogenicityScore> pathogenicityScores) {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (int i = 0; i < pathogenicityScores.size(); i++) {
            AlleleProto.PathogenicityScore pathogenicityScore = pathogenicityScores.get(i);
            stringBuilder.append(formatPathogenicityScore(pathogenicityScore));
            if (i < pathogenicityScores.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public static String formatPathogenicityScore(AlleleProto.PathogenicityScore pathogenicityScore) {
        return pathogenicityScore.getPathogenicitySource() + "=" + pathogenicityScore.getScore();
    }
}
