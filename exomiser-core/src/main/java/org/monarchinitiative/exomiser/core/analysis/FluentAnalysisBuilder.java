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
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.filters.FailedVariantFilter;
import org.monarchinitiative.exomiser.core.filters.FrequencyFilter;
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

    public FluentAnalysisBuilder analysisMode(AnalysisMode analysisMode);

    public FluentAnalysisBuilder frequencySources(Set<FrequencySource> frequencySources);

    public FluentAnalysisBuilder pathogenicitySources(Set<PathogenicitySource> pathogenicitySources);

    // Filters

    /**
     * Adds a {@link FailedVariantFilter} to the {@link Analysis}. This will remove variants without a 'PASS' or '.' in
     * the input VCF.
     *
     * @return An {@link AnalysisBuilder} with a {@link FailedVariantFilter} added to the analysis steps.
     */
    public FluentAnalysisBuilder addFailedVariantFilter();

    public FluentAnalysisBuilder addIntervalFilter(GeneticInterval interval);

    /**
     * @param chromosomalRegions regions within which variants should be considered in an analysis
     * @return An {@link AnalysisBuilder} with a {@link Collection < ChromosomalRegion >} added to the analysis steps.
     * @since 12.0.0
     */
    public FluentAnalysisBuilder addIntervalFilter(Collection<ChromosomalRegion> chromosomalRegions);

    public FluentAnalysisBuilder addGeneIdFilter(Set<String> entrezIds);

    public FluentAnalysisBuilder addVariantEffectFilter(Set<VariantEffect> variantEffects);

    public FluentAnalysisBuilder addQualityFilter(double cutoff);

    public FluentAnalysisBuilder addKnownVariantFilter();

    public FluentAnalysisBuilder addFrequencyFilter(float cutOff);

    public FluentAnalysisBuilder addGeneBlacklistFilter();

    /**
     * Add a frequency filter using the maximum frequency for any defined mode of inheritance as the cut-off. Calling this
     * method requires that the {@code inheritanceModes} method has already been called and supplied with a non-empty
     * {@link InheritanceModeOptions} instance.
     *
     * @return an {@link AnalysisBuilder} with an added {@link FrequencyFilter} instantiated with the maximum
     * frequency taken from the {@link InheritanceModeOptions}.
     * @since 11.0.0
     */
    public FluentAnalysisBuilder addFrequencyFilter();

    public FluentAnalysisBuilder addPathogenicityFilter(boolean keepNonPathogenic);

    public FluentAnalysisBuilder addPriorityScoreFilter(PriorityType priorityType, float minPriorityScore);

    public FluentAnalysisBuilder addRegulatoryFeatureFilter();

    public FluentAnalysisBuilder addInheritanceFilter();

    // Prioritisers
    public FluentAnalysisBuilder addOmimPrioritiser();

    public FluentAnalysisBuilder addPhivePrioritiser();

    public FluentAnalysisBuilder addHiPhivePrioritiser();

    public FluentAnalysisBuilder addHiPhivePrioritiser(HiPhiveOptions hiPhiveOptions);

    public FluentAnalysisBuilder addPhenixPrioritiser();

    public FluentAnalysisBuilder addExomeWalkerPrioritiser(List<Integer> seedGenes);

}
