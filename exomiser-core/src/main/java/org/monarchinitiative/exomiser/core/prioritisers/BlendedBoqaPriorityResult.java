package org.monarchinitiative.exomiser.core.prioritisers;

import org.apache.commons.collections.Unmodifiable;
import org.apache.commons.collections.map.UnmodifiableMap;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.p2gx.boqa.core.analysis.CandidateResult;
import org.p2gx.boqa.core.diseases.TargetDisease;

import java.util.List;
import java.util.Map;

/**
 * We return one of thiese objects for each blended diseases that is better than its best component single disease.
 * The genes list will allow us to show some variants etc., and the CandidateResult has all of the details coming
 * from BOQA.
 * @param genes
 * @param
 */
public record BlendedBoqaPriorityResult (
        List<Gene> genes,
        CandidateResult candidate
) implements PriorityResult {
    public Map <Disease, CandidateResult> boqaResults(){
        return Map.of(genes.getFirst().associatedDiseases().getFirst(), candidate);
    }
    @Override
    public double score(){return candidate.score();}
    @Override
    // TODO temporary! We need to figure this out
    public int geneId(){return genes.stream().map(Gene::entrezGeneId).toList().getFirst();}
    @Override
    // TODO temporary! We need to figure this out
    public String geneSymbol() { return candidate.finalDiseases().stream().map(TargetDisease::diseaseId).toString();}
    @Override
    public PriorityType priorityType() {
        return PriorityType.BLENDED_BOQA_PRIORITY;
    }
}
