/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.model;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantRecord implements Comparable<VariantRecord> {

    private final String chr;
    private final int start;
    private final int end;

    private final String vcfRecord;

    public static VariantRecord valueOf(String vcfRecord) {
        //CHR POS ID REF ALT QUAL FILTER INFO
        String[] fields = vcfRecord.split("\t");
        if (fields.length < 4) {
//            return new VariantRecord("", 0, 0, vcfRecord);
            throw new IllegalArgumentException("Input string should contain minimum 4 tab separated fields - '" + vcfRecord + "'" );
        }
        String chr = fields[0];
        int start = Integer.valueOf(fields[1]);
        //TODO: this isn't right - need to test this for both insertions and deletions plus multiple alts.
        int end = (start - 1) + fields[4].length();
        return new VariantRecord(chr, start, end, vcfRecord);
    }

    public VariantRecord(String chr, int start, int end, String vcfRecord) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.vcfRecord = vcfRecord;
    }

    public String getChr() {
        return chr;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getVcfRecord() {
        return vcfRecord;
    }

    @Override
    public int compareTo(VariantRecord o) {

        return 0;
    }

    @Override
    public String toString() {
        return "SimpleVariant{" +
                "chr='" + chr + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", vcfRecord='" + vcfRecord + '\'' +
                '}';
    }
}
