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

import de.charite.compbio.exomiser.core.model.DiseaseModel;
import de.charite.compbio.exomiser.core.model.Model;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class HiPhivePriorityResult extends AbstractPriorityResult {

    private final List<PhenotypeTerm> queryPhenotypeTerms;
    private final List<Model> phenotypeEvidence;
    private final List<Model> ppiEvidence;

    private double humanScore = 0f;
    private double mouseScore = 0f;
    private double fishScore = 0f;

    private final double walkerScore;
    
    private final boolean candidateGeneMatch;

    /**
     * @param score The similarity score assigned by the random walk.
     */
    public HiPhivePriorityResult(int geneId, String geneSymbol, double score, List<PhenotypeTerm> queryPhenotypeTerms, List<Model> phenotypeEvidence, List<Model> ppiEvidence, double walkerScore, boolean candidateGeneMatch) {
        super(PriorityType.HIPHIVE_PRIORITY, geneId, geneSymbol, score);
        this.queryPhenotypeTerms = queryPhenotypeTerms;
        setPhenotypeEvidenceScores(phenotypeEvidence);

        this.phenotypeEvidence = phenotypeEvidence;
        this.ppiEvidence = ppiEvidence;
        this.walkerScore = walkerScore;
        
        this.candidateGeneMatch = candidateGeneMatch;
    }

    private void setPhenotypeEvidenceScores(List<Model> phenotypeEvidence) {
        if (phenotypeEvidence != null) {
            for (Model model : phenotypeEvidence) {
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

    public List<Model> getPhenotypeEvidence() {
        return phenotypeEvidence;
    }

    public List<Model> getPpiEvidence() {
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

    public double getWalkerScore() {
        return walkerScore;
    }

    public boolean isCandidateGeneMatch() {
        return candidateGeneMatch;
    }

    /**
     * @return A summary for the text output formats
     */
    public String getPhenotypeEvidenceText() {
        StringBuilder humanBuilder = new StringBuilder();
        StringBuilder mouseBuilder = new StringBuilder();
        StringBuilder fishBuilder = new StringBuilder();
        StringBuilder humanPPIBuilder = new StringBuilder();
        StringBuilder mousePPIBuilder = new StringBuilder();
        StringBuilder fishPPIBuilder = new StringBuilder();
        for (Model model : phenotypeEvidence) {
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = model.getBestPhenotypeMatchForTerms();
            switch (model.getOrganism()) {
                case HUMAN:
                    DiseaseModel diseaseModel = (DiseaseModel) model;
                    humanBuilder.append(diseaseModel.getDiseaseTerm() + " (" + diseaseModel.getDiseaseId() + "): ");
                    makeBestPhenotypeMatchText(humanBuilder, bestMatchesForModel);
                    break;
                case MOUSE:
                    makeBestPhenotypeMatchText(mouseBuilder, bestMatchesForModel);
                    break;
                case FISH:
                    makeBestPhenotypeMatchText(fishBuilder, bestMatchesForModel);
            }
        }
        for (Model model : ppiEvidence) {
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = model.getBestPhenotypeMatchForTerms();
            switch (model.getOrganism()) {
                case HUMAN:
                    DiseaseModel diseaseModel = (DiseaseModel) model;
                    humanPPIBuilder.append("Proximity to " + model.getHumanGeneSymbol() + " associated with " + diseaseModel.getDiseaseTerm() + " (" + diseaseModel.getDiseaseId() + "): ");
                    makeBestPhenotypeMatchText(humanPPIBuilder, bestMatchesForModel);
                    break;
                case MOUSE:
                    mousePPIBuilder.append("Proximity to " + model.getHumanGeneSymbol() + " ");
                    makeBestPhenotypeMatchText(mousePPIBuilder, bestMatchesForModel);
                    break;
                case FISH:
                    fishPPIBuilder.append("Proximity to " + model.getHumanGeneSymbol() + " ");
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

    /**
     */
    @Override
    public String getHTMLCode() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Model model : phenotypeEvidence) {
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = model.getBestPhenotypeMatchForTerms();
            switch (model.getOrganism()) {
                case HUMAN:
                    DiseaseModel diseaseModel = (DiseaseModel) model;
                    String diseaseLink = makeDiseaseLink(diseaseModel.getDiseaseId(), diseaseModel.getDiseaseTerm());
                    stringBuilder.append(String.format("<dl><dt>Phenotypic similarity %.3f to %s associated with %s.</dt>", model.getScore(), diseaseLink, model.getHumanGeneSymbol()));
                    break;
                case MOUSE:
                    stringBuilder.append(String.format("<dl><dt>Phenotypic similarity %.3f to mouse mutant involving <a href=\"http://www.informatics.jax.org/searchtool/Search.do?query=%s\">%s</a>.</dt>", model.getScore(), model.getHumanGeneSymbol(), model.getHumanGeneSymbol()));
                    break;
                case FISH:
                    stringBuilder.append(String.format("<dl><dt>Phenotypic similarity %.3f to zebrafish mutant involving <a href=\"http://zfin.org/action/quicksearch/query?query=%s\">%s</a>.</dt>", model.getScore(), model.getHumanGeneSymbol(), model.getHumanGeneSymbol()));
                    break;
            }
            makeBestPhenotypeMatchHtml(stringBuilder, bestMatchesForModel);
            stringBuilder.append("</dl>");
        }

        for (Model model : ppiEvidence) {
            String stringDbLink = "http://string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + geneSymbol + "%0D" + model.getHumanGeneSymbol() + "&required_score=700&network_flavor=evidence&species=9606&limit=20";

            Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches = model.getBestPhenotypeMatchForTerms();
            switch (model.getOrganism()) {
                case HUMAN:
                    DiseaseModel diseaseModel = (DiseaseModel) model;
                    String diseaseLink = makeDiseaseLink(diseaseModel.getDiseaseId(), diseaseModel.getDiseaseTerm());
                    stringBuilder.append(String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to %s associated with %s.</dt>", stringDbLink, model.getHumanGeneSymbol(), diseaseLink, model.getHumanGeneSymbol()));
                    break;
                case MOUSE:
                    stringBuilder.append(String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to mouse mutant of %s.</dt>", stringDbLink, model.getHumanGeneSymbol(), model.getHumanGeneSymbol()));
                    break;
                case FISH:
                    stringBuilder.append(String.format("<dl><dt>Proximity in <a href=\"%s\">interactome to %s</a> and phenotypic similarity to fish mutant of %s.</dt>", stringDbLink, model.getHumanGeneSymbol(), model.getHumanGeneSymbol()));
                    break;
            }
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
                ", walkerScore=" + walkerScore +
                ", candidateGeneMatch=" + candidateGeneMatch +
                ", queryPhenotypeTerms=" + queryPhenotypeTerms +
                ", phenotypeEvidence=" + phenotypeEvidence +
                ", ppiEvidence=" + ppiEvidence +
                '}';
    }
}
