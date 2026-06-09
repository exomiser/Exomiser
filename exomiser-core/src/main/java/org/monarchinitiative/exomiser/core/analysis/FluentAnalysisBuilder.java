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

package org.monarchinitiative.exomiser.core.analysis;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import org.monarchinitiative.exomiser.core.filters.FailedVariantFilter;
import org.monarchinitiative.exomiser.core.filters.FrequencyFilter;
import org.monarchinitiative.exomiser.core.filters.PathogenicityFilter;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.HiPhiveOptions;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Non-public interface to define the behaviour of the Fluent API for building an Analysis.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
interface FluentAnalysisBuilder<R> {

    public R build();

    // Sources

    public FluentAnalysisBuilder<R> inheritanceModes(InheritanceModeOptions inheritanceModeOptions);

    public FluentAnalysisBuilder<R> analysisMode(AnalysisMode analysisMode);

    public FluentAnalysisBuilder<R> frequencySources(Set<FrequencySource> frequencySources);

    public FluentAnalysisBuilder<R> pathogenicitySources(Set<PathogenicitySource> pathogenicitySources);

    // Filters

    /**
     * Adds a {@link FailedVariantFilter} to the {@link Analysis}. This will remove variants without a 'PASS' or '.' in
     * the input VCF.
     *
     * @return An {@link AnalysisBuilder} with a {@link FailedVariantFilter} added to the analysis steps.
     */
    public FluentAnalysisBuilder<R> addFailedVariantFilter();

    public FluentAnalysisBuilder<R> addIntervalFilter(GeneticInterval interval);

    /**
     * @param chromosomalRegions regions within which variants should be considered in an analysis
     * @return An {@link AnalysisBuilder} with a {@link Collection < ChromosomalRegion >} added to the analysis steps.
     * @since 12.0.0
     */
    public FluentAnalysisBuilder<R> addIntervalFilter(Collection<ChromosomalRegion> chromosomalRegions);

    public FluentAnalysisBuilder<R> addGeneIdFilter(Set<String> entrezIds);

    public FluentAnalysisBuilder<R> addVariantEffectFilter(Set<VariantEffect> variantEffects);

    public FluentAnalysisBuilder<R> addQualityFilter(double cutoff);

    public FluentAnalysisBuilder<R> addAlleleBalanceFilter();

    public FluentAnalysisBuilder<R> addKnownVariantFilter();

    public FluentAnalysisBuilder<R> addFrequencyFilter(float cutOff);

    public FluentAnalysisBuilder<R> addGeneBlacklistFilter();

    /**
     * Add a frequency filter using the maximum frequency for any defined mode of inheritance as the cut-off. Calling this
     * method requires that the {@code inheritanceModes} method has already been called and supplied with a non-empty
     * {@link InheritanceModeOptions} instance.
     *
     * @return an {@link AnalysisBuilder} with an added {@link FrequencyFilter} instantiated with the maximum
     * frequency taken from the {@link InheritanceModeOptions}.
     * @since 11.0.0
     */
    public FluentAnalysisBuilder<R> addFrequencyFilter();

    default FluentAnalysisBuilder<R> addPathogenicityFilter(boolean keepNonPathogenic) {
        return addPathogenicityFilter(keepNonPathogenic, PathogenicityFilter.Target.ALL);
    }

    public FluentAnalysisBuilder<R> addPathogenicityFilter(boolean keepNonPathogenic, PathogenicityFilter.Target target);

    public FluentAnalysisBuilder<R> addPriorityScoreFilter(PriorityType priorityType, float minPriorityScore);

    public FluentAnalysisBuilder<R> addRegulatoryFeatureFilter();

    public FluentAnalysisBuilder<R> addInheritanceFilter();

    // Prioritisers
    public FluentAnalysisBuilder<R> addOmimPrioritiser();

    public FluentAnalysisBuilder<R> addPhivePrioritiser();

    public FluentAnalysisBuilder<R> addHiPhivePrioritiser();

    public FluentAnalysisBuilder<R> addHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions);

    public FluentAnalysisBuilder<R> addPhenixPrioritiser();

    public FluentAnalysisBuilder<R> addExomeWalkerPrioritiser(List<Integer> seedGenes);

    public FluentAnalysisBuilder addBoqaPrioritiser();

}
