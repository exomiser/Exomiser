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

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.jblas.FloatMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 */
public class DataMatrixIO {

    private static final Logger logger = LoggerFactory.getLogger(DataMatrixIO.class);

    private static final String TAB_DELIMITER = "\t";

    /*
     * This shouldn't be instantiated.
     */
    private DataMatrixIO() {}

    public static void convertToMap(String matrixFileZip, String entrezId2indexFileZip, Path outfileName) {
        //load original data matrix from file sources (*really* slow)
        Map<Integer, Integer> index = createIndex(entrezId2indexFileZip);
        DataMatrix dataMatrix = loadDataMatrix(matrixFileZip, entrezId2indexFileZip, true);

        //create the new MVStore (fast!)
        MVStore mvStore = MVStore.open(outfileName.toAbsolutePath().toString());
        MVMap<Integer, Integer> rowIndex = mvStore.openMap("gene_id_row_index");
        MVMap<Integer, float[]> columns = mvStore.openMap("columns");
        // transpose the matrix and store the columns as rows in the map using the index key as the key.
        FloatMatrix floatMatrix = dataMatrix.getMatrix();
        for (Map.Entry<Integer, Integer> geneIdToIndex : index.entrySet()) {
            Integer geneId = geneIdToIndex.getKey();
            Integer indexKey = geneIdToIndex.getValue();
            //make the row index
            rowIndex.put(geneId, indexKey);
            //make the columns
            FloatMatrix col = floatMatrix.getColumn(indexKey);
            float[] colVals = col.toArray();
            columns.put(geneId, colVals);
        }
        mvStore.commit();
        mvStore.close();
    }

    public static DataMatrix loadDataMatrix(Path ppiMapPath) {
        logger.info("Loading PPI data matrix from map...");
        MVStore s = MVStore.open(ppiMapPath.toAbsolutePath().toString());
        Map<Integer, Integer> rowIndex = s.openMap("gene_id_row_index");
        Map<Integer, float[]> columns = s.openMap("columns");
        Map<Integer, Integer> index = new HashMap<>(rowIndex);
        FloatMatrix floatMatrix = createMatrixfromMap(rowIndex, columns);
        logger.info("Done - loaded {} * {} interactions.", floatMatrix.getRows(), floatMatrix.getColumns());
        return new DataMatrix(floatMatrix, index);
    }

    private static FloatMatrix createMatrixfromMap(Map<Integer, Integer> rowIndex, Map<Integer, float[]> columns) {
        FloatMatrix floatMatrix = new FloatMatrix(rowIndex.size(), rowIndex.size());

        for (Map.Entry<Integer, float[]> column : columns.entrySet()){
            Integer row = rowIndex.get(column.getKey());
            float[] columnValues = column.getValue();
            for (int i = 0; i < columnValues.length; i++) {
                float value = columnValues[i];
                //the matrix is stored as a list of columns, so we need to transpose the row and column values here
                floatMatrix.put(i, row, value);
            }
        }

        return floatMatrix;

    }

    public static DataMatrix loadDataMatrix(String matrixFileZip, String entrezId2indexFileZip, boolean shouldUseExponent) {
        Map<Integer, Integer> index = createIndex(entrezId2indexFileZip);
        FloatMatrix floatMatrix = createMatrixfromFile(index.size(), matrixFileZip, shouldUseExponent);
        return new DataMatrix(floatMatrix, index);
    }

    private static Map<Integer, Integer> createIndex(String object2idxFileZip) {
        Map<Integer, Integer> index = new HashMap<>();
        File indexFile = new File(object2idxFileZip);
        try (BufferedReader indexReader = gzippedFileBufferedReader(indexFile)) {
            String line;
            while ((line = indexReader.readLine()) != null) {
                addLineDataToIndex(line, index);
            }
        } catch (IOException e) {
            throw new DataMatrixIoException(e);
        }
        return index;
    }

    private static void addLineDataToIndex(String line, Map<Integer, Integer> index) {
        String[] split = line.split(TAB_DELIMITER);
        int objectId = Integer.parseInt(split[0]);
        int idx = Integer.parseInt(split[1]);
        index.put(objectId, idx);
    }

    private static FloatMatrix createMatrixfromFile(int matrixSize, String matrixFileZip, boolean shouldUseExponent) {
        FloatMatrix floatMatrix = new FloatMatrix(matrixSize, matrixSize);

        File matrixFile = new File(matrixFileZip);
        try (BufferedReader in = gzippedFileBufferedReader(matrixFile)) {
            int row = 0;
            String line;
            while ((line = in.readLine()) != null) {
                //just a nicety to stop you wondering if anything is going on - this stage takes a minute or two
                logLineNumberIfMultipleOf(row, 500);
                addLineDataToMatrixRow(floatMatrix, line, row, shouldUseExponent);
                row++;
            }
        } catch (IOException e) {
            throw new DataMatrixIoException(e);
        }
        return floatMatrix;
    }

    private static void logLineNumberIfMultipleOf(int lineNumber, int multiple) {
        if (lineNumber % multiple == 0) {
            logger.info("reading line {}", lineNumber);
        }
    }

    private static void addLineDataToMatrixRow(FloatMatrix floatMatrix, String line, int row, boolean shouldUseExponent) {
        String[] rowData = line.split(TAB_DELIMITER);
        int matrixSize = floatMatrix.rows;
        for (int j = 0; j < matrixSize; j++) {
            float entry = Float.parseFloat(rowData[j]);
            if (shouldUseExponent) {
                entry = (float) Math.exp(entry);
            }
            floatMatrix.put(row, j, entry);
        }
    }

    private static BufferedReader gzippedFileBufferedReader(File file) throws IOException {
        // if the gz-file exists we try to create a BufferedReader for this file
        BufferedReader bufferedReader;
        int bufferSizeInBytes = 1024;
        if (file.exists()) {
            InputStream inputStream = new GZIPInputStream(new FileInputStream(file), bufferSizeInBytes);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        } else {
            throw new DataMatrixIoException(new RuntimeException("FATAL: file not found " + file.getName()));
        }
        return bufferedReader;
    }

    /**
     * WARNING! This is a legacy method - it is not known whether this was used to generate the files read by the loadDataMatrix method.
     * @param dataMatrix
     * @param file
     * @param doLogarithm
     */
    public static void writeMatrix(DataMatrix dataMatrix, String file, boolean doLogarithm) {
        FloatMatrix matrix = dataMatrix.getMatrix();
        Map<String, Integer> id2index = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : dataMatrix.getEntrezIdToRowIndex().entrySet()) {
            id2index.put(entry.getKey().toString(), entry.getValue());
        }
        writeMatrix(matrix, file, id2index, doLogarithm);
    }

    /**
     * WARNING! This is a legacy method - it is not known whether this was used to generate the files read by the loadDataMatrix method.
     * @param matrix
     * @param file
     * @param id2index
     * @param doLogarithm
     */
    public static void writeMatrix(FloatMatrix matrix, String file, Map<String, Integer> id2index, boolean doLogarithm) {
        try (
                BufferedWriter outMatrix = new BufferedWriter(new FileWriter(file));
                BufferedWriter outIndexMap = new BufferedWriter(new FileWriter(file + "_id2index"))) {

            for (Map.Entry<String, Integer> entry : id2index.entrySet()) {
                String entrezId = entry.getKey();
                outIndexMap.write(entrezId + "\t" + entry.getValue() + "\n");
            }

            for (int i = 0; i < matrix.rows; i++) {
                for (int j = 0; j < matrix.columns; j++) {
                    if (j > 0) {
                        outMatrix.write("\t");
                    }
                    if (doLogarithm) {
                        outMatrix.write(String.format("%.5f", Math.log(matrix.get(i, j))));
                    } else {
                        outMatrix.write(String.format("%.5f", matrix.get(i, j)));
                    }
                }
                outMatrix.write("\n");
            }

        } catch (IOException e) {
            logger.info("Unable to write DataMatrix to file {}", file);
        }
    }

    /**
     * WARNING! This is a legacy method - it is not known whether this was used to generate the files read by the loadDataMatrix method.
     * @param dataMatrix
     * @param file
     * @param doLogarithm
     */
    public static void writeMatrixInclHeaderAndRowIDs(DataMatrix dataMatrix, String file, boolean doLogarithm) {
        FloatMatrix matrix = dataMatrix.getMatrix();
        Map<String, Integer> id2index = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : dataMatrix.getEntrezIdToRowIndex().entrySet()) {
            id2index.put(entry.getKey().toString(), entry.getValue());
        }
        writeMatrixInclHeaderAndRowIDs(matrix, file, id2index, doLogarithm);
    }

    /**
     * WARNING! This is a legacy method - it is not known whether this was used to generate the files read by the loadDataMatrix method.
     * @param matrix
     * @param file
     * @param id2index
     * @param doLogarithm
     */
    public static void writeMatrixInclHeaderAndRowIDs(FloatMatrix matrix, String file, Map<String, Integer> id2index, boolean doLogarithm) {
        try (BufferedWriter outMatrix = new BufferedWriter(new FileWriter(file))) {
            Map<Integer, String> index2id = new HashMap<>();
            for (Map.Entry<String, Integer> entry : id2index.entrySet()) {
                index2id.put(entry.getValue(), entry.getKey());
            }
            outMatrix.write("Key");
            for (int i = 0; i < id2index.size(); i++) {
                outMatrix.write(";" + index2id.get(i));
            }
            outMatrix.write("\n");

            for (int i = 0; i < matrix.rows; i++) {
                outMatrix.write(index2id.get(i));
                for (int j = 0; j < matrix.columns; j++) {
                    outMatrix.write(";");
                    if (doLogarithm) {
                        outMatrix.write(String.format("%.5f", Math.log(matrix.get(i, j))));
                    } else {
                        outMatrix.write(String.format("%.10f", matrix.get(i, j)));
                    }
                }

                outMatrix.write("\n");
            }
        } catch (IOException e) {
            logger.info("Unable to write DataMatrix with headers to file {}", file);
        }
    }

    public static class DataMatrixIoException extends RuntimeException {

        public DataMatrixIoException(Throwable cause) {
            super(cause);
        }

        public DataMatrixIoException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
