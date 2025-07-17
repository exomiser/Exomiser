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
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease.DiseaseGeneMoiComparisonStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease.DiseaseGeneStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease.DiseasePhenotypeStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease.EntrezIdGeneSymbolStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseaseProcessingGroup implements ProcessingGroup {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseProcessingGroup.class);

    private final List<Resource> resources;

    private final DiseasePhenotypeStep diseasePhenotypeStep;
    private final DiseaseGeneStep diseaseGeneStep;
    private final DiseaseGeneMoiComparisonStep diseaseGeneMoiComparisonStep;
    private final EntrezIdGeneSymbolStep entrezIdGeneSymbolStep;

    public DiseaseProcessingGroup(List<Resource> diseaseResources, DiseasePhenotypeStep diseasePhenotypeStep, DiseaseGeneStep diseaseGeneStep, DiseaseGeneMoiComparisonStep diseaseGeneMoiComparisonStep, EntrezIdGeneSymbolStep entrezIdGeneSymbolStep) {
        this.resources = List.copyOf(diseaseResources);
        diseaseResources.forEach(resource -> logger.debug("Using {}", resource));

        this.diseasePhenotypeStep = diseasePhenotypeStep;
        this.diseaseGeneStep = diseaseGeneStep;
        this.diseaseGeneMoiComparisonStep = diseaseGeneMoiComparisonStep;
        this.entrezIdGeneSymbolStep = entrezIdGeneSymbolStep;
    }

    @Override
    public String getName() {
        return "DiseaseProcessingGroup";
    }

    @Override
    public List<Resource> getResources() {
        return resources;
    }

    @Override
    public void processResources() {
        diseasePhenotypeStep.run();
        diseaseGeneStep.run();
        diseaseGeneMoiComparisonStep.run();
        entrezIdGeneSymbolStep.run();
    }
}
