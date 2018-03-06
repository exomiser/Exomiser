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

import java.util.Map;

/**
 * Interface defining how classes can access PPI matrix data.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public interface DataMatrix {

    /**
     * Returns a empty, immutable {@code DataMatrix}
     * @return an immutable, empty {@code DataMatrix}
     * @since 10.0.0
     */
    public static DataMatrix empty() {
        return StubDataMatrix.empty();
    }

    /**
     * Returns the Entrez gene id to row index.
     *
     * @return a map representation of the entrez gene id of a gene and its row position in the {@code DataMatrix}
     */
    public Map<Integer, Integer> getEntrezIdToRowIndex();

    /**
     * The {@code FloatMatrix} representation of the data, without an index.
     *
     * @return a {@code FloatMatrix} representation of the data, without an index.
     */
    public FloatMatrix getMatrix();

    /**
     *
     * @return the number of rows in this {@code DataMatrix}
     */
    public int numRows();

    /**
     *
     * @return the number of columns in this {@code DataMatrix}
     */
    public int numColumns();

    /**
     *
     * @param entrezGeneId the entrez gene identifier of the gene
     * @return true if the argument {@code entrezGeneId} is contained in the matrix
     */
    public boolean containsGene(Integer entrezGeneId);

    /**
     * Finds the {@code Integer} row index for the argument gene identifier.
     *
     * @param entrezGeneId the entrez gene identifier of the gene
     * @return the {@code Integer} row index for this gene identifier or {@code null} if not present.
     */
    public Integer getRowIndexForGene(int entrezGeneId);

    /**
     * Finds the {@code FloatMatrix} column for the argument gene identifier.
     *
     * @param entrezGeneId the entrez gene identifier of the gene
     * @return the {@code FloatMatrix} column for this gene identifier or {@code null} if not present.
     */
    public FloatMatrix getColumnMatrixForGene(int entrezGeneId);

}
