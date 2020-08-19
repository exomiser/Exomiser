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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease;

import com.google.common.collect.ListMultimap;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease.*;
import org.monarchinitiative.exomiser.data.phenotype.processors.steps.ProcessingStep;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class DiseaseGeneStep implements ProcessingStep {

    private static final Logger logger = LoggerFactory.getLogger(DiseaseGeneStep.class);

    private final OmimGeneMap2Reader omimGeneMap2Reader;
    private final Product1DiseaseXmlReader product1DiseaseXmlReader;
    private final Product6DiseaseGeneXmlReader product6DiseaseGeneXmlReader;
    private final Product9InheritanceXmlReader product9InheritanceXmlReader;
    private final OutputLineWriter<DiseaseGene> diseaseGeneWriter;

    private DiseaseGeneStep(OmimGeneMap2Reader omimGeneMap2Reader, Product1DiseaseXmlReader product1DiseaseXmlReader, Product6DiseaseGeneXmlReader product6DiseaseGeneXmlReader, Product9InheritanceXmlReader product9InheritanceXmlReader, OutputLineWriter<DiseaseGene> diseaseGeneWriter) {
        this.omimGeneMap2Reader = omimGeneMap2Reader;
        this.product1DiseaseXmlReader = product1DiseaseXmlReader;
        this.product6DiseaseGeneXmlReader = product6DiseaseGeneXmlReader;
        this.product9InheritanceXmlReader = product9InheritanceXmlReader;
        this.diseaseGeneWriter = diseaseGeneWriter;
    }

    public static DiseaseGeneStep create(Resource phenotypeAnnotationsResource, Resource geneMap2Resource, Resource mimToGeneResource, Resource product1Resource, Resource product6Resource, Resource product9Resource, OutputLineWriter<DiseaseGene> diseaseGeneWriter) {
        // OMIM resources
        DiseaseInheritanceCacheReader diseaseInheritanceCacheReader = new DiseaseInheritanceCacheReader(phenotypeAnnotationsResource);
        OmimGeneMap2Reader omimGeneMap2Reader = new OmimGeneMap2Reader(diseaseInheritanceCacheReader, geneMap2Resource);

        // Orphanet resources
        // Orphanet-OMIM Disease mappings
        Product1DiseaseXmlReader product1DiseaseXmlReader = new Product1DiseaseXmlReader(product1Resource);
        // Orphanet-OMIM Gene mappings
        OmimMimToGeneReader mimToGeneReader = new OmimMimToGeneReader(mimToGeneResource);
        Product6DiseaseGeneXmlReader product6DiseaseGeneXmlReader = new Product6DiseaseGeneXmlReader(mimToGeneReader, product6Resource);
        // Orphanet MOI
        Product9InheritanceXmlReader product9InheritanceXmlReader = new Product9InheritanceXmlReader(product9Resource);

        return new DiseaseGeneStep(omimGeneMap2Reader, product1DiseaseXmlReader, product6DiseaseGeneXmlReader, product9InheritanceXmlReader, diseaseGeneWriter);
    }

    @Override
    public void run() {
        // Read OMIM
        List<DiseaseGene> omimDiseases = omimGeneMap2Reader.read();

        // parse all the Orphanet resources
        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = product1DiseaseXmlReader.read();
        ListMultimap<String, DiseaseGene> orphaDiseaseGenes = product6DiseaseGeneXmlReader.read();
        ListMultimap<String, InheritanceMode> inheritanceModesMap = product9InheritanceXmlReader.read();

        // return a list of DiseaseGene and write out
        OrphanetDiseaseGeneFactory orphanetDiseaseGeneFactory = new OrphanetDiseaseGeneFactory(omimDiseases, orphaOmimMappings, orphaDiseaseGenes, inheritanceModesMap);

        List<DiseaseGene> diseaseGenes = new ArrayList<>(omimDiseases);
        diseaseGenes.addAll(orphanetDiseaseGeneFactory.buildDiseaseGeneAssociations());
        diseaseGeneWriter.write(diseaseGenes);
    }
}
