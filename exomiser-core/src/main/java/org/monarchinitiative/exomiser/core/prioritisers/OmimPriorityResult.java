/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class for holding the results of the prioritisation of a {@link org.monarchinitiative.exomiser.core.model.Gene} by
 * the {@link OmimPriority}.
 *
 * @author Peter N Robinson
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @version 0.08 (9 February, 2014).
 */
public class OmimPriorityResult extends AbstractPriorityResult {

    private final List<Disease> diseases;
    private final Map<ModeOfInheritance, Double> scoresByMode;

    public OmimPriorityResult(int geneId, String geneSymbol, double score, List<Disease> diseases, Map<ModeOfInheritance, Double> scoresByMode) {
        super(PriorityType.OMIM_PRIORITY, geneId, geneSymbol, score);
        this.diseases = ImmutableList.copyOf(diseases);
        this.scoresByMode = Maps.immutableEnumMap(scoresByMode);
    }

    /**
     * @return A list of diseases associated with this gene.
     */
    public List<Disease> getAssociatedDiseases() {
        return diseases;
    }

    /**
     * Returns the map of {@code ModeOfInheritance} to prioritiser score. If the mode of inheritance hasn't been
     * scored it will be missing. This score is intended to be used as a modifier for another phenotype specific score,
     * in conjunction with a specific {@code ModeOfInheritance}, as a means of prioritising a gene based on the compatible
     * modes of inheritance for the argument gene and the modes of inheritance for known diseases associated with that gene.
     *
     * @return the map of {@code ModeOfInheritance} and the prioritiser modifier score for that mode.
     * @since 10.0.0
     */
    public Map<ModeOfInheritance, Double> getScoresByMode() {
        return scoresByMode;
    }

    /**
     * Returns the prioritiser score for this specific mode of inheritance. If the mode of inheritance hasn't been
     * scored a default value of 1.0 will be returned. This score is intended to be used as a modifier for another
     * phenotype specific score as a means of prioritising a gene based on the compatible modes of inheritance for the
     * argument gene and the modes of inheritance for known diseases associated with that gene.
     *
     * @param modeOfInheritance The {@code ModeOfInheritance} under which you want the score.
     * @return the value 0.0, 0.5 or 1.0
     * @since 10.0.0
     */
    public double getScoreForMode(ModeOfInheritance modeOfInheritance) {
        return scoresByMode.getOrDefault(modeOfInheritance, 1d);
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
        if (diseaseId.startsWith("OMIM:")) {
            return makeOmimDisplayString(disease);
        }
        if (diseaseId.startsWith("ORPHA:")) {
            return makeOrphanetDisplayString(disease);
        }
        //default return non-formatted disease id
        return String.format("%s %s", disease.getDiseaseId(), disease.getDiseaseName());
    }

    private String makeOmimDisplayString(Disease disease) {
        //OMIM:101600 Pfeiffer syndrome - autosomal dominant
        //OMIM:10111 Other thing (non-disease)
        //OMIM:10111 Other thing, X chromosomal (susceptibility)
        String[] idParts = disease.getDiseaseId().split(":");
        String url = String.join("", "http://www.omim.org/entry/", idParts[1]);
        String href = href(url, disease.getDiseaseId());
        return diseaseLinkAndDisplayName(disease, href);
    }

    private String makeOrphanetDisplayString(Disease disease) {
        String[] idParts = disease.getDiseaseId().split(":");
        String url = String.join("", "http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=", idParts[1]);
        String href = href(url, disease.getDiseaseId());
        return diseaseLinkAndDisplayName(disease, href);
    }

    private String diseaseLinkAndDisplayName(Disease disease, String href) {
        InheritanceMode inheritanceMode = disease.getInheritanceMode();
        Disease.DiseaseType diseaseType = disease.getDiseaseType();
        if (diseaseType == Disease.DiseaseType.DISEASE) {
            return String.format("%s %s - %s", href, disease.getDiseaseName(), inheritanceMode.getTerm());
        }
        return String.format("%s %s (%s)", href, disease.getDiseaseName(), diseaseType.getValue());
    }

    private String href(String url, String displayText) {
        return String.format("<a href=\"%s\">%s</a>", url, displayText);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OmimPriorityResult that = (OmimPriorityResult) o;
        return Objects.equals(diseases, that.diseases) &&
                Objects.equals(scoresByMode, that.scoresByMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), diseases, scoresByMode);
    }

    @Override
    public String toString() {
        return "OMIMPriorityResult{" +
                "geneId=" + geneId +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", score=" + score +
                ", scoresByMode=" + scoresByMode +
                ", diseases=" + diseases +
                '}';
    }
}
