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

import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
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
import java.util.Map;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class Orphanet2GeneParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(Orphanet2GeneParser.class);

    private final Map<String, String> disease2TermMap;
    private final Map<String, String> diseaseGeneTypeMap;

    public Orphanet2GeneParser(Map<String, String> disease2TermMap, Map<String, String> diseaseGeneTypeMap) {
        this.disease2TermMap = disease2TermMap;
        this.diseaseGeneTypeMap = diseaseGeneTypeMap;
    }

    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {
        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());
        logger.info("!!!!! Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);
        ResourceOperationStatus status;
        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.defaultCharset());
             BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            String line;
            String lastkey = "";
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t");
                final int expectedFields = 9;
                if (fields.length != expectedFields) {
                    //logger.error("Expected {} fields but got {} for line {}", expectedFields, fields.length, line);
                    continue;
                }
                String diseaseId = fields[8];
                if (!diseaseId.startsWith("ORPHA"))
                    continue;
                String entrezGeneId = fields[0];
                String geneSymbol = fields[1];
                String key = diseaseId + geneSymbol;
                if (key.equals(lastkey)){
                    continue;// new file contains duplicated per HPO annotation
                }
                lastkey = key;
                String diseaseTypeValue = diseaseGeneTypeMap.getOrDefault(key, "");
                String type = mapToDiseaseType(diseaseTypeValue);
                // ? if MOI in this file or easy to add
                String diseaseName = disease2TermMap.get(diseaseId);
                writer.write(String.format("%s|%s|%s|%s|%s|%s%n", diseaseId, "", diseaseName, entrezGeneId, type, ""));
            }
            status = ResourceOperationStatus.SUCCESS;

        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FILE_NOT_FOUND;
        } catch (IOException ex) {
            logger.error(null, ex);
            status = ResourceOperationStatus.FAILURE;
        }
        logger.info("{}", status);
        resource.setParseStatus(status);
    }

    private String mapToDiseaseType(String description) {
        // n.b we're using the Disease.DiseaseType here as these are what the data is transformed into and the
        // DiseaseType.code() is used to parse the values from the database.
        if (!description.isEmpty()){
            // note Orphanet defines candidate genes as those that are routinely tested for in clinical labs
            // but not fully proven - a review suggested these are worth knowing about still
            if (description.startsWith("Disease-causing germline mutation")
                    || description.startsWith("Candidate gene tested in")){
                return Disease.DiseaseType.DISEASE.getCode();
            }
            else if (description.startsWith("Major susceptibility factor")){
                return Disease.DiseaseType.SUSCEPTIBILITY.getCode();
            }
            else if (description.startsWith("Role in the phenotype")){
                return Disease.DiseaseType.CNV.getCode();
            }
            /* other types in Orphanet that we are just leaving as ? for now
            Disease-causing somatic mutation
            Biomarker tested in
            Modifying germline mutation in
            Part of a fusion gene in*/
        }
        return Disease.DiseaseType.UNCONFIRMED.getCode();
    }
}
