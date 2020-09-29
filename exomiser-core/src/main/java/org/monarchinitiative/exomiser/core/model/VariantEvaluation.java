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

package org.monarchinitiative.exomiser.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.GenotypeBuilder;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.Contigs;
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
//@JsonPropertyOrder({"genomeAssembly", "chromosomeName", "chromosome", "position", "ref", "alt", "id", "phredScore", "variantEffect", "nonCodingVariant", "whiteListed", "filterStatus", "variantScore", "frequencyScore", "pathogenicityScore", "predictedPathogenic", "passedFilterTypes", "failedFilterTypes", "frequencyData", "pathogenicityData", "compatibleInheritanceModes", "contributingInheritanceModes", "transcriptAnnotations"})
public class VariantEvaluation extends AbstractVariant implements Comparable<VariantEvaluation>, Filterable, Inheritable {

    //threshold over which a variant effect score is considered pathogenic
    private static final float DEFAULT_PATHOGENICITY_THRESHOLD = 0.5f;

    private static final String DEFAULT_SAMPLE_NAME = SampleIdentifier.defaultSample().getId();
    // These shouldn't be used in production, but in cases where there is no genotype this will prevent NullPointer and ArrayIndexOutOfBounds Exceptions
    static final ImmutableMap<String, SampleGenotype> SINGLE_SAMPLE_HET_GENOTYPE = ImmutableMap.of(
            DEFAULT_SAMPLE_NAME, SampleGenotype.het()
    );

    // HTSJDK {@link VariantContext} instance of this allele
    private final VariantContext variantContext;

    // numeric index of the alternative allele in {@link #vc}.
    private final int altAlleleId;

    private final String id;

    // VariantCoordinates variables - these are a minimal requirement for describing a variant
    private final String chromosomeName;

    // Variant variables, for a richer more VCF-like experience
    private final double phredScore;

    // IMPORTANT! This map *MUST* be an ordered map
    // TODO: link to SampleIdentifier
    private final Map<String, SampleGenotype> sampleGenotypes;

    //VariantAnnotation
    private final String geneSymbol;

    // results from filters
    // mutable
    private final Set<FilterType> passedFilterTypes;
    private final Set<FilterType> failedFilterTypes;

    // score-related stuff - these are mutable
    private boolean whiteListed;
    private FrequencyData frequencyData;
    private PathogenicityData pathogenicityData;
    @JsonProperty("contributingInheritanceModes")
    private final Set<ModeOfInheritance> contributingModes;
    private Set<ModeOfInheritance> compatibleInheritanceModes;

    private VariantEvaluation(Builder builder) {
        super(builder);

        this.chromosomeName = ((super.contig == null) || super.contig.isEmpty())
                ? Contigs.toString(super.chromosome) : super.contig;

        this.geneSymbol = inputOrFirstValueInCommaSeparatedString((super.geneSymbol.isEmpty()) ? "." : super.geneSymbol);

        this.variantContext = builder.variantContext;
        this.altAlleleId = builder.altAlleleId;
        this.id = builder.id;
        this.phredScore = builder.phredScore;
        // IMPORTANT! This map *MUST* be an ordered map
        this.sampleGenotypes = builder.sampleGenotypes.isEmpty() ? SINGLE_SAMPLE_HET_GENOTYPE
                : ImmutableMap.copyOf(builder.sampleGenotypes);

        this.passedFilterTypes = EnumSet.copyOf(builder.passedFilterTypes);
        this.failedFilterTypes = EnumSet.copyOf(builder.failedFilterTypes);

        this.compatibleInheritanceModes = EnumSet.copyOf(builder.compatibleInheritanceModes);
        this.contributingModes = EnumSet.copyOf(builder.contributingModes);

        this.whiteListed = builder.whiteListed;
        this.frequencyData = builder.frequencyData;
        this.pathogenicityData = builder.pathogenicityData;
    }

    private String inputOrFirstValueInCommaSeparatedString(String geneSymbol) {
        int commaIndex = geneSymbol.indexOf(',');
        return (commaIndex > -1) ? geneSymbol.substring(0, commaIndex) : geneSymbol;
    }

    /**
     * @return a String such "4" or "X" in the case of chromosome 23
     */
    @Override
    public String getStartContigName() {
        return chromosomeName;
    }

    @JsonIgnore
    public VariantContext getVariantContext() {
        return variantContext;
    }

    public int getAltAlleleId() {
        return altAlleleId;
    }

    /**
     * An identifier for the variant. Note that this is implementation specific and can be any value. Typically it will
     * be derived from the initial VCF id field.
     *
     * @return the id of the variant
     * @since 12.1.0
     */
    public String getId() {
        return id;
    }

    public double getPhredScore() {
        return phredScore;
    }

    /**
     * @return the gene symbol associated with the variant.
     */
    @JsonIgnore
    @Override
    public String getGeneSymbol() {
        return geneSymbol;
    }

    @JsonIgnore
    @Override
    public String getGeneId() {
        return geneId;
    }

    /**
     * Returns a gnomAD formatted string for easier identification of variants in output. HGVS doesn't look similar
     * enough to VCF to enable linking to the source VCF in the output files.
     * <p>
     * Structural variants will be displayed with chromosome-start-end-ref-alt and length, whereas small variants will
     * have the more classical chromosome-start-ref-alt format.
     *
     * @return a String such as X-31517201-T-C or 4-65216746-65216746-G-<INS:ME:ALU>
     */
    @JsonIgnore
    public String toGnomad() {
        if (isStructuralVariant()) {
            // can be searched for in gnomad like so:
            // https://gnomad.broadinstitute.org/region/4-65216746-65216746-G-<INS:ME:ALU>?dataset=gnomad_sv_r2_1
            return Contigs.toString(chromosome) + '-' + start + '-' + end + '-' + ref + '-' + alt + ' ' + unitLength();
        }
        // can be searched for in gnomad like so:
        // https://gnomad.broadinstitute.org/variant/X-31517201-T-C
        return Contigs.toString(chromosome) + '-' + start + '-' + ref + '-' + alt;
    }

    private String unitLength() {
        if (length < 1000) {
            return length + "bp";
        }
        // this does not perform any rounding
        if (length < 1_000_000) {
            return (length / 1_000) + "." + (length / 100) % 10 + "kb";
        }
        if (length < 1_000_000_000) {
            return (length / 1_000_000) + "." + (length / 100_000) % 10 + "Mb";
        }
        // Max integer is 2_147_483_647 i.e. 2.1Gb - given the whole human genome is ~ 3Gb, an int is way larger than we need
        //  to represent the length of a variation, even if it involves 2/3 of the genome being fused into one enormous BND
        return (length / 1_000_000_000) + "." + (length / 100_000_000) % 10 + "Gb";
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
    @JsonIgnore
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
    public synchronized Set<FilterType> getFailedFilterTypesForMode(ModeOfInheritance modeOfInheritance) {
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
     * <p>
     * Note that we use results of filtering to remove Variants that are predicted to be simply non-pathogenic. However,
     * amongst variants predicted to be potentially pathogenic, there are different strengths of prediction, which is
     * what this score tries to reflect.
     * <p>
     * For missense mutations, we use the predictions of MutationTaster, polyphen, and SIFT taken from the dbNSFP
     * project, if present, or otherwise return a default score.
     * <p>
     * The score returned here is therefore an overall pathogenicity score defined on the basis of
     * "medical genetic intuition".
     *
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
     * @return true if the VariantEvaluation has been marked as whitelisted
     * @since 12.0.0
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
        if (this.chromosome != other.chromosome) {
            return Integer.compare(this.chromosome, other.chromosome);
        }
        if (this.start != other.start) {
            return Integer.compare(this.start, other.start);
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
        if (some.chromosome != other.chromosome) {
            return Integer.compare(some.chromosome, other.chromosome);
        }
        if (some.start != other.start) {
            return Integer.compare(some.start, other.start);
        }
        if (!some.ref.equals(other.ref)) {
            return some.ref.compareTo(other.ref);
        }
        return some.alt.compareTo(other.alt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genomeAssembly, chromosome, start, ref, alt);
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
        if (this.chromosome != other.chromosome) {
            return false;
        }
        if (this.start != other.start) {
            return false;
        }
        return Objects.equals(this.ref, other.ref) && Objects.equals(this.alt, other.alt);
    }

    public String toString() {
        // expose frequency and pathogenicity scores?
        if (contributesToGeneScore()) {
            //Add a star to the output string between the variantEffect and the score
            return "VariantEvaluation{assembly=" + genomeAssembly + " chr=" + chromosome + " start=" + start + " end=" + end + " length=" + length + " ref=" + ref + " alt=" + alt + " qual=" + phredScore + " " + variantType + " " + variantEffect + " * score=" + getVariantScore() + " " + getFilterStatus() + " failedFilters=" + failedFilterTypes + " passedFilters=" + passedFilterTypes
                    + " compatibleWith=" + compatibleInheritanceModes + " sampleGenotypes=" + sampleGenotypes + "}";
        }
        return "VariantEvaluation{assembly=" + genomeAssembly + " chr=" + chromosome + " start=" + start + " end=" + end + " length=" + length + " ref=" + ref + " alt=" + alt + " qual=" + phredScore + " " + variantType + " " + variantEffect + " score=" + getVariantScore() + " " + getFilterStatus() + " failedFilters=" + failedFilterTypes + " passedFilters=" + passedFilterTypes
                + " compatibleWith=" + compatibleInheritanceModes + " sampleGenotypes=" + sampleGenotypes + "}";
    }

    /**
     * @return
     * @since 13.0.0
     */
    public VariantEvaluation.Builder toBuilder() {
        return new Builder()
                //ChromosomalRegion fields
                .genomeAssembly(this.genomeAssembly)
                .contig(this.contig)
                .chromosome(this.chromosome)
                .start(this.start)
                .startCi(this.startCi)
                .endContig(this.endContig)
                .endChromosome(this.endChromosome)
                .end(this.end)
                .endCi(this.endCi)
                .length(this.length)
                // Variant fields
                .variantType(this.variantType)
                .ref(this.ref)
                .alt(this.alt)
                .geneSymbol(this.geneSymbol)
                .geneId(this.geneId)
                .variantEffect(this.variantEffect)
                .annotations(this.annotations)
                // VariantContext-derived fields
                .variantContext(this.variantContext)
                .altAlleleId(this.altAlleleId)
                .id(this.id)
                .sampleGenotypes(this.sampleGenotypes)
                .quality(this.phredScore)
                // Additional annotations
                .whiteListed(this.whiteListed)
                .frequencyData(this.frequencyData)
                .pathogenicityData(this.pathogenicityData)
                .failedFilters(this.failedFilterTypes)
                .passedFilters(this.passedFilterTypes)
                .compatibleInheritanceModes(this.compatibleInheritanceModes)
                .contributingModes(this.contributingModes);
    }

    /**
     * Copies all variant fields into a new VariantEvaluation.Builder
     *
     * @param variant
     * @return a Builder instance pre-populated with fields copied from the input Variant
     * @since 13.0.0
     */
    public static VariantEvaluation.Builder copy(Variant variant) {
        return new Builder()
                .genomeAssembly(variant.getGenomeAssembly())
                .contig(variant.getStartContigName())
                .chromosome(variant.getStartContigId())
                .start(variant.getStart())
                .startCi(variant.getStartCi())
                .endContig(variant.getEndContigName())
                .endChromosome(variant.getEndContigId())
                .end(variant.getEnd())
                .endCi(variant.getEndCi())
                .length(variant.getLength())
                .variantType(variant.getVariantType())
                .ref(variant.getRef())
                .alt(variant.getAlt())
                .geneSymbol(variant.getGeneSymbol())
                .geneId(variant.getGeneId())
                .variantEffect(variant.getVariantEffect())
                .annotations(variant.getTranscriptAnnotations());
    }

    /**
     * Testing only builder function - TODO: move to TestFactory or something?
     * DO NOT USE IN PRODUCTION CODE!
     *
     * @param chr
     * @param start
     * @param ref
     * @param alt
     * @return
     */
    public static VariantEvaluation.Builder builder(int chr, int start, String ref, String alt) {
        int length = AllelePosition.isSymbolic(ref, alt) ? 0 : AllelePosition.length(ref, alt);
        VariantType variantType = VariantType.parseAllele(ref, alt);
        return new Builder()
                .genomeAssembly(GenomeAssembly.HG19)
                .chromosome(chr)
                .start(start)
                .ref(ref)
                .alt(alt)
                .length(length)
                .variantType(variantType);
    }

    /**
     * Builder class for producing a valid VariantEvaluation.
     */
    public static class Builder extends AbstractVariant.Builder<Builder> {

        private double phredScore = 0;
        private VariantContext variantContext;
        private int altAlleleId;
        private String id = "";
        private Map<String, SampleGenotype> sampleGenotypes = ImmutableMap.of();

        private PathogenicityData pathogenicityData = PathogenicityData.empty();
        private FrequencyData frequencyData = FrequencyData.empty();

        private boolean whiteListed = false;
        private Set<FilterType> passedFilterTypes = EnumSet.noneOf(FilterType.class);
        private Set<FilterType> failedFilterTypes = EnumSet.noneOf(FilterType.class);
        private Set<ModeOfInheritance> contributingModes = EnumSet.noneOf(ModeOfInheritance.class);
        private Set<ModeOfInheritance> compatibleInheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);

        public Builder variantContext(VariantContext variantContext) {
            this.variantContext = Objects.requireNonNull(variantContext);
            return this;
        }

        public Builder altAlleleId(int altAlleleId) {
            this.altAlleleId = altAlleleId;
            return this;
        }

        public Builder id(String id) {
            this.id = Objects.requireNonNullElse(id, "");
            return this;
        }

        public Builder quality(double phredScore) {
            this.phredScore = phredScore;
            return this;
        }

        //TODO - this is error-prone as it is possible to supply a HashMap which would screw-up the fact that we're
        // relying on the inherently ORDERED ImmutableMap implementation
        public Builder sampleGenotypes(Map<String, SampleGenotype> sampleGenotypes) {
            this.sampleGenotypes = Objects.requireNonNull(sampleGenotypes);
            return this;
        }

        public Builder whiteListed(boolean whiteListed) {
            this.whiteListed = whiteListed;
            return this;
        }

        public Builder pathogenicityData(PathogenicityData pathogenicityData) {
            this.pathogenicityData = Objects.requireNonNull(pathogenicityData);
            return this;
        }

        public Builder frequencyData(FrequencyData frequencyData) {
            this.frequencyData = Objects.requireNonNull(frequencyData);
            return this;
        }

        public Builder filterResults(FilterResult... filterResults) {
            return filterResults(Arrays.asList(filterResults));
        }

        Builder filterResults(Collection<FilterResult> filterResults) {
            for (FilterResult filterResult : filterResults) {
                if (filterResult.passed()) {
                    this.passedFilterTypes.add(filterResult.getFilterType());
                } else {
                    this.failedFilterTypes.add(filterResult.getFilterType());
                }
            }
            return this;
        }

        Builder failedFilters(Set<FilterType> failedFilterTypes) {
            this.failedFilterTypes = Objects.requireNonNull(failedFilterTypes);
            return this;
        }

        Builder passedFilters(Set<FilterType> passedFilterTypes) {
            this.passedFilterTypes = Objects.requireNonNull(passedFilterTypes);
            return this;
        }

        Builder compatibleInheritanceModes(Set<ModeOfInheritance> compatibleInheritanceModes) {
            this.compatibleInheritanceModes = Objects.requireNonNull(compatibleInheritanceModes);
            return this;
        }

        public Builder contributingModes(Set<ModeOfInheritance> contributingModes) {
            this.contributingModes = Objects.requireNonNull(contributingModes);
            return this;
        }

        public VariantEvaluation build() {

            if (variantContext == null) {
                // We don't check that the variant context agrees with the coordinates here as the variant context could
                // have been split into different allelic variants so the positions and alleles could differ.
//             TODO: This was removed post 12.1.0 release awaiting further production checks before fully removing this.
//                variantContext = buildVariantContext(chromosome, start, ref, alt, phredScore);
            }

            return new VariantEvaluation(this);
        }

        @Override
        protected Builder self() {
            return this;
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
