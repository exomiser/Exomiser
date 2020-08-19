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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.gene;

import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneModel;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.GenePhenotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
class MouseGeneModelFactory {

    private static final Logger logger = LoggerFactory.getLogger(MouseGeneModelFactory.class);

    private final List<GeneOrtholog> humanMouseGeneOrthologs;
    private final List<GenePhenotype> mgiModels;
    private final List<GenePhenotype> impcModels;

    MouseGeneModelFactory(List<GeneOrtholog> humanMouseGeneOrthologs, List<GenePhenotype> mgiModels, List<GenePhenotype> impcModels) {
        this.humanMouseGeneOrthologs = humanMouseGeneOrthologs;
        this.mgiModels = mgiModels;
        this.impcModels = impcModels;
    }

    List<GeneModel> buildGeneModels() {
        Map<String, String> mgiGeneIdToSymbol = new HashMap<>();
        for (GeneOrtholog humanMouseGeneOrtholog : humanMouseGeneOrthologs) {
            // there will be duplicates due to N:M orthology
            mgiGeneIdToSymbol.put(humanMouseGeneOrtholog.getOrthologGeneId(), humanMouseGeneOrtholog.getOrthologGeneSymbol());
        }

        List<GenePhenotype> allModels = new ArrayList<>(mgiModels.size() + impcModels.size());
        allModels.addAll(mgiModels);
        allModels.addAll(impcModels);
        allModels.sort(Comparator.comparing(GenePhenotype::getGeneId).thenComparing(GenePhenotype::getId));

        List<GeneModel> geneModels = new ArrayList<>(allModels.size());
        for (GenePhenotype genePhenotype : allModels) {
            String mouseGeneSymbol = mgiGeneIdToSymbol.get(genePhenotype.getGeneId());
            if (mouseGeneSymbol != null) {
                GeneModel geneModel = new GeneModel(genePhenotype.getId(), genePhenotype.getGeneId(), mouseGeneSymbol, new ArrayList<>(genePhenotype
                        .getPhenotypeIds()));
                logger.debug("{}", geneModel.toOutputLine());
                geneModels.add(geneModel);
            }
        }
        return geneModels;
    }
}
