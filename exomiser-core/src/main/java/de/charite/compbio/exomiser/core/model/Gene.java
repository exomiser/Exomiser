package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.prioritisers.PriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

/**
 * This class represents a Gene in which {@link Variant}
 * objects have been identified by exome sequencing. Note that this class stores
 * information about observed variants and quality scores etc. In contrast, the
 * class {@link de.charite.compbio.jannovar.reference.TranscriptModel} stores
 * information from UCSC about all genes, irrespective of whether we see a
 * variant in the gene by exome sequencing. Therefore, the program uses
 * information from {@link de.charite.compbio.jannovar.reference.TranscriptModel TranscriptModel}
 * object to annotate variants found by exome sequencing, and stores the results
 * of that annotation in {@link Variant Variant} objects. Objects
 * of this class have a list of Variant objects, one for each variant observed
 * in the exome. Additionally, the Gene objects get prioritized for their
 * biomedical relevance to the disease in question, and each such prioritization
 * results in an
 * {@link de.charite.compbio.exomiser.core.prioritisers.PriorityResult PriorityResult}
 * object.
 * <p>
 * There are additionally some prioritization procedures that only can be
 * performed on genes (and not on the individual variants). For instance, there
 * are certain genes such as the Mucins or the Olfactory receptor genes that are
 * often found to have variants in WES data but are known not to be the relevant
 * disease genes. Additionally, filtering for autosomal recessive or dominant
 * patterns in the data is done with this class. This kind of prioritization is
 * done by classes that implement
 * {@link de.charite.compbio.exomiser.core.prioritisers.Prioritiser Prioritiser}.
 * Recently, the ability to downweight genes with too many variants (now
 * hardcoded to 5) was added).
 *
 * @author Peter Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.21 (16 January, 2013)
 */
public class Gene implements Comparable<Gene>, Filterable, Inheritable {

    /**
     * A list of all of the variants that affect this gene.
     */
    private final List<VariantEvaluation> variantEvaluations;

    private final Set<FilterType> failedFilterTypes;
    private final Set<FilterType> passedFilterTypes;
    private final Map<FilterType, FilterResult> filterResults;

    /**
     * A priority score between 0 (irrelevant) and an arbitrary number (highest
     * prediction for a disease gene) reflecting the predicted relevance of this
     * gene for the disease under study by exome sequencing.
     */
    private float priorityScore = 0f;

    /**
     * A score representing the combined pathogenicity predictions for the
     * {@link Variant} objects associated with this gene.
     */
    private float filterScore = 0f;
    /**
     * A score representing the combined filter and priority scores.
     */
    private float combinedScore = 0f;

    private final Map<PriorityType, PriorityResult> priorityResultsMap;
    private Set<ModeOfInheritance> inheritanceModes;
    private final String geneSymbol;
    private final int entrezGeneId;

    /**
     * Construct the gene by providing a gene symbol and Entrez id.
     *
     * @param geneSymbol
     * @param geneId
     */
    public Gene(String geneSymbol, int geneId) {
        this.geneSymbol = geneSymbol;
        this.entrezGeneId = geneId;
        variantEvaluations = new ArrayList();
        inheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);
        failedFilterTypes = new LinkedHashSet<>();
        passedFilterTypes = new LinkedHashSet<>();
        filterResults = new LinkedHashMap<>();
        priorityResultsMap = new LinkedHashMap();
    }

    /**
     * @return the number of {@link Variant} associated with this gene.
     */
    public int getNumberOfVariants() {
        return variantEvaluations.size();
    }

    /**
     * Downrank gene because it has a large numbers of variants (under the
     * assumption that such genes are unlikely to be be true disease genes,
     * rather, by chance say 2 of 20 variants are score as highly pathogenic by
     * polyphen, leading to a false positive call. This method downweights the
     * {@link #filterScore} of this gene, which is the aggregate score for the
     * variants.
     *
     * @param threshold Downweighting occurs for variants that have this number
     * or more variants.
     */
//commented out as this was unused - use the GeneScorer for this sort of function.
//    public void downWeightGeneWithManyVariants(int threshold) {
//        if (this.variantList.size() < threshold) {
//            return;
//        }
//        // Start with downweighting factor of 5%
//        // For every additional variant, add half again to the factor
//        int s = this.variantList.size();
//        float factor = 0.05f;
//        float downweight = 0f;
//        while (s > threshold) {
//            downweight += factor;
//            factor *= 1.5;
//            s--;
//        }
//        if (downweight > 1f) {
//            downweight = 1f;
//        }
//        this.filterScore = this.filterScore * (1f - downweight);
//        /*
//         * filterscore is now at least zero
//         */
//
//    }

    /**
     * This function adds additional variants to the current gene. The variants
     * have been identified by parsing the VCF file.
     *
     * @param var A Variant affecting the current gene.
     */
    public final void addVariant(VariantEvaluation var) {
        addGeneFilterResultsToVariant(var);
        variantEvaluations.add(var);
    }

    private void addGeneFilterResultsToVariant(VariantEvaluation var) {
        filterResults.values().stream().filter(isNotInheritanceFilterResult())
                .forEach(result -> {var.addFilterResult(result);});
    }
    
    private Predicate<FilterResult> isNotInheritanceFilterResult() {
        return filterResult -> {return filterResult.getFilterType() != FilterType.INHERITANCE_FILTER;};
    }
    
    /**
     * @return A list of all variants in the VCF file that affect this gene.
     */
    public List<VariantEvaluation> getVariantEvaluations() {
        return variantEvaluations;
    }

    public List<VariantEvaluation> getPassedVariantEvaluations() {
        return variantEvaluations.stream().filter(VariantEvaluation::passedFilters).collect(toList());
    }

    /**
     * @return the NCBI Entrez Gene ID associated with this gene (extracted from
     * one of the Variant objects)
     */
    public int getEntrezGeneID() {
        return entrezGeneId;
    }

    /**
     * Note that currently, the gene symbols are associated with the Variants.
     * Probably it would be more natural to associate that with a field of this
     * Gene object. For now, leave it as be, and return "-" if this gene has no
     * {@link Variant} objects.
     *
     * @return the symbol associated with this gene (extracted from one of the
     * Variant objects)
     */
    public String getGeneSymbol() {
        return geneSymbol;
    }

    @Override
    public Set<ModeOfInheritance> getInheritanceModes() {
        return inheritanceModes;
    }

    @Override
    public void setInheritanceModes(Set<ModeOfInheritance> inheritanceModes) {
        this.inheritanceModes = inheritanceModes;
    }

    /**
     * @param modeOfInheritance
     * @return true if the variants for this gene are compatible with the given
     * {@code ModeOfInheritance} otherwise false.
     */
    @Override
    public boolean isCompatibleWith(ModeOfInheritance modeOfInheritance) {
        return inheritanceModes.contains(modeOfInheritance);
    }

    /**
     * @return true if the variants for this gene are compatible with autosomal
     * recessive inheritance, otherwise false.
     */
    public boolean isCompatibleWithRecessive() {
        return inheritanceModes.contains(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
    }

    /**
     * @return true if the variants for this gene are compatible with autosomal
     * dominant inheritance, otherwise false.
     */
    public boolean isCompatibleWithDominant() {
        return inheritanceModes.contains(ModeOfInheritance.AUTOSOMAL_DOMINANT);
    }

    /**
     * @return true if the variants for this gene are consistent with X
     * chromosomal inheritance, otherwise false.
     */
    public boolean isConsistentWithX() {
        return inheritanceModes.contains(ModeOfInheritance.X_RECESSIVE);
    }

    /**
     * @return true if the gene is X chromosomal, otherwise false.
     */
    public boolean isXChromosomal() {
        if (variantEvaluations.isEmpty()) {
            return false;
        }
        Variant ve = variantEvaluations.get(0);
        return ve.isXChromosomal();
    }

    public boolean isYChromosomal() {
        if (variantEvaluations.isEmpty()) {
            return false;
        }
        Variant ve = variantEvaluations.get(0);
        return ve.isYChromosomal();
    }

    /**
     * @param priorityResult Result of a prioritization algorithm
     */
    public void addPriorityResult(PriorityResult priorityResult) {
        priorityResultsMap.put(priorityResult.getPriorityType(), priorityResult);
    }

    /**
     * @param type {@code PriorityType} representing the priority type
     * @return The result applied by that {@code Priority}.
     */
    public PriorityResult getPriorityResult(PriorityType type) {
        return priorityResultsMap.get(type);
    }

    /**
     * @return the map of {@code PriorityResult} objects that represent the
     * result of filtering
     */
    public Map<PriorityType, PriorityResult> getPriorityResults() {
        return priorityResultsMap;
    }

    /**
     * Returns the priority score of this gene based on the relevance of the
     * gene as determined by a prioritiser.
     *
     * @return a score that will be used to rank the gene.
     */
    public float getPriorityScore() {
        return priorityScore;
    }

    /**
     * Sets the priority score for the gene.
     *
     * @param score
     */
    public void setPriorityScore(float score) {
        priorityScore = score;
    }

    /**
     * @return a filter score that will be used to rank the gene.
     */
    public float getFilterScore() {
        return this.filterScore;
    }

    /**
     * Set the filtering score for the gene.
     *
     * @param filterScore
     */
    public void setFilterScore(float filterScore) {
        this.filterScore = filterScore;
    }

    /**
     * Return the combined score of this gene based on the relevance of the gene
     * (priorityScore) and the predicted effects of the variants (filterScore).
     *
     * @return a combined score that will be used to rank the gene.
     */
    public float getCombinedScore() {
        return combinedScore;
    }

    public void setCombinedScore(float combinedScore) {
        this.combinedScore = combinedScore;
    }

    /**
     * Returns true if the gene has passed all filters and at least one Variant
     * associated with the Gene has also passed all filters. Will also return
     * true if the gene has no variants associated with it.
     */
    @Override
    public boolean passedFilters() {
        if (isUnfiltered()) {
            return true;
        }
        return failedFilterTypes.isEmpty() && atLeastOneVariantPassedFilters();
    }

    private boolean isUnfiltered() {
        return failedFilterTypes.isEmpty() && variantEvaluations.isEmpty();
    }

    private boolean atLeastOneVariantPassedFilters() {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            if (variantEvaluation.passedFilters()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean passedFilter(FilterType filterType) {
        if (!failedFilterTypes.contains(filterType) && passedFilterTypes.contains(filterType)) {
            return true;
        }
        return atLeastOneVariantPassedFilter(filterType);
    }

    private boolean atLeastOneVariantPassedFilter(FilterType filterType) {
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            if (variantEvaluation.passedFilter(filterType)) {
                return true;
            }
        }
        return false;
    }

    private FilterStatus getFilterStatus() {
         if (passedFilters()) {
            return FilterStatus.PASSED;
        }
        return FilterStatus.FAILED;
    }

    @Override
    public boolean addFilterResult(FilterResult filterResult) {
        filterResults.put(filterResult.getFilterType(), filterResult);
        if (filterResult.getResultStatus() == FilterResultStatus.PASS) {
            return addPassedFilterResult(filterResult);
        }
        return addFailedFilterResult(filterResult);
    }

    private boolean addPassedFilterResult(FilterResult filterResult) {
        passedFilterTypes.add(filterResult.getFilterType());
        return true;
    }

    private boolean addFailedFilterResult(FilterResult filterResult) {
        failedFilterTypes.add(filterResult.getFilterType());
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.geneSymbol);
        hash = 97 * hash + this.entrezGeneId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Gene other = (Gene) obj;
        if (!Objects.equals(this.geneSymbol, other.geneSymbol)) {
            return false;
        }
        if (this.entrezGeneId != other.entrezGeneId) {
            return false;
        }
        return true;
    }

    /**
     * Sort this gene based on priority and filter score. This function
     * satisfies the Interface {@code Comparable}.
     *
     * @param other
     */
    @Override
    public int compareTo(Gene other) {
        float thisScore = this.combinedScore;
        float otherScore = other.combinedScore;
        if (thisScore < otherScore) {
            return 1;
        }
        if (thisScore > otherScore) {
            return -1;
        }
        //if the scores are equal then return an alphabeticised list
        if (thisScore == otherScore) {
            return geneSymbol.compareTo(other.geneSymbol);
        }
        return 0;
    }


    @Override
    public String toString() {
        return String.format("%s entrezId=%d compatibleWith=%s filterScore=%.3f priorityScore=%.3f combinedScore=%.3f variants=%d filterStatus=%s failedFilters=%s passedFilters=%s", geneSymbol, entrezGeneId, inheritanceModes, filterScore, priorityScore, combinedScore, variantEvaluations.size(), getFilterStatus(), failedFilterTypes, passedFilterTypes);
    }

}
