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

package org.monarchinitiative.exomiser.core.genome.dao;

import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.Variant;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;

import java.util.Objects;
import java.util.Set;

/**
 * An in-memory implementation of the {@link VariantWhiteList}
 * @since 12.0.0
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class InMemoryVariantWhiteList implements VariantWhiteList {

    private static final VariantWhiteList EMPTY = new EmptyVariantWhiteList();

    private final Set<AlleleProto.AlleleKey> whiteList;


    public static VariantWhiteList empty() {
        return EMPTY;
    }

    public static VariantWhiteList of(Set<AlleleProto.AlleleKey> whiteList) {
        Objects.requireNonNull(whiteList);

        if (whiteList.isEmpty()) {
            return EMPTY;
        }
        return new InMemoryVariantWhiteList(whiteList);
    }

    private InMemoryVariantWhiteList(Set<AlleleProto.AlleleKey> whiteList) {
        this.whiteList = Set.copyOf(whiteList);
    }

    @Override
    public boolean contains(Variant variant) {
        AlleleProto.AlleleKey alleleKey = variant.alleleKey();
        return whiteList.contains(alleleKey);
    }

    @Override
    public String toString() {
        return "InMemoryVariantWhiteList{" +
                "whiteList=" + whiteList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InMemoryVariantWhiteList)) return false;
        InMemoryVariantWhiteList that = (InMemoryVariantWhiteList) o;
        return whiteList.equals(that.whiteList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(whiteList);
    }

    private static class EmptyVariantWhiteList implements VariantWhiteList {

        @Override
        public boolean contains(Variant variant) {
            return false;
        }
    }

}
