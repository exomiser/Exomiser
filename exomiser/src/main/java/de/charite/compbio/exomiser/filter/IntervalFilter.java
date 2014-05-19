package de.charite.compbio.exomiser.filter;


import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import jannovar.common.Constants;
import jannovar.exome.Variant;

import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exome.VariantEvaluation;

import de.charite.compbio.exomiser.exception.ExomizerInitializationException;


/**
 * Filter variants according to a linkage interval. For instance,
 * if the interval is chr2:12345-67890, then we would only keep
 * variants located between positions 12345 and 67890 on chromosome 2.
 * All other variants are discarded.
 * <P>
 * The interval must be given as chr2:12345-67890 (format), otherwise,
 * an error message is given and no filtering is done.
 * @author Peter N Robinson
 * @version 0.08 (April 28, 2013)
 */
public class IntervalFilter implements Filter {
    /** The chromosome of the linkage interval */
    int chromosome;
    /** The 5' position of the linkage interval on the chromosome */
    int from;
    /** The 3' position of the linkage interval on the chromosome */
    int to;
    /** Number of variants before filtering */
    int n_before;
    /** Number of variants after filtering */
    int n_after;
    /** Keeps track of whether we could successfully initialize this filter or whether there 
	parse errors. */
    private boolean successfully_initialized;
     /** A list of messages that can be used to create a display in a HTML page or elsewhere. */
    private List<String> messages = null;

     public IntervalFilter()  {
	this.messages = new ArrayList<String>();
     }

      
    /** This method sets the interval based on a String such as
     chr2:12345-67890. */
    public void setParameters(String par) throws ExomizerInitializationException
    {
	String chr,x,y,tmp;
	this.successfully_initialized=true;
	if (! par.substring(0,3).equals("chr")) {
	    String s = String.format("Error parsing interval string (%s): could not find \"chr\"",par);
	    this.messages.add(s);
	    successfully_initialized=false;
	}
	int i = par.indexOf(':');
	if (i<0) {
	    String s = String.format("Error parsing interval string (%s): could not find \":\"",par);
	    this.messages.add(s);
	    successfully_initialized=false;
	}
	chr = par.substring(0,i);

	try {
	    if (chr.equals("chrX"))  this.chromosome = Constants.X_CHROMOSOME;     // 23
	    else if (chr.equals("chrY")) this.chromosome = Constants.Y_CHROMOSOME; // 24
	    else if (chr.equals("chrM")) this.chromosome = Constants.M_CHROMOSOME;  // 25
	    else {
		tmp = chr.substring(3); // remove leading "chr"
		this.chromosome = Integer.parseInt(tmp);
	    }
	} catch (NumberFormatException e) {  // scaffolds such as chrUn_gl000243 cause Exception to be thrown.
	    String s = String.format("Error parsing interval string (%s) with chromosome (%s): %s",par,chr,e.toString());
	    this.messages.add(s);
	    successfully_initialized=false;
	}
	tmp = par.substring(i+1); /* The +1 is added to skip the ":" after the chromosome */
	i = tmp.indexOf('-');
	if (i<0) {
	    String s = String.format("Error parsing interval string (%s): could not find \"-\"",par);
	    this.messages.add(s);
	    successfully_initialized=false;
	}
	x = tmp.substring(0,i);
	i++;
	y = tmp.substring(i);
	try{
	    this.from = Integer.parseInt(x);
	    this.to   = Integer.parseInt(y);
	    String s = String.format("<ul><li>Restricting variants to the interval: %s</li></ul>\n",par);
	    this.messages.add(s);
	} catch (NumberFormatException e) {  
	    String s = String.format("Error parsing interval string (%s) (%s): %s",par,chr,e.toString());
	    this.messages.add(s);
	    successfully_initialized=false;
	}

    }
    /** Take a list of variants and apply the filter to each variant. If a variant does not
     * pass the filter, remove it. Note that we use an explicit for loop to avoid a
     * java.util.ConcurrentModificationException that occurs with an Iterator implementation.
    */
    public void filterVariants(List<VariantEvaluation> variant_list)
    {
	this.n_before = variant_list.size();
	Iterator<VariantEvaluation> it = variant_list.iterator();
	this.n_before = variant_list.size();
	while (it.hasNext()) {
	    VariantEvaluation ve = it.next();
	    Variant v = ve.getVariant();
	    if (successfully_initialized) {
		int c = v.get_chromosome();
		if (c != this.chromosome) {
		    it.remove();
		    continue;
		}
		/* If we get here, we are on the same chromosome */
		int pos = v.get_position();
		if (pos <= to && pos >= from)
		    continue;
		else {
		 it.remove();
		}
	    }
	}
	this.n_after = variant_list.size();
    }
    /** get a list of messages that represent the process and result of applying the filter. This
	list can be used to make an HTML list for explaining the result to users (for instance).
    */
    public List<String> getMessages(){
	return this.messages;

    }

    public String getFilterName() { return "Interval filter"; }

    /** @return an integer constant (as defined in exomizer.common.Constants) that will act
     * as a flag to generate the output HTML dynamically depending on the filters that the 
     * user has chosen.
     */
    @Override public FilterType getFilterTypeConstant() { return FilterType.INTERVAL_FILTER; }

    /** Get number of variants before filter was applied */
    public int getBefore()
    {
	return this.n_before;
    }
    /** Get number of variants after filter was applied */
    public int getAfter() {

	return this.n_after;
    }

    /** Should this Filter be shown in the HTML output? */
    public boolean displayInHTML() {return false; }
    
    /**
     * Not needed in this class.
     * @param connection An SQL (postgres) connection that was initialized elsewhere.
     */
    @Override public void setDatabaseConnection(java.sql.Connection connection) 
	throws ExomizerInitializationException  { /* no-op. */ }

}