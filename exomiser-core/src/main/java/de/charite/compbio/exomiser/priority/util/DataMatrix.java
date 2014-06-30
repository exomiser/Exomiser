package de.charite.compbio.exomiser.priority.util;

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
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.jblas.DoubleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the random walk relationships and the entrez-id to index relations.
 *
 * @author sebastiankohler
 *
 */
public class DataMatrix {

    private final Logger logger = LoggerFactory.getLogger(DataMatrix.class);

    private Map<Integer, Integer> idx2objectid = new HashMap<>();
    private Map<Integer, Integer> objectid2idx = new HashMap<>();
    private DoubleMatrix data;
    private String matrixName;
    private static final Pattern splitPattern = Pattern.compile("\t");

    public DataMatrix(DoubleMatrix matrix, Map<Integer, Integer> id2index) {

        this.data = matrix;
        this.objectid2idx = id2index;
        for (int id : id2index.keySet()) {
            idx2objectid.put(id2index.get(id), id);
        }
    }

    public DataMatrix(String matrixFileZip, String object2idxFileZip, boolean haveToExponentiate) {
        try {

            File file = new File(object2idxFileZip);
            BufferedReader in = null;
            // if the gz-file exists we try to create a BufferedReader for this file
            if (file.exists()) {
                InputStream is = new GZIPInputStream(new FileInputStream(file));
                in = new BufferedReader(new InputStreamReader(is));
            } else {
                throw new RuntimeException("FATAL: file not found " + object2idxFileZip);
            }

            String line = null;
            while ((line = in.readLine()) != null) {

                String[] split = splitPattern.split(line);
                int objectId = Integer.parseInt(split[0]);
                int idx = Integer.parseInt(split[1]);
                idx2objectid.put(idx, objectId);
                objectid2idx.put(objectId, idx);
            }
            in.close();

            // set up matrix
            int matrixSize = idx2objectid.size();
            data = new DoubleMatrix(matrixSize, matrixSize);
            // fill matrix with data
            in = null;
            file = new File(matrixFileZip);
            // if the gz-file exists we try to create a BufferedReader for this file
            if (file.exists()) {
                InputStream is = new GZIPInputStream(new FileInputStream(file));
                in = new BufferedReader(new InputStreamReader(is));
            } else {
                throw new RuntimeException("FATAL: file not found " + matrixFileZip);
            }
            line = null;
            int i = 0;
            while ((line = in.readLine()) != null) {

                if (i % 500 == 0) {
                    logger.info("   -> read line {}", i);
                }

                String[] row = splitPattern.split(line);
                for (int j = 0; j < matrixSize; j++) {

                    double entry = Double.parseDouble(row[j]);
                    if (haveToExponentiate) {
                        entry = Math.exp(entry);
                    }

                    data.put(i, j, entry);
                }
                i++;
            }
            in.close();

            // set the name of the matrix
            matrixName = file.getName();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Integer, Integer> getIdx2objectid() {
        return idx2objectid;
    }

    public Map<Integer, Integer> getObjectid2idx() {
        return objectid2idx;
    }

    public DoubleMatrix getData() {
        return data;
    }

    public String getMatrixName() {
        return matrixName;
    }

    public void writeMatrix(DoubleMatrix walkMatrixAll, String file, Map<String, Integer> id2index, boolean doLogarithm) {
        try {
            BufferedWriter outMatrix = new BufferedWriter(new FileWriter(file));
            BufferedWriter outIndexMap = new BufferedWriter(new FileWriter(file + "_id2index"));

            for (String entrez : id2index.keySet()) {
                outIndexMap.write(entrez + "\t" + id2index.get(entrez) + "\n");
            }

            for (int i = 0; i < walkMatrixAll.rows; i++) {

                for (int j = 0; j < walkMatrixAll.columns; j++) {

                    if (j > 0) {
                        outMatrix.write("\t");
                    }

                    if (doLogarithm) {
                        outMatrix.write(String.format("%.5f", Math.log(walkMatrixAll.get(i, j))));
                    } else {
                        outMatrix.write(String.format("%.5f", walkMatrixAll.get(i, j)));

                    }
                }

                outMatrix.write("\n");
            }

            outMatrix.close();
            outIndexMap.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeMatrixInclHeaderAndRowIDs(DoubleMatrix walkMatrixAll, String file, Map<String, Integer> id2index, boolean doLogarithm) {

        try {

            BufferedWriter outMatrix = new BufferedWriter(new FileWriter(file));

            Map<Integer, String> index2id = new HashMap<Integer, String>();
            for (String s : id2index.keySet()) {
                int idx = id2index.get(s);
                index2id.put(idx, s);
            }
            outMatrix.write("Key");
            for (int i = 0; i < id2index.size(); i++) {
                outMatrix.write(";" + index2id.get(i));
            }
            outMatrix.write("\n");

            for (int i = 0; i < walkMatrixAll.rows; i++) {

                outMatrix.write(index2id.get(i));

                for (int j = 0; j < walkMatrixAll.columns; j++) {

                    outMatrix.write(";");

                    if (doLogarithm) {
                        outMatrix.write(String.format("%.5f", Math.log(walkMatrixAll.get(i, j))));
                    } else {
                        outMatrix.write(String.format("%.10f", walkMatrixAll.get(i, j)));

                    }
                }

                outMatrix.write("\n");
            }

            outMatrix.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
