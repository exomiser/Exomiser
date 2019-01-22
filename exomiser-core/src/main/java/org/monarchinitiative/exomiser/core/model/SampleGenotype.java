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

package org.monarchinitiative.exomiser.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Immutable class representing the genotype of a sample, expressed as a phased or un-phased set of {@link AlleleCall}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public class SampleGenotype {

    private static final SampleGenotype EMPTY = new SampleGenotype(false);
    //cached common diploid genotypes - these are going to be super-common in multi-sample cases
    private static final SampleGenotype UNPHASED_DIPLOID_NO_CALL = new SampleGenotype(false, AlleleCall.NO_CALL, AlleleCall.NO_CALL);
    private static final SampleGenotype UNPHASED_DIPLOID_HET = new SampleGenotype(false, AlleleCall.REF, AlleleCall.ALT);
    private static final SampleGenotype UNPHASED_DIPLOID_HOM_REF = new SampleGenotype(false, AlleleCall.REF, AlleleCall.REF);
    private static final SampleGenotype UNPHASED_DIPLOID_HOM_ALT = new SampleGenotype(false, AlleleCall.ALT, AlleleCall.ALT);

    private static final SampleGenotype PHASED_DIPLOID_NO_CALL = new SampleGenotype(true, AlleleCall.NO_CALL, AlleleCall.NO_CALL);
    private static final SampleGenotype PHASED_DIPLOID_HET_REF_ALT = new SampleGenotype(true, AlleleCall.REF, AlleleCall.ALT);
    private static final SampleGenotype PHASED_DIPLOID_HET_ALT_REF = new SampleGenotype(true, AlleleCall.ALT, AlleleCall.REF);
    private static final SampleGenotype PHASED_DIPLOID_HOM_REF = new SampleGenotype(true, AlleleCall.REF, AlleleCall.REF);
    private static final SampleGenotype PHASED_DIPLOID_HOM_ALT = new SampleGenotype(true, AlleleCall.ALT, AlleleCall.ALT);

    private final AlleleCall[] alleleCalls;
    private final boolean phased;

    public static SampleGenotype empty() {
        return EMPTY;
    }

    /**
     * Returns an instance representing an unphased diploid no-call genotype e.g. ./.
     * @return an unphased diploid no-call SampleGenotype
     * @since 11.0.0
     */
    public static SampleGenotype noCall() {
        return UNPHASED_DIPLOID_NO_CALL;
    }

    /**
     * Returns an instance representing an unphased diploid heterozygous genotype e.g. 0/1
     * @return an unphased diploid heterozygous SampleGenotype
     * @since 11.0.0
     */
    public static SampleGenotype het() {
        return UNPHASED_DIPLOID_HET;
    }


    /**
     * Returns an instance representing an unphased diploid homozygous reference genotype e.g. 0/0
     * @return an unphased diploid homozygous reference SampleGenotype
     * @since 11.0.0
     */
    public static SampleGenotype homRef() {
        return UNPHASED_DIPLOID_HOM_REF;
    }


    /**
     * Returns an instance representing an unphased homozygous alternate genotype e.g. 1/1
     * @return an unphased diploid homozygous alternate SampleGenotype
     * @since 11.0.0
     */
    public static SampleGenotype homAlt() {
        return UNPHASED_DIPLOID_HOM_ALT;
    }

    public static SampleGenotype of(AlleleCall... alleleCalls) {
        if (alleleCalls.length == 0) {
            return EMPTY;
        }
        Arrays.sort(alleleCalls);
        if (Arrays.equals(alleleCalls, UNPHASED_DIPLOID_HET.alleleCalls) ) {
            return UNPHASED_DIPLOID_HET;
        }
        if (Arrays.equals(alleleCalls, UNPHASED_DIPLOID_HOM_ALT.alleleCalls) ) {
            return UNPHASED_DIPLOID_HOM_ALT;
        }
        if (Arrays.equals(alleleCalls, UNPHASED_DIPLOID_HOM_REF.alleleCalls) ) {
            return UNPHASED_DIPLOID_HOM_REF;
        }
        if (Arrays.equals(alleleCalls, UNPHASED_DIPLOID_NO_CALL.alleleCalls) ) {
            return UNPHASED_DIPLOID_NO_CALL;
        }
        return new SampleGenotype(false, alleleCalls);
    }

    public static SampleGenotype phased(AlleleCall... alleleCalls) {
        if (alleleCalls.length == 0) {
            return EMPTY;
        }
        if (Arrays.equals(alleleCalls, PHASED_DIPLOID_HET_REF_ALT.alleleCalls) ) {
            return PHASED_DIPLOID_HET_REF_ALT;
        }
        if (Arrays.equals(alleleCalls, PHASED_DIPLOID_HET_ALT_REF.alleleCalls) ) {
            return PHASED_DIPLOID_HET_ALT_REF;
        }
        if (Arrays.equals(alleleCalls, PHASED_DIPLOID_HOM_ALT.alleleCalls) ) {
            return PHASED_DIPLOID_HOM_ALT;
        }
        if (Arrays.equals(alleleCalls, PHASED_DIPLOID_HOM_REF.alleleCalls) ) {
            return PHASED_DIPLOID_HOM_REF;
        }
        if (Arrays.equals(alleleCalls, PHASED_DIPLOID_NO_CALL.alleleCalls) ) {
            return PHASED_DIPLOID_NO_CALL;
        }
        return new SampleGenotype(true, alleleCalls);
    }

    private SampleGenotype(boolean phased, AlleleCall... alleleCalls) {
        this.alleleCalls = Arrays.copyOf(alleleCalls, alleleCalls.length);
        this.phased = phased;
    }

    public List<AlleleCall> getCalls() {
        return ImmutableList.copyOf(alleleCalls);
    }

    /**
     * Tests whether the current {@link SampleGenotype} is heterozygous.
     *
     * @return true if the genotype is heterozygous, otherwise false
     * @since 11.0.0
     */
    @JsonIgnore
    public boolean isHet() {
        if (alleleCalls.length <= 1) {
            return false;
        }
        AlleleCall first = alleleCalls[0];
        for (int i = 1; i < alleleCalls.length; i++) {
            AlleleCall current = alleleCalls[i];
            if (first != current) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether the current {@link SampleGenotype} is homozygous for the reference allele.
     *
     * @return true if the genotype is homozygous ref, otherwise false
     * @since 11.0.0
     */
    @JsonIgnore
    public boolean isHomRef() {
        if (alleleCalls.length == 0) {
            return false;
        }
        for (int i = 0, alleleCallsLength = alleleCalls.length; i < alleleCallsLength; i++) {
            AlleleCall alleleCall = alleleCalls[i];
            if (alleleCall == AlleleCall.ALT || alleleCall == AlleleCall.NO_CALL || alleleCall == AlleleCall.OTHER_ALT) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether the current {@link SampleGenotype} is homozygous for the alternate allele.
     *
     * @return true if the genotype is homozygous alt, otherwise false
     * @since 11.0.0
     */
    @JsonIgnore
    public boolean isHomAlt() {
        if (alleleCalls.length == 0) {
            return false;
        }
        for (int i = 0, alleleCallsLength = alleleCalls.length; i < alleleCallsLength; i++) {
            AlleleCall alleleCall = alleleCalls[i];
            if (alleleCall == AlleleCall.REF || alleleCall == AlleleCall.NO_CALL || alleleCall == AlleleCall.OTHER_ALT) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether the current {@link SampleGenotype} is phased.
     *
     * @return true if the genotype is phased, otherwise false
     * @since 11.0.0
     */
    @JsonIgnore
    public boolean isPhased() {
        return phased;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return alleleCalls.length == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleGenotype that = (SampleGenotype) o;
        return phased == that.phased &&
                Arrays.equals(alleleCalls, that.alleleCalls);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(phased);
        result = 31 * result + Arrays.hashCode(alleleCalls);
        return result;
    }

    @Override
    public String toString() {
        if (isEmpty()){
            return "NA";
        }
        StringJoiner stringJoiner;
        if (phased) {
            stringJoiner = new StringJoiner("|");
        } else {
            stringJoiner = new StringJoiner("/");
        }
        for (AlleleCall alleleCall : alleleCalls) {
            stringJoiner.add(alleleCall.toVcfString());
        }
        return stringJoiner.toString();
    }
}
