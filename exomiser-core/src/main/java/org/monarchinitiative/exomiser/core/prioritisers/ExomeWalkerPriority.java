/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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
public class ExomeWalkerPriority implements Prioritiser<ExomeWalkerPriorityResult> {

    private static final Logger logger = LoggerFactory.getLogger(ExomeWalkerPriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.EXOMEWALKER_PRIORITY;

    /**
     * A list of messages that can be used to create a display in a HTML page or
     * elsewhere.
     */
    private final List<String> messages = new ArrayList<>();

    /**
     * The random walk matrix object
     */
    private final DataMatrix randomWalkMatrix;

    /**
     * List of the Entrez Gene IDs corresponding to the disease gene family that
     * will be used to prioritize the genes with variants in the exome.
     */
    private final List<Integer> seedGenes;

    /**
     * This is the matrix of similarities between the seeed genes and all genes
     * in the network, i.e., p<sub>infinity</sub>.
     */
    private final FloatMatrix combinedProximityVector;

    /**
     *
     * @param randomWalkMatrix
     * @param entrezSeedGenes
     */
    public ExomeWalkerPriority(DataMatrix randomWalkMatrix, List<Integer> entrezSeedGenes) {
        this.randomWalkMatrix = randomWalkMatrix;
        this.seedGenes = addMatchedGenesToSeedGeneList(this.randomWalkMatrix, entrezSeedGenes);
        this.combinedProximityVector = computeDistanceAllNodesFromStartNodes(this.randomWalkMatrix, this.seedGenes);
    }

    public List<Integer> getSeedGenes() {
        return seedGenes;
    }

    /**
     * Adds the Entrez ids in the list provided to the seedGenes if it is
     * contained in the DataMatrix.
     *
     * @param entrezSeedGenes
     */
    private List<Integer> addMatchedGenesToSeedGeneList(DataMatrix randomWalkMatrix, List<Integer> entrezSeedGenes) {
        List<Integer> matchedGeneIdentifiers = new ArrayList<>();
        for (Integer entrezId : entrezSeedGenes) {
            if (randomWalkMatrix.containsGene(entrezId)) {
                matchedGeneIdentifiers.add(entrezId);
            } else {
                logger.warn("Cannot use entrez-id {} as seed gene as it is not present in the DataMatrix provided.", entrezId);
            }
        }

        if (matchedGeneIdentifiers.isEmpty()) {
            logger.error("Could not find any of the given genes in random-walk matrix. You gave entrez ids: {}", entrezSeedGenes);
        }
        return matchedGeneIdentifiers;
    }

    /**
     * Flag to output results of filtering against Genewanderer.
     */
    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    /**
     * Compute the distance of all genes in the Random Walk matrix to the set of
     * seed genes given by the user.
     *
     * @param randomWalkMatrix
     * @param seedGenes
     */
    private FloatMatrix computeDistanceAllNodesFromStartNodes(DataMatrix randomWalkMatrix, List<Integer> seedGenes) {
        FloatMatrix seedGeneProximityVectors = FloatMatrix.EMPTY;
        boolean first = true;
        for (Integer seedGeneEntrezId : seedGenes) {
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
                seedGeneProximityVectors = column;
                first = false;
            } else {
                seedGeneProximityVectors = seedGeneProximityVectors.add(column);
            }
        }
        return seedGeneProximityVectors;
    }

    @Override
    public Stream<ExomeWalkerPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {
        if (seedGenes.isEmpty()) {
            logger.error("Seed genes is empty - please specify a valid list of known genes!");
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
        if (seedGenes.isEmpty()) {
            logger.error("Seed genes is empty - please specify a valid list of known genes!");
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
        hash = 37 * hash + Objects.hashCode(this.PRIORITY_TYPE);
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
        if (this.PRIORITY_TYPE != other.PRIORITY_TYPE) {
            return false;
        }
        return Objects.equals(this.seedGenes, other.seedGenes);
    }

    @Override
    public String toString() {
        return "ExomeWalkerPriority{" + "seedGenes=" + seedGenes + '}';
    }
  
}
