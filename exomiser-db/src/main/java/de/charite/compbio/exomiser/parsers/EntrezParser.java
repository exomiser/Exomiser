package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class EntrezParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(EntrezParser.class);
    private final HashMap<String, List<Integer>> ensembl2EntrezGene;
    
    /**
     * @param ensembl2EntrezGene
     */
    public EntrezParser(HashMap<String, List<Integer>> ensembl2EntrezGene) {
        this.ensembl2EntrezGene = ensembl2EntrezGene;
    }

    /**
     * This function does the actual work of parsing the Entrez data.
     *
     * @param inPath Complete path to string file.
     * @param outPath Path where output file is to be written
     * @return the ResourceOperationStatus
     */
    @Override
    public ResourceOperationStatus parse(String inPath, String outPath) {

        logger.info("Parsing Entrez gene to  file: {}. Writing out to: {}", inPath, outPath);
        HashMap<Integer, String> entrez2sym = new HashMap<>();
        try (
                BufferedReader br = new BufferedReader(new FileReader(inPath));
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outPath)))) {            
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("\t");
                if (split.length != 3) {
                    continue;
                }
                String ens = split[0];
                Integer entrez = null;
                if (split[1].equals("")) {
                    continue;
                }
                try {
                    entrez = Integer.parseInt(split[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Error: malformed line: " + line + " (could not parse entrez gene field: " + split[1]);
                    System.exit(1);
                }
                
                if (split[2] == null || split[2].isEmpty()) {
                    System.err.println("[WARN- could not extract symbol, skipping line: " + line);
                }
                String symbol = split[2];
                entrez2sym.put(entrez, symbol);
                if (this.ensembl2EntrezGene.containsKey(ens)) {
                    List<Integer> test = this.ensembl2EntrezGene.get(ens);
                    if (!test.contains(entrez)) {
                        test.add(entrez);
                    }
                } else {
                    List<Integer> lst = new ArrayList<>();
                    lst.add(entrez);
                    this.ensembl2EntrezGene.put(ens, lst);
                }
            }

            Iterator<Integer> it = entrez2sym.keySet().iterator();
            while (it.hasNext()) {
                Integer id = it.next();
                if (id == null) {
                    continue;
                }
                String sym = entrez2sym.get(id);
                writer.write(String.format("%s|%s", id, sym));
                writer.newLine();
            }
            writer.close();
            br.close();
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            return ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            return ResourceOperationStatus.FAILURE;
        }
        return ResourceOperationStatus.SUCCESS;
    }
}
