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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**

 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MouseHomoloGeneOrthologParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(MouseHomoloGeneOrthologParser.class);

    private final Map<String, String> mouseId2Symbol;
    private final Map<String, String> humanId2Symbol;
    private final Map<String, Set<String>> mouse2human;

    public MouseHomoloGeneOrthologParser(Map<String, String> mouseId2Symbol,Map<String, String> humanId2Symbol,Map<String, Set<String>> mouse2human){
        this.mouseId2Symbol = mouseId2Symbol;
        this.humanId2Symbol = humanId2Symbol;
        this.mouse2human = mouse2human;
    }

    @Override
    public void parseResource(Resource resource, Path inDir, Path outDir) {
        Path inFile = inDir.resolve(resource.getExtractedFileName());
        Path outFile = outDir.resolve(resource.getParsedFileName());
        logger.info("Parsing {} file: {}. Writing out to: {}", resource.getName(), inFile, outFile);
        ResourceOperationStatus status;
        try (BufferedReader reader = Files.newBufferedReader(inFile, Charset.defaultCharset());
            BufferedWriter writer = Files.newBufferedWriter(outFile, Charset.defaultCharset())) {
            String line;
            Map<String , Set<String>> mouseIds = new HashMap<>();
            Map<String , Set<String>> humanIds = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t");
                String homoloGeneId = fields[0];
                if (fields[1].equals("human")){
                    String humanSymbol = fields[3];
                    String humanId = fields[4];
                    if (null != humanIds.get(homoloGeneId)) {
                        humanIds.get(homoloGeneId).add(humanId);
                    }
                    else{
                        Set<String> newSet = new HashSet<>();
                        newSet.add(humanId);
                        humanIds.put(homoloGeneId,newSet);
                    }
                    humanId2Symbol.put(humanId,humanSymbol);
                }
                else if (fields[1].equals("mouse, laboratory")){
                    String mouseSymbol = fields[3];
                    String mouseId = fields[5];
                    //mouseIds.put(homoloGeneId,mouseId);
                    if (null != mouseIds.get(homoloGeneId)) {
                        mouseIds.get(homoloGeneId).add(mouseId);
                    }
                    else{
                        Set<String> newSet = new HashSet<>();
                        newSet.add(mouseId);
                        mouseIds.put(homoloGeneId,newSet);
                    }

                    mouseId2Symbol.put(mouseId,mouseSymbol);
                }
            }
            for (String homoloGeneId: mouseIds.keySet()){
                if (null != mouseIds.get(homoloGeneId) && null != humanIds.get(homoloGeneId)) {
                    for (String mouseId : mouseIds.get(homoloGeneId)) {
                        for (String humanId : humanIds.get(homoloGeneId)){
                            if (null != mouse2human.get(mouseId)) {
                                mouse2human.get(mouseId).add(humanId);
                            } else {
                                Set<String> humanSet = new HashSet<>();
                                humanSet.add(humanId);
                                mouse2human.put(mouseId, humanSet);
                            }
                        }
                    }
                }
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
}
