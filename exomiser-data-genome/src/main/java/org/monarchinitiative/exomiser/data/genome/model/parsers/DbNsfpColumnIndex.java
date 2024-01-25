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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import java.util.Objects;

/**
 * DbNSFP is a massive TSV table which is great, however they like to add new columns with new data but in doing so they
 * often change the position of the column we want. This class holds the column header names for use by the column indexer
 * in the {@link DbNsfpAlleleParser}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbNsfpColumnIndex {

    public static final DbNsfpColumnIndex HG19 = new DbNsfpColumnIndex("hg19_chr", "hg19_pos(1-based)");
    public static final DbNsfpColumnIndex HG38 = new DbNsfpColumnIndex("chr", "pos(1-based)");

    private final String chrHeader;
    private final String posHeader;

    private DbNsfpColumnIndex(String chrHeader, String posHeader) {
        if (!chrHeader.contains("chr")) {
            throw new IllegalArgumentException("Expected 'chr' but got " + chrHeader);
        }
        if (!posHeader.contains("pos")) {
            throw new IllegalArgumentException("Expected 'pos' but got " + posHeader);
        }
        this.chrHeader = chrHeader;
        this.posHeader = posHeader;
    }

    public String getChrHeader() {
        return chrHeader;
    }

    public String getPosHeader() {
        return posHeader;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbNsfpColumnIndex that = (DbNsfpColumnIndex) o;
        return Objects.equals(chrHeader, that.chrHeader) &&
                Objects.equals(posHeader, that.posHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chrHeader, posHeader);
    }

    @Override
    public String toString() {
        return "DbNsfpColumnIndex{" +
                "chrHeader='" + chrHeader + '\'' +
                ", posHeader='" + posHeader + '\'' +
                '}';
    }

}
