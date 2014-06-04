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
import de.charite.compbio.exomiser.exception.ExomizerException;


/**
 * Filter variants according to the phenotypic similarity of the specified disease to mouse models disrupting 
 * the same gene. We use MGI annotated phenotype data and the Phenodigm/OWLSim algorithm. 
 * The filter is implemented with an SQL query.
 * <P>
 * This class prioritizes the genes that have survived the initial VCF filter (i.e., it is use on genes
 * for which we have found rare, potentially pathogenic variants).
 * <P>
 * Note that this class was refactored from MGIPhenodigmFilter on 15 January 2013.
 * @author Damian Smedley
 * @version 0.06 (22 April, 2013)
 */
public class MGIPhenodigmPriority implements Priority {
    /** Threshold for filtering. Retain only those variants whose score is below this threshold. */
    private float score_threshold = 2.0f;
    private String disease = null;
    /** Database handle to the postgreSQL database used by this application. */
    private Connection connection=null;
    /** A prepared SQL statement for mgi phenodigm score. */
    private PreparedStatement findScoreStatement = null;
    /** A prepared SQL statement for mgi phenodigm score. */
    private PreparedStatement testGeneStatement = null;
    
    /** A flag to indicate we could not retrieve data from the database for some variant. Using the
	value 200% means that the variant will not fail the Phenodigm filter. */
    private static final float NO_MGI_PHENODIGM_DATA = 2.0f;

    /** score of the variant. Zero means no relevance, 
	but the score is not necessarily scaled to [0..1] */
    private float MGI_PHENODIGM_SCORE;
    /** 
     * Number of variants considered by this filter
     */
    private int n_before=0;
    /**
     * Number of variants after applying this filter.
     */
    private int n_after=0;
   
    /** A list of messages that can be used to create a display in a HTML page or elsewhere. */
    private List<String> messages = null;
    /** Keeps track of the number of variants for which data was available in Phenodigm. */
    private int found_data_for_mgi_phenodigm;
    
    /**
     * After constructing this object, use the method {@link #setDatabaseConnection}
     * to initialize the database connection.
     */
    public MGIPhenodigmPriority(String disease) throws ExomizerInitializationException  {
    	this.disease = disease;
    	this.messages = new ArrayList<String>();
	String url = String.format("http://omim.org/%s",disease);
        if (disease.contains("ORPHANET")){
            String diseaseId = disease.split(":")[1];
            url = String.format("http://www.orpha.net/consor/cgi-bin/OC_Exp.php?lng=en&Expert=%s",diseaseId);
        }
	String anchor = String.format("Mouse phenotypes for candidate genes were compared to <a href=\"%s\">%s</a>\n",url,disease);
	this.messages.add(String.format("Mouse PhenoDigm Filter for OMIM"));
	messages.add(anchor);
     }

    /** Get the name of this prioritization algorithm. */
    @Override public String getPriorityName() { return "MGI PhenoDigm"; }

    /** Flag to output results of filtering against PhenoDigm data. */
    @Override public FilterType getPriorityTypeConstant() { return FilterType.PHENODIGM_MGI_PRIORITY; } 

     /** Sets the score threshold for variants.
      * Note: Keeping this method for now, but I do not think we need
      * parameters for Phenodigm prioritization?
      * @param par A score threshold, e.g., a string such as "0.02"
      * @throws exomizer.exception.ExomizerInitializationException
      */
     @Override public void setParameters(String par) throws ExomizerInitializationException
     {
	 try {
	     this.score_threshold  = Float.parseFloat(par);
	 } catch (NumberFormatException e) {
	     String  msg = "Could not parse score parameter for MGI PhenoDigm filter: \"" + par + "\"";
	     throw new ExomizerInitializationException(msg);
	 }
     }
 
    /**
     * @return list of messages representing process, result, and if any, errors of score filtering. 
     */
    //@Override
    public List<String> getMessages() {
	return this.messages;
    }


   
    
    public void prioritizeGenes(List<Gene> gene_list)
    {
	Iterator<Gene> it = gene_list.iterator();
	this.found_data_for_mgi_phenodigm=0;
	this.n_before = gene_list.size();
	while (it.hasNext()) {
	    Gene g = it.next();
	    try {
		MGIPhenodigmRelevanceScore rscore = retrieve_score_data(g);
		g.addRelevanceScore(rscore, FilterType.PHENODIGM_MGI_PRIORITY);
	    } catch (ExomizerException e) {
		this.messages.add("Error: " + e.toString());
	    }
	}
	this.n_after = gene_list.size();
	String s = 
	    String.format("Data analysed for %d genes using Mouse PhenoDigm",
			  gene_list.size());
	this.messages.add(s);
   }

    /** 
     * @param g A gene whose relevance score is to be retrieved from the SQL database by this function.
     * @return result of prioritization (represents a non-negative score)
     */
  private MGIPhenodigmRelevanceScore retrieve_score_data(Gene g) throws ExomizerSQLException {
      float MGI_SCORE = 0;
      String MGI_GENE_ID = null;
      String MGI_GENE=null;
	  
      String genesymbol = g.getGeneSymbol();
      ResultSet rs2 = null;
      try {	  
    	  this.testGeneStatement.setString(1,genesymbol);
	  rs2 = testGeneStatement.executeQuery();
    	  if ( rs2.next() ) { 
	      MGI_GENE_ID = rs2.getString(2);
	      MGI_GENE    = rs2.getString(3);
	      ResultSet rs = null;
	      try {
		  this.findScoreStatement.setString(1,this.disease);	  
		  this.findScoreStatement.setString(2,genesymbol);
		  rs = findScoreStatement.executeQuery();
		  //System.out.println(findScoreStatement);
		  if ( rs.next() ) { 
		      MGI_GENE_ID = rs.getString(1); 
		      MGI_GENE    = rs.getString(2);
		      MGI_SCORE   = rs.getFloat(3);
		      if (MGI_SCORE > 0 ) {
                          found_data_for_mgi_phenodigm++;
                      }
		  }
		  rs.close();
	      } catch(SQLException e) {
		  throw new ExomizerSQLException("Error executing Phenodigm query: " + e);
	      } 
  	  }
    	  else {
	      MGI_SCORE = Constants.NOPARSE_FLOAT;//use to indicate there is no phenotyped mouse model in MGI
    	  }
    	  rs2.close();
      }
      catch(SQLException e) {
	  throw new ExomizerSQLException("Error executing Phenodigm query: " + e);
      }
      MGIPhenodigmRelevanceScore rscore = new MGIPhenodigmRelevanceScore(MGI_GENE_ID,MGI_GENE, MGI_SCORE);
      return rscore;
  }

    /**
     *  Prepare the SQL query statements required for this filter.
     * <p>
     * Note that there are two queries. The testGeneStatement basically tests whether
     * the gene in question is in the database; it must have both an orthologue in the
     * table {@code human2mouse_orthologs}, and there must be some data on it
     * in the table {@code  mouse_gene_level_summary}.
     * <P>
     * Then, we select the MGI gene id (e.g., MGI:1234567), the corresponding mouse
     * gene symbol and the phenodigm score. There is currently one score for
     * each pair of OMIM diseases and MGI genes. 
     */
    private void setUpSQLPreparedStatements() throws ExomizerInitializationException
    {
	String score_query = String.format("SELECT mouse_gene_level_summary.mgi_gene_id, "+
					   "mouse_gene_level_summary.mgi_gene_symbol, max_combined_perc/100 " +
					   "FROM mouse_gene_level_summary, human2mouse_orthologs"+
					   "WHERE mouse_gene_level_summary.mgi_gene_id=human2mouse_orthologs.mgi_gene_id " +
					   "AND omim_disease_id = ? " +
					   "AND human_gene_symbol = ?");

	score_query ="SELECT M.mgi_gene_id, M.mgi_gene_symbol, max_combined_perc/100 "+
	    "FROM mouse_gene_level_summary M, human2mouse_orthologs H "+
	    "WHERE M.mgi_gene_id=H.mgi_gene_id "+
	    "AND omim_disease_id = \'187500\' AND human_gene_symbol = \'FBN1\'";

//	score_query ="SELECT M.mgi_gene_id, M.mgi_gene_symbol, max_combined_perc/100 "+
//	    "FROM mouse_gene_level_summary M, human2mouse_orthologs H "+
//	    "WHERE M.mgi_gene_id=H.mgi_gene_id "+
//	    "AND omim_disease_id = ?  AND human_gene_symbol = ?";
 
        // Query for new Exomiser db
        score_query ="SELECT M.mgi_gene_id, M.mgi_gene_symbol, max_combined_perc/100 "+
	    "FROM mouse_gene_level_summary M, human2mouse_orthologs H "+
	    "WHERE M.mgi_gene_id=H.mgi_gene_id "+
	    "AND disease_id = ?  AND human_gene_symbol = ?";
	

	try {
	    this.findScoreStatement  = connection.prepareStatement(score_query);
	  
        } catch (SQLException e) {
	    String error = "Problem setting up SQL query:" +score_query + e.toString();
	    throw new ExomizerInitializationException(error);
        }

//	String test_gene_query = String.format("SELECT human_gene_symbol, hm.mgi_gene_id, hm.mgi_gene_symbol " +
//                        "FROM mouse_gene_level_summary, human2mouse_orthologs_new hm "+
//                        "WHERE mouse_gene_level_summary.mgi_gene_id=hm.mgi_gene_id " +
//					       "AND human_gene_symbol = ? LIMIT 1");
        // Query for new Exomiser db
        String test_gene_query = String.format("SELECT human_gene_symbol, hm.mgi_gene_id, hm.mgi_gene_symbol " +
                        "FROM mouse_gene_level_summary, human2mouse_orthologs hm "+
                        "WHERE mouse_gene_level_summary.mgi_gene_id=hm.mgi_gene_id " +
					       "AND human_gene_symbol = ? LIMIT 1");
        
	try {
	    this.testGeneStatement  = connection.prepareStatement(test_gene_query);
	  
        } catch (SQLException e) {
	    String error = "Problem setting up SQL query:" +score_query + e.toString();
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
    public boolean displayInHTML() { return true; }

    /**
     * @return an HTML message for the table describing the action of filters
     */
    public String getHTMLCode() { 
	StringBuilder sb = new StringBuilder();
	sb.append("<ul>\n");
	Iterator<String> it = this.messages.iterator();
	while (it.hasNext()) {
	    String s = it.next();
	    sb.append("<li>"+s+"</li>\n");
	}
	sb.append("</ul>\n");
	return sb.toString();
    }

     /** Get number of variants before filter was applied */
    public int getBefore() {return this.n_before; }
    /** Get number of variants after filter was applied */
    public int getAfter() {return this.n_after; }

}