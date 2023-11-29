package org.monarchinitiative.exomiser.core.filters;


import java.util.Objects;

/**
 * A simple count of the number of passed and failed genes/variants for a given {@link FilterType}.
 *
 * @since 13.4.0
 */
public final class FilterResultCount {

    private final FilterType filterType;
    private final int passCount;
    private final int failCount;

    public FilterResultCount(FilterType filterType, int passCount, int failCount) {
        this.filterType = filterType;
        this.passCount = passCount;
        this.failCount = failCount;
    }

    public FilterType filterType() {
        return filterType;
    }

    public int passCount() {
        return passCount;
    }

    public int failCount() {
        return failCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterResultCount)) return false;
        FilterResultCount that = (FilterResultCount) o;
        return passCount == that.passCount &&
               failCount == that.failCount &&
               filterType == that.filterType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterType, passCount, failCount);
    }

    @Override
    public String toString() {
        return "FilterCount{" +
               "filterType=" + filterType +
               ", passCount=" + passCount +
               ", failCount=" + failCount +
               '}';
    }
}
