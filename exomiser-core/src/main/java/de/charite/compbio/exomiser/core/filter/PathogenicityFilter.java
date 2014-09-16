package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.pathogenicity.MutationTasterScore;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityScore;
import de.charite.compbio.exomiser.core.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.pathogenicity.SiftScore;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;

import static de.charite.compbio.exomiser.core.pathogenicity.VariantTypePathogenicityScores.*;
import jannovar.common.VariantType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VariantFilter variants according to their predicted pathogenicity. There are two
 * components to this, which may better be separated in later versions of this
 * software, but I think there are more advantages to keeping them all in one
 * class.
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

    private final Logger logger = LoggerFactory.getLogger(PathogenicityFilter.class);

    private final FilterType filterType = FilterType.PATHOGENICITY_FILTER;

    //keep this as a constant for now, however it might be nicer to let the user decide on this?
    private static final float PATHOGENICITY_SCORE_THRESHOLD = 0.5f;

    private boolean keepNonPathogenicMissense = false;

    public PathogenicityFilter(boolean keepNonPathogenicMissense) {
        this.keepNonPathogenicMissense = keepNonPathogenicMissense;
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
     * VariantFilter variants based on their calculated pathogenicity. Those that pass
     * have a pathogenicity score assigned to them. The failed ones are deemed
     * to be non-pathogenic and marked as such.
     *
     * @param variantList
     */
    @Override
    public void filter(List<VariantEvaluation> variantList) {

        for (VariantEvaluation ve : variantList) {
            filter(ve);
        }
    }

    @Override
    public boolean filter(VariantEvaluation variantEvaluation) {
        PathogenicityData pathData = variantEvaluation.getPathogenicityData();
        VariantType variantType = variantEvaluation.getVariantType();
        
        FilterScore filterScore = calculateFilterScore(variantType, pathData);
        
        if (passesFilter(variantType, pathData)) {
            // We passed the filter (Variant is predicted pathogenic).
            return variantEvaluation.addPassedFilter(filterType, filterScore);
        }
        // Variant is not predicted pathogenic, mark as failed.
        return variantEvaluation.addFailedFilter(filterType, filterScore);
    }

    /**
     * @param variantType
     * @param pathData
     * @return true if the variant being analyzed passes the filter (e.g., has
     * high quality )
     */
    protected boolean passesFilter(VariantType variantType, PathogenicityData pathData) {
        if (variantType == VariantType.MISSENSE) {
            if (keepNonPathogenicMissense) {
                //We don't care about the score - we want all Missense mutations included regrardless of how benign or pathogenic they are.
                return true;//no SIFT, PolyPhen, MT filtering
            } 
            if (!pathData.hasPredictedScore()) {
//                logger.info("MISSENSE variant score PASSES: {} over threshold of {}", pathScores.getScore(), PATHOGENICITY_SCORE_THRESHOLD);
                //assume that all missense variants are pathogenic if we have no predictions
                return true;
            }
            //otherwise check the scores - only one has to pass
            SiftScore sift = pathData.getSiftScore();
            if (sift != null && sift.getScore() < SiftScore.SIFT_THRESHOLD) {
//                logger.info("SIFT score PASSES: {} under threshold of {}", pathScores.getSift(), SIFT_THRESHOLD);
                return true;
            }
            PolyPhenScore poly = pathData.getPolyPhenScore();
            if (poly != null && poly.getScore() > PolyPhenScore.POLYPHEN_THRESHOLD) {
//                logger.info("PolyPhen score PASSES: {} over threshold of {}", pathScores.getPolyPhen(), POLYPHEN_THRESHOLD);
                return true;
            }
            MutationTasterScore mut = pathData.getMutationTasterScore();
            if (mut != null && mut.getScore() > MutationTasterScore.MTASTER_THRESHOLD) {
//                logger.info("Mutation taster score PASSES: {} over threshold of {}", pathScores.getMutationTaster(), MTASTER_THRESHOLD);
                return true;
            }
            
            return false;

        } else {
            return getPathogenicityScoreOf(variantType) >= PATHOGENICITY_SCORE_THRESHOLD;
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.filterType);
        hash = 97 * hash + (this.keepNonPathogenicMissense ? 1 : 0);
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
        if (this.filterType != other.filterType) {
            return false;
        }
        if (this.keepNonPathogenicMissense != other.keepNonPathogenicMissense) {
            return false;
        }
        return true;
    }

    /**
     * Creates the PathogenicityScore data
     *
     * @param variantType
     * @param pathogenicityData
     * @return
     */
    protected FilterScore calculateFilterScore(VariantType variantType, PathogenicityData pathogenicityData) {
        //TODO: having a getVariantTypePathogenicityScore() method might be better in the PathogenicityData class for use later in the controller layer too
        if (variantType != VariantType.MISSENSE) {
            float pathScore = getPathogenicityScoreOf(variantType);
            return new PathogenicityFilterScore(pathScore);
        }
        float pathScore = findMostPathogenicScore(pathogenicityData);
        return new PathogenicityFilterScore(pathScore);
    }

    //TODO: this might be better in the PathogenicityData class for use later in the controller layer too
    protected float findMostPathogenicScore(PathogenicityData pathogenicityData) {

        List<PathogenicityScore> pathScores = pathogenicityData.getKnownPathogenicityScores();
        logger.debug("Finding most pathogenic score from : {}", pathScores);
        if (pathScores.isEmpty()) {
            return DEFAULT_MISSENSE_SCORE;
        }

        Collections.sort(pathScores);
        PathogenicityScore mostPathogenic = pathScores.get(0);
        logger.debug("Most pathogenic score is: {} Score = {}", mostPathogenic, mostPathogenic.getScore());
        //Thanks to SIFT being about tolerance rather than pathogenicity the score is inverted
        if (mostPathogenic.getClass() == SiftScore.class) {
            return 1 - mostPathogenic.getScore();
        }
        return mostPathogenic.getScore();

    }

    @Override
    public String toString() {
        return String.format("%s filter: keepNonPathogenicMissense=%s", filterType, keepNonPathogenicMissense);
    }

}
