package de.charite.compbio.exomiser.core.filter;

import de.charite.compbio.exomiser.core.frequency.Frequency;
import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter variants according to their frequency. The Frequency is retrieved from
 * our database and comes from dbSNP (see
 * {@link exomizer.io.dbSNP2FrequencyParser dbSNP2FrequencyParser} and
 * {@link exomizer.io.ESP2FrequencyParser ESP2FrequencyParser}), and the
 * frequency data are expressed as percentages.
 *
 * @author Peter N Robinson
 * @version 0.09 (April 28, 2013)
 */
public class FrequencyFilter implements Filter {

    /**
     * Threshold for filtering. Retain only those variants whose frequency
     * (expressed as a percentage) is below this threshold. The default value is
     * 100%, i.e., no filtering out.
     */
    private float maxFreq = 100.0f;

    private final Logger logger = LoggerFactory.getLogger(FrequencyFilter.class);

    private final FilterType filterType = FilterType.FREQUENCY_FILTER;

    private static final FrequencyFilterScore FAIL_FREQUENCY_FILTER_SCORE = new FrequencyFilterScore(0f);
    private static final FrequencyFilterScore PASS_FREQUENCY_FILTER_SCORE = new FrequencyFilterScore(1f);

    /**
     * Filter out variants if they are represented at all in dbSNP or ESP,
     * regardless of frequency.
     */
    private boolean strictFiltering = false;

    /**
     * Creates a filter with a maximum frequency threshold for variants.
     *
     * @param maxFreq sets the maximum frequency threshold (percent value) of
     * the minor allele required to pass the filer. For example a value of 1
     * will set the threshold of the minor allele frequency to under 1%.
     * @param filterOutAllKnownVariants removes all variants found in the dbSNP
     * or in the ESP database regardless of their frequency.
     *
     */
    public FrequencyFilter(float maxFreq, boolean filterOutAllKnownVariants) {
        if (maxFreq < 0f || maxFreq > 100f) {
            throw new IllegalArgumentException(String.format("Illegal value for maximum frequency threshold: %2f. Value should be between 0 and 100", maxFreq));
        }
        this.maxFreq = maxFreq;
        this.strictFiltering = filterOutAllKnownVariants;
    }

    public float getMaxFreq() {
        return maxFreq;
    }

    public void setMaxFreq(float maxFreq) {
        this.maxFreq = maxFreq;
    }

    public boolean filterOutAllDbsnp() {
        return strictFiltering;
    }

    public void setFilterOutAllDbsnp(boolean removeDbSnp) {
        this.strictFiltering = removeDbSnp;
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
     * Filter out list of variants on the basis of the estimated frequency of
     * the variant in the population. If the variant in question has a higher
     * frequency than the threshold in either the dbSNP data or the ESP data,
     * then we flag it as failed.
     *
     * @param variantEvaluationtList a list of Variants to be tested for rarity.
     */
    @Override
    public void filterVariants(List<VariantEvaluation> variantEvaluationtList) {

        for (VariantEvaluation ve : variantEvaluationtList) {
            filterVariant(ve);
        }
    }

    /**
     * Returns true if the {@code VariantEvaluation} passes the filter.
     *
     * @param variantEvaluation
     * @return
     */
    @Override
    public boolean filterVariant(VariantEvaluation variantEvaluation) {
        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        //frequency data is derived from the database and depending on whether the full-analysis option has been specified the data may not have been set
        //by the time it gets here. It should have been, so this will issue a warning.
        if (checkFrequencyDataIsNotNull(frequencyData, variantEvaluation, FAIL_FREQUENCY_FILTER_SCORE)) {
            return false;
        }
        
        FilterScore filterScore = calculateFilterScore(frequencyData);
        
        if (strictFiltering) {
            if (frequencyData.representedInDatabase()) {
                //not rare enough! 
                variantEvaluation.addFailedFilter(filterType, filterScore);
                return false;
            }
            //wow - a variant no one has seen before this could be interesting! 
            variantEvaluation.addPassedFilter(filterType, filterScore);
            return true;
        } else {
            if (passesFilter(frequencyData)) {
                // We passed the filter (Variant is rare).
                variantEvaluation.addPassedFilter(filterType, filterScore);
                return true;
            } else {
                // Variant is not rare, fail the filter.
                variantEvaluation.addFailedFilter(filterType, filterScore);
                return false;
            }
        }
    }

    private boolean checkFrequencyDataIsNotNull(FrequencyData frequencyData, VariantEvaluation variantEvaluation, FilterScore filterScore) {
        if (frequencyData == null) {
            // frequencyData has not been set, fail the filter.
            variantEvaluation.addFailedFilter(filterType, filterScore);
            logger.warn("{} frequency data has not been set - {} filter failed.", variantEvaluation.getChromosomalVariant(), filterType);
            return true;
        }
        return false;
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

    /**
     * This method returns a numerical value that is closer to one, the rarer
     * the variant is. If a variant is not entered in any of the four data
     * sources, it returns one (highest score). Otherwise, it identifies the
     * maximum MAF in any of the databases, and returns a score that depends on
     * the MAF. Note that the frequency is expressed as a percentage.
     *
     * @param frequencyData
     *
     * @return return a float representation of the filter result [0..1]. If the
     * result is boolean, return 0.0 for false and 1.0 for true
     */
    protected FrequencyFilterScore calculateFilterScore(FrequencyData frequencyData) {

        float max = frequencyData.getMaxFreq();

        if (max <= 0) {
            return PASS_FREQUENCY_FILTER_SCORE;
        } else if (max > 2) {
            return FAIL_FREQUENCY_FILTER_SCORE;
        } else {
            float score = 1f - (0.13533f * (float) Math.exp(max));
            return new FrequencyFilterScore(score);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Float.floatToIntBits(this.maxFreq);
        hash = 29 * hash + Objects.hashCode(this.filterType);
        hash = 29 * hash + (this.strictFiltering ? 1 : 0);
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
        if (this.filterType != other.filterType) {
            return false;
        }
        if (this.strictFiltering != other.strictFiltering) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s filter: Maximum frequency threshold=%s filter out dbSNP and ESP=%s", filterType, maxFreq, strictFiltering);
    }
}
