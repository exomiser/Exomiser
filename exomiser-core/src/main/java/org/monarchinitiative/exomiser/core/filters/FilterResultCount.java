package org.monarchinitiative.exomiser.core.filters;


/**
 * A simple count of the number of passed and failed genes/variants for a given {@link FilterType}.
 *
 * @since 13.4.0
 */
public record FilterResultCount(FilterType filterType, int passCount, int failCount) {

    @Override
    public String toString() {
        return "FilterResultCount{" +
               "filterType=" + filterType +
               ", passCount=" + passCount +
               ", failCount=" + failCount +
               '}';
    }
}
