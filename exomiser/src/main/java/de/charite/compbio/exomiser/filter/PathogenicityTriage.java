package de.charite.compbio.exomiser.filter;

import jannovar.common.Constants;
import jannovar.common.VariantType;
import jannovar.exome.Variant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter Variants on the basis of the predicted pathogenicity. This class
 * filters both on variant class (NONSENSE, MISSENSE, INTRONIC) etc., as well as
 * on the basis of MutationTaster/Polyphen2/SIFT scores for mutations.
 *
 * @author Peter N Robinson
 * @version 0.17 (3 February, 2014)
 *
 */
public class PathogenicityTriage implements Triage {

    private static final Logger logger = LoggerFactory.getLogger(PathogenicityTriage.class);
    /**
     * Pathogenicity score according to POLYPHEN2
     */
    private float polyphen = Constants.UNINITIALIZED_FLOAT;
    /**
     * Pathogenicity score according to MutTaster
     */
    private float mutation_taster = Constants.UNINITIALIZED_FLOAT;
    /**
     * Pathogenicity score according to SIFT.
     */
    private float sift = Constants.UNINITIALIZED_FLOAT;

    private float cadd_raw = Constants.UNINITIALIZED_FLOAT;

    /**
     * Category of current variant, e.g., MISSENSE, SLICE...
     */
    private VariantType variantType;
    /**
     * The overall value of the estimated pathogenicity of this mutation. Must
     * be a value between 0 and 1. 0: Completely sure it is nonpathogenic 1.0:
     * Completely sure it is pathogenic
     */
    private float pathogenicityScore = Constants.UNINITIALIZED_FLOAT;
    /**
     * Pathogenicity score for a mutation class such as INTERGENIC where we are
     * entirely sure it is nonpathogenic (for the purposes of this software).
     */
    private static final float NON_PATHOGENIC = 0.00f;
    /**
     * Assumed pathogenicity score for a frameshift mutation.
     */
    private static final float FRAMESHIFT_SCORE = 0.95f;
    /**
     * Assumed pathogenicity score for a nonframeshift indel mutation.
     */
    private static final float NONFRAMESHIFT_INDEL_SCORE = 0.85f;
    /**
     * Assumed pathogenicity score for a nonsense mutation.
     */
    private static final float NONSENSE_SCORE = 0.95f;
    /**
     * Assumed pathogenicity score for a splice site mutation.
     */
    private static final float SPLICING_SCORE = 0.90f;
    /**
     * Assumed pathogenicity score for a synonymous mutation.
     */
    private static final float SYNONYMOUS_SCORE = 0.10f;
    /**
     * Assumed pathogenicity score for a stoploss mutation.
     */
    private static final float STOPLOSS_SCORE = 0.70f;
    /**
     * Assumed pathogenicity score for a variant that causes the start codon to
     * be lost.
     */
    private static final float STARTLOSS_SCORE = 0.95f;
    /**
     * Overall threshold for non-missense mutation pathogenicity.
     */
    private static float PATHOGENICITY_SCORE_THRESHOLD = 0.5f;
    /**
     * A SIFT score below this threshold is considered to be pathogenic
     */
    private static final float SIFT_THRESHOLD = 0.06f;
    /**
     * Possibly damaging is > 0.446 with Polyphen2 (this is an intermediate
     * category, thus, we are not being extremely strict with the polyphen
     * filter).
     */
    private static final float POLYPHEN_THRESHOLD = 0.446f;
    /**
     * This is the pathogenicity value we will give to missense (nonsynonymous)
     * variants for which we cannot find values for mutationTaster, polyphen2,
     * or SIFT.
     */
    private static final float DEFAULT_MISSENSE_SCORE = 0.6f;
    /**
     * A polyphen2 score above this threshold is probably damaging
     */
    private static final float POLYPHEN_PROB_DAMAGING_THRESHOLD = 0.956f;
    /**
     * TODO CHeck whether this threshold is correct. Note it is the dbNSFP
     * normalized version of mutation taster?
     */
    private static final float MTASTER_THRESHOLD = 0.94f;
    private static boolean useMisSenseFiltering = false;

    /**
     * This constructor is intended to be used for MISSENSE mutations, and the
     * pathogenicity filter is based upon the classification of the variant by
     * polyphen, mutation taster and sift. The constructor also calculates an
     * overall pathogenicity score based on the predictions of the three
     * pathogenicity programs.
     *
     * @param poly Polyphen2 score
     * @param mt MutationTaster score
     * @param SIFT SIFT score
     * @param CADD CADD_RAW score
     */
    public PathogenicityTriage(float poly, float mt, float SIFT, float CADD) {
        this.polyphen = poly;
        this.mutation_taster = mt;
        this.sift = SIFT;
        this.cadd_raw = CADD;
        this.variantType = VariantType.MISSENSE;
        //now get the worstest pathogenicity score!
        if (poly < 0 && mt < 0 && SIFT < 0) {
            //i.e., all three pathogenicity scores are not available
            this.pathogenicityScore = DEFAULT_MISSENSE_SCORE;
        } else {
            float m = Math.max(poly, mt);
            // only test SIFT if initialised - otherwise 1 - -5 - 6
            if (SIFT >= 0) {
                m = Math.max(m, 1f - SIFT);
            }
            this.pathogenicityScore = m;
        }
    }

    /**
     * This constructor is intended to be used for all variants except MISSENSE.
     */
    public PathogenicityTriage(VariantType type, float pathogenicity, float CADD) {
        this.variantType = type;
        this.pathogenicityScore = pathogenicity;
        this.cadd_raw = CADD;
    }

    private boolean sift_is_initialized() {
        return this.sift > -0.5f;
    }

    private boolean polyphen_is_initialized() {
        return this.polyphen > -0.5f;
    }

    private boolean mutation_taster_is_initialized() {
        return this.mutation_taster > -0.05f;
    }

    private boolean no_prediction_initialized() {
        return (this.sift < 0f && this.polyphen < 0f && this.mutation_taster < 0f);
    }

    /**
     * Defaults to false. Set to true if you wish to 
     * @param useMisSenseFiltering
     */
    public static void setUseMissenseFiltering(boolean useMisSenseFiltering) {
        PathogenicityTriage.useMisSenseFiltering = useMisSenseFiltering;
    }

    public static void keepSynonymousVariants() {
        logger.info("SETTING PATHOGENICITY_SCORE_THRESHOLD TO 0");
        PathogenicityTriage.PATHOGENICITY_SCORE_THRESHOLD = 0;
    }

    /**
     * @return true if the variant being analyzed passes the filter (e.g., has
     * high quality )
     */
    public boolean passesFilter() {
        if (this.variantType == VariantType.MISSENSE) {
            if (sift_is_initialized() && this.sift < SIFT_THRESHOLD) {
                return true;
            }
            if (polyphen_is_initialized() && this.polyphen > POLYPHEN_THRESHOLD) {
                return true;
            }
            if (mutation_taster_is_initialized() && this.mutation_taster > MTASTER_THRESHOLD) {
                return true;
            }
            if (no_prediction_initialized()) {
                return true;
            }
            if (!useMisSenseFiltering) {
                //TODO: I'm not sure what this is trying to achieve - the MISSENSE variants have already been evaluated by this point.
                return true;//no SIFT, PolyPhen, MT filtering
            } else { //redundant else 
                return false;// user-specified filtering implemented 
            }
        } else {
            return this.pathogenicityScore >= PATHOGENICITY_SCORE_THRESHOLD;
        }
    }

    /**
     * This method creates a PathogenicityTriage object for certain kinds of
     * variants. Note that NONSYNONYMOUS variants are not evaluated in this way,
     * rather they are evaluated with specific scores (MutationTaster, SIFT, and
     * Polyphen2). This function basically just assigns a score based on the
     * class of the variant using the function <i>getVariantTypeConstant</i>.
     * Probably this can be simplified by just using this directly. The only
     * thing that this function does that is additional is basically to check
     * that the variant type is one of the registered types. TODO
     */
    public static PathogenicityTriage evaluateVariantClass(Variant v, float CADD) {
        VariantType variantType = v.getVariantTypeConstant();
        if (!variantType.isTopPriorityVariant()) {
            return createNonPathogenicTriageObject(variantType, CADD);
        }
        switch (variantType) {

            case FS_DELETION:
                return createPathogenicTriageObjectByType(VariantType.FS_DELETION, FRAMESHIFT_SCORE, CADD);
            case FS_INSERTION:
                return createPathogenicTriageObjectByType(VariantType.FS_INSERTION, FRAMESHIFT_SCORE, CADD);
            case NON_FS_SUBSTITUTION:
                return createPathogenicTriageObjectByType(VariantType.NON_FS_SUBSTITUTION, NONFRAMESHIFT_INDEL_SCORE, CADD);
            case FS_SUBSTITUTION:
                return createPathogenicTriageObjectByType(VariantType.FS_SUBSTITUTION, FRAMESHIFT_SCORE, CADD);
            case NON_FS_DELETION:
                return createPathogenicTriageObjectByType(VariantType.NON_FS_DELETION, NONFRAMESHIFT_INDEL_SCORE, CADD);
            case NON_FS_INSERTION:
                return createPathogenicTriageObjectByType(VariantType.NON_FS_INSERTION, NONFRAMESHIFT_INDEL_SCORE, CADD);
            case SPLICING:
                return createPathogenicTriageObjectByType(VariantType.SPLICING, SPLICING_SCORE, CADD);
            case STOPGAIN:
                return createPathogenicTriageObjectByType(VariantType.STOPGAIN, NONSENSE_SCORE, CADD);
            case STOPLOSS:
                return createPathogenicTriageObjectByType(VariantType.STOPLOSS, STOPLOSS_SCORE, CADD);
            //Note, the frameshift duplication get the FRAMESHIFT default score
            case FS_DUPLICATION:
                return createPathogenicTriageObjectByType(VariantType.FS_DUPLICATION, FRAMESHIFT_SCORE, CADD);
            case NON_FS_DUPLICATION:
                return createPathogenicTriageObjectByType(VariantType.NON_FS_DUPLICATION, NONFRAMESHIFT_INDEL_SCORE, CADD);
            case START_LOSS:
                return createPathogenicTriageObjectByType(VariantType.START_LOSS, STARTLOSS_SCORE, CADD);
            default:
                //(we should actually never get here).
                return createNonPathogenicTriageObject(VariantType.ERROR, CADD);
        }
    }

    /**
     * @return return a float representation of the filter result [0..1]. Note
     * that 0 means predicted to be non-pathogenic, and 1.0 means maximally
     * pathogenic prediction.
     */
    @Override
    public float filterResult() {
        return this.pathogenicityScore;
    }

    public float getPolyphen() {
        return this.polyphen;
    }

    public float getSift() {
        return this.sift;
    }

    public float getMutTaster() {
        return this.mutation_taster;
    }

    public float getCADDRaw() {
        return this.cadd_raw;
    }

    /**
     * @return A string with a summary of the filtering results for HTML
     * display.
     */
    public String getFilterResultSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<b>%s</b><br/>\n", this.variantType.toString()));
        if (this.variantType != VariantType.MISSENSE) {
            sb.append(String.format("Path score: %.3f<br/>\n", this.pathogenicityScore));
        } else {
            //how many precitions do we have?
            int c = 0;
            String s = null;
            if (mutation_taster_is_initialized()) {
                if (this.mutation_taster > MTASTER_THRESHOLD) {
                    s = String.format("Mutation Taster: %.1f (P)", this.mutation_taster);
                } else {
                    s = String.format("Mutation Taster: %.1f", this.mutation_taster);
                }
                sb.append(s + "<br/>\n");
                c++;
            }
            if (polyphen_is_initialized()) {
                if (this.polyphen > POLYPHEN_PROB_DAMAGING_THRESHOLD) {
                    s = String.format("Polyphen2: %.1f (D)", this.polyphen);
                } else if (this.polyphen > POLYPHEN_THRESHOLD) {
                    s = String.format("Polyphen2: %.1f (P)", this.polyphen);
                } else {
                    s = String.format("Polyphen2: %.1f (B)", this.polyphen);
                }
                sb.append(s + "<br/>\n");
                c++;
            }
            if (sift_is_initialized()) {
                if (this.sift < SIFT_THRESHOLD) {
                    s = String.format("SIFT: %.1f (D)", this.sift);
                } else {
                    s = String.format("SIFT: %.1f (T)", this.sift);
                }
                sb.append(s + "<br/>\n");
                c++;
            }
            if (c == 0) {
                sb.append("No pathogenicity predictions found<br/>\n");
            }
        }
        return sb.toString();
    }

    /**
     * This function returns a list with a summary of the pathogenicity
     * analysis. It first enters the variant type (see
     * {@link jannovar.common.VariantType VariantType}), and then either shows
     * the overall pathogenicity score (defined in several constants in this
     * class such as {@link #FRAMESHIFT_SCORE}), or additionally shows the
     * results of analysis by polyphen2, MutationTaster, and SIFT. This list can
     * be displayed as an HTML list if desired (see
     * {@link exomizer.io.html.HTMLTable HTMLTable}).
     *
     * @return A list with detailed results of filtering.
     */
    public List<String> getFilterResultList() {
        List<String> L = new ArrayList<String>();
        /**
         * First add the variant type, this will be the first display item
         */
        L.add(this.variantType.toString());
        String s = null;
        /**
         * For non-missense mutations, just return the overall type)
         */
        if (this.variantType != VariantType.MISSENSE) {
            s = String.format("Path score: %.3f", this.pathogenicityScore);
            L.add(s);
            return L;
        }

        if (mutation_taster_is_initialized()) {
            if (this.mutation_taster > MTASTER_THRESHOLD) {
                s = String.format("Mutation Taster: %.1f (P)", this.mutation_taster);
            } else {
                s = String.format("Mutation Taster: %.1f", this.mutation_taster);
            }
            L.add(s);
        } else {
            L.add("Mutation Taster: .");
        }
        if (polyphen_is_initialized()) {
            if (this.polyphen > POLYPHEN_PROB_DAMAGING_THRESHOLD) {
                s = String.format("Polyphen2: %.1f (D)", this.polyphen);
            } else if (this.polyphen > POLYPHEN_THRESHOLD) {
                s = String.format("Polyphen2: %.1f (P)", this.polyphen);
            } else {
                s = String.format("Polyphen2: %.1f (B)", this.polyphen);
            }
            L.add(s);
        } else {
            L.add("Polyphen2: .");
        }
        if (sift_is_initialized()) {
            if (this.sift < SIFT_THRESHOLD) {
                s = String.format("SIFT: %.1f (D)", this.sift);
            } else {
                s = String.format("SIFT: %.1f (T)", this.sift);
            }
            L.add(s);
        } else {
            L.add("SIFT: .");
        }
        s = String.format("Path score: %.3f", this.pathogenicityScore);
        L.add(s);
        return L;
    }

    @Override
    public String getHTMLCode() {
        StringBuilder sb = new StringBuilder();
        String s = this.variantType.toString();
        sb.append("<ul><li><b>" + s + "</b></li>\n");
        /**
         * For non-missense mutations, just return the overall type)
         */
        if (mutation_taster_is_initialized()) {
            if (this.mutation_taster > MTASTER_THRESHOLD) {
                s = String.format("Mutation Taster: %.3f (P)", this.mutation_taster);
            } else {
                s = String.format("Mutation Taster: %.3f", this.mutation_taster);
            }
            sb.append("<li>" + s + "</li>\n");;
        }
        if (polyphen_is_initialized()) {
            if (this.polyphen > POLYPHEN_PROB_DAMAGING_THRESHOLD) {
                s = String.format("Polyphen2: %.3f (D)", this.polyphen);
            } else if (this.polyphen > POLYPHEN_THRESHOLD) {
                s = String.format("Polyphen2: %.3f (P)", this.polyphen);
            } else {
                s = String.format("Polyphen2: %.3f (B)", this.polyphen);
            }
            sb.append("<li>" + s + "</li>");
        }
        if (sift_is_initialized()) {
            if (this.sift < SIFT_THRESHOLD) {
                s = String.format("SIFT: %.3f (D)", this.sift);
            } else {
                s = String.format("SIFT: %.3f (T)", this.sift);
            }
            sb.append("<li>" + s + "</li>");
        }
        s = String.format("Path score: %.3f", this.pathogenicityScore);
        sb.append("<li>" + s + "</li>");
        sb.append("</ul>\n");
        return sb.toString();
    }

    /**
     * If we have not data for the object, then we simply create a noData object
     * that is initialized with flags such that we "know" that this object was
     * not initialized. The purpose of this is so that we do not throuw away
     * Variants if there is no data about them in our database -- presumably,
     * these are really rare.
     */
    public static PathogenicityTriage createNoDataTriageObject() {
        PathogenicityTriage pt = new PathogenicityTriage(Constants.UNINITIALIZED_FLOAT, Constants.UNINITIALIZED_FLOAT, Constants.UNINITIALIZED_FLOAT, Constants.UNINITIALIZED_FLOAT);
        return pt;
    }

    /**
     * This method creates a PathogenicityTriage object for a variant that we
     * have judged to be non-pathogenic.
     */
    public static PathogenicityTriage createNonPathogenicTriageObject(VariantType type, float CADD) {
        PathogenicityTriage pt = new PathogenicityTriage(type, NON_PATHOGENIC, CADD);
        return pt;
    }

    /**
     * This method creates a PathogenicityTriage object for a variant that we
     * have found to belong to one of the classes of potentially pathogenic
     * variants such as NONSENSE, FRAMESHIFT, etc.
     */
    public static PathogenicityTriage createPathogenicTriageObjectByType(VariantType type, float score, float CADD) {
        PathogenicityTriage pt = new PathogenicityTriage(type, score, CADD);
        return pt;
    }
}
