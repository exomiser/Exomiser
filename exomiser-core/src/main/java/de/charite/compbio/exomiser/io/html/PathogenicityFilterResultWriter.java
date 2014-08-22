/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.io.html;

import de.charite.compbio.exomiser.core.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.filter.FilterScore;
import de.charite.compbio.exomiser.core.filter.FilterType;
import jannovar.common.VariantType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PathogenicityFilterResultWriter implements FilterResultWriter {

    /**
     * @param variantEvaluation
     * @return A string with a summary of the filtering results for HTML
     * display.
     */
    @Override
    public String getFilterResultSummary(VariantEvaluation variantEvaluation) {
        PathogenicityData pathogenicityData = variantEvaluation.getPathogenicityData();
        FilterScore pathogenicityScore = variantEvaluation.getFilterScore(FilterType.PATHOGENICITY_FILTER);
        VariantType variantType = variantEvaluation.getVariantType();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<b>%s</b><br/>\n", variantType.toString()));
        if (variantType != VariantType.MISSENSE) {
            sb.append(String.format("%s<br/>\n", pathogenicityScore));
        } else {
            //how many predictions do we have?
            int c = 0;
            MutationTasterScore mutTasterScore = pathogenicityData.getMutationTasterScore();
            if (mutTasterScore != null) {
                sb.append(mutTasterScore);
                sb.append("<br/>\n");
                c++;
            }
            PolyPhenScore polyphenScore = pathogenicityData.getPolyPhenScore();
            if (polyphenScore != null) {
                sb.append(polyphenScore);
                sb.append("<br/>\n");
                c++;
            }
            SiftScore siftScore = pathogenicityData.getSiftScore();
            if (siftScore != null) {
                sb.append(siftScore);
                sb.append("<br/>\n");
                c++;
            }
            if (c == 0) {
                sb.append("No pathogenicity predictions found<br/>\n");
            }
        }
        return sb.toString();
    }

    /**
     * This function returns a list with a summary of the pathogenicity
     * analysis. It first enters the variant type (see
     * {@link jannovar.common.VariantType VariantType}), and then either shows
     * the overall pathogenicity score (defined in several constants in this
     * class such as {@link #FRAMESHIFT_SCORE}), or additionally shows the
     * results of analysis by polyphen2, MutationTaster, and SIFT. This list can
     * be displayed as an HTML list if desired (see
     * {@link exomizer.io.html.HTMLTable HTMLTable}).
     *
     * @return A list with detailed results of filtering.
     */
    public List<String> getFilterResultList(VariantEvaluation variantEvaluation) {
        PathogenicityData pathogenicityData = variantEvaluation.getPathogenicityData();
        FilterScore pathogenicityScore = variantEvaluation.getFilterScore(FilterType.PATHOGENICITY_FILTER);
        VariantType variantType = variantEvaluation.getVariantType();

        List<String> resultList = new ArrayList<>();
        /**
         * First add the variant type, this will be the first display item
         */
        resultList.add(variantType.toString());
        /**
         * For non-missense mutations, just return the overall type)
         */
        if (variantType != VariantType.MISSENSE) {
            resultList.add(pathogenicityScore.toString());
            return resultList;
        }

        MutationTasterScore mutTasterScore = pathogenicityData.getMutationTasterScore();
        if (mutTasterScore != null) {
            resultList.add(mutTasterScore.toString());
        } else {
            resultList.add("Mutation Taster: .");
        }
        PolyPhenScore polyphenScore = pathogenicityData.getPolyPhenScore();
        if (polyphenScore != null) {
            resultList.add(polyphenScore.toString());
        } else {
            resultList.add("Polyphen2: .");
        }
        SiftScore siftScore = pathogenicityData.getSiftScore();
        if (siftScore != null) {
            resultList.add(siftScore.toString());
        } else {
            resultList.add("SIFT: .");
        }
        resultList.add(pathogenicityScore.toString());
        
        return resultList;
    }

    public String getHTMLCode(VariantEvaluation variantEvaluation) {
        PathogenicityData pathogenicityData = variantEvaluation.getPathogenicityData();
        FilterScore pathogenicityScore = variantEvaluation.getFilterScore(FilterType.PATHOGENICITY_FILTER);
        VariantType variantType = variantEvaluation.getVariantType();

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("<ul><li><b>%s</b></li>\n", variantType));
        /**
         * For non-missense mutations, just return the overall type)
         */
        MutationTasterScore mutTasterScore = pathogenicityData.getMutationTasterScore();
        if (mutTasterScore != null) {
            sb.append(String.format("<li>%s</li>\n", mutTasterScore));
        }
        PolyPhenScore polyphenScore = pathogenicityData.getPolyPhenScore();
        if (polyphenScore != null) {
            sb.append(String.format("<li>%s</li>\n", polyphenScore));
        }
        SiftScore siftScore = pathogenicityData.getSiftScore();
        if (siftScore != null) {
            sb.append(String.format("<li>%s</li>\n", siftScore));
        }
        sb.append(String.format("<li>%s</li>\n", pathogenicityScore));
        sb.append("</ul>\n");
        return sb.toString();
    }

}
