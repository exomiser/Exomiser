/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.data.phenotype.parsers;

import org.monarchinitiative.exomiser.core.prioritisers.model.Disease.DiseaseType;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus;
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
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the OMIM morbid map file.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MorbidMapParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(MorbidMapParser.class);

    private static final int NO_DISEASE_ID = -10;
    private static final Pattern PHEN_MIM_PATTERN = Pattern.compile(", [0-9]{6} \\([1-4]\\)");

    private final DiseaseInheritanceCache diseaseInheritanceCache;
    private final Map<Integer, Integer> mim2geneMap;

    public MorbidMapParser(DiseaseInheritanceCache diseaseInheritanceCache, Map<Integer, Integer> mim2geneMap) {
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
             BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            String line;
            // expected format
            // # Phenotype     Gene Symbols    MIM Number      Cyto Location
            // 17,20-lyase deficiency, isolated, 202110 (3)    CYP17A1, CYP17, P450C17 609300  10q24.32
            while ((line = reader.readLine()) != null) {
                logger.debug("{}", line);
                if (line.startsWith("#")) {
                    continue; // comment.
                }
                String[] fields = line.split("\\t");
                if (fields.length != 4) {
                    logger.error("Malformed morbid map line: {}", line);
                    logger.error("Expected 4 fields per line but got {}", fields.length);
                    break;
                }
                OmimDisease omimDisease = parseLine(line);

                // The omimDiseases might differ purely by the name for a syndrome subtype
                // e.g. OMIM:601198 - HYPOCALCEMIA, AUTOSOMAL DOMINANT 1 and HYPOCALCEMIA, AUTOSOMAL DOMINANT 1, WITH BARTTER SYNDROME, INCLUDED
                // e.g. OMIM:110100 - BPES, TYPE I, INCLUDED  and BPES, TYPE II, INCLUDED
                String unique = omimDisease.diseaseId + "-" + omimDisease.omimGeneId + "-" + omimDisease.entrezGeneId;
                if (omimDisease.entrezGeneId != 0 && !seen.contains(unique)) {
                    seen.add(unique);
                    logger.debug("Adding MIM to file: {}", omimDisease);
                    writer.write(omimDisease.dumpLine());
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

    protected OmimDisease parseLine(String line) {
        String[] fields = line.split("\\t");
        if (fields.length != 4) {
            logger.error("Malformed morbid map line: {}", line);
            logger.error("Expected 4 fields per line but got {}", fields.length);
            throw new IllegalArgumentException("Malformed morbid map line:" + line);
        }

        // e.g.
        // 17,20-lyase deficiency, isolated, 202110 (3)
        // Leukemia, acute T-cell (2)
        // Trypsinogen deficiency, 614044 (1)
        // Williams-Beuren syndrome (4)
        // [Blood group, Rhesus] (3)
        // [Hex A pseudodeficiency], 272800 (3)
        // [High density lipoprotein cholesterol level QTL 1] (2)
        // [High density lipoprotein cholesterol level QTL 12], 612797 (3)
        String diseaseToken = fields[0];

        int diseaseId = parseDiseaseId(diseaseToken);
        String diseaseName = parseDiseaseName(diseaseToken, diseaseId);
        DiseaseType diseaseType = parseDiseaseType(diseaseToken);
        Integer geneMim = Integer.valueOf(fields[2]);
        Integer entrezGeneId = mim2geneMap.getOrDefault(geneMim, 0);
        if (entrezGeneId.equals(0)) {
            // No entrez gene link - this is permissible e.g. in the case of phenotype MIM codes
            logger.debug("No associated geneId for {}", line);
        }
        InheritanceMode inheritanceMode = diseaseInheritanceCache.getInheritanceMode(diseaseId);
        return new OmimDisease(diseaseId, geneMim, diseaseName, entrezGeneId, diseaseType, inheritanceMode);
    }

    private int parseDiseaseId(String diseaseToken) {
        Matcher matcher = PHEN_MIM_PATTERN.matcher(diseaseToken);
        // [High density lipoprotein cholesterol level QTL 1] (2) -> '' (invalid ID, can't be empty)
        // [High density lipoprotein cholesterol level QTL 12], 612797 (3) -> 612797
        // Myoclonic epilepsy, infantile, familial, 605021 (3) -> 605021
        // Myoclonic epilepsy, juvenile, 4 (2) -> 4 (invalid ID, should be a six-figure int)

        if (matcher.find()) {
            // trim off the preceding ', ' and trailing ' (2)' from the match
            String phenMim = diseaseToken.substring(matcher.start() + 2, matcher.end() - 4);
            try {
                return Integer.parseInt(phenMim);
            } catch (NumberFormatException e) {
                // shouldn't throw one of these, but if it does carry on
            }
        }
        // Note by inspection, these lines have no valid phenMIM, it is ok to skip them.
        return NO_DISEASE_ID;
    }

    /**
     * Return a one-letter string representing the OMIM disease type. We use a
     * string instead of a char because this works better for JDBC, and the
     * database table will wind up with a CHAR anyway.
     */
    private DiseaseType parseDiseaseType(String diseaseToken) {
        // https://omim.org/help/faq#1_6
        if (diseaseToken.indexOf('[') == 0) {
            // Brackets, "[ ]", indicate "nondiseases
            return DiseaseType.NON_DISEASE;
        }
        if (diseaseToken.indexOf('{') == 0) {
            // Braces, "{ }", indicate mutations that contribute to susceptibility to multifactorial disorders
            return DiseaseType.SUSCEPTIBILITY;
        }
        if (diseaseToken.indexOf('?') == 0) {
            // A question mark, "?", before the disease name indicates an unconfirmed or possibly spurious mapping.
            // n.b. this can also happen inside [] or {}, so really the unconfirmed ought to be a distinct flag, not a type.
            // {?Epidermodysplasia verruciformis, susceptibility to, 4}, 618307 (3)	RHOH, ARHH, TTF	602037	4p14
            // [?Hypertryptophanemia], 600627 (3)	TDO2, TPH2, TRPO, HYPTRP	191070	4q32.1
            return DiseaseType.UNCONFIRMED;
        }
        if (diseaseToken.endsWith("(4)")) {
            // (4) the disorder is a chromosome deletion or duplication syndrome.
            return DiseaseType.CNV;
        }
        // Default
        return DiseaseType.DISEASE;
    }

    /**
     * Extract the disease name from the weird OMIM morbid map format.
     *
     * @return Name of the disease in a morbidmap entry.
     */
    private String parseDiseaseName(String diseaseToken, int diseaseId) {
        String diseaseName = trimDiseaseId(diseaseToken, diseaseId);

        if (diseaseName.indexOf('[') == 0) {
            return trimEnclosingChars(diseaseName, ']');
        }
        if (diseaseName.indexOf('{') == 0) {
            return trimEnclosingChars(diseaseName, '}');
        }
        // Leave the '?' unchanged
        // If we get here, just return the string
        return diseaseName.trim();
    }

    private String trimDiseaseId(String diseaseToken, int diseaseId) {
        if (diseaseId == NO_DISEASE_ID) {
            // e.g.
            // [High density lipoprotein cholesterol level QTL 1] (2) -> [High density lipoprotein cholesterol level QTL 1]
            // Myoclonic epilepsy, juvenile, 4 (2) -> Myoclonic epilepsy, juvenile, 4
            int i = diseaseToken.lastIndexOf('(');
            if (i > 0) {
                return diseaseToken.substring(0, i).trim();
            }
            // shouldn't happen
            return diseaseToken;
        }
        // [High density lipoprotein cholesterol level QTL 12], 612797 (3) -> [High density lipoprotein cholesterol level QTL 12]
        // Myoclonic epilepsy, infantile, familial, 605021 (3) -> Myoclonic epilepsy, infantile, familial
       return diseaseToken.substring(0, diseaseToken.lastIndexOf(','));
    }

    private String trimEnclosingChars(String diseaseName, char c) {
        int i = diseaseName.lastIndexOf(c);
        if (i > 0) {
            // remove first and last ]
            return diseaseName.substring(1, i);
        } else {
            // remove first [
            return diseaseName.substring(1);
        }
    }

    /**
     * A simple struct-like class that encapsulates a single OMIM
     * phenotype/gene/Entrez-Gene-ID association.
     */
    protected static class OmimDisease {

        /**
         * MIM id for the phenotype
         */
        private final int diseaseId;
        /**
         * MIM Id for a gene corresponding to the phenotype entry
         */
        private final int omimGeneId;
        /**
         * Name of the disease corresponding to the phenotype entry
         */
        private final String diseaseName;
        /**
         * NCBI Entrez Gene ID corresponding to the gene entry.
         */
        private final int entrezGeneId;
        /**
         * One of: 'N', non-disease; 'S', susceptibility to multifactorial
         * disorders, '?' an unconfirmed or possibly spurious mapping; 'C', a
         * chromosome deletion or duplication syndrome, or 'D', default, a
         * Mendelian disease.
         */
        private final DiseaseType diseaseType;
        /**
         * One of 'D' for autosomal Dominant, 'R' for autosomal recessive, 'B'
         * for both D and R, 'X' for X chromosomal, and 'U' for unknown.
         */
        private final InheritanceMode inheritanceMode;

        protected OmimDisease(int diseaseId, int omimGeneId, String diseaseName, int entrezGeneId, DiseaseType diseaseType, InheritanceMode inheritanceMode) {
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
            return String.format("OMIM:%d|OMIM:%d|%s|%d|%s|%s%n", diseaseId, omimGeneId, diseaseName, entrezGeneId, diseaseType
                    .getCode(), inheritanceMode
                    .getInheritanceCode());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OmimDisease that = (OmimDisease) o;
            return diseaseId == that.diseaseId &&
                    omimGeneId == that.omimGeneId &&
                    entrezGeneId == that.entrezGeneId &&
                    Objects.equals(diseaseName, that.diseaseName) &&
                    Objects.equals(diseaseType, that.diseaseType) &&
                    inheritanceMode == that.inheritanceMode;
        }

        @Override
        public int hashCode() {
            return Objects.hash(diseaseId, omimGeneId, diseaseName, entrezGeneId, diseaseType, inheritanceMode);
        }

        @Override
        public String toString() {
            return "OmimDisease{" +
                    "diseaseId=" + diseaseId +
                    ", omimGeneId=" + omimGeneId +
                    ", diseaseName='" + diseaseName + '\'' +
                    ", entrezGeneId=" + entrezGeneId +
                    ", diseaseType=" + diseaseType +
                    ", inheritanceMode=" + inheritanceMode.name() +
                    '}';
        }
    }
}
