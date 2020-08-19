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

import com.google.common.collect.Multimap;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class FishGeneModelFactory {

    private static final Logger logger = LoggerFactory.getLogger(FishGeneModelFactory.class);

    private final Map<String, String> fishGeneLabels;
    private final Multimap<String, String> fishGenePhenotypes;

    public FishGeneModelFactory(Map<String, String> fishGeneLabels, Multimap<String, String> fishGenePhenotypes) {
        this.fishGeneLabels = fishGeneLabels;
        this.fishGenePhenotypes = fishGenePhenotypes;
    }

    public List<GeneModel> buildGeneModels() {
        List<GeneModel> geneModels = new ArrayList<>();
        int id = 1;
        for (Map.Entry<String, Collection<String>> entry : fishGenePhenotypes.asMap().entrySet()) {
            String fishId = entry.getKey();
            Collection<String> zpIds = entry.getValue();
            String fishSymbol = fishGeneLabels.get(fishId);
            GeneModel geneModel = new GeneModel(String.valueOf(id++), fishId, fishSymbol, List.copyOf(zpIds));
            logger.debug("{}", geneModel.toOutputLine());
            geneModels.add(geneModel);
        }
        return geneModels;
    }
}
