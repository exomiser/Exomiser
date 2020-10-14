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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class MonarchFishGenePhenotypeReader implements ResourceReader<SetMultimap<String, String>> {

    private static final Logger logger = LoggerFactory.getLogger(MonarchFishGenePhenotypeReader.class);

    private final Resource drGenePhenotypeResource;

    public MonarchFishGenePhenotypeReader(Resource drGenePhenotypeResource) {
        this.drGenePhenotypeResource = drGenePhenotypeResource;
    }

    @Override
    public SetMultimap<String, String> read() {
        SetMultimap<String, String> genePhenotypes = LinkedHashMultimap.create();
        try (BufferedReader reader = drGenePhenotypeResource.newBufferedReader()) {
            for (String line; (line = reader.readLine()) != null; ) {
                String[] fields = line.split("\t");
                // ZFIN:ZDB-MIRNAG-090929-37	GO:0030218PHENOTYPE
                // ZFIN:ZDB-MIRNAG-090929-37	ZP:0000516
                // ZFIN:ZDB-MIRNAG-090929-37	ZP:0002086
                // ZFIN:ZDB-MIRNAG-090929-37	ZP:0009405
                // ZFIN:ZDB-MIRNAG-090929-37	ZP:0009406
                String phenotypeId = fields[1];
                if (phenotypeId != null && phenotypeId.startsWith("ZP:")) {
                    genePhenotypes.put(fields[0], phenotypeId);
                }
            }
        } catch (IOException e) {
            logger.error("Unable to read {}", drGenePhenotypeResource, e);
        }
        return genePhenotypes;
    }
}
