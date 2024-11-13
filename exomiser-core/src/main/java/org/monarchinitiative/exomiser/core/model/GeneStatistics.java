package org.monarchinitiative.exomiser.core.model;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;

import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

import static de.charite.compbio.jannovar.annotation.VariantEffect.*;

/**
 * Data class for collating ClinVar pathogenicity assertions against variant effect, primarily for the application of
 * ACMG criteria BP1 and PP2.
 *
 * @since 14.1.0
 */
public class GeneStatistics {

    private static final int PATH_INDEX = 0;
    private static final int VUS_INDEX = 1;
    private static final int BENIGN_INDEX = 2;

    private static final VariantEffect[] LOF_EFFECTS = {
            STOP_LOST, STOP_GAINED, FRAMESHIFT_ELONGATION, FRAMESHIFT_TRUNCATION, FRAMESHIFT_VARIANT,
            SPLICE_DONOR_VARIANT, SPLICE_ACCEPTOR_VARIANT,
            TRANSCRIPT_ABLATION, EXON_LOSS_VARIANT,
            START_LOST
    };

    private final String geneSymbol;
    private final Map<VariantEffect, int[]> clinVarCounts;

    private GeneStatistics(String geneSymbol, Map<VariantEffect, int[]> clinVarCounts) {
        this.geneSymbol = geneSymbol;
        this.clinVarCounts = clinVarCounts;
    }

    /**
     * @return The gene symbol for the gene related to these statistics
     */
    public String geneSymbol() {
        return geneSymbol;
    }

    /**
     * @return The sum of all pathogenic / likely pathogenic variants associated with this gene.
     */
    public int pathCount() {
        return totalCountForIndex(PATH_INDEX);
    }

    /**
     * @return The sum of all variants of uncertain significance (VUS) associated with this gene.
     */
    public int vusCount() {
        return totalCountForIndex(VUS_INDEX);
    }

    /**
     * @return The sum of all benign / likely benign variants associated with this gene.
     */
    public int benignCount() {
        return totalCountForIndex(BENIGN_INDEX);
    }

    private int totalCountForIndex(int i) {
        int count = 0;
        for (Map.Entry<VariantEffect, int[]> variantEffectEntry : clinVarCounts.entrySet()) {
            count += variantEffectEntry.getValue()[i];
        }
        return count;
    }

    /**
     * The sum of all pathogenic / likely pathogenic Loss Of Function variants associated with this gene. These are
     * defined here as:
     * <pre>
     *      STOP_LOST, STOP_GAINED, FRAMESHIFT_ELONGATION, FRAMESHIFT_TRUNCATION, FRAMESHIFT_VARIANT,
     *      SPLICE_DONOR_VARIANT, SPLICE_ACCEPTOR_VARIANT,
     *      TRANSCRIPT_ABLATION, EXON_LOSS_VARIANT,
     *      START_LOST
     * </pre>
     *
     * @return The sum of all pathogenic / likely pathogenic Loss Of Function variants associated with this gene.
     */
    public int lofPathCount() {
        return pathCount(LOF_EFFECTS);
    }

    /**
     * The sum of all variants of uncertain significance (VUS) Loss Of Function variants associated with this gene. These are
     * defined here as:
     * <pre>
     *      STOP_LOST, STOP_GAINED, FRAMESHIFT_ELONGATION, FRAMESHIFT_TRUNCATION, FRAMESHIFT_VARIANT,
     *      SPLICE_DONOR_VARIANT, SPLICE_ACCEPTOR_VARIANT,
     *      TRANSCRIPT_ABLATION, EXON_LOSS_VARIANT,
     *      START_LOST
     * </pre>
     *
     * @return The sum of all variants of uncertain significance (VUS) Loss Of Function variants associated with this gene.
     */
    public int lofVusCount() {
        return vusCount(LOF_EFFECTS);
    }

    /**
     * The sum of all benign / likely benign Loss Of Function variants associated with this gene. These are defined here
     * as:
     * <pre>
     *      STOP_LOST, STOP_GAINED, FRAMESHIFT_ELONGATION, FRAMESHIFT_TRUNCATION, FRAMESHIFT_VARIANT,
     *      SPLICE_DONOR_VARIANT, SPLICE_ACCEPTOR_VARIANT,
     *      TRANSCRIPT_ABLATION, EXON_LOSS_VARIANT,
     *      START_LOST
     * </pre>
     *
     * @return The sum of all benign / likely benign Loss Of Function variants associated with this gene.
     */
    public int lofBenignCount() {
        return benignCount(LOF_EFFECTS);
    }

    /**
     * @return The sum of all pathogenic / likely pathogenic missense variants associated with this gene.
     */
    public int missensePathCount() {
        return pathCount(VariantEffect.MISSENSE_VARIANT);
    }

    /**
     * @return The sum of all VUS missense variants associated with this gene.
     */
    public int missenseVusCount() {
        return vusCount(VariantEffect.MISSENSE_VARIANT);
    }

    /**
     * @return The sum of all benign / likely benign missense variants associated with this gene.
     */
    public int missenseBenignCount() {
        return benignCount(VariantEffect.MISSENSE_VARIANT);
    }

    /**
     * @return The sum of all pathogenic / likely pathogenic variants for the given variantEffect associated with this gene.
     */
    public int pathCount(VariantEffect variantEffect) {
        return effectIndexCount(variantEffect, PATH_INDEX);
    }

    /**
     * @return The sum of all VUS variants for the given variantEffect associated with this gene.
     */
    public int vusCount(VariantEffect variantEffect) {
        return effectIndexCount(variantEffect, VUS_INDEX);
    }

    /**
     * @return The sum of all benign / likely benign variants for the given variantEffect associated with this gene.
     */
    public int benignCount(VariantEffect variantEffect) {
        return effectIndexCount(variantEffect, BENIGN_INDEX);
    }

    /**
     * @return The sum of all pathogenic / likely pathogenic variants for the given variantEffects associated with this gene.
     */
    public int pathCount(VariantEffect... variantEffect) {
        return effectIndexCounts(variantEffect, PATH_INDEX);
    }

    /**
     * @return The sum of all VUS variants for the given variantEffects associated with this gene.
     */
    public int vusCount(VariantEffect... variantEffect) {
        return effectIndexCounts(variantEffect, VUS_INDEX);
    }

    /**
     * @return The sum of all benign / likely benign variants for the given variantEffects associated with this gene.
     */
    public int benignCount(VariantEffect... variantEffect) {
        return effectIndexCounts(variantEffect, BENIGN_INDEX);
    }

    private int effectIndexCounts(VariantEffect[] variantEffect, int index) {
        int count = 0;
        for (int i = 0; i < variantEffect.length; i++) {
            count += effectIndexCount(variantEffect[i], index);
        }
        return count;
    }

    private int effectIndexCount(VariantEffect variantEffect, int i) {
        return clinVarCounts.getOrDefault(variantEffect, new int[3])[i];
    }

    @Override
    public String toString() {
        StringBuilder statsBuilder = new StringBuilder("{");
        clinVarCounts.forEach((key, value) -> statsBuilder.append(key).append("=").append("[").append(prettyStats(value)).append("], "));
        if (statsBuilder.length() > 1) {
            statsBuilder.delete(statsBuilder.length() - 2, statsBuilder.length());
        }
        statsBuilder.append("}");
        return "GeneStatistics{" +
               "geneSymbol=" + geneSymbol + ',' +
               " counts=" + statsBuilder +
               '}';
    }

    private static final String[] categories = {"P", "VUS", "B"};

    private String prettyStats(int[] counts) {
        StringJoiner countStr = new StringJoiner(", ");
        for (int i = 0; i < 3; i++) {
            countStr.add(categories[i] + "=" + counts[i]);
        }
        return countStr.toString();
    }

    public static Builder builder(String geneSymbol) {
        return new Builder(geneSymbol);
    }

    public static Builder builder(String geneSymbol, VariantEffect variantEffect, ClinVarData.ClinSig clinSig) {
        return new Builder(geneSymbol, variantEffect, clinSig);
    }

    public static class Builder {

        private final String geneSymbol;
        private final Map<VariantEffect, int[]> geneStats = new EnumMap<>(VariantEffect.class);

        public Builder(String geneSymbol) {
            this.geneSymbol = geneSymbol;
        }

        public Builder(String geneSymbol, VariantEffect variantEffect, ClinVarData.ClinSig clinSig) {
            this.geneSymbol = geneSymbol;
            put(variantEffect, clinSig);
        }

        public Builder put(VariantEffect variantEffect, ClinVarData.ClinSig clinSig) {
            int countIndex = getCountIndex(clinSig);
            if (countIndex != -1) {
                int[] counts = geneStats.get(variantEffect);
                if (counts == null) {
                    counts = new int[3];
                    counts[countIndex]++;
                    geneStats.put(variantEffect, counts);
                } else {
                    counts[countIndex]++;
                }
            }
            return this;
        }

        private static int getCountIndex(ClinVarData.ClinSig clinSig) {
            return switch (clinSig) {
                case PATHOGENIC, PATHOGENIC_OR_LIKELY_PATHOGENIC, LIKELY_PATHOGENIC -> PATH_INDEX;
                case UNCERTAIN_SIGNIFICANCE -> VUS_INDEX;
                case LIKELY_BENIGN, BENIGN_OR_LIKELY_BENIGN, BENIGN -> BENIGN_INDEX;
                default -> -1;
            };
        }

        public GeneStatistics build() {
            return new GeneStatistics(geneSymbol, geneStats);
        }
    }

}
