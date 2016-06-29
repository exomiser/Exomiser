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
import de.charite.compbio.exomiser.core.prioritisers.util.UberphenoAnnotationContainer;
import de.charite.compbio.exomiser.core.prioritisers.util.UberphenoIO;
import ontologizer.go.Ontology;
import ontologizer.go.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import similarity.concepts.ResnikSimilarity;
import similarity.objects.InformationContentObjectSimilarity;
import sonumina.math.graph.SlimDirectedGraphView;

import java.io.File;
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
 * @see <a href="http://purl.obolibrary.org/obo/hp/uberpheno/">Uberpheno Hudson
 * page</a>
 * @see <a href="http://f1000research.com/articles/2-30/v1">F1000 research
 * article</a>
 * @author Sebastian Koehler
 * @version 0.05 (April 28, 2013)
 */
public class UberphenoPriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(UberphenoPriority.class);

    /**
     * The Uberpheno as Ontologizer-Ontology object
     */
    private Ontology uberpheno;

    /**
     * The Uberpheno as SlimDirectedGraph (fast access to ancestor etc.)
     */
    private SlimDirectedGraphView<Term> uberphenoSlim;

    /**
     * A list of error-messages
     */
    private List<String> errorMessages = new ArrayList<>();

    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private List<String> messages = new ArrayList<>();

    /**
     * Modificiation date of the Uberpheno obo file
     */
    private long uberphenoOboFileLastModified = -1;

    /**
     * Modification date of the annotation file
     */
    private long uberphenoAnnotationFileLastModified = -1;

    /**
     * Contains the associations between genes and terms from the uberpheno
     */
    private UberphenoAnnotationContainer uberphenoAnnotationContainer;

    /**
     * The semantic similarity measure used to calculate phenotypic similarity
     */
    private InformationContentObjectSimilarity similarityMeasure;

    /**
     * The HPO-terms associated with the current disease
     */
    private List<Term> annotationsOfDisease;

    /**
     * Assignment of an IC to each term in the Uberpheno
     */
    private Map<Term, Double> uberphenoterm2informationContent;

    /**
     * Create a new instance of the UberphenoFilter.
     *
     * @param uberphenoOboFile The uberpheno obo-file obtained from
     * {@code http://purl.obolibrary.org/obo/hp/uberpheno/}
     * @param uberphenoAnnotationFile The annotation file obtained from
     * {@code http://purl.obolibrary.org/obo/hp/uberpheno/}
     * @param disease The disease ID. At the moment only OMIM-IDs allowed.
     * @throws ExomizerInitializationException
     * @see <a href="http://purl.obolibrary.org/obo/hp/uberpheno/">Uberpheno
     * Hudson page</a>
     */
    public UberphenoPriority(String uberphenoOboFile, String uberphenoAnnotationFile, String disease) {

        /* IO responsible for Uberpheno files */
        UberphenoIO uphenoIO = new UberphenoIO();

        boolean reparsed = false; /* will be set to true if we have reparsed at least one of the obo- or annotation-file. */

        /* check if we have to parse the obo-file */
        File uberphenoOboFileObj = new File(uberphenoOboFile);
        if (uberphenoOboFileObj.lastModified() != uberphenoOboFileLastModified) {
            uberpheno = uphenoIO.parseUberpheno(uberphenoOboFile);
            uberphenoSlim = uberpheno.getSlimGraphView();
            uberphenoOboFileLastModified = uberphenoOboFileObj.lastModified();
            reparsed = true;
        }

        /* check if we have to parse the annotation-file */
        File uberphenoAnnotationFileObj = new File(uberphenoAnnotationFile);
        if (uberphenoAnnotationFileObj.lastModified() != uberphenoAnnotationFileLastModified) {
            uberphenoAnnotationContainer = uphenoIO.createUberphenoAnnotationFromFile(uberphenoAnnotationFile, uberpheno, uberphenoSlim);
            uberphenoAnnotationFileLastModified = uberphenoAnnotationFileObj.lastModified();
            reparsed = true;
        }

        /* get the information-content for each term in uberpheno */
        if (reparsed) {
            uberphenoterm2informationContent = uberphenoAnnotationContainer.calculateInformationContentUberpheno(uberpheno, uberphenoSlim);

            /* Similarity calculation ... TODO: parameter-based choice of similarity measure */
            ResnikSimilarity resnik = new ResnikSimilarity(uberpheno, (HashMap) uberphenoterm2informationContent);
            similarityMeasure = new InformationContentObjectSimilarity(resnik, false, false);
        }

        /*
         * Take the given disease ID and find the associated HPO-terms for that disease
         * by converting it to an OMIM-ID-Integer
         */
        int omimId = 0;
        try {
            omimId = Integer.parseInt(disease);
        } catch (NumberFormatException e) {
            logger.error("UberphenoFilter: Could not parse OMIM-id (int) from {}", disease);
        }

        /* Store the annotations of the given disease. This is used as query */
        Set<Term> annotationsOfCurrentDiseaseHs = uberphenoAnnotationContainer.getAnnotationsOfOmim(omimId);
        annotationsOfDisease = new ArrayList<Term>(annotationsOfCurrentDiseaseHs);

        /* some logging stuff */
        this.errorMessages = new ArrayList<String>();
        this.messages = new ArrayList<String>();
    }

    /**
     * Flag to output results of filtering against Uberpheno data.
     */
    @Override
    public PriorityType getPriorityType() {
        return PriorityType.UBERPHENO_PRIORITY;
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     * of score filtering.
     */
    public List<String> getMessages() {
        if (this.errorMessages.size() > 0) {
            for (String s : errorMessages) {
                this.messages.add("Error: " + s);
            }
        }
        return this.messages;
    }

    /**
     * Prioritize a list of candidate {@link Gene Gene} objects
     * (the candidate genes have rare, potentially pathogenic variants).
     *
     * @param gene_list List of candidate genes.
     */
    @Override
    public void prioritizeGenes(List<Gene> gene_list) {
        int analysedGenes = gene_list.size();

        for (Gene gene : gene_list) {
            UberphenoPriorityResult uberphenoRelScore = scoreVariantUberpheno(gene);
            gene.addPriorityResult(uberphenoRelScore);
        }

        String s = String.format("Data investigated in Uberpheno for %d genes (%.1f%%)", analysedGenes);
        messages.add(s);
    }

    /**
     * @param gene A {@link Gene Gene} whose score is to be determined.
     */
    private UberphenoPriorityResult scoreVariantUberpheno(Gene gene) {

        int entrezGeneId = gene.getEntrezGeneID();
        Set<Term> terms = uberphenoAnnotationContainer.getAnnotationsOfGene(entrezGeneId);
        if (terms == null || terms.size() < 1) {
            return new UberphenoPriorityResult(entrezGeneId, gene.getGeneSymbol(), -10.0f);
        }
        List<Term> termsAl = new ArrayList<>(terms);
        double similarityScore = similarityMeasure.computeObjectSimilarity((ArrayList) annotationsOfDisease, (ArrayList) termsAl);
        return new UberphenoPriorityResult(entrezGeneId, gene.getGeneSymbol(), similarityScore);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.uberpheno);
        hash = 29 * hash + Objects.hashCode(this.annotationsOfDisease);
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
        final UberphenoPriority other = (UberphenoPriority) obj;
        if (!Objects.equals(this.uberpheno, other.uberpheno)) {
            return false;
        }
        if (!Objects.equals(this.annotationsOfDisease, other.annotationsOfDisease)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UberphenoPriority{" + '}';
    }
    
}
