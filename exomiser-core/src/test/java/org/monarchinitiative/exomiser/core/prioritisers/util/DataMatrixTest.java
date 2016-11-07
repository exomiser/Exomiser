/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.prioritisers.util;

import org.jblas.FloatMatrix;
import org.jblas.ranges.IntervalRange;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DataMatrixTest {
    
    private DataMatrix instance;
    
    private FloatMatrix floatMatrix;
    private Map<Integer, Integer> entrezIdToRowIndex;

    @Before
    public void setUp() {
        float[][] matrix = {
                {0.0f, 0.1f, 0.2f, 0.3f},
                {1.0f, 1.1f, 1.2f, 1.3f},
                {2.0f, 2.1f, 2.2f, 2.3f},
                {3.0f, 3.1f, 3.2f, 3.3f}
        };

        floatMatrix = new FloatMatrix(matrix);

        //Here the single integer row is linked to an Entrez geneId in the entrezIdToRowIndex

        entrezIdToRowIndex = new TreeMap<>();
        entrezIdToRowIndex.put(0000, 0);
        entrezIdToRowIndex.put(1111, 1);
        entrezIdToRowIndex.put(2222, 2);
        entrezIdToRowIndex.put(3333, 3);

        instance = new DataMatrix(floatMatrix, entrezIdToRowIndex);
    }

    @Test
    public void testEmptyMatrix() {
        DataMatrix emptyMatrix = DataMatrix.EMPTY;
        assertThat(emptyMatrix.containsGene(112), is(false));
        assertThat(emptyMatrix.getEntrezIdToRowIndex(), equalTo(Collections.emptyMap()));
        assertThat(emptyMatrix.getMatrix().isEmpty(), is(true));
    }

    @Test
    public void testGetObjectid2idx() {
        assertThat(instance.getEntrezIdToRowIndex(), equalTo(entrezIdToRowIndex));
    }

    @Test
    public void testGetData() {
        assertThat(instance.getMatrix(), equalTo(floatMatrix));
    }

    @Test
    public void testContainsGeneIsTrue() {
        assertThat(instance.containsGene(0000), is(true));
    }
    
    @Test
    public void testContainsGeneIsFalse() {
        assertThat(instance.containsGene(9999), is(false));
    }
    
    @Test
    public void testGetRowIndexForGeneInIndex() {
        assertThat(instance.getRowIndexForGene(3333), equalTo(3));
    }
    
    @Test
    public void testGetRowIndexForGeneNotInIndex() {
        assertThat(instance.getRowIndexForGene(9999), nullValue());
    }
    
    @Test
    public void testGetColumnMatrixForGeneInIndex() {
        //expect a new single column matrix with 4 rows
        float[] column = {0.3f, 1.3f, 2.3f, 3.3f};
        FloatMatrix geneColumn = new FloatMatrix(column);

        assertThat(instance.getColumnMatrixForGene(3333), equalTo(geneColumn));
        System.out.println("Column matrix for gene 3333:");
        for (float value : geneColumn.toArray()) {
            System.out.println(value);
        }

        System.out.println("Column matrix for gene 3333 multiplied by 2:");
        for (float value : instance.getColumnMatrixForGene(3333).mul(2.0f).toArray()) {
            System.out.println(value);
        }

        System.out.printf("%nMatrix is (%d rows * %d columns:%n)", floatMatrix.rows, floatMatrix.columns);
        System.out.println(floatMatrix);

        System.out.println("Matrix as columns:");
        List<FloatMatrix> columns = floatMatrix.columnsAsList();
        for (int i = 0; i < columns.size(); i++) {
            FloatMatrix matrix = columns.get(i);
            System.out.printf("Column %d: ", i);
            for (float value : matrix.toArray()) {
                System.out.printf("%f ", value);
            }
            System.out.println();
        }
        System.out.println("Matrix as rows:");
        List<FloatMatrix> rows = floatMatrix.rowsAsList();
        for (int i = 0; i < rows.size(); i++) {
            FloatMatrix matrix = rows.get(i);
            System.out.printf("Row %d: ", i);
            for (float value : matrix.toArray()) {
                System.out.printf("%f, ", value);
            }
            System.out.println();
        }
    }

    @Test
    public void testMakeSubMatrix() {
        float[][] matrix = {{0.0f, 0.1f, 0.2f}, {1.0f, 1.1f, 1.2f}, {2.0f, 2.1f, 2.2f}};
        FloatMatrix expected = new FloatMatrix(matrix);

        FloatMatrix subMatrix = floatMatrix.get(new IntervalRange(0, 3), new IntervalRange(0, 3));
        System.out.println(subMatrix);
        assertThat(subMatrix, equalTo(expected));

    }

    @Test
    public void testEquals() {
        DataMatrix identicalMatrix = new DataMatrix(floatMatrix, entrezIdToRowIndex);
        assertThat(instance, equalTo(identicalMatrix));
    }
    
}
