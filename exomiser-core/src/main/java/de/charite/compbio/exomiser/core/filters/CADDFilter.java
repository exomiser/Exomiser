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
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class CaddFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(CaddFilter.class);

    private static final FilterType filterType = FilterType.CADD_FILTER;

    private final FilterResult passesFilter = new PassFilterResult(filterType);
    private final FilterResult failsFilter = new FailFilterResult(filterType);

    public static final float DEFAULT_PATHOGENICITY_THRESHOLD = 0.5f;

    private final boolean keepNonPathogenic;

    /**
     * Produces a Pathogenicity filter using a user-defined pathogenicity
     * threshold. The keepNonPathogenic parameter will apply the pathogenicity
     * scoring, but no further filtering will be applied so all variants will
     * pass irrespective of their score.
     *
     * @param keepNonPathogenic
     */
    public CaddFilter(boolean keepNonPathogenic) {
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
     * @param pathogenicityData
     * @return true if the variant being analysed passes the runFilter (e.g.,
     * has high quality )
     */
    protected boolean variantIsPredictedPathogenic(VariantEffect variantEffect, PathogenicityData pathogenicityData) {
        if (pathogenicityData.hasPredictedScore(PathogenicitySource.CADD)) {
            return true;
        } else {
            return VariantTypePathogenicityScores.getPathogenicityScoreOf(variantEffect) >= DEFAULT_PATHOGENICITY_THRESHOLD;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(CaddFilter.filterType);
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
        final CaddFilter other = (CaddFilter) obj;
        return this.keepNonPathogenic == other.keepNonPathogenic;
    }

    @Override
    public String toString() {
        return "CADDFilter{" + "removePathFilterCutOff=" + keepNonPathogenic + '}';
    }

}
