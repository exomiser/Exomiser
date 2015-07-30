package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityScore;
import de.charite.compbio.exomiser.core.model.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
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
public class PathogenicityFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(PathogenicityFilter.class);

    private static final FilterType filterType = FilterType.PATHOGENICITY_FILTER;

    private final boolean keepNonPathogenic;

    /**
     * Produces a Pathogenicity filter using a user-defined pathogenicity
     * threshold. The keepNonPathogenic parameter will apply the pathogenicity
     * scoring, but no further filtering will be applied so all variants will
     * pass irrespective of their score.
     *
     * @param keepNonPathogenic
     */
    public PathogenicityFilter(boolean keepNonPathogenic) {
        this.keepNonPathogenic = keepNonPathogenic;
    }

    public boolean keepNonPathogenic() {
        return keepNonPathogenic;
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
        VariantEffect variantEffect = variantEvaluation.getVariantEffect();

        //TODO - move the score into GeneScorer or a new VariantScorer. The filter should just be filtering. variantEvaluation.getPathogenicityScore()
        float variantPathogenicityScore = calculateVariantPathogenicityScore(variantEffect, pathData);

        if (keepNonPathogenic) {
            return returnPassResult(variantPathogenicityScore);
        }
        if (variantEvaluation.isPredictedPathogenic()) {
            return returnPassResult(variantPathogenicityScore);
        }
        return returnFailResult(variantPathogenicityScore);
    }

    /**
     * Creates the PathogenicityScore data
     *
     * @param variantEffect
     * @param pathogenicityData
     * @return
     */
    //TODO: move into VariantEvaluation - rename to getPathogenicityScore also create getFrequencyScore method
    protected float calculateVariantPathogenicityScore(VariantEffect variantEffect, PathogenicityData pathogenicityData) {
        if (variantEffect == VariantEffect.MISSENSE_VARIANT) {
            return returnMissenseScore(pathogenicityData);
        } else {
            //return the default score - in time we might want to use the predicted score if there are any and handle things like the missense variants.
            return VariantTypePathogenicityScores.getPathogenicityScoreOf(variantEffect);
        }
    }

    //TODO: move into PathogenicityData - make the method signature analogous to the new FrequencyData method
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

    private FilterResult returnPassResult(float filterScore) {
        // We passed the filter (Variant is predicted pathogenic).
        return new PassFilterResult(filterType, filterScore);
    }

    private FilterResult returnFailResult(float filterScore) {
        // Variant is not predicted pathogenic, return failed.
        return new FailFilterResult(filterType, filterScore);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(PathogenicityFilter.filterType);
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
        final PathogenicityFilter other = (PathogenicityFilter) obj;
        return this.keepNonPathogenic == other.keepNonPathogenic;
    }

    @Override
    public String toString() {
        return "PathogenicityFilter{" + "keepNonPathogenic=" + keepNonPathogenic + '}';
    }

}
