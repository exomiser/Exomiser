package de.charite.compbio.exomiser.priority;




import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import jannovar.common.ModeOfInheritance;

import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;


/**
 * Prioritize genes according to the mode of inheritance. Essentially,
 * we pass the query on to the {@link exomizer.exome.Gene Gene}
 * objects, that in turn analyze the {@link jannovar.genotype.GenotypeCall GenotypeCall}
 * objects of each {@link jannovar.exome.Variant Variant} to determine if
 * the distribution of variants is compatible with a given
 * {@link jannovar.common.ModeOfInheritance ModeOfInheritance}.
 * <P>
 * This is implemented as a knockout. That is,
 * if the variants are not compatible with the mode of inheritance, the
 * genes are simply removed.
 * @author Peter N Robinson
 * @version 0.13 (13 May, 2013)
 */
public class InheritancePriority implements Priority {
    /** Number of variants before filtering */
    private int n_before;
    /** Number of variants after filtering */
    private int n_after;
    
    /** One of AD, AR, or XR (X chromosomal recessive). If uninitialized,
	this prioritizer has no effect). */
    private ModeOfInheritance inheritanceMode = ModeOfInheritance.UNINITIALIZED;

    /** A list of messages that can be used to create a display in a HTML page or elsewhere. */
    private List<String> messages = null;


    public InheritancePriority() {
	this.messages = new ArrayList<String>();
     }

    public InheritancePriority(ModeOfInheritance modeOfInheritance) {
        this.inheritanceMode = modeOfInheritance;
        this.messages = new ArrayList();
    }
   
   @Override 
   public void setParameters(String par) throws ExomizerInitializationException {
	this.inheritanceMode = InheritancePriority.getModeOfInheritance(par);
	if (this.inheritanceMode ==  ModeOfInheritance.UNINITIALIZED)
	    messages.add("Could not initialize the Inheritance Filter for parameter: \"" + par + "\"");
    }

    /**
     * @param s A String (one of "AR","AD","X") representing the mode of inheritance.
     * @return The corresponding constant or UNITIALIZED if s could not be parsed.
     */
    public static ModeOfInheritance getModeOfInheritance(String s) {
	if (s.equalsIgnoreCase("AR"))
	    return ModeOfInheritance.AUTOSOMAL_RECESSIVE;
	else if (s.equalsIgnoreCase("AD"))
	    return ModeOfInheritance.AUTOSOMAL_DOMINANT;
	else if (s.equalsIgnoreCase("X"))
	    return ModeOfInheritance.X_RECESSIVE;
	else 
	    return ModeOfInheritance.UNINITIALIZED;

    }
    
    @Override public String getPriorityName() { return "Mode of Inheritance"; }

    /**  Flag for output field representing the Inheritance pattern filter. */
    @Override public PriorityType getPriorityTypeConstant() { return PriorityType.INHERITANCE_MODE_PRIORITY; } 
    
    /**
     * @return list of messages representing process, result, and if any, errors of frequency filtering. 
   */
    @Override public List<String> getMessages() {
	return this.messages;
    }  

    
    /** Get number of variants before filter was applied */
    @Override public int getBefore() { return this.n_before; }
    /** Get number of variants after filter was applied */
    @Override public int getAfter() { return this.n_after; }

    /** This function filters the list of genes as to whether they
     * contain one or more variants that are compatible with
     * the indicated mode of inheritance. The actual work is done
     * in the {@link exomizer.exome.Gene Gene} class.
     * @param gene_list list of genes to be filtered.
     * <P>
     * Note that if a Gene contains at least one variant that is compatible
     * with the indicated mode of inheritance, it is retained, together
     * with all of the variants it has (users may want to see all of the
     * variants present in some gene). If a Gene contains no variant that
     * is compatible with the indicated inheritance pattern, it is 
     * completely removed.
     */
    @Override public void prioritizeGenes(List<Gene> gene_list)
    {	  
	Iterator<Gene> it = gene_list.iterator();	  
	this.n_before = gene_list.size();
	if (inheritanceMode == ModeOfInheritance.UNINITIALIZED){
	    this.n_after=this.n_before;
	    return;
	}

	int n_genes=0;
	int n_compatible_genes=0;
	while (it.hasNext()) {  
	    Gene g = it.next();
	    n_genes++;
	    if (inheritanceMode ==  ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
		if (g.is_consistent_with_recessive()) {
		    n_compatible_genes++;
		} else {
		    it.remove();
		}
	    } else if (inheritanceMode ==  ModeOfInheritance.AUTOSOMAL_DOMINANT) {
		if (g.is_consistent_with_dominant()) {
		    n_compatible_genes++;
		}  else {
		    it.remove();
		}
	    } else if (inheritanceMode  == ModeOfInheritance.X_RECESSIVE) {
		if (g.is_consistent_with_X()) {
		    n_compatible_genes++;
		} else {
		    it.remove();
		}
	    }  	    
	}
	this.n_after =  gene_list.size();
	String inh=null;
	if (inheritanceMode  ==  ModeOfInheritance.AUTOSOMAL_RECESSIVE)
	    inh = "autosomal recessive";
	else if (inheritanceMode ==  ModeOfInheritance.AUTOSOMAL_DOMINANT)
	    inh = "autosomal dominant";
	else if (inheritanceMode  == ModeOfInheritance.X_RECESSIVE)
	    inh = "X chromosomal";
	String s = String.format("Total of %d genes were analyzed. %d had variants with distribution compatible with %s inheritance.",
				 n_genes,n_compatible_genes,inh);
	this.messages.add(s);
    }


    /**
     * If this filter was applied, then show a brief summary of the results in the HTML output
     */
    @Override public boolean displayInHTML() { 
	return true;
    }

    /**
     * @return HTML code to display an unordered list with inheritance results.
     */
    public String getHTMLCode() {
	StringBuilder sb = new StringBuilder();
	sb.append("<ul>\n");
	for (String s : this.messages) {
	    sb.append(String.format("<li>%s</li>\n",s));
	}
	sb.append("</ul>\n");
	return sb.toString();
    }
    
    /**
     * This class does not need a database connection, this function only there to satisfy the interface.
     * @param connection An SQL (postgres) connection that was initialized elsewhere.
     */
    @Override  public void setDatabaseConnection(java.sql.Connection connection) 
	throws ExomizerInitializationException  { /* no-op */ }
    

}