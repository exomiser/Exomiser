/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This class will add annotations to genes based on their annotations to OMIM or Orphanet disease entries in the
 * exomiser database.
 *
 * Since version 10.0.0 It will always produce a score of 1 for a gene as the gene scoring has been consolidated in one
 * place to be able to take into account the score under multiple inheritance models.
 *
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @version 0.16 (28 January,2014)
 */
public class OMIMPriority implements Prioritiser<OMIMPriorityResult> {

    private static final Logger logger = LoggerFactory.getLogger(OMIMPriority.class);

    private final PriorityService priorityService;

    public OMIMPriority(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    /**
     * Flag for output field representing OMIM.
     */
    @Override
    public PriorityType getPriorityType() {
        return PriorityType.OMIM_PRIORITY;
    }

    /**
     * This method just annotates each gene with OMIM/Orphanet data, if
     * available, and shows a link in the HTML output.
     *
     * @param genes A list of the {@link Gene} objects that
     * have survived the filtering (i.e., have rare, potentially pathogenic
     * variants).
     */
    @Override
    public void prioritizeGenes(List<String> hpoIds, List<Gene> genes) {
        for (Gene gene : genes) {
            OMIMPriorityResult result = prioritiseGene().apply(gene);
            gene.addPriorityResult(result);
        }
    }

    @Override
    public Stream<OMIMPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {
        return genes.stream().map(prioritiseGene());
    }

    /**
     * If the gene is not contained in the database, we return an empty
     * but initialized RelevanceScore object. Otherwise, we retrieve a list of
     * all OMIM and Orphanet diseases associated with the entrez Gene.
     *
     * Since version 10.0.0 all results from this method will have a score of 1.
     *
     **/
    private Function<Gene, OMIMPriorityResult> prioritiseGene() {
        return gene -> {
            List<Disease> diseases = priorityService.getDiseaseDataAssociatedWithGeneId(gene.getEntrezGeneID());
            // This is a non-punitive prioritiser. We're relying on the other prioritisers to do the main ranking
            // and this class to add in the known diseases associated with the gene.
            // Arguably this shouldn't even exist as a prioritiser any more.
            return new OMIMPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), 1, diseases);
        };
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(OMIMPriority.class.getName());
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass();
    }

    @Override
    public String toString() {
        return "OmimPrioritiser{}";
    } 
}
