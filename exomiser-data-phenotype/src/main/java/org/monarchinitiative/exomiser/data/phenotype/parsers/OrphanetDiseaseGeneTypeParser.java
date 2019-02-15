/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OrphanetDiseaseGeneTypeParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(OrphanetDiseaseGeneTypeParser.class);

    private final Map<String, String> diseaseGeneTypeMap;

    public OrphanetDiseaseGeneTypeParser(Map<String, String> diseaseGeneTypeMap) {
        this.diseaseGeneTypeMap = diseaseGeneTypeMap;
    }

    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {
        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());
        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);
        ResourceOperationStatus status;
        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.forName("ISO-8859-1"));
             BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            String line;
            Integer orphaNum = null;
            String name = null;
            String type = null;
            List<String> synonyms = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("</DisorderGeneAssociation>")) {
                    if (orphaNum != null && name != null) {
                        String key = "ORPHA:" + orphaNum + name;
                        diseaseGeneTypeMap.put(key, type);
                        // add synomyms
                        for (String syn: synonyms){
                            key = "ORPHA:" + orphaNum + syn;
                            diseaseGeneTypeMap.put(key, type);
                        }
                    }
                    //orphaNum = null;
                    name = null; // reset
                    synonyms = new ArrayList<>();
                    type = null; // reset
                } else if (line.startsWith("<Disorder id")) {
                    // move to next line with has the disease ID
                    line = reader.readLine();
                    line = line.trim();
                    // and not gene
                    int i = line.indexOf("<",13);// skip to after start tag
                    if (i<0) continue;
                    String num = line.substring(13,i);
                    try {
                        orphaNum = Integer.parseInt(num);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not parse Orphanumber from line "
                                + line + "; " + e.getMessage());
                        continue;
                    }
                } else if (line.startsWith("<Symbol")) {
                    int i = line.indexOf(">");
                    if (i<0) continue;
                    i++; // skip the >
                    int j = line.indexOf("<",i);
                    if (j<i) continue;
                    name = line.substring(i,j);
                } else if (line.startsWith("<Synonym")) {
                    int i = line.indexOf(">");
                    if (i<0) continue;
                    i++; // skip the >
                    int j = line.indexOf("<",i);
                    if (j<i) continue;
                    String syn = line.substring(i,j);
                    synonyms.add(syn);
                } else if (line.startsWith("<DisorderGeneAssociationType")) {
                    //logger.info("Trying to extract type from line " + line);
                    line = reader.readLine();
                    line = line.trim();
                    //logger.info("Trying to extract type from line " + line);
                    int i = line.indexOf(">");
                    if (i<0) continue;
                    i++; // skip the >
                    int j = line.indexOf("<",i);
                    if (j<i) continue;
                    type = line.substring(i,j);
                }
            }
            // get very last one!
            //if (orphaNum != null && name != null){
                //String key = "ORPHA:" + orphaNum + name;
                //diseaseGeneTypeMap.put(key, type);
            //}

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
}
