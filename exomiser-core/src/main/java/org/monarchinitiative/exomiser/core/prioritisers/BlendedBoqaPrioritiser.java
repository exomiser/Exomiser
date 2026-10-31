package org.monarchinitiative.exomiser.core.prioritisers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.p2gx.boqa.core.PatientData;
import org.p2gx.boqa.core.analysis.BoqaBlendedExomiserAnalyser;
import org.p2gx.boqa.core.analysis.CandidateResult;
import org.p2gx.boqa.core.diseases.TargetDisease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;


/**
 * Implements BOQA for multiple genetic diagnosis (Blended diseases)
 * BlendedBoqaPriority. This is a demonstration of how to connect to the BOQA
 * library. Given our decision not to add the blended diseases to the "main" output
 * of Exomiser, we could move the code that is shown here to some other place.
 */
public class BlendedBoqaPrioritiser implements Prioritiser<BlendedBoqaPriorityResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlendedBoqaPrioritiser.class);
    private final PriorityService priorityService;
    private final Ontology hpo;
    private final HpoDiseases hpoDiseases;
    private final static Double GENE_SCORE_THRESHOLD = 0.90;
    /** A map of the genes related to our candidate diseases for blended analysis. The Key is the gene symbol. */
    private final Map<String, Gene> geneMap;


    public BlendedBoqaPrioritiser(PriorityService priorityService, Ontology hpo, HpoDiseases diseases) {
        this.priorityService = priorityService;
        this.hpo = hpo;
        this.hpoDiseases = diseases;
        geneMap = new HashMap<>();
    }

   /**
     * Filters and maps a list of genes into a list of valid {@link TargetDisease} candidates
     * based on inheritance modes and score thresholds.
     * <p>
     * This method processes the input genes through a stream pipeline that:
     * <ul>
     *   <li>Retrieves associated diseases for each gene's Entrez ID.</li>
     *   <li>Inspects all applicable modes of inheritance.</li>
     *   <li>Filters for combinations compatible with the gene.</li>
     *   <li>Ensures the combined gene score exceeds the defined {@link #GENE_SCORE_THRESHOLD}.</li>
     *   <li>Caches the gene mapping as a side-effect into {@link #geneMap}.</li>
     *   <li>Collects and returns the resulting target diseases.</li>
     * </ul>
     *
     * @param genes the list of {@link Gene} objects to evaluate
     * @return a list of qualified {@link TargetDisease} candidates
     */
    private List<TargetDisease.PhenotypeAndGene> getCandidateDiseases(List<Gene> genes) {
        return genes.stream()
            .flatMap(gene -> priorityService.getDiseaseDataAssociatedWithGeneId(gene.entrezGeneId()).stream()
                .flatMap(d -> d.inheritanceMode().toModeOfInheritance().stream()
                    .filter(moi -> geneCompatibleWithInheritanceMode(gene, d.inheritanceMode(), moi))
                    .filter(moi -> gene.geneScoreForMode(moi).combinedScore() > GENE_SCORE_THRESHOLD)
                    .peek(moi -> this.geneMap.put(gene.geneSymbol(), gene))
                    .map(moi -> new TargetDisease.PhenotypeAndGene(
                            d.diseaseId(), d.diseaseName(), gene.geneId(), gene.geneSymbol(),
                            d.phenotypeIds().stream().map(TermId::of).collect(Collectors.toSet())))
                )
            )
            .toList();
    }

    /**
     * Evaluates a list of genes against patient HPO phenotypes to identify blended 
     * multi-gene disease combinations using BOQA.
     * <p>
     * This method performs the following steps:
     * <ul>
     *   <li>Retrieves candidate target diseases for the provided genes.</li>
     *   <li>Initializes patient phenotype data from the supplied HPO term IDs.</li>
     *   <li>Executes the BOQA blended analysis to compute candidate results.</li>
     *   <li>Filters and processes the results, ignoring single-gene outcomes while 
     *       mapping multi-gene (blended) outcomes back to their respective {@link Gene} objects.</li>
     * </ul>
     *
     * @param hpoIds a list of strings representing the patient's observed HPO phenotype IDs
     * @param genes  a list of {@link Gene} objects to be evaluated as candidates
     * @return a list of {@link BlendedBoqaPriorityResult} containing the relevant genes and their BOQA results
     */
    @Override
    public Stream<BlendedBoqaPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {
        List<BlendedBoqaPriorityResult> blendedResults = new ArrayList<>();
        // 1. Find genes with candidate pathogenic variants
        List<TargetDisease.PhenotypeAndGene> targetDiseaseList = getCandidateDiseases(genes);

        BoqaBlendedExomiserAnalyser bbqAnalyser = new BoqaBlendedExomiserAnalyser(hpo, hpoDiseases);
        PatientData patientData = PatientData.fromObservedHpoTermList(hpoIds);
        List<CandidateResult> candidateResults = bbqAnalyser.computeBlendedBoqaResults(
                patientData, targetDiseaseList);
        Map<Class<? extends CandidateResult>, Long> counts = candidateResults.stream()
                .collect(Collectors.groupingBy(CandidateResult::getClass, Collectors.counting()));
        counts.forEach((type, count) ->
                LOGGER.info("Number of {}: {}", type.getSimpleName(), count));
        candidateResults.forEach(c -> {
            List<Gene> diseaseGenes = c.finalDiseases()
                    .stream()
                    .map(TargetDisease::diseaseId)
                    .map(geneMap::get).toList();
            blendedResults.add(new BlendedBoqaPriorityResult(diseaseGenes, c));
        });

        return blendedResults.stream();
    }


    @Override
    public PriorityType priorityType() {
        return PriorityType.BLENDED_BOQA_PRIORITY;
    }

      /**
     * Copied and modified to use streams from BoqaPrioritiser. 
     * We should probably refactor to leave only this prioritiser
     * and make the Blended part an option
     **/
//    @SuppressWarnings("null")
//    private Function<Gene, BoqaPriorityResult> prioritiseGene(Map<String, BoqaResult> boqaResultsByDiseaseId) {
//        return gene -> {
//            List<Disease> diseases = priorityService.getDiseaseDataAssociatedWithGeneId(gene.entrezGeneId());
//            // Apart from very few exceptions, all diseases witth an OMIM id have just one associated gene
//            Map<Disease, BoqaResult> map = diseases.stream()
//                .filter(disease -> disease.id().startsWith("OMIM"))
//                .filter(disease -> boqaResultsByDiseaseId.containsKey(disease.diseaseId()))
//                .collect(Collectors.toMap(
//                    disease -> disease,
//                    disease -> boqaResultsByDiseaseId.get(disease.diseaseId())));
//                Map<Disease, BoqaResult> boqaResults = Collections.unmodifiableMap(map);
//            double score = boqaResults.values().stream()
//                .mapToDouble(BoqaResult::boqaScore)
//                .max()
//                .orElse(0d);
//            BoqaPriorityResult boqaPriorityResult = new BoqaPriorityResult(gene.entrezGeneId(), gene.geneSymbol(),
//                    score, boqaResults);
//            LOGGER.trace("BOQA score for {} is {} {}", gene.geneSymbol(), score, boqaResults);
//            return boqaPriorityResult;
//        };
//    }


    /** Taken from the OMIM prioritiser */
      private boolean geneCompatibleWithInheritanceMode(Gene gene, InheritanceMode inheritanceMode, ModeOfInheritance currentMode) {
        /* inheritance unknown (not mentioned in OMIM or not annotated correctly in HPO */
        if (gene.compatibleInheritanceModes().isEmpty() || inheritanceMode == InheritanceMode.UNKNOWN) {
            return true;
        }
        return gene.isCompatibleWith(currentMode) && inheritanceMode.isCompatibleWith(currentMode);
    }
}


