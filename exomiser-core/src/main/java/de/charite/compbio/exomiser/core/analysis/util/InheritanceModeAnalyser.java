/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.*;
import de.charite.compbio.jannovar.pedigree.Genotype;
import de.charite.compbio.jannovar.pedigree.compatibilitychecker.CompatibilityCheckerException;
import htsjdk.variant.variantcontext.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import java.util.Collections;

/**
 * This class allows us to do segregation analysis for the variants supplied to
 * it i.e., to determine if they are compatible with autosomal recessive,
 * autosomal dominant, or X-linked recessive inheritance.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceModeAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceModeAnalyser.class);
    
    // FIXME: there are more modes of inheritance implemented in Jannovar - ModeOfInheritance.values() should suffice.
    private static final Set<ModeOfInheritance> MODES_TO_CHECK = EnumSet.of(
            ModeOfInheritance.AUTOSOMAL_RECESSIVE,
            ModeOfInheritance.AUTOSOMAL_DOMINANT,
            ModeOfInheritance.X_RECESSIVE); 

    /**
     * Analyses the inheritance modes for a gene according to the variants which have *PASSED* filtering.
     *
     * @param gene
     * @param pedigree
     * @return a Set of inheritance modes with which the gene is compatible with.
     */
    public Set<ModeOfInheritance> analyseInheritanceModes(Gene gene, Pedigree pedigree) {
        return analyseInheritanceModes(gene.getPassedVariantEvaluations(), pedigree);
    }

    /**
     * Analyses the inheritance modes for the variants from a gene.
     *
     * @param variants
     * @return
     */
    private Set<ModeOfInheritance> analyseInheritanceModes(List<VariantEvaluation> variants, Pedigree pedigree) {

        if (variants.isEmpty()) {
            return Collections.emptySet();
        }

        Variant firstVariant = variants.get(0);
        // Build list of genotypes from the given variants.
        String geneSymbol = firstVariant.getGeneSymbol();
        // Use interval of transcript of first region, only used for the chromosome information anyway.
        GenotypeListBuilder genotypeListBuilder = new GenotypeListBuilder(geneSymbol, pedigree.getNames(), firstVariant.isXChromosomal());
        List<Person> people = pedigree.getMembers();
        for (VariantEvaluation variant : variants) {
            ImmutableList<Genotype> variantGenotypes = getVariantGenotypes(people, variant);
            genotypeListBuilder.addGenotypes(variantGenotypes);
        }
        GenotypeList genotypes = genotypeListBuilder.build();

        return getCompatibleInheritanceModes(pedigree, genotypes);
    }

    private ImmutableList<Genotype> getVariantGenotypes(List<Person> people, VariantEvaluation variant) {

        VariantContext variantContext = variant.getVariantContext();
        final int altAlleleID = variant.getAltAlleleId();
        final Allele alternateAllele = variantContext.getAlternateAllele(altAlleleID);
        final int numSamples = variantContext.getNSamples();
        ImmutableList.Builder<Genotype> variantGenotypes = new ImmutableList.Builder<>();
        
        for (int i = 0; i < numSamples; ++i) {
            final String name = people.get(i).getName();
            final List<Allele> alleles = variantContext.getGenotype(name).getAlleles();
            Genotype genotype = getIndividualGenotype(alternateAllele, alleles);
            variantGenotypes.add(genotype);
        }
        return variantGenotypes.build();

        //not sure why this C-style loop is required - could there be more people in the Pedigree than the sample?
        //If the order of people in the pedigree is different to the sample then this is FUBARed
        //in fact, do we even need the people? wouldn't this work:
//        List<Genotype> variantGenotypes = variantContext.getGenotypesOrderedByName().stream()
//                .map(genotype -> {
//                    final List<Allele> alleles = genotype.getAlleles();
//                    return getIndividualGenotype(alternateAllele, alleles);
//                })
//                .collect(toList());
//        return new ImmutableList.Builder<Genotype>().addAll(variantGenotypes).build();
    }

    private Genotype getIndividualGenotype(Allele alternateAllele, List<Allele> alleles) {
        if (alleles.size() != 2) {
            return Genotype.NOT_OBSERVED;
        }
        Allele allele0 = alleles.get(0);
        Allele allele1 = alleles.get(1);
        if (allele0.isNoCall() || allele1.isNoCall()) {
            return Genotype.NOT_OBSERVED;
        }

        final boolean isAlt0 = allele0.basesMatch(alternateAllele);
        final boolean isAlt1 = allele1.basesMatch(alternateAllele);
        if (isAlt0 && isAlt1) {
            return Genotype.HOMOZYGOUS_ALT;
        } else if (!isAlt0 && !isAlt1) {
            return Genotype.HOMOZYGOUS_REF;
        }  else {
            return Genotype.HETEROZYGOUS;
        }
    }

    private Set<ModeOfInheritance> getCompatibleInheritanceModes(Pedigree pedigree, GenotypeList genotypes) {

        Set<ModeOfInheritance> compatibleInheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);

        PedigreeDiseaseCompatibilityDecorator checker = new PedigreeDiseaseCompatibilityDecorator(pedigree);
        for (ModeOfInheritance mode : MODES_TO_CHECK) {
            try {
                if (checker.isCompatibleWith(genotypes, mode)) {
                    compatibleInheritanceModes.add(mode);
                }
            } catch (CompatibilityCheckerException e) {
                throw new RuntimeException("Problem in the mode of inheritance checks!", e);
            }
        }

        return compatibleInheritanceModes;
    }

}
