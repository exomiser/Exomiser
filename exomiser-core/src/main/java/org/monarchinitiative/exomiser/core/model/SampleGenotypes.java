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

import jakarta.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class SampleGenotypes implements Iterable<SampleData> {

    private final Map<String, SampleData> sampleDataById;

    private SampleGenotypes(List<SampleData> sampleData) {
        Objects.requireNonNull(sampleData);
        this.sampleDataById = buildSamplesMap(sampleData);
    }

    private Map<String, SampleData> buildSamplesMap(List<SampleData> samples) {
        Map<String, SampleData> sampleMap = LinkedHashMap.newLinkedHashMap(samples.size());
        for (SampleData sampleData : samples) {
            sampleMap.put(sampleData.id(), sampleData);
        }
        return Collections.unmodifiableMap(sampleMap);
    }

    public static SampleGenotypes of(List<SampleData> samples) {
        return new SampleGenotypes(samples);
    }

    public static SampleGenotypes of(SampleData... samples) {
        return of(List.of(samples));
    }

    public static SampleGenotypes of(String s1, SampleGenotype g1) {
        return of(SampleData.of(s1, g1));
    }

    public static SampleGenotypes of(String s1, SampleGenotype g1, String s2, SampleGenotype g2) {
        return of(SampleData.of(s1, g1), SampleData.of(s2, g2));
    }

    public static SampleGenotypes of(String s1, SampleGenotype g1, String s2, SampleGenotype g2, String s3, SampleGenotype g3) {
        return of(SampleData.of(s1, g1), SampleData.of(s2, g2), SampleData.of(s3, g3));
    }

    public static SampleGenotypes of(String s1, SampleGenotype g1, String s2, SampleGenotype g2, String s3, SampleGenotype g3, String s4, SampleGenotype g4) {
        return of(SampleData.of(s1, g1), SampleData.of(s2, g2), SampleData.of(s3, g3), SampleData.of(s4, g4));
    }

    public List<SampleData> sampleData() {
        return List.copyOf(sampleDataById.values());
    }

    @Nullable
    public SampleData sampleData(String sampleId) {
        return sampleDataById.get(sampleId);
    }

    public SampleGenotype sampleGenotype(String sampleId) {
        SampleData sampleData = sampleDataById.get(sampleId);
        return sampleData == null ? SampleGenotype.empty() : sampleData.sampleGenotype();
    }

    public CopyNumber sampleCopyNumber(String sampleId) {
        SampleData sampleData = sampleDataById.get(sampleId);
        return sampleData == null ? CopyNumber.empty() : sampleData.copyNumber();
    }

    public boolean isEmpty() {
        return sampleDataById.isEmpty();
    }

    public int size() {
        return sampleDataById.size();
    }

    @Override
    public Iterator<SampleData> iterator() {
        return sampleDataById.values().iterator();
    }

    public Stream<SampleData> stream() {
        return sampleDataById.values().stream();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleGenotypes that = (SampleGenotypes) o;
        return sampleDataById.equals(that.sampleDataById);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sampleDataById);
    }

    @Override
    public String toString() {
        StringJoiner commaSeparated = new StringJoiner(", ", "{", "}");
        for (SampleData sampleData : sampleDataById.values()) {
            commaSeparated.add(sampleData.id() + "=" + formatValues(sampleData));
        }
        return commaSeparated.toString();
    }

    private String formatValues(SampleData sampleData) {
        String gt = sampleData.sampleGenotype().toString();
        return sampleData.hasCopyNumber() ? gt + ":" + sampleData.copyNumber().copies() : gt;
    }


    public static class SingleSampleGenotypes implements Iterable<SampleData> {

        private final SampleData sampleData;

        public SingleSampleGenotypes(SampleData sampleData) {
            Objects.requireNonNull(sampleData);
            this.sampleData = sampleData;
        }

        public SampleGenotype sampleGenotype(String sampleId) {
            return sampleData.id().equals(sampleId) ? sampleData.sampleGenotype() : SampleGenotype.empty();
        }

        public CopyNumber sampleCopyNumber(String sampleId) {
            return sampleData.id().equals(sampleId) ? sampleData.copyNumber() : CopyNumber.empty();
        }

        public boolean isEmpty() {
            return false;
        }

        public Iterator<SampleData> iterator() {
            return new Iterator<>() {
                int cursor = 0;

                @Override
                public boolean hasNext() {
                    return cursor == 0;
                }

                @Override
                public SampleData next() {
                    if (++cursor == 1) {
                        return sampleData;
                    }
                    throw new NoSuchElementException();
                }
            };
        }

        public Stream<SampleData> stream() {
            return Stream.of(sampleData);
        }

    }

}
