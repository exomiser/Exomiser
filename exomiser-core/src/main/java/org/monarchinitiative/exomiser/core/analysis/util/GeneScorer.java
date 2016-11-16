/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
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

    Consumer<Gene> scoreGene(ModeOfInheritance modeOfInheritance, int probandSampleId);

    default List<Gene> scoreGenes(List<Gene> genes, ModeOfInheritance modeOfInheritance, int probandSampleId) {
        genes.forEach(scoreGene(modeOfInheritance, probandSampleId));
        Collections.sort(genes);
        return genes;
    }
}