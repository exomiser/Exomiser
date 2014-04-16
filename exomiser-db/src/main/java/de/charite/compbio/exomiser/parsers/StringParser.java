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
public class StringParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(StringParser.class);
    private HashMap<String, ArrayList<Integer>> ensembl2EntrezGene;
    private HashSet<Interaction> interactionSet = new HashSet<Interaction>();

    /**
     * @param conn COnnection to the Exomiser database.
     */
    public StringParser(HashMap<String, ArrayList<Integer>> ensembl2EntrezGene) {
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
        logger.info("Parsing String file: {}. Writing out to: {}", inPath, outPath);
        try (FileReader fileReader = new FileReader(inPath);
            BufferedReader br = new BufferedReader(fileReader);
            FileWriter fileWriter = new FileWriter(new File(outPath));
            BufferedWriter writer = new BufferedWriter(fileWriter)){
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("protein1")) {
                    continue;
                }
                String split[] = line.split("\\s+");
                String p1 = null, p2 = null;
                if (split[0].substring(0, 5).equals("9606.")) {
                    p1 = split[0].substring(5);
                } else {
                    System.err.println("Malformed protein (p1): " + line);
                    System.exit(1);
                }
                if (split[1].substring(0, 5).equals("9606.")) {
                    p2 = split[1].substring(5);
                } else {
                    System.err.println("Malformed protein (p2): " + line);
                    System.exit(1);
                }
                Integer score = null;
                try {
                    score = Integer.parseInt(split[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Malformed score: " + line + "\n\t(could not parse field: \"" + split[2] + "\"");
                    System.exit(1);
                }
                ArrayList<Integer> e1 = this.ensembl2EntrezGene.get(p1);
                ArrayList<Integer> e2 = this.ensembl2EntrezGene.get(p2);
                if (e1 == null || e2 == null) {
                    /*
                     * cannot find entrezgene id, just skip
                     */
                    continue;
                }
                if (score < 700) {
                    continue;
                }
                for (Integer a : e1) {
                    for (Integer b : e2) {
                        Interaction ita = new Interaction(a, b, score);
                        //System.out.println(a + " / " + b + "(" + score + ")");
                        if (!this.interactionSet.contains(ita)) {
                            writer.write(String.format("%s|%s|%s", a, b, score));
                            writer.newLine();
                            this.interactionSet.add(ita);
                        }
                    }
                }
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

    /**
     * A simple struct-like class representing an interaction.
     */
    class Interaction {

        int entrezGeneA;
        int entrezGeneB;
        int score;

        public Interaction(int A, int B, int sc) {
            this.entrezGeneA = A;
            this.entrezGeneB = B;
            this.score = sc;
        }

        public String getDumpLine() {
            return String.format("%d|%d|%d", entrezGeneA, entrezGeneB, score);
        }

        /**
         * We regard two interaction objects as being equal if both of the
         * interactants are the same. Note we are not interested in the score
         * and will take one or other of the scores arbitrarily if we find
         * objects that are equal like this while constructing the hashmap of
         * interactions.
         */
        @Override
        public boolean equals(Object obj) {

            StringParser.Interaction other = (StringParser.Interaction) obj;
            if (this == other) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (other.entrezGeneA == this.entrezGeneA && other.entrezGeneB == this.entrezGeneB) {
                return true;
            }
            if (other.entrezGeneB == this.entrezGeneA && other.entrezGeneA == this.entrezGeneB) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int x = 37;
            x += 17 * entrezGeneA;
            x += 17 * entrezGeneB;
            return x + 13;
        }
    }
}
/*
 * eof
 */
