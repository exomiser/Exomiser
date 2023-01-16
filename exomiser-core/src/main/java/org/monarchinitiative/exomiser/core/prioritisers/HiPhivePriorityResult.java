/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneDiseaseModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriorityResult extends AbstractPriorityResult {

    private final double ppiScore;

    private final boolean candidateGeneMatch;

    private final List<PhenotypeTerm> queryPhenotypeTerms;
    private final Map<Organism, GeneModelPhenotypeMatch> phenotypeEvidence;

    private final List<GeneModelPhenotypeMatch> diseaseMatches;
    private final Map<ModeOfInheritance, List<ModelPhenotypeMatch<Disease>>> diseaseMatchesByMoi;

    private final List<GeneModelPhenotypeMatch> ppiEvidence;


    /**
     * @param score The similarity score assigned by the random walk.
     */
    public HiPhivePriorityResult(int geneId, String geneSymbol, double score, List<PhenotypeTerm> queryPhenotypeTerms, List<GeneModelPhenotypeMatch> phenotypeEvidence, List<GeneModelPhenotypeMatch> ppiEvidence, double ppiScore, boolean candidateGeneMatch) {
        super(PriorityType.HIPHIVE_PRIORITY, geneId, geneSymbol, score);
        this.queryPhenotypeTerms = Objects.requireNonNullElse(queryPhenotypeTerms, List.of());
        Objects.requireNonNull(phenotypeEvidence);
        this.phenotypeEvidence = getTopScoringModels(phenotypeEvidence);
        this.diseaseMatches = getDiseaseMatches(phenotypeEvidence);
//        interface ModelMatch<T extends Model> extends Comparable<ModelPhenotypeMatch<?>>
//        final class ModelPhenotypeMatch<T extends Model> implements ModelMatch<Model>
//        abstract class GeneModelPhenotypeMatch<T extends GeneModel> implements ModelMatch<T>
//        class OrthologModelPhenotypeMatch extends GeneModelPhenotypeMatch<GeneModel>
//        class DiseaseModelPhenotypeMatch extends GeneModelPhenotypeMatch<Disease>
        this.diseaseMatchesByMoi = mapDiseaseModelsByMoi(this.diseaseMatches);

        this.ppiEvidence = ppiEvidence;
        this.ppiScore = ppiScore;

        this.candidateGeneMatch = candidateGeneMatch;
    }

    private Map<Organism, GeneModelPhenotypeMatch> getTopScoringModels(List<GeneModelPhenotypeMatch> phenotypeEvidence) {
        Map<Organism, GeneModelPhenotypeMatch> topScoringModels = new EnumMap<>(Organism.class);
        for (Organism organism : Organism.values()) {
            topScoringModels.compute(organism, (k, v) -> getBestMatchForOrganism(phenotypeEvidence, organism));
        }
        return Collections.unmodifiableMap(topScoringModels);
    }

    private List<GeneModelPhenotypeMatch> getDiseaseMatches(List<GeneModelPhenotypeMatch> phenotypeEvidence) {
        List<GeneModelPhenotypeMatch> toSort = new ArrayList<>();
        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : phenotypeEvidence) {
            if (geneModelPhenotypeMatch.getOrganism() == Organism.HUMAN) {
                toSort.add(geneModelPhenotypeMatch);
            }
        }
        toSort.sort(Comparator.comparing(GeneModelPhenotypeMatch::getScore).reversed());
        return List.copyOf(toSort);
    }

    private Map<ModeOfInheritance, List<ModelPhenotypeMatch<Disease>>> mapDiseaseModelsByMoi(List<GeneModelPhenotypeMatch> diseaseMatches) {
        Map<ModeOfInheritance, List<ModelPhenotypeMatch<Disease>>> diseaseModelsMoi = new EnumMap<>(ModeOfInheritance.class);
        for (GeneModelPhenotypeMatch diseasePhenotypeMatch : diseaseMatches) {
            GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) diseasePhenotypeMatch.getModel();
            Disease disease = geneDiseaseModel.getDisease();
            // permissive - will add any disease with unknown or compatible MOI
//            InheritanceMode inheritanceMode = disease.getInheritanceMode();
//            for (ModeOfInheritance moi : ModeOfInheritance.values()) {
//                if (inheritanceMode.isCompatibleWith(moi) || inheritanceMode == InheritanceMode.UNKNOWN) {
//                    if (diseaseModelsMoi.containsKey(moi)) {
//                        diseaseModelsMoi.get(moi).add(toModelPhenotypeMatch(diseasePhenotypeMatch));
//                    } else {
//                        ArrayList<ModelPhenotypeMatch<Disease>> matches = new ArrayList<>();
//                        matches.add(toModelPhenotypeMatch(diseasePhenotypeMatch));
//                        diseaseModelsMoi.put(moi, matches);
//                    }
//                }
//            }
            // strict - will only add disease with known and compatible MOI
            Set<ModeOfInheritance> modesOfInheritance = copyWithAnyMoi(disease.getInheritanceMode().toModeOfInheritance());
            ModelPhenotypeMatch<Disease> diseaseModelPhenotypeMatch = toModelPhenotypeMatch(diseasePhenotypeMatch);
            for (ModeOfInheritance moi : modesOfInheritance) {
                if (diseaseModelsMoi.containsKey(moi)) {
                    diseaseModelsMoi.get(moi).add(diseaseModelPhenotypeMatch);
                } else {
                    List<ModelPhenotypeMatch<Disease>> matches = new ArrayList<>();
                    matches.add(diseaseModelPhenotypeMatch);
                    diseaseModelsMoi.put(moi, matches);
                }
            }
        }
        return Maps.immutableEnumMap(diseaseModelsMoi);
    }

    private Set<ModeOfInheritance> copyWithAnyMoi(Set<ModeOfInheritance> modesOfInheritance) {
        Set<ModeOfInheritance> withAny = EnumSet.of(ModeOfInheritance.ANY);
        withAny.addAll(modesOfInheritance);
        return withAny;
    }

    private ModelPhenotypeMatch<Disease> toModelPhenotypeMatch(GeneModelPhenotypeMatch diseasePhenotypeMatch) {
        GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) diseasePhenotypeMatch.getModel();
        return ModelPhenotypeMatch.of(diseasePhenotypeMatch.getScore(), geneDiseaseModel.getDisease(), diseasePhenotypeMatch.getBestModelPhenotypeMatches());
    }

    @Nullable
    private GeneModelPhenotypeMatch getBestMatchForOrganism(List<GeneModelPhenotypeMatch> phenotypeEvidence, Organism organism) {
        double bestScore = 0;
        GeneModelPhenotypeMatch bestMatch = null;
        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : phenotypeEvidence) {
            if (geneModelPhenotypeMatch.getOrganism() == organism) {
                double matchScore = geneModelPhenotypeMatch.getScore();
                if (Double.compare(matchScore, bestScore) > 0) {
                    bestScore = matchScore;
                    bestMatch = geneModelPhenotypeMatch;
                }
            }
        }
        return bestMatch;
    }

    @Override
    public String getGeneSymbol() {
        return geneSymbol;
    }

    @Override
    public double getScore() {
        return score;
    }

    public List<PhenotypeTerm> getQueryPhenotypeTerms() {
        return queryPhenotypeTerms;
    }

    public List<GeneModelPhenotypeMatch> getPhenotypeEvidence() {
        return List.copyOf(phenotypeEvidence.values());
    }

    public List<GeneModelPhenotypeMatch> getDiseaseMatches() {
        return diseaseMatches;
    }

    public List<ModelPhenotypeMatch<Disease>> getCompatibleDiseaseMatches(ModeOfInheritance modeOfInheritance) {
        return diseaseMatchesByMoi.getOrDefault(modeOfInheritance, List.of());
    }

    public List<GeneModelPhenotypeMatch> getPpiEvidence() {
        return ppiEvidence;
    }

    public double getHumanScore() {
        return getScoreForOrganism(Organism.HUMAN);
    }

    public double getMouseScore() {
        return getScoreForOrganism(Organism.MOUSE);
    }

    public double getFishScore() {
        return getScoreForOrganism(Organism.FISH);
    }

    private double getScoreForOrganism(Organism organism) {
        GeneModelPhenotypeMatch geneModelPhenotypeMatch = phenotypeEvidence.get(organism);
        return geneModelPhenotypeMatch == null ? 0d : geneModelPhenotypeMatch.getScore();
    }

    public double getPpiScore() {
        return ppiScore;
    }

    public boolean isCandidateGeneMatch() {
        return candidateGeneMatch;
    }

    /**
     * @return A summary for the text output formats
     */
    @JsonIgnore
    public String getPhenotypeEvidenceText() {
        StringBuilder humanBuilder = new StringBuilder();
        StringBuilder mouseBuilder = new StringBuilder();
        StringBuilder fishBuilder = new StringBuilder();

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : phenotypeEvidence.values()) {
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            switch (geneModelPhenotypeMatch.getOrganism()) {
                case HUMAN -> {
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.getModel();
                    humanBuilder.append(geneDiseaseModel.getDiseaseTerm())
                            .append(" (").append(geneDiseaseModel.getDiseaseId()).append("): ");
                    makeBestPhenotypeMatchText(humanBuilder, bestMatchesForModel);
                }
                case MOUSE -> makeBestPhenotypeMatchText(mouseBuilder, bestMatchesForModel);
                case FISH -> makeBestPhenotypeMatchText(fishBuilder, bestMatchesForModel);
            }
        }

        StringBuilder humanPPIBuilder = new StringBuilder();
        StringBuilder mousePPIBuilder = new StringBuilder();
        StringBuilder fishPPIBuilder = new StringBuilder();

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : ppiEvidence) {
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            String proximityToGeneSymbol = "Proximity to " + geneModelPhenotypeMatch.getHumanGeneSymbol() + " ";
            switch (geneModelPhenotypeMatch.getOrganism()) {
                case HUMAN -> {
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.getModel();
                    humanPPIBuilder.append(proximityToGeneSymbol)
                            .append("associated with ")
                            .append(geneDiseaseModel.getDiseaseTerm())
                            .append(" (").append(geneDiseaseModel.getDiseaseId()).append("): ");
                    makeBestPhenotypeMatchText(humanPPIBuilder, bestMatchesForModel);
                }
                case MOUSE -> {
                    mousePPIBuilder.append(proximityToGeneSymbol);
                    makeBestPhenotypeMatchText(mousePPIBuilder, bestMatchesForModel);
                }
                case FISH -> {
                    fishPPIBuilder.append(proximityToGeneSymbol);
                    makeBestPhenotypeMatchText(fishPPIBuilder, bestMatchesForModel);
                }
            }
        }
        String human = humanBuilder.toString();
        String mouse = mouseBuilder.toString();
        String fish = fishBuilder.toString();

        String humanPPI = humanPPIBuilder.toString();
        String mousePPI = mousePPIBuilder.toString();
        String fishPPI = fishPPIBuilder.toString();
        return String.format("%s\t%s\t%s\t%s\t%s\t%s", human, mouse, fish, humanPPI, mousePPI, fishPPI);
    }

    private Map<PhenotypeTerm, PhenotypeMatch> getPhenotypeTermPhenotypeMatchMap(GeneModelPhenotypeMatch geneModelPhenotypeMatch) {
        return geneModelPhenotypeMatch
                .getBestModelPhenotypeMatches()
                .stream()
                .collect(toMap(PhenotypeMatch::getQueryPhenotype, Function.identity()));
    }

    /**
     */
    @JsonIgnore
    @Override
    public String getHTMLCode() {
        StringBuilder stringBuilder = new StringBuilder();

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : phenotypeEvidence.values()) {
            switch (geneModelPhenotypeMatch.getOrganism()) {
                case HUMAN -> {
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.getModel();
                    String diseaseLink = makeDiseaseLink(geneDiseaseModel.getDiseaseId(), geneDiseaseModel.getDiseaseTerm());
                    stringBuilder.append(String.format("<dl><dt>Phenotypic similarity %.3f to %s associated with %s.</dt>", geneModelPhenotypeMatch
                            .getScore(), diseaseLink, geneModelPhenotypeMatch.getHumanGeneSymbol()));
                }
                case MOUSE ->
                        stringBuilder.append(String.format("<dl><dt>Phenotypic similarity %.3f to mouse mutant involving <a href=\"http://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a>.</dt>", geneModelPhenotypeMatch
                                .getScore(), geneModelPhenotypeMatch.getHumanGeneSymbol(), geneModelPhenotypeMatch.getHumanGeneSymbol()));
                case FISH ->
                        stringBuilder.append(String.format("<dl><dt>Phenotypic similarity %.3f to zebrafish mutant involving <a href=\"http://zfin.org/action/quicksearch/query?query=%s\">%s</a>.</dt>", geneModelPhenotypeMatch
                                .getScore(), geneModelPhenotypeMatch.getHumanGeneSymbol(), geneModelPhenotypeMatch.getHumanGeneSymbol()));
            }
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            makeBestPhenotypeMatchHtml(stringBuilder, bestMatchesForModel);
            stringBuilder.append("</dl>");
        }

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : ppiEvidence) {
            String stringDbLink = "http://version10.string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + geneSymbol + "%0D" + geneModelPhenotypeMatch
                    .getHumanGeneSymbol() + "&required_score=700&network_flavor=evidence&species=9606&limit=20";

            switch (geneModelPhenotypeMatch.getOrganism()) {
                case HUMAN -> {
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.getModel();
                    String diseaseLink = makeDiseaseLink(geneDiseaseModel.getDiseaseId(), geneDiseaseModel.getDiseaseTerm());
                    stringBuilder.append(String.format("<dl><dt>Proximity score %.3f in <a href=\"%s\">interactome to %s</a> and phenotypic similarity %.3f to %s associated with %s.</dt>", ppiScore, stringDbLink, geneModelPhenotypeMatch
                            .getHumanGeneSymbol(), geneModelPhenotypeMatch.getScore(), diseaseLink, geneModelPhenotypeMatch
                            .getHumanGeneSymbol()));
                }
                case MOUSE ->
                        stringBuilder.append(String.format("<dl><dt>Proximity score %.3f in <a href=\"%s\">interactome to %s</a> and phenotypic similarity %.3f to mouse mutant of %s.</dt>", ppiScore, stringDbLink, geneModelPhenotypeMatch
                                .getHumanGeneSymbol(), geneModelPhenotypeMatch.getScore(), geneModelPhenotypeMatch.getHumanGeneSymbol()));
                case FISH ->
                        stringBuilder.append(String.format("<dl><dt>Proximity score %.3f in <a href=\"%s\">interactome to %s</a> and phenotypic similarity %.3f to fish mutant of %s.</dt>", ppiScore, stringDbLink, geneModelPhenotypeMatch
                                .getHumanGeneSymbol(), geneModelPhenotypeMatch.getScore(), geneModelPhenotypeMatch.getHumanGeneSymbol()));
            }
            Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            makeBestPhenotypeMatchHtml(stringBuilder, bestModelPhenotypeMatches);
            stringBuilder.append("</dl>");
        }
        String html = stringBuilder.toString();
        if (html.isEmpty()) {
            return "<dl><dt>No phenotype or PPI evidence</dt></dl>";
        }
        return html;
    }

    private void makeBestPhenotypeMatchText(StringBuilder stringBuilder, Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches) {
        for (PhenotypeTerm queryTerm : queryPhenotypeTerms) {
            if (bestModelPhenotypeMatches.containsKey(queryTerm)) {// && bestModelPhenotypeMatches.get(queryTerm).getScore() > 1.75) {// RESTRICT TO HIGH QUALITY MATCHES
                PhenotypeMatch match = bestModelPhenotypeMatches.get(queryTerm);
                PhenotypeTerm matchTerm = match.getMatchPhenotype();
                stringBuilder.append(String.format("%s (%s)-%s (%s), ", queryTerm.getLabel(), queryTerm.getId(), matchTerm.getLabel(), matchTerm.getId()));
            }
        }
    }

    private void makeBestPhenotypeMatchHtml(StringBuilder stringBuilder, Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches) {
        stringBuilder.append("<dt>Best Phenotype Matches:</dt>");
        for (PhenotypeTerm queryTerm : queryPhenotypeTerms) {
            if (bestModelPhenotypeMatches.containsKey(queryTerm)) {
                PhenotypeMatch match = bestModelPhenotypeMatches.get(queryTerm);
                PhenotypeTerm matchTerm = match.getMatchPhenotype();
                stringBuilder.append(String.format("<dd>%s, %s - %.3f - %s, %s</dd>", queryTerm.getId(), queryTerm.getLabel(), match.getScore(), matchTerm.getId(), matchTerm.getLabel()));
            } else {
                stringBuilder.append(String.format("<dd>%s, %s -</dd>", queryTerm.getId(), queryTerm.getLabel()));
            }
        }
    }

    private String makeDiseaseLink(String diseaseId, String diseaseTerm) {
        String[] databaseNameAndIdentifier = diseaseId.split(":");
        String databaseName = databaseNameAndIdentifier[0];
        String id = databaseNameAndIdentifier[1];
        if (databaseName.equals("OMIM")) {
            return "<a href=\"http://www.omim.org/" + id + "\">" + diseaseTerm + "</a>";
        } else {
            return "<a href=\"http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=" + id + "\">" + diseaseTerm + "</a>";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        HiPhivePriorityResult that = (HiPhivePriorityResult) o;
        return Double.compare(that.ppiScore, ppiScore) == 0 && candidateGeneMatch == that.candidateGeneMatch && queryPhenotypeTerms.equals(that.queryPhenotypeTerms) && phenotypeEvidence.equals(that.phenotypeEvidence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ppiScore, candidateGeneMatch, queryPhenotypeTerms, phenotypeEvidence);
    }

    @Override
    public String toString() {
        return "HiPhivePriorityResult{" +
                "geneId=" + geneId +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", score=" + score +
                ", humanScore=" + getHumanScore() +
                ", mouseScore=" + getMouseScore() +
                ", fishScore=" + getFishScore() +
                ", ppiScore=" + ppiScore +
                ", candidateGeneMatch=" + candidateGeneMatch +
                ", queryPhenotypeTerms=" + queryPhenotypeTerms +
                ", phenotypeEvidence=" + phenotypeEvidence.values() +
                ", ppiEvidence=" + ppiEvidence +
                '}';
    }
}
