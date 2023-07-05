/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.analysis.AnalysisStep;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.model.Gene;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;

/**
 * This interface is implemented by classes that perform prioritisation of genes
 * (i.e., {@link org.monarchinitiative.exomiser.core.model.Gene Gene} objects). In contrast to the classes
 * that implement {@code org.monarchinitiative.exomiser.filter.Filter}, which remove variants from
 * further consideration (e.g., because they are not predicted to be at all
 * pathogenic), FilterType is intended to work on genes (predict the relevance of
 * the gene to the disease, without taking the nature or pathogenicity of any
 * variant into account).
 * <p>
 * It is expected that the Exomiser will combine the evaluations of the Filter
 * and the FilterType evaluations in order to reach a final ranking of the genes
 * and variants into candidate disease-causing mutations.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @version 0.13 (13 May, 2013).
 * @see org.monarchinitiative.exomiser.core.filters.Filter
 */
public interface Prioritiser<T extends PriorityResult> extends AnalysisStep {

    /**
     * Apply a prioritization algorithm to a list of {@link Gene Gene} objects ranking the results against the similarity
     * to the input HPO ids.
     * This will have the side effect of adding the PriorityResult to the Gene object.
     *
     * @param sample
     * @param genes
     */
    default void prioritizeGenes(Sample sample, List<Gene> genes) {
        Map<Integer, Optional<T>> results = prioritise(sample, genes)
                .collect(groupingBy(PriorityResult::getGeneId, maxBy(comparingDouble(PriorityResult::getScore))));

        genes.forEach(gene -> results.getOrDefault(gene.getEntrezGeneID(), Optional.empty())
                .ifPresent(gene::addPriorityResult));
    }

    /**
     * Scores the list of genes by phenotypic similarity to the provided HPO identifiers returning a Stream of
     * PriorityResult from the Prioritiser.
     *
     * @param sample
     * @param genes
     * @return the stream of results.
     */
    Stream<T> prioritise(Sample sample, List<Gene> genes);

    /**
     * @return an enum constant representing the type of the implementing class.
     */
    PriorityType getPriorityType();

}
