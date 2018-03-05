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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Contains the random walk relationships and the entrez-id to index relations.
 *
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DataMatrix {

    private static final Logger logger = LoggerFactory.getLogger(DataMatrix.class);

    public static final DataMatrix EMPTY = new DataMatrix(FloatMatrix.EMPTY, Collections.emptyMap());

    private final FloatMatrix matrix;
    private final Map<Integer, Integer> entrezIdToRowIndex;

    public DataMatrix(FloatMatrix matrix, Map<Integer, Integer> entrezIdToRowIndex) {
        this.matrix = matrix;
        this.entrezIdToRowIndex = entrezIdToRowIndex;
    }

    public Map<Integer, Integer> getEntrezIdToRowIndex() {
        return entrezIdToRowIndex;
    }

    public FloatMatrix getMatrix() {
        return matrix;
    }

    public int getRows() {
        return matrix.getRows();
    }

    public int getColumns() {
        return matrix.getColumns();
    }

    public boolean containsGene(Integer entrezGeneId) {
        return entrezIdToRowIndex.containsKey(entrezGeneId);
    }

    public Integer getRowIndexForGene(int entrezGeneId) {
        return entrezIdToRowIndex.get(entrezGeneId);
    }

    public FloatMatrix getColumnMatrixForGene(int entrezGeneId) {
        //the PPI float matrix is symmetrical so this will work here.
        Integer rowIndex = entrezIdToRowIndex.get(entrezGeneId);
        return matrix.getColumn(rowIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataMatrix)) return false;
        DataMatrix that = (DataMatrix) o;
        return Objects.equals(matrix, that.matrix) &&
                Objects.equals(entrezIdToRowIndex, that.entrezIdToRowIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matrix, entrezIdToRowIndex);
    }
}
