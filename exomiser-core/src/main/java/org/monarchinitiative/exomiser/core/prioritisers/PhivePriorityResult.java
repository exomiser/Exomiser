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
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.GeneOrthologModel;

import java.util.Objects;

/**
 * Filter Variants on the basis of OWLSim phenotypic comparisons between the HPO
 * clinical phenotypes associated with the disease being sequenced and MP
 * annotated MGI mouse models. The MGIPhenodigmTriage is created by the
 * MGIPhenodigmFilter, one for each tested variant. The MGIPhenodigmTriage
 * object can be used to ask whether the variant passes the filter, in this case
 * whether it the mouse gene scores greater than the threshold in. If no
 * information is available the filter is not applied (ergo the Variant does not
 * fail the filter).
 * <P>
 * This code was extended on Feb 1, 2013 to show links to the MGI webpage for
 * the model in question.
 *
 * @author Damian Smedley
 * @author Jules Jacobsen
 * @version 0.06 (April 22, 2013).
 */
public class PhivePriorityResult extends AbstractPriorityResult {

    /**
     * The MGI id of the model most similar to the gene being analysed. For
     * instance, the MGI id MGI:101757 corresponding to the webpage
     * {@code http://www.informatics.jax.org/marker/MGI:101757} describes the
     * gene Cfl1 (cofilin 1, non-muscle) and the phenotypic features associated
     * with the several mouse models that have been made to investigate this
     * gene.
     */
    private final GeneModelPhenotypeMatch geneModelPhenotypeMatch;

    /**
     * @param geneSymbol The corresponding gene symbol, e.g., Gfl1
     * @param score the phenodigm score for this gene as calculated by OWLsim. This score indicates the
     * similarity between a humam disease and the phenotype of a genetically
     * modified mouse model.
     * @param geneModelPhenotypeMatch the mouse model evidence for this result.
     */
    public PhivePriorityResult(int geneId, String geneSymbol, double score, GeneModelPhenotypeMatch geneModelPhenotypeMatch) {
        super(PriorityType.PHIVE_PRIORITY, geneId, geneSymbol, score);
        this.geneModelPhenotypeMatch = geneModelPhenotypeMatch;
    }

    public GeneModelPhenotypeMatch getGeneModelPhenotypeMatch() {
        return geneModelPhenotypeMatch;
    }

    /**
     * @return HTML code with score the Phenodigm score for the current gene or
     * a message if no MGI data was found.
     */
    @JsonIgnore
    @Override
    public String getHTMLCode() {
        if (geneModelPhenotypeMatch == null) {
            return "<dl><dt>No mouse model for this gene</dt></dl>";
        } else {
            String link = makeMgiGeneLink((GeneOrthologModel) geneModelPhenotypeMatch.getModel());
            return String.format("<dl><dt>Mouse phenotype data for %s</dt></dl>", link);
        }
    }

    /**
     * This function creates an HTML anchor link for a MGI id, e.g., for
     * MGI:101757 it will create a link to
     * {@code http://www.informatics.jax.org/marker/MGI:101757}.
     */
    private String makeMgiGeneLink(GeneOrthologModel geneOrthologModel) {
        String url = String.format("http://www.informatics.jax.org/marker/%s", geneOrthologModel.getModelGeneId());
        return String.format("<a href=\"%s\">%s</a>", url, geneOrthologModel.getModelGeneSymbol());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhivePriorityResult)) return false;
        if (!super.equals(o)) return false;
        PhivePriorityResult that = (PhivePriorityResult) o;
        return Objects.equals(geneModelPhenotypeMatch, that.geneModelPhenotypeMatch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), geneModelPhenotypeMatch);
    }

    @Override
    public String toString() {
        return "PhivePriorityResult{" +
                "geneId=" + geneId +
                ", geneSymbol='" + geneSymbol + '\'' +
                ", score=" + score +
                ", geneModelPhenotypeMatch=" + geneModelPhenotypeMatch +
                "}";
    }
}
