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

package org.monarchinitiative.exomiser.core.prioritisers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeMatch;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneDiseaseModel;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriorityResult extends AbstractPriorityResult {

    private double humanScore = 0;
    private double mouseScore = 0;
    private double fishScore = 0;

    private final double ppiScore;

    private final boolean candidateGeneMatch;

    private final List<PhenotypeTerm> queryPhenotypeTerms;
    private final List<GeneModelPhenotypeMatch> phenotypeEvidence;
    private final List<GeneModelPhenotypeMatch> ppiEvidence;


    /**
     * @param score The similarity score assigned by the random walk.
     */
    public HiPhivePriorityResult(int geneId, String geneSymbol, double score, List<PhenotypeTerm> queryPhenotypeTerms, List<GeneModelPhenotypeMatch> phenotypeEvidence, List<GeneModelPhenotypeMatch> ppiEvidence, double ppiScore, boolean candidateGeneMatch) {
        super(PriorityType.HIPHIVE_PRIORITY, geneId, geneSymbol, score);
        this.queryPhenotypeTerms = queryPhenotypeTerms;
        setPhenotypeEvidenceScores(phenotypeEvidence);

        this.phenotypeEvidence = phenotypeEvidence;
        this.ppiEvidence = ppiEvidence;
        this.ppiScore = ppiScore;
        
        this.candidateGeneMatch = candidateGeneMatch;
    }

    private void setPhenotypeEvidenceScores(List<GeneModelPhenotypeMatch> phenotypeEvidence) {
        if (phenotypeEvidence != null) {
            for (GeneModelPhenotypeMatch model : phenotypeEvidence) {
                switch (model.getOrganism()) {
                    case HUMAN:
                        humanScore = model.getScore();
                        break;
                    case MOUSE:
                        mouseScore = model.getScore();
                        break;
                    case FISH:
                        fishScore = model.getScore();
                        break;
                }
            }
        }
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
        return phenotypeEvidence;
    }

    public List<GeneModelPhenotypeMatch> getPpiEvidence() {
        return ppiEvidence;
    }

    public double getHumanScore() {
        return humanScore;
    }

    public double getMouseScore() {
        return mouseScore;
    }

    public double getFishScore() {
        return fishScore;
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
        StringBuilder humanPPIBuilder = new StringBuilder();
        StringBuilder mousePPIBuilder = new StringBuilder();
        StringBuilder fishPPIBuilder = new StringBuilder();

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : phenotypeEvidence) {
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            switch (geneModelPhenotypeMatch.getOrganism()) {
                case HUMAN:
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.getModel();
                    humanBuilder.append(geneDiseaseModel.getDiseaseTerm() + " (" + geneDiseaseModel.getDiseaseId() + "): ");
                    makeBestPhenotypeMatchText(humanBuilder, bestMatchesForModel);
                    break;
                case MOUSE:
                    makeBestPhenotypeMatchText(mouseBuilder, bestMatchesForModel);
                    break;
                case FISH:
                    makeBestPhenotypeMatchText(fishBuilder, bestMatchesForModel);
            }
        }
        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : ppiEvidence) {
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            switch (geneModelPhenotypeMatch.getOrganism()) {
                case HUMAN:
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.getModel();
                    humanPPIBuilder.append("Proximity to " + geneModelPhenotypeMatch.getHumanGeneSymbol() + " associated with " + geneDiseaseModel
                            .getDiseaseTerm() + " (" + geneDiseaseModel.getDiseaseId() + "): ");
                    makeBestPhenotypeMatchText(humanPPIBuilder, bestMatchesForModel);
                    break;
                case MOUSE:
                    mousePPIBuilder.append("Proximity to " + geneModelPhenotypeMatch.getHumanGeneSymbol() + " ");
                    makeBestPhenotypeMatchText(mousePPIBuilder, bestMatchesForModel);
                    break;
                case FISH:
                    fishPPIBuilder.append("Proximity to " + geneModelPhenotypeMatch.getHumanGeneSymbol() + " ");
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

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : phenotypeEvidence) {
            switch (geneModelPhenotypeMatch.getOrganism()) {
                case HUMAN:
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.getModel();
                    String diseaseLink = makeDiseaseLink(geneDiseaseModel.getDiseaseId(), geneDiseaseModel.getDiseaseTerm());
                    stringBuilder.append(String.format("<dl><dt>Phenotypic similarity %.3f to %s associated with %s.</dt>", geneModelPhenotypeMatch
                            .getScore(), diseaseLink, geneModelPhenotypeMatch.getHumanGeneSymbol()));
                    break;
                case MOUSE:
                    stringBuilder.append(String.format("<dl><dt>Phenotypic similarity %.3f to mouse mutant involving <a href=\"http://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a>.</dt>", geneModelPhenotypeMatch
                            .getScore(), geneModelPhenotypeMatch.getHumanGeneSymbol(), geneModelPhenotypeMatch.getHumanGeneSymbol()));
                    break;
                case FISH:
                    stringBuilder.append(String.format("<dl><dt>Phenotypic similarity %.3f to zebrafish mutant involving <a href=\"http://zfin.org/action/quicksearch/query?query=%s\">%s</a>.</dt>", geneModelPhenotypeMatch
                            .getScore(), geneModelPhenotypeMatch.getHumanGeneSymbol(), geneModelPhenotypeMatch.getHumanGeneSymbol()));
                    break;
            }
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = getPhenotypeTermPhenotypeMatchMap(geneModelPhenotypeMatch);
            makeBestPhenotypeMatchHtml(stringBuilder, bestMatchesForModel);
            stringBuilder.append("</dl>");
        }

        for (GeneModelPhenotypeMatch geneModelPhenotypeMatch : ppiEvidence) {
            String stringDbLink = "http://string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + geneSymbol + "%0D" + geneModelPhenotypeMatch
                    .getHumanGeneSymbol() + "&required_score=700&network_flavor=evidence&species=9606&limit=20";

            switch (geneModelPhenotypeMatch.getOrganism()) {
                case HUMAN:
                    GeneDiseaseModel geneDiseaseModel = (GeneDiseaseModel) geneModelPhenotypeMatch.getModel();
                    String diseaseLink = makeDiseaseLink(geneDiseaseModel.getDiseaseId(), geneDiseaseModel.getDiseaseTerm());
                    stringBuilder.append(String.format("<dl><dt>Proximity score %.3f in <a href=\"%s\">interactome to %s</a> and phenotypic similarity %.3f to %s associated with %s.</dt>", ppiScore, stringDbLink, geneModelPhenotypeMatch
                            .getHumanGeneSymbol(), geneModelPhenotypeMatch.getScore(), diseaseLink, geneModelPhenotypeMatch
                            .getHumanGeneSymbol()));
                    break;
                case MOUSE:
                    stringBuilder.append(String.format("<dl><dt>Proximity score %.3f in <a href=\"%s\">interactome to %s</a> and phenotypic similarity %.3f to mouse mutant of %s.</dt>", ppiScore, stringDbLink, geneModelPhenotypeMatch
                            .getHumanGeneSymbol(), geneModelPhenotypeMatch.getScore(), geneModelPhenotypeMatch.getHumanGeneSymbol()));
                    break;
                case FISH:
                    stringBuilder.append(String.format("<dl><dt>Proximity score %.3f in <a href=\"%s\">interactome to %s</a> and phenotypic similarity %.3f to fish mutant of %s.</dt>", ppiScore, stringDbLink, geneModelPhenotypeMatch
                            .getHumanGeneSymbol(), geneModelPhenotypeMatch.getScore(), geneModelPhenotypeMatch.getHumanGeneSymbol()));
                    break;
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
                stringBuilder.append(String.format("<dd>%s, %s - %s, %s</dd>", queryTerm.getId(), queryTerm.getLabel(), matchTerm.getId(), matchTerm.getLabel()));
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
    public String toString() {
        return "HiPhivePriorityResult{" +
                "geneId=" + geneId +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", score=" + score +
                ", humanScore=" + humanScore +
                ", mouseScore=" + mouseScore +
                ", fishScore=" + fishScore +
                ", ppiScore=" + ppiScore +
                ", candidateGeneMatch=" + candidateGeneMatch +
                ", queryPhenotypeTerms=" + queryPhenotypeTerms +
                ", phenotypeEvidence=" + phenotypeEvidence +
                ", ppiEvidence=" + ppiEvidence +
                '}';
    }
}
