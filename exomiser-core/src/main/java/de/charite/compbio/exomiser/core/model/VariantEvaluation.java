package de.charite.compbio.exomiser.core.model;

import de.charite.compbio.exomiser.core.filter.FilterResultStatus;
import de.charite.compbio.exomiser.core.frequency.FrequencyData;
import de.charite.compbio.exomiser.core.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.filter.FilterResult;
import de.charite.compbio.exomiser.core.filter.FilterType;
import jannovar.common.VariantType;
import jannovar.exome.Variant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a wrapper for the {@code Variant} class from the jannovar
 * hierarchy, and additionally includes all of the information on pathogenicity
 * and frequency that is added to each variant by the Exomizer program.
 *
 * @author Peter Robinson
 * @version 0.15 (25 January, 2014)
 */
public class VariantEvaluation implements Comparable<VariantEvaluation>, Filterable {

    public static final Logger logger = LoggerFactory.getLogger(VariantEvaluation.class);
    /**
     * An instance of this class encapsulates an object of the class
     * {@link jannovar.exome.Variant Variant} from the Jannovar library, and
     * basically combines this Variant with a list of variant evaluation objects
     * ({@link exomizer.filter.VariantScore}).
     */
    private final Variant var;

    /**
     * A map of the results of filtering. The key to the map is an integer
     * constant as defined in {@link exomizer.common.FilterType FilterType}.
     */
    private final Map<FilterType, FilterResult> passedFilterResultsMap;
    private final Set<FilterType> failedFilterTypes;

    private float variantScore = 1f;
    private List<String> mutationRefList = null;
    private FrequencyData frequencyData;
    private PathogenicityData pathogenicityData;

    public VariantEvaluation(Variant v) {
        var = v;
        passedFilterResultsMap = new LinkedHashMap<>();
        failedFilterTypes = EnumSet.noneOf(FilterType.class);
        //why not set the frequency data too? Well, not having a null implies that
        //the data has been set from the database and if there is no data then 
        //it must be an extremely rare and therefore interesting variant. 
        //This will then erroneously pass the frequency filter.
        pathogenicityData = new PathogenicityData(null, null, null, null);
    }

    /**
     * @return The original variant object resulting from the parse of the VCF
     * file.
     */
    public Variant getVariant() {
        return this.var;
    }

    /**
     * @return the VariantType such as MISSENSE, FRAMESHIFT DELETION, etc.
     */
    public VariantType getVariantType() {
        return this.var.getVariantTypeConstant();
    }

    /**
     * @return the HGVS gene symbol associated with the variant.
     */
    public String getGeneSymbol() {
        String name = var.getGeneSymbol();
        return (name == null) ? "." : parseGeneSymbol(name);
    }
    /**
     * Jannovar produces a string of comma-separated gene symbols if a variant is located in
     * regions associated with more than one gene e.g. A variant located in an exon of
     * GENE1 and an intron of GENE2 would have the gene symbol GENE1,GENE2. We
     * are going to assign the variant to the gene in which it is most
     * unfavourably located. By convention, Jannovar reports this as the first
     * gene symbol.
     */
    private String parseGeneSymbol(String name) {
        if (name.contains(",")) {
            String nameParts[] = name.split(",");
            String firstName = nameParts[0];
            logger.debug("Variant found in multiple genes: {}. Assigning it to gene {}", nameParts, firstName);
            return firstName;
        }
        return name;
    }

    public int getEntrezGeneID() {
        return this.var.getEntrezGeneID();
    }

    /**
     * @return true if the variant belongs to a class that is non-exonic and
     * nonsplicing.
     */
    public boolean isOffExomeTarget() {
        return this.var.isOffExomeTarget();
    }

    public String getRef() {
        return this.var.get_ref();
    }

    public String getAlt() {
        return this.var.get_alt();
    }

    /**
     * @return true if this variant is not a frameshift.
     */
    public boolean isSNV() {
        if (this.var.get_ref() == "-") {
            return false;
        } else if (this.var.get_alt() == "-") {
            return false;
        } else {
            return true;
        }
    }

    public int getVariantReadDepth() {
        return this.var.getVariantReadDepth();
    }

    /**
     * @return An annotation for a single transcript, representing one of the
     * annotations with the highest priority score (most pathogenic).
     */
    public String getRepresentativeAnnotation() {
        return this.var.getRepresentativeAnnotation();
    }

    /**
     * This function returns a list of all of the
     * {@link jannovar.annotation.Annotation Annotation} objects that have been
     * associated with the current variant. This function can be called if
     * client code wants to display one line for each affected transcript, e.g.,
     * <ul>
     * <li>LTF(uc003cpr.3:exon5:c.30_31insAAG:p.R10delinsRR)
     * <li>LTF(uc003cpq.3:exon2:c.69_70insAAG:p.R23delinsRR)
     * <li>LTF(uc010hjh.3:exon2:c.69_70insAAG:p.R23delinsRR)
     * </ul>
     * <P>
     * If client code wants instead to display just a single string that
     * summarizes all of the annotations, it should call the function
     * {@link #getRepresentativeAnnotation}.
     */
    public List<String> getAnnotationList() {
        return this.var.getAnnotationList();
    }

    public boolean hasAnnotations() {
        //this is a bit of a hack to flag up any variant evaluations which Jannovar
        //failed to annotate and therefore will have not have passed and filters.
        return !var.getAnnotation().equals(".");
    }
    /**
     * This function returns a list of all of the
     * {@link jannovar.annotation.Annotation Annotation} objects that have been
     * associated with the current variant. This function can be called if
     * client code wants to display one line for each affected transcript, e.g.,
     * <ul>
     * <li>LTF(uc003cpr.3:exon5:c.30_31insAAG:p.R10delinsRR)
     * <li>LTF(uc003cpq.3:exon2:c.69_70insAAG:p.R23delinsRR)
     * <li>LTF(uc010hjh.3:exon2:c.69_70insAAG:p.R23delinsRR)
     * </ul>
     * <P>
     * If client code wants instead to display just a single string that
     * summarizes all of the annotations, it should call the function
     * {@link #getRepresentativeAnnotation}.
     */
    public List<String> getAnnotationListWithoutGeneSymbols() {
        @SuppressWarnings("unchecked")
        List<String> lst = (List<String>) this.var.getAnnotationList().clone();
        for (String s : lst) {
            int i = s.indexOf("(");
            if (i < 0) {
                continue;
            }
            int j = s.indexOf(")", i);
            if (j < 0) {
                continue;
            }
            s = s.substring(i + 1, j);
        }
        return lst;
    }

    /**
     * Get a list of annotations for the variant preceded by their annotation
     * class (e.g., missense) and separated by a pipe (|).
     */
    public List<String> getAnnotationListWithAnnotationClass() {
        return this.var.getAnnotationListWithAnnotationClass();
    }

    /**
     * @return a String such as chr6:g.29911092G>T
     */
    public String getChromosomalVariant() {
        return this.var.get_chromosomal_variant();
    }

    /**
     * @return a string such as "chr4"
     */
    public String getChromosomeAsString() {
        return this.var.get_chromosome_as_string();
    }

    /**
     * @return the start position of the variant on the chromosome
     */
    public int getVariantStartPosition() {
        return this.var.get_position();
    }

    /**
     * @return the end position of the variant on the chromosome
     */
    public int getVariantEndPosition() {
        int x = var.get_ref().length(); /* size of variant */

        return this.var.get_position() + x - 1;
    }

    /**
     * @return The count of transcripts that are affected by the current
     * variant.
     */
    public int getNumberOfAffectedTranscripts() {
        return this.var.getTranscriptAnnotations().size();
    }

    public List<String> getGenotypeList() {
        return this.var.getGenotypeList();
    }

    /**
     * @return an integer representing the chromosome. 1-22 are obvious,
     * chrX=23, ChrY=24, ChrM=25.
     */
    public int getChromosomeAsInteger() {
        return this.var.get_chromosome();
    }

    /**
     * @return Return the start position of the variant on its chromosome.
     */
    public int getPosition() {
        return this.var.get_position();
    }

    public String getGenotypeAsString() {
        return this.var.getGenotypeAsString();
    }

    /**
     * @return the number of individuals with a genotype at this variant.
     */
    public int getNumberOfIndividuals() {
        return this.var.getGenotype().getNumberOfIndividuals();
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
     * This method returns the variant score (prediction of the pathogenicity
     * and relevance of the Variant) by using data from the {@code FilterResult}
     * objects associated with this Variant.
     * <P>
     * Note that we use results of filtering to remove Variants that are
     * predicted to be simply non-pathogenic. However, amongst variants
     * predicted to be potentially pathogenic, there are different strengths of
     * prediction, which is what this score tries to reflect.
     *
     * @return a score between 0 and 1
     */
    public float getVariantScore() {
        return variantScore;
    }

    /**
     * @return the map of FilterResult objects that represent the result of
     * filtering
     */
    public Map<FilterType, FilterResult> getFilterResults() {
        return passedFilterResultsMap;
    }

    /**
     * This method is used to add a {@code FilterResult} object to this variant.
     * Such objects represent the results of running the variant through a
     * {@code Filter}.
     *
     * @param filterResult
     * @return
     */
    public boolean addFilterResult(FilterResult filterResult) {
        reCalculateVariantScore(filterResult);

        if (filterResult.getResultStatus() == FilterResultStatus.PASS) {
            return addPassedFilterResult(filterResult);
        }
        return addFailedFilterResult(filterResult);
    }

    private void reCalculateVariantScore(FilterResult filterScore) {
        //remember to re-calculate the overall filtering score each time a new
        //filterScore is added
        variantScore *= filterScore.getScore();
    }

    private boolean addPassedFilterResult(FilterResult filterResult) {
        passedFilterResultsMap.put(filterResult.getFilterType(), filterResult);
        return true;
    }

    private boolean addFailedFilterResult(FilterResult filterResult) {
        failedFilterTypes.add(filterResult.getFilterType());
        return false;
    }

    /**
     *
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
     *
     * @return
     */
    @Override
    public boolean passedFilters() {
        return failedFilterTypes.isEmpty();
    }

    @Override
    public boolean passedFilter(FilterType filterType) {
//        if (filterResultsMap.containsKey(filterType)) {
//            FilterResult filterResult = filterResultsMap.get(filterType);
//            return filterResult.passedFilter();
//        }
//        return false;
        return !failedFilterTypes.contains(filterType) && passedFilterResultsMap.containsKey(filterType);
    }

    public FilterResult getFilterResult(FilterType filterType) {
        return passedFilterResultsMap.get(filterType);
    }

    /**
     * Sort based on the variant score. Variant scores are ranked on a scale of
     * 1 to 0. The comparator will rank the variants with a higher numerical
     * value variant score before those with a lower value variant score.
     *
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    @Override
    public int compareTo(VariantEvaluation other) {
        float me = getVariantScore();
        float them = other.getVariantScore();
        if (me > them) {
            return -1;
        } else if (them > me) {
            return 1;
        }
        return 0;
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

}
