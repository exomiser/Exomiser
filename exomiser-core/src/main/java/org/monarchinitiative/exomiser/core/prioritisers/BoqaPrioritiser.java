package org.monarchinitiative.exomiser.core.prioritisers;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.p2gx.boqa.core.Counter;
import org.p2gx.boqa.core.DiseaseData;
import org.p2gx.boqa.core.PatientData;
import org.p2gx.boqa.core.algorithm.AlgorithmParameters;
import org.p2gx.boqa.core.analysis.BoqaAnalysisResult;
import org.p2gx.boqa.core.analysis.BoqaPatientAnalyzer;
import org.p2gx.boqa.core.analysis.BoqaResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class BoqaPrioritiser implements Prioritiser<BoqaPriorityResult> {

    private static final Logger logger = LoggerFactory.getLogger(BoqaPrioritiser.class);

    private final PriorityService priorityService;
    private final Counter counter;

    public BoqaPrioritiser(PriorityService priorityService, Counter counter) {
        // TODO: add getCounter(): Counter to Priority Service, then initialise the Counter @Lazy in the exomiser-config
        // or make a @Lazy Counter bean to inject along with the Priority Service in the PriorityFactoryImpl
        // The Counter takes about 1 minute to create, so we only want to do that once and only if we really want to use
        // it. The Counter now takes ~ 300ms to create, but still, it would be best to move it's creation into the config code.
        this.priorityService = priorityService;
        this.counter = counter;
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
        double alpha = 1.0/19077;
        double beta = 0.9;
        AlgorithmParameters params = AlgorithmParameters.create(alpha, beta);
        BoqaAnalysisResult boqaAnalysisResult = BoqaPatientAnalyzer.computeBoqaExomiserResults(patientData, counter, params);
        List<BoqaResult> rescaledBoqaResults = reScaledRawLogExomiserScores(boqaAnalysisResult.boqaResults());
        logger.debug("Top 10 BOQA results:");
        rescaledBoqaResults.stream().sorted(Comparator.comparing(BoqaResult::boqaScore)).limit(10).forEach(b -> logger.debug("BOQA score: {} {} {}", b.counts().diseaseId(), b.boqaScore(), b.counts().diseaseLabel()));
        Map<String, BoqaResult> boqaResultsByDiseaseId = rescaledBoqaResults.stream()
                .collect(toUnmodifiableMap(boqaResult -> boqaResult.counts().diseaseId(), Function.identity()));
        return genes.stream().map(prioritiseGene(boqaResultsByDiseaseId));
    }

    /**
     * Takes a list of BOQA results and transforms the raw BOQA log scores as follows:
     *
     *      boqaExomiserScore_i = (boqaRawLogScore_i + abs(min(boqaRawLogScore))) / (max(boqaRawLogScore) + abs(min(boqaRawLogScore)))
     *
     * @param boqaResults
     * @return reScaledBoqaResults
     */
    private static List<BoqaResult> reScaledRawLogExomiserScores(List<BoqaResult> boqaResults) {

        int numBoqaResults = boqaResults.size();
        List<BoqaResult> rankedBoqaResults = boqaResults.stream().sorted(Comparator.comparing(BoqaResult::boqaScore)).toList();
        List<BoqaResult> rescaledBoqaResults = new ArrayList<>(numBoqaResults);

        List<Double> rawLogBoqaScores = new ArrayList<>(numBoqaResults);
        for (int i = 0; i < numBoqaResults; i++) {
            BoqaResult boqaResult = rankedBoqaResults.get(i);
            double rawLogBoqaScore = boqaResult.boqaScore();
            rawLogBoqaScores.add(rawLogBoqaScore);
        }
        double x = Math.abs(Collections.min(rawLogBoqaScores));
        double y = Collections.max(rawLogBoqaScores) + x;
        for (int i = 0; i < numBoqaResults; i++) {
            BoqaResult boqaResult = rankedBoqaResults.get(i);
            double rawLogBoqaScore = rawLogBoqaScores.get(i);
            double boqaExomiserScore = (rawLogBoqaScore + x) / y;
            rescaledBoqaResults.add(new BoqaResult(boqaResult.counts(), boqaExomiserScore));
        }
        return rescaledBoqaResults;
    }

    /**
     * Rescales the BOQA result scores to adjust their range. The method normalizes the scores so that
     * the highest scoring result is scaled to 1.0, while preserving the relative differences between
     * score values.
     *
     * @return the list of rescaled BOQA results with updated scores
     * @param boqaResults
     */
    private static List<BoqaResult> minMaxScaledBoqaResultScores(List<BoqaResult> boqaResults) {
        // BoqaResult scores are normalised so that they sum to 1 across all results. This leads to tiny, tiny scores.
        // This uses min-max scaling, which doesn't perform well with outliers. It seems like BOQA produces data with
        // extreme outliers, so this needs to use a different scaling method.
        // Min-max scaled score:
        //   scaled_score = (score - min_score) / (max_score - min_score)
        // Equivalent to:
        //   scale_factor = 1 / (max_score - min_score)
        //   scaled_score = (score - min_score) * scale_factor
        double minScore = boqaResults.stream().mapToDouble(BoqaResult::boqaScore).min().orElse(0d);
        double maxScore = boqaResults.stream().mapToDouble(BoqaResult::boqaScore).max().orElse(0d);
        double scaleFactor = 1 / (maxScore - minScore);
        return boqaResults.stream()
                .map(boqaResult -> new BoqaResult(boqaResult.counts(), (boqaResult.boqaScore() - minScore) * scaleFactor))
                .toList();
    }

    // TODO: use softmax?
    private static List<BoqaResult> softMaxScaledBoqaResultScores(List<BoqaResult> boqaResults) {
        // Apply temperature scaling first
        double maxScore = boqaResults.stream()
                .mapToDouble(BoqaResult::boqaScore)
                .max()
                .orElse(0.0);

        double total = boqaResults.stream()
                .mapToDouble(b -> Math.exp(b.boqaScore() - maxScore))
                .sum();
        double scaleFactor = 1 / total;

//        return (Math.exp(input) / total) == (Math.expt(score) * ( 1 / total))
        return boqaResults.stream()
                .map(boqaResult -> new BoqaResult(boqaResult.counts(), 0.5))
                .toList();
    }

    // 1 - ( rank / numTotalDiseases) // rank-scaled score
    private static List<BoqaResult> rankScaledBoqaResultScores(List<BoqaResult> boqaResults) {
        int numBoqaResults = boqaResults.size();
        List<BoqaResult> rankedBoqaResults = boqaResults.stream().sorted(Comparator.comparing(BoqaResult::boqaScore)).toList();
        List<BoqaResult> rankScaledResults = new ArrayList<>(numBoqaResults);
        for (int i = 0; i < numBoqaResults; i++) {
            BoqaResult boqaResult = rankedBoqaResults.get(i);
            rankScaledResults.add(new BoqaResult(boqaResult.counts(), 1.0 - (i / (double) numBoqaResults)));
        }
        return rankScaledResults;
    }


    /**
     * If the gene is not contained in the database, we return an empty
     * but initialized RelevanceScore object. Otherwise, we retrieve a list of
     * all OMIM and Orphanet diseases associated with the entrez Gene.
     *
     **/
    private Function<Gene, BoqaPriorityResult> prioritiseGene(Map<String, BoqaResult> boqaResultsByDiseaseId) {
        return gene -> {
            List<Disease> diseases = priorityService.getDiseaseDataAssociatedWithGeneId(gene.entrezGeneId());

            // n.b. BOQA uses the HPOA phenotypes to generate a score and does not use any gene or MOI specific information,
            // so the results will be identical for all diseases with the same OMIM id, irrespective of their gene/MOI association
            // An Exomiser Disease is a disease-gene-moi concept, but again these will all have the same phenotypic
            // features extracted from the HPOA
            // new disease table: id, label, source (e.g. HPOA, DDD2G), geneId, geneSymbol, moi, validity (ClinGen/GENCC), triplosensitivity, haploinsufficient, observedPhenotypes, excludedPhenotypes
            Map<Disease, BoqaResult> boqaResults = diseases.stream()
                    .filter(disease -> disease.id().startsWith("OMIM"))
                    .collect(toUnmodifiableMap(Function.identity(), disease -> boqaResultsByDiseaseId.get(disease.diseaseId()), (first, second) -> first));

            double score = boqaResults.values().stream().mapToDouble(BoqaResult::boqaScore).max().orElse(0d);
            BoqaPriorityResult boqaPriorityResult = new BoqaPriorityResult(gene.entrezGeneId(), gene.geneSymbol(), score, boqaResults);
            logger.trace("BOQA score for {} is {} {}", gene.geneSymbol(), score, boqaResults);
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
        private final Map<String, Set<String>> diseaseGeneIds;
        private final Map<String, Set<String>> diseaseGeneSymbols;

        // TODO: check if there are multiple D2G mappings in the HPO disease2Gene.tsv
        public ExomiserDiseaseData(List<Disease> diseases) {
            diseaseIdToLabel = diseases.stream()
                    .filter(disease -> disease.diseaseId().startsWith("OMIM"))
                    .collect(toMap(Disease::diseaseId, Disease::diseaseName, (first, second) -> first));

            diseaseObservedPhenotypes = diseases.stream()
                    .filter(disease -> disease.diseaseId().startsWith("OMIM"))
                    .collect(toMap(Disease::diseaseId, disease -> Set.copyOf(disease.phenotypeIds()), (first, second) -> first));

            diseaseGeneIds = diseases.stream()
                    .filter(disease -> disease.diseaseId().startsWith("OMIM"))
                    .collect(groupingBy(Disease::diseaseId, mapping(disease -> Integer.toString(disease.associatedGeneId()), toUnmodifiableSet())));

            diseaseGeneSymbols = diseases.stream()
                    .filter(disease -> disease.diseaseId().startsWith("OMIM"))
                    .collect(groupingBy(Disease::diseaseId, mapping(Disease::associateGeneSymbol, toUnmodifiableSet())));

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
        public Set<String> getDiseaseGeneIds(String diseaseId) {
            return diseaseGeneIds.getOrDefault(diseaseId, Set.of());
        }

        @Override
        public Set<String> getDiseaseGeneSymbols(String diseaseId) {
            return diseaseGeneSymbols.getOrDefault(diseaseId, Set.of());
        }

        @Override
        public Map<String, String> getIdToLabel() {
            return diseaseIdToLabel;
        }
    }

}
