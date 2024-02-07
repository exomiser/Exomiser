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

package org.monarchinitiative.exomiser.data.genome.model;

import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.core.proto.AlleleProtoFormatter;

import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Allele implements Comparable<Allele> {

    private final int chr;
    private final int pos;
    private final String ref;
    private final String alt;

    private String rsId = "";
    private ClinVarData clinVarData = null;
    private final Map<AlleleProperty, Float> values = new EnumMap<>(AlleleProperty.class);
    private final List<AlleleProto.Frequency> frequencies = new ArrayList<>();
    private final List<AlleleProto.PathogenicityScore> pathogenicityScores = new ArrayList<>();

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

    public boolean hasClinVarData() {
        return clinVarData != null;
    }

    public ClinVarData getClinVarData() {
        return this.clinVarData;
    }

    public void setClinVarData(ClinVarData clinVarData) {
        this.clinVarData = clinVarData;
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "14.0.0")
    public Map<AlleleProperty, Float> getValues() {
        return values;
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "14.0.0")
    public void addValue(AlleleProperty key, Float value) {
        values.put(key, value);
    }

    /**
     * @deprecated
     */
    @Deprecated(since = "14.0.0")
    public Float getValue(AlleleProperty key) {
        return values.get(key);
    }


    /**
     * @since 14.0.0
     */
    public void addFrequency(AlleleProto.Frequency frequency) {
        frequencies.add(frequency);
    }

    /**
     * @since 14.0.0
     */
    public void addAllFrequencies(Collection<AlleleProto.Frequency> frequencies) {
        this.frequencies.addAll(frequencies);
    }

    /**
     * @since 14.0.0
     */
    public List<AlleleProto.Frequency> getFrequencies() {
        return frequencies;
    }

    /**
     * @since 14.0.0
     */
    public void addPathogenicityScore(AlleleProto.PathogenicityScore pathogenicityScore) {
        pathogenicityScores.add(pathogenicityScore);
    }

    /**
     * @since 14.0.0
     */
    public void addAllPathogenicityScores(Collection<AlleleProto.PathogenicityScore> pathogenicityScores) {
        this.pathogenicityScores.addAll(pathogenicityScores);
    }

    /**
     * @since 14.0.0
     */
    public List<AlleleProto.PathogenicityScore> getPathogenicityScores() {
        return pathogenicityScores;
    }

    @Override
    public int compareTo(Allele other) {
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
        return chr == allele.chr && pos == allele.pos && ref.equals(allele.ref) && alt.equals(allele.alt) && Objects.equals(rsId, allele.rsId) && Objects.equals(clinVarData, allele.clinVarData) && values.equals(allele.values) && frequencies.equals(allele.frequencies) && pathogenicityScores.equals(allele.pathogenicityScores);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chr, pos, ref, alt, rsId, clinVarData, values, frequencies, pathogenicityScores);
    }

    @Override
    public String toString() {
        return "Allele{" +
               "chr=" + chr +
               ", pos=" + pos +
               ", ref='" + ref + '\'' +
               ", alt='" + alt + '\'' +
               ", rsId='" + rsId + '\'' +
               ", clinVarData='" + clinVarData + '\'' +
               ", values=" + values + '\'' +
               ", frequencies=" + AlleleProtoFormatter.formatFrequencies(frequencies) +
               ", pathogenicityScores=" + AlleleProtoFormatter.formatPathScores(pathogenicityScores) +
               '}';
    }
}
