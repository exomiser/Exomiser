package org.monarchinitiative.exomiser.core.prioritisers;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.p2gx.boqa.core.Counter;
import org.p2gx.boqa.core.DiseaseData;
import org.p2gx.boqa.core.PatientData;
import org.p2gx.boqa.core.algorithm.AlgorithmParameters;
import org.p2gx.boqa.core.algorithm.BoqaSetCounter;
import org.p2gx.boqa.core.analysis.BoqaAnalysisResult;
import org.p2gx.boqa.core.analysis.BoqaBlendedExomiserAnalyser;
import org.p2gx.boqa.core.analysis.BoqaPatientAnalyzer;
import org.p2gx.boqa.core.analysis.BoqaResult;
import org.p2gx.boqa.core.diseases.CandidateResult;
import org.p2gx.boqa.core.diseases.DiseaseComponent;
import org.p2gx.boqa.core.diseases.DiseaseDataPhenolIngest;
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
public class BlendedBoqaPrioriser implements Prioritiser<BoqaPriorityResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlendedBoqaPrioriser.class);
    private final PriorityService priorityService;
    private final Ontology hpo;
    private final HpoDiseases hpoDiseases;
    private final Counter counter;
    private final static Double GENE_SCORE_THRESHOLD = 0.90;
    /** A map of the genes related to our candidate diseases for blended analysis. The Key is the gene symbol. */
    private final Map<String, Gene> geneMap;


    public BlendedBoqaPrioriser(PriorityService priorityService, Ontology hpo, HpoDiseases diseases) {
        this.priorityService = priorityService;
        this.hpo = hpo;
        this.hpoDiseases = diseases;
        geneMap = new HashMap<>();
        DiseaseData diseaseData = DiseaseDataPhenolIngest.of(hpo, diseases);
        this.counter = new BoqaSetCounter(diseaseData, hpo);
    }

    /**
     * We return one of thiese objects for each blended diseases that is better than its best component single disease. 
     * The genes list will allow us to show some variants etc., and the CandidateResult has all of the details coming
     * from BOQA.
     * @param genes
     * @param result
     */
    record BlendedGeneResult(
        List<Gene> genes,
        CandidateResult result
    ) {

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
    private List<TargetDisease> getCandidateDiseases(List<Gene> genes) {
        return genes.stream()
            .flatMap(gene -> priorityService.getDiseaseDataAssociatedWithGeneId(gene.entrezGeneId()).stream()
                .flatMap(d -> d.inheritanceMode().toModeOfInheritance().stream()
                    .filter(moi -> geneCompatibleWithInheritanceMode(gene, d.inheritanceMode(), moi))
                    .filter(moi -> gene.geneScoreForMode(moi).combinedScore() > GENE_SCORE_THRESHOLD)
                    .peek(moi -> this.geneMap.put(gene.geneSymbol(), gene))
                    .map(moi -> new TargetDisease(d.diseaseId(), d.diseaseName(), gene.geneId(), gene.geneSymbol()))
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
     * @return a list of {@link BlendedGeneResult} containing the relevant genes and their BOQA results
     */
    public List<BlendedGeneResult> blend(List<String> hpoIds, List<Gene> genes) {
        List<BlendedGeneResult> blendedResults = new ArrayList<>();
        // 1. Find genes with candidate pathogenic variants
        List<TargetDisease> candidates = getCandidateDiseases(genes);
        BoqaBlendedExomiserAnalyser bbqAnalyser = new BoqaBlendedExomiserAnalyser(hpo, hpoDiseases);
        PatientData patientData = PatientData.fromObservedHpoTermList(hpoIds);
        List<CandidateResult> candidateResults = bbqAnalyser.computeBlendedBoqaResults(patientData, candidates);
        int singeDiseasesReturned = 0;
        for (CandidateResult cresult: candidateResults) {
            switch (cresult) {
                case CandidateResult.Single(DiseaseComponent dc) -> {
                    singeDiseasesReturned++; // We do not show single diseases
                }
                case CandidateResult.Blended(List<DiseaseComponent> components,DiseaseComponent finalDiseaseModel) -> {
                    List<Gene> relevantGenes = components.stream()
                        .map(dc -> this.geneMap.get(dc.disease().geneSymbol()))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                    BlendedGeneResult bgr = new BlendedGeneResult(relevantGenes, cresult);
                    blendedResults.add(bgr);
                }
            }
        }
        System.out.printf("BOQA returned %d single gene results", singeDiseasesReturned);
        return blendedResults;
    }
   

    /**
     * This method is implementing the "normal" BOQA algorithm. In principle, we could use
     * and prioritizer, and the Blended BOQA would be an add-on.
     */
    @SuppressWarnings("null")
    @Override
    public Stream<BoqaPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {
        LOGGER.info("Running Blended BOQA prioritiser...");
        PatientData patientData = PatientData.fromObservedHpoTermList(hpoIds);
        AlgorithmParameters params = AlgorithmParameters.defaultParams();
       // BoqaAnalysisResult boqaAnalysisResult = BoqaPatientAnalyzer.computeBoqaResultsRescaled(patientData, counter, params);
        List<BoqaResult> rescaledBoqaResults = BoqaPatientAnalyzer.computeBoqaResultsRescaled(patientData, counter, params);
        // reScaledRawLogBoqaExomiserScores(boqaAnalysisResult.boqaResults());
        LOGGER.debug("Top 10 BOQA results:");
        rescaledBoqaResults.stream().sorted(Comparator.comparing(BoqaResult::boqaScore)).limit(10).forEach(b -> LOGGER.debug("BOQA score: {} {} {}", b.counts().diseaseId(), b.boqaScore(), b.counts().diseaseLabel()));
        Map<String, BoqaResult> boqaResultsByDiseaseId = rescaledBoqaResults.stream()
                .collect(toUnmodifiableMap(boqaResult -> boqaResult.counts().diseaseId(), Function.identity()));
        return genes.stream().map(prioritiseGene(boqaResultsByDiseaseId));
    }

      /**
     * Copied and modified to use streams from BoqaPrioritiser. 
     * We should probably refactor to leave only this prioritiser
     * and make the Blended part an option
     **/
    @SuppressWarnings("null")
    private Function<Gene, BoqaPriorityResult> prioritiseGene(Map<String, BoqaResult> boqaResultsByDiseaseId) {
        return gene -> {
            List<Disease> diseases = priorityService.getDiseaseDataAssociatedWithGeneId(gene.entrezGeneId());
            // Apart from very few exceptions, all diseases witth an OMIM id have just one associated gene
            Map<Disease, BoqaResult> map = diseases.stream()
                .filter(disease -> disease.id().startsWith("OMIM"))
                .filter(disease -> boqaResultsByDiseaseId.containsKey(disease.diseaseId()))
                .collect(Collectors.toMap(
                    disease -> disease,
                    disease -> boqaResultsByDiseaseId.get(disease.diseaseId())));
                Map<Disease, BoqaResult> boqaResults = Collections.unmodifiableMap(map);
            double score = boqaResults.values().stream()
                .mapToDouble(BoqaResult::boqaScore)
                .max()
                .orElse(0d);
            BoqaPriorityResult boqaPriorityResult = new BoqaPriorityResult(gene.entrezGeneId(), gene.geneSymbol(),
                    score, boqaResults);
            LOGGER.trace("BOQA score for {} is {} {}", gene.geneSymbol(), score, boqaResults);
            return boqaPriorityResult;
        };
    }



    @Override
    public PriorityType priorityType() {
        return PriorityType.BOQA_PRIORITY;
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


