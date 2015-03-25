package de.charite.compbio.exomiser.core.prioritisers.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import ontologizer.go.Ontology;
import ontologizer.go.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import similarity.SimilarityUtilities;
import sonumina.math.graph.SlimDirectedGraphView;

/**
 *
 * Container
 *
 * @author sebastiankohler
 *
 */
public class UberphenoAnnotationContainer {

    private static final Logger logger = LoggerFactory.getLogger(UberphenoAnnotationContainer.class);
    
    public final Map<Integer, HashSet<UberphenoAnnotation>> container = new HashMap<>();

    /**
     * @param entrezId
     * @param omimIdsToHandle
     * @param isToExclude
     * @return Set of annotations
     */
    private Set<Term> getAnnotationsFiltered(int entrezId, Set<Integer> omimIdsToHandle, boolean isToExclude) {

        Set<Term> annots = new HashSet<>();

        for (UberphenoAnnotation an : container.get(entrezId)) {
            /*
             * If we have to exclude links stemming from 
             * specific omim-entries we check that here
             */
            boolean annotationHasEvidenceMatching = false;
            for (int omimEvidence : an.getEvidenceOmimIds()) { // iterate all omim evidences for that annotation
                if (omimIdsToHandle.contains(omimEvidence)) { // is this an omim-entry that forbids to use this annotation?
                    annotationHasEvidenceMatching = true;
                    break;
                }
            }

            if (isToExclude) {
                //				if (annotationHasvidenceMatching){
                if (an.getTerm().getID().getPrefix().toString().equals("HP")) {
                    continue;
                }
            } else if (!isToExclude) {
                if (!annotationHasEvidenceMatching) {
                    continue;
                }
            }
            annots.add(an.getTerm());
        }
        return annots;
    }

    /**
     * @param entrezId
     * @param omimLinksToInclude
     * @return Set of annotations
     */
    public Set<Term> getAnnotationsOfGeneOnlyFromOmims(int entrezId, Set<Integer> omimLinksToInclude) {
        if (!container.containsKey(entrezId)) {
            return null;
        }
        return getAnnotationsFiltered(entrezId, omimLinksToInclude, false);
    }

    /**
     * @param entrezId
     * @param omimLinksToExclude
     * @return Set of annotations
     */
    public Set<Term> getAnnotationsOfGeneExcludingOmims(int entrezId, Set<Integer> omimLinksToExclude) {
        if (!container.containsKey(entrezId)) {
            return null;
        }
        return getAnnotationsFiltered(entrezId, omimLinksToExclude, true);
    }

    /**
     * @param omimId
     * @return Set of annotations
     */
    public Set<Term> getAnnotationsOfOmim(int omimId) {

        Set<Term> annotations = new HashSet<>();
        for (int entrezId : container.keySet()) {
            for (UberphenoAnnotation annotation : container.get(entrezId)) {
                if (annotation.getEvidenceOmimIds().contains(omimId)) {
                    annotations.add(annotation.getTerm());
                }
            }
        }
        return annotations;
    }

    /**
     * @param entrezId
     * @return Set of annotations
     */
    public Set<Term> getAnnotationsOfGene(int entrezId) {
        if (!container.containsKey(entrezId)) {
            return null;
        }

        Set<Term> annots = new HashSet<>();
        for (UberphenoAnnotation an : container.get(entrezId)) {
            annots.add(an.getTerm());
        }
        return annots;
    }

    /**
     * @param symbol
     * @return an NCBI Entrez ID (integer) corresponding to the Gene Symbol
     */
    public int getEntrezIdFromSymbol(String symbol) {
        for (int e : container.keySet()) {
            for (UberphenoAnnotation an : container.get(e)) {
                if (an.getGeneSymbol().equalsIgnoreCase(symbol)) {
                    return e;
                }
            }
        }
        return -1;
    }

    /**
     * @param entrez
     * @return a Gene symbol corresponding to the entrez ID or null if entrez id
     * cannot be found.
     */
    public String getSymbolFromEntrez(int entrez) {
        for (UberphenoAnnotation an : container.get(entrez)) {
            return an.getGeneSymbol();
        }
        return null;
    }

    /**
     * @param uberpheno
     * @param uberphenoSlim
     */
    public Map<Term, Double> calculateInformationContentUberpheno(Ontology uberpheno, SlimDirectedGraphView<Term> uberphenoSlim) {

        Map<Term, Set<Integer>> term2geneids = new HashMap<>();

        for (int geneId : container.keySet()) {
            Set<Term> annotations = getAnnotationsOfGene(geneId);
            for (Term annotated : annotations) {

                if (!uberphenoSlim.vertex2Index.containsKey(annotated)) {
                    annotated = uberpheno.getTermIncludingAlternatives(annotated.getIDAsString());
                }

                for (Term annotatedAndInduced : uberphenoSlim.getAncestors(annotated)) {

                    Set<Integer> genesForTerm;
                    if (term2geneids.containsKey(annotatedAndInduced)) {
                        genesForTerm = term2geneids.get(annotatedAndInduced);
                    } else {
                        genesForTerm = new HashSet<>();
                    }

                    genesForTerm.add(geneId);
                    term2geneids.put(annotatedAndInduced, genesForTerm);
                }
            }
        }

        return calculateTermIC(term2geneids, uberpheno);
    }

    private Map<Term, Double> calculateTermIC(Map<Term, Set<Integer>> term2objectIds, Ontology ontology) {

        Term root = ontology.getRootTerm();
        Map<Term, Integer> term2frequency = new HashMap<>();
        for (Term t : term2objectIds.keySet()) {
            term2frequency.put(t, term2objectIds.get(t).size());
        }
        int maxFreq = term2frequency.get(root);
        Map<Term, Double> term2informationContent = SimilarityUtilities.caculateInformationContent(maxFreq, (HashMap) term2frequency);

        int frequencyZeroCounter = 0;
        double ICzeroCountTerms = -1 * (Math.log(1 / (double) maxFreq));

        for (Term t : ontology) {
            if (!term2frequency.containsKey(t)) {
                ++frequencyZeroCounter;
                term2informationContent.put(t, ICzeroCountTerms);
            }
        }

        logger.info("WARNING: Frequency of " + frequencyZeroCounter + " terms was zero!! Calculated by -1 * (Math.log(1/(double)maxFreq)) = -1 * (Math.log(1/(double)" + maxFreq + ")))");
        logger.info("Set IC of these to : " + ICzeroCountTerms);
        return term2informationContent;
    }

}
