package org.monarchinitiative.exomiser.core.prioritisers;

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.prioritisers.BoqaPrioritiser.ExomiserPatientData;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.p2gx.boqa.core.PatientData;
import org.p2gx.boqa.core.algorithm.BoqaCounts;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;



/** BOQA now has a similar Class that we will import. For now, this is here for clarity. 
 * I think we want to additionally add more information about the genes and the variants so that
 * we will have everything in one place for the HTML or TSV output.
 */
final record TargetDisease(
    String diseaseId,
    String diseaseLabel,
    String geneId,
    String geneSymbol //, and variant data for HTML
) {
}

/** We need to return enough data to enable Exomiser to create the HTML output.
 * BOQA now has a similar Class that we will import. For now, this is here for clarity.
 */
final record BlendedResult(
    List<TargetDisease> diseaseList,
    TargetDisease finalDisease, 
    /* Boqa Counts for each component (or just one for a single disease) */
    List<BoqaCounts> boqaCountsList,
    /* Boqa counts for melded (or this is identical to the above if there is just one, i.e., single disease) */
    BoqaCounts finalCounts,
    double score
){

    boolean isBlended(){ return diseaseList.size()>1; }
    // if we have two blended diseases, boqCountsList has disease1, disease2, and disease1+2 in that order
    // alternatively, we dou

}


/**
 * Implements BOQA for multiple genetic diagnosis (Blended diseases)
 * BlendedBoqaPriority
 */
public class BlendedBoqaPriority implements Prioritiser<BoqaPriorityResult> {
    private final PriorityService priorityService;
    private final Ontology hpo;
    private final HpoDiseases hpoDiseases;
    private final static Double GENE_SCORE_THRESHOLD = 0.90;



    public BlendedBoqaPriority(PriorityService priorityService, Ontology hpo, HpoDiseases diseases) {
            this.priorityService = priorityService;
            this.hpo = hpo;
            this.hpoDiseases = diseases;
        }





   


    @Override
    public Stream<BoqaPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {
        // 1. Find genes with candidate pathogenic variants
        Set<TargetDisease> candidates = new HashSet<>();
        for (Gene gene: genes) {
            List<Disease> diseases = priorityService.getDiseaseDataAssociatedWithGeneId(gene.entrezGeneId());
            for (Disease d: diseases) {
                InheritanceMode mode = d.inheritanceMode();
                for (ModeOfInheritance moi: mode.toModeOfInheritance()) {
                    if (geneCompatibleWithInheritanceMode(gene, mode, moi)) {
                        GeneScore score = gene.geneScoreForMode(moi);
                        if (score.combinedScore()>GENE_SCORE_THRESHOLD) {
                            candidates.add(new TargetDisease(d.diseaseId(), d.diseaseName(), gene.geneId(), gene.geneSymbol()));
                        }
                    }
                }
                
            }
        }
        var observedHpoIds = hpoIds.stream().map(TermId::of).collect(toUnmodifiableSet());
        PatientData patientData = new ExomiserPatientData(observedHpoIds, Collections.emptySet());

        List<BlendedResult> results = performBoqaBlendedAnalysis(patientData, this.hpo, this.hpoDiseases, candidates);
        return results.stream().map(this::toPriorityResult);
    }


    /**
     * TODO -- Create new BlendedBoqaPriorityResult (Exomiser class). This method transforms BlendedResult (Boqa class) into the BlendedBoqaPriorityResult,
     * because we have all of the data we need for the result (We probably need to add a few fields to the Boqa records)
     * @param bresult
     * @return
     */
    private BoqaPriorityResult toPriorityResult(BlendedResult bresult) {
        return new BoqaPriorityResult();
    }


    private List<BlendedResult> performBoqaBlendedAnalysis(
            PatientData patientData, 
            Ontology hpo,
            HpoDiseases diseases, 
            Set<TargetDisease> targetDiseaseIdSet) {
        System.out.println("Arguments needed to run BoqaBlended - for the real implementation we would call new BoqaBlendedExomiserAnalyser from the BOQA codebase with exactly these arguments");
        return List.of();
        
    }

    @Override
    public PriorityType priorityType() {
        // TODO Make BlendedBoqa priority type
        throw new UnsupportedOperationException("Unimplemented method 'priorityType'");
    }

    /** Taken from the OMIM prioritiser */
      private boolean geneCompatibleWithInheritanceMode(Gene gene, InheritanceMode inheritanceMode, ModeOfInheritance currentMode) {
        /* inheritance unknown (not mentioned in OMIM or not annotated correctly in HPO */
        if (gene.compatibleInheritanceModes().isEmpty() || inheritanceMode == InheritanceMode.UNKNOWN) {
            return true;
        }
        return gene.isCompatibleWith(currentMode) && inheritanceMode.isCompatibleWith(currentMode);
    }
}