/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.svart.GenomicInterval;
import org.monarchinitiative.svart.GenomicVariant;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class GenomeAnalysisServiceImpl implements GenomeAnalysisService {

    private final GenomeAssembly genomeAssembly;

    private final GenomeDataService genomeDataService;
    private final VariantDataService variantDataService;
    private final VariantAnnotator variantAnnotator;

    public GenomeAnalysisServiceImpl(GenomeAssembly genomeAssembly, GenomeDataService genomeDataService, VariantDataService variantDataService, VariantAnnotator variantAnnotator) {
        this.genomeAssembly = genomeAssembly;
        this.genomeDataService = genomeDataService;
        this.variantDataService = variantDataService;
        this.variantAnnotator = variantAnnotator;
    }

    @Override
    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    @Override
    public VariantAnnotator getVariantAnnotator() {
        return variantAnnotator;
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
    public ClinVarData getClinVarData(Variant variant) {
        return variantDataService.getClinVarData(variant);
    }

    @Override
    public ClinVarData getClinVarData(GenomicVariant genomicVariant) {
        return variantDataService.getClinVarData(genomicVariant);
    }

    @Override
    public Map<GenomicVariant, ClinVarData> findClinVarRecordsOverlappingInterval(GenomicInterval genomicInterval) {
        return variantDataService.findClinVarRecordsOverlappingInterval(genomicInterval);
    }
}
