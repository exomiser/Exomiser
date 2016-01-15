/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis.util;

import java.util.List;
import java.util.Set;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public interface GeneScorer {
 
    public void scoreGenes(List<Gene> geneList, Set<ModeOfInheritance> modesOfInheritance);
}
