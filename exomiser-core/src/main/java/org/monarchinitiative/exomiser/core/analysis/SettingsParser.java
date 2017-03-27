/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.analysis;

import com.google.common.collect.Sets;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.Exomiser;
import org.monarchinitiative.exomiser.core.filters.*;
import org.monarchinitiative.exomiser.core.genome.VariantDataService;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PrioritiserSettings;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityFactory;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Will set-up an {@link Exomiser} exome {@link Analysis} according to the original
 * Exomiser protocol. If you wish to analyse a whole genome you will want to set 
 * up an {@link Analysis} either programmatically with an {@link AnalysisFactory}
 * or via a YAML input file through the {@link AnalysisParser}
 *
 * @since 7.0.0
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class SettingsParser {

    private static final Logger logger = LoggerFactory.getLogger(SettingsParser.class);

    public static final Set<VariantEffect> NON_EXONIC_VARIANT_EFFECTS = Sets.immutableEnumSet(
            VariantEffect.UPSTREAM_GENE_VARIANT,
            VariantEffect.INTERGENIC_VARIANT,
            VariantEffect.DOWNSTREAM_GENE_VARIANT,
            VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT,
            VariantEffect.NON_CODING_TRANSCRIPT_INTRON_VARIANT,
            VariantEffect.SYNONYMOUS_VARIANT,
            VariantEffect.SPLICE_REGION_VARIANT,
            VariantEffect.REGULATORY_REGION_VARIANT
    );

    private static final Set<PathogenicitySource> MISSENSE_VARIANT_PATH_SOURCES = Sets.immutableEnumSet(
            PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER, PathogenicitySource.SIFT);

    private final PriorityFactory prioritiserFactory;
    private final VariantDataService variantDataService;

    @Autowired
    public SettingsParser(PriorityFactory prioritiserFactory, VariantDataService variantDataService) {
        this.prioritiserFactory = prioritiserFactory;
        this.variantDataService = variantDataService;
    }

    /**
     * Sets up an Analysis using the published Exomiser algorithm where variants
     * are filtered first, then genes and finally the prioritisers are run.
     *
     * @param settings
     * @return
     */
    public Analysis parse(Settings settings) {
        logger.info("SETTING-UP ANALYSIS");

        return Analysis.builder()
            .vcfPath(settings.getVcfPath())
            .pedPath(settings.getPedPath())
            .probandSampleName(settings.getProbandSampleName())
            .modeOfInheritance(settings.getModeOfInheritance())
            .hpoIds(settings.getHpoIds())
            .frequencySources(FrequencySource.ALL_EXTERNAL_FREQ_SOURCES)
            .pathogenicitySources(MISSENSE_VARIANT_PATH_SOURCES)
            .analysisMode(makeAnalysisMode(settings.runFullAnalysis()))
            .steps(makeAnalysisSteps(settings, settings))
            .build();
    }

    private AnalysisMode makeAnalysisMode(boolean runFullAnalysis) {
        return runFullAnalysis ? AnalysisMode.FULL : AnalysisMode.SPARSE;
    }

    private List<AnalysisStep> makeAnalysisSteps(FilterSettings filterSettings, PrioritiserSettings prioritiserSettings) {
        List<AnalysisStep> steps = new ArrayList<>();
        steps.addAll(makeFilters(filterSettings));
        //Prioritisers should ALWAYS run last.
        steps.addAll(makePrioritisers(prioritiserSettings));
        steps.forEach(step -> logger.info("ADDING ANALYSIS STEP {}", step));
        return steps;
    }

    private List<Filter> makeFilters(FilterSettings filterSettings) {
        List<Filter> filters = new ArrayList<>();
        //don't change the order here - variants should ALWAYS be filtered before
        //genes have their inheritance modes filtered otherwise the inheritance mode will break leading to altered
        //predictions downstream as we only want to test the mode for candidate variants.
        //inheritance modes are needed even if we don't have an inheritance gene filter set as the OMIM prioritiser relies on it
        filters.addAll(makeVariantFilters(filterSettings));
        filters.addAll(makeGeneFilters(filterSettings));
        return filters;
    }

    private List<VariantFilter> makeVariantFilters(FilterSettings settings) {
        List<VariantFilter> variantFilters = new ArrayList<>();
        //IMPORTANT: These are ordered by increasing computational difficulty and
        //the number of variants they will remove.
        //Don't change them as this will negatively effect performance.
        //FAILED VARIANTS
        if (settings.removeFailedVariants()) {
            variantFilters.add(new FailedVariantFilter());
        }
        //GENE_ID
        if (!settings.getGenesToKeep().isEmpty()) {
            variantFilters.add(new EntrezGeneIdFilter(settings.getGenesToKeep()));
        }
        //INTERVAL
        if (settings.getGeneticInterval() != null) {
            variantFilters.add(new IntervalFilter(settings.getGeneticInterval()));
        }
        //TARGET
        //this would make more sense to be called 'removeOffTargetVariants'
        if (settings.keepOffTargetVariants() == false) {
            //add off target variant effect here
            variantFilters.add(new VariantEffectFilter(NON_EXONIC_VARIANT_EFFECTS));
        }
        //QUALITY
        if (settings.getMinimumQuality() != 0) {
            variantFilters.add(new QualityFilter(settings.getMinimumQuality()));
        }
        //KNOWN VARIANTS
        if (settings.removeKnownVariants()) {
            variantFilters.add(new FrequencyDataProvider(variantDataService, FrequencySource.ALL_EXTERNAL_FREQ_SOURCES, new KnownVariantFilter()));
        }
        //FREQUENCY
        variantFilters.add(new FrequencyDataProvider(variantDataService, FrequencySource.ALL_EXTERNAL_FREQ_SOURCES, new FrequencyFilter(settings.getMaximumFrequency())));
        //PATHOGENICITY
        // if keeping off-target variants need to remove the pathogenicity cutoff to ensure that these variants always
        // pass the pathogenicity filter and still get scored for pathogenicity
        variantFilters.add(new PathogenicityDataProvider(variantDataService, MISSENSE_VARIANT_PATH_SOURCES, new PathogenicityFilter(settings.keepNonPathogenicVariants())));
        return variantFilters;
    }

    private List<GeneFilter> makeGeneFilters(FilterSettings settings) {
        List<GeneFilter> geneFilters = new ArrayList<>();
        //INHERITANCE
        if (settings.getModeOfInheritance() != ModeOfInheritance.ANY) {
            geneFilters.add(new InheritanceFilter(settings.getModeOfInheritance()));
        }
        return geneFilters;
    }

    private List<Prioritiser> makePrioritisers(PrioritiserSettings settings) {
        List<Prioritiser> prioritisers = new ArrayList<>();

        PriorityType prioritiserType = settings.getPrioritiserType();
        if (prioritiserType == PriorityType.NONE) {
            return prioritisers;
        }

        //always run OMIM unless the user specified what they really don't want to run any prioritisers
        Prioritiser omimPrioritiser = prioritiserFactory.makeOmimPrioritiser();
        prioritisers.add(omimPrioritiser);
        //don't add OMIM prioritiser twice to the list
        if (prioritiserType != PriorityType.OMIM_PRIORITY) {
            Prioritiser prioritiser = prioritiserFactory.makePrioritiser(settings);
            prioritisers.add(prioritiser);
        }
        return prioritisers;
    }

}
