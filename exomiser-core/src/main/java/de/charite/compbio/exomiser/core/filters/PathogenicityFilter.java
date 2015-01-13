package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.VariantTypePathogenicityScores;

import jannovar.common.VariantType;
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
 * @version 0.09 (29 December, 2012).
 */
public class PathogenicityFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(PathogenicityFilter.class);

    private static final FilterType filterType = FilterType.PATHOGENICITY_FILTER;

    public static final float DEFAULT_PATHOGENICITY_THRESHOLD = 0.5f;

    private final boolean removePathFilterCutOff;

    /**
     * Produces a Pathogenicity filter using a user-defined pathogenicity
     * threshold. The removePathFilterCutOff parameter will apply the pathogenicity
     * scoring, but no further filtering will be applied so all variants will
     * pass irrespective of their score.
     *
     * @param removePathFilterCutOff
     */
    public PathogenicityFilter(boolean removePathFilterCutOff) {
        this.removePathFilterCutOff = removePathFilterCutOff;
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
     * VariantFilter variants based on their calculated pathogenicity. Those
     * that pass have a pathogenicity score assigned to them. The failed ones
     * are deemed to be non-pathogenic and marked as such.
     *
     */
    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        PathogenicityData pathData = variantEvaluation.getPathogenicityData();
        VariantType variantType = variantEvaluation.getVariantType();

        float filterScore = calculateFilterScore(variantType, pathData);

        if (removePathFilterCutOff) {
            return returnPassResult(filterScore);
        }
        if (variantIsPredictedPathogenic(variantType)) {
            return returnPassResult(filterScore);
        }
        return returnFailResult(filterScore);
    }

    /**
     * Creates the PathogenicityScore data
     *
     * @param variantType
     * @param pathogenicityData
     * @return
     */
    protected float calculateFilterScore(VariantType variantType, PathogenicityData pathogenicityData) {
        if (variantType == VariantType.MISSENSE) {
            return returnMissenseScore(pathogenicityData);
        } else {
            //return the default score - in time we might want to use the predicted score if there are any and handle things like the missense variants.
            return VariantTypePathogenicityScores.getPathogenicityScoreOf(variantType);
        }
    }

    private float returnMissenseScore(PathogenicityData pathogenicityData) {
        if (pathogenicityData.hasPredictedScore()) {
            return returnMostPathogenicPredictedScore(pathogenicityData);
        }
        return VariantTypePathogenicityScores.DEFAULT_MISSENSE_SCORE;
    }

    private float returnMostPathogenicPredictedScore(PathogenicityData pathogenicityData) {
        PathogenicityScore mostPathogenicPredictedScore = pathogenicityData.getMostPathogenicScore();
        //Thanks to SIFT being about tolerance rather than pathogenicity, the score is inverted
        if (mostPathogenicPredictedScore.getClass() == SiftScore.class) {
            return 1 - mostPathogenicPredictedScore.getScore();
        }
        return mostPathogenicPredictedScore.getScore();
    }

    /**
     * @param variantType
     * @param pathData
     * @return true if the variant being analysed passes the runFilter (e.g.,
     * has high quality )
     */
    protected boolean variantIsPredictedPathogenic(VariantType variantType) {
        if (variantType == VariantType.MISSENSE) {
            //we're making the assumption that a miissense variant is always 
            //potentially pathogenic as the prediction scores are predictions, 
            //we'll leave it up to the user to decide
            return true;
        } else {
            return VariantTypePathogenicityScores.getPathogenicityScoreOf(variantType) >= DEFAULT_PATHOGENICITY_THRESHOLD;
        }
    }

    private FilterResult returnPassResult(float filterScore) {
        // We passed the filter (Variant is predicted pathogenic).
        FilterResult passResult = new PathogenicityFilterResult(filterScore, FilterResultStatus.PASS);
        return passResult;
    }

    private FilterResult returnFailResult(float filterScore) {
        // Variant is not predicted pathogenic, return failed.
        FilterResult failResult = new PathogenicityFilterResult(filterScore, FilterResultStatus.FAIL);
        return failResult;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(PathogenicityFilter.filterType);
        hash = 97 * hash + (this.removePathFilterCutOff ? 1 : 0);
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
        final PathogenicityFilter other = (PathogenicityFilter) obj;
        return this.removePathFilterCutOff == other.removePathFilterCutOff;
    }

    @Override
    public String toString() {
        return String.format("%s filter: removePathFilterCutOff=%s", filterType, removePathFilterCutOff);
    }

}
