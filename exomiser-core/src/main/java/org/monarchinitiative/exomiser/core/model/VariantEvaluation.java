/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.VariantEffectPathogenicityScore;

import java.util.*;

/**
 * This class is a wrapper for the {@code Variant} class from the jannovar
 * hierarchy, and additionally includes all of the information on pathogenicity
 * and frequency that is added to each variant by the Exomizer program.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Peter Robinson <peter.robinson@charite.de>
 */
@JsonPropertyOrder({"genomeAssembly", "chromosomeName", "chromosome", "position", "ref", "alt", "phredScore", "variantEffect", "nonCodingVariant", "whiteListed", "filterStatus", "variantScore", "frequencyScore", "pathogenicityScore", "predictedPathogenic", "passedFilterTypes", "failedFilterTypes", "frequencyData", "pathogenicityData", "compatibleInheritanceModes", "contributingInheritanceModes", "transcriptAnnotations"})
public class VariantEvaluation implements Comparable<VariantEvaluation>, Filterable, Inheritable, Variant {

    //threshold over which a variant effect score is considered pathogenic
    private static final float DEFAULT_PATHOGENICITY_THRESHOLD = 0.5f;

    // HTSJDK {@link VariantContext} instance of this allele
    @JsonIgnore
    private final VariantContext variantContext;
//    private final String vcfString;

    // numeric index of the alternative allele in {@link #vc}.
    private final int altAlleleId;

    // VariantCoordinates variables - these are a minimal requirement for describing a variant
    private final GenomeAssembly genomeAssembly;
    private final int chr;
    private final String chromosomeName;
    private final int pos;
    private final String ref;
    private final String alt;

    // Variant variables, for a richer more VCF-like experience
    private final double phredScore;

    @JsonIgnore
    // IMPORTANT! This map *MUST* be an ordered map
    private Map<String, SampleGenotype> sampleGenotypes;

    //VariantAnnotation
    private VariantEffect variantEffect;
    private List<TranscriptAnnotation> annotations;
    @JsonIgnore
    private String geneSymbol;
    @JsonIgnore
    private String geneId;

    // results from filters
    // mutable
    private final Set<FilterType> passedFilterTypes;
    private final Set<FilterType> failedFilterTypes;

    // score-related stuff - these are mutable
    private boolean whiteListed;
    private FrequencyData frequencyData;
    private PathogenicityData pathogenicityData;
    @JsonProperty("contributingInheritanceModes")
    private Set<ModeOfInheritance> contributingModes = EnumSet.noneOf(ModeOfInheritance.class);
    private Set<ModeOfInheritance> compatibleInheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);

    private VariantEvaluation(Builder builder) {
        genomeAssembly = builder.genomeAssembly;
        chr = builder.chr;
        chromosomeName = builder.chromosomeName;
        pos = builder.pos;
        ref = builder.ref;
        alt = builder.alt;

        phredScore = builder.phredScore;
        variantEffect = builder.variantEffect;
        annotations = ImmutableList.copyOf(builder.annotations);
        geneSymbol = builder.geneSymbol;
        geneId = builder.geneId;

//        vcfString = builder.vcfString;
        variantContext = builder.variantContext;
        altAlleleId = builder.altAlleleId;
        // IMPORTANT! This map *MUST* be an ordered map
        sampleGenotypes = ImmutableMap.copyOf(builder.sampleGenotypes);

        passedFilterTypes = EnumSet.copyOf(builder.passedFilterTypes);
        failedFilterTypes = EnumSet.copyOf(builder.failedFilterTypes);

        whiteListed = builder.whiteListed;
        frequencyData = builder.frequencyData;
        pathogenicityData = builder.pathogenicityData;
    }

    @Override
    public GenomeAssembly getGenomeAssembly() {
        return genomeAssembly;
    }

    /**
     * @return an integer representing the chromosome. 1-22 are obvious,
     * chrX=23, ChrY=24, ChrM=25.
     */
    @Override
    public int getChromosome() {
        return chr;
    }

    /**
     * @return a String such "4" or "X" in the case of chromosome 23
     */
    @Override
    public String getChromosomeName() {
        return chromosomeName;
    }

    /**
     * @return Return the 1-based start position of the variant on its
     * chromosome.
     */
    @Override
    public int getPosition() {
        return pos;
    }

    /**
     * @return reference allele, or "-" in case of insertions.
     */
    @Override
    public String getRef() {
        return ref;
    }

    /**
     * @return alternative allele, or "-" in case of deletions.
     */
    @Override
    public String getAlt() {
        return alt;
    }

    public VariantContext getVariantContext() {
//        VCFCodec vcfCodec = VcfCodecs.decoder(sampleGenotypes.keySet());
//        return vcfCodec.decode(vcfString);
        return variantContext;
    }

    public int getAltAlleleId() {
        return altAlleleId;
    }

    public double getPhredScore() {
        return phredScore;
    }

    /**
     * @return the most prevalent {@link VariantEffect} such as {@link VariantEffect#MISSENSE_VARIANT},
     * {@link VariantEffect#FRAMESHIFT_ELONGATION}, etc., or <code>null</code>
     * if there is no annotated effect.
     */
    @Override
    public VariantEffect getVariantEffect() {
        return variantEffect;
    }
    
    public void setVariantEffect (VariantEffect ve){
        variantEffect = ve;
    }

    /**
     * @return the gene symbol associated with the variant.
     */
    @Override
    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String symbol) {
        geneSymbol = symbol;
    }

    @Override
    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    /**
     * This function returns a list of all of the
     * {@link de.charite.compbio.jannovar.annotation.Annotation Annotation} objects that have been
     * associated with the current variant. This function can be called if
     * client code wants to display one line for each affected transcript, e.g.,
     * <ul>
     * <li>LTF(uc003cpr.3:exon5:c.30_31insAAG:p.R10delinsRR)
     * <li>LTF(uc003cpq.3:exon2:c.69_70insAAG:p.R23delinsRR)
     * <li>LTF(uc010hjh.3:exon2:c.69_70insAAG:p.R23delinsRR)
     * </ul>
     * <p>
     */
    @Override
    public List<TranscriptAnnotation> getTranscriptAnnotations() {
        return annotations;
    }
    
    public void setAnnotations(List<TranscriptAnnotation> annotations) {
        this.annotations = annotations;
    }

    @Override
    public boolean hasTranscriptAnnotations() {
        return !annotations.isEmpty();
    }

    /**
     * @return a String such as chr6:g.29911092G>T
     */
    // SPDI?
    @JsonIgnore
    public String getHgvsGenome() {
        return chr + ":g." + pos + ref + ">" + alt;
    }

    @JsonIgnore
    public String getGenotypeString() {
        List<String> genotypeStrings = new ArrayList<>(sampleGenotypes.size());

        for (SampleGenotype sampleGenotype : sampleGenotypes.values()) {
            if (sampleGenotype.isEmpty()) {
                genotypeStrings.add(SampleGenotype.noCall().toString());
            } else {
                genotypeStrings.add(sampleGenotype.toString());
            }
        }
        return String.join(":", genotypeStrings);
    }

    /**
     * @return A map of sample ids and their corresponding {@link SampleGenotype}
     * @since 11.0.0
     */
    public Map<String, SampleGenotype> getSampleGenotypes() {
        return sampleGenotypes;
    }

    /**
     * Returns the {@link SampleGenotype} for a given sample identifier. If the identifier is not found an empty
     * {@link SampleGenotype} will be returned.
     *
     * @param sampleId sample id of the individual of interest
     * @return the {@link SampleGenotype} of the individual for this variant, or an empty {@link SampleGenotype} if the
     * sample is not represented
     * @since 11.0.0
     */
    public SampleGenotype getSampleGenotype(String sampleId) {
        return sampleGenotypes.getOrDefault(sampleId, SampleGenotype.empty());
    }

    /**
     * This method is used to add a {@code FilterResult} object to this variant.
     * Such objects represent the results of running the variant through a {@code Filter}.
     *
     * @param filterResult
     * @return
     */
    @Override
    public boolean addFilterResult(FilterResult filterResult) {
        if (filterResult.passed()) {
            return addPassedFilterResult(filterResult);
        }
        return addFailedFilterResult(filterResult);
    }

    private synchronized boolean addPassedFilterResult(FilterResult filterResult) {
        passedFilterTypes.add(filterResult.getFilterType());
        return true;
    }

    private synchronized boolean addFailedFilterResult(FilterResult filterResult) {
        failedFilterTypes.add(filterResult.getFilterType());
        return false;
    }

    /**
     * @return the set of FilterResult objects that represent the result of
     * filtering
     */
    public Set<FilterType> getPassedFilterTypes() {
        return EnumSet.copyOf(passedFilterTypes);
    }

    /**
     * @return the Set of {@code FilterType} which the {@code VariantEvaluation}
     * failed to pass.
     */
    public Set<FilterType> getFailedFilterTypes() {
        return EnumSet.copyOf(failedFilterTypes);
    }

    /**
     * Under some inheritance modes a variant should not pass, but others it will. For example if a variant is relatively
     * common it could pass as being compatible under a compound heterozygous model, but might be too common to be
     * considered as a candidate under an autosomal dominant model. Hence we need to be able to check whether a variant
     * passed under a specific mode of inheritance otherwise alleles will be reported as having passed under the wrong mode.
     *
     * @param modeOfInheritance the mode of inheritance under which the failed filters are required.
     * @return a set of failed {@code FilterType} for the variant under the {@code ModeOfInheritance} input model.
     */
    public synchronized Set<FilterType> getFailedFilterTypesForMode(ModeOfInheritance modeOfInheritance){
        EnumSet<FilterType> failedFiltersCopy = EnumSet.copyOf(failedFilterTypes);
        if (!isCompatibleWith(modeOfInheritance)) {
            failedFiltersCopy.add(FilterType.INHERITANCE_FILTER);
            return failedFiltersCopy;
        }
        return failedFiltersCopy;
    }

    /**
     * We're making the assumption that all variants will pass a filter, so if
     * no filters have been applied, this method will return true. Once a
     * {@link VariantEvaluation} has been filtered this will return true until
     * the {@link VariantEvaluation} has failed a filter.
     * <p>
     * Note: This may change so that passed/failed/unfiltered can only ever be
     * true for one status.
     *
     * @return
     */
    @Override
    public synchronized boolean passedFilters() {
        return failedFilterTypes.isEmpty();
    }

    @Override
    public synchronized boolean passedFilter(FilterType filterType) {
        return !failedFilterTypes.contains(filterType) && passedFilterTypes.contains(filterType);
    }

    private synchronized boolean isUnFiltered() {
        return failedFilterTypes.isEmpty() && passedFilterTypes.isEmpty();
    }

    public FilterStatus getFilterStatus() {
        if (isUnFiltered()) {
            return FilterStatus.UNFILTERED;
        }
        if (passedFilters()) {
            return FilterStatus.PASSED;
        }
        return FilterStatus.FAILED;
    }

    public FilterStatus getFilterStatusForMode(ModeOfInheritance modeOfInheritance) {
        if (isUnFiltered()) {
            return FilterStatus.UNFILTERED;
        }
        if (isCompatibleWith(modeOfInheritance) && passedFilters()) {
            return FilterStatus.PASSED;
        }
        return FilterStatus.FAILED;
    }

    /**
     * Returns the variant score (prediction of the pathogenicity
     * and relevance of the Variant) by combining the frequency and pathogenicity scores for this variant.
     *
     * @return a score between 0 and 1
     */
    public float getVariantScore() {
        return whiteListed ? 1f : getFrequencyScore() * getPathogenicityScore();
    }

    /**
     * @return a score between 0 and 1
     */
    public float getFrequencyScore() {
        return whiteListed ? 1f : frequencyData.getScore();
    }

    /**
     * Some variants such as splice site variants, are assumed to be pathogenic. At the moment no particular
     * software is used to evaluate this, we merely take the variant class from the Jannovar code and assign a score.
     *
     * Note that we use results of filtering to remove Variants that are predicted to be simply non-pathogenic. However,
     * amongst variants predicted to be potentially pathogenic, there are different strengths of prediction, which is
     * what this score tries to reflect.
     *
     * For missense mutations, we use the predictions of MutationTaster, polyphen, and SIFT taken from the dbNSFP
     * project, if present, or otherwise return a default score.
     *
     * The score returned here is therefore an overall pathogenicity score defined on the basis of
     * "medical genetic intuition".

     * @return a score between 0 and 1
     */
    public float getPathogenicityScore() {
        if (whiteListed) {
            return 1f;
        }
        float predictedScore = pathogenicityData.getScore();
        float variantEffectScore = VariantEffectPathogenicityScore.getPathogenicityScoreOf(variantEffect);
        if (variantEffect == VariantEffect.MISSENSE_VARIANT) {
            // CAUTION! REVEL scores tend to be more nuanced and frequently lower thant either the default variant effect score
            // or the other predicted path scores, yet apparently are more concordant with ClinVar. For this reason it might be
            // best to check for a REVEL prediction and defer wholly to that if present rather than do the following.

            // In version 10.1.0 the MISSENSE variant constraint was removed from the defaultPathogenicityDao and variantDataServiceImpl
            // so that non-missense variants would get ClinVar annotations and other non-synonymous path scores from the variant store.
            // In order that missense variants are not over-represented if they have poor predicted scores this clause was added here.
            return pathogenicityData.hasPredictedScore() ? predictedScore : variantEffectScore;
        } else {
            return Math.max(predictedScore, variantEffectScore);
        }
    }

    /**
     * @return true or false depending on whether the variant effect is considered pathogenic. Pathogenoic variants are
     * considered to be those with a pathogenicity score greater than 0.5. Missense variants will always return true.
     */
    public boolean isPredictedPathogenic() {
        if (whiteListed) {
            return true;
        }
        if (variantEffect == VariantEffect.MISSENSE_VARIANT) {
            // We're making the assumption that a missense variant is always potentially pathogenic.
            // Given the prediction scores are predictions, they could fall below the default threshold so
            // we'll leave it up to the user to decide.
            // TODO: This might actually be too permissive. Might be best to return pathogenicityData.isPredictedPathogenic()
            //  which will utilise thresholds for the included scores.
            return true;
        } else {
            return getPathogenicityScore() >= DEFAULT_PATHOGENICITY_THRESHOLD;
        }
    }

    public FrequencyData getFrequencyData() {
        return frequencyData;
    }

    public void setFrequencyData(FrequencyData frequencyData) {
        this.frequencyData = frequencyData;
    }

    public PathogenicityData getPathogenicityData() {
        return pathogenicityData;
    }

    public void setPathogenicityData(PathogenicityData pathogenicityData) {
        this.pathogenicityData = pathogenicityData;
    }

    /**
     * @since 12.0.0
     * @return true if the VariantEvaluation has been marked as whitelisted
     */
    public boolean isWhiteListed() {
        return whiteListed;
    }

    /**
     * @since 12.0.0
     */
    public void setWhiteListed(boolean whiteListed) {
        this.whiteListed = whiteListed;
    }

    public void setContributesToGeneScoreUnderMode(ModeOfInheritance modeOfInheritance) {
        contributingModes.add(modeOfInheritance);
    }

    public boolean contributesToGeneScore() {
        return !contributingModes.isEmpty();
    }

    public boolean contributesToGeneScoreUnderMode(ModeOfInheritance modeOfInheritance) {
        return modeOfInheritance == ModeOfInheritance.ANY && !contributingModes.isEmpty() || contributingModes.contains(modeOfInheritance);
    }

    @Override
    public void setCompatibleInheritanceModes(Set<ModeOfInheritance> compatibleModes) {
        if (compatibleModes.isEmpty()) {
            compatibleInheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);
        } else {
            this.compatibleInheritanceModes = EnumSet.copyOf(compatibleModes);
        }
    }
    
    @Override
    public Set<ModeOfInheritance> getCompatibleInheritanceModes() {
        return EnumSet.copyOf(compatibleInheritanceModes);
    }

    @Override
    public boolean isCompatibleWith(ModeOfInheritance modeOfInheritance) {
        return modeOfInheritance == ModeOfInheritance.ANY || compatibleInheritanceModes.contains(modeOfInheritance);
    }
    
    /**
     * Sorts variants according to their natural ordering of genome position. Variants are sorted according to
     * chromosome number, chromosome position, reference sequence then alternative sequence.
     *
     * @param other
     * @return comparator score consistent with equals.
     */
    @Override
    public int compareTo(VariantEvaluation other) {
        if (this.chr != other.chr) {
            return Integer.compare(this.chr, other.chr);
        }
        if (this.pos != other.pos) {
            return Integer.compare(this.pos, other.pos);
        }
        if (!this.ref.equals(other.ref)) {
            return this.ref.compareTo(other.ref);
        }
        return this.alt.compareTo(other.alt);
    }

    public static class RankBasedComparator implements Comparator<VariantEvaluation> {

        @Override
        public int compare(VariantEvaluation v1, VariantEvaluation v2) {
            return compareByRank(v1, v2);
        }
    }

    public static int compareByRank(VariantEvaluation some, VariantEvaluation other) {
        if (some.contributesToGeneScore() != other.contributesToGeneScore()) {
            return -Boolean.compare(some.contributesToGeneScore(), other.contributesToGeneScore());
        }
        float thisScore = some.getVariantScore();
        float otherScore = other.getVariantScore();
        if (thisScore != otherScore) {
            return -Float.compare(thisScore, otherScore);
        }
        if (some.chr != other.chr) {
            return Integer.compare(some.chr, other.chr);
        }
        if (some.pos != other.pos) {
            return Integer.compare(some.pos, other.pos);
        }
        if (!some.ref.equals(other.ref)) {
            return some.ref.compareTo(other.ref);
        }
        return some.alt.compareTo(other.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomeAssembly, chr, pos, ref, alt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VariantEvaluation other = (VariantEvaluation) obj;
        if (this.genomeAssembly != other.genomeAssembly) {
            return false;
        }
        if (this.chr != other.chr) {
            return false;
        }
        if (this.pos != other.pos) {
            return false;
        }
        return Objects.equals(this.ref, other.ref) && Objects.equals(this.alt, other.alt);
    }

    public String toString() {
        // expose frequency and pathogenicity scores?
        if(contributesToGeneScore()) {
            //Add a star to the output string between the variantEffect and the score
            return "VariantEvaluation{assembly=" + genomeAssembly + " chr=" + chr + " pos=" + pos + " ref=" + ref + " alt=" + alt + " qual=" + phredScore + " " + variantEffect + " * score=" + getVariantScore() + " " + getFilterStatus() + " failedFilters=" + failedFilterTypes + " passedFilters=" + passedFilterTypes
                    + " compatibleWith=" + compatibleInheritanceModes + " sampleGenotypes=" + sampleGenotypes + "}";
        }
        return "VariantEvaluation{assembly=" + genomeAssembly + " chr=" + chr + " pos=" + pos + " ref=" + ref + " alt=" + alt + " qual=" + phredScore + " " + variantEffect + " score=" + getVariantScore() + " " + getFilterStatus() + " failedFilters=" + failedFilterTypes + " passedFilters=" + passedFilterTypes
                + " compatibleWith=" + compatibleInheritanceModes + " sampleGenotypes=" + sampleGenotypes + "}";
    }

    public static Builder builder(int chr, int pos, String ref, String alt) {
        return new Builder(chr, pos, ref, alt);
    }

    /**
     * Builder class for producing a valid VariantEvaluation.
     */
    public static class Builder {

        private GenomeAssembly genomeAssembly = GenomeAssembly.HG19;
        private int chr;
        private String chromosomeName;
        private int pos;
        private String ref;
        private String alt;

        private double phredScore = 0;

        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;
        private List<TranscriptAnnotation> annotations = Collections.emptyList();
        private String geneSymbol = ".";
        private String geneId = GeneIdentifier.EMPTY_FIELD;

        private String vcfString;
        private VariantContext variantContext;
        private int altAlleleId;
        private Map<String,SampleGenotype> sampleGenotypes = ImmutableMap.of();

        private PathogenicityData pathogenicityData = PathogenicityData.empty();
        private FrequencyData frequencyData = FrequencyData.empty();

        private boolean whiteListed = false;
        private final Set<FilterType> passedFilterTypes = EnumSet.noneOf(FilterType.class);
        private final Set<FilterType> failedFilterTypes = EnumSet.noneOf(FilterType.class);

        private static final String DEFAULT_SAMPLE_NAME = SampleIdentifier.defaultSample().getId();
        // These shouldn't be used in production, but in cases where there is no genotype this will prevent NullPointer and ArrayIndexOutOfBounds Exceptions
        static final ImmutableMap<String, SampleGenotype> SINGLE_SAMPLE_HET_GENOTYPE = ImmutableMap.of(DEFAULT_SAMPLE_NAME, SampleGenotype.het());

        /**
         * Creates a minimal variant
         *
         * @param chr
         * @param pos
         * @param ref
         * @param alt
         */
        private Builder(int chr, int pos, String ref, String alt) {
            this.chr = chr;
            this.pos = pos;
            this.ref = ref;
            this.alt = alt;
        }

        public Builder genomeAssembly(GenomeAssembly genomeAssembly) {
            this.genomeAssembly = genomeAssembly;
            return this;
        }

        public Builder genomeAssembly(String genomeAssembly) {
            this.genomeAssembly = GenomeAssembly.fromValue(genomeAssembly);
            return this;
        }

        public Builder chromosomeName(String chromosomeName) {
            this.chromosomeName = chromosomeName;
            return this;
        }

        /**
         * Safety method to handle creating the chromosome name in cases where
         * the name is not explicitly set. This should happen in the
         * VariantFactory, but for testing we're happy with a sensible default
         * value. It's not critical, but is nice to prevent a lot of silly
         * duplicate code.
         *
         * @param chr
         * @return
         */
        private String buildChromosomeName(int chr) {
            switch (chr) {
                case 23:
                    return "X";
                case 24:
                    return "Y";
                case 25:
                    return "MT";
                default:
                    return String.valueOf(chr);
            }
        }

        public Builder variantContext(VariantContext variantContext) {
            this.variantContext = variantContext;
            return this;
        }

        public Builder altAlleleId(int altAlleleId) {
            this.altAlleleId = altAlleleId;
            return this;
        }

        public Builder quality(double phredScore) {
            this.phredScore = phredScore;
            return this;
        }

        //TODO - this is error-prone as it is possible to supply a HashMap which would screw-up the fact that we're
        // relying on the inherently ORDERED ImmutableMap implementation
        public Builder sampleGenotypes(Map<String, SampleGenotype> sampleGenotypes) {
            Objects.requireNonNull(sampleGenotypes);
            this.sampleGenotypes = sampleGenotypes;
            return this;
        }

        public Builder variantEffect(VariantEffect variantEffect) {
            Objects.requireNonNull(variantEffect);
            this.variantEffect = variantEffect;
            return this;
        }

        public Builder annotations(List<TranscriptAnnotation> annotations) {
            Objects.requireNonNull(annotations);
            this.annotations = annotations;
            return this;
        }

        public Builder geneSymbol(String geneSymbol) {
            Objects.requireNonNull(geneSymbol);
            if (geneSymbol.isEmpty()) {
                throw new IllegalArgumentException("Variant gene symbol cannot be empty");
            }
            this.geneSymbol = inputOrfirstValueInCommaSeparatedString(geneSymbol);
            return this;
        }

        private String inputOrfirstValueInCommaSeparatedString(String geneSymbol) {
            int commaIndex = geneSymbol.indexOf(',');
            return (commaIndex > -1) ? geneSymbol.substring(0, commaIndex) : geneSymbol;
        }

        public Builder geneId(String geneId) {
            this.geneId = geneId;
            return this;
        }

        public Builder whiteListed(boolean whiteListed) {
            this.whiteListed = whiteListed;
            return this;
        }

        public Builder pathogenicityData(PathogenicityData pathogenicityData) {
            this.pathogenicityData = pathogenicityData;
            return this;
        }

        public Builder frequencyData(FrequencyData frequencyData) {
            this.frequencyData = frequencyData;
            return this;
        }

        public Builder filterResults(FilterResult... filterResults) {
            return filterResults(Arrays.asList(filterResults));
        }

        public Builder filterResults(Collection<FilterResult> filterResults) {
            for (FilterResult filterResult : filterResults) {
                if (filterResult.passed()) {
                    this.passedFilterTypes.add(filterResult.getFilterType());
                } else {
                    this.failedFilterTypes.add(filterResult.getFilterType());
                }
            }
            return this;
        }

        public VariantEvaluation build() {
            if (chromosomeName == null || chromosomeName.isEmpty()) {
                chromosomeName = buildChromosomeName(chr);
            }

            if (variantContext == null) {
                // We don't check that the variant context agrees with the coordinates here as the variant context could
                // have been split into different allelic variants so the positions and alleles could differ.
                variantContext = buildVariantContext(chr, pos, ref, alt, phredScore);
            }
            // Should this be here? Would it be safer to validate for null/empty fields here? This is primarily for
            // ease of testing. The TestAlleleFactory should fill in the missing fields for tests, although this
            // replicates what buildVariantContext is doing for the SampleGenotypes
            if (sampleGenotypes.isEmpty()) {
                sampleGenotypes = SINGLE_SAMPLE_HET_GENOTYPE;
            }

            // n.b Storing the VariantContext in its raw string state as found in the file
            // can save a significant amount of RAM, at the expense of having to decode and encode the variant context
            // object twice.
            //
            // Standard POMP sample (4.3 million variants, single sample) Oracle JDK 11 (G1CG), Windows 10 release 1803
            // -Xmx4G, preload random walk, caching maximumSize=6000000
            // Heap used = max heap used after final GC event during variant filtering. RW matrix etc uses 2G.
            // running on PASS_ONLY and discarding failed variants
            //                | Heap Used |  TIME
            // VariantContext | 3.1G   |  3m50s
            // String         | 2.9G   |  4m45s
            //
            // running on PASS_ONLY but *RETAINING* all variants
            //                | Heap Used |  TIME
            // VariantContext | 4G+   |  didn't complete - constant GC churn after 2.3 mill variants
            // String         | 3.4G  |  5m30s (constant spiky 30-60% CPU three full (90% CPU) GC events towards end)
            //
            // -Xmx8G, preload random walk, caching maximumSize=6000000
            // running on PASS_ONLY and discarding failed variants
            //                | Heap Used |  TIME
            // VariantContext | 3.1G   |  3m55s (2 partial GC (60% CPU), avg 30-40% CPU)
            // String         | 2.9G   |  4m25s (several partial GC (60% CPU), avg 30-40% CPU)
            //
            // running on PASS_ONLY but *RETAINING* all variants
            //                | Heap Used |  TIME
            // VariantContext | 5.9G  |  4m10s (7 partial GC (60% CPU), avg 50-60% CPU)
            // String         | 4.3G  |  4m30s (3 partial GC (60% CPU), avg 30-40% CPU)

//            VCFEncoder vcfEncoder = VcfCodecs.encoder(sampleGenotypes.keySet());
//            vcfString = vcfEncoder.encode(variantContext);

            return new VariantEvaluation(this);
        }

        /**
         * @return a generic one-based position variant context with a heterozygous genotype having no attributes.
         */
        private VariantContext buildVariantContext(int chr, int pos, String ref, String alt, double qual) {
            Allele refAllele = Allele.create(ref, true);
            Allele altAllele = Allele.create(alt);
            List<Allele> alleles = Arrays.asList(refAllele, altAllele);

            VariantContextBuilder vcBuilder = new VariantContextBuilder();

            // build Genotype
            GenotypeBuilder gtBuilder = new GenotypeBuilder(DEFAULT_SAMPLE_NAME).noAttributes();
            //default to HETEROZYGOUS
            gtBuilder.alleles(alleles);

            // build VariantContext
            vcBuilder.loc(String.valueOf(chr), pos, pos - 1L + ref.length());
            vcBuilder.alleles(alleles);
            vcBuilder.genotypes(gtBuilder.make());
            vcBuilder.log10PError(-0.1 * qual);

            return vcBuilder.make();
        }

    }
}
