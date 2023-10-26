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

package org.monarchinitiative.exomiser.data.phenotype.config;

import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.groups.DiseaseProcessingGroup;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGeneMoiComparison;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseasePhenotype;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.EntrezIdGeneSymbol;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease.DiseaseGeneMoiComparisonStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease.DiseaseGeneStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease.DiseasePhenotypeStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease.EntrezIdGeneSymbolStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class DiseaseResourceConfig {

    private final ResourceConfigurationProperties resourceProperties;
    private final Path processPath;
    private final ResourceBuilder resourceBuilder;

    public DiseaseResourceConfig(ResourceConfigurationProperties resourceProperties, ReleaseFileSystem releaseFileSystem, ResourceBuilder resourceBuilder) {
        this.resourceProperties = resourceProperties;
        this.processPath = releaseFileSystem.processedDir();
        this.resourceBuilder = resourceBuilder;
    }

    @Bean
    public DiseaseProcessingGroup diseaseProcessingGroup() {
        Resource hpoAnnotationsResource = resourceBuilder.buildResource(resourceProperties.getHpoAnnotations());

        OutputLineWriter<DiseasePhenotype> diseasePhenotypeWriter = new OutputLineWriter<>(processPath.resolve("diseaseHp.pg"));
        DiseasePhenotypeStep diseasePhenotypeStep = DiseasePhenotypeStep.create(hpoAnnotationsResource, diseasePhenotypeWriter);

        //
        Resource geneMap2Resource = resourceBuilder.buildResource(resourceProperties.getGenemap2());
        Resource mimToGeneResource = resourceBuilder.buildResource(resourceProperties.getMim2gene());
        Resource product1Resource = resourceBuilder.buildResource(resourceProperties.getOrphaProduct1());
        Resource product6Resource = resourceBuilder.buildResource(resourceProperties.getOrphaProduct6());
        Resource product9Resource = resourceBuilder.buildResource(resourceProperties.getOrphaProduct9Ages());

        OutputLineWriter<DiseaseGene> diseaseGeneWriter = new OutputLineWriter<>(processPath.resolve("disease.pg"));
        DiseaseGeneStep diseaseGeneStep = DiseaseGeneStep.create(hpoAnnotationsResource, geneMap2Resource, mimToGeneResource, product1Resource, product6Resource, product9Resource, diseaseGeneWriter);

        // Output files for HPO annotations QC - open a ticket with these using the md file as the ticket body
        OutputLineWriter<DiseaseGeneMoiComparison> missingInHpoMoiWriter = new OutputLineWriter<>(processPath.resolve("missing_moi_hpo.md"));
        OutputLineWriter<DiseaseGeneMoiComparison> missingInOmimMoiWriter = new OutputLineWriter<>(processPath.resolve("missing_moi_omim.md"));
        OutputLineWriter<DiseaseGeneMoiComparison> mismatchedMoiWriter = new OutputLineWriter<>(processPath.resolve("mismatched_moi.md"));

        DiseaseGeneMoiComparisonStep diseaseGeneMoiComparisonStep = DiseaseGeneMoiComparisonStep.create(
                hpoAnnotationsResource,
                geneMap2Resource,
                missingInHpoMoiWriter,
                missingInOmimMoiWriter,
                mismatchedMoiWriter
        );

        // HGNC data
        Resource hgncResource = resourceBuilder.buildResource(resourceProperties.getHgncCompleteSet());
        OutputLineWriter<EntrezIdGeneSymbol> entrezGeneSymbolWriter = new OutputLineWriter<>(processPath.resolve("entrez2sym.pg"));
        EntrezIdGeneSymbolStep entrezIdGeneSymbolStep = EntrezIdGeneSymbolStep.create(hgncResource, entrezGeneSymbolWriter);

        List<Resource> diseaseResources = List.of(hpoAnnotationsResource, geneMap2Resource, mimToGeneResource, product1Resource, product6Resource, product9Resource, hgncResource);

        return new DiseaseProcessingGroup(diseaseResources, diseasePhenotypeStep, diseaseGeneStep, diseaseGeneMoiComparisonStep, entrezIdGeneSymbolStep);
    }
}
