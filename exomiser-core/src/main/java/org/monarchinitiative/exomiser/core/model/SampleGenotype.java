/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

    @JsonIgnore
    public boolean isHomozygousAlt() {
        for (int i = 0, alleleCallsLength = alleleCalls.length; i < alleleCallsLength; i++) {
            AlleleCall alleleCall = alleleCalls[i];
            if (alleleCall == AlleleCall.REF || alleleCall == AlleleCall.NO_CALL || alleleCall == AlleleCall.OTHER_ALT) {
                return false;
            }
        }
        return true;
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
        if (alleleCalls.length == 0){
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
