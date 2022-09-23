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
import org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology.AltToCurrentId;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology.OboOntologyTerm;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ontology.*;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.ontology.CopyResourceStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class OntologyProcessingGroup implements ProcessingGroup {

    private static final Logger logger = LoggerFactory.getLogger(OntologyProcessingGroup.class);

    private final List<Resource> ontologyResources;

    private final HpoResourceReader hpoResourceReader;
    private final OutputLineWriter<OboOntologyTerm> hpWriter;
    private final OutputLineWriter<AltToCurrentId> hpAltIdWriter;
    private final OwlSimPhenodigmProcessor hpHpPhenodigmProcessor;

    private final CopyResourceStep copyHpoResourceStep;

    private final MpoResourceReader mpoResourceReader;
    private final OutputLineWriter<OboOntologyTerm> mpWriter;
    private final OwlSimPhenodigmProcessor hpMpPhenodigmProcessor;

    private final ZpoResourceReader zpoResourceReader;
    private final OutputLineWriter<OboOntologyTerm> zpWriter;
    private final OwlSimPhenodigmProcessor hpZpPhenodigmProcessor;

    private OntologyProcessingGroup(List<Resource> ontologyResources, HpoResourceReader hpoResourceReader, OutputLineWriter<OboOntologyTerm> hpWriter, OutputLineWriter<AltToCurrentId> hpAltIdWriter, OwlSimPhenodigmProcessor hpHpPhenodigmProcessor, CopyResourceStep copyHpoResourceStep, MpoResourceReader mpoResourceReader, OutputLineWriter<OboOntologyTerm> mpWriter, OwlSimPhenodigmProcessor hpMpPhenodigmProcessor, ZpoResourceReader zpoResourceReader, OutputLineWriter<OboOntologyTerm> zpWriter, OwlSimPhenodigmProcessor hpZpPhenodigmProcessor) {
        this.ontologyResources = ontologyResources;
        ontologyResources.forEach(resource -> logger.debug("Using {}", resource));

        this.hpoResourceReader = hpoResourceReader;
        this.hpWriter = hpWriter;
        this.hpAltIdWriter = hpAltIdWriter;
        this.hpHpPhenodigmProcessor = hpHpPhenodigmProcessor;
        this.copyHpoResourceStep = copyHpoResourceStep;

        this.mpoResourceReader = mpoResourceReader;
        this.mpWriter = mpWriter;
        this.hpMpPhenodigmProcessor = hpMpPhenodigmProcessor;

        this.zpoResourceReader = zpoResourceReader;
        this.zpWriter = zpWriter;
        this.hpZpPhenodigmProcessor = hpZpPhenodigmProcessor;
    }

    public static OntologyProcessingGroup create(List<Resource> ontologyResources, Resource hpoResource, OutputLineWriter<OboOntologyTerm> hpWriter, OutputLineWriter<AltToCurrentId> hpAltIdWriter, OwlSimPhenodigmProcessor hpHpPhenodigmProcessor, CopyResourceStep copyHpoResourceStep, Resource mpoResource, OutputLineWriter<OboOntologyTerm> mpWriter, OwlSimPhenodigmProcessor hpMpPhenodigmProcessor, Resource zpoResource, OutputLineWriter<OboOntologyTerm> zpWriter, OwlSimPhenodigmProcessor hpZpPhenodigmProcessor) {
        HpoResourceReader hpoResourceReader = new HpoResourceReader(hpoResource);
        MpoResourceReader mpoResourceReader = new MpoResourceReader(mpoResource);
        ZpoResourceReader zpoResourceReader = new ZpoResourceReader(zpoResource);
        return new OntologyProcessingGroup(ontologyResources, hpoResourceReader, hpWriter, hpAltIdWriter, hpHpPhenodigmProcessor, copyHpoResourceStep, mpoResourceReader, mpWriter, hpMpPhenodigmProcessor, zpoResourceReader, zpWriter, hpZpPhenodigmProcessor);
    }

    @Override
    public String getName() {
        return "OntologyProcessingGroup";
    }

    @Override
    public List<Resource> getResources() {
        return ontologyResources;
    }

    @Override
    public void processResources() {
        // HP step
        OboOntology hpOntology = hpoResourceReader.read();
        List<OboOntologyTerm> hpTerms = hpOntology.getCurrentOntologyTerms();
        // id: label
        hpWriter.write(hpTerms);
        List<AltToCurrentId> altToCurrentIds = hpOntology.getIdToTerms().entrySet().stream()
                .map(entry -> new AltToCurrentId(entry.getKey(), entry.getValue().getId()))
                .toList();
        // alt_id : current_id
        hpAltIdWriter.write(altToCurrentIds);
        hpHpPhenodigmProcessor.process(hpTerms, hpTerms);

        copyHpoResourceStep.run();

        // MP step
        List<OboOntologyTerm> mpTerms = mpoResourceReader.read();
        mpWriter.write(mpTerms);
        hpMpPhenodigmProcessor.process(hpTerms, mpTerms);

        // ZP step
        List<OboOntologyTerm> zpTerms = zpoResourceReader.read();
        zpWriter.write(zpTerms);
        hpZpPhenodigmProcessor.process(hpTerms, zpTerms);
    }
}
