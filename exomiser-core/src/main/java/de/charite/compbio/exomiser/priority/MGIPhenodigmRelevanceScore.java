package de.charite.compbio.exomiser.priority;




import java.util.ArrayList;
import jannovar.common.Constants;

/**
 * Filter Variants on the basis of OWLSim phenotypic comparisons between the HPO clinical phenotypes
 * associated with the disease being sequenced and MP annotated MGI mouse models.
 * The MGIPhenodigmTriage is created by the MGIPhenodigmFilter, one for each tested
 * variant. The MGIPhenodigmTriage object can be used to ask whether the variant
 * passes the filter, in this case whether it the mouse gene scores greater than the threshold in. 
 * If no information is available the filter is not applied (ergo the Variant does not fail the filter).
 * <P>
 * This code was extended on Feb 1, 2013 to show links to the MGI webpage for the model in question.
 * @author Damian Smedley
 * @version 0.05 (April 2, 2013).
 */
public class MGIPhenodigmRelevanceScore implements GeneScore {
    /** The phenodigm score as calculated by OWLsim. This score indicates the 
     * similarity between a humam disease and the phenotype of a genetically
     * modified mouse model.*/
    private float MGI_Phenodigm;
    /**
     * The MGI id of the model most similar to the gene being analysed.
     * For instance, the MGI id MGI:101757 corresponding to the webpage
     * {@code http://www.informatics.jax.org/marker/MGI:101757} describes
     * the gene Cfl1 (cofilin 1, non-muscle) and the phenotypic features
     * associated with the several mouse models that have been made to
     * investigate this gene.
     */
    private String MGI_ID=null;
    /**
     * The gene symbol corresponding to the mouse gene, e.g., Cfl1.
     */
    private String geneSymbol=null;

    //private static final float NO_DATA = 0.3f;
    
    /**
     * @param mgi_id An ID from Mouse Genome Informatics such as MGI:101757
     * @param gene The corresponding gene symbol, e.g., Gfl1
     * @param PHENODIGM_MGI the phenodigm score for this gene.
     */
    public MGIPhenodigmRelevanceScore(String mgi_id,String gene, float PHENODIGM_MGI) {
	this.MGI_ID=mgi_id;
	this.geneSymbol = gene;
	this.MGI_Phenodigm =  PHENODIGM_MGI;
    }

    /** If we have not data for the object, then we simply create a noData object
	that is initialized with flags such that we "know" that this object was not
	initialized. The purpose of this is so that we do not throuw away Variants if 
	there is no data about them in our database -- presumably, these are really rare.
    */
   public static MGIPhenodigmRelevanceScore createNoDataRelevanceObject()
    {
	MGIPhenodigmRelevanceScore rscore = new MGIPhenodigmRelevanceScore(null,null,Constants.UNINITIALIZED_FLOAT);
	return rscore;
    }
    
    /**
     * @return Relevance score for the current Gene
     */
    @Override public float getScore(){
    	if (MGI_Phenodigm == Constants.UNINITIALIZED_FLOAT){
	    return 0.1f;// mouse model exists but no hit to this disease
    	}
    	else if (MGI_Phenodigm == Constants.NOPARSE_FLOAT){
	    return 0.60f;// no mouse model exists in MGI
    	}
    	else{
	    return MGI_Phenodigm;
    	}
    }


      
    /** @return A string with a summary of the filtering results .*/
    public String getFilterResultSummary() {return null;}
    /** @return A list with detailed results of filtering. The list is intended to be displayed as an HTML list if desired. */
    public java.util.ArrayList<String> getFilterResultList() {
	ArrayList<String> L = new ArrayList<String>();
	if (MGI_Phenodigm == Constants.UNINITIALIZED_FLOAT) {
	    L.add("MGI Phenodigm: no hit for this disease");
	} else if (MGI_Phenodigm == Constants.NOPARSE_FLOAT){
	    L.add("MGI Phenodigm: no mouse model for this gene");
	} else  {
	    String s1 = String.format("MGI Phenodigm: (%.3f%%)",100*MGI_Phenodigm);
	    L.add(s1);
	}
	return L;
    }


    /**
     * @return HTML code with score the Phenodigm score for the current gene or a message if no MGI data was found.
     */
    @Override  
    public String getHTMLCode() {
	//if (MGI_Phenodigm == UNINITIALIZED_FLOAT) {
	  //  return "<ul><li>MGI Phenodigm: no hit for this disease</li></ul>";
	if (MGI_Phenodigm == Constants.NOPARSE_FLOAT){
	    return "<ul><li>No mouse model for this gene</li></ul>";
        } else  {
                String link = getHTMLLink();
                //String s1 = String.format("<ul><li>MGI: %s: Phenodigm score: %.3f%%</li></ul>",link,100*MGI_Phenodigm);
                String s1 = String.format("<ul><li>Mouse phenotype data for %s</li></ul>",link);
                return s1;
        }
    }

    /**
     * This function creates an HTML anchor link for a MGI id, e.g., 
     * for MGI:101757 it will create a link to
     * {@code http://www.informatics.jax.org/marker/MGI:101757}.
     */

    private String getHTMLLink() {
	String url = String.format("http://www.informatics.jax.org/marker/%s",this.MGI_ID);
	String anchor = String.format("<a href=\"%s\">%s</a>",url,this.geneSymbol);
	return anchor;
    }

     @Override public void resetScore(float newscore){ /* not implemented */ }
}