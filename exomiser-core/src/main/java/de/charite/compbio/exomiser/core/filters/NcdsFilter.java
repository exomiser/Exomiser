package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.VariantTypePathogenicityScores;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VariantFilter variants according to their predicted pathogenicity. There are
 * two components to this, which may better be separated in later versions of
 * this software, but I think there are more advantages to keeping them all in
 * one class.
 * <P>
 * There are variants such as splice site variants, which we can assume are in
 * general pathogenic. We at the moment do not need to use any particular
 * software to evaluate this, we merely take the variant class from the Jannovar
 * code.
 * <P>
 * For missense mutations, we will use the predictions of MutationTaster,
 * polyphen, and SIFT taken from the data from the dbNSFP project.
 * <P>
 * The code therefore removes mutations judged not to be pathogenic (intronic,
 * etc.), and assigns each other mutation an overall pathogenicity score defined
 * on the basis of "medical genetic intuition".
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.09 (29 December, 2012).
 */
public class NcdsFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(NcdsFilter.class);
    private static final FilterType filterType = FilterType.NCDS_FILTER;
    private final FilterResult passesFilter = new PassFilterResult(filterType);
    private final FilterResult failsFilter = new FailFilterResult(filterType);

    public static final float DEFAULT_PATHOGENICITY_THRESHOLD = 0.5f;
    
    private final boolean keepNonPathogenic;

    /**
     * Produces a Pathogenicity filter using a user-defined pathogenicity
     * threshold. The keepNonPathogenic parameter will apply the
     * pathogenicity scoring, but no further filtering will be applied so all
     * variants will pass irrespective of their score.
     *
     * @param keepNonPathogenic
     */
    public NcdsFilter(boolean keepNonPathogenic) {
        this.keepNonPathogenic = keepNonPathogenic;
    }

    /**
     * Flag to output results of filtering against CADD.
     */
    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * VariantFilter variants based on their calculated pathogenicity. Those
     * that pass have a pathogenicity score assigned to them. The failed ones
     * are deemed to be non-pathogenic and marked as such.
     *
     * @return 
     */
    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        PathogenicityData pathData = variantEvaluation.getPathogenicityData();
        VariantEffect variantEffect = variantEvaluation.getVariantEffect();

        if (keepNonPathogenic) {
            return passesFilter;
        }
        if (variantIsPredictedPathogenic(variantEffect, pathData)) {
            return passesFilter;
        }
        return failsFilter;
    }

    /**
     * @param variantEffect
     * @param pathData
     * @return true if the variant being analysed passes the runFilter (e.g., has high quality )
     */
    private boolean variantIsPredictedPathogenic(VariantEffect variantEffect, PathogenicityData pathogenicityData) {
        if (pathogenicityData.hasPredictedScore(PathogenicitySource.NCDS)) {
            return true;
        }
        else {
            return VariantTypePathogenicityScores.getPathogenicityScoreOf(variantEffect) >= DEFAULT_PATHOGENICITY_THRESHOLD;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(NcdsFilter.filterType);
        hash = 97 * hash + (this.keepNonPathogenic ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NcdsFilter other = (NcdsFilter) obj;
        return this.keepNonPathogenic == other.keepNonPathogenic;
    }

    @Override
    public String toString() {
        return "NCDSFilter{" + "removePathFilterCutOff=" + keepNonPathogenic + '}';
    }

}
