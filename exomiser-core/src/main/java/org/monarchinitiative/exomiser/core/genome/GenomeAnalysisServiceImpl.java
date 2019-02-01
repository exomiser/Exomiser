/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.genome;

import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeAnalysisServiceImpl implements GenomeAnalysisService {

    private final GenomeAssembly genomeAssembly;

    private final GenomeDataService genomeDataService;
    private final VariantDataService variantDataService;
    private final VariantFactory variantFactory;

    public GenomeAnalysisServiceImpl(GenomeAssembly genomeAssembly, GenomeDataService genomeDataService, VariantDataService variantDataService, VariantFactory variantFactory) {
        this.genomeAssembly = genomeAssembly;
        this.genomeDataService = genomeDataService;
        this.variantDataService = variantDataService;
        this.variantFactory = variantFactory;
    }

    @Override
    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    @Override
    public List<Gene> getKnownGenes() {
        return genomeDataService.getKnownGenes();
    }

    @Override
    public Set<GeneIdentifier> getKnownGeneIdentifiers() {
        return genomeDataService.getKnownGeneIdentifiers();
    }

    @Override
    public List<RegulatoryFeature> getRegulatoryFeatures() {
        return genomeDataService.getRegulatoryFeatures();
    }

    @Override
    public List<TopologicalDomain> getTopologicallyAssociatedDomains() {
        return genomeDataService.getTopologicallyAssociatedDomains();
    }

    @Override
    public boolean variantIsWhiteListed(Variant variant) {
        return variantDataService.variantIsWhiteListed(variant);
    }

    @Override
    public FrequencyData getVariantFrequencyData(Variant variant, Set<FrequencySource> frequencySources) {
        return variantDataService.getVariantFrequencyData(variant, frequencySources);
    }

    @Override
    public PathogenicityData getVariantPathogenicityData(Variant variant, Set<PathogenicitySource> pathogenicitySources) {
        return variantDataService.getVariantPathogenicityData(variant, pathogenicitySources);
    }

    @Override
    public Stream<VariantEvaluation> createVariantEvaluations(Stream<VariantContext> variantContextStream) {
        return variantFactory.createVariantEvaluations(variantContextStream);
    }
}
