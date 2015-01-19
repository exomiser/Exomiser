package de.charite.compbio.exomiser.core.prioritisers;

import de.charite.compbio.exomiser.core.prioritisers.util.UberphenoIO;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import ontologizer.go.Ontology;
import ontologizer.go.Term;
import similarity.concepts.ResnikSimilarity;
import similarity.objects.InformationContentObjectSimilarity;
import sonumina.math.graph.SlimDirectedGraphView;

import jannovar.common.Constants;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.prioritisers.util.UberphenoAnnotationContainer;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class UberphenoPriority implements Priority {

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
    private List<String> error_record = null;

    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private List<String> messages = null;

    /**
     * Keeps track of the number of variants for which data was available in
     * Uberpheno annotation.
     */
    private int found_annotation_in_uberpheno;

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
    private ArrayList<Term> annotationsOfDisease;

    /**
     * Assignment of an IC to each term in the Uberpheno
     */
    private HashMap<Term, Double> uberphenoterm2informationContent;

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
            ResnikSimilarity resnik = new ResnikSimilarity(uberpheno, uberphenoterm2informationContent);
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
        this.error_record = new ArrayList<String>();
        this.messages = new ArrayList<String>();
    }

    /* (non-Javadoc)
     * @see exomizer.priority.FilterType#getPriorityName()
     */
    @Override
    public String getPriorityName() {
        return "Uberpheno semantic similarity filter";
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
        this.found_annotation_in_uberpheno = 0;
        int analysedGenes = gene_list.size();

        for (Gene gene : gene_list) {
            try {
                UberphenoPriorityResult uberphenoRelScore = scoreVariantUberpheno(gene);
                gene.addPriorityResult(uberphenoRelScore);
            } catch (Exception e) {
                error_record.add(e.toString());
            }
        }

        String s
                = String.format("Data investigated in Uberpheno for %d genes (%.1f%%)",
                        analysedGenes);
        this.messages.add(s);
    }

    /**
     * @param g A {@link exomizer.exome.Gene Gene} whose score is to be
     * determined.
     */
    private UberphenoPriorityResult scoreVariantUberpheno(Gene g) {

        int entrezGeneId = g.getEntrezGeneID();
        Set<Term> terms = uberphenoAnnotationContainer.getAnnotationsOfGene(entrezGeneId);
        if (terms == null || terms.size() < 1) {
            return new UberphenoPriorityResult(Constants.UNINITIALIZED_FLOAT);
        }
        ArrayList<Term> termsAl = new ArrayList<Term>(terms);
        double similarityScore = similarityMeasure.computeObjectSimilarity(annotationsOfDisease, termsAl);
        return new UberphenoPriorityResult(similarityScore);
    }

    /**
     * To do
     *
     * @return
     */
    @Override
    public boolean displayInHTML() {
        return false;
    }

    @Override
    public String getHTMLCode() {
        return "";
    }

}
