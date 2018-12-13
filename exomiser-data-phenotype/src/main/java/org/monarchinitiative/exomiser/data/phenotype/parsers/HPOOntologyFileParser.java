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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.StringJoiner;

import static org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus.FAILURE;
import static org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus.SUCCESS;

/**
 * Parse the good old human-phenotype-ontology.obo file (or alternatively the
 * hp.obo file from our Hudson server). We want to create a table in the
 * database with lcname - HP:id - preferred name, where lcname is the lower-case
 * name or synonym, ID is the HPO id, and preferred name is the HPO Term name.
 * We lower-case the name ans synonyms to be able to search only over lower
 * cased names for the autosuggestion. However, we want to display the preferred
 * name in the end.
 *
 * @author Peter Robinson
 * @version 0.04 (27 November, 2013)
 */
public class HPOOntologyFileParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(HPOOntologyFileParser.class);

    /**
     * A variable that keeps count of the number of rows added to the database.
     */
    int n_row = 0;

    private final Map<String, String> hpId2termMap;

    public HPOOntologyFileParser(Map<String, String> hpId2termMap) {
        this.hpId2termMap = hpId2termMap;
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

        logger.info("Parsing {} file: {}.", resource.getName(), inFile);
        OboOntology oboOntology = OboOntologyParser.parseOboFile(inFile);
        resource.setVersion(oboOntology.getDataVersion());
        logger.info("HPO version: {}", oboOntology.getDataVersion());

        for (OboOntologyTerm ontologyTerm : oboOntology.getCurrentOntologyTerms()) {
            hpId2termMap.put(ontologyTerm.getId(), ontologyTerm.getLabel());
        }

        ResourceOperationStatus hpStatus = writeHpFile(outFile, oboOntology);
        logger.info("{} Writing hp to: {}", hpStatus, outFile);

        // hack in a new file not defined in the usual resources place
        Path hpAltIdFile = outDir.resolve("hp_alt_ids.pg");
        ResourceOperationStatus hpAltIdStatus = writeHpAltIdFile(hpAltIdFile, oboOntology);
        logger.info("{} Writing hp_alt_ids to: {}", hpAltIdStatus, hpAltIdFile);

        ResourceOperationStatus status = hpStatus == FAILURE ? FAILURE : hpAltIdStatus;
        resource.setParseStatus(status);
        logger.info("{}", status);
    }

    private ResourceOperationStatus writeHpFile(Path outFile, OboOntology oboOntology) {
        try (BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.forName("UTF-8"))){
            for (OboOntologyTerm ontologyTerm : oboOntology.getCurrentOntologyTerms()) {
                StringJoiner stringJoiner = new StringJoiner("|");
                stringJoiner.add(ontologyTerm.getId());
                stringJoiner.add(ontologyTerm.getLabel());
                writer.write(stringJoiner.toString());
                writer.newLine();
            }
            return SUCCESS;
        } catch (Exception ex) {
            logger.error("Error writing to file {}", outFile, ex);
            return ResourceOperationStatus.FAILURE;
        }
    }

    private ResourceOperationStatus writeHpAltIdFile(Path hpAltIdFile, OboOntology oboOntology) {
        try (BufferedWriter writer = Files.newBufferedWriter(hpAltIdFile, Charset.forName("UTF-8"))){
            Map<String, OboOntologyTerm> phenotypeTermMap = oboOntology.getIdToTerms();
            for (Map.Entry<String, OboOntologyTerm> entry : phenotypeTermMap.entrySet()) {
                String altId = entry.getKey();
                OboOntologyTerm ontologyTerm = entry.getValue();
                StringJoiner stringJoiner = new StringJoiner("|");
                stringJoiner.add(altId);
                stringJoiner.add(ontologyTerm.getId());
                writer.write(stringJoiner.toString());
                writer.newLine();
            }
            return SUCCESS;
        } catch (Exception ex) {
            logger.error("Error writing to file {}", hpAltIdFile, ex);
            return ResourceOperationStatus.FAILURE;
        }
    }
}
