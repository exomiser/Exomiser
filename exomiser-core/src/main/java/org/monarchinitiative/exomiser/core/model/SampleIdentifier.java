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

import java.util.Objects;

/**
 * Immutable tuple-like data class for holding the sample identifier and position in the VCF file.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public class SampleIdentifier {

    private final String id;
    private final int position;

    private SampleIdentifier(String id, int position) {
        this.id = id;
        this.position = position;
    }

    /**
     * Creates a new {@code SampleIdentifier} for the id and position. These should be produced from the VCF genotype
     * sample names as declared in the VCF header. <b>IMPORTANT</b> The position should use 0-based numbering. Failure
     * to do so will result in incorrect inheritance calculations and candidate allele prioritisation.
     *
     * @param id       the sample identifier as declared in the VCF header field.
     * @param position the 0-based position of the sample as declared in the VCF header field.
     * @return an identifier for the sample.
     * @throws NullPointerException     if the id is null
     * @throws IllegalArgumentException if the id is empty
     */
    public static SampleIdentifier of(String id, int position) {
        Objects.requireNonNull(id, "sample id cannot be null");
        if (id.isEmpty()) {
            throw new IllegalArgumentException("sample id cannot be empty");
        }
        return new SampleIdentifier(id, position);
    }

    public String getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SampleIdentifier that = (SampleIdentifier) o;
        return position == that.position &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position);
    }

    @Override
    public String toString() {
        return "SampleIdentifier{" +
                "id='" + id + '\'' +
                ", position=" + position +
                '}';
    }

}
