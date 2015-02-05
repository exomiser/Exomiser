package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.util.ScoreDistribution;
import de.charite.compbio.exomiser.core.prioritisers.util.ScoreDistributionContainer;
import hpo.HPOutils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ontologizer.go.OBOParser;
import ontologizer.go.OBOParserException;
import ontologizer.go.Ontology;
import ontologizer.go.Term;
import ontologizer.go.TermContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import similarity.SimilarityUtilities;
import similarity.concepts.ResnikSimilarity;
import similarity.objects.InformationContentObjectSimilarity;
import sonumina.math.graph.SlimDirectedGraphView;

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
public class PhenixPriority implements Priority {

    private static final Logger logger = LoggerFactory.getLogger(PhenixPriority.class);

    /**
     * The HPO as Ontologizer-Ontology object
     */
    private Ontology hpo;

    /**
     * The HPO as SlimDirectedGraph (fast access to ancestors etc.)
     */
    private SlimDirectedGraphView<Term> hpoSlim;

    /**
     * A list of error-messages
     */
    private ArrayList<String> error_record = null;
    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private ArrayList<String> messages = null;

    /**
     * The semantic similarity measure used to calculate phenotypic similarity
     */
    private InformationContentObjectSimilarity similarityMeasure;
    /**
     * The HPO terms entered by the user describing the individual who is being
     * sequenced by exome-sequencing or clinically relevant genome panel.
     */
    private ArrayList<Term> hpoQueryTerms;

    private float DEFAULT_SCORE = 0f;

    private HashMap<String, ArrayList<Term>> geneId2annotations;

    private HashMap<Term, HashSet<String>> annotationTerm2geneIds;

    private HashMap<Term, Double> term2ic;

    private final ScoreDistributionContainer scoredistributionContainer = new ScoreDistributionContainer();

    private int numberQueryTerms;
    /**
     * A counter of the number of genes that could not be found in the database
     * as being associated with a defined disease gene.
     */
    private int offTargetGenes = 0;
    /**
     * Total number of genes used for the query, including genes with no
     * associated disease.
     */
    private int analysedGenes;

    private boolean symmetric;
    /**
     * Path to the directory that has the files needed to calculate the score
     * distribution.
     */
    private String scoredistributionFolder;
    /**
     * Keeps track of the maximum semantic similarity score to date
     */
    private double maxSemSim = 0d;
    /**
     * Keeps track of the maximum negative log of the p value to date
     */
    private double maxNegLogP = 0d;

    /**
     * Create a new instance of the PhenixPriority.
     *
     * @param scoreDistributionFolder Folder which contains the score
     * distributions (e.g. 3.out, 3_symmetric.out, 4.out, 4_symmetric.out). It
     * must also contain the files hp.obo (obtained from
     * {@code http://compbio.charite.de/hudson/job/hpo/}) and
     * ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt-file (obtained from
     * {@code http://compbio.charite.de/hudson/job/hpo.annotations.monthly/lastSuccessfulBuild/artifact/annotation/}).
     * @param hpoQueryTermIds List of HPO terms
     * @param symmetric Flag to indicate if the semantic similarity score should
     * be calculated using the symmetrix formula.
     * @throws ExomizerInitializationException
     * @see <a href="http://purl.obolibrary.org/obo/hp/uberpheno/">Uberpheno
     * Hudson page</a>
     */
    public PhenixPriority(String scoreDistributionFolder, Set<String> hpoQueryTermIds, boolean symmetric) {

        if (!scoreDistributionFolder.endsWith(File.separatorChar + "")) {
            scoreDistributionFolder += File.separatorChar;
        }
        this.scoredistributionFolder = scoreDistributionFolder;
        String hpoOboFile = String.format("%s%s", scoreDistributionFolder, "hp.obo");
        String hpoAnnotationFile = String.format("%s%s", scoreDistributionFolder, "ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt");
        parseData(hpoOboFile, hpoAnnotationFile);

        HashSet<Term> hpoQueryTermsHS = new HashSet<Term>();
        for (String termIdString : hpoQueryTermIds) {
            Term t = hpo.getTermIncludingAlternatives(termIdString);
            if (t != null) {
                hpoQueryTermsHS.add(t);
            } else {
                logger.error("invalid term-id given: " + termIdString);
            }
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

    private void parseData(String hpoOboFile, String hpoAnnotationFile) {
        //The phenomizerData directory must contain the files "hp.obo", "ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt" 
        //as well as the score distribution files "*.out", all of which can be downloaded from the HPO hudson server.
        try {
            parseOntology(hpoOboFile);
        } catch (OBOParserException e) {
            logger.error("Error parsing ontology file {}", hpoOboFile, e);
        } catch (IOException ioe) {
            logger.error("I/O Error with ontology file{}", hpoOboFile, ioe);
        }
        try {
            parseAnnotations(hpoAnnotationFile);
        } catch (IOException e) {
            logger.error("Error parsing annotation file {}", hpoAnnotationFile, e);
        }
    }

    /**
     * Parse the HPO phenotype annotation file (e.g., phenotype_annotation.tab).
     * The point of this is to get the links between diseases and HPO phenotype
     * terms. The hpoAnnotationFile is The
     * ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt-file
     *
     * @param hpoAnnotationFile path to the file
     */
    private void parseAnnotations(String hpoAnnotationFile) throws IOException {
        geneId2annotations = new HashMap<String, ArrayList<Term>>();

        BufferedReader in = new BufferedReader(new FileReader(hpoAnnotationFile));
        String line = null;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }

            String[] split = line.split("\t");
            String entrez = split[0];
            Term t = null;
            try {
                /* split[4] is the HPO term field of an annotation line. */
                t = hpo.getTermIncludingAlternatives(split[3]);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get term for line \n{}\n", line);
                logger.error("The offending field was '{}'", split[3]);
                for (int k = 0; k < split.length; ++k) {
                    logger.error("{} '{}'", k, split[k]);
                }
                t = null;
            }
            if (t == null) {
                continue;
            }

            ArrayList<Term> annot;
            if (geneId2annotations.containsKey(entrez)) {
                annot = geneId2annotations.get(entrez);
            } else {
                annot = new ArrayList<>();
            }
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
                    if (annotationTerm2geneIds.containsKey(t)) {
                        objectsAnnotatedByTerm = annotationTerm2geneIds.get(t);
                    } else {
                        objectsAnnotatedByTerm = new HashSet<String>();
                    }

                    objectsAnnotatedByTerm.add(oId); // add the current object
                    annotationTerm2geneIds.put(t, objectsAnnotatedByTerm);
                }
            }
        }
        term2ic = caclulateTermIC(hpo, annotationTerm2geneIds);

    }

    /**
     * Parses the human-phenotype-ontology.obo file (or equivalently, the hp.obo
     * file from our Hudosn server).
     *
     * @param hpoOboFile path to the hp.obo file.
     */
    private Ontology parseOntology(String hpoOboFile) throws IOException, OBOParserException {
        OBOParser oboParser = new OBOParser(hpoOboFile, OBOParser.PARSE_XREFS);
        String parseInfo = oboParser.doParse();
        logger.info(parseInfo);

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
    @Override
    public String getPriorityName() {
        return "HPO Phenomizer prioritizer";
    }

    /**
     * Flag to output results of filtering against Uberpheno data.
     */
    @Override
    public PriorityType getPriorityType() {
        return PriorityType.PHENIX_PRIORITY;
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     * of score filtering.
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
     * @param gene_list List of candidate genes.
     * @see exomizer.filter.Filter#filter_list_of_variants(java.util.ArrayList)
     */
    @Override
    public void prioritizeGenes(List<Gene> gene_list) {
        analysedGenes = gene_list.size();

        for (Gene gene : gene_list) {
            PhenixPriorityResult phenomizerRelScore = scoreVariantHPO(gene);
            gene.addPriorityResult(phenomizerRelScore);
            //System.out.println("Phenomizer Gene="+gene.getGeneSymbol()+" score=" +phenomizerRelScore.getScore());
        }
        String s = String.format("Data investigated in HPO for %d genes. No data for %d genes", analysedGenes, this.offTargetGenes);
        //System.out.println(s);
        normalizePhenomizerScores(gene_list);
        this.messages.add(s);
    }

    /**
     * The gene relevance scores are to be normalized to lie between zero and
     * one. This function, which relies upon the variable {@link #maxSemSim}
     * being set in {@link #scoreVariantHPO}, divides each score by
     * {@link #maxSemSim}, which has the effect of putting the phenomizer scores
     * in the range [0..1]. Note that for now we are using the semantic
     * similarity scores, but we should also try the P value version (TODO).
     * Note that this is not the same as rank normalization!
     */
    private void normalizePhenomizerScores(List<Gene> gene_list) {
        if (maxSemSim < 1) {
            return;
        }
        PhenixPriorityResult.setNormalizationFactor(1d / maxSemSim);
        /*for (Gene g : gene_list) {
         float score = g.getRelevagetScorepe.PHENIX_PRIORITY);
         score /= this.maxSemSim;
         g.setScore(FilterType.PHENIX_PRIORITY, score);
         }*/
    }

    /**
     * @param g A {@link exomizer.exome.Gene Gene} whose score is to be
     * determined.
     */
    private PhenixPriorityResult scoreVariantHPO(Gene g) {

        int entrezGeneId = g.getEntrezGeneID();
        String entrezGeneIdString = entrezGeneId + "";

        if (!geneId2annotations.containsKey(entrezGeneIdString)) {
            //System.err.println("INVALID GENE GIVEN (will set to default-score): Entrez ID: " + g.getEntrezGeneID() + " / " + g.getGeneSymbol());
            this.offTargetGenes++;
            return new PhenixPriorityResult(DEFAULT_SCORE);
        }

        ArrayList<Term> annotationsOfGene = geneId2annotations.get(entrezGeneIdString);

        double similarityScore = similarityMeasure.computeObjectSimilarity(hpoQueryTerms, annotationsOfGene);
        if (similarityScore > maxSemSim) {
            maxSemSim = similarityScore;
        }
        if (Double.isNaN(similarityScore)) {
            error_record.add("score was NAN for gene:" + g + " : " + hpoQueryTerms + " <-> " + annotationsOfGene);
        }

        ScoreDistribution scoreDist = scoredistributionContainer.getDistribution(entrezGeneIdString, numberQueryTerms, symmetric,
                scoredistributionFolder);

	// get the pvalue
        double rawPvalue;
        if (scoreDist == null) {
            return new PhenixPriorityResult(DEFAULT_SCORE);
        } else {
            rawPvalue = scoreDist.getPvalue(similarityScore, 1000.);
            rawPvalue = Math.log(rawPvalue) * -1.0; /* Negative log of p value : most significant get highest score */

            if (rawPvalue > maxNegLogP) {
                maxNegLogP = rawPvalue;
            }
        }

        return new PhenixPriorityResult(rawPvalue, similarityScore);
	// // filter genes not associated with any disease
        // if
        // (!HPOutils.diseaseGeneMapper.entrezId2diseaseIds.containsKey(entrezGeneId))
        // return new PhenixPriorityResult(DEFAULT_SCORE);
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
        // // return new PhenixPriorityResult(DEFAULT_SCORE);
        // continue;
        // }
        // ArrayList<Term> termsAL = diseaseEntry.getOrganAssociatedTerms();
        // if (termsAL == null || termsAL.size() < 1) {
        // continue;
        // // return new PhenixPriorityResult(DEFAULT_SCORE);
        // }
        // double similarityScore =
        // similarityMeasure.computeObjectSimilarity(hpoQueryTerms, termsAL);
        // sum += similarityScore;
        // ++num;
        // }
        // if (num == 0) {
        // return new PhenixPriorityResult(DEFAULT_SCORE);
        // }
        //
        // double avg = sum / num;
        // return new PhenixPriorityResult(avg);
    }

    /**
     * Flag to show results of this analysis in the HTML page.
     *
     * @return
     */
    @Override
    public boolean displayInHTML() {
        return true;
    }

    /**
     * @return an ul list with summary of phenomizer prioritization.
     */
    @Override
    public String getHTMLCode() {
        String s = String.format("Phenomizer: %d genes were evaluated; no phenotype data available for %d of them",
                this.analysedGenes, this.offTargetGenes);
        String t = null;
        if (symmetric) {
            t = String.format("Symmetric Phenomizer query with %d terms was performed", this.numberQueryTerms);
        } else {
            t = String.format("Asymmetric Phenomizer query with %d terms was performed", this.numberQueryTerms);
        }
        String u = String.format("Maximum semantic similarity score: %.2f, maximum negative log. of p-value: %.2f",
                this.maxSemSim, this.maxNegLogP);
        return String.format("<ul><li>%s</li><li>%s</li><li>%s</li></ul>\n", s, t, u);

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

        logger.info("WARNING: Frequency of {} terms was zero!! Set IC of these to : {}", frequencyZeroCounter, ICzeroCountTerms);
        return term2informationContent;
    }

}
