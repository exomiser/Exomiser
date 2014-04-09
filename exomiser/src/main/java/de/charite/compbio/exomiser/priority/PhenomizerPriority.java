package de.charite.compbio.exomiser.priority;



import hpo.HPOutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import ontologizer.go.OBOParser;
import ontologizer.go.OBOParserException;
import ontologizer.go.Ontology;
import ontologizer.go.Term;
import ontologizer.go.TermContainer;
import similarity.SimilarityUtilities;
import similarity.concepts.ResnikSimilarity;
import similarity.objects.InformationContentObjectSimilarity;
import sonumina.math.graph.SlimDirectedGraphView;
import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.priority.util.ScoreDistribution;
import de.charite.compbio.exomiser.priority.util.ScoreDistributionContainer;

/**
 * Filter variants according to the phenotypic similarity of the specified
 * disease to mouse models disrupting the same gene. We use semantic similarity
 * calculations in the uberpheno.
 * 
 * The files required for the constructor of this filter should be downloaded
 * from: {@code http://purl.obolibrary.org/obo/hp/uberpheno/}
 * (HSgenes_crossSpeciesPhenoAnnotation.txt, crossSpeciesPheno.obo)
 * 
 * @author Sebastian Koehler
 * @version 0.06 (6 December, 2013)
 */
public class PhenomizerPriority implements IPriority {

    /** The HPO as Ontologizer-Ontology object */
    private Ontology hpo;

    /** The HPO as SlimDirectedGraph (fast access to ancestors etc.) */
    private SlimDirectedGraphView<Term> hpoSlim;

    /** A list of error-messages */
    private ArrayList<String> error_record = null;
    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private ArrayList<String> messages = null;

    /** The semantic similarity measure used to calculate phenotypic similarity */
    private InformationContentObjectSimilarity similarityMeasure;
    /** The HPO terms entered by the user describing the individual who is being
     * sequenced by exome-sequencing or clinically relevant genome panel. */
    private ArrayList<Term> hpoQueryTerms;

    private float DEFAULT_SCORE = 0f;

    private HashMap<String, ArrayList<Term>> geneId2annotations;
    
    private HashMap<Term, HashSet<String>> annotationTerm2geneIds;

    private HashMap<Term, Double> term2ic;

    private final ScoreDistributionContainer scoredistributionContainer = new ScoreDistributionContainer();

    private int numberQueryTerms;
    /** A counter of the number of genes that could not be found in the database as being associated with 
     * a defined disease gene. */
    private int offTargetGenes=0;
    /** Total number of genes used for the query, including genes with no associated disease. */
    private int totalGenes;

    private boolean symmetric;
    /** Path to the directory that has the files needed to calculate the score distribution. */
    private String scoredistributionFolder;
    /** Keeps track of the maximum semantic similarity score to date */
    private double maxSemSim=0d;
    /** Keeps track of the maximum negative log of the p value to date */
    private double maxNegLogP=0d;
    

    /**
     * Create a new instance of the PhenomizerPriority.
     * @param scoreDistributionFolder
     *            Folder which contains the score distributions (e.g. 3.out,
     *            3_symmetric.out, 4.out, 4_symmetric.out). It must also contain the
     *  files hp.obo (obtained from {@code http://compbio.charite.de/hudson/job/hpo/}) and
     * ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt-file
     *  (obtained from
     *   {@code http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastSuccessfulBuild/artifact/annotation/}).
     * @param hpoQueryTermIds
     *            List of HPO terms
     * @param symmetric Flag to indicate if the semantic similarity score should be calculated using the symmetrix formula.
     * @throws ExomizerInitializationException
     * @see <a href="http://purl.obolibrary.org/obo/hp/uberpheno/">Uberpheno
     *      Hudson page</a>
     */
    public PhenomizerPriority(String scoreDistributionFolder, Set<String> hpoQueryTermIds,
			      boolean symmetric) throws ExomizerInitializationException {
	
	if (!scoreDistributionFolder.endsWith(File.separatorChar + ""))
	    scoreDistributionFolder += File.separatorChar;
	this.scoredistributionFolder = scoreDistributionFolder;
	String hpoOboFile = String.format("%s%s",scoreDistributionFolder,"hp.obo");
	String hpoAnnotationFile =  String.format("%s%s",scoreDistributionFolder,"ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt");
	try {
	    parseData(hpoOboFile, hpoAnnotationFile, scoreDistributionFolder);
	} catch (ExomizerInitializationException e) {
	    String s = String.format("Error parsing Phenomizer input files. The phenomizerData directory must "+
		"contain the files \"hp.obo\", \"ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt\" as " +
		" well as the score distribution files \"*.out\", all of which can be downloaded from the "+
				     " HPO hudson server. The ScoreDistribution Folder path was:\"%s\"",
				     scoreDistributionFolder);  
	    s = String.format("%s\n%s", s, e.getMessage());
	    throw new ExomizerInitializationException(s);

	} 
	
	HashSet<Term> hpoQueryTermsHS = new HashSet<Term>();
	for (String termIdString : hpoQueryTermIds) {
	    Term t = hpo.getTermIncludingAlternatives(termIdString);
	    if (t == null) {
		throw new ExomizerInitializationException("invalid term-id given: " + termIdString);
	    }
	    hpoQueryTermsHS.add(t);
	}
	hpoQueryTerms = new ArrayList<Term>();
	hpoQueryTerms.addAll(hpoQueryTermsHS);
	this.symmetric = symmetric;
	
	numberQueryTerms = hpoQueryTerms.size();
	if (!scoredistributionContainer.didParseDistributions(symmetric, numberQueryTerms)) {
	    scoredistributionContainer.parseDistributions(symmetric, numberQueryTerms, scoreDistributionFolder);
	}
	
	ResnikSimilarity resnik = new ResnikSimilarity(hpo, term2ic);
	similarityMeasure = new InformationContentObjectSimilarity(resnik, symmetric, false);
	
	/* some logging stuff */
	this.error_record = new ArrayList<String>();
	this.messages = new ArrayList<String>();
    }
    
    private void parseData(String hpoOboFile, String hpoAnnotationFile, String scoreDistributionFolder) 
	throws ExomizerInitializationException
    {
	try {
	    parseOntology(hpoOboFile);
	} catch (OBOParserException e) {
	    String s = String.format("Error parsing ontology file (%s): %s",hpoOboFile,e.getMessage());
	    throw new ExomizerInitializationException(s);
	} catch (IOException ioe) {
	    String s = String.format("I/O Error with ontology file (%s): %s",hpoOboFile,ioe.getMessage());
	    throw new ExomizerInitializationException(s);
	}
	try {
	    parseAnnotations(hpoAnnotationFile);
	} catch (IOException e) {
	    String s = String.format("Error parsing annotation file (%s): %s",hpoAnnotationFile,e.getMessage());
	    throw new ExomizerInitializationException(s);
	}
    }

    /**
     * Parse the HPO phenotype annotation file (e.g., phenotype_annotation.tab). The
     * point of this is to get the links between diseases and HPO phenotype terms.
     * The hpoAnnotationFile is
     *            The ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt-file
     * 
     * @param hpoAnnotationFile path to the file 
     */
    private void parseAnnotations(String hpoAnnotationFile) throws IOException {
	geneId2annotations = new HashMap<String, ArrayList<Term>>();

	BufferedReader in = new BufferedReader(new FileReader(hpoAnnotationFile));
	String line = null;
	while ((line = in.readLine()) != null) {
	    if (line.startsWith("#"))
		continue;
	    
	    String[] split = line.split("\t");
	    String entrez = split[0];
	    Term t = null;
	    try {
		/* split[4] is the HPO term field of an annotation line. */
		t = hpo.getTermIncludingAlternatives(split[3]);
	    } catch (IllegalArgumentException e) {
		System.out.println("Unable to get term for line \n" + line + "\n");
		System.out.println("The offending field was \"" + split[3] + "\"");
		for (int k=0;k<split.length;++k) {
		    System.out.println(k + ") \"" + split[k] + "\"");
		}
		t = null;
	    }
	    if (t == null)
		continue;
	    
	    ArrayList<Term> annot;
	    if (geneId2annotations.containsKey(entrez))
		annot = geneId2annotations.get(entrez);
	    else
		annot = new ArrayList<Term>();
	    annot.add(t);
	    geneId2annotations.put(entrez, annot);
	}
	in.close();
	
	// cleanup annotations
	for (String entrez : geneId2annotations.keySet()) {
	    ArrayList<Term> terms = geneId2annotations.get(entrez);
	    HashSet<Term> uniqueTerms = new HashSet<Term>(terms);
	    ArrayList<Term> uniqueTermsAL = new ArrayList<Term>();
	    uniqueTermsAL.addAll(uniqueTerms);
	    ArrayList<Term> termsMostSpecific = HPOutils.cleanUpAssociation(uniqueTermsAL, hpoSlim, hpo.getRootTerm());
	    geneId2annotations.put(entrez, termsMostSpecific);
	}
	
	// prepare IC computation
	annotationTerm2geneIds = new HashMap<Term, HashSet<String>>();
	for (String oId : geneId2annotations.keySet()) {
	    ArrayList<Term> annotations = geneId2annotations.get(oId);
	    for (Term annot : annotations) {
		ArrayList<Term> termAndAncestors = hpoSlim.getAncestors(annot);
		for (Term t : termAndAncestors) {
		    HashSet<String> objectsAnnotatedByTerm; // here we store
		    // which objects
		    // have been
		    // annotated with
		    // this term
		    if (annotationTerm2geneIds.containsKey(t))
			objectsAnnotatedByTerm = annotationTerm2geneIds.get(t);
		    else
			objectsAnnotatedByTerm = new HashSet<String>();
		    
		    objectsAnnotatedByTerm.add(oId); // add the current object
		    annotationTerm2geneIds.put(t, objectsAnnotatedByTerm);
		}
	    }
	}
	term2ic = caclulateTermIC(hpo, annotationTerm2geneIds);
	
    }

    /**
     * Parses the human-phenotype-ontology.obo file (or equivalently, the
     * hp.obo file from our Hudosn server).
     * @param hpoOboFile path to the hp.obo file.
     */
    private Ontology parseOntology(String hpoOboFile) throws IOException, OBOParserException {
	OBOParser oboParser = new OBOParser(hpoOboFile, OBOParser.PARSE_XREFS);
	String parseInfo = oboParser.doParse();
	System.out.println(parseInfo);
	
	TermContainer termContainer = new TermContainer(oboParser.getTermMap(), oboParser.getFormatVersion(), oboParser.getDate());
	Ontology hpo = new Ontology(termContainer);
	hpo.setRelevantSubontology(termContainer.get(HPOutils.organAbnormalityRootId).getName());
	SlimDirectedGraphView<Term> hpoSlim = hpo.getSlimGraphView();
	
	this.hpo = hpo;
	this.hpoSlim = hpoSlim;
	return hpo;
    }

    /**
     * @see exomizer.priority.IPriority#getPriorityName()
     */
    @Override public String getPriorityName() {
	return "HPO Phenomizer prioritizer";
    }

    /** Flag to output results of filtering against Uberpheno data. */
    @Override
	public FilterType getPriorityTypeConstant() {
	return FilterType.PHENOMIZER_FILTER;
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     *         of score filtering.
     */
    public ArrayList<String> getMessages() {
	if (this.error_record.size() > 0) {
	    for (String s : error_record) {
		this.messages.add("Error: " + s);
	    }
	}
	return this.messages;
    }

    /**
     * Prioritize a list of candidate {@link exomizer.exome.Gene Gene} objects
     * (the candidate genes have rare, potentially pathogenic variants).
     * 
     * @param gene_list  List of candidate genes.
     * @see exomizer.filter.IFilter#filter_list_of_variants(java.util.ArrayList)
     */
    @Override 
    public void prioritize_list_of_genes(List<Gene> gene_list) {
	this.totalGenes = gene_list.size();

	for (Gene gene : gene_list) {
	    try {
		PhenomizerRelevanceScore phenomizerRelScore = scoreVariantHPO(gene);
		gene.addRelevanceScore(phenomizerRelScore, FilterType.PHENOMIZER_FILTER);
		//System.out.println("Phenomizer Gene="+gene.getGeneSymbol()+" score=" +phenomizerRelScore.getRelevanceScore());
	    } catch (ExomizerException e) {
		error_record.add(e.toString());
	    }
	}
	String s = String.format("Data investigated in HPO for %d genes. No data for %d genes", gene_list.size(),this.offTargetGenes);
	//System.out.println(s);
	normalizePhenomizerScores(gene_list);
	this.messages.add(s);
    }

    /**
     * The gene relevance scores are to be normalized to lie between zero and
     * one. This function, which relies upon the variable {@link #maxSemSim} being
     * set in {@link #scoreVariantHPO}, divides each score by {@link #maxSemSim}, 
     * which has the effect of putting the phenomizer scores in the range [0..1]. 
     * Note that for now we are using the semantic similarity scores, but we should 
     * also try the P value version (TODO).
     * Note that this is not the same as rank normalization!
     */
    private void normalizePhenomizerScores(List<Gene> gene_list) {
	if ( maxSemSim < 1) return;
	PhenomizerRelevanceScore.setNormalizationFactor(1d/maxSemSim);
	/*for (Gene g : gene_list) {
	    float score = g.getRelevanceScore(FilterType.PHENOMIZER_FILTER);
	    score /= this.maxSemSim;
	    g.resetRelevanceScore(FilterType.PHENOMIZER_FILTER, score);
	    }*/
    }




    /**
     * @param g   A {@link exomizer.exome.Gene Gene} whose score is to be
     *            determined.
     */
    private PhenomizerRelevanceScore scoreVariantHPO(Gene g) throws ExomizerException {
	
	int entrezGeneId = g.getEntrezGeneID();
	String entrezGeneIdString = entrezGeneId + "";
	
	if (!geneId2annotations.containsKey(entrezGeneIdString)) {
	    //System.err.println("INVALID GENE GIVEN (will set to default-score): Entrez ID: " + g.getEntrezGeneID() + " / " + g.getGeneSymbol());
	    this.offTargetGenes++;
	    return new PhenomizerRelevanceScore(DEFAULT_SCORE);
	}
	
	ArrayList<Term> annotationsOfGene = geneId2annotations.get(entrezGeneIdString);
	
	double similarityScore = similarityMeasure.computeObjectSimilarity(hpoQueryTerms, annotationsOfGene);
	if (similarityScore > maxSemSim) 
	    maxSemSim = similarityScore;
	if (Double.isNaN(similarityScore)) {
	    throw new ExomizerException("score was NAN for gene:" + g + " : " + hpoQueryTerms + " <-> " + annotationsOfGene);
	}
	
	ScoreDistribution scoreDist = scoredistributionContainer.getDistribution(entrezGeneIdString, numberQueryTerms, symmetric,
										 scoredistributionFolder);
	
	// get the pvalue
	
	double rawPvalue;
	if (scoreDist == null)
	    return new PhenomizerRelevanceScore(DEFAULT_SCORE);
	else {
	    rawPvalue = scoreDist.getPvalue(similarityScore, 1000.);
	    rawPvalue = Math.log(rawPvalue) * -1.0; /* Negative log of p value : most significant get highest score */
	    if (rawPvalue > maxNegLogP)
		maxNegLogP = rawPvalue;
	}
	
	return new PhenomizerRelevanceScore(rawPvalue, similarityScore);
	// // filter genes not associated with any disease
	// if
	// (!HPOutils.diseaseGeneMapper.entrezId2diseaseIds.containsKey(entrezGeneId))
	// return new PhenomizerRelevanceScore(DEFAULT_SCORE);
	//
	// double sum = 0; // sum of semantic similarity
	// int num = 0; // required to make average
	// for (DiseaseId diseaseId :
	// HPOutils.diseaseGeneMapper.entrezId2diseaseIds.get(entrezGeneId)) {
	//
	// DiseaseEntry diseaseEntry = HPOutils.diseaseId2entry.get(diseaseId);
	//
	// if (diseaseEntry == null) {
	// // System.out.println("diseaseID = " + diseaseId);
	// // System.out.println("diseaseEntry = NULL " );
	// // return new PhenomizerRelevanceScore(DEFAULT_SCORE);
	// continue;
	// }
	// ArrayList<Term> termsAL = diseaseEntry.getOrganAssociatedTerms();
	// if (termsAL == null || termsAL.size() < 1) {
	// continue;
	// // return new PhenomizerRelevanceScore(DEFAULT_SCORE);
	// }
	// double similarityScore =
	// similarityMeasure.computeObjectSimilarity(hpoQueryTerms, termsAL);
	// sum += similarityScore;
	// ++num;
	// }
	// if (num == 0) {
	// return new PhenomizerRelevanceScore(DEFAULT_SCORE);
	// }
	//
	// double avg = sum / num;
	// return new PhenomizerRelevanceScore(avg);
    }
    
    /**
     * Flag to show results of this analysis in the HTML page.
     */
    public boolean display_in_HTML() {
	return true;
    }
    
    /**
     * @return an ul list with summary of phenomizer prioritization.
     */
    public String getHTMLCode() {
	String s = String.format("Phenomizer: %d genes were evaluated; no phenotype data available for %d of them",
				 this.totalGenes, this.offTargetGenes);
	String t = null;
	if (symmetric)
	    t = String.format("Symmetric Phenomizer query with %d terms was performed",this.numberQueryTerms);
	else
	    t = String.format("Asymmetric Phenomizer query with %d terms was performed",this.numberQueryTerms);
	String u = String.format("Maximum semantic similarity score: %.2f, maximum negative log. of p-value: %.2f",
				 this.maxSemSim, this.maxNegLogP);
	return String.format("<ul><li>%s</li><li>%s</li><li>%s</li></ul>\n",s,t,u);
	
    }
    
    /** Get number of variants before filter was applied */
    @Override public int getBefore() {
	return this.totalGenes;
    }
    
    /** Get number of variants after filter was applied (this number will show the number of genes with 
     * HPO annotations. Other genes are not removed, however.*/
    @Override public int getAfter() {
	return this.totalGenes - this.offTargetGenes;
    }
    
    /**
     * Set parameters of prioritizer if needed.
     * 
     * @param par
     *            A String with the parameters (usually extracted from the cmd
     *            line) for this prioiritizer)
     */
    @Override
	public void setParameters(String par) {
	/* -- Nothing needed now --- */
    }
    
    /**
     * This class does not need a database connection, this function only there
     * to satisfy the interface.
     * 
     * @param connection
     *            An SQL (postgres) connection that was initialized elsewhere.
     */
    @Override
	public void setDatabaseConnection(java.sql.Connection connection) throws ExomizerInitializationException { 
	/** no - op */
    }

    private HashMap<Term, Double> caclulateTermIC(Ontology ontology, HashMap<Term, HashSet<String>> term2objectIdsAnnotated) {
	
	Term root = ontology.getRootTerm();
	HashMap<Term, Integer> term2frequency = new HashMap<Term, Integer>();
	for (Term t : term2objectIdsAnnotated.keySet()) {
	    term2frequency.put(t, term2objectIdsAnnotated.get(t).size());
	}
	
	int maxFreq = term2frequency.get(root);
	HashMap<Term, Double> term2informationContent = SimilarityUtilities.caculateInformationContent(maxFreq, term2frequency);
	
	int frequencyZeroCounter = 0;
	double ICzeroCountTerms = -1 * (Math.log(1 / (double) maxFreq));
	
	for (Term t : ontology) {
	    if (!term2frequency.containsKey(t)) {
		++frequencyZeroCounter;
		term2informationContent.put(t, ICzeroCountTerms);
	    }
	}
	
	System.out.println("WARN: Frequency of " + frequencyZeroCounter + " terms was zero!!");
	System.out.println("Set IC of these to : " + ICzeroCountTerms);
	return term2informationContent;
    }
    
}
/* eof */
