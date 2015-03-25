package de.charite.compbio.exomiser.core.prioritisers.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.jblas.FloatMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the random walk relationships and the entrez-id to index relations.
 *
 * @author sebastiankohler
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class DataMatrix {

    private static final Logger logger = LoggerFactory.getLogger(DataMatrix.class);

    private final Map<Integer, Integer> rowToEntrezIdIndex = new HashMap<>();
    private Map<Integer, Integer> entrezIdToRowIndex = new HashMap<>();
    private FloatMatrix matrix;
    private String name = "";
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\t");

    public DataMatrix(FloatMatrix matrix, Map<Integer, Integer> entrezIdToRowIndex) {
        this.matrix = matrix;
        this.entrezIdToRowIndex = entrezIdToRowIndex;
        for (Entry<Integer, Integer> entrezIdToRow : entrezIdToRowIndex.entrySet()) {
            rowToEntrezIdIndex.put(entrezIdToRow.getValue(), entrezIdToRow.getKey());
        }
    }

    public DataMatrix(String matrixFileZip, String entrezId2indexFileZip, boolean shouldUseExponent) {

        setUpIndexAndCreateEmptyMatrix(entrezId2indexFileZip);

        fillMatrixfromFile(matrixFileZip, shouldUseExponent);

        name = matrixFileZip;
    }

    private void setUpIndexAndCreateEmptyMatrix(String object2idxFileZip) {
        File indexFile = new File(object2idxFileZip);
        try (BufferedReader indexReader = gzippedFileBufferedReader(indexFile)) {
            String line;
            while ((line = indexReader.readLine()) != null) {
                addLineDataToIndexes(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setUpMatrix();
    }

    private void addLineDataToIndexes(String line) {
        String[] split = SPLIT_PATTERN.split(line);
        int objectId = Integer.parseInt(split[0]);
        int idx = Integer.parseInt(split[1]);
        rowToEntrezIdIndex.put(idx, objectId);
        entrezIdToRowIndex.put(objectId, idx);
    }

    private void setUpMatrix() {
        int matrixSize = rowToEntrezIdIndex.size();
        matrix = new FloatMatrix(matrixSize, matrixSize);
    }
    
    private void fillMatrixfromFile(String matrixFileZip, boolean shouldUseExponent) {
        File matrixFile = new File(matrixFileZip);
        try (BufferedReader in = gzippedFileBufferedReader(matrixFile)) {
            int i = 0;
            String line;
            while ((line = in.readLine()) != null) {
                //just a nicety to stop you wondering if anything is going on - this stage takes a minute or two
                logLineNumberIfMultipleOf500(i);
                addLineDataToMatrixRow(line, i, shouldUseExponent);
                i++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void logLineNumberIfMultipleOf500(int i) {
        if (i % 500 == 0) {
            logger.info("reading line {}", i);
        }
    }

    private void addLineDataToMatrixRow(String line, int i, boolean shouldUseExponent) {
        String[] row = SPLIT_PATTERN.split(line);
        int matrixSize = matrix.rows;
        for (int j = 0; j < matrixSize; j++) {
            float entry = Float.parseFloat(row[j]);
            if (shouldUseExponent) {
                entry = (float) Math.exp(entry);
            }
            matrix.put(i, j, entry);
        }
    }

    private BufferedReader gzippedFileBufferedReader(File file) throws IOException {
        // if the gz-file exists we try to create a BufferedReader for this file
        BufferedReader bufferedReader;
        int bufferSizeInBytes = 2048;
        if (file.exists()) {
            InputStream inputStream = new GZIPInputStream(new FileInputStream(file), bufferSizeInBytes);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        } else {
            throw new RuntimeException("FATAL: file not found " + file.getName());
        }
        return bufferedReader;
    }

    public Map<Integer, Integer> getEntrezIdToRowIndex() {
        return entrezIdToRowIndex;
    }

    public FloatMatrix getMatrix() {
        return matrix;
    }

    public String getName() {
        return name;
    }

    public boolean containsGene(Integer entrezGeneId) {
        return entrezIdToRowIndex.containsKey(entrezGeneId);
    }
    
    public Integer getRowIndexForGene(int entrezGeneId) {
        return entrezIdToRowIndex.get(entrezGeneId);
    }
    
    public FloatMatrix getColumnMatrixForGene(int entrezGeneId) {
        Integer rowIndex = entrezIdToRowIndex.get(entrezGeneId);
        return matrix.getColumn(rowIndex);
    }
    
    public static void writeMatrix(FloatMatrix matrix, String file, Map<String, Integer> id2index, boolean doLogarithm) {
        try (
                BufferedWriter outMatrix = new BufferedWriter(new FileWriter(file));
                BufferedWriter outIndexMap = new BufferedWriter(new FileWriter(file + "_id2index"))) {

            for (String entrezId : id2index.keySet()) {
                outIndexMap.write(entrezId + "\t" + id2index.get(entrezId) + "\n");
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

    public static void writeMatrixInclHeaderAndRowIDs(FloatMatrix matrix, String file, Map<String, Integer> id2index, boolean doLogarithm) {
        try (BufferedWriter outMatrix = new BufferedWriter(new FileWriter(file))) {
            Map<Integer, String> index2id = new HashMap<>();
            for (String s : id2index.keySet()) {
                int idx = id2index.get(s);
                index2id.put(idx, s);
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

}
