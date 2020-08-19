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

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ResourceReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class ZfinGeneOrthologReader implements ResourceReader<List<GeneOrtholog>> {

    private static final Logger logger = LoggerFactory.getLogger(ZfinGeneOrthologReader.class);

    private final Resource zfinHumanOrthosResource;

    public ZfinGeneOrthologReader(Resource zfinHumanOrthosResource) {
        this.zfinHumanOrthosResource = zfinHumanOrthosResource;
    }

    @Override
    public List<GeneOrtholog> read() {
        Set<GeneOrtholog> fishOrthologs = new LinkedHashSet<>();

        try (BufferedReader reader = zfinHumanOrthosResource.newBufferedReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t");
                String fishId = "ZFIN:" + fields[0];
                String fishSymbol = fields[1];
                String humanSymbol = fields[3];
                int humanId = Integer.parseInt(fields[6]);
                fishOrthologs.add(new GeneOrtholog(fishId, fishSymbol, humanSymbol, humanId));
            }
        } catch (IOException ex) {
            logger.error("Unable to read {}", zfinHumanOrthosResource, ex);
        }

        return new ArrayList<>(fishOrthologs);
    }
}
