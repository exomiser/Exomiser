package de.charite.compbio.exomiser.filter;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;


import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.VariantEvaluation;




public class BedFilter implements Filter {
     /** Number of variants analyzed by filter */
    private int n_before;
    /** Number of variants passing filter */
    private int n_after;

    private final FilterType filterType = FilterType.BED_FILTER;
   
    private List<String> messages = null;

    /**
     * A set of off-target variant types such as Intergenic that
     * we will filter out from further consideration.
     */
    private Set<String> targetGenes=null;


    /**
     * The constructor initializes the set of off-target {@link jannovar.common.VariantType VariantType}
     * constants, e.g., INTERGENIC, that we will filter out using this class.
     */
    public BedFilter(Set<String> genes) {
	this.targetGenes = genes;
	
	this.messages = new ArrayList<String>();
    }


     /** This method is required by the interface but not needed by this class. */
    @Override public void setParameters(String par) throws ExomizerInitializationException{
	/* nothing to do here */
    }

    /** get a list of messages that represent the process and result of applying the filter. This
	list can be used to make an HTML list for explaining the result to users (for instance).
    */
    @Override public List<String> getMessages() {

	return this.messages;
    }

    @Override public String getFilterName() {
	return "Gene panel target region (Bed filter)";
    }
    /** @return an integer constant (as defined in exomizer.common.Constants) that will act
     * as a flag to generate the output HTML dynamically depending on the filters that the 
     * user has chosen.
     */
    @Override public FilterType getFilterType() { return filterType; }

    /** Get number of variants before filter was applied */
    @Override public int getBefore() {return this.n_before; }
    /** Get number of variants after filter was applied */
    @Override public int getAfter(){return this.n_after; }

    /** Should this Filter be shown in the HTML output? */
    public boolean displayInHTML() {
	return true;
    }

     /** Take a list of variants and apply the filter to each variant. If a variant does not
	pass the filter, remove it. */
    @Override public void filterVariants(List<VariantEvaluation> variant_list) {
	if (variant_list.size()==0) {
	    System.err.println("[Error: TargetFilter.java] Size of variant list is zero");
	    return;
	}
	Set<String> nontargetGenes = new HashSet<String> ();
	
	
	Iterator<VariantEvaluation> it = variant_list.iterator();
	this.n_before = variant_list.size();
	while (it.hasNext()) {
	    VariantEvaluation ve = it.next();
	    if (ve.isOffExomeTarget()) {
		it.remove();
		continue;
	    }
	    String gs = ve.getGeneSymbol();
	    if (this.targetGenes.contains(gs)) {
		continue;
	    } else {
		nontargetGenes.add(gs);
		it.remove(); 
	    }
	}
	this.n_after = variant_list.size();
	int removed = n_before - n_after;
	String s = String.format("Removed a total of %d off-target variants from further consideration",removed);
	this.messages.add(s);
	s = "Off target variants are defined as intergenic or intronic but not in splice sequences";
	this.messages.add(s);
	StringBuilder sb = new StringBuilder();
	int n = nontargetGenes.size();
	if (n>0) {
	    sb.append("Variants were found in " + n +  " off target genes: ");
	    Iterator<String> iter = nontargetGenes.iterator();
	    boolean notfirst=false;
	    while (iter.hasNext()) {
		String g = iter.next();
		if (notfirst)
		    sb.append(", ");
		notfirst=true;
		sb.append(g);
	    }
	    sb.append(". Variants in these off-target genes were not further considered in the analysis.");
	    this.messages.add(sb.toString());
	}

    }

    
    /**
     * Not needed in this class.
     * @param connection An SQL (postgres) connection that was initialized elsewhere.
     */
//    @Override public void setDatabaseConnection(java.sql.Connection connection) { /* no-op. */ }


}