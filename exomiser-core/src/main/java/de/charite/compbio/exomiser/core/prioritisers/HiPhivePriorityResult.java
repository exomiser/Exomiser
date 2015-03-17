package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.PhenotypeMatch;
import de.charite.compbio.exomiser.core.model.PhenotypeTerm;
import de.charite.compbio.exomiser.core.prioritisers.util.Species;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Koehler
 * @version 0.06 (6 January, 2014).
 */
public class HiPhivePriorityResult implements PriorityResult {

    private final String geneSymbol; 
    private double score;
    private final List<PhenotypeTerm> queryPhenotypeTerms;
    private final Map<Species, GeneModel> phenotypeEvidence;
    private final Map<Species, GeneModel> ppiEvidence;
    private final double walkerScore;

    /**
     * @param score The similarity score assigned by the random walk.
     */
    public HiPhivePriorityResult(String geneSymbol, double score, List<PhenotypeTerm> queryPhenotypeTerms, Map<Species, GeneModel> phenotypeEvidence, Map<Species, GeneModel> ppiEvidence, double walkerScore) {
        this.geneSymbol = geneSymbol;
        this.score = score;
        this.queryPhenotypeTerms = queryPhenotypeTerms;
        this.phenotypeEvidence = phenotypeEvidence;
        this.ppiEvidence = ppiEvidence;
        this.walkerScore = walkerScore;
    }

    @Override
    public PriorityType getPriorityType() {
        return PriorityType.HI_PHIVE_PRIORITY;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    @Override
    public float getScore() {
        return (float) score;
    }

    public void setScore(float newScore) {
        this.score = newScore;
    }

    public List<PhenotypeTerm> getQueryPhenotypeTerms() {
        return queryPhenotypeTerms;
    }

    public Map<Species, GeneModel> getPhenotypeEvidence() {
        return phenotypeEvidence;
    }

    public Map<Species, GeneModel> getPpiEvidence() {
        return ppiEvidence;
    }

    /**
     * @return An HTML list with an entry representing the GeneWanderer (Random
     * walk) similarity score.
     * @see exomizer.filter.ITriage#getHTMLCode()
     */
    @Override
    public String getHTMLCode() {
        //return String.format("<ul><li>Similarity score: %.3f %s</li></ul>",this.genewandererScore,this.evidence);
        StringBuilder stringBuilder = new StringBuilder();
        
        for (Species species : phenotypeEvidence.keySet()) {
            GeneModel model = phenotypeEvidence.get(species);
            Map<PhenotypeTerm, PhenotypeMatch> bestMatchesForModel = model.getBestPhenotypeMatchForTerms();
            switch(species) {
                case HUMAN:
                    String diseaseLink = makeDiseaseLink(model.getModelId(), model.getModelSymbol());
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
        }

        for (Species species : ppiEvidence.keySet()) {
            GeneModel model = ppiEvidence.get(species);
            String stringDbLink = "http://string-db.org/newstring_cgi/show_network_section.pl?identifiers=" + geneSymbol + "%0D" + model.getHumanGeneSymbol() + "&required_score=700&network_flavor=evidence&species=9606&limit=20";
            
            Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches = model.getBestPhenotypeMatchForTerms();
            switch(species) {
                case HUMAN:
                    String diseaseLink = makeDiseaseLink(model.getModelId(), model.getModelSymbol());
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
        }
        String html = stringBuilder.toString();
        if (html.isEmpty()) {
            return "<dl><dt>No phenotype or PPI evidence</dt></dl>";
        } 
        return html;
    }

    private void makeBestPhenotypeMatchHtml(StringBuilder stringBuilder, Map<PhenotypeTerm, PhenotypeMatch> bestModelPhenotypeMatches) {
        stringBuilder.append("<dt>Best Phenotype Matches:</dt>");
        for (PhenotypeTerm queryTerm : queryPhenotypeTerms) {
            if (bestModelPhenotypeMatches.containsKey(queryTerm)) {
                PhenotypeMatch match = bestModelPhenotypeMatches.get(queryTerm);
                PhenotypeTerm matchTerm = match.getMatchPhenotype();
                stringBuilder.append(String.format("<dd>%s (%s) - %s (%s)</dd>", queryTerm.getTerm(), queryTerm.getId(), matchTerm.getTerm(), matchTerm.getId()));
            } else {
                stringBuilder.append(String.format("<dd>%s (%s) -</dd>", queryTerm.getTerm(), queryTerm.getId()));
            }
        }
        stringBuilder.append("</dl>");
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


    public float getHumanScore() {
        GeneModel model = phenotypeEvidence.get(Species.HUMAN);
        return returnModelScoreOrZeroIfAbsent(model);
    }

    public float getMouseScore() {
        GeneModel model = phenotypeEvidence.get(Species.MOUSE);
        return returnModelScoreOrZeroIfAbsent(model);
    }

    public float getFishScore() {
        GeneModel model = phenotypeEvidence.get(Species.FISH);
        return returnModelScoreOrZeroIfAbsent(model);
    }

    private float returnModelScoreOrZeroIfAbsent(GeneModel model) {
        if (model != null) {
            return (float) model.getScore();
        }
        return 0f;
    }

    public float getWalkerScore() {
        return (float) this.walkerScore;
    }

}
