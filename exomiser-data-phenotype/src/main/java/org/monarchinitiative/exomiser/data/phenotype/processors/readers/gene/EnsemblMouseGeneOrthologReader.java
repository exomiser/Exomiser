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
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class EnsemblMouseGeneOrthologReader implements ResourceReader<List<GeneOrtholog>> {
    private static final Logger logger = LoggerFactory.getLogger(EnsemblMouseGeneOrthologReader.class);

    private final Resource homMouseHumanSequenceResource;

    public EnsemblMouseGeneOrthologReader(Resource homMouseHumanSequenceResource) {
        this.homMouseHumanSequenceResource = homMouseHumanSequenceResource;
    }

    @Override
    public List<GeneOrtholog> read() {
        List<GeneOrtholog> mouseHumanOrthologs = new ArrayList<>();

        try (BufferedReader reader = homMouseHumanSequenceResource.newBufferedReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\t");
                String humanId = fields[0];
                String humanSymbol = fields[1];
                String mouseId = fields[2];
                String mouseSymbol = fields[3];
                if (humanId.isEmpty() || humanSymbol.isEmpty() || mouseId.isEmpty() || mouseSymbol.isEmpty()) {
                    continue;
                }
                GeneOrtholog geneOrtholog = new GeneOrtholog(mouseId, mouseSymbol, humanSymbol, Integer.parseInt(humanId));
                logger.debug("{}", geneOrtholog);
                mouseHumanOrthologs.add(geneOrtholog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mouseHumanOrthologs;
    }
}
