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

package org.monarchinitiative.exomiser.core.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.Prioritiser;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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
 * {@link PriorityResult PriorityResult}
 * object.
 * <p>
 * There are additionally some prioritization procedures that only can be
 * performed on genes (and not on the individual variants). For instance, there
 * are certain genes such as the Mucins or the Olfactory receptor genes that are
 * often found to have variants in WES data but are known not to be the relevant
 * disease genes. Additionally, filtering for autosomal recessive or dominant
 * patterns in the data is done with this class. This kind of prioritization is
 * done by classes that implement
 * {@link Prioritiser Prioritiser}.
 * Recently, the ability to downweight genes with too many variants (now
 * hardcoded to 5) was added).
 *
 * @author Peter Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.21 (16 January, 2013)
 */
@JsonPropertyOrder({"geneSymbol", "geneIdentifier", "combinedScore", "priorityScore", "variantScore", "pValue", "filterResults", "priorityResults", "compatibleInheritanceModes", "geneScores", "variantEvaluations"})
public class Gene implements Comparable<Gene>, Filterable, Inheritable {

    private final GeneIdentifier geneIdentifier;
    private final String geneSymbol;
    @JsonIgnore //cut down on repeated fields
    private final int entrezGeneId;

    private final Set<FilterType> failedFilterTypes = new LinkedHashSet<>();
    private final Set<FilterType> passedFilterTypes = new LinkedHashSet<>();
    private final Map<FilterType, FilterResult> filterResults = new EnumMap<>(FilterType.class);

    private GeneScore topGeneScore;
    private final Map<ModeOfInheritance, GeneScore> geneScoreMap = new EnumMap<>(ModeOfInheritance.class);

    private final Map<PriorityType, PriorityResult> priorityResultsMap = new EnumMap<>(PriorityType.class);
    /**
     * A list of all of the variants that affect this gene.
     */
    private final List<VariantEvaluation> variantEvaluations = new ArrayList<>();
    private Set<ModeOfInheritance> inheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);

    /**
     * Preferred constructor. Given the {@link GeneIdentifier} contains all the data it can
     *
     * @param geneIdentifier
     */
    public Gene(GeneIdentifier geneIdentifier) {
        this.geneIdentifier = Objects.requireNonNull(geneIdentifier, "GeneIdentifier for a Gene cannot be null");
        if (geneIdentifier.getGeneSymbol().isEmpty()) {
            //we can tolerate empty gene identifiers as sometimes there is none - GeneIdentifier will supply a -1 code
            throw new IllegalArgumentException("GeneIdentifier geneSymbol cannot be empty for a Gene");
        }

        this.geneSymbol = geneIdentifier.getGeneSymbol();
        this.entrezGeneId = geneIdentifier.getEntrezIdAsInteger();

        this.topGeneScore = GeneScore.builder().geneIdentifier(geneIdentifier).build();
    }

    /**
     * Alternate constructor. Construct the gene by providing a gene symbol and Entrez id. Internally this will create a
     * new {@link GeneIdentifier}, but this will not contain the alternate database identifiers (ENSEMBL, HGNC, UCSC) so
     * may result in odd behaviour in the reports. For this reason this constructor should be regarded as a convenient
     * 'hack' for testing purposes.
     *
     * @param geneSymbol
     * @param geneId
     */
    public Gene(String geneSymbol, int geneId) {
        this(GeneIdentifier.builder().geneId(String.valueOf(geneId)).geneSymbol(geneSymbol).entrezId(String.valueOf(geneId)).build());
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
        return geneIdentifier.getGeneSymbol();
    }

    @JsonIgnore
    public String getGeneId() {
        return geneIdentifier.getGeneId();
    }

    public GeneIdentifier getGeneIdentifier() {
        return geneIdentifier;
    }

    /**
     * @return the NCBI Entrez Gene ID associated with this gene (extracted from
     * one of the Variant objects)
     */
    @JsonIgnore
    public int getEntrezGeneID() {
        return geneIdentifier.getEntrezIdAsInteger();
    }

    /**
     * @return the number of {@link Variant} associated with this gene.
     */
    @JsonIgnore
    public int getNumberOfVariants() {
        return variantEvaluations.size();
    }

    public boolean hasVariants() {
        return !variantEvaluations.isEmpty();
    }

    /**
     * This function adds additional variants to the current gene. The variants
     * have been identified by parsing the VCF file.
     *
     * @param variantEvaluation A Variant affecting the current gene.
     */
    public final void addVariant(VariantEvaluation variantEvaluation) {
        Objects.requireNonNull(variantEvaluation);
        addGeneFilterResultsToVariant(variantEvaluation);
        variantEvaluations.add(variantEvaluation);
    }

    private void addGeneFilterResultsToVariant(VariantEvaluation variantEvaluation) {
        for (FilterResult filterResult : filterResults.values()) {
            if (filterResult.getFilterType() != FilterType.INHERITANCE_FILTER) {
                variantEvaluation.addFilterResult(filterResult);
            }
        }
    }

    /**
     * @return A list of all variants in the VCF file that affect this gene.
     */
    public List<VariantEvaluation> getVariantEvaluations() {
        return variantEvaluations;
    }

    @JsonIgnore
    public List<VariantEvaluation> getPassedVariantEvaluations() {
        return variantEvaluations.stream().filter(VariantEvaluation::passedFilters).collect(toList());
    }

    @JsonIgnore
    public List<VariantEvaluation> getNonContributingPassedVariantEvaluations() {
        return variantEvaluations.stream()
                .filter(VariantEvaluation::passedFilters)
                .filter(variantEvaluation -> !variantEvaluation.contributesToGeneScore())
                .collect(toList());
    }

    @Override
    public Set<ModeOfInheritance> getCompatibleInheritanceModes() {
        return inheritanceModes;
    }

    @Override
    public void setCompatibleInheritanceModes(Set<ModeOfInheritance> inheritanceModes) {
        this.inheritanceModes = Collections.unmodifiableSet(EnumSet.copyOf(inheritanceModes));
    }

    /**
     * @param modeOfInheritance
     * @return true if the variants for this gene are compatible with the given
     * {@code ModeOfInheritance} otherwise false.
     */
    @Override
    public boolean isCompatibleWith(ModeOfInheritance modeOfInheritance) {
        return modeOfInheritance == ModeOfInheritance.ANY || inheritanceModes.contains(modeOfInheritance);
    }

    /**
     * @return true if the variants for this gene are compatible with autosomal
     * recessive inheritance, otherwise false.
     */
    @JsonIgnore
    public boolean isCompatibleWithRecessive() {
        return inheritanceModes.contains(ModeOfInheritance.AUTOSOMAL_RECESSIVE) || inheritanceModes.contains(ModeOfInheritance.X_RECESSIVE);
    }

    /**
     * @return true if the variants for this gene are compatible with autosomal
     * dominant inheritance, otherwise false.
     */
    @JsonIgnore
    public boolean isCompatibleWithDominant() {
        return inheritanceModes.contains(ModeOfInheritance.AUTOSOMAL_DOMINANT) || inheritanceModes.contains(ModeOfInheritance.X_DOMINANT);
    }

    /**
     * @return true if the variants for this gene are consistent with X
     * chromosomal inheritance, otherwise false.
     */
    @JsonIgnore
    public boolean isConsistentWithX() {
        return inheritanceModes.contains(ModeOfInheritance.X_RECESSIVE) || inheritanceModes.contains(ModeOfInheritance.X_DOMINANT);
    }

    /**
     * @return true if the gene is X chromosomal, otherwise false.
     */
    @JsonIgnore
    public boolean isXChromosomal() {
        if (variantEvaluations.isEmpty()) {
            return false;
        }
        Variant ve = variantEvaluations.get(0);
        return ve.contigId() == 23;
    }

    @JsonIgnore
    public boolean isYChromosomal() {
        if (variantEvaluations.isEmpty()) {
            return false;
        }
        Variant ve = variantEvaluations.get(0);
        return ve.contigId() == 24;
    }

    /**
     * @param priorityResult Result of a prioritization algorithm
     */
    public void addPriorityResult(PriorityResult priorityResult) {
        Objects.requireNonNull(priorityResult);
        priorityResultsMap.put(priorityResult.getPriorityType(), priorityResult);
    }

    /**
     * @param type {@code PriorityType} representing the priority type
     * @return The result applied by that {@code Priority}.
     */
    @Nullable
    public PriorityResult getPriorityResult(PriorityType type) {
        return priorityResultsMap.get(type);
    }

    /**
     * Type-safe version of getPriorityResult which will return a fully-typed instance of a PriorityResult or null if
     * not present.
     *
     * @param clazz The class of the PriorityResult to search for.
     * @param <T>   They type of PriorityResult class.
     * @return a fully-typed instance of a PriorityResult or null if not present.
     * @since 13.0.0
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends PriorityResult> T getPriorityResult(Class<T> clazz) {
        for (PriorityResult priorityResult : priorityResultsMap.values()) {
            if (clazz.isInstance(priorityResult)) {
                return (T) priorityResult;
            }
        }
        return null;
    }

    /**
     * @return the map of {@code PriorityResult} objects that represent the
     * result of filtering
     */
    public Map<PriorityType, PriorityResult> getPriorityResults() {
        return priorityResultsMap;
    }

    public List<Disease> getAssociatedDiseases() {
        OmimPriorityResult omimPriorityResult = getPriorityResult(OmimPriorityResult.class);
        // This is a bit silly. Known diseases should be added as part of Gene creation.
        return omimPriorityResult == null ? List.of() : omimPriorityResult.getAssociatedDiseases();
    }

    /**
     * @param geneScore
     * @throws NullPointerException if the argument is null
     * @since 10.0.0
     */
    public synchronized void addGeneScore(GeneScore geneScore) {
        Objects.requireNonNull(geneScore);
        geneScoreMap.put(geneScore.getModeOfInheritance(), geneScore);
        topGeneScore = GeneScore.max(topGeneScore, geneScore);
    }

    public synchronized void addGeneScores(Collection<GeneScore> geneScores) {
        Objects.requireNonNull(geneScores);
        for (GeneScore geneScore : geneScores) {
            addGeneScore(geneScore);
        }
    }

    @JsonIgnore
    public GeneScore getTopGeneScore() {
        return topGeneScore;
    }

    public List<GeneScore> getGeneScores() {
        return List.copyOf(geneScoreMap.values());
    }

    /**
     * Returns a list of GeneScores with inheritance modes compatible with the filtered variants compatible modes of
     * inheritance. This will only return a (potentially) populated list once a GeneScorer has been applied to the Gene
     * @since 13.1.0
     * @return A list of {@link GeneScore} with a compatible mode of inheritance.
     */
    public List<GeneScore> getCompatibleGeneScores() {
        // An explanation about the logic here: If the Analysis.inheritanceModes is empty or no MOI dependent analysis
        // step has been run the (compatible) inheritanceModes will be empty. The GeneScorer will always give a gene a
        // GeneScore but in this case it will have an MOI.ANY. So, in the case that no compatible inheritanceModes are
        // present, the ANY GeneScore should be returned here otherwise the ResultsWriters relying on this method
        // (TSV and VCF) will return empty data. See issue https://github.com/exomiser/Exomiser/issues/481
        return inheritanceModes.isEmpty() ? anyMoiScoreOrEmptyList() : geneScoreMap.entrySet().stream()
                .filter(entry -> inheritanceModes.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toUnmodifiableList());
    }

    private List<GeneScore> anyMoiScoreOrEmptyList() {
        GeneScore geneScore = geneScoreMap.get(ModeOfInheritance.ANY);
        return geneScore != null ? List.of(geneScore) : List.of();
    }

    public GeneScore getGeneScoreForMode(ModeOfInheritance modeOfInheritance) {
        Objects.requireNonNull(modeOfInheritance);
        return geneScoreMap.getOrDefault(modeOfInheritance, GeneScore.builder()
                .geneIdentifier(this.geneIdentifier)
                .modeOfInheritance(modeOfInheritance)
                .build()
        );
    }

    /**
     * Gets the priority score for the gene.
     */
    public double getPriorityScore() {
        return topGeneScore.getPhenotypeScore();
    }

    /**
     * Returns the priority score of this gene based on the relevance of the
     * gene as determined by a prioritiser.
     *
     * @return a score that will be used to rank the gene.
     */
    public double getPriorityScoreForMode(ModeOfInheritance modeOfInheritance) {
        Objects.requireNonNull(modeOfInheritance);
        GeneScore geneScore = geneScoreMap.getOrDefault(modeOfInheritance, GeneScore.empty());
        return geneScore.getPhenotypeScore();
    }

    /**
     * Get the variant score for the gene.
     */
    public double getVariantScore() {
        return topGeneScore.getVariantScore();
    }

    /**
     * @return a variant score that will be used to rank the gene.
     */
    public double getVariantScoreForMode(ModeOfInheritance modeOfInheritance) {
        Objects.requireNonNull(modeOfInheritance);
        GeneScore geneScore = geneScoreMap.getOrDefault(modeOfInheritance, GeneScore.empty());
        return geneScore.getVariantScore();
    }

    public double getCombinedScore() {
        return topGeneScore.getCombinedScore();
    }

    @JsonGetter
    public double pValue() {
        return topGeneScore.pValue();
    }

    /**
     * Return the combined score of this gene based on the relevance of the gene
     * (priorityScore) and the predicted effects of the variants (variantScore).
     *
     * @return a combined score that will be used to rank the gene.
     */
    public double getCombinedScoreForMode(ModeOfInheritance modeOfInheritance) {
        Objects.requireNonNull(modeOfInheritance);
        GeneScore geneScore = geneScoreMap.getOrDefault(modeOfInheritance, GeneScore.empty());
        return geneScore.getCombinedScore();
    }

    /**
     * Returns true if the gene has passed all filters and at least one Variant
     * associated with the Gene has also passed all filters. Will also return
     * true if the gene has no variants associated with it.
     */
    @Override
    public boolean passedFilters() {
        return isUnfiltered() || failedFilterTypes.isEmpty() && atLeastOneVariantPassedFilters();
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
        Objects.requireNonNull(filterType);
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

    @Override
    public boolean failedFilter(FilterType filterType) {
        Objects.requireNonNull(filterType);
        // Test variants too? Should return true if *all* variants have failed the filter?
        return failedFilterTypes.contains(filterType) && !passedFilterTypes.contains(filterType);
    }

    private FilterStatus getFilterStatus() {
         if (passedFilters()) {
            return FilterStatus.PASSED;
        }
        return FilterStatus.FAILED;
    }

    @Override
    public boolean addFilterResult(FilterResult filterResult) {
        Objects.requireNonNull(filterResult);
        filterResults.put(filterResult.getFilterType(), filterResult);
        if (filterResult.passed()) {
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
        return Objects.equals(this.geneSymbol, other.geneSymbol) && this.entrezGeneId == other.entrezGeneId;
    }

    /**
     * Sorts a pair of {@code Gene} objects according to the natural {@code GeneScore} ordering for the argument
     * {@code ModeOfInheritance}. This method is same to use for {@code ModeOfInheritance} which the {@code Gene} has no
     * {@code GeneScore} for.
     *
     * @param modeOfInheritance the {@code ModeOfInheritance} under which the genes should be sorted.
     * @return a negative integer, zero, or a positive integer as the first argument is compared to the second according
     *         to the {@code GeneScore} for the {@code ModeOfInheritance} argument.
     * @throws NullPointerException if any argument is null.
     * @since 10.0.0
     */
    public static Comparator<Gene> comparingScoreForInheritanceMode(ModeOfInheritance modeOfInheritance) {
        return (g1, g2) -> {
            GeneScore g1ScoreForMode = g1.getGeneScoreForMode(modeOfInheritance);
            GeneScore g2ScoreForMode = g2.getGeneScoreForMode(modeOfInheritance);
            return GeneScore.compare(g1ScoreForMode, g2ScoreForMode);
        };
    }

    /**
     * Sorts a pair of {@code Gene} objects according to the natural {@code GeneScore} ordering for the argument
     * {@code ModeOfInheritance}. This method is same to use for {@code ModeOfInheritance} which the {@code Gene} has no
     * {@code GeneScore} for. Note the natural ordering of this class is inconsistent with equals.
     *
     * @param otherGene the other {@code Gene} to compare this {@code Gene} with.
     * @return a negative integer, zero, or a positive integer as the first argument is compared to the second according
     *         to their top {@code GeneScore}.
     * @throws NullPointerException if the argument is null.
     */
    @Override
    public int compareTo(Gene otherGene) {
        return GeneScore.compare(topGeneScore, otherGene.topGeneScore);
    }

    @Override
    public String toString() {
        return "Gene{" +
                "geneSymbol='" + geneSymbol + '\'' +
                ", entrezGeneId=" + entrezGeneId +
                ", compatibleWith=" + inheritanceModes +
                ", filterStatus=" + getFilterStatus() +
                ", failedFilterTypes=" + failedFilterTypes +
                ", passedFilterTypes=" + passedFilterTypes +
                ", combinedScore=" + getCombinedScore() +
                ", phenotypeScore=" + getPriorityScore() +
                ", variantScore=" + getVariantScore() +
                ", variants=" + variantEvaluations.size() +
                '}';
    }
}
