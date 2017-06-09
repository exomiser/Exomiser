/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis.util;

import org.monarchinitiative.exomiser.core.model.Gene;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@FunctionalInterface
public interface GeneScorer {

    Consumer<Gene> scoreGene();

    default List<Gene> scoreGenes(List<Gene> genes) {
        genes.forEach(scoreGene());
        Collections.sort(genes);
        return genes;
    }
}