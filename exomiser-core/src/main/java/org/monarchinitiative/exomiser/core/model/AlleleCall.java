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

/**
 * Representation of the call state of an allele. Crucially this differs from the HTSJKD because the exomiser does not
 * represent multiple alleles at the same position in the same way. So in most simple cases the allele is either a
 * ref (0), alt (1) or a no call (.) however for genotypes reported as a 1/2 in a VCF file they will be represented in
 * exomiser as a -/1 where the '-' indicates a genotype in conjunction with another allele at this position.
 *
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public enum AlleleCall {

    //CAUTION! Don't change the order of these as the SampleGenotype is relying it.
    NO_CALL,
    REF,
    OTHER_ALT,
    ALT;

    public String toVcfString() {
        switch(this){
            case REF:
                return "0";
            case ALT:
                return "1";
            case OTHER_ALT:
                return "-";
            case NO_CALL:
            default:
                return ".";
        }
    }

    public static AlleleCall parseAlleleCall(String call) {
        if (call == null || call.isEmpty() || call.equals(".")) {
            return NO_CALL;
        }
        if (call.equals("-")) {
            return OTHER_ALT;
        }
        try {
            int callInt = Integer.parseInt(call);
            switch (callInt) {
                case 0:
                    return REF;
                case 1:
                    return ALT;
                default:
                    return OTHER_ALT;
            }
        } catch (NumberFormatException e) {
            // oops! swallow
        }
        return NO_CALL;
    }
}
