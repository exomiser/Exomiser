/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers;

import org.jblas.FloatMatrix;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrix;
import org.monarchinitiative.exomiser.core.prioritisers.util.DataMatrixIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Filter genes according to the random walk proximity in the protein-protein
 * interaction network.
 * <P>
 * The files required for the constructor of this filter should be downloaded
 * from:
 * http://compbio.charite.de/hudson/job/randomWalkMatrix/lastSuccessfulBuild/artifact/
 * <P>
 * This class coordinates random walk analysis as described in the paper
 * <a hred="http://www.ncbi.nlm.nih.gov/pubmed/18371930">
 * Walking the interactome for prioritization of candidate disease genes</a>.
 *
 * @see <a
 * href="http://compbio.charite.de/hudson/job/randomWalkMatrix/">RandomWalk
 * Hudson page</a>
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 * @version 0.09 (3 November, 2013)
 */
public class ExomeWalkerPriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(ExomeWalkerPriority.class);

    private final PriorityType priorityType = PriorityType.EXOMEWALKER_PRIORITY;

    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private List<String> messages = new ArrayList<>();

    /**
     * The random walk matrix object
     */
    private DataMatrix randomWalkMatrix;

    /**
     * List of the Entrez Gene IDs corresponding to the disease gene family that
     * will be used to prioritize the genes with variants in the exome.
     */
    private List<Integer> seedGenes;

    /**
     * This is the matrix of similarities between the seeed genes and all genes
     * in the network, i.e., p<sub>infinity</sub>.
     */
    private FloatMatrix combinedProximityVector;

    /**
     * Create a new instance of the {@link ExomeWalkerPriority}.
     *
     * Assumes the list of seed genes (Entrez gene IDs) has been set!! This
     * happens with the method {@link #setParameters}.
     *
     * @param randomWalkMatrixFileZip The zipped(!) RandomWalk matrix file.
     * @param randomWalkGeneId2IndexFileZip The zipped(!) file with the mapping
     * between Entrez-Ids and Matrix-Indices.
     * @see <a
     * href="http://compbio.charite.de/hudson/job/randomWalkMatrix/">Uberpheno
     * Hudson page</a>
     */
    public ExomeWalkerPriority(String randomWalkMatrixFileZip, String randomWalkGeneId2IndexFileZip) {

        if (randomWalkMatrix == null) {
            try {
                randomWalkMatrix = DataMatrixIO.loadDataMatrix(randomWalkMatrixFileZip, randomWalkGeneId2IndexFileZip, true);
            } catch (Exception e) {
                /* This exception is thrown if the files for the random walk cannot be found. */
                logger.error("Unable to initialize the random walk matrix", e);
            }
        }
    }

    /**
     *
     * @param randomWalkMatrix
     * @param entrezSeedGenes
     */
    public ExomeWalkerPriority(DataMatrix randomWalkMatrix, List<Integer> entrezSeedGenes) {

        this.randomWalkMatrix = randomWalkMatrix;
        seedGenes = new ArrayList<>();
        addMatchedGenesToSeedGeneList(entrezSeedGenes);
        computeDistanceAllNodesFromStartNodes();
    }

    /**
     * Adds the Entrez ids in the list provided to the seedGenes if it is
     * contained in the DataMatrix.
     *
     * @param entrezSeedGenes
     */
    private void addMatchedGenesToSeedGeneList(List<Integer> entrezSeedGenes) {
        for (Integer entrezId : entrezSeedGenes) {

            if (randomWalkMatrix.containsGene(entrezId)) {
                seedGenes.add(entrezId);
            } else {
                logger.warn("Cannot use entrez-id {} as seed gene as it is not present in the DataMatrix provided.", entrezId);
            }
        }

        if (this.seedGenes.isEmpty()) {
            logger.error("Could not find any of the given genes in random-walk matrix. You gave: {}", entrezSeedGenes);
        }
    }

    /**
     * Flag to output results of filtering against Genewanderer.
     */
    @Override
    public PriorityType getPriorityType() {
        return priorityType;
    }

    /**
     * Compute the distance of all genes in the Random Walk matrix to the set of
     * seed genes given by the user.
     */
    private void computeDistanceAllNodesFromStartNodes() {
        boolean first = true;
//        FloatMatrix combinedProximityVector = null;
        for (Integer seedGeneEntrezId : seedGenes) {
            //shouldn't happed as we've already thrown this in the constructor
//	    if (this.randomWalkMatrix == null) {
//		String e = "[GeneWanderer.java] Error: randomWalkMatrix is null";
//		throw new ExomizerInitializationException(e);
//	    }
//	    if (this.randomWalkMatrix.objectid2idx == null) {
//		String e = "[GeneWanderer.java] Error: randomWalkMatrix.object2idx is null";
//		throw new ExomizerInitializationException(e);
//	    }
            if (!randomWalkMatrix.containsGene(seedGeneEntrezId)) {
                /* Note that the RW matrix does not have an entry for every
                 Entrez Gene. If the gene is not contained in the matrix, we
                 skip it. The gene will be given a (low) default score in 
                 Genewanderer Relevance.
                 */
                continue;
            }
            //Get the column we need, this has the distances of ALL genes to the current gene
            FloatMatrix column = randomWalkMatrix.getColumnMatrixForGene(seedGeneEntrezId);

            // for the first column/known gene we have to init the resulting vector
            if (first) {
                combinedProximityVector = column;
                first = false;
            } else {
                combinedProximityVector = combinedProximityVector.add(column);
            }
        }
        /* p_{\infty} */
//        this.combinedProximityVector = combinedProximityVector;
    }

    @Override
    public Stream<ExomeWalkerPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {
        if (seedGenes == null || seedGenes.isEmpty()) {
            throw new RuntimeException("Please specify a valid list of known genes!");
        }
        return genes.stream().map(prioritiseGene());
    }

    private Function<Gene, ExomeWalkerPriorityResult> prioritiseGene() {
        return gene -> {
            double score = calculateGeneScore(gene.getEntrezGeneID());
            return new ExomeWalkerPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), score);
        };
    }

    /**
     * Prioritize a list of candidate {@link Gene Gene} objects
     * (the candidate genes have rare, potentially pathogenic variants).
     *
     * @param geneList List of candidate genes.
     */
    @Override
    public void prioritizeGenes(List<String> hpoIds, List<Gene> geneList) {
        if (seedGenes == null || seedGenes.isEmpty()) {
            throw new RuntimeException("Please specify a valid list of known genes!");
        }

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (Gene gene : geneList) {
            ExomeWalkerPriorityResult relScore = prioritiseGene().apply(gene);
            double score = relScore.getScore();
            if (score > max) {
                max = score;
            }
            if (score < min) {
                min = score;
            }
            gene.addPriorityResult(relScore);
        }


//        float factor = 1f / (float) max;
//        float factorMaxPossible = 1f / (float) combinedProximityVector.max();
//
//        for (Gene gene : geneList) {
//            float scr = gene.getPriorityResult(EXOMEWALKER_PRIORITY);
//            float newscore = factor * (scr - (float) min);
//            gene.resetPriorityScore(EXOMEWALKER_PRIORITY, newscore);
//            newscore = factorMaxPossible * (scr - (float) min);
//            gene.resetPriorityScore(EXOMEWALKER_PRIORITY, newscore);
//        }
        
        //TODO: move this into a report if required 
//        String s = String.format("Protein-Protein Interaction Data was available for %d of %d genes (%.1f%%)",
//                PPIdataAvailable, totalGenes, 100f * ((float) PPIdataAvailable / (float) totalGenes));
//        this.messages.add(s);
//        StringBuilder sb = new StringBuilder();
//        sb.append("Seed genes:");
//        for (Integer seed : seedGenes) {
//            sb.append(seed + "&nbsp;");
//        }
//        this.messages.add(sb.toString());
    }

    private double calculateGeneScore(int entrezId) {
        if (randomWalkMatrix.containsGene(entrezId)) {
            return computeSimStartNodesToNode(entrezId);
        } else {
            return 0;
        }
    }

    /**
     * @return list of messages representing process, result, and if any, errors
     * of score filtering.
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * This function retrieves the random walk similarity score for the gene
     *
     * @param nodeToCompute Gene Id for which the RW score is to bee retrieved
     */
    private double computeSimStartNodesToNode(int nodeToCompute) {
        int idx = randomWalkMatrix.getRowIndexForGene(nodeToCompute);
        return combinedProximityVector.get(idx, 0);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.priorityType);
        hash = 37 * hash + Objects.hashCode(this.seedGenes);
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
        final ExomeWalkerPriority other = (ExomeWalkerPriority) obj;
        if (this.priorityType != other.priorityType) {
            return false;
        }
        return Objects.equals(this.seedGenes, other.seedGenes);
    }

    @Override
    public String toString() {
        return "ExomeWalkerPriority{" + "seedGenes=" + seedGenes + '}';
    }
  
}
