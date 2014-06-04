package de.charite.compbio.exomiser.priority;



import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import ontologizer.association.AssociationContainer;
import ontologizer.association.AssociationParser;
import ontologizer.association.Gene2Associations;
import ontologizer.go.OBOParser;
import ontologizer.go.OBOParserException;
import ontologizer.go.Ontology;
import ontologizer.go.Term;
import ontologizer.go.TermContainer;
import ontologizer.types.ByteString;
import sonumina.boqa.calculation.BOQA;
import sonumina.boqa.calculation.Observations;
import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exception.ExomizerSQLException;
import de.charite.compbio.exomiser.exome.Gene;

/**
 * Filter variants according to the phenotypic similarity to a set of user-supplied HPO terms.
 * This method implements the BOQA algorithm as described in Bauer S et al (2012) Bayesian
 * ontology querying for accurate and noise-tolerant semantic searches. 
 * Bioinformatics. 28(19):2502-8. (PubMed PMID: 22843981).+
 * <P>
 * Note that the class connects to the Exomizer SQL database in order to retrieve the OMIM phenotype
 * IDs that correspond to a given gene. Note that there may be multiple such phenotype IDs per gene, and
 * we need to choose the disease with the highest BOQA score to assign to a gene that is being prioiritized.
 * 
 * The files required for the constructor of this filter should be downloaded from:
 * {@code http://purl.obolibrary.org/obo/hp/uberpheno/} (HSgenes_crossSpeciesPhenoAnnotation.txt, crossSpeciesPheno.obo)
 * 
 * @see <a href="http://purl.obolibrary.org/obo/hp/uberpheno/">Uberpheno Hudson page</a>
 * @see <a href="http://f1000research.com/articles/2-30/v1">F1000 research article</a>
 * @author Peter Robinson
 * @version 0.03 (9 September, 2013)
 */
public class BoqaPriority implements Priority {

    /** This is core class implementing BOQA */
    private BOQA boqa = null;
    /** The HPO lives here!*/
    private Ontology ontology = null;
    /** THe HPO term to disease associations go here */
    private AssociationContainer assocs = null;
    /** List of input HPO terms. */
    private ArrayList<String> hpoList = null;
    /** Map of results of BOQA analysis. Key: Name of disease (SHOULD BE OMIM ID) and Value: BOQA posterior probability. */
    private HashMap<String,Double> resultMap = null;

     /** Database handle to the postgreSQL database used by this application. */
    private Connection connection=null;
    /** A prepared SQL statement for finding the Phenotype IDs that match to a gene. */
    private PreparedStatement preparedQuery = null;

      /** A list of messages that can be used to create a display in a HTML page or elsewhere. */
    private ArrayList<String> messages = null;

    /**
     * Create a new instance of the UberphenoFilter.
     * 
     * @param hpoOboFile Human Phenotype Ontology obo-file 
     * @param hpoAnnotationFile The annotation file obtained from {@code http://purl.obolibrary.org/obo/hp/uberpheno/}
     * @param hpoTermList alist of HPO terms separated by comma, e.g., "HP:0000407,HP:0009830,HP:0002858".
     * @throws ExomizerInitializationException
     * @see <a href="http://purl.obolibrary.org/obo/hp/uberpheno/">Uberpheno Hudson page</a>
     */
    public BoqaPriority(String hpoOboFile, String hpoAnnotationFile, String hpoTermList) 
	throws ExomizerInitializationException  {
	this.boqa = new BOQA();
	this.messages = new ArrayList<String>();
	boqa.setConsiderFrequenciesOnly(false);
	boqa.setPrecalculateScoreDistribution(false);
	boqa.setCacheScoreDistribution(false);
	boqa.setPrecalculateItemMaxs(false);
	boqa.setPrecalculateMaxICs(false);
	boqa.setMaxFrequencyTerms(2);
	inputHPO(hpoOboFile,hpoAnnotationFile);
	initializeHPOTermList(hpoTermList);
	performBOQACalculations();
    }


    
    /**
     * Add an HPO term and all of its ancestors to the BOQA
     * object, i.e., make the term/ancestors "on"
     * @param hpoTermString A Term id expressed as a String, e.g., "HP:0000407"
     */
    public void addTermAndAncestors(String hpoTermString, Observations obsv) {
	Term t = this.ontology.getTerm(hpoTermString);
	int id = boqa.getTermIndex(t);
	obsv.observations[id] = true;
	for (int ancestor : this.boqa.getAncestors(id)) {
            obsv.observations[ancestor] = true;
	}
    }


    /**
     * BOQA currently is using field 3 (i.e., zero-based field 2) of the HPO
     * association file in order to get the unique ID of the disease. This corresponds
     * to a String such as 
     * " #101000 NEUROFIBROMATOSIS, TYPE II; NF2;;NEUROFIBROMATOSIS, CENTRAL TYPE;;
     * ACOUSTIC SCHWANNOMAS, BILATERAL;;BILATERAL ACOUSTIC NEUROFIBROMATOSIS; BANF;;
     * ACOUSTIC NEURINOMA, BILATERAL; ACN". This method retrieves the corresponding MIM
     * ID, which is a 6-digit integer (here: 101000), which we get in form of a ByteSrtring.
     */
    private ByteString getMIMid(ByteString MIMname)
    {
	Gene2Associations g2a = assocs.get(MIMname);
	ByteString objectID = g2a.iterator().next().getDB_Object();
	return objectID;
    }


    /**
     * This method performs the BOQA analysis for the HPO terms that have
     * been entered in the class variable {@link #hpoList}.
     */   
    public void performBOQACalculations() {
	this.resultMap = new HashMap<String,Double>();
	this.boqa.setup(this.ontology, this.assocs);
	//System.err.println("Set up ontology and associations");
	
	Observations o = new Observations();
	o.observations = new boolean[boqa.getOntology().getNumberOfTerms()];
	
	/* Add HPO terms (physician observations) to initialize the search */
	/* Model:   o.observations[boqa.getTermIndex(ontology.getTerm("HP:0009830"))] = true; */
	for (String hpo : this.hpoList) {
	    addTermAndAncestors(hpo,o); //o.observations[boqa.getTermIndex(ontology.getTerm(hpo))] = true;
	}

	final BOQA.Result res = this.boqa.assignMarginals(o, false, 1);

	/* For debugging purposes, we determine the order of the marginals.
	 * Unfortunately, Java doesn't prove an order method but we can
	 * reduce this to a sort. Again unfortunately, Java doesn't support
	 * custom comparators for native datatypes so we have to resort
	 * the the Integer class here, which is a bit of overhead.
	 */
	Integer [] order = new Integer[res.size()];
	for (int i=0; i < order.length; i++) {
	    order[i] = i;
	}
	Arrays.sort(order, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (res.getMarginal(o1) < res.getMarginal(o2)) return 1;
                if (res.getMarginal(o1) > res.getMarginal(o2)) return -1;
                return 0;
            }
        });

	for (int i=0;i<res.size();++i) {
	    double marg = res.getMarginal(i);
	    //Gene2Associations g2a = this.assocs.get(item);
	    //String objectID = hpo.assoc.get(null).iterator().next().getDB_Object();

	    ByteString item = boqa.getItem(i);
	    ByteString mimID = getMIMid(item);
	    /* The following adds a MIM number such as "613092" to the map. */
	    this.resultMap.put(mimID.toString(),marg);
	}

	int maxTopHitNumber = Math.min(5, order.length);
	for (int i = 0; i < maxTopHitNumber; i++)
	{
	    int itemId = order[i];
	    System.err.println(res.getMarginal(itemId) + "->" + boqa.getItem(itemId));
	}
    }

    





    /**
     * @param hpoTermList alist of HPO terms separated by comma, e.g., "HP:0000407,HP:0009830,HP:0002858".
     */
    private void initializeHPOTermList(String hpoTermList) throws ExomizerInitializationException {
	String A[] = hpoTermList.split(",");
	if (A.length<1) {
	    String e = String.format("Error: Could not parse any HPO terms from the input string \"%s\"",hpoTermList);
	    throw new ExomizerInitializationException(e);
	}
	this.hpoList = new ArrayList<String>();
	for (String a : A) {
	    a = a.trim();
	    if (! a.startsWith("HP:") || a.length() != 10) { /* A well formed HPO term starts with "HP:" and has ten characters. */
		String e = String.format("Error: malformed HPO input string \"%s\". Could not parse term \"%s\"",hpoTermList,a);
		throw new ExomizerInitializationException(e);
	    }
	    hpoList.add(a);
	}
    }

    	    
    private void inputHPO(String hpopath, String assocpath) throws ExomizerInitializationException {
	OBOParser hpoParser = new OBOParser(hpopath);
	try {
	    hpoParser.doParse();
	    TermContainer tc = new TermContainer(hpoParser.getTermMap(),hpoParser.getFormatVersion(),hpoParser.getDate());
	    this.ontology = new Ontology(tc);
	    AssociationParser ap = new AssociationParser(assocpath, tc);
	    this.assocs = new AssociationContainer(ap.getAssociations(), ap.getSynonym2gene(), ap.getDbObject2gene() );
	} catch (OBOParserException e) {
	    throw new ExomizerInitializationException(e.toString());
	} catch (IOException e) {
	    throw new ExomizerInitializationException(e.toString());
	} 
    }


    /**
     * An implementation of Kahan's compensated summation which tries to minimize precision
     * errors when summing up ffp values.
     *
     * @author Sebastian Bauer
     *
     * @see "https://en.wikipedia.org/wiki/Kahan_summation_algorithm"
     */
    class KahanSummation {
        /** The current sum */
        private float sum;

        /** The running compensation */
        private float c;

        /**
         * Add the given float to the accu/sum. Return the sum
         * after the float has been added.
         *
         * @param toAdd
         * @return the current sum.
         */
        public float add(float toAdd) {
            float y = toAdd - c;
            float t = sum + y;
            c = (t - sum) - y;
            sum = t;
            return sum;
        }
    }

    /**
     * Prioritize a list of candidate {@link exomizer.exome.Gene Gene} objects (the candidate
     * genes have rare, potentially pathogenic variants).
     *
     * @param gene_list List of candidate genes.
     *
     * @see exomizer.filter.Filter#filter_list_of_variants(java.util.ArrayList)
     */
    @Override 
    public void prioritizeGenes(List<Gene> gene_list)
    {
        ArrayList<BoqaRelevanceScore> scoreList = new ArrayList<BoqaRelevanceScore>(gene_list.size());
        KahanSummation sum = new KahanSummation(); 

        /* First, calculate the score for each gene */
        for (Gene g : gene_list) {
	    try {
		BoqaRelevanceScore bqrel = retrieve_boqa_result(g);
		sum.add(bqrel.getPosteriorProbability());
		scoreList.add(bqrel);
	    } catch (ExomizerException e) {
		this.messages.add(e.toString());
		System.err.println(e);
	    }
	}

        float totalSum = sum.add(0);

        /* Now normalize such that the total sum will be 1.0 next time,
         * i.e., make it a probability measure */
        for (int i=0; i < gene_list.size(); i++) {
            BoqaRelevanceScore bqrel = scoreList.get(i);
            Gene g = gene_list.get(i);
            bqrel.setPosteriorProbability(bqrel.getPosteriorProbability() / totalSum);
            g.addRelevanceScore(bqrel,PriorityType.BOQA_PRIORITY);

            System.err.println("Gene " + g.getGeneSymbol() + " has an relevance score of " + bqrel.getRelevanceScore());
        }
    }


    /**
     * This function uses the SQL table "omim" to look up the phenotype MIM number(s)
     * for a gene. The MIM numbers represent all of the diseases that are associated
     * with the gene. The MIM numbers should be identical with the MIM numbers that
     * were used to store the results of BOQA calculation in the class variable
     * {@link #resultMap}. Since there may be multiple such diseases, the function
     * chooses the maximum BOQA score (which represents the posterior probability). 
     * It uses this score to initialize the 
     * {@link exomizer.priority.BoqaRelevanceScore BoqaRelevanceScore} object that will
     * be association with this gene.
     * @param g A gene for which we will find the BOQA score.
     */
    private BoqaRelevanceScore retrieve_boqa_result(Gene g) throws ExomizerSQLException  {
	int entrez = g.getEntrezGeneID();
	ResultSet rs = null;
	BoqaRelevanceScore rel = null;
	try {
	  this.preparedQuery.setInt(1,entrez);
	  rs = preparedQuery.executeQuery();
	  //System.err.println(preparedQuery );
	  double maxProb = Double.MIN_VALUE;
	  String maxDis = null;
	  double PRIORPROB = 0.1d;
	  while ( rs.next() ) { /* The way the db was constructed, there is just one line for each such query. */
	      int phenmim = rs.getInt(1);
	      String mim = String.format("%d",phenmim);
	      if (this.resultMap.containsKey(mim)) {
		  Double score = this.resultMap.get(mim); 
		  double d = score.doubleValue();
		  if (d > maxProb) {
		      System.err.println("Got d=" + d + " for disease " + mim);
		      maxProb = d;
		      maxDis = mim;
		  }
	      }
	  }
	  rs.close();
	  if (maxProb < 0) 
	      rel = noDataScore();
	  else
	      rel = new BoqaRelevanceScore(maxProb,maxDis);
	} catch(SQLException e) {
	    throw new ExomizerSQLException("Error executing OMIM query: " + e);
	}
	return rel;
    }


    /**
     * This is call for genes with no PPI data; they are assigned a score of zero.
     * They will be assigned a score equivalent to the median of all genes by
     * the function {@code prioritize_listofgenes} in 
     * {@link exomizer.priority.IPriority FilterType}.
     * basically as a kind of uniform prior.
     */
    public static BoqaRelevanceScore noDataScore() {
	float nodatascore = 0.1f;
	BoqaRelevanceScore brs = new BoqaRelevanceScore(nodatascore,"No disease data found");
	return brs;
    }




 /* (non-Javadoc)
     * @see exomizer.priority.FilterType#getPriorityName()
     */
    @Override public String getPriorityName() { 
	return "Uberpheno semantic similarity filter"; 
    }
    
    /** Flag to output results of filtering against Uberpheno data. */
    @Override public PriorityType getPriorityTypeConstant() { 
	return PriorityType.BOQA_PRIORITY; 
    } 

    @Override  public ArrayList<String> getMessages() {
	return null;
    }
    
     /**
     *  Prepare the SQL query statements required for this filter.
     * <p>
     * SELECT phenmim,genemim,diseasename,type</br>
     * FROM omim</br>
     * WHERE gene_id  = ? </br>
     */
    private void setUpSQLPreparedStatement() throws ExomizerInitializationException
    {	
	String query = String.format("SELECT phenmim "+
				     "FROM omim " +
				     "WHERE gene_id = ?");
        try {
	    this.preparedQuery  = connection.prepareStatement(query);
        } catch (SQLException e) {
	    String error = "Problem setting up SQL query:" + query;
	    throw new ExomizerInitializationException(error);
        }
    }

    
    
    /**
     * Initialize the database connection and call {@link #setUpSQLPreparedStatement}
     * @param connection A connection to a postgreSQL database from the exomizer or tomcat.
     */
    @Override public void setDatabaseConnection(java.sql.Connection connection) 
	throws ExomizerInitializationException
    {
	this.connection = connection;
	setUpSQLPreparedStatement();
    }


       /** Get number of variants before filter was applied TODO */
     @Override  public int getBefore() {return 0; }
    /** Get number of variants after filter was applied TODO */
     @Override  public int getAfter() {return 0; }
    
    /**
     * To do
     */
     @Override public boolean displayInHTML() { return false; }

  
     @Override public String getHTMLCode() { return "To Do"; }
    
    /**
     * Set parameters of prioritizer if needed.
     * @param par A String with the parameters (usually extracted from the cmd line) for this prioiritizer)
     */
    @Override public void setParameters(String par){
	/* -- Nothing needed now --- */
    }




}