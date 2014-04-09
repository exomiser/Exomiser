package de.charite.compbio.exomiser.priority;




import java.util.ArrayList;

import jannovar.common.Constants;

/**
 * Filter Variants on the basis of OWLSim phenotypic comparisons between the HPO clinical phenotypes
 * associated with the disease being sequenced and MP annotated ZFIN fish models.
 * The ZFINPhenodigmTriage is created by the ZFINPhenodigmFilter, one for each tested
 * variant. The ZFINPhenodigmTriage object can be used to ask whether the variant
 * passes the filter, in this case whether it the fish gene scores greater than the threshold in. 
 * If no information is available the filter is not applied (ergo the Variant does not fail the filter).
 * <P>
 * This code was extended on Feb 1, 2013 to show links to the ZFIN webpage for the model in question.
 * @author Damian Smedley
 * @version 0.05 (April 2,2013).
 */
public class ZFINPhenodigmRelevanceScore implements IRelevanceScore {
    /** The phenodigm score as calculated by OWLsim. This score indicates the 
     * similarity between a humam disease and the phenotype of a genetically
     * modified fish model.*/
    private float ZFIN_Phenodigm;
    /**
     * The ZFIN id of the model most similar to the gene being analysed.
     * For instance, the ZFIN id ZFIN:101757 corresponding to the webpage
     * {@code http://www.informatics.jax.org/marker/ZFIN:101757} describes
     * the gene Cfl1 (cofilin 1, non-muscle) and the phenotypic features
     * associated with the several fish models that have been made to
     * investigate this gene.
     */
    private String ZFIN_ID=null;
    /**
     * The gene symbol corresponding to the fish gene, e.g., Cfl1.
     */
    private String geneSymbol=null;

    //private static final float NO_DATA = 0.3f;
    
    /**
     * @param zfin_id An ID from Mouse Genome Informatics such as ZFIN:101757
     * @param gene The corresponding gene symbol, e.g., Gfl1
     * @param PHENODIGM_ZFIN the phenodigm score for this gene.
     */
    public ZFINPhenodigmRelevanceScore(String zfin_id,String gene, float PHENODIGM_ZFIN) {
	this.ZFIN_ID=zfin_id;
	this.geneSymbol = gene;
	this.ZFIN_Phenodigm =  PHENODIGM_ZFIN;
    }

    /** If we have not data for the object, then we simply create a noData object
	that is initialized with flags such that we "know" that this object was not
	initialized. The purpose of this is so that we do not throuw away Variants if 
	there is no data about them in our database -- presumably, these are really rare.
    */
   public static ZFINPhenodigmRelevanceScore createNoDataRelevanceObject()
    {
	ZFINPhenodigmRelevanceScore rscore = new ZFINPhenodigmRelevanceScore(null, null, Constants.UNINITIALIZED_FLOAT);
	return rscore;
    }
    
    /**
     * @return Relevance score for the current Gene
     */
    @Override public float getRelevanceScore(){
    	if (ZFIN_Phenodigm == Constants.UNINITIALIZED_FLOAT){
	    return 0.1f;// model exists but no hit to this disease
    	}
    	else if (ZFIN_Phenodigm == Constants.NOPARSE_FLOAT){
	    return 0.5f;// no model exists in ZFIN
    	}
    	else{
	    return ZFIN_Phenodigm;
    	}
    }


      
    /** @return A string with a summary of the filtering results .*/
    public String getFilterResultSummary() {return null;}
    /** @return A list with detailed results of filtering. The list is intended to be displayed as an HTML list if desired. */
    public java.util.ArrayList<String> getFilterResultList() {
	ArrayList<String> L = new ArrayList<String>();
	if (ZFIN_Phenodigm == Constants.UNINITIALIZED_FLOAT) {
	    L.add("ZFIN Phenodigm: no hit for this disease");
	} else if (ZFIN_Phenodigm == Constants.NOPARSE_FLOAT){
	    L.add("ZFIN Phenodigm: no fish model for this gene");
	} else  {
	    String s1 = String.format("ZFIN Phenodigm: (%.3f%%)",100*ZFIN_Phenodigm);
	    L.add(s1);
	}
	return L;
    }


    /**
     * @return HTML code with score the Phenodigm score for the current gene or a message if no ZFIN data was found.
     */
    @Override  public String getHTMLCode() {
	if (ZFIN_Phenodigm == Constants.UNINITIALIZED_FLOAT) {
	    return "<ul><li>ZFIN Phenodigm: no hit for this disease</li></ul>";
	} else if (ZFIN_Phenodigm == Constants.NOPARSE_FLOAT){
	    return "<ul><li>ZFIN Phenodigm: no fish model for this gene</li></ul>";
    	} else  {
	    String link = getHTMLLink();
	    String s1 = String.format("<ul><li>ZFIN: %s: Phenodigm score: %.3f%%</li></ul>",link,100*ZFIN_Phenodigm);
	    return s1;
    	}
    }

    /**
     * This function creates an HTML anchor link for a ZFIN id, e.g., 
     * for ZFIN:101757 it will create a link to
     * {@code http://www.informatics.jax.org/marker/ZFIN:101757}.
     */

    private String getHTMLLink() {
	String url = String.format("http://zfin.org/action/marker/view/%s",this.ZFIN_ID);
	String anchor = String.format("<a href=\"%s\">%s</a>",url,this.geneSymbol);
	return anchor;
    }

     @Override public void resetRelevanceScore(float newscore){ /* not implemented */ }
}