package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.phenol.annotations.formats.hpo.HpoDiseases;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.p2gx.boqa.core.Counter;
import org.p2gx.boqa.core.DiseaseData;
import org.p2gx.boqa.core.PatientData;
import org.p2gx.boqa.core.algorithm.AlgorithmParameters;
import org.p2gx.boqa.core.algorithm.OntologyTraverser;
import org.p2gx.boqa.core.algorithm.SetCounter;
import org.p2gx.boqa.core.analysis.BoqaPatientAnalyzer;
import org.p2gx.boqa.core.analysis.BoqaResult;
import org.p2gx.boqa.core.analysis.CandidateResult;
import org.p2gx.boqa.core.analysis.PatientAnalysisResult;
import org.p2gx.boqa.core.diseases.CandidateDisease;
import org.p2gx.boqa.core.diseases.TargetDisease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class BoqaPrioritiser implements Prioritiser<BoqaPriorityResult> {

    private static final Logger logger = LoggerFactory.getLogger(BoqaPrioritiser.class);

    private final PriorityService priorityService;
    private final Ontology hpo;
    private final HpoDiseases diseases;


    public BoqaPrioritiser(PriorityService priorityService, Ontology hpo, HpoDiseases diseases) {
        // TODO: add getCounter(): Counter to Priority Service, then initialise the Counter @Lazy in the exomiser-config
        // or make a @Lazy Counter bean to inject along with the Priority Service in the PriorityFactoryImpl
        // The Counter takes about 1 minute to create, so we only want to do that once and only if we really want to use
        // it. The Counter now takes ~ 300ms to create, but still, it would be best to move it's creation into the config code.
        this.priorityService = priorityService;
        this.hpo = hpo;
        this.diseases = diseases;
    }

    @Override
    public PriorityType priorityType() {
        return PriorityType.BOQA_PRIORITY;
    }

    @Override
    public Stream<BoqaPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {
        logger.info("Running BOQA prioritiser...");
        var observedHpoIds = hpoIds.stream().map(TermId::of).collect(toUnmodifiableSet());
        PatientData patientData = new ExomiserPatientData(observedHpoIds, Collections.emptySet());
        AlgorithmParameters params = AlgorithmParameters.defaultParams();
        OntologyTraverser ontologyTraverser = new OntologyTraverser(hpo);
        Counter counter = new SetCounter(ontologyTraverser, patientData.getObservedTerms());
        List<TargetDisease.PhenotypeOnly> targetDiseaseList = diseases.stream()
                .map(d -> new TargetDisease.PhenotypeOnly(d.id().toString(), d.diseaseName(),
                        d.annotationTermIds().collect(Collectors.toSet())
                )).toList();
        List<CandidateDisease> diseaseCandidateList = CandidateDisease.createSingleDiseaseCandidates(targetDiseaseList);

        int limit = Integer.MAX_VALUE;
        List<CandidateResult> candidateResults = BoqaPatientAnalyzer.computeBoqaResults(
                patientData, counter, limit, params,  diseaseCandidateList);
//        PatientAnalysisResult patientAnalysisResult = new PatientAnalysisResult(
//                ppkt, candidateResults.stream().limit(limit).toList());
//
//        BoqaAnalysisResult boqaAnalysisResult = BoqaPatientAnalyzer.computeBoqaResultsRawLog(patientData, counter, params);
//        List<BoqaResult> rescaledBoqaResults = reScaledRawLogBoqaExomiserScores(boqaAnalysisResult.boqaResults());
//        logger.debug("Top 10 BOQA results:");
//        rescaledBoqaResults.stream().sorted(Comparator.comparing(BoqaResult::boqaScore)).limit(10).forEach(b -> logger.debug("BOQA score: {} {} {}", b.counts().diseaseId(), b.boqaScore(), b.counts().diseaseLabel()));
          Map<String, CandidateResult> boqaResultsByDiseaseId = candidateResults.stream()
                .collect(toUnmodifiableMap(c -> c.finalDiseases().stream().map(TargetDisease::diseaseId).toString(), Function.identity()));
        return genes.stream().map(prioritiseGene(boqaResultsByDiseaseId));
    }


    /**
     * Transforms a list of BOQA results by rescaling their raw log scores into the range [0, 1].
     *
     * <p>The transformation is done as follows:</p>
     * <pre>
     * boqaExomiserScore_i =
     *     (boqaRawLogScore_i + abs(min(boqaRawLogScore)))
     *     / (max(boqaRawLogScore) + abs(min(boqaRawLogScore)))
     * </pre>
     *
     * <p>This ensures that the minimum raw score maps to 0, and the maximum maps to 1.</p>
     *
     * @param boqaResults the list of BOQA results to rescale
     * @return a list of BOQA results with rescaled scores
     */
    private static List<BoqaResult> reScaledRawLogBoqaExomiserScores(List<BoqaResult> boqaResults) {

        // Extract raw BOQA log scores
        List<Double> rawLogBoqaScores =
                boqaResults.stream()
                        .map(BoqaResult::boqaScore)
                        .toList();

        // Compute offset and normalization factor
        double offset = Math.abs(Collections.min(rawLogBoqaScores));
        double scale = Collections.max(rawLogBoqaScores) + offset;

        // Rescale
        return boqaResults.stream()
                .map(br -> {
                    double boqaExomiserScore = (br.boqaScore() + offset) / scale;
                    return new BoqaResult(br.counts(), boqaExomiserScore);
                })
                .toList();
    }

    /**
     * If the gene is not contained in the database, we return an empty
     * but initialized RelevanceScore object. Otherwise, we retrieve a list of
     * all OMIM and Orphanet diseases associated with the entrez Gene.
     *
     **/
    private Function<Gene, BoqaPriorityResult> prioritiseGene(Map<String, CandidateResult> boqaResultsByDiseaseId) {
        return gene -> {
            List<Disease> diseases = priorityService.getDiseaseDataAssociatedWithGeneId(gene.entrezGeneId());

            // n.b. BOQA uses the HPOA phenotypes to generate a score and does not use any gene or MOI specific information,
            // so the results will be identical for all diseases with the same OMIM id, irrespective of their gene/MOI association
            // An Exomiser Disease is a disease-gene-moi concept, but again these will all have the same phenotypic
            // features extracted from the HPOA
            // new disease table: id, label, source (e.g. HPOA, DDD2G), geneId, geneSymbol, moi, validity (ClinGen/GENCC), triplosensitivity, haploinsufficient, observedPhenotypes, excludedPhenotypes
            Map<Disease, CandidateResult> map = new HashMap<>();
            for (Disease disease : diseases) {
                if (disease.id().startsWith("OMIM")) {
                    CandidateResult c = boqaResultsByDiseaseId.get(disease.diseaseId());
                    if (c != null) {
                        map.putIfAbsent(disease, c);
                    }
                }
            }
            Map<Disease, CandidateResult> res = Collections.unmodifiableMap(map);

            double score = res.values().stream().mapToDouble(CandidateResult::score).max().orElse(0d);
            BoqaPriorityResult boqaPriorityResult = new BoqaPriorityResult(gene.entrezGeneId(), gene.geneSymbol(), score, res);
            logger.trace("BOQA score for {} is {} {}", gene.geneSymbol(), score, res);
            return boqaPriorityResult;
        };
    }

    @Override
    public String toString() {
        return "BoqaPrioritiser{}";
    }

    record ExomiserPatientData(Set<TermId> observedTerms,  Set<TermId> excludedTerms) implements PatientData {

        @Override
        public String getID() {
            return "";
        }

        @Override
        public Set<TermId> getObservedTerms() {
            return observedTerms;
        }

        @Override
        public Set<TermId> getExcludedTerms() {
            return excludedTerms;
        }

    }

    private static class ExomiserDiseaseData implements DiseaseData {

        private final Map<String, String> diseaseIdToLabel;
        private final Map<String, Set<String>> diseaseObservedPhenotypes;

        // TODO: check if there are multiple D2G mappings in the HPO disease2Gene.tsv
        public ExomiserDiseaseData(List<Disease> diseases) {
            diseaseIdToLabel = diseases.stream()
                    .filter(disease -> disease.diseaseId().startsWith("OMIM"))
                    .collect(toMap(Disease::diseaseId, Disease::diseaseName, (first, second) -> first));

            diseaseObservedPhenotypes = diseases.stream()
                    .filter(disease -> disease.diseaseId().startsWith("OMIM"))
                    .collect(toMap(Disease::diseaseId, disease -> Set.copyOf(disease.phenotypeIds()), (first, second) -> first));

//            diseaseGeneIds = diseases.stream()
//                    .filter(disease -> disease.diseaseId().startsWith("OMIM"))
//                    .collect(groupingBy(Disease::diseaseId, mapping(disease -> Integer.toString(disease.associatedGeneId()), toUnmodifiableSet())));
//
//            diseaseGeneSymbols = diseases.stream()
//                    .filter(disease -> disease.diseaseId().startsWith("OMIM"))
//                    .collect(groupingBy(Disease::diseaseId, mapping(Disease::associateGeneSymbol, toUnmodifiableSet())));

        }

        @Override
        public int size() {
            return diseaseIdToLabel.size();
        }

        @Override
        public Set<String> getDiseaseIds() {
            return diseaseIdToLabel.keySet();
        }

        @Override
        public Set<String> getObservedDiseaseFeatures(String diseaseId) {
            return diseaseObservedPhenotypes.getOrDefault(diseaseId, Set.of());
        }

        @Override
        public Set<String> getExcludedDiseaseFeatures(String diseaseId) {
            return Set.of();
        }


        @Override
        public Map<String, String> getIdToLabel() {
            return diseaseIdToLabel;
        }
    }
}
