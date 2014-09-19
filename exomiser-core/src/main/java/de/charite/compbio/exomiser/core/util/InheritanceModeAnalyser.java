/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.util;

import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import jannovar.common.ModeOfInheritance;
import jannovar.exome.Variant;
import jannovar.pedigree.Pedigree;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * This class allows us to do segregation analysis for the variants supplied to
 * it i.e., to determine if they are compatible with autosomal recessive,
 * autosomal dominant, or X-linked recessive inheritance.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceModeAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceModeAnalyser.class);

    private final Pedigree pedigree;

    public InheritanceModeAnalyser(Pedigree pedigree) {
        this.pedigree = pedigree;
    }

    /**
     * Analyses the inheritance modes for a gene according to the 
     * @param gene
     * @return a Set of inheritance modes with which the gene is compatible with.
     */
    public Set<ModeOfInheritance> analyseInheritanceModesForGene(Gene gene) {
        return analyseInheritanceModes(gene.getPassedVariantEvaluations());
    }

    /**
     * Caution - this only really works if 
     * @param variantEvaluations
     * @return 
     */
    public Set<ModeOfInheritance> analyseInheritanceModes(List<VariantEvaluation> variantEvaluations) {
        Set inheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);
        
        ArrayList<Variant> variantList = new ArrayList<>();
        
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            variantList.add(variantEvaluation.getVariant());
        }
        inheritanceModes.addAll(analyseInheritanceModes(variantList));
      
        return inheritanceModes;
    }

    public Set<ModeOfInheritance> analyseInheritanceModes(ArrayList<Variant> variantList) {
        Set inheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);

        if (pedigree.isCompatibleWithAutosomalRecessive(variantList)) {
            inheritanceModes.add(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        }
        if (pedigree.isCompatibleWithAutosomalDominant(variantList)) {
            inheritanceModes.add(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        }
        if (pedigree.isCompatibleWithXChromosomalRecessive(variantList)) {
            inheritanceModes.add(ModeOfInheritance.X_RECESSIVE);
        }

        return inheritanceModes;
    }

}
