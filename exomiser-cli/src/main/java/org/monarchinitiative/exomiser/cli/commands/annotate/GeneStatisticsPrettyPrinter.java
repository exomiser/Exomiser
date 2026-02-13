package org.monarchinitiative.exomiser.cli.commands.annotate;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.analysis.score.GeneConstraint;
import org.monarchinitiative.exomiser.core.analysis.score.GeneConstraints;
import org.monarchinitiative.exomiser.core.model.GeneStatistics;

import static de.charite.compbio.jannovar.annotation.VariantEffect.*;

public class GeneStatisticsPrettyPrinter {
    private static final VariantEffect[] NON_CODING = {CODING_TRANSCRIPT_INTRON_VARIANT, INTRON_VARIANT, FIVE_PRIME_UTR_INTRON_VARIANT, FIVE_PRIME_UTR_EXON_VARIANT, FIVE_PRIME_UTR_PREMATURE_START_CODON_GAIN_VARIANT, THREE_PRIME_UTR_INTRON_VARIANT, THREE_PRIME_UTR_EXON_VARIANT, THREE_PRIME_UTR_TRUNCATION};
    private static final  VariantEffect[] INFRAME_INDELS = {INFRAME_DELETION, INFRAME_INSERTION};

    public static String prettyPrint(GeneStatistics geneStatistics) {
        String geneSymbol = geneStatistics.geneSymbol();
        GeneConstraint geneConstraint = GeneConstraints.geneConstraint(geneSymbol);

        String header = String.format("|                      %-8s                    |%n", geneSymbol);
        String headerSep = ("|--------------------------------------------------|\n");
//        System.out.println("|       |  P  |  B  |  V  |  P/T  |  B/A  | pLof/Z |");
//        System.out.println("|-------|-----|-----|-----|-------|-------|--------|");
//        System.out.printf("| Miss  | %3s | %3s | %3s | %.3f | %.3f | %.3f |%n", geneStatistics.missensePathCount(), geneStatistics.missenseBenignCount(), geneStatistics.missenseVusCount(), geneStatistics.missensePathCount() / (float) geneStatistics.pathCount(), (float) geneStatistics.missenseBenignCount() / (geneStatistics.missenseBenignCount() + geneStatistics.missenseVusCount() + geneStatistics.missensePathCount()), (float) (geneConstraint == null ? 0f : geneConstraint.missenseZ()));
//        System.out.printf("| LOF   | %3s | %3s | %3s | %.3f | %.3f | %.3f |%n", geneStatistics.lofPathCount(), geneStatistics.lofBenignCount(), geneStatistics.lofVusCount(), geneStatistics.lofPathCount() / (float) geneStatistics.pathCount(), (float) geneStatistics.lofBenignCount() / (geneStatistics.lofBenignCount() + geneStatistics.lofVusCount() + geneStatistics.lofPathCount()), (float) (geneConstraint == null ? 0f : geneConstraint.loeuf()));
//        System.out.printf("| Syn   | %3s | %3s | %3s | %.3f | %.3f | %.3f |%n", geneStatistics.pathCount(SYNONYMOUS_VARIANT), geneStatistics.benignCount(SYNONYMOUS_VARIANT), geneStatistics.vusCount(SYNONYMOUS_VARIANT), geneStatistics.pathCount(SYNONYMOUS_VARIANT) / (float) geneStatistics.pathCount(), (float) geneStatistics.benignCount(SYNONYMOUS_VARIANT) / (geneStatistics.benignCount(SYNONYMOUS_VARIANT) + geneStatistics.vusCount(SYNONYMOUS_VARIANT) + geneStatistics.pathCount(SYNONYMOUS_VARIANT)), (float) (geneConstraint == null ? 0f : geneConstraint.synonymousZ()));
//        System.out.printf("| NC    | %3s | %3s | %3s | %.3f | %.3f | %.3f |%n", geneStatistics.pathCount(NON_CODING), geneStatistics.benignCount(NON_CODING), geneStatistics.vusCount(NON_CODING), geneStatistics.pathCount(NON_CODING) / (float) geneStatistics.pathCount(), (float) geneStatistics.benignCount(NON_CODING) / (geneStatistics.benignCount(NON_CODING) + geneStatistics.vusCount(NON_CODING) + geneStatistics.pathCount(NON_CODING)), 0f);
//        System.out.printf("| InDel | %3s | %3s | %3s | %.3f | %.3f | %.3f |%n", geneStatistics.pathCount(INFRAME_INDELS), geneStatistics.benignCount(INFRAME_INDELS), geneStatistics.vusCount(INFRAME_INDELS), geneStatistics.pathCount(INFRAME_INDELS) / (float) geneStatistics.pathCount(), (float) geneStatistics.benignCount(INFRAME_INDELS) / (geneStatistics.benignCount(INFRAME_INDELS) + geneStatistics.vusCount(INFRAME_INDELS) + geneStatistics.pathCount(INFRAME_INDELS)), 0f);
//        System.out.printf("| Total | %3s | %3s | %3s | %.3f |   %-3s |   %-3s |%n", geneStatistics.pathCount(), geneStatistics.benignCount(), geneStatistics.vusCount(), geneStatistics.pathCount() / (float) geneStatistics.benignCount(), "-", "-", "-");
        return "";
    }
}
