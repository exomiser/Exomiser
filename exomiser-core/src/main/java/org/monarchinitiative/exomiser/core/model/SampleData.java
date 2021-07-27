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

import java.util.Objects;

/**
 *
 */
public class SampleData {

    private final String sampleIdentifier;
    private final SampleGenotype sampleGenotype;
    private final CopyNumber copyNumber;

    private SampleData(String sampleIdentifier, SampleGenotype sampleGenotype, CopyNumber copyNumber) {
        this.sampleIdentifier = Objects.requireNonNull(sampleIdentifier);
        this.sampleGenotype = Objects.requireNonNull(sampleGenotype);
        this.copyNumber = Objects.requireNonNull(copyNumber);
    }

    public static SampleData of(String sampleIdentifier, SampleGenotype sampleGenotype) {
        return new SampleData(sampleIdentifier, sampleGenotype, CopyNumber.empty());
    }

    public static SampleData of(String sampleIdentifier, SampleGenotype sampleGenotype, int copyNumber) {
        return new SampleData(sampleIdentifier, sampleGenotype, CopyNumber.of(copyNumber));
    }

    public static SampleData of(String sampleIdentifier, SampleGenotype sampleGenotype, CopyNumber copyNumber) {
        return new SampleData(sampleIdentifier, sampleGenotype, copyNumber);
    }

    public String getId() {
        return sampleIdentifier;
    }

    public SampleGenotype getSampleGenotype() {
        return sampleGenotype;
    }

    @JsonIgnore
    public boolean hasCopyNumber() {
        return !copyNumber.isEmpty();
    }

    public CopyNumber getCopyNumber() {
        return copyNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleData sampleData = (SampleData) o;
        return copyNumber.equals(sampleData.copyNumber) && sampleIdentifier.equals(sampleData.sampleIdentifier) && sampleGenotype.equals(sampleData.sampleGenotype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sampleIdentifier, sampleGenotype, copyNumber);
    }

    @Override
    public String toString() {
        if (hasCopyNumber()) {
            return "SampleData{" +
                    "sampleIdentifier=" + sampleIdentifier +
                    ", sampleGenotype=" + sampleGenotype +
                    ", copyNumber=" + copyNumber +
                    '}';
        }
        return "SampleData{" +
                "sampleIdentifier=" + sampleIdentifier +
                ", sampleGenotype=" + sampleGenotype +
                '}';
    }
}
