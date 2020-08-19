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
import org.monarchinitiative.exomiser.data.phenotype.processors.groups.GeneProcessingGroup;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneModel;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.*;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.gene.FishGeneModelStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.gene.MouseGeneModelStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class GeneResourceConfig {

    private final ResourceConfigurationProperties resourceProperties;
    private final Path processPath;
    private final ResourceBuilder resourceBuilder;

    public GeneResourceConfig(ResourceConfigurationProperties resourceProperties, ReleaseFileSystem releaseFileSystem, ResourceBuilder resourceBuilder) {
        this.resourceProperties = resourceProperties;
        this.processPath = releaseFileSystem.processedDir();
        this.resourceBuilder = resourceBuilder;
    }

    // TODO: should these ProcessingSteps not be
    //  MouseGeneProcessingGroup
    //    and
    //  FishGeneProcessingGroup
    //   to go with
    //  DiseaseProcessingGroup -- should this be DiseaseGeneProcessingGroup?
    //   and
    //  OntologyProcessingGroup

    @Bean
    public GeneProcessingGroup geneProcessingGroup() {
        // Mouse-Human Orthologs
        Resource mgiMouseGeneOrthologResource = resourceBuilder.buildResource(resourceProperties.getMgiMouseHumanOrthologs());
        Resource ensemblMouseGeneOrthologResource = resourceBuilder.buildResource(resourceProperties.getEnsemblMouseHumanOrthologs());

        MgiMouseGeneOrthologReader mgiMouseGeneOrthologReader = new MgiMouseGeneOrthologReader(mgiMouseGeneOrthologResource);
        EnsemblMouseGeneOrthologReader ensemblMouseGeneOrthologReader = new EnsemblMouseGeneOrthologReader(ensemblMouseGeneOrthologResource);
        OutputLineWriter<GeneOrtholog> mouseGeneOrthologOutputLineWriter = new OutputLineWriter<>(processPath.resolve("human2mouseOrthologs.pg"));

        // Mouse Gene-Phenotype models
        Resource mgiGenePhenotypeResource = resourceBuilder.buildResource(resourceProperties.getMgiGenePheno());
        Resource impcGenePhenotypeResource = resourceBuilder.buildResource(resourceProperties.getImpcAllGenotypePhenotype());

        MgiMouseGenePhenotypeReader mgiMouseGenePhenotypeReader = new MgiMouseGenePhenotypeReader(mgiGenePhenotypeResource);
        ImpcMouseGenePhenotypeReader impcMouseGenePhenotypeReader = new ImpcMouseGenePhenotypeReader(impcGenePhenotypeResource);
        OutputLineWriter<GeneModel> mouseGeneModelOutputLineWriter = new OutputLineWriter<>(processPath.resolve("mouseMp.pg"));

        MouseGeneModelStep mouseGeneModelStep = new MouseGeneModelStep(mgiMouseGeneOrthologReader, ensemblMouseGeneOrthologReader, mouseGeneOrthologOutputLineWriter, mgiMouseGenePhenotypeReader, impcMouseGenePhenotypeReader, mouseGeneModelOutputLineWriter);


        // Fish-Human Orthologs
        Resource zfinGeneOrthologResource = resourceBuilder.buildResource(resourceProperties.getZfinFishHumanOrthologs());
        ZfinGeneOrthologReader zfinGeneOrthologReader = new ZfinGeneOrthologReader(zfinGeneOrthologResource);
        OutputLineWriter<GeneOrtholog> fishGeneOrthologOutputLineWriter = new OutputLineWriter<>(processPath.resolve("human2fishOrthologs.pg"));

        // Fish Gene-Phenotype models
        Resource monarchFishGeneLabelResource = resourceBuilder.buildResource(resourceProperties.getMonarchFishGeneLabels());
        Resource monarchFishGenePhenotypeResource = resourceBuilder.buildResource(resourceProperties.getMonarchFishPhenotypes());

        MonarchFishGeneLabelReader monarchFishGeneLabelReader = new MonarchFishGeneLabelReader(monarchFishGeneLabelResource);
        MonarchFishGenePhenotypeReader monarchFishGenePhenotypeReader = new MonarchFishGenePhenotypeReader(monarchFishGenePhenotypeResource);
        OutputLineWriter<GeneModel> fishGeneModelOutputLineWriter = new OutputLineWriter<>(processPath.resolve("fishZp.pg"));

        FishGeneModelStep fishGeneModelStep = new FishGeneModelStep(zfinGeneOrthologReader, fishGeneOrthologOutputLineWriter, monarchFishGeneLabelReader, monarchFishGenePhenotypeReader, fishGeneModelOutputLineWriter);


        List<Resource> geneResources = List.of(
                mgiMouseGeneOrthologResource,
                ensemblMouseGeneOrthologResource,
                mgiGenePhenotypeResource,
                impcGenePhenotypeResource,
                // given these two sets of resources are independent there could to be a
                // MouseGeneProcessingGroup
                // and
                // FishGeneProcessingGroup
                zfinGeneOrthologResource,
                monarchFishGeneLabelResource,
                monarchFishGenePhenotypeResource
        );

        return new GeneProcessingGroup(geneResources, mouseGeneModelStep, fishGeneModelStep);
    }
}
