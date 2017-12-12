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

package org.monarchinitiative.exomiser.core.model.frequency;

import java.util.Objects;

/**
 * Immutable value Class representing an NCBI dbSNP reference SNP rsID.
 * <p>
 * {@link http://www.ncbi.nlm.nih.gov/projects/SNP/index.html}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public final class RsId {

    private static final RsId EMPTY = new RsId(0);
    private static final String VCF_EMPTY_VALUE = ".";

    private final int id;

    /**
     * Returns new RsId from the integer provided. Integers less than or equal to zero will return an instance
     * representing an empty value.
     *
     * @param id
     * @return the rsId value represented by the argument provided.
     */
    public static RsId valueOf(int id) {
        if (id <= 0) {
            return EMPTY;
        }
        return new RsId(id);
    }

    /**
     * Parses rs ids from their VCF representation - can be in the form "rs123456", "123456" or "." for an empty value.
     * Will accept a null as representing an empty value.
     *
     * @param id a {@code String} containing the {@code int} representation to be parsed
     * @return the rsId value represented by the argument provided.
     */
    public static RsId valueOf(String id) {
        if (id == null || id.isEmpty() || VCF_EMPTY_VALUE.equals(id)) {
            return EMPTY;
        }
        if (id.startsWith("rs")) {
            return new RsId(Integer.parseInt(id.substring(2)));
        }
        return new RsId(Integer.parseInt(id));
    }

    /**
     * Returns an instance of an empty value. This is represented as '.' in VCF notation.
     *
     * @return an empty rsId value.
     */
    public static RsId empty() {
        return EMPTY;
    }

    private RsId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isEmpty() {
        return id == EMPTY.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RsId rsId = (RsId) o;
        return id == rsId.id;
    }

    /**
     * Returns an rsID formatted string - 'rs123456' or '.' for an empty value.
     */
    @Override
    public String toString() {
        if (id == EMPTY.id) {
            return VCF_EMPTY_VALUE;
        }
        return "rs" + id;
    }

}
