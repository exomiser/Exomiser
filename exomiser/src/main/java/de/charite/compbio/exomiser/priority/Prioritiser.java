package de.charite.compbio.exomiser.priority;




import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.BedFilter;
import de.charite.compbio.exomiser.filter.FrequencyFilter;
import de.charite.compbio.exomiser.filter.IFilter;
import de.charite.compbio.exomiser.filter.IntervalFilter;
import de.charite.compbio.exomiser.filter.PathogenicityFilter;
import de.charite.compbio.exomiser.filter.QualityFilter;
import de.charite.compbio.exomiser.filter.TargetFilter;
import de.charite.compbio.exomiser.priority.util.DataMatrix;
import jannovar.common.ModeOfInheritance;
import jannovar.exome.Variant;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;





/**
 * This class is meant to organize and streamline the prioritization of
 * genes according to user settings. It restricts the possible paths through
 * the various ways of prioritising genes in order to hopefully avoid errors
 * and make extneding prioritization for further stretegies easier.
 * <P>
 * The user can pass a comma-separated list of options from the command line
 * that will determine the behaviour of this program. This method parses
 * them and sets various filter flags. The Filters are added in the order
 * they should be used (if present): Frequency, Quality, Pathogenicity,
 * Inheritance, Model-Org.
 * <P>
 * The logic of this function is such that the filters are always applied,
 * but with default settings that cause the analysis to be done without
 * filtering out variants (the latter only happens if the user actually sets
 * the command line params).
 * <P>
 * This function also passes the postgreSQL connection to the two filters
 * that need to connect to the database,
 * {@link exomizer.filter.FrequencyFilter FrequencyFilter} and
 * {@link exomizer.filter.PathogenicityFilter PathogenicityFilter}. The
 * other filters do not need a database connection.
 * This method initializes the gene-level prioritization object (these
 * objects are programmed to extend the interface (
 * {@link exomizer.priority.IPriority Ipriority}). The prioritizers can be
 * set flexibly as follows.
 * <ul>
 * <li>OMIMPriority. This just creates an HTML link to any OMIM disease
 * entries that are linked to the gene
 * <li>InheritancePriority. This is added to the list of prioritizes only if
 * the user passes a <b>-I</b> flag on the command line with one of AR
 * (autosomal recessive), AD (autosomal dominant) or X (X chromosomal).
 * Genes are then filtered out unless they have a pattern of variants that
 * matches with the mode of inheritance.
 * <li>MGIPhenodigmPriority. This is addded to the list of prioritizers only
 * if the user passes the flag -A to set an OMIM id (disease) against which
 * the MGI phenotypes will be compared
 * <li>DynamicPhenodigmPriority. This is addded if the users sets the
 * <b>-O</b> flag to pass a list of HPO ids against which the MGI phenotypes
 * will be compared
 * <li>ZFINPhenodigmPriority. This is addded if the users sets the <b>-Z</b>
 * and also the <b>-A</b> disease flag to search against ZFIN phenotypes
 * (TODO: check this!)
 * <li>GenewandererPriority. This is added to perform Random Walk analysis
 * prioritization of the candidate genes. For now it is intended to be used
 * as an alternative to phenotype prioritization.
 * </ul>
 * <P>
 * This function also passes the postgreSQL connection to the two filters
 * that need to connect to the database,
 * {@link exomizer.priority.OMIMPriority OMIMPriority},
 * {@link exomizer.priority.MGIPhenodigmPriority MGIPhenodigmPriority},
 * {@link exomizer.priority.DynamicPhenodigmPriority
 * DynamicPhenodigmPriority}, and
 * {@link exomizer.priority.ZFINPhenodigmPriority ZFINPhenodigmPriority}.
 * The other prioritizers do not need a database connection.
 * @version 0.03 (20 January 2014)
 * @author Peter Robinson
 */
public class Prioritiser {
    
    private static final Logger logger = LoggerFactory.getLogger(Prioritiser.class);
    
    /** Database handle to the postgreSQL database used by this application. */
    private Connection connection = null;
    /**
     * List of filters (see {@link exomizer.filter.IFilter IFilter}).
     */
    private List<IFilter> filterList = null;
    /** List of gene-prioritizers (see {@link exomizer.priority.IPriority IPriority}). */
    private List<IPriority> priorityList = null;
    /**
     * One of AD, AR, or XR (X chromosomal recessive). If uninitialized, this
     * prioritizer has no effect).
     */
    private ModeOfInheritance inheritanceMode = ModeOfInheritance.UNINITIALIZED;
    /**
     * List of all Genes that are to be prioritized. Note that the Genes will
     * contain the variants that have passed the filtering step.
     */
    private List<Gene> geneList=null;
    
    public Prioritiser(Connection conn) {
	this.connection = conn;
	this.filterList = new ArrayList<IFilter>();
	this.priorityList = new ArrayList<IPriority>();
    }


    /**
     * This function iterates over all of the Prioritization schemes chosen by
     * the user and applies them the list of variants (a Java ArrayList). At
     * present, the code is written such that there will be one prioritization
     * filter applied per run, however, they could be combined with one another
     * in the future.
     * <P>
     * Prioritization will look at all variants that have survived filtering and
     * then use an appropriate algorithm to assign a score to each
     * {@link exomizer.exome.Gene Gene} object. The function rank variants is
     * then applied to rank the variants according to the combined
     * filter/priority score.
     * <P>
     * This method first combines all the {@link jannovar.exome.Variant Variant}
     * objects into the corresponing {@link exomizer.exome.Gene Gene} objects,
     * and then it applies all active prioritization algorithms.
     */
    private void prioritizeGenes(List<VariantEvaluation> variantList) {
	/** Record list of genes we have seen before. */
	Map<String, Gene> gene_map = new HashMap<String, Gene>();
	Iterator<VariantEvaluation> it = variantList.iterator();
	while (it.hasNext()) {
	    VariantEvaluation ve = it.next();
	    Variant v = ve.getVariant();
	    /*
	     * Jannovar  outputs multiple possible symbols for
	     * a variant e.g. where a variant is an exon in one gene and intron
	     * in another gene The order of these symbols can vary depending on
	     * the variant although the first one always refers to the most
	     * pathogenic. Therefore hash on this first symbol if multiple
	     */
	    if (v.getGeneSymbol() != null) {
		// Off target variants do nothave gene-symbols.
		// This if avoids null pointers
		String name = v.getGeneSymbol();
		if (name.contains(",")) {
		    String nameParts[] = name.split(",");
		    name = nameParts[0];
		}
		if (gene_map.containsKey(name)) {
		    Gene g = gene_map.get(name);
		    g.addVariant(ve);
		}
		else {
		    Gene g = new Gene(ve);
		    gene_map.put(name, g);
		}
	    }
	}
	/** Now create a list of Genes so we can sort them. */
	this.geneList = new ArrayList<Gene>(gene_map.values());
	for (IPriority priority : this.priorityList) {
            logger.info("STARTING prioritiser: {}", priority.getPriorityName());
            priority.prioritize_list_of_genes(this.geneList);
	}
    }



    

    /**
     * This function iterates over all of the Filters chosen by the user and
     * applies each filter to the list of variants (a Java ArrayList). If a
     * variant does not pass the filter, it is removed from the list. Thus,
     * after this method is called, the list of variants is usually much shorter
     * and should just contain the major candidates from Exome sequencing.
     * 
     * @see exomizer.filter.IFilter
     */
    private void filterVariants(List<VariantEvaluation> variantList) {
	for (IFilter f : this.filterList) {
            logger.info("STARTING filter: {}", f.getFilterName());
	    f.filter_list_of_variants(variantList);
	}
    }


     /**
     * This method goes through VCF parsing, deserializing the UCSC data,
     * annotating variants, filtering and prioritizing genes, and ranking the
     * candidate genes. It is a convenience method that groups together most of
     * the pipeline and can be used by the apache tomcat Webserver.
     * <p>
     * Note that we assume that the transcript definition data has been
     * deserialized before this method is called.
     * @param variantList A list of annotated variants from Jannovar, passed to us
     * from the main exomizer program
     * @param rankBasedScoring True if we should perform rank-based scoring rather than using the raw scores.
     * @throws ExomizerException
     */
    public List<Gene> executePrioritization(List<VariantEvaluation> variantList, boolean rankBasedScoring) 
	throws ExomizerException 
    {
	/**************************************************************/
	/* 1) Filter the variants according to user-supplied params. */
	/**************************************************************/
        logger.info("FILTERING VARIANTS");
	filterVariants(variantList);
	/**************************************************************/
	/* 2) Prioritize the variants according to phenotype, model */
	/* organism data, protein protein interactions, whatever */
	/**************************************************************/
        logger.info("PRIORITISING GENES");
	prioritizeGenes(variantList);
	/**************************************************************/
	/* 3) Rank all genes now according to combined score. */
	/**************************************************************/
        logger.info("RANKING GENES");
        if (rankBasedScoring) {
	    scoreCandidateGenesByRank();
	} else {
	    rankCandidateGenes();
	}
	return this.geneList;
    }



    /** In the original implementation of the Exomiser, the genes were scored
     * according to various criteria that gave them scores between [0,1].
     * However, these scores tended not to be uniformly distributed. This
     * function implements an alternative scoring scheme that first ranks the
     * genes according to their score and then overwrites the original score
     * according to a uniform distribution based on the ranks of the genes.
     */
    private void scoreCandidateGenesByRank() {
	for (Gene g : this.geneList) {
	    g.calculateGeneAndVariantScores(this.inheritanceMode);
	}
	// Store all gene and variant scores in sortable map
	// The key is a Float representing the raw score.
	// The value is a list of one or more genes with this score.
	TreeMap<Float, List<Gene>> geneScoreMap = new TreeMap<Float, List<Gene>>();
	for (Gene g : this.geneList) {
	    float geneScore = g.getPriorityScore();
	    //System.out.println("scoreCandidateGenesByRank, " + g.getGeneSymbol() + ": " + geneScore);
	    if (geneScoreMap.containsKey(geneScore)) {
		List<Gene> geneScoreGeneList = geneScoreMap.get(geneScore);
		geneScoreGeneList.add(g);
	    } else {
		List<Gene> geneScoreGeneList = new ArrayList<Gene>();
		geneScoreGeneList.add(g);
		geneScoreMap.put(geneScore, geneScoreGeneList);
	    }
	}
	/*
	 * iterate through all gene scores in descending order calculating a
	 * score between 1 and 0 depending purely on rank and overwrite gene
	 * scores with these new scores
	 */
	float rank = 1;
	Set<Float> set = geneScoreMap.descendingKeySet();
	Iterator<Float> i = set.iterator();
	while (i.hasNext()) {
	    float score = i.next();
	    List<Gene> geneScoreGeneList = geneScoreMap.get(score);
	    int sharedHits = geneScoreGeneList.size();
	    float adjustedRank = rank;
	    if (sharedHits > 1) {
		adjustedRank = rank + (sharedHits / 2);
	    }
	    float newScore = 1f - (adjustedRank / geneList.size());
	    rank = rank + sharedHits;
	    for (Gene g : geneScoreGeneList) {
		// System.out.print(g.getGeneSymbol()+"\t");
		g.setPriorityScore(newScore);
	    }
	}
	Collections.sort(geneList);
    }

     /**
     * Calculates the final ranks of all genes that have survived the filtering
     * and ranking steps. The strategy is that for autosomal dominant diseases,
     * we take the single most pathogenic score of any variant affecting the
     * gene; for autosomal recessive diseases, we take the mean of the two most
     * pathogenic variants. X-linked diseases are filtered such that only
     * X-chromosomal genes are left over, and the single worst variant is taken.
     * <P>
     * Once the scores have been calculated, we sort the array list of
     * {@link exomizer.exome.Gene Gene} objects according to the combined
     * filter/priority score.
     */
    private void rankCandidateGenes() {
	for (Gene g : this.geneList) {
	    g.calculateGeneAndVariantScores(this.inheritanceMode);
	}
	Collections.sort(this.geneList);
    }
    




    public List<IFilter> getFilterList() { return this.filterList; }
    
    public List<IPriority> getPriorityList() { return this.priorityList; }


    public void addOMIMPrioritizer() throws ExomizerInitializationException {
	IPriority ip = new OMIMPriority();
	this.priorityList.add(ip);
	ip.setDatabaseConnection(this.connection);
    }

    public void addInheritancePrioritiser(String inheritance_filter_type) 
	throws ExomizerInitializationException 
    {
	IPriority inhp = new InheritancePriority();
	if (inheritance_filter_type != null) {
	    inhp.setParameters(inheritance_filter_type);
	    this.priorityList.add(inhp);
	    this.inheritanceMode = InheritancePriority.getModeOfInheritance(inheritance_filter_type);
	}
    }

    public void addPhenomizerPrioritiser(String phenomizerDataDirectory, String hpoTermList) 
	throws ExomizerInitializationException
    {
	Set<String> hpoIDset = new HashSet<String>();
	String A[] = hpoTermList.split(",");
	for (String s : A) {
	    hpoIDset.add(s.trim());
	}
	boolean symmetric = false;
	IPriority ip = new PhenomizerPriority(phenomizerDataDirectory, hpoIDset, symmetric);
	this.priorityList.add(ip);
    }

    public void addMGIPhenodigmPrioritiser(String disease) 
	throws ExomizerInitializationException
    {
	IPriority ip = new MGIPhenodigmPriority(disease);
	ip.setDatabaseConnection(this.connection);
	this.priorityList.add(ip);
    }
	    
    public void addBOQAPrioritiser(String hpoOntologyFile, String hpoAnnotationFile,  String hpoTermList)
	throws ExomizerInitializationException
    {
	IPriority ip = new BoqaPriority(hpoOntologyFile, hpoAnnotationFile, hpoTermList);
	ip.setDatabaseConnection(this.connection);
	this.priorityList.add(ip);
    }

    public void addDynamicPhenodigmPrioritiser(String hpoTermList)
	throws ExomizerInitializationException
    {
	IPriority ip = new DynamicPhenodigmPriority(hpoTermList);
	ip.setDatabaseConnection(this.connection);
	this.priorityList.add(ip);
    }

    public void addZFINPrioritiser(String disease)
	throws ExomizerInitializationException
    {
	IPriority ip = new ZFINPhenodigmPriority(disease);
	ip.setDatabaseConnection(this.connection);
	this.priorityList.add(ip);
    }

    public void addExomeWalkerPrioritiser(String rwFilePath, String rwIndexPath, String entrezSeedGenes) 
	throws ExomizerInitializationException
    {
	IPriority ip = new GenewandererPriority(rwFilePath, rwIndexPath);
        ip.setParameters(entrezSeedGenes);
	this.priorityList.add(ip);
    }
    
//    public void addPhenoWandererPrioritiser(String rwFilePath, String rwIndexPath, String disease, String candGene) 
//	throws ExomizerInitializationException
//    {
//	IPriority ip = new PhenoWandererPriority(rwFilePath, rwIndexPath, disease, candGene);
//        ip.setDatabaseConnection(this.connection);
//	this.priorityList.add(ip);
//    }
//    
    public void addDynamicPhenoWandererPrioritiser(String rwFilePath, String rwIndexPath, String hpoids, String candGene, String disease, DataMatrix rwMatrix) 
	throws ExomizerInitializationException
    {
        IPriority ip = new DynamicPhenoWandererPriority(rwFilePath, rwIndexPath, hpoids, candGene, disease, rwMatrix);
        ip.setDatabaseConnection(this.connection);
	this.priorityList.add(ip);
    }

    /** Filter on variant type that is expected potential pathogenic (Missense, Intergenic etc
     * and not off target (INTERGENIC, UPSTREAM, DOWNSTREAM). */
    public void addTargetFilter() 
	throws ExomizerInitializationException
    {
	IFilter f = new TargetFilter();
	this.filterList.add(f);
    }


     public void addBedFilter(Set<String> commalist)	throws ExomizerInitializationException
    {
	IFilter f = new BedFilter(commalist);
	this.filterList.add(f);
    }


    /** Add a frequency filter. There are several options. If the argument
     * filterOutAllDbsnp is true, then all dbSNP entries are removed (dangerous).
     * Else if the freuqency is set to some value, we set this is the maximum MAF.
     * else we set the frequency filter to 100%, i.e., no filtering.
     */
    public void addFrequencyFilter(String frequency_threshold, boolean filterOutAllDbsnp) 
     	throws ExomizerInitializationException
    {
	IFilter f=null;
	if (filterOutAllDbsnp) {
	    f = new FrequencyFilter();
	    f.setDatabaseConnection(this.connection);
	    f.set_parameters("RS");
	    this.filterList.add(f);
	} else if (frequency_threshold != null && 
		   !frequency_threshold.equals("none")) {
	    f = new FrequencyFilter();
	    f.setDatabaseConnection(this.connection);
	    f.set_parameters(frequency_threshold);
	    this.filterList.add(f);
	} else {
	    // default is freq filter at 100 i.e. keep everything so still
	    // get freq data in output and inclusion in prioritization
	    f = new FrequencyFilter();
	    f.setDatabaseConnection(this.connection);
	    f.set_parameters("100");
	    this.filterList.add(f);
	}
    }

     public void addQualityFilter(String quality_threshold)
	 throws ExomizerInitializationException
    {
	IFilter f=null;
	if (quality_threshold != null) {
	    f = new QualityFilter();
	    f.set_parameters(quality_threshold);
	    this.filterList.add(f);
	}
     }

    public void addPathogenicityFilter(boolean filterOutNonpathogenic, boolean removeSyn) 
    	throws ExomizerInitializationException
    {
	PathogenicityFilter f = new PathogenicityFilter();
	f.setDatabaseConnection(this.connection);
	if (filterOutNonpathogenic) {
	    f.set_parameters("filter");
	}
        f.set_syn_filter_status(removeSyn);
	this.filterList.add(f);
    }

    public void addLinkageFilter(String interval)
    	throws ExomizerInitializationException
    {
	if (interval != null) {
	   IFilter f = new IntervalFilter();
	   f.set_parameters(interval);
	   this.filterList.add(f);
	}
    }
    
    public void setPrioritizer(IPriority ip) {
	if (this.priorityList == null) {
	    this.priorityList = new ArrayList<IPriority>();
	}
	this.priorityList.add(ip);
    }
}
