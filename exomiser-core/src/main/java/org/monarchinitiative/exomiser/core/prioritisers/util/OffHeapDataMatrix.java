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
import org.h2.mvstore.MVStore;
import org.jblas.FloatMatrix;

import jakarta.annotation.PreDestroy;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Off-heap {@link MVStore}-backed {@code DataMatrix} implementation.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
public class OffHeapDataMatrix implements DataMatrix {

    private final MVStore mvStore;

    private final Map<Integer, float[]> columns;
    private final Map<Integer, Integer> rowIndex;

    private FloatMatrix floatMatrix = null;

    public static OffHeapDataMatrix load(Path ppiMapPath) {
        Objects.requireNonNull(ppiMapPath);
        MVStore mvStore = new MVStore.Builder()
                .fileName(ppiMapPath.toAbsolutePath().toString())
                .readOnly()
                .open();

        return new OffHeapDataMatrix(mvStore);
    }

    public OffHeapDataMatrix(MVStore mvStore) {
        Objects.requireNonNull(mvStore);
        this.mvStore = mvStore;
        this.columns = mvStore.openMap("columns");
        this.rowIndex = mvStore.openMap("gene_id_row_index");
        DataMatrixUtil.checkKeys(columns, rowIndex);
    }

    /**
     * Only one {@linkplain MVStore} backing this class can be open at a time in any one JVM. This method is required to
     * ensure the class is properly closed by any IOC containers.
     */
    @PreDestroy
    public void close() {
        mvStore.close();
    }

    public Map<Integer, float[]> getColumns() {
        return ImmutableMap.copyOf(columns);
    }

    @Override
    public Map<Integer, Integer> getEntrezIdToRowIndex() {
        return ImmutableMap.copyOf(rowIndex);
    }

    @Override
    public FloatMatrix getMatrix() {
        if (floatMatrix == null) {
            //if using a large off-heap map this could be quite an expensive operation
            floatMatrix = DataMatrixUtil.createMatrixfromMap(columns, rowIndex);
        }
        return floatMatrix;
    }

    @Override
    public int numRows() {
        return rowIndex.size();
    }

    @Override
    public int numColumns() {
        return columns.size();
    }

    @Override
    public boolean containsGene(Integer entrezGeneId) {
        return columns.containsKey(entrezGeneId);
    }

    @Override
    public Integer getRowIndexForGene(int entrezGeneId) {
        return rowIndex.get(entrezGeneId);
    }

    @Override
    public FloatMatrix getColumnMatrixForGene(int entrezGeneId) {
        float[] columnValues = columns.get(entrezGeneId);
        if (columnValues == null) {
            return null;
        }
        return new FloatMatrix(columnValues);
    }
}
