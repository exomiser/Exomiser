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

package org.monarchinitiative.exomiser.core.analysis;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.SubModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.genome.GenomeAnalysisServiceProvider;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.phenotype.service.OntologyService;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static org.monarchinitiative.exomiser.core.model.frequency.FrequencySource.*;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.*;

/**
 * Package-private utility class to support creating analyses based on a preset. These presets are known-good configurations
 * which have given good results on the 100K genome project cases.
 *
 * @since 13.0.0
 */
class AnalysisPresetBuilder {

    private static final InheritanceModeOptions DEFAULT_INHERITANCE_MODE_OPTIONS;

    static {
        Map<SubModeOfInheritance, Float> inheritanceModeFrequencyCutoffs = new EnumMap<>(SubModeOfInheritance.class);
        // all frequencies are in percentage values
        inheritanceModeFrequencyCutoffs.put(SubModeOfInheritance.AUTOSOMAL_DOMINANT, 0.1f);
        inheritanceModeFrequencyCutoffs.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_COMP_HET, 2.0f);
        inheritanceModeFrequencyCutoffs.put(SubModeOfInheritance.AUTOSOMAL_RECESSIVE_HOM_ALT, 0.1f);
        inheritanceModeFrequencyCutoffs.put(SubModeOfInheritance.X_DOMINANT, 0.1f);
        inheritanceModeFrequencyCutoffs.put(SubModeOfInheritance.X_RECESSIVE_COMP_HET, 2.0f);
        inheritanceModeFrequencyCutoffs.put(SubModeOfInheritance.X_RECESSIVE_HOM_ALT, 0.1f);
        inheritanceModeFrequencyCutoffs.put(SubModeOfInheritance.MITOCHONDRIAL, 0.2f);
        DEFAULT_INHERITANCE_MODE_OPTIONS = InheritanceModeOptions.of(inheritanceModeFrequencyCutoffs);
    }

    private static final EnumSet<FrequencySource> DEFAULT_FREQUENCY_SOURCES = EnumSet.of(
            ESP_AA, ESP_ALL, ESP_EA,
            UK10K,
            //GNOMAD_E_ASJ,
            GNOMAD_E_AFR, GNOMAD_E_AMR,
            GNOMAD_E_EAS, GNOMAD_E_FIN,
            GNOMAD_E_NFE, GNOMAD_E_OTH, GNOMAD_E_SAS,
            //Excluded due to population size:
            // GNOMAD_G_ASJ, GNOMAD_G_AMI, GNOMAD_G_MID
            GNOMAD_G_AFR, GNOMAD_G_AMR,
            GNOMAD_G_EAS, GNOMAD_G_FIN,
            GNOMAD_G_NFE, GNOMAD_G_OTH, GNOMAD_G_SAS,
            ALFA_AFA, ALFA_AFO, ALFA_AFR,
            ALFA_EUR, ALFA_LAC, ALFA_LEN,
            ALFA_SAS, ALFA_EAS, ALFA_ASN,
            ALFA_OAS, ALFA_OTR, ALFA_TOT
    );

    private static final HiPhiveOptions HI_PHIVE_OPTIONS = HiPhiveOptions.builder()
            .runParams("human, mouse, fish, ppi")
            .build();

    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider;
    private final PriorityFactory prioritiserFactory;
    private final OntologyService ontologyService;

    /**
     * @param genomeAnalysisServiceProvider
     * @param prioritiserFactory
     * @param ontologyService
     */
    // TODO: This could take the AnalysisFactory and call AnalysisFactory.getAnalysisBuilder() OR be created and called by AnalysisFactory
    protected AnalysisPresetBuilder(GenomeAnalysisServiceProvider genomeAnalysisServiceProvider, PriorityFactory prioritiserFactory, OntologyService ontologyService) {
        this.genomeAnalysisServiceProvider = genomeAnalysisServiceProvider;
        this.prioritiserFactory = prioritiserFactory;
        this.ontologyService = ontologyService;
    }

    protected Analysis buildGenomePreset() {
        return new AnalysisBuilder(genomeAnalysisServiceProvider, prioritiserFactory, ontologyService)
                .analysisMode(AnalysisMode.PASS_ONLY)
                .inheritanceModes(DEFAULT_INHERITANCE_MODE_OPTIONS)
                .frequencySources(DEFAULT_FREQUENCY_SOURCES)
                .pathogenicitySources(EnumSet.of(REVEL, MVP, REMM, SPLICE_AI))
                .addHiPhivePrioritiser(HI_PHIVE_OPTIONS)
                .addPriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.5f)// will remove a lot of the weak PPI hits
                .addFailedVariantFilter()
                .addRegulatoryFeatureFilter()
                .addFrequencyFilter()
                .addPathogenicityFilter(true)
                .addInheritanceFilter()
                .addOmimPrioritiser()
                .build();
    }

    protected Analysis buildExomePreset() {
        return new AnalysisBuilder(genomeAnalysisServiceProvider, prioritiserFactory, ontologyService)
                .analysisMode(AnalysisMode.PASS_ONLY)
                .inheritanceModes(DEFAULT_INHERITANCE_MODE_OPTIONS)
                .frequencySources(DEFAULT_FREQUENCY_SOURCES)
                .pathogenicitySources(EnumSet.of(REVEL, MVP, SPLICE_AI))
                .addVariantEffectFilter(EnumSet.of(
                        VariantEffect.FIVE_PRIME_UTR_EXON_VARIANT,
                        VariantEffect.FIVE_PRIME_UTR_INTRON_VARIANT,
                        VariantEffect.THREE_PRIME_UTR_EXON_VARIANT,
                        VariantEffect.THREE_PRIME_UTR_INTRON_VARIANT,
                        VariantEffect.NON_CODING_TRANSCRIPT_EXON_VARIANT,
                        VariantEffect.NON_CODING_TRANSCRIPT_INTRON_VARIANT,
                        VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT,
                        VariantEffect.UPSTREAM_GENE_VARIANT,
                        VariantEffect.DOWNSTREAM_GENE_VARIANT,
                        VariantEffect.INTERGENIC_VARIANT,
                        VariantEffect.REGULATORY_REGION_VARIANT
                ))
                .addFailedVariantFilter()
                .addFrequencyFilter()
                .addPathogenicityFilter(true)
                .addInheritanceFilter()
                .addOmimPrioritiser()
                .addHiPhivePrioritiser(HI_PHIVE_OPTIONS)
                .build();
    }

    protected Analysis buildPhenotypeOnlyPreset() {
        return new AnalysisBuilder(genomeAnalysisServiceProvider, prioritiserFactory, ontologyService)
                .analysisMode(AnalysisMode.PASS_ONLY)
                .inheritanceModes(InheritanceModeOptions.empty())
                .addOmimPrioritiser()
                .addHiPhivePrioritiser(HI_PHIVE_OPTIONS)
                .build();
    }
}
