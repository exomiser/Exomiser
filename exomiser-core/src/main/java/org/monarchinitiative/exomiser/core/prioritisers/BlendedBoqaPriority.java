package org.monarchinitiative.exomiser.core.prioritisers;

import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
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
import org.p2gx.boqa.core.analysis.BoqaBlendedExomiserAnalyser;
import org.p2gx.boqa.core.diseases.CandidateDisease;
import org.p2gx.boqa.core.diseases.CandidateResult;
import org.p2gx.boqa.core.diseases.DiseaseComponent;
import org.p2gx.boqa.core.diseases.TargetDisease;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;








/**
 * Implements BOQA for multiple genetic diagnosis (Blended diseases)
 * BlendedBoqaPriority
 */
public class BlendedBoqaPriority implements Prioritiser<BoqaPriorityResult> {
    private final PriorityService priorityService;
    private final Ontology hpo;
    private final HpoDiseases hpoDiseases;
    private final static Double GENE_SCORE_THRESHOLD = 0.90;
    private final Map<String, Gene> geneMap;


    public BlendedBoqaPriority(PriorityService priorityService, Ontology hpo, HpoDiseases diseases) {
            this.priorityService = priorityService;
            this.hpo = hpo;
            this.hpoDiseases = diseases;
            geneMap = new HashMap<>();
        }


record BlendedGeneResult(
    List<Gene> genes,
    CandidateResult result
) {

}

    public List<BlendedGeneResult> blend(List<String> hpoIds, List<Gene> genes) {
        List<BlendedGeneResult> blendedResults = new ArrayList<>();
        // 1. Find genes with candidate pathogenic variants
        List<TargetDisease> candidates = new ArrayList<>();
        for (Gene gene: genes) {
            List<Disease> diseases = priorityService.getDiseaseDataAssociatedWithGeneId(gene.entrezGeneId());
            for (Disease d: diseases) {
                InheritanceMode mode = d.inheritanceMode();
                for (ModeOfInheritance moi: mode.toModeOfInheritance()) {
                    if (geneCompatibleWithInheritanceMode(gene, mode, moi)) {
                        GeneScore score = gene.geneScoreForMode(moi);
                        if (score.combinedScore()>GENE_SCORE_THRESHOLD) {
                            candidates.add(new TargetDisease(d.diseaseId(), d.diseaseName(), gene.geneId(), gene.geneSymbol()));
                            this.geneMap.put(gene.geneSymbol(), gene);
                        }
                    }
                }
                
            }
        }
        var observedHpoIds = hpoIds.stream().map(TermId::of).collect(toUnmodifiableSet());
        PatientData patientData = new ExomiserPatientData(observedHpoIds, Collections.emptySet());
        List<CandidateResult> candidateResults = BoqaBlendedExomiserAnalyser.computeBlendedBoqaResults(patientData, hpo, hpoDiseases,  candidates);
        int singeDiseasesReturned = 0;
        for (CandidateResult cresult: candidateResults) {
            switch (cresult) {
                case CandidateResult.Single(DiseaseComponent dc, double score) -> {
                    singeDiseasesReturned++; // We do not show single diseases
                }
                case CandidateResult.Blended(List<DiseaseComponent> components,DiseaseComponent finalDiseaseModel, double score) -> {
                  List<Gene> relevantGenes = new ArrayList<>();
                  List<String> geneSymbols = components.stream()
                    .map(DiseaseComponent::disease)
                    .map(TargetDisease::geneId)
                    .toList();
                    geneSymbols.stream().forEach(gs -> {
                        Gene gene = this.geneMap.get(gs);
                        if (gene != null) {
                            relevantGenes.add(gene);
                        }
                    });
                    BlendedGeneResult bgr = new BlendedGeneResult(relevantGenes, cresult);
                    blendedResults.add(bgr);
                }
            }
        }
        System.out.printf("BOQA returned %d single gene results", singeDiseasesReturned);
        return blendedResults;
    }
   


    @Override
    public Stream<BoqaPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {


        /// Do whatever prioritziation we weant
        List<BoqaPriorityResult> results = List.of();
        /// Also search for Melded results
        List<BlendedGeneResult> blendedResults = blend(hpoIds, genes);
        /// Do something with the blended results, e.g., save them to a class variable so we
        /// can use them for a special thymeleaf template.
        return results.stream();
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


