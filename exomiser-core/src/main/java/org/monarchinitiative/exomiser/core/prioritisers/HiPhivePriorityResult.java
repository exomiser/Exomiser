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
import org.monarchinitiative.exomiser.core.model.DiseaseIdentifiers;
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

    private void makeBestPhenotypeMatchText(StringBuilder stringBuilder, Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches) {
        for (PhenotypeTerm queryTerm : queryPhenotypeTerms) {
            if (bestModelPhenotypeMatches.containsKey(queryTerm)) {// && bestModelPhenotypeMatches.get(queryTerm).score() > 1.75) {// RESTRICT TO HIGH QUALITY MATCHES
                PhenotypeMatch match = bestModelPhenotypeMatches.get(queryTerm);
                PhenotypeTerm matchTerm = match.matchPhenotype();
                stringBuilder.append(String.format("%s (%s)-%s (%s), ", queryTerm.label(), queryTerm.id(), matchTerm.label(), matchTerm.id()));
            }
        }
    }

    /**
     */
    @JsonIgnore
    @Override
    public String getHTMLCode() {
        StringBuilder stringBuilder = new StringBuilder();
        if (!phenotypeEvidence.isEmpty()) {
            stringBuilder.append("<div class=\"container container-fluid text-center\">");
            stringBuilder.append("<div class=\"row\">");
            for (GeneModelPhenotypeMatch match : phenotypeEvidence.values()) {
                stringBuilder.append("<div class=\"col\">");
                stringBuilder.append("<table class=\"table table-striped caption-top\">");
                stringBuilder.append("<caption class=\"card-title text-center text-primary border border-primary-subtle rounded-3\">");
                switch (match.organism()) {
                    case HUMAN:
                        GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) match.model();
                        String diseaseLink = makePicoDiseaseLink(geneDiseaseModel.diseaseId(), geneDiseaseModel.diseaseTerm());
                        stringBuilder.append(String.format("Phenotype Score <b>%.3f</b> %s <a href=\"https://ensembl.org/Homo_sapiens/Gene/Summary?g=%s\">%s</a>",
                                match.score(), diseaseLink, match.humanGeneSymbol(), match.humanGeneSymbol()));
                        break;
                    case MOUSE:
                        stringBuilder.append(String.format("Phenotype Score <b>%.3f</b> Mouse Mutant <a href=\"https://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a>",
                                match.score(), match.humanGeneSymbol(), match.model().id()));
                        break;
                    case FISH:
                        stringBuilder.append(String.format("Phenotype Score <b>%.3f</b> Zebrafish Mutant <a href=\"https://zfin.org/action/quicksearch/query?query=%s\">%s</a>",
                                match.score(), match.humanGeneSymbol(), match.model().id()));
                        break;
                }
                stringBuilder.append("</caption>");
                stringBuilder.append("""
                                     <thead>
                                         <tr>
                                             <th scope="col">Sample Phenotype</th>
                                             <th scope="col">Score</th>
                                             <th scope="col">Match Phenotype</th>
                                         </tr>
                                     </thead>
                                     """);
                Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = getPhenotypeTermPhenotypeMatchMap(match);
                stringBuilder.append("<tbody>");
                makePicoBestPhenotypeMatchHtml(stringBuilder, bestMatchesForModel);
                stringBuilder.append("</tbody>");
                stringBuilder.append("</table>");
                stringBuilder.append("</div>");
            }
            stringBuilder.append("</div>");
            stringBuilder.append("</div>");
        }
        if (!ppiEvidence.isEmpty()) {
            stringBuilder.append("<div class=\"container container-fluid text-center\">");
            stringBuilder.append("<div class=\"row\">");
            for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : ppiEvidence) {
                String stringDbLink = "http://version10.string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + geneSymbol + "%0D" + geneModelPhenotypeMatch
                        .humanGeneSymbol() + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
                stringBuilder.append("<div class=\"col\">");
                stringBuilder.append("<table class=\"table table-striped caption-top\">");
                stringBuilder.append("<caption class=\"card-title text-center text-info border border-info-subtle rounded-3\">");
                switch (geneModelPhenotypeMatch.organism()) {
                    case HUMAN:
                        GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.model();
                        String diseaseLink = makePicoDiseaseLink(geneDiseaseModel.diseaseId(), geneDiseaseModel.diseaseTerm());
                        stringBuilder.append(String.format("Phenotype Score <b>%.3f</b> %s via Proximity Score <b>%.3f</b> <a href=\"%s\">Interactome Proximity</a> to <a href=\"https://ensembl.org/Homo_sapiens/Gene/Summary?g=%s\">%s</a>",
                                geneModelPhenotypeMatch.score(), diseaseLink, ppiScore, stringDbLink, geneModelPhenotypeMatch.humanGeneSymbol(), geneModelPhenotypeMatch.humanGeneSymbol()));
                        break;
                    case MOUSE:
                        stringBuilder.append(String.format("Phenotype Score <b>%.3f</b> Mouse Mutant via Proximity Score <b>%.3f</b> <a href=\"%s\">Interactome Proximity</a> to <a href=\"https://ensembl.org/Homo_sapiens/Gene/Summary?g=%s\">%s</a>",
                                geneModelPhenotypeMatch.score(), ppiScore, stringDbLink,  geneModelPhenotypeMatch.humanGeneSymbol(), geneModelPhenotypeMatch.humanGeneSymbol()));
                        break;
                    case FISH:
                        stringBuilder.append(String.format("Phenotype Score <b>%.3f</b> Zebrafish via Proximity Score <b>%.3f</b> <a href=\"%s\">Interactome Proximity</a> to <a href=\"https://ensembl.org/Homo_sapiens/Gene/Summary?g=%s\">%s</a>",
                                geneModelPhenotypeMatch.score(), ppiScore, stringDbLink, geneModelPhenotypeMatch.humanGeneSymbol(), geneModelPhenotypeMatch.humanGeneSymbol()));
                        break;
                }
                stringBuilder.append("</caption>");
                stringBuilder.append("""
                                     <thead>
                                         <tr>
                                             <th scope="col">Sample Phenotype</th>
                                             <th scope="col">Score</th>
                                             <th scope="col">Match Phenotype</th>
                                         </tr>
                                     </thead>
                                     """);
                Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
                stringBuilder.append("<tbody>");
                makePicoBestPhenotypeMatchHtml(stringBuilder, bestModelPhenotypeMatches);
                stringBuilder.append("</tbody>");
                stringBuilder.append("</table>");
                stringBuilder.append("</div>");
            }
            stringBuilder.append("</div>");
            stringBuilder.append("</div>");
        }
        String html = stringBuilder.toString();
        if (html.isEmpty()) {
            return "<dl><dt>No phenotype or PPI evidence</dt></dl>";
        }
        return html;
    }

    private void makePicoBestPhenotypeMatchHtml(StringBuilder stringBuilder, Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches) {
        Collection<PhenotypeMatch> matches = new ArrayList<>();
        Collection<PhenotypeTerm> unmatched = new ArrayList<>();
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
                .toList();

        for (PhenotypeMatch match: matches){
            stringBuilder.append("<tr>");
            stringBuilder.append(String.format(
                    "<td><div class=\"secondary-text-emphasis\">%s</div>\n%s</td>" +
                    "<td>%.2f</td>" +
                    "<td><div class=\"secondary-text-emphasis\">%s</div>\n%s</td>", match.queryPhenotypeId(), match.queryPhenotype().label(), match.score(), match.matchPhenotype().id(), match.matchPhenotype().label()));
            stringBuilder.append("</tr>");
        }

        for (PhenotypeTerm term: unmatched){
            stringBuilder.append("<tr>");
            stringBuilder.append(String.format(
                    "<td><div class=\"secondary-text-emphasis\">%s</div>\n%s</td>" +
                    "<td>%.2f</td>" +
                    "<td><div class=\"secondary-text-emphasis\">%s</div>\n%s</td>", term.id(), term.label(), 0.00, "", ""));
            stringBuilder.append("</tr>");
        }
    }

    private String makePicoDiseaseLink(String diseaseId, String diseaseTerm) {
        String target = DiseaseIdentifiers.toURLString(diseaseId);
        return "<a href=\""+ target + "\">" + diseaseTerm + "</a>";
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
