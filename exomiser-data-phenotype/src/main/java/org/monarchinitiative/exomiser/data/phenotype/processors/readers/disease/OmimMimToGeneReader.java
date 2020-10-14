/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease;

import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class OmimMimToGeneReader implements ResourceReader<Map<String, Integer>> {

    private static final Logger logger = LoggerFactory.getLogger(OmimMimToGeneReader.class);

    private final Resource mimToGeneResource;

    /**
     * Key: A MIM id for a Gene; Value: the corresponding entrez Gene id. This
     * information comes from mim2gene.txt
     *
     */
    public OmimMimToGeneReader(Resource mimToGeneResource) {
        this.mimToGeneResource = mimToGeneResource;
    }

    @Override
    public Map<String, Integer> read() {
        logger.info("Reading resource: {}", mimToGeneResource.getResourcePath());
        Map<String, Integer> mim2geneMap = new HashMap<>();

        try (BufferedReader reader = mimToGeneResource.newBufferedReader()) {
            for (String line; (line = reader.readLine()) != null; ) {
                //ignore comment lines
                if (!line.startsWith("#")) {
                    parseLine(mim2geneMap, line);
                }
            }
            logger.info("Extracted {} genes from {}", mim2geneMap.size(), mimToGeneResource.getResourcePath());

        } catch (FileNotFoundException ex) {
            logger.error("Unable to find file: {}", mimToGeneResource.getResourcePath(), ex);
        } catch (IOException ex) {
            logger.error("Error parsing file: {}", mimToGeneResource.getResourcePath(), ex);
        }

        return ImmutableMap.copyOf(mim2geneMap);
    }

    private void parseLine(Map<String, Integer> mim2geneMap, String line) {
        String[] fields = line.split("\t");
        try {
            String type = fields[1].trim();
            /* The following gets both "gene" and "gene/phenotype" */
            if (type.startsWith("gene") && fields.length >=3) {
                // typical line: 100850  gene    50      ACO2    ENSG00000100412
                String mim = "OMIM:" + fields[0];
                Integer entrezGeneId = Integer.parseInt(fields[2]); // Entrez Gene ID */
                if (mim2geneMap.containsKey(mim)) {
                    logger.warn("{} already mapped to EntrezId {}", mim, mim2geneMap.get(mim));
                } else {
                    logger.debug("Adding {} Entrez:{}", mim, entrezGeneId);
                    mim2geneMap.put(mim, entrezGeneId);
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing line {}", line, e);
        }
    }
}
