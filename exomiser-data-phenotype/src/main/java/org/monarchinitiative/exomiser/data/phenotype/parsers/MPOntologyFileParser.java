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

package org.monarchinitiative.exomiser.data.phenotype.parsers;

import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parse the MP ontology obo file.
 *
 */
public class MPOntologyFileParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(MPOntologyFileParser.class);

    private final Map<String, String> mpId2termMap;

    public MPOntologyFileParser(Map<String, String> mpId2termMap) {
        this.mpId2termMap = mpId2termMap;
    }

    /**
     * This function does the actual work of parsing the HPO file.
     *
     * @param resource
     * @param inDir    Complete path to directory containing the human-phenotype-ontology.obo or hp.obo file.
     * @param outDir   Directory where output file is to be written
     * @return
     */
    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {

        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());

        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);

        OboOntology oboOntology = OboOntologyParser.parseOboFile(inFile);
        resource.setVersion(oboOntology.getDataVersion());
        logger.info("MP version: {}", oboOntology.getDataVersion());

        int currPlusObs = oboOntology.getCurrentOntologyTerms().size() + oboOntology.getObsoleteOntologyTerms().size();
        List<OboOntologyTerm> allTerms = new ArrayList<>(currPlusObs);
        allTerms.addAll(oboOntology.getCurrentOntologyTerms());
        allTerms.addAll(oboOntology.getObsoleteOntologyTerms());

        allTerms.sort(Comparator.comparing(OboOntologyTerm::getId));
        // CAUTION! In the HPO parser only the current terms are added to the map and written out - in the case of the MP
        // both obsolete and current terms were included. It's not clear why this is the case, but this behaviour has been
        // retained following refactoring this class. If this functionality is undesired the allTerms should be replaced
        // with oboOntology.getCurrentOntologyTerms()
        for (OboOntologyTerm ontologyTerm : allTerms) {
            mpId2termMap.put(ontologyTerm.getId(), ontologyTerm.getLabel());
        }
        ResourceOperationStatus status = writeMpFile(outDir.resolve("mp.pg"), allTerms);

        resource.setParseStatus(status);
        logger.info("{}", status);
    }

    private ResourceOperationStatus writeMpFile(Path outFile, List<OboOntologyTerm> ontologyTerms) {
        try (BufferedWriter writer = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8)){
            for (OboOntologyTerm ontologyTerm : ontologyTerms) {
                StringJoiner stringJoiner = new StringJoiner("|");
                if (ontologyTerm.getId().contains("MP:")) {
                    stringJoiner.add(ontologyTerm.getId());
                    stringJoiner.add(ontologyTerm.getLabel());
                    writer.write(stringJoiner.toString());
                    writer.newLine();
                }
            }
            return ResourceOperationStatus.SUCCESS;
        } catch (FileNotFoundException ex) {
            logger.error("", ex);
            return ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            return ResourceOperationStatus.FAILURE;
        }
    }
}