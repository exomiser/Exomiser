package de.charite.compbio.exomiser.priority;



/**
 * Filter Variants on the basis of Uberpheno semantic similarity measure between the HPO clinical phenotypes
 * associated with the disease being sequenced and MP annotated MGI mouse models and/or Zebrafish phenotypes.
 * @author Sebastian Koehler
 * @version 0.02 (April 2, 2013).
 */
public class UberphenoRelevanceScore implements IRelevanceScore {

	private double uberphenoScore;

	/**
	 * @param uberphenoSemSimScore
	 */
	public UberphenoRelevanceScore(double uberphenoSemSimScore) {
		this.uberphenoScore = uberphenoSemSimScore;
	}



	/* (non-Javadoc)
	 * @see exomizer.priority.IPriority#getRelevanceScore
	 */
	@Override
	public float getRelevanceScore() {
		return (float)uberphenoScore;
	}

	


	/* (non-Javadoc)
	 * @see exomizer.filter.ITriage#getHTMLCode()
	 */
	@Override
	public String getHTMLCode() {
		return "TODO";
	}



     @Override public void resetRelevanceScore(float newscore){ /* not implemented */ }

     @Override public String getFilterResultSummary() {
	return String.format("Uberpheno score: %.2f", 
			     this.uberphenoScore);
    }
    
    /** @return A list with detailed results of filtering. Not yet implemented for gene wanderer. */
    @Override public java.util.ArrayList<String> getFilterResultList(){
	return null;
    }

}