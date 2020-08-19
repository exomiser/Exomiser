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

package org.monarchinitiative.exomiser.data.phenotype.processors.groups;

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.gene.FishGeneModelStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.gene.MouseGeneModelStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class GeneProcessingGroup implements ProcessingGroup {

    private static final Logger logger = LoggerFactory.getLogger(GeneProcessingGroup.class);

    private final List<Resource> geneResources;
    private final MouseGeneModelStep mouseGeneModelStep;
    private final FishGeneModelStep fishGeneModelStep;

    public GeneProcessingGroup(List<Resource> geneResources, MouseGeneModelStep mouseGeneModelStep, FishGeneModelStep fishGeneModelStep) {
        this.geneResources = geneResources;
        geneResources.forEach(resource -> logger.debug("Using {}", resource));

        this.mouseGeneModelStep = mouseGeneModelStep;
        this.fishGeneModelStep = fishGeneModelStep;
    }

    @Override
    public String getName() {
        return "GeneProcessingGroup";
    }

    @Override
    public List<Resource> getResources() {
        return geneResources;
    }

    @Override
    public void processResources() {
        mouseGeneModelStep.run();
        fishGeneModelStep.run();
    }
}
