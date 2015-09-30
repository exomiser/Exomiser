/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis;

import de.charite.compbio.exomiser.core.factories.VariantDataService;
import de.charite.compbio.exomiser.core.filters.EntrezGeneIdFilter;
import de.charite.compbio.exomiser.core.filters.Filter;
import de.charite.compbio.exomiser.core.filters.FilterSettings;
import de.charite.compbio.exomiser.core.filters.FrequencyDataProvider;
import de.charite.compbio.exomiser.core.filters.FrequencyFilter;
import de.charite.compbio.exomiser.core.filters.GeneFilter;
import de.charite.compbio.exomiser.core.filters.InheritanceFilter;
import de.charite.compbio.exomiser.core.filters.IntervalFilter;
import de.charite.compbio.exomiser.core.filters.KnownVariantFilter;
import de.charite.compbio.exomiser.core.filters.PathogenicityDataProvider;
import de.charite.compbio.exomiser.core.filters.PathogenicityFilter;
import de.charite.compbio.exomiser.core.filters.QualityFilter;
import de.charite.compbio.exomiser.core.filters.VariantEffectFilter;
import de.charite.compbio.exomiser.core.filters.VariantFilter;
import de.charite.compbio.exomiser.core.model.frequency.FrequencySource;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicitySource;
import de.charite.compbio.exomiser.core.prioritisers.Prioritiser;
import de.charite.compbio.exomiser.core.prioritisers.PrioritiserSettings;
import de.charite.compbio.exomiser.core.prioritisers.PriorityFactory;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for analysing variant data. This will orchestrate the set-up of
 * Filters and Priotitisers according to the supplied settings and then apply
 * them to the data.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SettingsParser {

    private static final Logger logger = LoggerFactory.getLogger(SettingsParser.class);

    public static final Set<VariantEffect> NON_EXONIC_VARIANT_EFFECTS = EnumSet.of(
            VariantEffect.UPSTREAM_GENE_VARIANT,
            VariantEffect.INTERGENIC_VARIANT,
            VariantEffect.DOWNSTREAM_GENE_VARIANT,
            VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT,
            VariantEffect.NON_CODING_TRANSCRIPT_INTRON_VARIANT,
            VariantEffect.SYNONYMOUS_VARIANT,
            VariantEffect.SPLICE_REGION_VARIANT,
            VariantEffect.REGULATORY_REGION_VARIANT
    );

    private static final Set<PathogenicitySource> MISSENSE_VARIANT_PATH_SOURCES = EnumSet.of(
            PathogenicitySource.POLYPHEN, PathogenicitySource.MUTATION_TASTER, PathogenicitySource.SIFT);

    private final PriorityFactory prioritiserFactory;
    private final VariantDataService variantDataService;

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
        PriorityType prioritiserType = settings.getPrioritiserType();

        Analysis analysis = new Analysis();
        analysis.setVcfPath(settings.getVcfPath());
        analysis.setPedPath(settings.getPedPath());
        analysis.setModeOfInheritance(settings.getModeOfInheritance());
        analysis.setHpoIds(settings.getHpoIds());
        analysis.setFrequencySources(FrequencySource.ALL_EXTERNAL_FREQ_SOURCES);
        analysis.setPathogenicitySources(MISSENSE_VARIANT_PATH_SOURCES);
        analysis.setScoringMode(prioritiserType.getScoringMode());
        if (settings.runFullAnalysis()) {
            analysis.setAnalysisMode(AnalysisMode.FULL);
        }

        List<AnalysisStep> analysisSteps = makeAnalysisSteps(settings, settings);
        analysisSteps.forEach(step -> {
            logger.info("ADDING ANALYSIS STEP {}", step);
            analysis.addStep(step);
        });

        return analysis;
    }

    public List<AnalysisStep> makeAnalysisSteps(FilterSettings filterSettings, PrioritiserSettings prioritiserSettings) {
        List<AnalysisStep> steps = new ArrayList<>();
        steps.addAll(makeFilters(filterSettings));
        //Prioritisers should ALWAYS run last.
        steps.addAll(makePrioritisers(prioritiserSettings));
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
        variantFilters.add(new PathogenicityDataProvider(variantDataService, MISSENSE_VARIANT_PATH_SOURCES, new PathogenicityFilter(settings.removePathFilterCutOff())));
        return variantFilters;
    }

    private List<GeneFilter> makeGeneFilters(FilterSettings settings) {
        List<GeneFilter> geneFilters = new ArrayList<>();
        //INHERITANCE
        if (settings.getModeOfInheritance() != ModeOfInheritance.UNINITIALIZED) {
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
            Prioritiser prioritiser = prioritiserFactory.makePrioritiser(prioritiserType, settings);
            prioritisers.add(prioritiser);
        }
        return prioritisers;
    }

}
