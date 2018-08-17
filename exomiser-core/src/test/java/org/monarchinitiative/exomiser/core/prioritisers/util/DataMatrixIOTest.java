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
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DataMatrixIOTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @After
    public void tearDown() {
        tempFolder.delete();
    }

    private final DataMatrix dataMatrix = loadMatrix();

    private DataMatrix loadMatrix() {
        FloatMatrix floatMatrix = new FloatMatrix(4, 4);

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

        Map<Integer, Integer> entrezIdToRowIndex = new TreeMap<>();
        entrezIdToRowIndex.put(0000, 0);
        entrezIdToRowIndex.put(1111, 1);
        entrezIdToRowIndex.put(2222, 2);
        entrezIdToRowIndex.put(3333, 3);

        return new InMemoryDataMatrix(floatMatrix, entrezIdToRowIndex);
    }

    @Test
    public void testConvertToMap() throws Exception{
        String dataPath = "src/test/resources/prioritisers/";
        String indexPath = dataPath + "test_ppi_matrix_id2index.gz";
        String matrixPath = dataPath + "test_ppi_matrix.gz";

        File matrixMapFile = tempFolder.newFile("test_ppi_matrix.mv");
        DataMatrixIO.convertToMap(matrixPath, indexPath, matrixMapFile.toPath());

        // load the in memory version first as this copies all the data and closes the map otherwise it will throw an
        // IllegalStateException caused by an OverlappingFileLockException
        DataMatrix inMemoryMapMatrix = DataMatrixIO.loadInMemoryDataMatrix(matrixMapFile.toPath());
        DataMatrix offHeapMapMatrix = DataMatrixIO.loadOffHeapDataMatrix(matrixMapFile.toPath());

        DataMatrix fromFile = DataMatrixIO.loadInMemoryDataMatrixFromFile(matrixPath, indexPath, true);

        assertThat(offHeapMapMatrix.getEntrezIdToRowIndex(), equalTo(fromFile.getEntrezIdToRowIndex()));

        testMatrixEquality(offHeapMapMatrix.getMatrix(), fromFile.getMatrix());
        testMatrixEquality(inMemoryMapMatrix.getMatrix(), fromFile.getMatrix());
    }

    private void testMatrixEquality(FloatMatrix mapMatrix, FloatMatrix fileMatrix) {
        int rows = mapMatrix.getRows();
        int cols = mapMatrix.getColumns();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                assertThat(fileMatrix.get(i, j), equalTo(mapMatrix.get(i, j)));
            }
        }
    }

    @Test
    public void loadDataMatrixFromMap() {
        Path mapPath = Paths.get("src/test/resources/prioritisers/test_ppi_matrix.mv");
        DataMatrix dataMatrix = DataMatrixIO.loadOffHeapDataMatrix(mapPath);
        assertThat(dataMatrix.numRows(), equalTo(10));
        assertThat(dataMatrix.numColumns(), equalTo(10));
    }

    @Test
    public void testWriteMatrix() throws Exception {
        Path matrixFile = Paths.get("target/testMatrix");
        Path matrixIndexFile = Paths.get("target/testMatrix_id2index");

        DataMatrixIO.writeMatrix(dataMatrix, matrixFile.toString(), true);

        assertFileExistsThenDelete(matrixFile);
        assertFileExistsThenDelete(matrixIndexFile);
    }

    @Test
    public void testWriteMatrixWithHeaders() throws Exception {
        Path outFile = Paths.get("target/testMatrixWithRowIdAndHeader");
        DataMatrixIO.writeMatrixInclHeaderAndRowIDs(dataMatrix, outFile.toString(), true);
        assertFileExistsThenDelete(outFile);
    }

    private void assertFileExistsThenDelete(Path outFile) throws IOException {
        assertThat(Files.exists(outFile), is(true));
        Files.delete(outFile);
    }

}