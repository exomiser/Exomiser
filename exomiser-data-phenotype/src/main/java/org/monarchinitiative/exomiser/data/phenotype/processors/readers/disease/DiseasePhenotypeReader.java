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

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseasePhenotype;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseasePhenotypeReader implements ResourceReader<List<DiseasePhenotype>> {

    private static final Logger logger = LoggerFactory.getLogger(DiseasePhenotypeReader.class);

    private final Resource hpoPhenotypeAnnotationsResource;

    public DiseasePhenotypeReader(Resource hpoPhenotypeAnnotationsResource) {
        this.hpoPhenotypeAnnotationsResource = hpoPhenotypeAnnotationsResource;
    }

    @Override
    public List<DiseasePhenotype> read() {
        Map<String, Set<String>> disease2PhenotypeMap = new LinkedHashMap<>();
        try (BufferedReader reader = hpoPhenotypeAnnotationsResource.newBufferedReader()) {
            String line;
            // skip header
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t");
                String diseaseId = fields[0] + ":" + fields[1];
                String hpId = fields[4];
                if (disease2PhenotypeMap.containsKey(diseaseId)) {
                    disease2PhenotypeMap.get(diseaseId).add(hpId);
                } else {
                    Set<String> hpIds = new LinkedHashSet<>();
                    hpIds.add(hpId);
                    disease2PhenotypeMap.put(diseaseId, hpIds);
                }
            }
        } catch (IOException ex) {
            logger.error("Error reading file: {}", hpoPhenotypeAnnotationsResource.getResourcePath(), ex);
        }

        return disease2PhenotypeMap.entrySet()
                .stream()
                .map(entry -> new DiseasePhenotype(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
