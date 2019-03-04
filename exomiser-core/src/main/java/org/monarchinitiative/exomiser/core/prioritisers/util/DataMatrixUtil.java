/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import java.util.Map;
import java.util.Objects;

/**
 * Non-public utility class for helping DataMatrix classes create FloatMatrix from Maps.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 10.0.0
 */
class DataMatrixUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataMatrixUtil.class);

    private DataMatrixUtil() {
    }

    static void checkKeys(Map<Integer, float[]> columns, Map<Integer, Integer> rowIndex) {
        Objects.requireNonNull(columns);
        Objects.requireNonNull(rowIndex);
        if (!rowIndex.keySet().equals(columns.keySet())) {
            throw new IllegalArgumentException("rowIndex and columns must have same keys");
        }
    }

    static FloatMatrix createMatrixfromMap(Map<Integer, float[]> columns, Map<Integer, Integer> rowIndex) {
        checkKeys(columns, rowIndex);
        return createFloatMatrix(columns, rowIndex);
    }

    private static FloatMatrix createFloatMatrix(Map<Integer, float[]> columns, Map<Integer, Integer> rowIndex) {
        logger.debug("Creating {} * {} FloatMatrix", rowIndex.size(), columns.size());
        FloatMatrix floatMatrix = new FloatMatrix(rowIndex.size(), columns.size());
        for (Map.Entry<Integer, Integer> entry : rowIndex.entrySet()) {
            Integer entrezGeneId = entry.getKey();
            Integer row = entry.getValue();
            float[] columnValues = columns.get(entrezGeneId);
            FloatMatrix column = new FloatMatrix(columnValues);
            floatMatrix.putColumn(row, column);
        }
        logger.debug("Finished building matrix");
        return floatMatrix;
    }

}
