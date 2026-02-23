/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * Immutable class representing the genotype of a sample, expressed as a phased or un-phased set of {@link AlleleCall}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public record SampleGenotype(Phasing phasing, List<AlleleCall> calls) {

    private static final Pattern GT = Pattern.compile("([0-9.-][/|]?)*+");

    private static final SampleGenotype EMPTY = new SampleGenotype(Phasing.UNPHASED, List.of());
    //cached common diploid genotypes - these are going to be super-common in multi-sample cases
    private static final SampleGenotype UNPHASED_DIPLOID_NO_CALL = new SampleGenotype(Phasing.UNPHASED,  List.of(AlleleCall.NO_CALL, AlleleCall.NO_CALL));
    private static final SampleGenotype UNPHASED_DIPLOID_HET = new SampleGenotype(Phasing.UNPHASED, List.of(AlleleCall.REF, AlleleCall.ALT));
    private static final SampleGenotype UNPHASED_DIPLOID_HOM_REF = new SampleGenotype(Phasing.UNPHASED, List.of(AlleleCall.REF, AlleleCall.REF));
    private static final SampleGenotype UNPHASED_DIPLOID_HOM_ALT = new SampleGenotype(Phasing.UNPHASED, List.of(AlleleCall.ALT, AlleleCall.ALT));

    private static final SampleGenotype PHASED_DIPLOID_NO_CALL = new SampleGenotype(Phasing.PHASED, List.of(AlleleCall.NO_CALL, AlleleCall.NO_CALL));
    private static final SampleGenotype PHASED_DIPLOID_HET_REF_ALT = new SampleGenotype(Phasing.PHASED, List.of(AlleleCall.REF, AlleleCall.ALT));
    private static final SampleGenotype PHASED_DIPLOID_HET_ALT_REF = new SampleGenotype(Phasing.PHASED, List.of(AlleleCall.ALT, AlleleCall.REF));
    private static final SampleGenotype PHASED_DIPLOID_HOM_REF = new SampleGenotype(Phasing.PHASED, List.of(AlleleCall.REF, AlleleCall.REF));
    private static final SampleGenotype PHASED_DIPLOID_HOM_ALT = new SampleGenotype(Phasing.PHASED, List.of(AlleleCall.ALT, AlleleCall.ALT));

    enum Phasing {
        UNPHASED, PHASED;
    }

    public SampleGenotype {
        calls = List.copyOf(calls);
    }

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
        var calls = List.of(alleleCalls);
        if (calls.equals(UNPHASED_DIPLOID_HET.calls) ) {
            return UNPHASED_DIPLOID_HET;
        }
        if (calls.equals(UNPHASED_DIPLOID_HOM_ALT.calls) ) {
            return UNPHASED_DIPLOID_HOM_ALT;
        }
        if (calls.equals(UNPHASED_DIPLOID_HOM_REF.calls) ) {
            return UNPHASED_DIPLOID_HOM_REF;
        }
        if (calls.equals(UNPHASED_DIPLOID_NO_CALL.calls) ) {
            return UNPHASED_DIPLOID_NO_CALL;
        }
        return new SampleGenotype(Phasing.UNPHASED, calls);
    }

    public static SampleGenotype phased(AlleleCall... alleleCalls) {
        if (alleleCalls.length == 0) {
            return EMPTY;
        }
        var calls = List.of(alleleCalls);
        if (calls.equals(PHASED_DIPLOID_HET_REF_ALT.calls) ) {
            return PHASED_DIPLOID_HET_REF_ALT;
        }
        if (calls.equals(PHASED_DIPLOID_HET_ALT_REF.calls) ) {
            return PHASED_DIPLOID_HET_ALT_REF;
        }
        if (calls.equals(PHASED_DIPLOID_HOM_ALT.calls) ) {
            return PHASED_DIPLOID_HOM_ALT;
        }
        if (calls.equals(PHASED_DIPLOID_HOM_REF.calls) ) {
            return PHASED_DIPLOID_HOM_REF;
        }
        if (calls.equals(PHASED_DIPLOID_NO_CALL.calls) ) {
            return PHASED_DIPLOID_NO_CALL;
        }
        return new SampleGenotype(Phasing.PHASED, calls);
    }

    /**
     * Parse a VCF-style genotype string into a {@link SampleGenotype} - e.g. 0/1 or 1/1
     * This method will handle no calls (.) phasing (/ or |) and polyploid genotypes.
     *
     * @param genotype The genotype in VCF format.
     * @return a SampleGenotype parsed from the input string or an empty instance if unrecognised.
     * @since 13.0.0
     */
    public static SampleGenotype parseGenotype(String genotype) {
        if (genotype == null || genotype.isEmpty() || genotype.equals(".") || genotype.equals("NA")) {
            return SampleGenotype.empty();
        }
        if (GT.matcher(genotype).matches()) {
            boolean phased = genotype.contains("|");
            AlleleCall[] alleleCalls = parseAlleleCalls(phased, genotype);
            return phased ? SampleGenotype.phased(alleleCalls) : SampleGenotype.of(alleleCalls);
        }
        return SampleGenotype.empty();
    }

    private static AlleleCall[] parseAlleleCalls(boolean phased, String genotype) {
        String[] calls = phased ? genotype.split("\\|") : genotype.split("/");
        AlleleCall[] alleleCalls = new AlleleCall[calls.length];
        for (int i = 0; i < calls.length; i++) {
            alleleCalls[i] = AlleleCall.parseAlleleCall(calls[i]);
        }
        return alleleCalls;
    }

    /**
     * Returns the number of calls for the {@link SampleGenotype}. For example a monoploid sample would return 1,
     * diploid 2, triploid 3 etc.
     *
     * @return the number of calls for this {@link SampleGenotype}
     * @since 13.0.0
     */
    @JsonIgnore
    public int numCalls() {
        return calls.size();
    }

    /**
     * Tests whether the current {@link SampleGenotype} is heterozygous.
     *
     * @return true if the genotype is heterozygous, otherwise false
     * @since 11.0.0
     */
    @JsonIgnore
    public boolean isHet() {
        if (calls.size() <= 1) {
            return false;
        }
        AlleleCall first = calls.getFirst();
        for (int i = 1; i < calls.size(); i++) {
            AlleleCall current = calls.get(i);
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
        if (calls.isEmpty()) {
            return false;
        }
        for (int i = 0; i < calls.size(); i++) {
            AlleleCall alleleCall = calls.get(i);
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
        if (calls.isEmpty()) {
            return false;
        }
        for (int i = 0; i < calls.size(); i++) {
            AlleleCall alleleCall = calls.get(i);
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
        return phasing == Phasing.PHASED;
    }

    /**
     * Tests whether the current {@link SampleGenotype} is empty.
     *
     * @return true if the genotype is empty, otherwise false
     * @since 12.0.0
     */
    @JsonIgnore
    public boolean isEmpty() {
        return calls.isEmpty();
    }

    /**
     * Tests whether the current {@link SampleGenotype} does NOT contain a NO_CALL.
     *
     * @return true if the genotype only contains NO CALLs, otherwise false
     * @since 13.0.0
     */
    @JsonIgnore
    public boolean isNoCall() {
        for (int i = 0; i < calls.size(); i++) {
            if (calls.get(i) != AlleleCall.NO_CALL) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        if (isEmpty()){
            return ".";
        }
        StringJoiner stringJoiner;
        if (isPhased()) {
            stringJoiner = new StringJoiner("|");
        } else {
            stringJoiner = new StringJoiner("/");
        }
        for (AlleleCall alleleCall : calls) {
            stringJoiner.add(alleleCall.toVcfString());
        }
        return stringJoiner.toString();
    }
}
