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

import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import java.util.List;

/**
 * This class is used to annotate with OMIM data based on the link between the
 * entrez Gene and the OMIM data in the exomizer database table called omim.
 * There is no actual filtering out of variants.
 *
 * @author Peter N Robinson
 * @version 0.08 (9 February, 2014).
 */
public class OMIMPriorityResult extends AbstractPriorityResult {

    private final List<Disease> diseases;

    public OMIMPriorityResult(int geneId,  String geneSymbol, double score, List<Disease> diseases) {
        super(PriorityType.OMIM_PRIORITY, geneId, geneSymbol, score);
        this.diseases = diseases;
    }

    /**
     * @return A list of diseases associated with this gene.
     */
    public List<Disease> getAssociatedDiseases() {
        return diseases;
    }

    /**
     * @return 1.0 if the inheritance pattern of the diseases associated with the
     * gene match the variants, otherwise 0.5. For instance, if the gene has a
     * homozygous variant, than a disease with autosomal recessive inheritance
     * would get a score of 1.0. If the gene only has one het variant, the
     * disease would get a score of 0.5.
     */
    @Override
    public double getScore() {
        return score;
    }

    /**
     * @return A string with HTML code producing a bullet list of OMIM
     * entries/links.
     */
    @Override
    public String getHTMLCode() {
        StringBuilder sb = new StringBuilder("<dl>\n");
        if (hasNoKnownDisease()) {
            return "<dt>No known disease</dt>";
        }
        sb.append("<dt>Known diseases");
        if (this.score < 1f) {
            sb.append(" - observed variants incompatible with mode of inheritance");
        }
        sb.append(":</dt>\n");
        for (Disease disease : diseases) {
            String display = makeDisplayString(disease);
            sb.append("<dd>" + display + "</dd>\n");
        }
        sb.append("</dl>");
        return sb.toString();
    }

    private boolean hasNoKnownDisease() {
        return diseases.isEmpty();
    }

    private String makeDisplayString(Disease disease) {
        String diseaseId = disease.getDiseaseId();
        if (diseaseId.startsWith("OMIM:")){
            return makeOmimDisplayString(disease);
        }
        if (diseaseId.startsWith("ORPHANET:")) {
            return makeOrphanetDisplayString(disease);
        }
        //default return non-formatted disease id
        return String.format("%s %s",  disease.getDiseaseId(), disease.getDiseaseName());
    }

    private String makeOmimDisplayString(Disease disease) {
        //OMIM:101600 Pfeiffer syndrome - autosomal dominant
        //OMIM:10111 Other thing (non-disease)
        //OMIM:10111 Other thing, X chromosomal (susceptibility)
        String[] phenParts = disease.getDiseaseId().split(":");
        String mimPhenUrl = String.join("", "http://www.omim.org/entry/", phenParts[1]);
        String href = href(mimPhenUrl, disease.getDiseaseId());

        InheritanceMode inheritanceMode = disease.getInheritanceMode();
        Disease.DiseaseType diseaseType = disease.getDiseaseType();
        if (diseaseType == Disease.DiseaseType.DISEASE) {
            return String.format("%s %s - %s",  href, disease.getDiseaseName(), inheritanceMode.getTerm());
        }
        return String.format("%s %s (%s)",  href, disease.getDiseaseName(), diseaseType.getValue());

    }

    private String makeOrphanetDisplayString(Disease disease) {
        String[] orphaParts = disease.getDiseaseId().split(":");
        String url = String.join("", "http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=", orphaParts[1]);
        String href = href(url, disease.getDiseaseId());
        return String.join(" ", href, disease.getDiseaseName());
    }

    private String href(String url, String displayText) {
        return String.format("<a href=\"%s\">%s</a>", url, displayText);
    }


    @Override
    public String toString() {
        return "OMIMPriorityResult{" +
                "geneId=" + geneId +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", score=" + score +
                ", diseases=" + diseases +
                "} ";
    }
}
