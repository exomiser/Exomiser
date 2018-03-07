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

import com.google.common.collect.ImmutableMap;
import org.jblas.FloatMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * In-memory implementation of the DataMatrix. Contains the random walk relationships and the entrez-id to index relations.
 *
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @since 10.0.0
 */
public class InMemoryDataMatrix implements DataMatrix {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryDataMatrix.class);

    private final FloatMatrix matrix;
    private final Map<Integer, Integer> entrezIdToRowIndex;

    public InMemoryDataMatrix(FloatMatrix matrix, Map<Integer, Integer> entrezIdToRowIndex) {
        if (!matrix.isSquare()){
            throw new IllegalArgumentException("matrix must be square");
        }
        if(matrix.getRows() != entrezIdToRowIndex.size()) {
            throw new IllegalArgumentException("matrix must have same number of rows as index has keys");
        }
        this.matrix = matrix;
        this.entrezIdToRowIndex = ImmutableMap.copyOf(entrezIdToRowIndex);
    }

    public static InMemoryDataMatrix fromMap(Map<Integer, float[]> columns, Map<Integer, Integer> entrezIdToRowIndex) {
        FloatMatrix floatMatrix = DataMatrixUtil.createMatrixfromMap(columns, entrezIdToRowIndex);
        return new InMemoryDataMatrix(floatMatrix, entrezIdToRowIndex);
    }

    @Override
    public Map<Integer, Integer> getEntrezIdToRowIndex() {
        return entrezIdToRowIndex;
    }

    @Override
    public FloatMatrix getMatrix() {
        return matrix;
    }

    @Override
    public int numRows() {
        return matrix.getRows();
    }

    @Override
    public int numColumns() {
        return matrix.getColumns();
    }

    @Override
    public boolean containsGene(Integer entrezGeneId) {
        return entrezIdToRowIndex.containsKey(entrezGeneId);
    }

    @Override
    public Integer getRowIndexForGene(int entrezGeneId) {
        return entrezIdToRowIndex.get(entrezGeneId);
    }

    @Override
    public FloatMatrix getColumnMatrixForGene(int entrezGeneId) {
        //the PPI float matrix is symmetrical so this will work here.
        Integer rowIndex = entrezIdToRowIndex.get(entrezGeneId);
        return matrix.getColumn(rowIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InMemoryDataMatrix)) return false;
        InMemoryDataMatrix that = (InMemoryDataMatrix) o;
        return Objects.equals(matrix, that.matrix) &&
                Objects.equals(entrezIdToRowIndex, that.entrezIdToRowIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matrix, entrezIdToRowIndex);
    }
}
