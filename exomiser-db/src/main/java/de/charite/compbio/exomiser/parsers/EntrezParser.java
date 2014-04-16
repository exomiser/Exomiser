package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse the good old human-phenotype-ontology.obo file (or alternatively the
 * hp.obo file from our Hudson server). We want to create a table in the
 * database with lcname - HP:id - preferred name, where lcname is the lower-case
 * name or synonym, ID is the HPO id, and preferred name is the HPO Term name.
 * We lower-case the name ans synonyms to be able to search only over lower
 * cased names for the autosuggestion. However, we want to display the preferred
 * name in the end.
 *
 * @version 0.04 (27 November, 2013)
 * @author Peter Robinson
 */
public class EntrezParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(EntrezParser.class);
    private HashMap<String, ArrayList<Integer>> ensembl2EntrezGene;
    
    /**
     * @param conn COnnection to the Exomiser database.
     */
    public EntrezParser(HashMap<String, ArrayList<Integer>> ensembl2EntrezGene) {
        this.ensembl2EntrezGene = ensembl2EntrezGene;
    }

    /**
     * This function does the actual work of parsing the HPO file.
     *
     * @param inPath Complete path to string file.
     * @param outPath PAth where output file is to be written
     */
    @Override
    public ResourceOperationStatus parse(String inPath, String outPath) {

        // hack to cope with 2 files needed for StringParser but 2 output files generated - refactor when have working model
        logger.info(inPath);
        logger.info(outPath);
        logger.info("Parsing Entrez gene to  file: {}. Writing out to: {}", "data/extracted/ensembl_biomart.txt", "data/entrez2sym.pg");
        HashMap<Integer, String> entrez2sym = new HashMap<>();
        try (FileReader fileReader = new FileReader(inPath);
                BufferedReader br = new BufferedReader(fileReader);
                FileWriter fileWriter = new FileWriter(new File(outPath));
                BufferedWriter writer = new BufferedWriter(fileWriter)) {            
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
                    ArrayList<Integer> test = this.ensembl2EntrezGene.get(ens);
                    if (!test.contains(entrez)) {
                        test.add(entrez);
                    }
                } else {
                    ArrayList<Integer> lst = new ArrayList<Integer>();
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
/*
 * eof
 */
