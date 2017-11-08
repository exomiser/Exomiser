/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Allele implements Comparable<Allele> {

    private final int chr;
    private final int pos;
    private final String ref;
    private final String alt;

    private String rsId = ".";
    private Map<AlleleProperty, Float> values = new EnumMap<>(AlleleProperty.class);

    public Allele(int chr, int pos, String ref, String alt) {
        this.chr = chr;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
    }

    public int getChr() {
        return chr;
    }

    public int getPos() {
        return pos;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public String getRsId() {
        return rsId;
    }

    public void setRsId(String rsId) {
        this.rsId = rsId;
    }

    public Map<AlleleProperty, Float> getValues() {
        return values;
    }

    public void addValue(AlleleProperty key, Float value) {
        values.put(key, value);
    }

    public Float getValue(AlleleProperty key) {
        return values.get(key);
    }

    public String generateKey() {
        StringJoiner stringJoiner = new StringJoiner("-");
        stringJoiner.add(String.valueOf(chr));
        stringJoiner.add(String.valueOf(pos));
        stringJoiner.add(ref);
        stringJoiner.add(alt);
        return stringJoiner.toString();
    }

    public String generateInfoField() {
        StringJoiner stringJoiner = new StringJoiner(";");
        if (!rsId.equals(".")) {
            stringJoiner.add("RS=" + rsId);
        }
        for (Map.Entry<AlleleProperty, Float> value : values.entrySet()) {
            stringJoiner.add(value.toString());
        }
        return stringJoiner.toString();
    }

    @Override
    public int compareTo(@NotNull Allele other) {
        if (this.chr != other.chr) {
            return Integer.compare(this.chr, other.chr);
        }
        if (this.pos != other.pos) {
            return Integer.compare(this.pos, other.pos);
        }
        if (!this.ref.equals(other.ref)) {
            return this.ref.compareTo(other.ref);
        }
        return this.alt.compareTo(other.alt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Allele allele = (Allele) o;
        return chr == allele.chr &&
                pos == allele.pos &&
                Objects.equals(ref, allele.ref) &&
                Objects.equals(alt, allele.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chr, pos, ref, alt);
    }

    @Override
    public String toString() {
        return "Allele{" +
                "chr=" + chr +
                ", pos=" + pos +
                ", ref='" + ref + '\'' +
                ", alt='" + alt + '\'' +
                ", rsId='" + rsId + '\'' +
                ", values=" + values +
                '}';
    }

}
