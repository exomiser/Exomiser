package de.charite.compbio.exomiser.exome;

import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.priority.GeneScore;
import jannovar.common.Constants;
import jannovar.common.ModeOfInheritance;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import jannovar.pedigree.Pedigree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class represents a Gene in which {@link jannovar.exome.Variant Variant}
 * objects have been identified by exome sequencing. Note that this class stores
 * information about observed variants and quality scores etc. In contrast, the
 * class {@link jannovar.reference.TranscriptModel TranscriptModel} stores
 * information from UCSC about all genes, irrespective of whether we see a
 * variant in the gene by exome sequencing. Therefore, the program uses
 * information from
 * {@link jannovar.reference.TranscriptModel TranscriptModel} object to annotate
 * variants found by exome sequencing, and stores the results of that annotation
 * in
 * {@link jannovar.exome.Variant Variant} objects. Objects of this class have a
 * list of Variant objects, one for each variant observed in the exome. Additionally,
 * the Gene objects get prioritized for their biomedical relevance to the disease
 * in question, and each such prioritization results in an 
 * {@link exomizer.priority.IRelevanceScore GeneScore} object.
 * <P>
 * There are additionally some prioritization procedures that only can be
 * performed on genes (and not on the individual variants). For instance, there
 * are certain genes such as the Mucins or the Olfactory receptor genes that are
 * often found to have variants in WES data but are known not to be the
 * relevant disease genes. Additionally, filtering for autosomal recessive or 
 * dominant patterns in the data is done with this class. This kind of
 * prioritization is done by classes that implement 
 * {@link exomizer.priority.IPriority IPriority}.
 * Recently, the ability to downweight genes with too many variants (now hardcoded to 5)
 * was added).
 * @author Peter Robinson
 * @version 0.21 (16 January, 2013)
 */
public class Gene implements Comparable<Gene> {

    /**
     * A list of all of the variants that affect this gene.
     */
    private List<VariantEvaluation> variantList = null;

    /**
     * A priority score between 0 (irrelevant) and an arbitrary number (highest
     * prediction for a disease gene) reflecting the predicted relevance of this
     * gene for the disease under study by exome sequencing.
     */
    private float priorityScore = Constants.UNINITIALIZED_FLOAT;
    /**
     * A score representing the combined pathogenicity predictions for the
     * {@link jannovar.exome.Variant Variant} objects associated with this gene.
     */
    private float filterScore = Constants.UNINITIALIZED_FLOAT;

    /**
     * A map of the results of prioritization. The key to the map is from {@link exomizer.common.FilterType FilterType}.
     */
    private Map<PriorityType, GeneScore> relevanceMap = null;
    /**
     * A Reference to the {@link jannovar.pedigree.Pedigree Pedigree} object for
     * the current VCF file. This object allows us to do segregation analysis
     * for the variants associated with this gene, i.e., to determine if they
     * are compatible with autosomal recessive, autosomal dominant, or X-linked
     * recessive inheritance.
     */
    private static Pedigree pedigree = null;

    /**
     * This method sets the pedigree for all Gene objects (it is a static
     * method). It is intended that pedigree filtering algorithms can use the
     * genotypes associated with this Gene and its Variants as well as the
     * pedigree contained in the static Pedigree object in order to perform
     * inheritance filtering.
     *
     * @param ped The pedigree corresponding to the current VCF file.
     */
    public static void setPedigree(Pedigree ped) {
        Gene.pedigree = ped;
    }

    public void downrankGeneIfMoreVariantsThanThreshold(int threshold) {
        int n = variantList.size();
        if (threshold <= n) {
            return;
        }
        int diff = threshold - n;
        this.priorityScore = ((float) 1 / diff) * this.priorityScore;
        this.filterScore = ((float) 1 / diff) * this.filterScore;
    }

    /**
     * @return the number of {@link jannovar.exome.Variant Variant} objects for
     * this gene.
     */
    public int getNumberOfVariants() {
        return this.variantList.size();
    }

    /**
     * Downrank gene because it has a large numbers of variants (under the
     * assumption that such genes are unlikely to be be true disease genes,
     * rather, by chance say 2 of 20 variants are score as highly pathogenic by
     * polyphen, leading to a false positive call. This method downweights the {@link #filterScore}
     * of this gene, which is the aggregate score for the variants.
     *
     * @param threshold Downweighting occurs for variants that have this number
     * or more variants.
     */
    public void downWeightGeneWithManyVariants(int threshold) {
        if (this.variantList.size() < threshold) {
            return;
        }
        // Start with downweighting factor of 5%
        // For every additional variant, add half again to the factor
        int s = this.variantList.size();
        float factor = 0.05f;
        float downweight = 0f;
        while (s > threshold) {
            downweight += factor;
            factor *= 1.5;
            s--;
        }
        if (downweight > 1f) {
            downweight = 1f;
        }
        this.filterScore = this.filterScore * (1f - downweight);
        /*
         * filterscore is now at least zero
         */

    }

    /**
     * @return the nth {@link jannovar.exome.Variant Variant} object for this
     * gene.
     */
    public VariantEvaluation getNthVariant(int n) {
        if (n >= this.variantList.size()) {
            return null;
        } else {
            return this.variantList.get(n);
        }
    }

    /**
     * Construct the gene by adding the first variant that affects the gene. If
     * the current gene has additional variants, they will be added using the
     * function add_variant.
     *
     * @param var A variant located in this gene.
     */
    public Gene(VariantEvaluation var) {
	variantList = new ArrayList();
	variantList.add(var);
	this.relevanceMap = new HashMap();
    }

    /**
     * This function adds additional variants to the current gene. The variants
     * have been identified by parsing the VCF file.
     *
     * @param var A Variant affecting the current gene.
     */
    public void addVariant(VariantEvaluation var) {
        this.variantList.add(var);
    }

    /**
     * @param rel Result of a prioritization algorithm
     * @param type an integer constant from {@link exomizer.common.FilterType FilterType}
     * representing the filter type
     */
    public void addRelevanceScore(GeneScore rel, PriorityType type) {
	this.relevanceMap.put(type,rel);
    }

    /**
     * @param type an integer constant from {@link jannovar.common.Constants Constants}
     * representing the filter type
     * @return The IRelevance object corresponding to the filter type.
     */
    public float getRelevanceScore(PriorityType type) {
	GeneScore ir = this.relevanceMap.get(type);
	if (ir == null) {
	    return 0f; /* This should never happen, but if there is no relevance score, just return 0. */
	}
	return ir.getScore();
    }

    /**
     * @return A list of all variants in the VCF file that affect this gene.
     */
    public List<VariantEvaluation> getVariantList() {
        return variantList;
    }
    
    public void resetRelevanceScore(PriorityType type, float newval) {
	GeneScore rel = this.relevanceMap.get(type);
	if (rel == null) {
	    return;/* This should never happen. */
	}
	rel.resetScore(newval);
    }

    /**
     * Note that currently, the EntrezGene IDs are associated with the Variants.
     * Probably it would be more natural to associate that with a field of this
     * Gene object. For now, leave it as be, and return an UNINITIALIZED_INT
     * flag if this gene has no {@link jannovar.exome.Variant Variant} objects.
     *
     * @return the NCBI Entrez Gene ID associated with this gene (extracted from
     * one of the Variant objects)
     */
    public int getEntrezGeneID() {
        if (this.variantList.isEmpty()) {
            return Constants.UNINITIALIZED_INT;
        } else {
            VariantEvaluation ve = this.variantList.get(0);
            return ve.getVariant().getEntrezGeneID();
        }
    }

    /** 
     * @return the map of {@link exomizer.priority.IRelevanceScore  GeneScore} 
     * objects that represent the result of filtering 
     */
    public Map<PriorityType,GeneScore> getRelevanceMap() { return this.relevanceMap; }
    
    /**
     * Note that currently, the gene symbols are associated with the Variants.
     * Probably it would be more natural to associate that with a field of this
     * Gene object. For now, leave it as be, and return "-" if this gene has no  {@link jannovar.exome.Variant Variant}
     * objects.
     *
     * @return the symbol associated with this gene (extracted from one of the
     * Variant objects)
     */
    public String getGeneSymbol() {
        if (this.variantList.isEmpty()) {
            return "-";
        } else {
            VariantEvaluation ve = this.variantList.get(0);
            return ve.getVariant().getGeneSymbol();
        }
    }

    /**
     * Calculates the total priority score for this gene based on data stored in
     * its associated
     * {@link jannovar.exome.Variant Variant} objects. Note that for assumed
     * autosomal recessive variants, the mean of the worst two variants is
     * taken, and for other modes of inheritance,the since worst value is taken.
     * <P> Note that we <b>assume that genes have been filtered for mode of
     * inheritance before this function is called. This means that we do not
     * need to apply separate filtering for mode of inheritance here</b>. The
     * only thing we need to watch out for is whether a variant is homozygous or
     * not (for autosomal recessive inheritance, these variants get counted
     * twice).
     *
     * @param mode Autosomal recessive, doiminant, or X chromosomal recessive.
     */
    public void calculateFilteringScore(ModeOfInheritance mode) {
        this.filterScore = 0f;
        if (variantList.size() == 0) {
            return;
        }
        List<Float> vals = new ArrayList<Float>();
        if (mode == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
            for (VariantEvaluation ve : this.variantList) {
                float x = ve.getFilterScore();
                vals.add(x);
                GenotypeCall gc = ve.getVariant().getGenotype();
                if (Gene.pedigree.containsCompatibleHomozygousVariant(gc)) {
                    vals.add(x); /*
                     * Add the value a second time, it is homozygous
                     */
                }
            }
        } else { /*
             * not autosomal recessive
             */
            for (VariantEvaluation ve : this.variantList) {
                float x = ve.getFilterScore();
                vals.add(x);
            }
        }
        Collections.sort(vals, Collections.reverseOrder()); /*
         * Sort in descending order
         */
        if (mode == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
            if (vals.size() < 2) {
                return; /*
                 * Less than two variants, cannot be AR
                 */
            }
            float x = vals.get(0);
            float y = vals.get(1);
            this.filterScore = (x + y) / (2f);
        } else {
            /*
             * Not autosomal recessive, there is just one heterozygous mutation
             * thus return only the single best score.
             */
            this.filterScore = vals.get(0);
        }

    }

    /**
     * Calculate the combined priority score for this gene (the result is stored
     * in the class variable
     * {@link exomizer.exome.Gene#priorityScore}, which is used to help sort the
     * gene.
     */
     public void calculatePriorityScore() {
	 this.priorityScore  = 1f;
         for (Entry<PriorityType, GeneScore> entry : relevanceMap.entrySet()) {
	    GeneScore r = entry.getValue();
	    float x = r.getScore();
	    priorityScore *= x;
	 }
     }

    /**
     * @return A list order by descending variant score .
     */
    public List<VariantEvaluation> get_ordered_variant_list() {
        List<Float> vals = new ArrayList<Float>();
        for (VariantEvaluation ve : this.variantList) {
            Variant v = ve.getVariant();
            float x = ve.getFilterScore();
            vals.add(x);
        }
        Collections.sort(vals, Collections.reverseOrder()); /*
         * Sort in descending order
         */

        List<VariantEvaluation> new_variant_list = new ArrayList<VariantEvaluation>();
        for (float val : vals) {
            for (VariantEvaluation ve : this.variantList) {
                Variant v = ve.getVariant();
                float x = ve.getFilterScore();
                if (x == val && !new_variant_list.contains(ve)) {
                    new_variant_list.add(ve);// May be bug where have equal scores - vars will get added twice
                }
            }
        }

        return new_variant_list;
    }

    /**
     * @return true if the variants for this gene are consistent with autosomal
     * recessive inheritance, otherwise false.
     */
    public boolean is_consistent_with_recessive() {
        ArrayList<Variant> varList = new ArrayList<Variant>();
        for (VariantEvaluation ve : this.variantList) {
            Variant v = ve.getVariant();
            varList.add(v);
        }
        return Gene.pedigree.isCompatibleWithAutosomalRecessive(varList);
    }

    /**
     * @return true if the variants for this gene are consistent with autosomal
     * dominant inheritance, otherwise false.
     */
    public boolean is_consistent_with_dominant() {
        ArrayList<Variant> varList = new ArrayList<Variant>();
        for (VariantEvaluation ve : this.variantList) {
            Variant v = ve.getVariant();
            varList.add(v);
        }
        return Gene.pedigree.isCompatibleWithAutosomalDominant(varList);
    }

    /**
     * @return true if the variants for this gene are consistent with X
     * chromosomal inheritance, otherwise false.
     */
    public boolean is_consistent_with_X() {
        ArrayList<Variant> varList = new ArrayList<Variant>();
        for (VariantEvaluation ve : this.variantList) {
            Variant v = ve.getVariant();
            varList.add(v);
        }
        return Gene.pedigree.isCompatibleWithXChromosomalRecessive(varList);
    }

    /**
     * @return true if the gene is X chromosomal, otherwise false.
     */
    public boolean is_X_chromosomal() {
        if (this.variantList.size() < 1) {
            return false;
        }
        VariantEvaluation ve = this.variantList.get(0);
        Variant v = ve.getVariant();
        return v.is_X_chromosomal();
    }

    public boolean is_Y_chromosomal() {
        if (this.variantList.size() < 1) {
            return false;
        }
        VariantEvaluation ve = this.variantList.get(0);
        Variant v = ve.getVariant();
        return v.is_Y_chromosomal();
    }

    /**
     * Calculate the combined score of this gene based on the relevance of the
     * gene (priorityScore) and the predicted effects of the variants
     * (filterScore). <P> Note that this method assumes we have calculate the
     * scores, which is depending on the function {@link #calculateGeneAndVariantScores}
     * having been called.
     *
     * @return a combined score that will be used to rank the gene.
     */
    public float getCombinedScore() {
        if (relevanceMap.get(PriorityType.DYNAMIC_PHENOWANDERER_PRIORITY) != null){
            double logitScore = 1/(1 + Math.exp(-(-14.7538 + 12.0024*priorityScore + 8.2712*filterScore)));//logit model for Exomiser 2
            return (float) logitScore;
        }
        else if (relevanceMap.get(PriorityType.GENEWANDERER_PRIORITY) != null){
            //NB this is based on raw walker score
            double logitScore = 1/(1 + Math.exp(-(-8.67972 + 219.40082*priorityScore + 8.54374*filterScore)));//logit model for Exomiser 2
            return (float) logitScore;
        }
        else{
            return (priorityScore + filterScore) / 2f;
        }
    }

    /**
     * Calculate the priority score of this gene based on the relevance of the
     * gene (priorityScore) <P> Note that this method assumes we have calculate
     * the scores, which is depending on the function {@link #calculateGeneAndVariantScores}
     * having been called.
     *
     * @return a priority score that will be used to rank the gene.
     */
    public float getPriorityScore() {
        return priorityScore;
    }

    /**
     * setter only used for Walker rank based scoring
     */
    public void setPriorityScore(float score) {
        priorityScore = score;
    }

    /**
     * Calculate the filter score of this gene based on the relevance of the
     * gene (filterScore) <P> Note that this method assumes we have calculate
     * the scores, which is depending on the function {@link #calculateGeneAndVariantScores}
     * having been called.
     *
     * @return a filter score that will be used to rank the gene.
     */
    public float getFilterScore() {
        return this.filterScore;
    }

    /**
     * setter only used for Walker rank based scoring
     */
    //public void setFilterScore(float score) {
    //filterScore = score;
    //}
    /**
     * Calculate the gene (priority) and the variant (filtering) scores in
     * preparation for sorting.
     */
    public void calculateGeneAndVariantScores(ModeOfInheritance mode) {
        calculatePriorityScore();
        calculateFilteringScore(mode);
    }

    /**
     * Sort this gene based on priority and filter score. This function
     * satisfies the Interface {@code Comparable}.
     */
    public int compareTo(Gene other) {
        float me = getCombinedScore();
        float you = other.getCombinedScore();
        if (me < you) {
            return 1;
        }
        if (me > you) {
            return -1;
        }
        return 0;
    }

    public Iterator<VariantEvaluation> getVariantEvaluationIterator() {
        Collections.sort(this.variantList);
        return this.variantList.iterator();
    }
}
