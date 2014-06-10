package de.charite.compbio.exomiser.filter;

import de.charite.compbio.exomiser.dao.TriageDAO;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import jannovar.exome.Variant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter variants according to their predicted pathogenicity. There are two
 * components to this, which may better be separated in later versions of this
 * software, but I think there are more advantages to keeping them all in one
 * class. <P> There are variants such as splice site variants, which we can
 * assume are in general pathogenic. We at the moment do not need to use any
 * particular software to evaluate this, we merely take the variant class from
 * the Jannovar code. <P> For missense mutations, we will use the predictions of
 * MutationTaster, polyphen, and SIFT taken from the data from the dbNSFP
 * project. <P> The code therefore removes mutations judged not to be pathogenic
 * (intronic, etc.), and assigns each other mutation an overall pathogenicity
 * score defined on the basis of "medical genetic intuition".
 *
 * @author Peter N Robinson
 * @version 0.09 (29 December, 2012).
 */
public class PathogenicityFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(PathogenicityFilter.class);

    /**
     * DAO for retrieving Triage data from the database 
     */
    private final TriageDAO triageDao;

    private final FilterType filterType = FilterType.PATHOGENICITY_FILTER;

    /**
     * A flag to indicate we could not retrieve data from the database for some
     * variant. Using the value 200% means that the variant will not fail the
     * ESP filter.
     */
    private static final float NO_ESP_DATA = 2.0f;
    /**
     * A list of errors encountered during the calculation of the pathogenicity
     * score.
     */
    private List<String> error_record = null;
    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private List<String> messages = null;

    public PathogenicityFilter(TriageDAO triageDao, boolean useMisSenseFiltering) {

        this.triageDao = triageDao;

        this.error_record = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.messages.add("Synonymous and non-coding variants removed");
        
        if (useMisSenseFiltering) {
            // Set up the message
            messages.add("Pathogenicity predictions are based on the dbNSFP-normalized values");
            messages.add("Mutation Taster: &gt;0.95 assumed pathogenic, prediction categories not shown");
            messages.add("Polyphen2 (HVAR): \"D\" (&gt; 0.956,probably damaging), \"P\": [0.447-0.955], "
                    + "possibly damaging, and \"B\", &lt;0.447, benign.");
            messages.add("SIFT: \"D\"&lt;0.05, damaging and \"T\"&ge;0.05, tolerated</LI>");
            PathogenicityTriage.setUseMisSenseFiltering(useMisSenseFiltering);
        }
    }

    @Override
    public String getFilterName() {
        return "Pathogenicity filter";
    }

    /**
     * Flag to output results of filtering against polyphen, SIFT, and mutation
     * taster.
     */
    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * Sets the frequency threshold for variants.
     *
     * @param par A frequency threshold, e.g., a string such as "0.02"
     * @deprecated Use constructor arguments
     */
//    @Override
//    @Deprecated
//    public void setParameters(String par) {
//        // Set up the message
//        messages.add("Pathogenicity predictions are based on the dbNSFP-normalized values");
//        messages.add("Mutation Taster: &gt;0.95 assumed pathogenic, prediction categories not shown");
//        messages.add("Polyphen2 (HVAR): \"D\" (&gt; 0.956,probably damaging), \"P\": [0.447-0.955], "
//                + "possibly damaging, and \"B\", &lt;0.447, benign.");
//        messages.add("SIFT: \"D\"&lt;0.05, damaging and \"T\"&ge;0.05, tolerated</LI>");
////        PathogenicityTriage.setUseMisSenseFiltering(par);
//    }

    public void setRemoveSynonomousVariants(boolean removeSynonomousVariants) {
        if (!removeSynonomousVariants) {
            //yeah, not a great name for any of these variables. 
            PathogenicityTriage.keepSynonymousVariants();
            //sets the 
            //PathogenicityTriage.PATHOGENICITY_SCORE_THRESHOLD = 0;
        }
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     * of frequency filtering.
     */
    @Override
    public List<String> getMessages() {
        if (this.error_record.size() > 0) {
            for (String s : error_record) {
                this.messages.add("Error: " + s);
            }
        }
        return this.messages;
    }
    /**
     * Number of variants before filtering
     */
    private int n_before;
    /**
     * Number of variants after filtering
     */
    private int n_after;

    /**
     * Get number of variants before filter was applied
     */
    @Override
    public int getBefore() {
        return this.n_before;
    }

    /**
     * Get number of variants after filter was applied
     */
    @Override
    public int getAfter() {
        return this.n_after;
    }

    /**
     * Remove variants that are deemed to be not-pathogenic, and provide a
     * pathogenicity score for those that survive the filter.
     * @param variantList
     */
    @Override
    public void filterVariants(List<VariantEvaluation> variantList) {
        Iterator<VariantEvaluation> it = variantList.iterator();

        this.n_before = variantList.size();
        while (it.hasNext()) {
            VariantEvaluation ve = it.next();
            Variant v = ve.getVariant();
            //TODO: move the filtering logic into the filter and leave the pathogenicity data in the triage object.
            //Triage is then better named as FilterScore, or perhaps simply Score. 
            //Similarly in the priority package RelevanceScore might be better names PriorityScore or simply Score.
            Triage pt = triageDao.getTriageData(v);
                if (!pt.passesFilter()) {
                    // Variant is not predicted pathogenic, discard it.
                    it.remove();
                } else {
                    // We passed the filter (Variant is predicted pathogenic).
                    ve.addFilterTriage(pt, FilterType.PATHOGENICITY_FILTER);
                }
            }
        this.n_after = variantList.size();
        }
    //Deleted methods from here down
    //moved to {@code de.charite.compbio.exomiser.dao.PathogenicityTriageDAO}

    @Override
    public boolean displayInHTML() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s: ", filterType);
    }
}
