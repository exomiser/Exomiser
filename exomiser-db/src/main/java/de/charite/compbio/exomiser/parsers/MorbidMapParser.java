/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.core.InheritanceMode;
import de.charite.compbio.exomiser.io.FileOperationStatus;
import jannovar.common.Constants;
import java.io.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the OMIM morbid map file.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MorbidMapParser implements Parser {

    private static final Logger logger = LoggerFactory.getLogger(MorbidMapParser.class);

    private final DiseaseInheritanceCache diseaseInheritanceCache;
    private final Map<Integer, Set<Integer>> mim2geneMap;

    public MorbidMapParser(DiseaseInheritanceCache diseaseInheritanceCache, Map<Integer, Set<Integer>> mim2geneMap) {
        this.diseaseInheritanceCache = diseaseInheritanceCache;
        this.mim2geneMap = mim2geneMap;
    }

    @Override
    public FileOperationStatus parse(String inPath, String outPath) {

        if (diseaseInheritanceCache.isEmpty()) {
            logger.error("Aborting attempt to parse morbidmap file as the required DiseaseInheritanceCache is empty.");
            return FileOperationStatus.FAILURE;
        }
        // Parse morbidmap file
        List<MIM> mimList = parseMorbidMap(inPath, mim2geneMap);
        if (mimList.isEmpty()) {
            logger.error("Error parsing morbidmap file. Expected some data to write out but there is none.");
            return FileOperationStatus.FAILURE;
        }
        //write out the list
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
            for (MIM mim : mimList) {
                writer.write(mim.dumpLine());
            }
        
        } catch (IOException e) {
            logger.error("Error parsing morbidmap file: {}", inPath, e);
            return FileOperationStatus.FAILURE;
        }
        
        logger.info("Wrote {} OMIM entries to outfile: {}", mimList.size(), outPath);
        
        return FileOperationStatus.SUCCESS;
    }

    /**
     * This function parses the Morbid Map of OMIM and creates one
     * {@link exomizer.io.MIMParser.MIM MIM} object per line. However, the
     * function is called after we have already parsed the mim2gene file, and a
     * {@link exomizer.io.MIMParser.MIM MIM} is only created if we have an
     * Entrez Gene id for the gene in question.
     */
    private List<MIM> parseMorbidMap(String morbidMapPath, Map<Integer, Set<Integer>> mim2geneMap) {
        logger.info("Parsing morbidMap file: {}", morbidMapPath);
        List<MIM> mimList = new ArrayList<>();
        // A heuristic to avoid duplicate entries. TODO refactor
        Set<Integer> seen = new HashSet<>();

        try (FileReader fileReader = new FileReader(morbidMapPath);
                BufferedReader br = new BufferedReader(fileReader)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; // comment.
                }
                String fields[] = line.split("\\|");
                if (fields.length != 4) {
                    logger.error("Malformed morbid map line: {}", line);
                    logger.error("Expected 4 fields per line but got {}", fields.length);
                    break;
                }

                String disease = null;
                String phenMIM = null;
                Integer phenID = null;
                String diseaseString = fields[0]; /* e.g., 17,20-lyase deficiency, isolated, 202110 (3) */

                logger.debug("diseaseString = {}", diseaseString);
                int i = diseaseString.lastIndexOf(',');
                if (i > 0) { /* This means there is probably a MIM number */

                    disease = diseaseString.substring(0, i).trim();
                    logger.debug("disease = {}", disease);
                    phenMIM = diseaseString.substring(i + 1).trim();
                    logger.debug("phenMIM = {}", phenMIM);
                    i = phenMIM.indexOf('(');
                    if (i > 0) {
                        phenMIM = phenMIM.substring(0, i).trim();
                    }
                    try {
                        phenID = Integer.parseInt(phenMIM);
                    } catch (NumberFormatException e) {
                        //System.out.println("Could not parse phenMIM: " + phenMIM);
                        // Note by inspection, these lines have no valid phenMIM, it is ok to skip them.
                        phenID = Constants.UNINITIALIZED_INT;
                    }
                    logger.debug("2 disease=" + disease);
                    logger.debug("2 phenMIM=" + phenMIM);
                } else {
                    disease = diseaseString.trim();
                    phenID = Constants.UNINITIALIZED_INT;
                }
                Integer genemim = Integer.parseInt(fields[2]);
                logger.debug("genemim = {}", genemim);

                Set<Integer> entrezList = mim2geneMap.get(genemim);
                if (logger.isDebugEnabled()) {
                    logger.debug("mim2geneMap size = {}", mim2geneMap.size());
                    if (entrezList == null) {
                        logger.debug("entrez list is null");
                    } else {
                        logger.debug("entrez list size=" + entrezList.size());
                    }
                }
                if (entrezList == null || entrezList.isEmpty()) {
                    continue; /* No entrez gene link */

                }
                if (genemim < 0) {
                    continue;
                }
                String diseaseType = getDiseaseType(disease);
                disease = getDiseaseName(disease);
                InheritanceMode inh = diseaseInheritanceCache.getInheritanceMode(phenID);
                for (Integer id : entrezList) {
                    Integer unique = phenID + genemim + id;
                    if (!seen.contains(unique)) {
                        seen.add(unique);
                        MIM mim = new MIM(phenID, genemim, disease, id, diseaseType, inh);
                        mimList.add(mim);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error parsing morbid map file: " + e.getMessage());
        }
        logger.info("Extracted {} OMIM terms from morbidmap", mimList.size());
        return mimList;
    }

    /**
     * A simple struct-like class that encapsulates a single OMIM
     * phenotype/gene/Entrez-Gene-ID association.
     */
    private class MIM {

        /**
         * MIM id for the phenotype
         */
        private final int diseaseId;
        /**
         * MIM Id for a gene corresponding to the phenotype entry
         */
        private final  int omimGeneId;
        /**
         * Name of the disease corresponding to the phenotype entry
         */
        private final  String diseaseName;
        /**
         * NCBI Entrez Gene ID corresponding to the gene entry.
         */
        private final  int entrezGeneId;
        /**
         * One of: 'N', non-disease; 'S', susceptibility to multifactorial
         * disorders, '?' an unconfirmed or possibly spurious mapping; 'C', a
         * chromosome deletion or duplication syndrome, or 'D', default, a
         * Mendelian disease.
         */
        private final  String diseaseType;
        /**
         * One of 'D' for autosomal Dominant, 'R' for autosomal recessive, 'B'
         * for both D and R, 'X' for X chromosomal, and 'U' for unknown.
         */
        private final InheritanceMode inheritanceMode;

        MIM(int diseaseId, int omimGeneId, String diseaseName, int entrezGeneId, String diseaseType, InheritanceMode inheritanceMode) {
            this.diseaseId = diseaseId;
            this.omimGeneId = omimGeneId;
            this.diseaseName = diseaseName;
            this.entrezGeneId = entrezGeneId;
            this.diseaseType = diseaseType;
            this.inheritanceMode = inheritanceMode;
        }

        /**
         * @return a line that will serve to be imported into the postgreSQL
         * database of the Exomizer for OMIM associations.
         */
        String dumpLine() {
            return String.format("OMIM:%d|OMIM:%d|%s|%d|%s|%s%n", diseaseId, omimGeneId, diseaseName, entrezGeneId, diseaseType, inheritanceMode.getInheritanceCode());
        }

    }

    /**
     * Return a one-letter string representing the OMIM disease type. We use a
     * string instead of a char because this works better for JDBC, and the
     * database table will wind up with a CHAR anyway.
     */
    private String getDiseaseType(String disease) {
        int i;
        i = disease.indexOf('[');
        if (i == 0) {
            return "N"; /* Brackets, "[ ]", indicate "nondiseases */

        }
        i = disease.indexOf('{');
        if (i == 0) {
            return "S"; /*Braces, "{ }", indicate mutations that contribute to susceptibility to multifactorial disorders */

        }
        i = disease.indexOf('?');
        if (i == 0) {
            return "?"; /* A question mark, "?", before the disease name indicates an unconfirmed or possibly spurious mapping. */

        }
        if (disease.contains("(4)")) {
            return "C"; /* (4) the disorder is a chromosome deletion or duplication syndrome.  */

        }
        /* Default */
        return "D";
    }

    /**
     * Extract the disease name from the weird OMIM morbid map format.
     *
     * @return Name of the disease in a morbidmap entry.
     */
    private String getDiseaseName(String disease) {
        int i;
        String d;
        i = disease.indexOf('[');
        if (i == 0) {
            d = disease.substring(1); /* remove first [ */

            i = d.lastIndexOf(']');
            if (i > 0) {
                return d.substring(0, i);
            } else {
                return d;
            }
        }
        i = disease.indexOf('{');
        if (i == 0) {
            d = disease.substring(1); /* remove first { */

            i = d.lastIndexOf('}');
            if (i > 0) {
                return d.substring(0, i);
            } else {
                return d;
            }
        }
        /* Leave the '?' unchanged */
        i = disease.lastIndexOf('(');
        if (i > 0) {
            return disease.substring(0, i);
        }
        /* If we get here, just return the string */
        return disease.trim();
    }
}
