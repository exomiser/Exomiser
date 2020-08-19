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
import org.monarchinitiative.exomiser.data.phenotype.processors.groups.OntologyProcessingGroup;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology.AltToCurrentId;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology.OboOntologyTerm;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ontology.OwlSimPhenodigmProcessor;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.ontology.CopyResourceStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class OntologyResourceConfig {

    private final ResourceConfigurationProperties resourceProperties;
    private final Path processPath;
    private final Path releasePath;
    private final ResourceBuilder resourceBuilder;

    public OntologyResourceConfig(ResourceConfigurationProperties resourceProperties, ReleaseFileSystem releaseFileSystem, ResourceBuilder resourceBuilder) {
        this.resourceProperties = resourceProperties;
        this.processPath = releaseFileSystem.processedDir();
        this.releasePath = releaseFileSystem.releaseDir();
        this.resourceBuilder = resourceBuilder;
    }

    @Bean
    public OntologyProcessingGroup ontologyProcessingGroup() {
        // Human
        Resource hpoResource = resourceBuilder.buildResource(resourceProperties.getHp());
        OutputLineWriter<OboOntologyTerm> hpWriter = new OutputLineWriter<>(processPath.resolve("hpo.pg"));
        OutputLineWriter<AltToCurrentId> hpAltIdWriter = new OutputLineWriter<>(processPath.resolve("hp_alt_ids.pg"));
        Resource hpHpMappingsResource = resourceBuilder.buildResource(resourceProperties.getHpHpMappings());
        OwlSimPhenodigmProcessor hpHpPhenodigmProcessor = new OwlSimPhenodigmProcessor(hpHpMappingsResource, processPath.resolve("hpHpmapping.pg"));
        // copy the HPO file to the release directory
        CopyResourceStep copyHpoResourceStep = new CopyResourceStep(hpoResource, releasePath);

        // Mouse
        Resource mpoResource = resourceBuilder.buildResource(resourceProperties.getMp());
        OutputLineWriter<OboOntologyTerm> mpWriter = new OutputLineWriter<>(processPath.resolve("mp.pg"));
        Resource hpMpMappingsResource = resourceBuilder.buildResource(resourceProperties.getHpMpMappings());
        OwlSimPhenodigmProcessor hpMpPhenodigmProcessor = new OwlSimPhenodigmProcessor(hpMpMappingsResource, processPath.resolve("hpMpMapping.pg"));

        // Fish
        Resource zpoResource = resourceBuilder.buildResource(resourceProperties.getZp());
        OutputLineWriter<OboOntologyTerm> zpWriter = new OutputLineWriter<>(processPath.resolve("zp.pg"));
        Resource hpZpMappingsResource = resourceBuilder.buildResource(resourceProperties.getHpZpMappings());
        OwlSimPhenodigmProcessor hpZpPhenodigmProcessor = new OwlSimPhenodigmProcessor(hpZpMappingsResource, processPath.resolve("hpZpMapping.pg"));

        List<Resource> ontologyResources = List.of(
                hpoResource,
                hpHpMappingsResource,
                mpoResource,
                hpMpMappingsResource,
                zpoResource,
                hpZpMappingsResource
        );

        return OntologyProcessingGroup.create(ontologyResources, hpoResource, hpWriter, hpAltIdWriter, hpHpPhenodigmProcessor, copyHpoResourceStep, mpoResource, mpWriter, hpMpPhenodigmProcessor, zpoResource, zpWriter, hpZpPhenodigmProcessor);
    }

}