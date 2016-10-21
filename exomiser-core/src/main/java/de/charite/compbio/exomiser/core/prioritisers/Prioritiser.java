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

package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.analysis.AnalysisStep;
import de.charite.compbio.exomiser.core.model.Gene;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.comparingDouble;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;

/**
 * This interface is implemented by classes that perform prioritisation of genes
 * (i.e., {@link de.charite.compbio.exomiser.exome.Gene Gene} objects). In contrast to the classes
 * that implement {@code de.charite.compbio.exomiser.filter.Filter}, which remove variants from
 * further consideration (e.g., because they are not predicted to be at all
 * pathogenic), FilterType is intended to work on genes (predict the relevance of
 * the gene to the disease, without taking the nature or pathogenicity of any
 * variant into account).
 * <P>
 * It is expected that the Exomizer will combine the evaluations of the Filter
 * and the FilterType evaluations in order to reach a final ranking of the genes
 * and variants into candidate disease-causing mutations.
 *
 * @author Peter N Robinson
 * @version 0.13 (13 May, 2013).
 * @see de.charite.compbio.exomiser.filter.Filter
 */
public interface Prioritiser extends AnalysisStep {

    /**
     * Apply a prioritization algorithm to a list of
     * {@link de.charite.compbio.exomiser.core.model.Gene Gene} objects. This will have the side effect
     * of setting the Class variable {@link de.charite.compbio.exomiser.core.model.Gene#priorityScore}
     * correspondingly. This, together with the filter scores of the {@link jannovar.exome.Variant Variant}
     * {@link de.charite.compbio.exomiser.core.model.Gene Gene} objects can then be used to sort the
     * {@link de.charite.compbio.exomiser.core.model.Gene Gene} objects.
     * <p>
     *
     * @param genes
     */
    default void prioritizeGenes(List<Gene> genes){
        Map<String, Optional<PriorityResult>> results = prioritise(genes)
                .collect(groupingBy(PriorityResult::getGeneSymbol, maxBy(comparingDouble(PriorityResult::getScore))));

        genes.forEach(gene -> {
            Optional<PriorityResult> result = results.getOrDefault(gene.getGeneSymbol(), Optional.empty());
            if (result.isPresent()) {
                gene.addPriorityResult(result.get());
            }
        });
    }

    //TODO: Enable this. Consider using GeneIdentifier objects as we want to decouple Gene from this package.
    // OmimiPrioritiser will break though as this is the only prioritiser using anything other than geneId and geneSymbol.
    //    public List<? extends PriorityResult> prioritizeGenes(Collection<String> hpoIds, List<Gene> genes);

    /**
     * Applies the prioritiser to the list of genes and returns a Stream of PriorityResult from the Prioritiser.
     * @param genes
     * @return the stream of results.
     */
    Stream<? extends PriorityResult> prioritise(List<Gene> genes);

    /**
     * @return an enum constant representing the type of the implementing class.
     */
    PriorityType getPriorityType();

}
