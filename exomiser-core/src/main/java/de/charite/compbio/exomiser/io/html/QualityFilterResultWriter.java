/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.io.html;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.filter.FilterScore;
import de.charite.compbio.exomiser.core.filter.FilterType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class QualityFilterResultWriter implements FilterResultWriter {
    
    /**
     * @return A string with a summary of the filtering results .
     */
    public String getFilterResultSummary(VariantEvaluation variantEval) {
        FilterScore qualityScore = variantEval.getFilterScore(FilterType.QUALITY_FILTER);
        return String.format("Quality: %.1f", qualityScore.getScore());
    }

    /**
     * @return HTML code to display the PHRED score for the variant call as a
     * bullet point.
     */
    public String getHTMLCode(VariantEvaluation variantEval) {
        FilterScore qualityScore = variantEval.getFilterScore(FilterType.QUALITY_FILTER);
        return String.format("<UL><LI>PHRED: %d</LI></UL>\n", (int) qualityScore.getScore());
    }

    /**
     * This was removed because the DP4 field is too rarely used in VCF files
     * and many simply do not have thhis data. ield is too rarely used in VCF
     * files and many simply do not have thhis data. private String
     * getDP4TableAsHTML() { StringBuilder sb = new StringBuilder();
     * sb.append("<table
     * id=\"qy\"><tr><th>read</th><th>&rarr;</th><th>&larr;</th></tr>\n");
     * sb.append(String.format("<tr><td>ref</td><td>%d</td><td>%d</td></tr>\n",
     * DP4[N_REF_FORWARD_BASES],DP4[N_REF_REVERSE_BASES]));
     * sb.append(String.format("<tr><td>alt</td><td>%d</td><td>%d</td></tr>\n",
     * DP4[N_ALT_FORWARD_BASES],DP4[N_ALT_REVERSE_BASES]));
     * sb.append("</table>\n"); return sb.toString(); }
     */
    /**
     * @return A list with detailed results of filtering. The list is intended
     * to be displayed as an HTML list if desired. null should be interpreted as
     * "detailed list not available or sensible".
     */
    public List<String> getFilterResultList(VariantEvaluation variantEval) {
        FilterScore qualityScore = variantEval.getFilterScore(FilterType.QUALITY_FILTER);
        List<String> L = new ArrayList<>();
        L.add(String.format("%d", (int) qualityScore.getScore()));

        return L;
    }
    
}
