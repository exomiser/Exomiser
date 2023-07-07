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
import java.time.chrono.ChronoPeriod;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A period of time representing a persons age.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public interface Age {

    public static Age unknown() {
        return PostnatalAge.unknown();
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
        return GestationalAge.of(weeks, days);
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
            return unknown();
        }
        return new PostnatalAge(normalised.getYears(), normalised.getMonths(), normalised.getDays());
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

    /**
     * Flag indicating that the age is a gestational rather than post-natal age.
     *
     * @return true in the case that the age instance is a gestational age
     */
    boolean isGestationalAge();

    public int years();

    public int months();

    public int days();

    default Period toPeriod() {
        return Period.of(years(), months(), days()).normalized();
    }

    default boolean isUnknown() {
        return this == PostnatalAge.UNKNOWN;
    }


    record GestationalAge(int weeks, int days) implements Age {

        public static GestationalAge of(int weeks, int days) {
            int totalDays = weeks * 7 + days;
            Period period = Period.of(0, 0, totalDays);
            return new GestationalAge(weeks, days);
        }

        @Override
        public boolean isGestationalAge() {
            return true;
        }

        @Override
        public int years() {
            return 0;
        }

        @Override
        public int months() {
            return days / (int) ChronoUnit.MONTHS.getDuration().toDays();
        }

        @Override
        public String toString() {
            return weeks + "+" + days;
        }
    }

    record PostnatalAge(int years, int months, int days) implements Age {

        private static final Age UNKNOWN = new PostnatalAge(0, 0, 0);

        public static Age unknown() {
            return UNKNOWN;
        }

        @Override
        public boolean isGestationalAge() {
            return false;
        }
    }
}
