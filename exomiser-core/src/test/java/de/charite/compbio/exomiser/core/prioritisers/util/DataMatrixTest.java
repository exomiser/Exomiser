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
package de.charite.compbio.exomiser.core.prioritisers.util;

import org.jblas.FloatMatrix;
import org.junit.Before;
import org.junit.Test;

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
        floatMatrix = new FloatMatrix(4, 4);

        //produce the following matrix:
        //  0  1  2  3
        //0 00 01 02 03
        //1 10 11 12 13
        //2 20 21 22 23
        //3 30 31 32 33

        //Here the single integer row is linked to an Entrez geneId in the entrezIdToRowIndex

        //floatMatrix.put(row, column, value);
        floatMatrix.put(0, 0, 00);
        floatMatrix.put(0, 1, 01);
        floatMatrix.put(0, 2, 02);
        floatMatrix.put(0, 3, 03);

        floatMatrix.put(1, 0, 10);
        floatMatrix.put(1, 1, 11);
        floatMatrix.put(1, 2, 12);
        floatMatrix.put(1, 3, 13);

        floatMatrix.put(2, 0, 20);
        floatMatrix.put(2, 1, 21);
        floatMatrix.put(2, 2, 22);
        floatMatrix.put(2, 3, 23);

        floatMatrix.put(3, 0, 30);
        floatMatrix.put(3, 1, 31);
        floatMatrix.put(3, 2, 32);
        floatMatrix.put(3, 3, 33);

        entrezIdToRowIndex = new TreeMap<>();
        entrezIdToRowIndex.put(0000, 0);
        entrezIdToRowIndex.put(1111, 1);
        entrezIdToRowIndex.put(2222, 2);
        entrezIdToRowIndex.put(3333, 3);

        instance = new DataMatrix(floatMatrix, entrezIdToRowIndex);
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
        FloatMatrix geneColumn = new FloatMatrix(4, 1);
        geneColumn.put(0, 0, 03);
        geneColumn.put(1, 0, 13);
        geneColumn.put(2, 0, 23);        
        geneColumn.put(3, 0, 33);
        
        assertThat(instance.getColumnMatrixForGene(3333), equalTo(geneColumn));
        for (float value : geneColumn.toArray()) {
            System.out.println(value);
        }

        for (float value : geneColumn.mul(2.0f).toArray()) {
            System.out.println(value);
        }

        System.out.printf("Matrix is %d rows * %d columns%n", instance.getMatrix().rows, instance.getMatrix().columns);
        List<FloatMatrix> columns = floatMatrix.columnsAsList();
        for (FloatMatrix matrix : columns) {
            for (float value : matrix.toArray()) {
                System.out.println(value);
            }
        }
        System.out.println();
    }

    @Test
    public void testEquals() {
        DataMatrix identicalMatrix = new DataMatrix(floatMatrix, entrezIdToRowIndex);
        assertThat(instance, equalTo(identicalMatrix));
    }
    
}
