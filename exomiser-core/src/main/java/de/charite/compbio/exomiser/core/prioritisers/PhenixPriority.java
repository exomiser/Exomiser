/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.util.ScoreDistribution;
import de.charite.compbio.exomiser.core.prioritisers.util.ScoreDistributionContainer;
import hpo.HPOutils;
import ontologizer.go.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import similarity.SimilarityUtilities;
import similarity.concepts.ResnikSimilarity;
import similarity.objects.InformationContentObjectSimilarity;
import sonumina.math.graph.SlimDirectedGraphView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
public class PhenixPriority implements Prioritiser {

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
    private List<String> errorMessages = null;


    /**
     * The semantic similarity measure used to calculate phenotypic similarity
     */
    private InformationContentObjectSimilarity similarityMeasure;
    /**
     * The HPO terms entered by the user describing the individual who is being
     * sequenced by exome-sequencing or clinically relevant genome panel.
     */
    private List<Term> hpoQueryTerms;

    private float DEFAULT_SCORE = 0f;

    private Map<String, List<Term>> geneId2annotations;

    private Map<Term, Double> term2ic;

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
    public PhenixPriority(String scoreDistributionFolder, List<String> hpoQueryTermIds, boolean symmetric) {

        if (hpoQueryTermIds.isEmpty()) {
            throw new PhenixException("Please supply some HPO terms. PhenIX is unable to prioritise genes without these.");
        }

        if (!scoreDistributionFolder.endsWith(File.separatorChar + "")) {
            scoreDistributionFolder += File.separatorChar;
        }
        this.scoredistributionFolder = scoreDistributionFolder;
        String hpoOboFile = String.format("%s%s", scoreDistributionFolder, "hp.obo");
        String hpoAnnotationFile = String.format("%s%s", scoreDistributionFolder, "ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt");
        parseData(hpoOboFile, hpoAnnotationFile);

        Set<Term> hpoQueryTermsHS = new HashSet<>();        
        for (String termIdString : hpoQueryTermIds) {
            Term t = hpo.getTermIncludingAlternatives(termIdString);
            if (t != null) {
                hpoQueryTermsHS.add(t);
            } else {
                logger.error("invalid term-id given: " + termIdString);
            }
        }
        hpoQueryTerms = new ArrayList<>();
        hpoQueryTerms.addAll(hpoQueryTermsHS);
        this.symmetric = symmetric;

        numberQueryTerms = hpoQueryTerms.size();
        if (!scoredistributionContainer.didParseDistributions(symmetric, numberQueryTerms)) {
            scoredistributionContainer.parseDistributions(symmetric, numberQueryTerms, scoreDistributionFolder);
        }

        ResnikSimilarity resnik = new ResnikSimilarity(hpo, (HashMap<Term, Double>) term2ic);
        similarityMeasure = new InformationContentObjectSimilarity(resnik, symmetric, false);
    }

    /**
     * STUB CONSTRUCTOR - ONLY USED FOR TESTING PURPOSES TO AVOID NULL POINTERS FROM ORIGINAL CONSTRUCTOR. DO NOT USE FOR PRODUCTION CODE!!!!
     * @param hpoIds
     * @param symmetric 
     */
    protected PhenixPriority (List<String> hpoIds, boolean symmetric) {
        this.symmetric = symmetric;
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
        geneId2annotations = new HashMap<>();

        BufferedReader in = new BufferedReader(new FileReader(hpoAnnotationFile));
        String line = null;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }

            String[] split = line.split("\t");
            String entrez = split[0];
            Term term = null;
            try {
                /* split[4] is the HPO term field of an annotation line. */
                term = hpo.getTermIncludingAlternatives(split[3]);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get term for line \n{}\n", line);
                logger.error("The offending field was '{}'", split[3]);
                for (int k = 0; k < split.length; ++k) {
                    logger.error("{} '{}'", k, split[k]);
                }
                term = null;
            }
            if (term == null) {
                continue;
            }

            List<Term> annotations;

            if (geneId2annotations.containsKey(entrez)) {
                annotations = geneId2annotations.get(entrez);
            } else {
                annotations = new ArrayList<>();
            }
            annotations.add(term);
            geneId2annotations.put(entrez, annotations);
        }
        in.close();

        // cleanup annotations
        for (String entrez : geneId2annotations.keySet()) {
            List<Term> terms = geneId2annotations.get(entrez);
            Set<Term> uniqueTerms = new HashSet<>(terms);
            List<Term> uniqueTermsAL = new ArrayList<>();
            uniqueTermsAL.addAll(uniqueTerms);
            List<Term> termsMostSpecific = HPOutils.cleanUpAssociation((ArrayList<Term>)uniqueTermsAL, hpoSlim, hpo.getRootTerm());
            geneId2annotations.put(entrez, termsMostSpecific);
        }

        // prepare IC computation
        final Map<Term, Set<String>> annotationTerm2geneIds = new HashMap<>();
        for (String oId : geneId2annotations.keySet()) {
            List<Term> annotations = geneId2annotations.get(oId);
            for (Term annot : annotations) {
                List<Term> termAndAncestors = hpoSlim.getAncestors(annot);
                for (Term term : termAndAncestors) {
                    Set<String> objectsAnnotatedByTerm; 
                    // here we store which objects have been annotated with this term
                    if (annotationTerm2geneIds.containsKey(term)) {
                        objectsAnnotatedByTerm = annotationTerm2geneIds.get(term);
                    } else {
                        objectsAnnotatedByTerm = new HashSet<>();
                    }
                    // add the current object
                    objectsAnnotatedByTerm.add(oId); 
                    annotationTerm2geneIds.put(term, objectsAnnotatedByTerm);
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
     * Flag to output results of filtering against Uberpheno data.
     */
    @Override
    public PriorityType getPriorityType() {
        return PriorityType.PHENIX_PRIORITY;
    }

    /**
     * Prioritize a list of candidate {@link exomizer.exome.Gene Gene} objects
     * (the candidate genes have rare, potentially pathogenic variants).
     *
     * @param genes List of candidate genes.
     * @see exomizer.filter.Filter#filter_list_of_variants(java.util.ArrayList)
     */
    @Override
    public void prioritizeGenes(List<Gene> genes) {
        analysedGenes = genes.size();

        for (Gene gene : genes) {
            PhenixPriorityResult phenomizerRelScore = scoreVariantHPO(gene);
            gene.addPriorityResult(phenomizerRelScore);
            //System.out.println("Phenomizer Gene="+gene.getGeneSymbol()+" score=" +phenomizerRelScore.getScore());
        }
//        String s = String.format("Data investigated in HPO for %d genes. No data for %d genes", analysedGenes, this.offTargetGenes);
        //System.out.println(s);
        normalizePhenomizerScores(genes);
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
    private void normalizePhenomizerScores(List<Gene> genes) {
        if (maxSemSim < 1) {
            return;
        }
        PhenixPriorityResult.setNormalizationFactor(1d / maxSemSim);
        /*for (Gene g : genes) {
         float score = g.getRelevagetScorepe.PHENIX_PRIORITY);
         score /= this.maxSemSim;
         g.setScore(FilterType.PHENIX_PRIORITY, score);
         }*/
    }

    /**
     * @param gene A {@link Gene Gene} whose score is to be
     * determined.
     */
    private PhenixPriorityResult scoreVariantHPO(Gene gene) {

        int entrezGeneId = gene.getEntrezGeneID();
        String entrezGeneIdString = entrezGeneId + "";

        if (!geneId2annotations.containsKey(entrezGeneIdString)) {
            logger.error("INVALID GENE (will set to default-score): Entrez:{} ({})", gene.getEntrezGeneID(), gene.getGeneSymbol());
            this.offTargetGenes++;
            return new PhenixPriorityResult(entrezGeneId, gene.getGeneSymbol(), DEFAULT_SCORE);
        }

        List<Term> annotationsOfGene = geneId2annotations.get(entrezGeneIdString);

        double similarityScore = similarityMeasure.computeObjectSimilarity( (ArrayList<Term>) hpoQueryTerms, (ArrayList<Term>) annotationsOfGene);
        if (similarityScore > maxSemSim) {
            maxSemSim = similarityScore;
        }
        if (Double.isNaN(similarityScore)) {
            errorMessages.add("Error: score was NAN for gene:" + gene + " : " + hpoQueryTerms + " <-> " + annotationsOfGene);
        }

        ScoreDistribution scoreDist = scoredistributionContainer.getDistribution(entrezGeneIdString, numberQueryTerms, symmetric, scoredistributionFolder);

	// get the pvalue
        double negLogPvalue;
        if (scoreDist == null) {
            return new PhenixPriorityResult(entrezGeneId, gene.getGeneSymbol(), DEFAULT_SCORE);
        } else {
            double rawPvalue = scoreDist.getPvalue(similarityScore, 1000.);
            negLogPvalue = Math.log(rawPvalue) * -1.0; /* Negative log of p value : most significant get highest score */
        }

        return new PhenixPriorityResult(entrezGeneId, gene.getGeneSymbol(), negLogPvalue, similarityScore);
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

    private Map<Term, Double> caclulateTermIC(Ontology ontology, Map<Term, Set<String>> term2objectIdsAnnotated) {

        Term root = ontology.getRootTerm();
        Map<Term, Integer> term2frequency = new HashMap<>();
        for (Term t : term2objectIdsAnnotated.keySet()) {
            term2frequency.put(t, term2objectIdsAnnotated.get(t).size());
        }

        int maxFreq = term2frequency.get(root);
        Map<Term, Double> term2informationContent = SimilarityUtilities.caculateInformationContent(maxFreq, (HashMap<Term, Integer>) term2frequency);

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

    private static class PhenixException extends RuntimeException {

        private PhenixException(String message) {
            super(message);
        }
    }

//TODO move this to the messages
//    /**
//     * @return an ul list with summary of phenomizer prioritization.
//     */
//    @Override
//    public String getHTMLCode() {
//        String s = String.format("Phenomizer: %d genes were evaluated; no phenotype data available for %d of them",
//                this.analysedGenes, this.offTargetGenes);
//        String t = null;
//        if (symmetric) {
//            t = String.format("Symmetric Phenomizer query with %d terms was performed", this.numberQueryTerms);
//        } else {
//            t = String.format("Asymmetric Phenomizer query with %d terms was performed", this.numberQueryTerms);
//        }
//        String u = String.format("Maximum semantic similarity score: %.2f, maximum negative log. of p-value: %.2f",
//                this.maxSemSim, this.maxNegLogP);
//        return String.format("<ul><li>%s</li><li>%s</li><li>%s</li></ul>\n", s, t, u);
//
//    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.hpoQueryTerms);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PhenixPriority other = (PhenixPriority) obj;
        if (!Objects.equals(this.hpoQueryTerms, other.hpoQueryTerms)) {
            return false;
        }
        if (this.symmetric != other.symmetric) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PhenixPriority{" + "hpoQueryTerms=" + hpoQueryTerms + '}';
    }

}
