package de.charite.compbio.exomiser.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import jannovar.common.Constants;
import jannovar.exome.Variant;

import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exception.ExomizerSQLException;
import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.exome.VariantEvaluation;

/**
 * Filter variants according to their predicted pathogenicity. There are two components
 * to this, which may better be separated in later versions of this software, but I think 
 * there are more advantages to keeping them all in one class.
 * <P>
 * There are variants such as splice site variants, which we can assume are in general pathogenic.
 * We at the moment do not need to use any particular software to evaluate this, we merely take the
 * variant class from the Jannovar code. 
 * <P>
 * For missense mutations, we will use the predictions of MutationTaster, polyphen, and SIFT
 * taken from the data from the dbNSFP project.
 * <P>
 * The code therefore removes mutations judged not to be pathogenic (intronic, etc.), and assigns each other
 * mutation an overall pathogenicity score defined on the basis of "medical genetic intuition".
 * @author Peter N Robinson
 * @version 0.09 (29 December, 2012).
 */
public class PathogenicityFilter implements IFilter {
  
    /** Database handle to the postgreSQL database used by this application. */
    private Connection connection=null;
    /** A prepared SQL statement for thousand genomes frequency. */
    private PreparedStatement getPathogenicityDataStatement = null;
    

    /** A flag to indicate we could not retrieve data from the database for some variant. Using the
	value 200% means that the variant will not fail the ESP filter. */
    private static final float NO_ESP_DATA = 2.0f;
    /** A list of errors encountered during the calculation of the pathogenicity score. */
    private List<String> error_record = null;
    /** A list of messages that can be used to create a display in a HTML page or elsewhere. */
    private List<String> messages = null;
    
    public PathogenicityFilter() throws ExomizerInitializationException  {
	this.error_record = new ArrayList<String>();
	this.messages = new ArrayList<String>();
	this.messages.add("Synonymous and non-coding variants removed");
     }

    @Override public String getFilterName() { return "Pathogenicity filter"; }
    /** Flag to output results of filtering against polyphen, SIFT, and mutation taster. */
    @Override public FilterType getFilterTypeConstant() { return FilterType.PATHOGENICITY_FILTER; } 

 

     /** Sets the frequency threshold for variants.
      * @param par A frequency threshold, e.g., a string such as "0.02"
      */
     public void set_parameters(String par) throws ExomizerInitializationException
     {
	 // Set up the message
	 messages.add("Pathogenicity predictions are based on the dbNSFP-normalized values");
	 messages.add("Mutation Taster: &gt;0.95 assumed pathogenic, prediction categories not shown"); 
	 messages.add("Polyphen2 (HVAR): \"D\" (&gt; 0.956,probably damaging), \"P\": [0.447-0.955], "+
				     "possibly damaging, and \"B\", &lt;0.447, benign.");
	 messages.add("SIFT: \"D\"&lt;0.05, damaging and \"T\"&ge;0.05, tolerated</LI>");
	 PathogenicityTriage.set_missense_filtering(par);
	 return; // note there are no parameters for this filter.
     }

    /**
     * @return list of messages representing process, result, and if any, errors of frequency filtering. 
     */
    @Override
    public List<String> getMessages() {
	if (this.error_record.size()>0) {
	    for (String s : error_record) {
		this.messages.add("Error: " + s);
	    }
	}
	return this.messages;
    }
    /** Number of variants before filtering */
    private int n_before;
    /** Number of variants after filtering */
    private int n_after;

      /** Get number of variants before filter was applied */
    @Override public int getBefore() { return this.n_before; }
    /** Get number of variants after filter was applied */
    @Override public int getAfter() { return this.n_after; }

    /**
     * Remove variants that are deemed to be not-pathogenic, and provide a pathogenicity
     * score for thos ehtat survive the filter.
     */
    @Override public void filter_list_of_variants(List<VariantEvaluation> variant_list)
    {
	Iterator<VariantEvaluation> it = variant_list.iterator();
	
	this.n_before = variant_list.size();
	while (it.hasNext()) {
	    VariantEvaluation ve =  it.next();
	    Variant v = ve.getVariant();
	    try {
		PathogenicityTriage pt = retrieve_pathogenicity_data(v);
		if (! pt.passesFilter() ) {
		    // Variant is not predicted pathogenic, discard it.
		    it.remove();
		} else {
		    // We passed the filter (Variant is predicted pathogenic).
		    ve.addFilterTriage(pt, FilterType.PATHOGENICITY_FILTER);
		}    
	    } catch (ExomizerException e) {
		error_record.add(e.toString());
	    }
	}
	this.n_after =  variant_list.size();
    }

    /** Note that current, ESP only holds frequency data for nucleotide substitutions.
     * Therefore, just skip this step for other kinds of variants. Use the flag
     * 	NO_ESP_DATA to indicate this.
     * @param v A Variant whose frequency is to be retrieved from the SQL database by this function.
     */
    private PathogenicityTriage retrieve_pathogenicity_data(Variant v) throws ExomizerSQLException {
	/** The following classifies variants based upon their variant class (MISSENSE, NONSENSE, INTRONIC).
	 * The actual logic for assigning pathogenicity scores is in the PathogenicityTriage class.
	 */
	if (! v.is_missense_variant () ) {
	    return PathogenicityTriage.evaluateVariantClass(v); 
	}
	
	
	float polyphen = Constants.UNINITIALIZED_FLOAT;
	float mutation_taster = Constants.UNINITIALIZED_FLOAT;
	float sift = Constants.UNINITIALIZED_FLOAT;
	
	int chrom = v.get_chromosome();
	int position = v.get_position();
	/* Note: when we get here, we have tested above that we have a nonsynonymous substitution */
	char ref = v.get_ref().charAt(0);
	char alt = v.get_alt().charAt(0);
	ResultSet rs = null;
	try {
	    this.getPathogenicityDataStatement.setInt(1,chrom);
	    this.getPathogenicityDataStatement.setInt(2,position);
	    this.getPathogenicityDataStatement.setString(3,Character.toString(ref));
	    this.getPathogenicityDataStatement.setString(4,Character.toString(alt));
	    
	    rs = getPathogenicityDataStatement.executeQuery();
	    if ( rs.next() ) { /* The way the db was constructed, there is just one line for each such query. */
		sift = rs.getFloat(1);
		polyphen  = rs.getFloat(2);
		mutation_taster  = rs.getFloat(3);
	    }
	    rs.close();
	} catch(SQLException e) {
	    throw new ExomizerSQLException("Error executing pathogenicity query: " + e);
	}
	PathogenicityTriage pt = new PathogenicityTriage(polyphen,mutation_taster, sift);
	return pt;
    }


    /**
     *  Prepare the SQL query statements required for this filter.
     * <p>
     * SELECT sift,polyphen,mut_taster,phyloP</br>
     * FROM variant</br>
     * WHERE chromosome = ? </br>
     * AND position = ? </br>
     * AND ref = ? </br>
     * AND alt = ?</br>
     */
    private void setUpSQLPreparedStatement() throws ExomizerInitializationException
    {	
	String query = String.format("SELECT sift,"+
				     "polyphen,mut_taster,phyloP " +
				     "FROM variant " +
				     "WHERE chromosome = ? "+
				     "AND position = ? " +
				     "AND ref = ? " +
				     "AND alt = ? ");

        try {
	    this.getPathogenicityDataStatement  = connection.prepareStatement(query);
	  
        } catch (SQLException e) {
	    String error = "Problem setting up SQL query:" + query;
	    throw new ExomizerInitializationException(error);
        }
    }

   

    /**
     * Get the postgreSQL connection from calling code and set up a prepared statement
     * by calling the function {@link #setUpSQLPreparedStatement}.
     * @param connection An SQL (postgres) connection that was initialized elsewhere.
     */
    @Override public void setDatabaseConnection(java.sql.Connection connection) throws ExomizerInitializationException { 
	this.connection = connection;
	setUpSQLPreparedStatement();
    }

    public boolean display_in_HTML() { return true; }



}