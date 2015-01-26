package de.charite.compbio.exomiser.core.prioritisers;

import java.util.ArrayList;

import jannovar.common.Constants;
import java.util.List;

/**
 * Filter Variants on the basis of OWLSim phenotypic comparisons between the HPO
 * clinical phenotypes associated with the disease being sequenced and MP
 * annotated MGI mouse models. The MGIPhenodigmTriage is created by the
 * MGIPhenodigmFilter, one for each tested variant. The MGIPhenodigmTriage
 * object can be used to ask whether the variant passes the filter, in this case
 * whether it the mouse gene scores greater than the threshold in. If no
 * information is available the filter is not applied (ergo the Variant does not
 * fail the filter).
 * <P>
 * This code was extended on Feb 1, 2013 to show links to the MGI webpage for
 * the model in question.
 *
 * @author Damian Smedley
 * @version 0.06 (April 22, 2013).
 */
public class ExomiserMousePriorityResult implements PriorityResult {

    /**
     * The phenodigm score as calculated by OWLsim. This score indicates the
     * similarity between a humam disease and the phenotype of a genetically
     * modified mouse model.
     */
    private final float phenodigmScore;
    /**
     * The MGI id of the model most similar to the gene being analysed. For
     * instance, the MGI id MGI:101757 corresponding to the webpage
     * {@code http://www.informatics.jax.org/marker/MGI:101757} describes the
     * gene Cfl1 (cofilin 1, non-muscle) and the phenotypic features associated
     * with the several mouse models that have been made to investigate this
     * gene.
     */
    private final String mgiId;
    /**
     * The gene symbol corresponding to the mouse gene, e.g., Cfl1.
     */
    private final String geneSymbol;

    /**
     * @param mgiGeneId An ID from Mouse Genome Informatics such as MGI:101757
     * @param geneSymbol The corresponding gene symbol, e.g., Gfl1
     * @param phenodigmScore the phenodigm score for this gene.
     */
    public ExomiserMousePriorityResult(String mgiGeneId, String geneSymbol, float phenodigmScore) {
        this.mgiId = mgiGeneId;
        this.geneSymbol = geneSymbol;
        this.phenodigmScore = phenodigmScore;
    }

    @Override
    public PriorityType getPriorityType() {
        return PriorityType.EXOMISER_MOUSE_PRIORITY;
    }

    /**
     * @return Relevance score for the current Gene
     */
    @Override
    public float getScore() {
        if (phenodigmScore == Constants.UNINITIALIZED_FLOAT) {
            return 0.1f;// mouse model exists but no hit to this disease
        } else if (phenodigmScore == Constants.NOPARSE_FLOAT) {
            return 0.6f;// no mouse model exists in MGI
        } else {
            return phenodigmScore;
        }
    }

    /**
     * @return A list with detailed results of filtering. The list is intended
     * to be displayed as an HTML list if desired.
     */
    @Override
    public List<String> getFilterResultList() {
        List<String> results = new ArrayList<>();
        if (phenodigmScore == Constants.UNINITIALIZED_FLOAT) {
            results.add("MGI Phenodigm: no hit for these phenotypes");
        } else if (phenodigmScore == Constants.NOPARSE_FLOAT) {
            results.add("MGI Phenodigm: no mouse model for this gene");
        } else {
            results.add(String.format("MGI Phenodigm: (%.3f%%)", 100 * phenodigmScore));
        }
        return results;
    }

    /**
     * @return HTML code with score the Phenodigm score for the current gene or
     * a message if no MGI data was found.
     */
    @Override
    public String getHTMLCode() {
        if (phenodigmScore == Constants.NOPARSE_FLOAT) {
            return "<dl><dt>No mouse model for this gene</dt></dl>";
        } else {
            String link = makeMgiGeneLink();
            //String s1 = String.format("<ul><li>MGI: %s: Phenodigm score: %.3f%%</li></ul>",link,100*MGI_Phenodigm);
            return String.format("<dl><dt>Mouse phenotype data for %s</dt></dl>", link);
        }
    }

    /**
     * This function creates an HTML anchor link for a MGI id, e.g., for
     * MGI:101757 it will create a link to
     * {@code http://www.informatics.jax.org/marker/MGI:101757}.
     */
    private String makeMgiGeneLink() {
        String url = String.format("http://www.informatics.jax.org/marker/%s", mgiId);
        String anchor = String.format("<a href=\"%s\">%s</a>", url, geneSymbol);
        return anchor;
    }

}
