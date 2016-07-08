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

import com.google.common.collect.ImmutableMap;
import de.charite.compbio.exomiser.core.model.*;
import de.charite.compbio.exomiser.core.prioritisers.util.OrganismPhenotypeMatches;
import de.charite.compbio.exomiser.core.prioritisers.util.PriorityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * Filter variants according to the phenotypic similarity of the specified
 * clinical phenotypes to mouse models disrupting the same gene. We use MGI
 * annotated phenotype data and the Phenodigm/OWLSim algorithm. The filter is
 * implemented with an SQL query.
 * <P>
 * This class prioritizes the genes that have survived the initial VCF filter
 * (i.e., it is use on genes for which we have found rare, potentially
 * pathogenic variants).
 * <P>
 *
 * @author Damian Smedley
 * @author Jules Jacobsen
 * @version 0.05 (April 6, 2013)
 */
public class PhivePriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(PhivePriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.PHIVE_PRIORITY;
    private static final float NO_PHENOTYPE_HIT_SCORE = 0.1f;
    static final float NO_MOUSE_MODEL_SCORE = 0.6f;

    private final List<String> hpoIds;
    private final PriorityService priorityService;

    public PhivePriority(List<String> hpoIds, PriorityService priorityService) {
        this.hpoIds = hpoIds;
        this.priorityService = priorityService;
        //This can be moved into a report section - FilterReport should probably turn into an AnalysisStepReport
        //Then can remove getMessages from the interface. 
//        messages.add(String.format("<a href = \"http://www.sanger.ac.uk/resources/databases/phenodigm\">Mouse PhenoDigm Filter</a>"));
//        if (disease != null) {
//            String url = String.format("http://omim.org/%s", disease);
//            if (disease.contains("ORPHANET")) {
//                String diseaseId = disease.split(":")[1];
//                url = String.format("http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=%s", diseaseId);
//            }
//            String anchor = String.format("Mouse phenotypes for candidate genes were compared to <a href=\"%s\">%s</a>\n", url, disease);
//            this.messages.add(String.format("Mouse PhenoDigm Filter for OMIM"));
//            messages.add(anchor);
//        } else {
//            String anchor = String.format("Mouse phenotypes for candidate genes were compared to user-supplied clinical phenotypes");
//            messages.add(anchor);
//        }
    }

    /**
     * Flag to output results of filtering against PhenoDigm data.
     */
    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    @Override
    public void prioritizeGenes(List<Gene> genes) {

        List<PhenotypeTerm> hpoPhenotypeTerms = priorityService.makePhenotypeTermsFromHpoIds(hpoIds);

        OrganismPhenotypeMatches humanMousePhenotypeMatches = getMatchingPhenotypesForOrganism(hpoPhenotypeTerms, Organism.MOUSE);
        logOrganismPhenotypeMatches(humanMousePhenotypeMatches);

        List<Model> mouseModels = priorityService.getModelsForOrganism(Organism.MOUSE);

        List<Model> scoredModels = scoreModels(humanMousePhenotypeMatches, mouseModels);

        Map<Integer, List<Model>> geneMouseModels = scoredModels.parallelStream()
                .sorted(Comparator.comparingDouble(Model::getScore).reversed())
                .collect(Collectors.groupingBy(Model::getEntrezGeneId));

        for (Gene gene : genes) {
            List<Model> geneModels = geneMouseModels.getOrDefault(gene.getEntrezGeneID(), Collections.emptyList());
            PhivePriorityResult phiveScore = geneModels.stream()
                    .findFirst()
                    .map(makeModelPhivePriorityResult())
                    .orElse(new PhivePriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), NO_MOUSE_MODEL_SCORE, null, null));

            gene.addPriorityResult(phiveScore);
        }
//        messages.add(String.format("Data analysed for %d genes using Mouse PhenoDigm", genes.size()));
    }

    private Function<Model, PhivePriorityResult> makeModelPhivePriorityResult() {
        return model -> {
            GeneModel bestModelForGene = (GeneModel) model;
            return new PhivePriorityResult(bestModelForGene.getEntrezGeneId(), bestModelForGene.getHumanGeneSymbol(), bestModelForGene.getScore(), bestModelForGene.getModelGeneId(), bestModelForGene.getModelGeneSymbol());
        };
    }

    private List<Model> scoreModels(OrganismPhenotypeMatches humanMousePhenotypeMatches, List<Model> mouseModels) {
        //TODO: this is common to both phive and hiPhive and loks like the responsibility should lie with the OrganismPhenotypeMatches
        //or a CrossSpeciesModelPhenotypeScorer or something
        Set<String> hpIdsWithPhenotypeMatch = humanMousePhenotypeMatches.getBestPhenotypeMatches().stream().map(PhenotypeMatch::getQueryPhenotypeId).collect(toCollection(TreeSet::new));
//        logger.info("hpIds with phenotype match={}", hpIdsWithPhenotypeMatch);

        Map<String, PhenotypeMatch> mappedTerms = humanMousePhenotypeMatches.getCompoundKeyIndexedPhenotypeMatches();
        Set<String> matchedPhenotypeIdsForSpecies = mappedTerms.values().stream().map(PhenotypeMatch::getMatchPhenotypeId).collect(toCollection(TreeSet::new));

        for (Model model : mouseModels) {

            List<String> matchedPhenotypeIdsForModel = model.getPhenotypeIds().stream()
                    .filter(matchedPhenotypeIdsForSpecies::contains)
                    .collect(toList());

            double maxModelMatchScore = 0f;
            double sumModelBestMatchScores = 0f;

            for (String hpId : hpIdsWithPhenotypeMatch) {
                double bestScore = 0f;
                for (String mpId : matchedPhenotypeIdsForModel) {
                    String hashKey = hpId + mpId;
                    if (mappedTerms.containsKey(hashKey)) {
                        PhenotypeMatch match = mappedTerms.get(hashKey);
                        double score = match.getScore();
                        // identify best match
                        bestScore = Math.max(bestScore, score);
                    }
                }
                if (bestScore != 0) {
                    sumModelBestMatchScores += bestScore;
                    maxModelMatchScore = Math.max(bestScore, maxModelMatchScore);
                }
            }
            // Reciprocal hits
            for (String mpId : matchedPhenotypeIdsForModel) {
                double bestScore = 0f;
                for (String hpId : hpIdsWithPhenotypeMatch) {
                    String hashKey = hpId + mpId;
                    if (mappedTerms.containsKey(hashKey)) {
                        PhenotypeMatch match = mappedTerms.get(hashKey);
                        double score = match.getScore();
                        // identify best match
                        bestScore = Math.max(bestScore, score);
                    }
                }
                if (bestScore != 0) {
                    sumModelBestMatchScores += bestScore;
                    maxModelMatchScore = Math.max(bestScore, maxModelMatchScore);
                }
            }

            //in hiPhive we now use humanMousePhenotypeMatches.getQueryTerms().size()
//            int rowColumnCount = humanMousePhenotypeMatches.getQueryTerms().size() + matchedPhenotypeIdsForModel.size();
            int rowColumnCount = hpIdsWithPhenotypeMatch.size() + matchedPhenotypeIdsForModel.size();
            double modelScore = calculateModelScore(humanMousePhenotypeMatches, rowColumnCount, maxModelMatchScore, sumModelBestMatchScores);
            model.setScore(modelScore);
//            logger.info("{} model score = {}", modelId, modelScore);
        }

        return mouseModels;
    }

    private double calculateModelScore(OrganismPhenotypeMatches humanMousePhenotypeMatches, int rowColumnCount, double maxModelMatchScore, double sumModelBestMatchScores) {
        // calculate combined score
        double bestMaxScore = humanMousePhenotypeMatches.getBestMatchScore();
        double bestAvgScore = humanMousePhenotypeMatches.getBestAverageScore();

        if (sumModelBestMatchScores != 0) {
            double avgBestHitRowsColumnsScore = sumModelBestMatchScores / rowColumnCount;
            double combinedScore = 50 * (maxModelMatchScore / bestMaxScore + avgBestHitRowsColumnsScore / bestAvgScore);
            if (combinedScore > 100) {
                combinedScore = 100;
            }
            return combinedScore / 100;
        }
        return 0;
    }

    //TODO: turn this into a CrossSpeciesPhenotypeMatcher? - THIS IS CURRENTLY COPIED FROM HIPHIVEPRIORITY
    private OrganismPhenotypeMatches getMatchingPhenotypesForOrganism(List<PhenotypeTerm> queryHpoPhenotypes, Organism organism) {
        logger.info("Fetching HUMAN-{} phenotype matches...", organism);
        Map<PhenotypeTerm, Set<PhenotypeMatch>> speciesPhenotypeMatches = new LinkedHashMap<>();
        for (PhenotypeTerm hpoTerm : queryHpoPhenotypes) {
            Set<PhenotypeMatch> termMatches = priorityService.getSpeciesMatchesForHpoTerm(hpoTerm, organism);
            speciesPhenotypeMatches.put(hpoTerm, termMatches);
        }
        return new OrganismPhenotypeMatches(organism, ImmutableMap.copyOf(speciesPhenotypeMatches));
    }

    private void logOrganismPhenotypeMatches(OrganismPhenotypeMatches organismPhenotypeMatches) {
        logger.info("Best {} phenotype matches:", organismPhenotypeMatches.getOrganism());
        for (PhenotypeMatch bestMatch : organismPhenotypeMatches.getBestPhenotypeMatches()) {
            logger.info("{}-{}={}", bestMatch.getQueryPhenotypeId(), bestMatch.getMatchPhenotypeId(), bestMatch.getScore());
        }
        logger.info("bestMaxScore={} bestAvgScore={}", organismPhenotypeMatches.getBestMatchScore(), organismPhenotypeMatches.getBestAverageScore(), organismPhenotypeMatches.getBestPhenotypeMatches().size());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.hpoIds);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhivePriority other = (PhivePriority) obj;
        if (!Objects.equals(this.hpoIds, other.hpoIds)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PhivePriority{" + "hpoIds=" + hpoIds + '}';
    }

}
