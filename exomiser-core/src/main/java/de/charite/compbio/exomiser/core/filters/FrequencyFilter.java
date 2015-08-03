package de.charite.compbio.exomiser.core.filters;

import de.charite.compbio.exomiser.core.model.frequency.Frequency;
import de.charite.compbio.exomiser.core.model.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VariantFilter variants according to their frequency.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.09 (April 28, 2013)
 */
public class FrequencyFilter implements VariantFilter {

    /**
     * Threshold for filtering. Retain only those variants whose frequency
     * (expressed as a percentage) is below this threshold. The default value is
     * 100%, i.e., no filtering out.
     */
    private final float maxFreq;

    private static final Logger logger = LoggerFactory.getLogger(FrequencyFilter.class);

    private static final FilterType filterType = FilterType.FREQUENCY_FILTER;

    private final FilterResult passesFilter = new PassFilterResult(filterType);
    private final FilterResult failsFilter = new FailFilterResult(filterType);

    /**
     * Creates a runFilter with a maximum frequency threshold for variants.
     *
     * @param maxFreq sets the maximum frequency threshold (percent value) of
     *                the minor allele required to pass the filer. For example a value of 1
     *                will set the threshold of the minor allele frequency to under 1%.
     */
    public FrequencyFilter(float maxFreq) {
        if (maxFreq < 0f || maxFreq > 100f) {
            throw new IllegalArgumentException(String.format("Illegal value for maximum frequency threshold: %2f. Value should be between 0 and 100", maxFreq));
        }
        this.maxFreq = maxFreq;
    }

    public float getMaxFreq() {
        return maxFreq;
    }

    /**
     * Flag to output results of filtering against frequency with Thousand
     * Genomes and ESP data.
     */
    @Override
    public FilterType getFilterType() {
        return filterType;
    }

    /**
     * Returns true if the {@code VariantEvaluation} passes the runFilter.
     * Returns a PASS or FAIL on the basis of the estimated frequency of the
     * variant in the population. If the variant in question has a higher
     * frequency than the threshold in either the dbSNP data or the ESP data,
     * then we flag it as failed.
     *
     * @param variantEvaluation
     * @return
     */
    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        //frequency data is derived from the database - consequently make sure the data has been fetched otherwise the
        //score will be the same for all variants.
        float variantFrequencyScore = frequencyData.getScore();

        if (passesFilter(frequencyData)) {
            return passesFilter;
        }
        return failsFilter;
    }

    /**
     * This method returns false if the variant is more common than the
     * threshold in any one of the dbSNP data, or the ESP data for European
     * Americans, African Americans, or All comers.
     *
     * @param frequencyData
     * @return true if the variant being analyzed is rarer than the threshold
     */
    protected boolean passesFilter(FrequencyData frequencyData) {

        for (Frequency frequency : frequencyData.getKnownFrequencies()) {
            if (frequency.isOverThreshold(maxFreq)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Float.floatToIntBits(this.maxFreq);
        hash = 29 * hash + Objects.hashCode(FrequencyFilter.filterType);
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
        final FrequencyFilter other = (FrequencyFilter) obj;
        if (Float.floatToIntBits(this.maxFreq) != Float.floatToIntBits(other.maxFreq)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FrequencyFilter{" + "maxFreq=" + maxFreq + '}';
    }
}
