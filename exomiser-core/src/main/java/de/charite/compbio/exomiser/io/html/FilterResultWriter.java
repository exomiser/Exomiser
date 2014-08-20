/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.io.html;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.List;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface FilterResultWriter {
    
    /**
     * @param variantEvaluation
     * @return A string with a summary of the filtering results .
     */
    public String getFilterResultSummary(VariantEvaluation variantEvaluation);

    /**
     * @param variantEvaluation
     * @return A list with detailed results of filtering. The list is intended
     * to be displayed as an HTML list if desired.
     */
    public List<String> getFilterResultList(VariantEvaluation variantEvaluation);

    /**
     * @param variantEvaluation
     * @return HTML code for a cell representing the current triage result.
     */
    public String getHTMLCode(VariantEvaluation variantEvaluation);
}
