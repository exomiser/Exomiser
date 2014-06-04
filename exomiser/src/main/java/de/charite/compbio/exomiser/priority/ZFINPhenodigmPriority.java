package de.charite.compbio.exomiser.priority;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import jannovar.common.Constants;


import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exception.ExomizerSQLException;


/**
 * Filter variants according to the phenotypic similarity of the specified disease to mouse models disrupting 
 * the same gene. We use ZFIN annotated phenotype data and the Phenodigm/OWLSim algorithm. 
 * The filter is implemented with an SQL query.
 * <P>
 * This class prioritizes the genes that have survived the initial VCF filter (i.e., it is use on genes
 * for which we have found rare, potentially pathogenic variants).
 * <P>
 * This class uses a database connection that it obtains from the main Exomizer driver program (if the Exomizer was
 * started from the command line) or from a tomcat server (etc.) if the Exomizer was called from a Webserver.
 * @author Damian Smedley
 * @version 0.06 (28 April, 2013)
 */
public class ZFINPhenodigmPriority implements Priority {
    /** Threshold for filtering. Retain only those variants whose score is below this threshold. */
    private float score_threshold = 2.0f;
    private String disease = null;
    /** Database handle to the postgreSQL database used by this application. */
    private Connection connection=null;
    /** A prepared SQL statement for zfin phenodigm score. */
    private PreparedStatement findScoreStatement = null;
    /** A prepared SQL statement for zfin phenodigm score. */
    private PreparedStatement testGeneStatement = null;
    
    /** A flag to indicate we could not retrieve data from the database for some variant. Using the
	value 200% means that the variant will not fail the Phenodigm filter. */
    private static final float NO_ZFIN_PHENODIGM_DATA = 2.0f;

    /** score of the variant. Zero means no relevance, 
	but the score is not necessarily scaled to [0..1] */
    private float ZFIN_PHENODIGM_SCORE;
   
    /** A list of messages that can be used to create a display in a HTML page or elsewhere. */
    private  List<String> messages = null;
    /** Keeps track of the number of variants for which data was available in Phenodigm. */
    private int found_data_for_zfin_phenodigm;
    
    public ZFINPhenodigmPriority(String disease) throws ExomizerInitializationException  {
    	this.disease = disease;
    	this.messages = new ArrayList<String>();
     }

    /** Get the name of this prioritization algorithm. */
    @Override public String getPriorityName() { return "ZFIN PhenoDigm"; }

    /** Flag to output results of filtering against PhenoDigm data. */
    @Override public FilterType getPriorityTypeConstant() { return FilterType.PHENODIGM_ZFIN_PRIORITY; } 

     /** Sets the score threshold for variants.
      * Note: Keeping this method for now, but I do not think we need
      * parameters for Phenodigm prioritization?
      * @param par A score threshold, e.g., a string such as "0.02"
      */
     @Override public void setParameters(String par) throws ExomizerInitializationException
     {
	 try {
	     this.score_threshold  = Float.parseFloat(par);
	     
	     this.messages.add(String.format("ZFIN PhenoDigm Filter for OMIM:"
					     +disease
					     +" : Only show variants with a PhenoDigm score of more than %.2f %%",
					     100 * score_threshold));
	 } catch (NumberFormatException e) {
	     String  msg = "Could not parse score parameter for ZFIN PhenoDigm filter: \"" + par + "\"";
	     throw new ExomizerInitializationException(msg);
	 }
     }
 
    /**
     * @return list of messages representing process, result, and if any, errors of score filtering. 
     */
    // @Override
    public List<String> getMessages() {
	return this.messages;
    }


   
    
    public void prioritizeGenes(List<Gene> gene_list)
    {
	Iterator<Gene> it = gene_list.iterator();
	this.found_data_for_zfin_phenodigm=0;

	while (it.hasNext()) {
	    Gene g = it.next();
	    try {
		ZFINPhenodigmRelevanceScore rscore = retrieve_score_data(g);
		g.addRelevanceScore(rscore, FilterType.PHENODIGM_ZFIN_PRIORITY);
	    } catch (ExomizerSQLException e) {
		this.messages.add("Error: " + e.toString());
	    }
	}
	String s = 
	    String.format("Data analysed for %d genes using ZFIN PhenoDigm",
			  gene_list.size());
	this.messages.add(s);
   }

    /** 
     * @param g A gene whose relevance score is to be retrieved from the SQL database by this function.
     * @return result of prioritization (represents a non-negative score)
     */
  private ZFINPhenodigmRelevanceScore retrieve_score_data(Gene g) throws ExomizerSQLException {
      float ZFIN_SCORE = Constants.UNINITIALIZED_FLOAT;
      String ZFIN_GENE_ID = null;
      String ZFIN_GENE=null;
	  
      String genesymbol = g.getGeneSymbol();
      ResultSet rs2 = null;
      try {	  
    	  this.testGeneStatement.setString(1,genesymbol);
	  rs2 = testGeneStatement.executeQuery();
    	  if ( rs2.next() ) { 
	      ResultSet rs = null;
	      try {
		  this.findScoreStatement.setString(1,this.disease);	  
		  this.findScoreStatement.setString(2,genesymbol);
		  rs = findScoreStatement.executeQuery();
		  //System.out.println(findScoreStatement);
		  if ( rs.next() ) { 
		      ZFIN_GENE_ID = rs.getString(1); 
		      ZFIN_GENE    = rs.getString(2);
		      ZFIN_SCORE   = rs.getFloat(3);
		      if (! (ZFIN_SCORE < 0 )) found_data_for_zfin_phenodigm++;
		  }
		  rs.close();
	      } catch(SQLException e) {
		  throw new ExomizerSQLException("Error executing Phenodigm query: " + e);
	      } 
  	  }
    	  else {
	      ZFIN_SCORE = Constants.NOPARSE_FLOAT;//use to indicate there is no phenotyped mouse model in ZFIN
    	  }
    	  rs2.close();
      }
      catch(SQLException e) {
	  throw new ExomizerSQLException("Error executing Phenodigm query: " + e);
      }
      ZFINPhenodigmRelevanceScore rscore = new ZFINPhenodigmRelevanceScore(ZFIN_GENE_ID,ZFIN_GENE, ZFIN_SCORE);
      return rscore;
  }

    /**
     *  Prepare the SQL query statements required for this filter.
     * <p>
     * Note that there are two queries. The testGeneStatement basically tests whether
     * the gene in question is in the database; it must have both an orthologue in the
     * table {@code human2fish_orthologs}, and there must be some data on it
     * in the table {@code  fish_gene_level_summary}.
     * <P>
     * Then, we select the ZFIN gene id (e.g., ZFIN:1234567), the corresponding mouse
     * gene symbol and the phenodigm score. There is currently one score for
     * each pair of OMIM diseases and ZFIN genes. 
     */
    private void setUpSQLPreparedStatements() throws ExomizerInitializationException
    {
	String score_query = "SELECT M.zfin_gene_id, M.zfin_gene_symbol, max_combined_perc/100 "+
	    "FROM fish_gene_level_summary M, human2fish_orthologs H "+
	    "WHERE M.zfin_gene_id=H.zfin_gene_id "+
	    "AND omim_disease_id = ?  AND human_gene_symbol = ?";

	try {
	    this.findScoreStatement  = connection.prepareStatement(score_query);
	  
        } catch (SQLException e) {
	    String error = "Problem setting up SQL query:" +score_query;
	    throw new ExomizerInitializationException(error);
        }

	String test_gene_query = String.format("SELECT human_gene_symbol " +
			"FROM fish_gene_level_summary, human2fish_orthologs "+
			"WHERE fish_gene_level_summary.zfin_gene_id=human2fish_orthologs.zfin_gene_id " +
			"AND human_gene_symbol = ? LIMIT 1");

	try {
	    this.testGeneStatement  = connection.prepareStatement(test_gene_query);
	  
        } catch (SQLException e) {
	    String error = "Problem setting up SQL query:" +score_query;
	    throw new ExomizerInitializationException(error);
        }
	
    }
     
    
    /**
     * Initialize the database connection and call {@link #setUpSQLPreparedStatements}
     * @param connection A connection to a postgreSQL database from the exomizer or tomcat.
     */
    public void setDatabaseConnection(java.sql.Connection connection) 
	throws ExomizerInitializationException
    {
	this.connection = connection;
	setUpSQLPreparedStatements();
    }



    
    /**
     * To do
     */
    public boolean displayInHTML() { return false; }

  
    public String getHTMLCode() { return "To Do"; }

     /** Get number of variants before filter was applied TODO */
    public int getBefore() {return 0; }
    /** Get number of variants after filter was applied TODO */
    public int getAfter() {return 0; }

}