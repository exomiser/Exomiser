/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.util;

import de.charite.compbio.exomiser.core.Variant;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.CompatibilityCheckerException;
import de.charite.compbio.jannovar.pedigree.Genotype;
import de.charite.compbio.jannovar.pedigree.GenotypeListBuilder;
import de.charite.compbio.jannovar.pedigree.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Pedigree;
import de.charite.compbio.jannovar.pedigree.PedigreeDiseaseCompatibilityDecorator;
import htsjdk.variant.variantcontext.Allele;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

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
        Set<ModeOfInheritance> inheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);
        
        ArrayList<Variant> variantList = new ArrayList<>();
        
        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            variantList.add(variantEvaluation.getVariant());
        }
        inheritanceModes.addAll(analyseInheritanceModes(variantList));
      
        return inheritanceModes;
    }

    public Set<ModeOfInheritance> analyseInheritanceModes(ArrayList<Variant> variantList) {
        Set<ModeOfInheritance> inheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);
        
        PedigreeDiseaseCompatibilityDecorator checker = new PedigreeDiseaseCompatibilityDecorator(pedigree);
        
        // Build list of genotypes from the given variants.
        GenotypeListBuilder builder = new GenotypeListBuilder(null, null, ImmutableList.copyOf(variantList.get(0).vc
                .getSampleNames()));
        for (Variant var : variantList) {
            final int altAlleleID = var.altAlleleID;
            final int numSamples = var.vc.getNSamples();
            ImmutableList.Builder<Genotype> gtBuilder = new ImmutableList.Builder<Genotype>();
            for (int i = 0; i < numSamples; ++i) {
                final List<Allele> alleles = var.vc.getGenotype(i).getAlleles();
                if (alleles.size() != 2) {
                    gtBuilder.add(Genotype.NOT_OBSERVED);
                    continue;
                }
                
                final boolean isAlt0 = alleles.get(0).basesMatch(var.vc.getAlternateAllele(altAlleleID));
                final boolean isAlt1 = alleles.get(1).basesMatch(var.vc.getAlternateAllele(altAlleleID));
                if (!isAlt0 && !isAlt1)
                    gtBuilder.add(Genotype.HOMOZYGOUS_REF);
                else if ((isAlt0 && !isAlt1) || (!isAlt0 && isAlt1))
                    gtBuilder.add(Genotype.HETEROZYGOUS);
                else 
                    gtBuilder.add(Genotype.HOMOZYGOUS_ALT);
            }
            builder.addGenotypes(gtBuilder.build());
        }

        // FIXME: there are more modes of inheritance implemented in Jannovar
        final ImmutableList<ModeOfInheritance> toCheck = ImmutableList.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE,
                ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.X_RECESSIVE);
        for (ModeOfInheritance mode : toCheck)
            try {
                if (checker.isCompatibleWith(builder.build(), mode))
                    inheritanceModes.add(mode);
            } catch (CompatibilityCheckerException e) {
                throw new RuntimeException("Problem in the mode of inheritance checks!", e);
            }

        return inheritanceModes;
    }

}
