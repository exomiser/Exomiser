package org.monarchinitiative.exomiser.core.writers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jackson Mixin (http://www.cowtowncoder.com/blog/archives/2009/08/entry_305.html) class to add JSON serialisation to
 * {@link Gene} fields.
 *
 * @since 13.0.0
 */
@JsonPropertyOrder({"geneSymbol", "geneIdentifier", "combinedScore", "priorityScore", "variantScore", "pValue", "filterResults", "priorityResults", "compatibleInheritanceModes", "geneScores", "variantEvaluations"})
public abstract class JsonGeneMixin {
    @JsonProperty
    public abstract String geneSymbol();
    @JsonProperty
    public abstract GeneIdentifier geneIdentifier();
    @JsonProperty
    public abstract double combinedScore();
    @JsonProperty
    public abstract double priorityScore();
    @JsonProperty
    public abstract double variantScore();
    @JsonProperty
    public abstract double pValue();
    @JsonProperty
    public abstract Map<FilterType, FilterResult> filterResults();
    @JsonProperty
    public abstract Map<PriorityType, PriorityResult> priorityResults();
    @JsonPropertyOrder
    public abstract Set<ModeOfInheritance> compatibleInheritanceModes();
    @JsonProperty
    public abstract Map<ModeOfInheritance, GeneScore> geneScores();
    @JsonProperty
    public abstract List<VariantEvaluation> variantEvaluations();
}
