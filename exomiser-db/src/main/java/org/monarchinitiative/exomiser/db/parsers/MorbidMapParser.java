/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.db.parsers;

import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parses the OMIM morbid map file.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MorbidMapParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(MorbidMapParser.class);

    private static final int NO_DISEASE_ID = -10;
    
    private final DiseaseInheritanceCache diseaseInheritanceCache;
    private final Map<Integer, Set<Integer>> mim2geneMap;

    public MorbidMapParser(DiseaseInheritanceCache diseaseInheritanceCache, Map<Integer, Set<Integer>> mim2geneMap) {
        this.diseaseInheritanceCache = diseaseInheritanceCache;
        this.mim2geneMap = mim2geneMap;
    }

    /**
     * This function parses the Morbid Map of OMIM and creates one
     * {@link exomizer.io.MIMParser.MIM MIM} object per line. However, the
     * function is called after we have already parsed the mim2gene file, and a
     * {@link exomizer.io.MIMParser.MIM MIM} is only created if we have an
     * Entrez Gene id for the gene in question.
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        ResourceOperationStatus status;

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);

        if (diseaseInheritanceCache.isEmpty()) {
            logger.error("Aborting attempt to parse morbidmap file as the required DiseaseInheritanceCache is empty.");
            status = ResourceOperationStatus.FAILURE;
            resource.setParseStatus(status);
            logger.info("{}", status);
            return;
        }
        
        if (mim2geneMap.isEmpty()) {
            logger.error("Aborting attempt to parse morbidmap file as the required mim2geneMap is empty.");
            status = ResourceOperationStatus.FAILURE;
            resource.setParseStatus(status);
            logger.info("{}", status);
            return;
        }
        
        // A heuristic to avoid duplicate entries.
        Set<String> seen = new HashSet<>();
        
        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.defaultCharset());
                BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())){

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue; // comment.
                }
                String[] fields = line.split("\\|");
                if (fields.length != 4) {
                    logger.error("Malformed morbid map line: {}", line);
                    logger.error("Expected 4 fields per line but got {}", fields.length);
                    break;
                }
                logger.debug("Line: {}", line);
                String disease;
                String phenMIM;
                Integer phenID;
                String diseaseString = fields[0]; /* e.g., 17,20-lyase deficiency, isolated, 202110 (3) */

                logger.debug("diseaseString = {}", diseaseString);
                int i = diseaseString.lastIndexOf(',');
                /* This means there is probably a MIM number */
                if (i > 0) { 

                    disease = diseaseString.substring(0, i).trim();
                    phenMIM = diseaseString.substring(i + 1).trim();
                    i = phenMIM.indexOf('(');
                    if (i > 0) {
                        phenMIM = phenMIM.substring(0, i).trim();
                    }
                    try {
                        phenID = Integer.parseInt(phenMIM);
                    } catch (NumberFormatException e) {
                        //System.out.println("Could not parseResource phenMIM: " + phenMIM);
                        // Note by inspection, these lines have no valid phenMIM, it is ok to skip them.
                        phenID = NO_DISEASE_ID;
                    }
                    logger.debug("disease = {}", disease);
                    logger.debug("phenMIM = {}", phenMIM);
                    logger.debug("phenID = {}", phenID);
                } else {
                    disease = diseaseString.trim();
                    phenID = NO_DISEASE_ID;
                    logger.debug("disease = {}", disease);
                    logger.debug("phenID = {}", phenID);
                }
                Integer genemim = Integer.parseInt(fields[2]);
                logger.debug("genemim = {}", genemim);

                Set<Integer> associatedEntrezGeneIds = mim2geneMap.get(genemim);
                if (logger.isDebugEnabled()) {
                    logger.debug("mim2geneMap size = {}", mim2geneMap.size());
                    if (!mim2geneMap.containsKey(genemim)) {
                        logger.debug("No known genes associated with OMIM geneId {}", genemim);
                    } else {
                        logger.debug("Entrez gene ids associated with OMIM geneId {}: {}", genemim, associatedEntrezGeneIds);
                    }
                }
                if (associatedEntrezGeneIds == null || associatedEntrezGeneIds.isEmpty()) {
                    // No entrez gene link
                    continue; 
                }
                if (genemim < 0) {
                    continue;
                }
                String diseaseType = getDiseaseType(disease);
                disease = getDiseaseName(disease);
                InheritanceMode inh = diseaseInheritanceCache.getInheritanceMode(phenID);
                for (Integer id : associatedEntrezGeneIds) {
                    //TODO: this isn't unique - there could be a clash
                    String unique = phenID.toString() + genemim.toString() + id.toString();
                    logger.debug("unique={}", unique);
                    if (!seen.contains(unique)) {
                        seen.add(unique);
                        MIM mim = new MIM(phenID, genemim, disease, id, diseaseType, inh);
                        logger.debug("Adding MIM to file: {}", mim.dumpLine());
                        writer.write(mim.dumpLine());
                    }
                }
            }
            logger.info("Extracted {} OMIM terms from morbidmap", seen.size());

            status = ResourceOperationStatus.SUCCESS;
        } catch (FileNotFoundException ex) {
            logger.error("Error parsing morbid map file", ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error("Error parsing morbid map file", ex);
            status = ResourceOperationStatus.FAILURE;
        }
        resource.setParseStatus(status);
        logger.info("{}", status);
        
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
