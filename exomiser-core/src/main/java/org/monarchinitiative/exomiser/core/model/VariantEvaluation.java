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

package org.monarchinitiative.exomiser.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import htsjdk.variant.variantcontext.*;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.VariantTypePathogenicityScores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class is a wrapper for the {@code Variant} class from the jannovar
 * hierarchy, and additionally includes all of the information on pathogenicity
 * and frequency that is added to each variant by the Exomizer program.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Peter Robinson <peter.robinson@charite.de>
 */
public class VariantEvaluation implements Comparable<VariantEvaluation>, Filterable, Inheritable, Variant {

    private static final Logger logger = LoggerFactory.getLogger(VariantEvaluation.class);

    //threshold over which a variant effect score is considered pathogenic
    private static final float DEFAULT_PATHOGENICITY_THRESHOLD = 0.5f;

    // HTSJDK {@link VariantContext} instance of this allele
    @JsonIgnore
    private final VariantContext variantContext;

    // numeric index of the alternative allele in {@link #vc}.
    private final int altAlleleId;

    //VariantCoordinates variables - these are a minimal requirement for describing a variant
    private final int chr;
    private final String chromosomeName;
    private final int pos;
    private final String ref;
    private final String alt;

    //Variant variables, for a richer more VCF-like experience
    private final int numIndividuals;
    private final double phredScore;

    //Jannovar annotations
    private final boolean isOffExome;
    private VariantEffect variantEffect;
    private List<TranscriptAnnotation> annotations;
    private String geneSymbol;
    private int entrezGeneId;

    //results from filters
    private final Set<FilterType> passedFilterTypes;
    private final Set<FilterType> failedFilterTypes;

    //score-related stuff
    private FrequencyData frequencyData;
    private PathogenicityData pathogenicityData;
    private boolean contributesToGeneScore = false;
    private Set<ModeOfInheritance> inheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);

    //bit of an orphan variable - look into refactoring this
    @JsonIgnore
    private List<String> mutationRefList = null;

    private VariantEvaluation(Builder builder) {
        chr = builder.chr;
        chromosomeName = builder.chromosomeName;
        pos = builder.pos;
        ref = builder.ref;
        alt = builder.alt;

        numIndividuals = builder.numIndividuals;
        phredScore = builder.phredScore;
        isOffExome = builder.isOffExome;
        variantEffect = builder.variantEffect;
        annotations = builder.annotations;
        geneSymbol = builder.geneSymbol;
        entrezGeneId = builder.entrezGeneId;

        variantContext = builder.variantContext;
        altAlleleId = builder.altAlleleId;

        passedFilterTypes = EnumSet.copyOf(builder.passedFilterTypes);
        failedFilterTypes = EnumSet.copyOf(builder.failedFilterTypes);

        frequencyData = builder.frequencyData;
        pathogenicityData = builder.pathogenicityData;
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
        return variantContext;
    }

    public int getAltAlleleId() {
        return altAlleleId;
    }

    @Override
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
    
    @Override
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

    @Override
    public void setGeneSymbol(String symbol) {
        geneSymbol = symbol;
    }
    
    @Override
    public int getEntrezGeneId() {
        return entrezGeneId;
    }
    
    @Override
    public void setEntrezGeneId(int id) {
        entrezGeneId = id;
    }

    @Override
    public boolean isXChromosomal() {
        return chr == 23;
    }

    @Override
    public boolean isYChromosomal() {
        return chr == 24;
    }

    /**
     * @return true if the variant belongs to a class that is non-exonic and
     * non-splicing.
     */
    @Override
    public boolean isOffExome() {
        return isOffExome;
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
    public List<TranscriptAnnotation> getAnnotations() {
        return annotations;
    }
    
    @Override
    public void setAnnotations(List<TranscriptAnnotation> annotations) {
        this.annotations = annotations;
    }

    public boolean hasAnnotations() {
        return !getAnnotations().isEmpty();
    }

    /**
     * @return a String such as chr6:g.29911092G>T
     */
    @Override
    public String getHgvsGenome() {
        return chr + ":g." + pos + ref + ">" + alt;
    }

    public String getGenotypeString() {
        // collect genotype string list
        List<String> gtStrings = new ArrayList<>();
        for (Genotype gt : variantContext.getGenotypes()) {
            boolean firstAllele = true;
            StringBuilder builder = new StringBuilder();
            for (Allele allele : gt.getAlleles()) {
                if (firstAllele) {
                    firstAllele = false;
                } else {
                    builder.append('/');
                }

                if (allele.isNoCall()) {
                    builder.append('.');
                } else if (allele.equals(variantContext.getAlternateAllele(altAlleleId))) {
                    builder.append('1');
                } else {
                    builder.append('0');
                }
            }
            gtStrings.add(builder.toString());
        }

        // normalize 1/0 to 0/1 and join genotype strings with colon
        for (int i = 0; i < gtStrings.size(); ++i) {
            if (gtStrings.get(i).equals("1/0")) {
                gtStrings.set(i, "0/1");
            }
        }
        return Joiner.on(":").join(gtStrings);
    }

    /**
     * @return the number of individuals with a genotype at this variant.
     */
    public int getNumberOfIndividuals() {
        return numIndividuals;
    }

    /**
     * Add a mutation from ClinVar or HGMD to {@link #mutationRefList}. Note
     * that for now, we code this as cv|url or hd|url to save space.
     *
     * @param anch An HTML anchor element.
     */
    public void addMutationReference(String anch) {
        if (this.mutationRefList == null) {
            this.mutationRefList = new ArrayList<>();
        }
        this.mutationRefList.add(anch);
    }

    /**
     * @return list of ClinVar and HGMD references for this position. Note, it
     * returns an empty (but non-null) list if no mutations were found.
     */
    public List<String> getMutationReferenceList() {
        if (this.mutationRefList == null) {
            return new ArrayList<>();
        } else {
            return this.mutationRefList;
        }
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

    private boolean addPassedFilterResult(FilterResult filterResult) {
        passedFilterTypes.add(filterResult.getFilterType());
        return true;
    }

    private boolean addFailedFilterResult(FilterResult filterResult) {
        failedFilterTypes.add(filterResult.getFilterType());
        return false;
    }

    /**
     * @return the set of FilterResult objects that represent the result of
     * filtering
     */
    public Set<FilterType> getPassedFilterTypes() {
        return passedFilterTypes;
    }

    /**
     * @return the Set of {@code FilterType} which the {@code VariantEvaluation}
     * failed to pass.
     */
    public Set<FilterType> getFailedFilterTypes() {
        return failedFilterTypes;
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
    public boolean passedFilters() {
        return failedFilterTypes.isEmpty();
    }

    @Override
    public boolean passedFilter(FilterType filterType) {
        return !failedFilterTypes.contains(filterType) && passedFilterTypes.contains(filterType);
    }

    private boolean isUnFiltered() {
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

    /**
     * Returns the variant score (prediction of the pathogenicity
     * and relevance of the Variant) by combining the frequency and pathogenicity scores for this variant.
     *
     * @return a score between 0 and 1
     */
    public float getVariantScore() {
        return getFrequencyScore() * getPathogenicityScore();
    }

    /**
     * @return a score between 0 and 1
     */
    public float getFrequencyScore() {
        return frequencyData.getScore();
    }

    /**
     * Some variants such as splice site variants, are assumed to be pathogenic. At the moment no particular
     * software is used to evaluate this, we merely take the variant class from the Jannovar code and assign a score.
     *
     * Note that we use results of filtering to remove Variants that are predicted to be simply non-pathogenic. However,
     * amongst variants predicted to be potentially pathogenic, there are different strengths of prediction, which is
     * what this score tries to reflect.
     *
     * For missense mutations, we use the predictions of MutationTaster, polyphen, and SIFT taken from the data from
     * the dbNSFP project.
     *
     * The score returned here is therefore an overall pathogenicity score defined on the basis of
     * "medical genetic intuition".

     * @return a score between 0 and 1
     */
    public float getPathogenicityScore() {
        if (pathogenicityData.hasPredictedScore()) {
            return pathogenicityData.getScore();
        }
        //this will return 0 for SEQUENCE_VARIANT effects (i.e. unknown)
        //return the default score - in time we might want to use the predicted score if there are any and handle things like the missense variants.
        return VariantTypePathogenicityScores.getPathogenicityScoreOf(variantEffect);
    }

    /*
     * Retained in case we have some non-missesnse variants in the database. Shouldn't be needed though.
     */
    private float calculateMissenseScore(PathogenicityData pathogenicityData) {
        if (pathogenicityData.hasPredictedScore()) {
            return pathogenicityData.getScore();
        }
        return VariantTypePathogenicityScores.DEFAULT_MISSENSE_SCORE;
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

    public void setAsContributingToGeneScore() {
        contributesToGeneScore = true;
    }

    public boolean contributesToGeneScore() {
        return contributesToGeneScore;
    }

    /**
     * @return true or false depending on whether the variant effect is considered pathogenic. Pathogenoic variants are
     * considered to be those with a pathogenicity score greater than 0.5. Missense variants will always return true.
     */
    public boolean isPredictedPathogenic() {
        if (variantEffect == VariantEffect.MISSENSE_VARIANT) {
            //we're making the assumption that a missense variant is always potentially pathogenic.
            //Given the prediction scores are predictions, they could fall below the default threshold so
            //we'll leave it up to the user to decide
            return true;
        } else {
            return getPathogenicityScore() >= DEFAULT_PATHOGENICITY_THRESHOLD;
        }
    }

    @Override
    public void setInheritanceModes(Set<ModeOfInheritance> compatibleModes) {
        if (compatibleModes.isEmpty()) {
            inheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);
        } else {
            this.inheritanceModes = EnumSet.copyOf(compatibleModes);
        }
    }
    
    @Override
    public Set<ModeOfInheritance> getInheritanceModes() {
        return inheritanceModes;
    }

    @Override
    public boolean isCompatibleWith(ModeOfInheritance modeOfInheritance) {
        return inheritanceModes.contains(modeOfInheritance);
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
        if (some.contributesToGeneScore != other.contributesToGeneScore) {
            return -Boolean.compare(some.contributesToGeneScore, other.contributesToGeneScore);
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
        int hash = 3;
        hash = 71 * hash + this.chr;
        hash = 71 * hash + this.pos;
        hash = 71 * hash + Objects.hashCode(this.ref);
        hash = 71 * hash + Objects.hashCode(this.alt);
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
        final VariantEvaluation other = (VariantEvaluation) obj;
        if (this.chr != other.chr) {
            return false;
        }
        if (this.pos != other.pos) {
            return false;
        }
        if (!Objects.equals(this.ref, other.ref)) {
            return false;
        }
        return Objects.equals(this.alt, other.alt);
    }

    public String toString() {
        //TODO: expose frequency and pathogenicity scores?
        if(contributesToGeneScore) {
            //Add a star to the output string between the variantEffect and the score
            return "VariantEvaluation{chr=" + chr + " pos=" + pos + " ref=" + ref + " alt=" + alt + " qual=" + phredScore + " " + variantEffect + " * score=" + getVariantScore() + " " + getFilterStatus() + " failedFilters=" + failedFilterTypes + " passedFilters=" + passedFilterTypes
                    + " compatibleWith=" + inheritanceModes + "}";
        }
        return "VariantEvaluation{chr=" + chr + " pos=" + pos + " ref=" + ref + " alt=" + alt + " qual=" + phredScore + " " + variantEffect + " score=" + getVariantScore() + " " + getFilterStatus() + " failedFilters=" + failedFilterTypes + " passedFilters=" + passedFilterTypes
                + " compatibleWith=" + inheritanceModes + "}";
    }

    public static Builder builder(int chr, int pos, String ref, String alt) {
        return new Builder(chr, pos, ref, alt);
    }

    /**
     * Builder class for producing a valid VariantEvaluation.
     */
    public static class Builder {

        private int chr;
        private String chromosomeName;
        private int pos;
        private String ref;
        private String alt;

        private int numIndividuals = 1;
        private double phredScore = 0;

        private boolean isOffExome;
        private VariantEffect variantEffect = VariantEffect.SEQUENCE_VARIANT;
        private List<TranscriptAnnotation> annotations = Collections.emptyList();
        private String geneSymbol = ".";
        private int entrezGeneId = GeneIdentifier.NULL_ENTREZ_ID;

        private VariantContext variantContext;
        private int altAlleleId;

        private PathogenicityData pathogenicityData = PathogenicityData.EMPTY_DATA;
        private FrequencyData frequencyData = FrequencyData.EMPTY_DATA;

        private final Set<FilterType> passedFilterTypes = EnumSet.noneOf(FilterType.class);
        private final Set<FilterType> failedFilterTypes = EnumSet.noneOf(FilterType.class);

        /**
         * Creates a minimal variant
         *
         * @param chr
         * @param pos
         * @param ref
         * @param alt
         */
        public Builder(int chr, int pos, String ref, String alt) {
            this.chr = chr;
            this.pos = pos;
            this.ref = ref;
            this.alt = alt;
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
                    return "M";
                default:
                    return String.valueOf(chr);
            }
        }

        public Builder variantContext(VariantContext variantContext) {
            this.variantContext = variantContext;
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
            GenotypeBuilder gtBuilder = new GenotypeBuilder("sample").noAttributes();
            //default to HETEROZYGOUS
            gtBuilder.alleles(alleles);

            // build VariantContext
            vcBuilder.loc("chr" + chr, pos, pos - 1L + ref.length());
            vcBuilder.alleles(alleles);
            vcBuilder.genotypes(gtBuilder.make());
            vcBuilder.log10PError(-0.1 * qual);

            return vcBuilder.make();
        }

        public Builder altAlleleId(int altAlleleId) {
            this.altAlleleId = altAlleleId;
            return this;
        }

        public Builder quality(double phredScore) {
            this.phredScore = phredScore;
            return this;
        }

        public Builder numIndividuals(int numIndividuals) {
            this.numIndividuals = numIndividuals;
            return this;
        }

        public Builder isOffExome(boolean isOffExome) {
            this.isOffExome = isOffExome;
            return this;
        }

        public Builder variantEffect(VariantEffect variantEffect) {
            this.variantEffect = variantEffect;
            return this;
        }

        public Builder annotations(List<TranscriptAnnotation> annotations) {
            //TODO: can this be an ImmutableList.copyOf() ?
            this.annotations = annotations;
            return this;
        }

        public Builder geneSymbol(String geneSymbol) {
            if (geneSymbol.equals(".")) {
                this.geneSymbol = geneSymbol;
            } else {
                String[] tokens = geneSymbol.split(",");
                this.geneSymbol = tokens[0];
            }
            return this;
        }

        public Builder geneId(int geneId) {
            this.entrezGeneId = geneId;
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
            if (chromosomeName == null) {
                chromosomeName = buildChromosomeName(chr);
            }

            if (variantContext == null) {
                variantContext = buildVariantContext(chr, pos, ref, alt, phredScore);
            }
            return new VariantEvaluation(this);
        }

    }
}
