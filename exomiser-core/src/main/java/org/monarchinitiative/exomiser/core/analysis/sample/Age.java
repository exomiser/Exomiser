/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.analysis.sample;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Period;
import java.util.Objects;

/**
 * A period of time representing a persons age.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Age {

    private static final Age UNKNOWN = new Age(0, 0, 0);

    private final int years;
    private final int months;
    private final int days;

    private Age(int years, int months, int days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }

    public static Age unknown() {
        return UNKNOWN;
    }

    /**
     * The age of an individual stated in years, months and days. The returned {@linkplain Age} instance will have been
     * normalised so that an input of 1 year 15 months will be returned as an age of 2 years 3 months.
     *
     * @param years
     * @param months
     * @param days
     * @return a normalised Age instance
     */
    public static Age of(int years, int months, int days) {
        Period period = Period.of(years, months, days);
        return Age.of(period);
    }

    // TODO - how to define Ante/Post-natal ages? Need a pre/post natal flag or use a negative value?
    //  e.g. P-8W equals 8 week-old foetus i.e. prior to birth but after 8 weeks of gestation with P-40W being full
    //  term and P0D being a newborn.
    // usually written on referrals as gestation in the format eg. 22+5  (22 weeks + 5days)
    public static Age gestational(int weeks, int days) {
        int totalDays = weeks * 7 + days;
        Period period = Period.of(0, 0, totalDays);
        return Age.of(period);
    }

    /**
     * The age of an individual represented as a {@link Period}. The returned {@linkplain Age} instance will have been
     * normalised so that an input of 1 year 15 months will be returned as an age of 2 years 3 months.
     *
     * @param period {@link Period} representing the age of an individual
     * @return a normalised Age instance
     */
    public static Age of(Period period) {
        Period normalised = Objects.requireNonNull(period.normalized());
        if (normalised.isZero()) {
            return UNKNOWN;
        }
        return new Age(normalised.getYears(), normalised.getMonths(), normalised.getDays());
    }

    /**
     * Parses an ISO8601 period where P designates period, Y  years, M months, W weeks and D days. e.g.
     * P1D - 1 day
     * P2W3D - 2 weeks, 3 days
     * P2M3D - 2 months, 3 days
     * P1Y2M3D - 1 year, 2 months, 3 days
     * <p>
     * This method delegates to the {@link Period} class for actual parsing. See the javadoc there for specifics. The
     * returned {@linkplain Age} instance will have been normalised so that an input of P1Y15M will be returned as an
     * age of 2 years 3 months.
     *
     * @param iso8601period ISO8601 string period representing the age of an individual
     * @return a normalised Age instance
     */
    public static Age parse(String iso8601period) {
        Period period = Period.parse(iso8601period);
        return Age.of(period);
    }

    public int getYears() {
        return years;
    }

    public int getMonths() {
        return months;
    }

    public int getDays() {
        return days;
    }

    public Period toPeriod() {
        return Period.of(years, months, days);
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Age)) return false;
        Age age = (Age) o;
        return years == age.years &&
                months == age.months &&
                days == age.days;
    }

    @Override
    public int hashCode() {
        return Objects.hash(years, months, days);
    }

    @Override
    public String toString() {
        return "Age{" +
                "years=" + years +
                ", months=" + months +
                ", days=" + days +
                '}';
    }
}
