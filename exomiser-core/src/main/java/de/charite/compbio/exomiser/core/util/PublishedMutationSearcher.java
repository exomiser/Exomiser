package de.charite.compbio.exomiser.core.util;

import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Once the exomiser has identified a list of variants, it can
 * search the postgreSQL database to look for "hits" amongst the
 * HGMD Pro variants. To do so, it expects to get a list of variants
 * that remain after initial prioritization (frequency filtering etc), 
 * and then it looks up each variant in the database.
 * @version 0.05 (25 January, 2014).
 * @author Peter Robinson
 */
public class PublishedMutationSearcher {

    private final Logger logger = LoggerFactory.getLogger(PublishedMutationSearcher.class);
    
    /** List of variants for which we want to search the database for 
     * known mutations in the HGMD data. 
     */
    private List<VariantEvaluation> varEvalList=null;

    private List<String> hitList=null;

    private Connection connection=null;

    private int newcounter=1;

    /**
     * We have encoded the clinical significance classes of ClinVar using
     * chars (they are stored as such in the table "clinvar"). This function
     * retrieves the corresponding string.
     * @see exomizer.io.ClinVarParser
     */
    private String getClinVarClass(String s) {
	char c = s.charAt(0);
	switch (c) {
	case 'P': return "Pathogenic";
	case 'L': return "Likely pathogenic";
	case 'U': return "Uncertain significance";
	case 'B': return "Benign";
	case 'C': return "Likely benign";
	case 'T': return "protective";
	case 'D': return "drug response";
	case 'S': return "confers sensitivity";
	case 'A': return "Association";
	default: return "?";
	}
    }

    /**
     * This function looks for mutations published eitherin ClinVar or in HGMD 
     * that match with the chromosomal position of the variant identified in 
     * the exome sequencing project. For every mutation found,it adds an HTML link that
     * will be shown by the PheniX server.
     * @param lst A list of all variants found in a certain gene.
     */
    public void addPublishedMutationsToVariants(List<VariantEvaluation> lst) {
	/* First look for ClinVar stuff */
	try {
	    String query = "SELECT id,signif FROM clinvar "+
		"WHERE chromosome = ? AND position = ?;";
	    PreparedStatement qps = connection.prepareStatement(query);
	    for (VariantEvaluation ve : lst) {
		int chr = ve.getChromosome();
		int pos = ve.getPosition();
		qps.setInt(1,chr);
		qps.setInt(2,pos);
		ResultSet rs = qps.executeQuery();
		while (rs.next()) {
		    int id = rs.getInt(1);
		    String sign = rs.getString(2);
		    String acc = String.format("RCV%09d", id);
		    String url = String.format("http://www.ncbi.nlm.nih.gov/clinvar/%s/",acc);
		    String cls = getClinVarClass(sign);
		    String anch = String.format("<a href=\"%s\" class=\"button glow1\" target=\"_new%d\">ClinVar (%s)</a>",url,newcounter,cls);
		    newcounter++;
		    ve.addMutationReference(anch);
		}
	    }
	} catch (SQLException e) {
	    logger.error("SQL Error while retrieving ClinVar data.", e);
	    // skip
	}
	/* Now HGMD PRO */
	try {
	    String query = "SELECT hgmdacc FROM hgmdpro "+
		"WHERE chromosome = ? AND position = ?;";
	    PreparedStatement qps = connection.prepareStatement(query);
	    for (VariantEvaluation ve : lst) {
		int chr = ve.getChromosome();
		int pos = ve.getPosition();
		String sym = ve.getGeneSymbol();
		qps.setInt(1,chr);
		qps.setInt(2,pos);
		ResultSet rs = qps.executeQuery();
		while (rs.next()) {
		    String acc = rs.getString(1);
		    String url = String.format("http://www.hgmd.cf.ac.uk/ac/gene.php?gene=%s&accession=%s",sym,acc);
		    String anch = String.format("<a href=\"%s\" class=\"button glow2\">HGMD</a>",url,newcounter);
		    newcounter++;
		    ve.addMutationReference(anch);
		}
	    }
	} catch (SQLException e) {
	    logger.error("SQL Error while retrieving HGMD data", e);
	    // skip
	}
    }

    /**
     * Use this constructor if you want to use the method
     * {@link #addPublishedMutationsToVariants}. 
     * @param conn An SQL connection to the Exomiser database
     */
    public PublishedMutationSearcher(Connection conn) {
	this.connection = conn;
    }


    public PublishedMutationSearcher(List<VariantEvaluation> velist,Connection conn) {
	this.varEvalList = velist;
	this.hitList = new  ArrayList<String>();
	this.connection = conn;
    }

    /**
     * This function creates a String to display the HGMD pro information about a variant 
     * that was identified by the Exomiser/CRE-Server if there is information about
     * the variant in the Pro database.
     */
    private void addProHit(String cdna,String prot,int pmid,String disease,String gensym,String genotype){
	String s = String.format("%s (%s:%s:%s): %s",disease,gensym,cdna,prot,genotype);
	if (pmid>0) {
	    String a = String.format("<a href=\"http://www.ncbi.nlm.nih.gov/pubmed/%d\">%d</a>",
				     pmid,pmid);
	    s = String.format("%s [pmid: %s]",s,a);
	}
	this.hitList.add(s);
    }


    /**
     * @return A list of String intended for HTML display representing HGMD hits (published mutations).
     */
    public List<String> getHGMDHits() {
	return this.hitList;
    }

    /**
     * Search the database over all variants that have survived Exomiser filtering
     * for matches in the HGMD (Uses a table in the postgreSQL database). Any hits
     * found are added to the variable {@link #hitList} by calling the
     * function {@link #addProHit}.
     */
    public void evaluateHGMDPro() {
	try {
	    String query = "SELECT cdna,prot,pmid,disease,genesym FROM hgmddisease d, hgmdpro p "+
		"WHERE chromosome = ? AND position = ? AND d.id=p.id;";
	    PreparedStatement qps = connection.prepareStatement(query);
	    for (VariantEvaluation ve : varEvalList) {
		int chr = ve.getChromosome();
		int pos = ve.getPosition();
		String gt = ve.getGenotypeAsString();
		qps.setInt(1,chr);
		qps.setInt(2,pos);
		ResultSet rs = qps.executeQuery();
		while (rs.next()) {
		    String cdna = rs.getString(1);
		    String prot = rs.getString(2);
		    int pmid = rs.getInt(3);
		    String disease = rs.getString(4);
		    String gensym = rs.getString(5);
		    addProHit(cdna,prot,pmid,disease,gensym, gt);
		}
	    }
	} catch (SQLException e) {
	    System.err.println(e.getMessage());
	    /* Database error, just skip this step. */
	}


    }


	   


}