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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.Organism;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneDiseaseModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;

import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriorityResult implements PriorityResult {

    private final int geneId;
    private final String geneSymbol;
    private final double score;

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
        this.geneId = geneId;
        this.geneSymbol = geneSymbol;
        this.score = score;
        this.queryPhenotypeTerms = Objects.requireNonNullElse(queryPhenotypeTerms, List.of());
        Objects.requireNonNull(phenotypeEvidence);
        this.phenotypeEvidence = getTopScoringModels(phenotypeEvidence);
        this.diseaseMatches = diseaseMatches(phenotypeEvidence);
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

    private List<GeneModelPhenotypeMatch> diseaseMatches(List<GeneModelPhenotypeMatch> phenotypeEvidence) {
        List<GeneModelPhenotypeMatch> toSort = new ArrayList<>();
        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : phenotypeEvidence) {
            if (geneModelPhenotypeMatch.organism() == Organism.HUMAN) {
                toSort.add(geneModelPhenotypeMatch);
            }
        }
        toSort.sort(Comparator.comparing(GeneModelPhenotypeMatch::score).reversed());
        return List.copyOf(toSort);
    }

    private Map<ModeOfInheritance, List<ModelPhenotypeMatch<Disease>>> mapDiseaseModelsByMoi(List<GeneModelPhenotypeMatch> diseaseMatches) {
        Map<ModeOfInheritance, List<ModelPhenotypeMatch<Disease>>> diseaseModelsMoi = new EnumMap<>(ModeOfInheritance.class);
        for (GeneModelPhenotypeMatch diseasePhenotypeMatch : diseaseMatches) {
            GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) diseasePhenotypeMatch.model();
            Disease disease = geneDiseaseModel.disease();
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
            Set<ModeOfInheritance> modesOfInheritance = copyWithAnyMoi(disease.inheritanceMode().toModeOfInheritance());
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
        GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) diseasePhenotypeMatch.model();
        return ModelPhenotypeMatch.of(diseasePhenotypeMatch.score(), geneDiseaseModel.disease(), diseasePhenotypeMatch.bestPhenotypeMatches());
    }

    @Nullable
    private GeneModelPhenotypeMatch getBestMatchForOrganism(List<GeneModelPhenotypeMatch> phenotypeEvidence, Organism organism) {
        double bestScore = 0;
        GeneModelPhenotypeMatch bestMatch = null;
        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : phenotypeEvidence) {
            if (geneModelPhenotypeMatch.organism() == organism) {
                double matchScore = geneModelPhenotypeMatch.score();
                if (Double.compare(matchScore, bestScore) > 0) {
                    bestScore = matchScore;
                    bestMatch = geneModelPhenotypeMatch;
                }
            }
        }
        return bestMatch;
    }

    @Override
    public int geneId() {
        return geneId;
    }

    @Override
    public String geneSymbol() {
        return geneSymbol;
    }

    @Override
    public double score() {
        return score;
    }

    @Override
    public PriorityType priorityType() {
        return PriorityType.HIPHIVE_PRIORITY;
    }

    @JsonProperty
    public List<PhenotypeTerm> queryPhenotypeTerms() {
        return queryPhenotypeTerms;
    }

    @JsonProperty
    public List<GeneModelPhenotypeMatch> phenotypeEvidence() {
        return List.copyOf(phenotypeEvidence.values());
    }

    @JsonProperty
    public List<GeneModelPhenotypeMatch> diseaseMatches() {
        return diseaseMatches;
    }

    public List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches(ModeOfInheritance modeOfInheritance) {
        return diseaseMatchesByMoi.getOrDefault(modeOfInheritance, List.of());
    }

    @JsonProperty
    public List<GeneModelPhenotypeMatch> ppiEvidence() {
        return ppiEvidence;
    }

    public double humanScore() {
        return scoreForOrganism(Organism.HUMAN);
    }

    public double mouseScore() {
        return scoreForOrganism(Organism.MOUSE);
    }

    public double fishScore() {
        return scoreForOrganism(Organism.FISH);
    }

    private double scoreForOrganism(Organism organism) {
        GeneModelPhenotypeMatch geneModelPhenotypeMatch = phenotypeEvidence.get(organism);
        return geneModelPhenotypeMatch == null ? 0d : geneModelPhenotypeMatch.score();
    }

    @JsonProperty
    public double ppiScore() {
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
            switch (geneModelPhenotypeMatch.organism()) {
                case HUMAN:
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.model();
                    humanBuilder.append(geneDiseaseModel.diseaseTerm())
                            .append(" (").append(geneDiseaseModel.diseaseId()).append("): ");
                    makeBestPhenotypeMatchText(humanBuilder, bestMatchesForModel);
                    break;
                case MOUSE:
                    makeBestPhenotypeMatchText(mouseBuilder, bestMatchesForModel);
                    break;
                case FISH:
                    makeBestPhenotypeMatchText(fishBuilder, bestMatchesForModel);
            }
        }

        StringBuilder humanPPIBuilder = new StringBuilder();
        StringBuilder mousePPIBuilder = new StringBuilder();
        StringBuilder fishPPIBuilder = new StringBuilder();

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : ppiEvidence) {
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            String proximityToGeneSymbol = "Proximity to " + geneModelPhenotypeMatch.humanGeneSymbol() + " ";
            switch (geneModelPhenotypeMatch.organism()) {
                case HUMAN:
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.model();
                    humanPPIBuilder.append(proximityToGeneSymbol)
                            .append("associated with ")
                            .append(geneDiseaseModel.diseaseTerm())
                            .append(" (").append(geneDiseaseModel.diseaseId()).append("): ");
                    makeBestPhenotypeMatchText(humanPPIBuilder, bestMatchesForModel);
                    break;
                case MOUSE:
                    mousePPIBuilder.append(proximityToGeneSymbol);
                    makeBestPhenotypeMatchText(mousePPIBuilder, bestMatchesForModel);
                    break;
                case FISH:
                    fishPPIBuilder.append(proximityToGeneSymbol);
                    makeBestPhenotypeMatchText(fishPPIBuilder, bestMatchesForModel);
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
                .bestPhenotypeMatches()
                .stream()
                .collect(toMap(PhenotypeMatch::queryPhenotype, Function.identity()));
    }

    /**
     */
    @JsonIgnore
    @Override
    public String getHTMLCode() {
        StringBuilder stringBuilder = new StringBuilder();

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : phenotypeEvidence.values()) {
            stringBuilder.append("<div class=\"pheno-match-card card col-sm-5\">");
            switch (geneModelPhenotypeMatch.organism()) {
                case HUMAN:
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.model();
                    String diseaseLink = makeDiseaseLink(geneDiseaseModel.diseaseId(), geneDiseaseModel.diseaseTerm());
                    stringBuilder.append(String.format("<h5 class=\"card-header\"><span class=\"badge bg-secondary\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Phenotype Score\">%.3f</span>%s <a href=\"https://useast.ensembl.org/Homo_sapiens/Gene/Summary?g=%s\" target=\"_blank\" class=\"text-decoration-none\">%s</a></h5>", geneModelPhenotypeMatch
                            .score(), diseaseLink, geneModelPhenotypeMatch.humanGeneSymbol(), geneModelPhenotypeMatch.humanGeneSymbol()));
                    break;
                case MOUSE:
                    stringBuilder.append(String.format("<h5 class=\"card-header\"><span class=\"badge bg-secondary\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Phenotype Score\">%.3f</span><span>Mouse Mutant</span> <a class=\"text-decoration-none\" target=\"_blank\" href=\"https://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a></h5>", geneModelPhenotypeMatch
                            .score(), geneModelPhenotypeMatch.humanGeneSymbol(), geneModelPhenotypeMatch.humanGeneSymbol()));
                    break;
                case FISH:
                    stringBuilder.append(String.format("<h5 class=\"card-header\"><span class=\"badge bg-secondary\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Phenotype Score\">%.3f</span><span>Zebrafish Mutant</span> <a class=\"text-decoration-none\" target=\"_blank\" href=\"https://zfin.org/action/quicksearch/query?query=%s\">%s</a></h5>", geneModelPhenotypeMatch
                            .score(), geneModelPhenotypeMatch.humanGeneSymbol(), geneModelPhenotypeMatch.humanGeneSymbol()));
                    break;
            }
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            makeBestPhenotypeMatchHtml(stringBuilder, bestMatchesForModel);
            stringBuilder.append("</div>");
        }

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : ppiEvidence) {
            String stringDbLink = "http://version10.string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + geneSymbol + "%0D" + geneModelPhenotypeMatch
                    .humanGeneSymbol() + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
            stringBuilder.append("<div class=\"pheno-match-card card col-sm-5\">");
            switch (geneModelPhenotypeMatch.organism()) {
                case HUMAN:
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.model();
                    String diseaseLink = makeDiseaseLink(geneDiseaseModel.diseaseId(), geneDiseaseModel.diseaseTerm());
                    stringBuilder.append(String.format("<h5 class=\"card-header\"><div class=\"d-flex ai-c\"><span class=\"badge bg-secondary\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Phenotype Score\">%.3f</span>&nbsp;&nbsp;%s</div> via <div class=\"d-flex ai-c\"><span class=\"badge bg-secondary\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Proximity Score\">%.3f</span>&nbsp;&nbsp;<a class=\"text-decoration-none\" target=\"_blank\" href=\"%s\">Interactome Proximity</a></div> to <a href=\"https://useast.ensembl.org/Homo_sapiens/Gene/Summary?g=%s\" target=\"_blank\" class=\"text-decoration-none\">%s</a></h5>", geneModelPhenotypeMatch.score(), diseaseLink, ppiScore, stringDbLink, geneModelPhenotypeMatch
                            .humanGeneSymbol(), geneModelPhenotypeMatch
                            .humanGeneSymbol()));
                    break;
                case MOUSE:
                    stringBuilder.append(String.format("<h5 class=\"card-header\"><div class=\"d-flex ai-c\"><span class=\"badge bg-secondary\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Phenotype Score\">%.3f</span><span>&nbsp;&nbsp;Mouse Mutant</span></div> via <div class=\"d-flex ai-c\"><span class=\"badge bg-secondary\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Proximity Score\">%.3f</span>&nbsp;&nbsp;<a class=\"text-decoration-none\" target=\"_blank\" href=\"%s\">Interactome Proximity</a></div> to <a href=\"https://useast.ensembl.org/Homo_sapiens/Gene/Summary?g=%s\" target=\"_blank\" class=\"text-decoration-none\">%s</a></h5>", geneModelPhenotypeMatch.score(), ppiScore, stringDbLink,  geneModelPhenotypeMatch.humanGeneSymbol(), geneModelPhenotypeMatch.humanGeneSymbol()));
                    break;
                case FISH:
                    stringBuilder.append(String.format("<h5 class=\"card-header\"><div class=\"d-flex ai-c\"><span class=\"badge bg-secondary\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Phenotype Score\">%.3f</span>&nbsp;&nbsp;<span>Zebrafish</span></div> via <div class=\"d-flex ai-c\"><span class=\"badge bg-secondary\" data-bs-toggle=\"tooltip\" data-bs-placement=\"top\" title=\"Proximity Score\">%.3f</span>&nbsp;&nbsp;<a class=\"text-decoration-none\" target=\"_blank\" href=\"%s\">Interactome Proximity</a></div> to <a href=\"https://useast.ensembl.org/Homo_sapiens/Gene/Summary?g=%s\" target=\"_blank\" class=\"text-decoration-none\">%s</a></h5>", geneModelPhenotypeMatch.score(), ppiScore, stringDbLink, geneModelPhenotypeMatch.humanGeneSymbol(), geneModelPhenotypeMatch
                            .humanGeneSymbol()));
                    break;
            }
            Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            makeBestPhenotypeMatchHtml(stringBuilder, bestModelPhenotypeMatches);
            stringBuilder.append("</div>");
        }
        String html = stringBuilder.toString();
        if (html.isEmpty()) {
            return "<dl><dt>No phenotype or PPI evidence</dt></dl>";
        }
        return html;
    }

    private void makeBestPhenotypeMatchText(StringBuilder stringBuilder, Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches) {
        for (PhenotypeTerm queryTerm : queryPhenotypeTerms) {
            if (bestModelPhenotypeMatches.containsKey(queryTerm)) {// && bestModelPhenotypeMatches.get(queryTerm).score() > 1.75) {// RESTRICT TO HIGH QUALITY MATCHES
                PhenotypeMatch match = bestModelPhenotypeMatches.get(queryTerm);
                PhenotypeTerm matchTerm = match.matchPhenotype();
                stringBuilder.append(String.format("%s (%s)-%s (%s), ", queryTerm.label(), queryTerm.id(), matchTerm.label(), matchTerm.id()));
            }
        }
    }

    private void makeBestPhenotypeMatchHtml(StringBuilder stringBuilder, Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches) {
        Collection<PhenotypeMatch> matches = new ArrayList<>();
        Collection<PhenotypeTerm> unmatched = new ArrayList<>();
        stringBuilder.append("<div class=\"card-body\">");
        stringBuilder.append("<div class=\"d-flex px-2\"><div class=\"flex-grow-1 fw-bold\">Sample</div><div class=\"fw-bold\">Reference</div></div>");
        for (PhenotypeTerm queryTerm : queryPhenotypeTerms) {
            if (bestModelPhenotypeMatches.containsKey(queryTerm)) {
                PhenotypeMatch match = bestModelPhenotypeMatches.get(queryTerm);
                matches.add(match);
            } else {
                unmatched.add(queryTerm);
            }
        }

        matches = matches.stream()
                .sorted((a, b) -> {
                    boolean aMatches = a.matchPhenotypeId().equals(a.queryPhenotypeId());
                    boolean bMatches = b.matchPhenotypeId().equals(b.queryPhenotypeId());
                    int compare = Boolean.compare(bMatches, aMatches);
                    if (compare == 0) {
                        return Double.compare(b.simJ(), a.simJ());
                    }
                    return compare;
                })
                .collect(Collectors.toList());

        for (PhenotypeMatch match: matches){
            stringBuilder.append(String.format(
                    "<div class=\"matched-set\"><div class=\"match\"><div class=\"match-left text-sm\"><div class=\"match-id\">%s</div><div class=\"match-name px-2\">%s</div></div>" +
                    "<div class=\"align-self-center\"><span class=\"badge bg-secondary\">%.2f</span></div>" +
                    "<div class=\"match-right text-sm\"><div class=\"match-id\">%s</div><div class=\"match-name px-2\">%s</div></div></div></div>", match.queryPhenotypeId(), match.queryPhenotype().label(), match.simJ(), match.matchPhenotype().id(), match.matchPhenotype().label()));
        }

        for (PhenotypeTerm term: unmatched){
            stringBuilder.append(String.format(
                    "<div class=\"unmatched-set hidden\"><div class=\"match\"><div class=\"match-left text-sm\"><div class=\"match-id\">%s</div><div class=\"match-name px-2\">%s</div></div>" +
                            "<div class=\"align-self-center\"><span class=\"badge bg-warning\">%.2f</span></div>" +
                            "<div class=\"match-right text-sm\"><div class=\"match-id\">%s</div><div class=\"match-name px-2\">%s</div></div></div></div>", term.id(), term.label(), 0.00, "", ""));
        }
        stringBuilder.append("</div>");
    }

    private String makeDiseaseLink(String diseaseId, String diseaseTerm) {
        String[] databaseNameAndIdentifier = diseaseId.split(":");
        String databaseName = databaseNameAndIdentifier[0];
        String id = databaseNameAndIdentifier[1];
        if (databaseName.equals("OMIM")) {
            return "<a class=\"text-decoration-none\" href=\"http://www.omim.org/entry/" + id + "\" target=\"_blank\">" + diseaseTerm + "</a>";
        } else {
            return "<a class=\"text-decoration-none\" href=\"http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=" + id + "\" target=\"_blank\">" + diseaseTerm + "</a>";
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
               ", humanScore=" + humanScore() +
               ", mouseScore=" + mouseScore() +
               ", fishScore=" + fishScore() +
               ", ppiScore=" + ppiScore +
               ", candidateGeneMatch=" + candidateGeneMatch +
               ", queryPhenotypeTerms=" + queryPhenotypeTerms +
               ", phenotypeEvidence=" + phenotypeEvidence.values() +
               ", ppiEvidence=" + ppiEvidence +
               '}';
    }
}
