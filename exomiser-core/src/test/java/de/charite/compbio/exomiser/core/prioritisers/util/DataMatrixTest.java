/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.prioritisers.util;

import java.util.Map;
import java.util.TreeMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import org.jblas.FloatMatrix;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DataMatrixTest {
    
    private DataMatrix instance;
    
    private FloatMatrix floatMatrix;
    private Map<Integer, Integer> entrezIdToRowIndex;
    private Map<Integer, Integer> rowToEntrezIdIndex;
    
    @Before
    public void setUp() {
        floatMatrix = new FloatMatrix(4, 4);
        
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
        
        rowToEntrezIdIndex = new TreeMap<>();
        rowToEntrezIdIndex.put(0, 0000);
        rowToEntrezIdIndex.put(1, 1111);
        rowToEntrezIdIndex.put(2, 2222);
        rowToEntrezIdIndex.put(3, 3333);
                
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
    public void testGetMatrixName() {
        assertThat(instance.getName(), equalTo(""));
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
    }
    
//
//    @Test
//    public void testWriteMatrix() {
//    }
//
//    @Test
//    public void testWriteMatrixInclHeaderAndRowIDs() {
//    }
    
}
