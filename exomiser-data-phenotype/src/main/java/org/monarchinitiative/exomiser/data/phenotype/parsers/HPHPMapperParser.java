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
import java.util.Map;

/**

 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HPHPMapperParser implements ResourceParser {

    private static final Logger logger = LoggerFactory.getLogger(HPHPMapperParser.class);

    private final Map<String, String> hpId2termMap;

    public HPHPMapperParser(Map<String, String> hpId2termMap) {
        this.hpId2termMap = hpId2termMap;
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
            int id = 0;
            while ((line = reader.readLine()) != null) {
                id++;
                String[] fields = line.split("\\t");
                String queryId = fields[0];
                queryId = queryId.replace("_",":");
                String queryTerm = "";
                if (null != hpId2termMap.get(queryId))
                    queryTerm = hpId2termMap.get(queryId);
                String hitId = fields[1];
                hitId = hitId.replace("_",":");
                String hitTerm = "";
                if (null != hpId2termMap.get(hitId))
                    hitTerm = hpId2termMap.get(hitId);
                String simJ = fields[2];
                String ic = fields[3];
                //float score = Math.sqrt(Float.parseFloat(simJ) * Float.parseFloat(ic));
                double score = Math.sqrt(Double.parseDouble(simJ) * Double.parseDouble(ic));
                String lcs = fields[4].split(";")[0];
                lcs = lcs.replace("_",":");
                //lcs = lcs.replace(";","");
                String lcsTerm = "";
                if (null != hpId2termMap.get(lcs)) {
                    lcsTerm = hpId2termMap.get(lcs);
                }
                writer.write(String.format("%d|%s|%s|%s|%s|%s|%s|%s|%s|%s%n", id,queryId,queryTerm,hitId ,hitTerm,simJ,ic,score,lcs,lcsTerm));
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
