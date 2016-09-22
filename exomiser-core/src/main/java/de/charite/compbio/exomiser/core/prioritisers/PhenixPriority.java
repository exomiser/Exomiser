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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

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
     * The semantic similarity measure used to calculate phenotypic similarity
     */
    private InformationContentObjectSimilarity similarityMeasure;
    /**
     * The HPO terms entered by the user describing the individual who is being
     * sequenced by exome-sequencing or clinically relevant genome panel.
     */
    private List<Term> hpoQueryTerms;

    private static final double DEFAULT_SCORE = 0;

    private Map<String, List<Term>> geneId2annotations;

    private boolean symmetric;
    /**
     * Path to the directory that has the files needed to calculate the score
     * distribution.
     */
    private String scoredistributionFolder;

//counters for stats
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
    /**
     * Keeps track of the maximum semantic similarity score to date
     */
    private double maxSemSim = 0d;


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
     * @see <a href="http://purl.obolibrary.org/obo/hp/uberpheno/">Uberpheno
     * Hudson page</a>
     */
    public PhenixPriority(String scoreDistributionFolder, List<String> hpoQueryTermIds, boolean symmetric) {

        if (hpoQueryTermIds.isEmpty()) {
            throw new PhenixException("Please supply some HPO terms. PhenIX is unable to prioritise genes without these.");
        }

        if (!scoreDistributionFolder.endsWith(File.separator)) {
            scoreDistributionFolder += File.separator;
        }
        this.scoredistributionFolder = scoreDistributionFolder;
        this.symmetric = symmetric;

        String hpoOboFile = String.format("%s%s", scoreDistributionFolder, "hp.obo");
        String hpoAnnotationFile = String.format("%s%s", scoreDistributionFolder, "ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt");
        //The phenixData directory must contain the files "hp.obo", "ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt"
        //as well as the score distribution files "*.out", all of which can be downloaded from the HPO hudson server.
        this.hpo = parseOntology(hpoOboFile);
        //The HPO as SlimDirectedGraph (fast access to ancestors etc.)
        SlimDirectedGraphView<Term> hpoSlim = hpo.getSlimGraphView();
        this.geneId2annotations = parseAnnotations(hpoAnnotationFile, hpo, hpoSlim);
        this.similarityMeasure = calculateInformationContentSimilarityMeasures(symmetric, hpo, hpoSlim, geneId2annotations);

        //hpoQueryTerms can probably be calculated for each query and kept local
        hpoQueryTerms = hpoQueryTermIds.stream()
                .map(termIdString -> {
                    Term term = hpo.getTermIncludingAlternatives(termIdString);
                    if (term == null) {
                        logger.error("Unrecognised HPO input term {}. This will not be used in the analysis.", termIdString);
                    }
                    return term;
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        logger.info("Created HPO query terms {}", hpoQueryTerms);
    }

    private InformationContentObjectSimilarity calculateInformationContentSimilarityMeasures(boolean symmetric, Ontology hpo, SlimDirectedGraphView<Term> hpoSlim, Map<String, List<Term>> geneId2annotations) {
        Map<Term, Double> term2ic = calculateTermIC(hpo, hpoSlim, geneId2annotations);
        ResnikSimilarity resnik = new ResnikSimilarity(hpo, (HashMap<Term, Double>) term2ic);
        return new InformationContentObjectSimilarity(resnik, symmetric, false);
    }

    /**
     * STUB CONSTRUCTOR - ONLY USED FOR TESTING PURPOSES TO AVOID NULL POINTERS FROM ORIGINAL CONSTRUCTOR. DO NOT USE FOR PRODUCTION CODE!!!!
     * @param hpoIds
     * @param symmetric 
     */
    protected PhenixPriority (List<String> hpoIds, boolean symmetric) {
        this.symmetric = symmetric;
    }

    /**
     * Parses the human-phenotype-ontology.obo file (or equivalently, the hp.obo
     * file from our Hudosn server).
     *
     * @param hpoOboFile path to the hp.obo file.
     */
    private Ontology parseOntology(String hpoOboFile) {
        OBOParser oboParser = new OBOParser(hpoOboFile, OBOParser.PARSE_XREFS);

        try {
            String parseInfo = oboParser.doParse();
            logger.info(parseInfo);
        } catch (IOException | OBOParserException e) {
            logger.error("Error parsing HPO OBO file", e);
        }

        TermContainer termContainer = new TermContainer(oboParser.getTermMap(), oboParser.getFormatVersion(), oboParser.getDate());
        Ontology hpoOntology = new Ontology(termContainer);
        hpoOntology.setRelevantSubontology(termContainer.get(HPOutils.organAbnormalityRootId).getName());
        return hpoOntology;
    }

    /**
     * Parse the HPO phenotype annotation file (e.g., phenotype_annotation.tab).
     * The point of this is to get the links between diseases and HPO phenotype
     * terms. The hpoAnnotationFile is The
     * ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt-file
     *
     * @param hpoAnnotationFile path to the file
     */
    private Map<String, List<Term>> parseAnnotations(String hpoAnnotationFile, Ontology hpo, SlimDirectedGraphView<Term> hpoSlim) {
        Map<String, List<Term>> geneAnnotations = new HashMap<>();
        logger.info("Parsing Annotations file {}", hpoAnnotationFile);

        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(hpoAnnotationFile))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
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
                    logger.error("", e);
                }
                if (term != null) {
                    geneAnnotations.computeIfAbsent(entrez, annotations -> new ArrayList<>()).add(term);
                }
            }
        } catch (IOException e) {
            logger.error("Error parsing annotation file {}", hpoAnnotationFile, e);
        }

        // cleanup annotations
        for (Map.Entry<String, List<Term>> entry : geneAnnotations.entrySet()) {
            String entrezId = entry.getKey();
            List<Term> uniqueTerms = entry.getValue().stream().distinct().collect(Collectors.toCollection(ArrayList::new));
            List<Term> mostSpecificTerms = HPOutils.cleanUpAssociation((ArrayList<Term>) uniqueTerms, hpoSlim, hpo.getRootTerm());
            geneAnnotations.put(entrezId, mostSpecificTerms);
        }
        logger.info("Made HPO annotations for {} genes", geneAnnotations.size());
        return geneAnnotations;
    }

    private Map<Term, Double> calculateTermIC(Ontology ontology, SlimDirectedGraphView<Term> hpoSlim, Map<String, List<Term>> geneId2annotations) {

        // prepare IC computation
        // here we store which objects have been annotated with this term
        final Map<Term, Set<String>> annotationTerm2geneIds = new HashMap<>();
        for (Map.Entry<String, List<Term>> entry : geneId2annotations.entrySet()) {
            String entrezId = entry.getKey();
            List<Term> annotations = entry.getValue();
            for (Term annot : annotations) {
                List<Term> termAndAncestors = hpoSlim.getAncestors(annot);
                for (Term term : termAndAncestors) {
                    annotationTerm2geneIds.computeIfAbsent(term, objectsAnnotatedByTerm -> new HashSet<>()).add(entrezId);
                }
            }
        }

        Map<Term, Integer> termFrequencies = annotationTerm2geneIds.entrySet().stream()
                .collect(toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

        Term root = ontology.getRootTerm();
        int maxFreq = termFrequencies.get(root);
        double ICzeroCountTerms = -1 * (Math.log(1 / (double) maxFreq));

        Map<Term, Double> term2informationContent = SimilarityUtilities.caculateInformationContent(maxFreq, (HashMap<Term, Integer>) termFrequencies);
        int frequencyZeroCounter = 0;
        for (Term t : ontology) {
            if (!termFrequencies.containsKey(t)) {
                ++frequencyZeroCounter;
                term2informationContent.put(t, ICzeroCountTerms);
            }
        }

        logger.info("WARNING: Frequency of {} terms was zero!! Set IC of these to : {}", frequencyZeroCounter, ICzeroCountTerms);
        return term2informationContent;
    }

    /**
     * Flag to output results of filtering against Uberpheno data.
     */
    @Override
    public PriorityType getPriorityType() {
        return PriorityType.PHENIX_PRIORITY;
    }

    /**
     * Prioritize a list of candidate {@link Gene Gene} objects
     * (the candidate genes have rare, potentially pathogenic variants).
     *
     * @param genes List of candidate genes.
     */
    @Override
    public void prioritizeGenes(List<Gene> genes) {

        ScoreDistributionContainer scoredistributionContainer = new ScoreDistributionContainer(scoredistributionFolder, symmetric, hpoQueryTerms.size());

        Map<Gene, PhenixScore> geneScores = genes.stream().collect(toMap(Function.identity(), scoreGene(hpoQueryTerms, scoredistributionContainer)));

        double maxSemSimScore = geneScores.values().stream().mapToDouble(PhenixScore::getSemanticSimilarityScore).max().orElse(DEFAULT_SCORE);
        double maxNegLogP = geneScores.values().stream().mapToDouble(PhenixScore::getNegativeLogP).max().orElse(DEFAULT_SCORE);
        double normalisationFactor = calculateNormalisationFactor(maxSemSimScore);

        for (Map.Entry<Gene, PhenixScore> entry : geneScores.entrySet()) {
            Gene gene = entry.getKey();
            PhenixScore phenixScore = entry.getValue();
            double score = phenixScore.getSemanticSimilarityScore() * normalisationFactor;
            PriorityResult result = new PhenixPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), score, phenixScore.getSemanticSimilarityScore(), phenixScore.getNegativeLogP());
            gene.addPriorityResult(result);
        }

        logger.info("Data investigated in HPO for {} genes. No data for {} genes", genes.size(), geneId2annotations.keySet().size());
    }

    private Function<Gene, PhenixScore> scoreGene(List<Term> queryTerms, ScoreDistributionContainer scoredistributionContainer) {
        return gene -> {
            int entrezGeneId = gene.getEntrezGeneID();
            String geneIdString = Integer.toString(entrezGeneId);

            if (!geneId2annotations.containsKey(geneIdString)) {
                return new PhenixScore(DEFAULT_SCORE, DEFAULT_SCORE);
            }

            List<Term> geneAnnotations = geneId2annotations.get(geneIdString);
            double semanticSimilarityScore = similarityMeasure.computeObjectSimilarity( (ArrayList<Term>) queryTerms, (ArrayList<Term>) geneAnnotations);

            if (Double.isNaN(semanticSimilarityScore)) {
                logger.error("Score was NaN for geneId: {} : ", entrezGeneId, queryTerms);
            }
            ScoreDistribution scoreDist = scoredistributionContainer.getDistribution(geneIdString);

            double negLogP = calculateNegLogP(semanticSimilarityScore, scoreDist);
            return new PhenixScore(semanticSimilarityScore, negLogP);
        };
    }

    private double calculateNegLogP(double semanticSimilarityScore, ScoreDistribution scoreDist) {
        if (scoreDist == null) {
            return DEFAULT_SCORE;
        } else {
            double rawPvalue = scoreDist.getPvalue(semanticSimilarityScore, 1000d);
            // Negative log of p value : most significant get highest score
            return Math.log(rawPvalue) * -1.0;
        }
    }

    /**
     * The gene relevance scores are to be normalized to lie between zero and
     * one. This function, which relies upon the variable {@link #maxSemSim}
     * being set in {@link #scoreGene}, divides each score by
     * {@link #maxSemSim}, which has the effect of putting the phenomizer scores
     * in the range [0..1]. Note that for now we are using the semantic
     * similarity scores, but we should also try the P value version (TODO).
     * Note that this is not the same as rank normalization!
     */
    private double calculateNormalisationFactor(double maxSemSimScore) {
        if (maxSemSimScore < 1) {
            return 1d;
        }
        return 1d / maxSemSimScore;
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
//        String u = String.format("Maximum semantic similarity score: %.2f, maximum negative log. of p-value: %.2f", this.maxSemSim, this.maxNegLogP);
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

    //Tuple-esq container
    private class PhenixScore {

        private final double semanticSimilarityScore;
        private final double negativeLogP;

        PhenixScore(double semanticSimilarityScore, double negativeLogP) {
            this.semanticSimilarityScore = semanticSimilarityScore;
            this.negativeLogP = negativeLogP;
        }

        double getSemanticSimilarityScore() {
            return semanticSimilarityScore;
        }

        double getNegativeLogP() {
            return negativeLogP;
        }
    }
}
