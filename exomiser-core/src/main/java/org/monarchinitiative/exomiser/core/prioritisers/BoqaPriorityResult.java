package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.p2gx.boqa.core.analysis.BoqaResult;

import java.util.Map;

/**
 * This class groups {@link org.p2gx.boqa.core.analysis.BoqaResult} objects by associated gene.
 *
 * @param geneId      The NCBI gene id for this gene
 * @param geneSymbol  The gene symbol for this gene
 * @param score       The maximum score for all the {@link BoqaResult} linked to this gene
 * @param boqaResults A map of {@link Disease} to {@link BoqaResult} linked to this gene
 */
public record BoqaPriorityResult(int geneId, String geneSymbol, double score,
                                 Map<Disease, BoqaResult> boqaResults) implements PriorityResult {

    @Override
    public PriorityType priorityType() {
        return PriorityType.BOQA_PRIORITY;
    }

}
