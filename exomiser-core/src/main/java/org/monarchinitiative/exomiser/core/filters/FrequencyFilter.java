/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.filters;

import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * VariantFilter variants according to their frequency.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.09 (April 28, 2013)
 */
public class FrequencyFilter implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(FrequencyFilter.class);

    private static final FilterType filterType = FilterType.FREQUENCY_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);

    /**
     * Threshold for filtering. Retain only those variants whose frequency
     * (expressed as a percentage) is below this threshold. The default value is
     * 100%, i.e., no filtering out.
     */
    private final float maxFreq;

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
        if (variantEvaluation.isWhiteListed()) {
            return PASS;
        }
        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        //frequency data is derived from the database - consequently make sure the data has been fetched otherwise the
        //variant will always pass the filter.

        if (frequencyData.hasFrequencyOverPercentageValue(maxFreq)) {
            return FAIL;
        }
        return PASS;
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
        return Float.floatToIntBits(this.maxFreq) == Float.floatToIntBits(other.maxFreq);
    }

    @Override
    public String toString() {
        return "FrequencyFilter{" + "maxFreq=" + maxFreq + '}';
    }
}
