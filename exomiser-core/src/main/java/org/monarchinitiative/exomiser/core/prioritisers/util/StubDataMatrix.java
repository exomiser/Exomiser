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

package org.monarchinitiative.exomiser.core.prioritisers.util;

import org.jblas.FloatMatrix;

import java.util.Collections;
import java.util.Map;

/**
 * Specialised stub implementation of the {@code DataMatrix}. This is deliberately package private as it should only be
 * returned from the interface {@code empty()} method.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
class StubDataMatrix implements DataMatrix {

    private static final StubDataMatrix EMPTY = new StubDataMatrix();

    private StubDataMatrix() {
    }

    static DataMatrix empty() {
        return EMPTY;
    }

    @Override
    public Map<Integer, Integer> getEntrezIdToRowIndex() {
        return Collections.emptyMap();
    }

    @Override
    public FloatMatrix getMatrix() {
        return FloatMatrix.EMPTY;
    }

    @Override
    public int numRows() {
        return 0;
    }

    @Override
    public int numColumns() {
        return 0;
    }

    @Override
    public boolean containsGene(Integer entrezGeneId) {
        return false;
    }

    @Override
    public Integer getRowIndexForGene(int entrezGeneId) {
        return null;
    }

    @Override
    public FloatMatrix getColumnMatrixForGene(int entrezGeneId) {
        return null;
    }
}
