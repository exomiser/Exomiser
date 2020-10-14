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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene;

import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class ImpcMouseGenePhenotypeReader implements ResourceReader<List<GenePhenotype>> {

    private static final Logger logger = LoggerFactory.getLogger(ImpcMouseGenePhenotypeReader.class);

    private static final Pattern CSV = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*+[^\"]*$)");

    private final Resource allGenotypePhenotypeResource;

    public ImpcMouseGenePhenotypeReader(Resource allGenotypePhenotypeResource) {
        this.allGenotypePhenotypeResource = allGenotypePhenotypeResource;
    }

    @Override
    public List<GenePhenotype> read() {
        logger.info("Reading {}", allGenotypePhenotypeResource.getResourcePath());
        List<GenePhenotype> genePhenotypes = new ArrayList<>();

        Map<String, String> mouse2geneMap = new LinkedHashMap<>();
        SetMultimap<String, String> mouse2PhenotypeMap = TreeMultimap.create();

        try (BufferedReader reader = allGenotypePhenotypeResource.newBufferedReader()){
            // skip header line
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = CSV.split(line, -1);

                String markerAccession = fields[0];
                String colonyId = fields[3];
                String zygosity = fields[5];

                String modelId = "IMPC:" + colonyId + "_" + zygosity.substring(0, 3);

                mouse2geneMap.put(modelId, markerAccession);

                int mpIndex = fields.length - 7;
                String mpId = fields[mpIndex];
                if (!mpId.isEmpty()) {
                    mouse2PhenotypeMap.put(modelId, mpId);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read file {}", allGenotypePhenotypeResource.getResourcePath(), e);
        }

        for (Map.Entry<String, Collection<String>> entry : mouse2PhenotypeMap.asMap().entrySet()) {
            String modelId = entry.getKey();
            Collection<String> mpIds = entry.getValue();
            String markerId = mouse2geneMap.get(modelId);
            genePhenotypes.add(new GenePhenotype(modelId, markerId, mpIds));
        }
        genePhenotypes.sort(Comparator.comparing(GenePhenotype::getGeneId).thenComparing(GenePhenotype::getId));

        logger.info("Read {} IMPC mouse gene-phenotype models from {}", genePhenotypes.size(), allGenotypePhenotypeResource.getResourcePath());

        return genePhenotypes;
    }
}
