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

import com.google.common.base.Joiner;
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
public class MouseEnsemblOrthologParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(MouseEnsemblOrthologParser.class);

    private final Map<String, String> mouseId2Symbol;
    private final Map<String, String> humanId2Symbol;
    private final Map<String, Set<String>> mouse2human;

    public MouseEnsemblOrthologParser(Map<String, String> mouseId2Symbol,Map<String, String> humanId2Symbol,Map<String, Set<String>> mouse2human){
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
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t");
                String humanId = fields[0];
                String humanSymbol = fields[1];
                String mouseId = fields[2];
                String mouseSymbol = fields[3];
                if (humanId.equals("") || humanSymbol.equals("") || mouseId.equals("") || mouseSymbol.equals(""))
                    continue;
                //logger.info("Ensembl - Mouse ID:"+mouseId+"Mouse Symbol"+mouseSymbol);
                mouseId2Symbol.put(mouseId, mouseSymbol);
                humanId2Symbol.put(humanId, humanSymbol);
                //mouse2human.put(mouseId, humanId);

                if (null != mouse2human.get(mouseId)){
                    mouse2human.get(mouseId).add(humanId);
                }
                else {
                    Set<String> humanSet = new HashSet<>();
                    humanSet.add(humanId);
                    mouse2human.put(mouseId, humanSet);
                }

            }
            for (String mouseId: mouse2human.keySet()) {
                String mouseSymbol = mouseId2Symbol.get(mouseId);
                Set<String> humanIds = mouse2human.get(mouseId);
                for (String humanId : humanIds) {
                    String humanSymbol = humanId2Symbol.get(humanId);
                    writer.write(String.format("%s|%s|%s|%s%n", mouseId, mouseSymbol, humanSymbol, humanId));
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
