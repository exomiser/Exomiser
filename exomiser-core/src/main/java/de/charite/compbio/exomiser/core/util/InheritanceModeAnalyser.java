/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.util;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.*;
import de.charite.compbio.jannovar.pedigree.compatibilitychecker.CompatibilityCheckerException;
import htsjdk.variant.variantcontext.Allele;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import htsjdk.variant.variantcontext.VariantContext;
import java.util.Collections;

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

    /**
     * Analyses the inheritance modes for a gene according to the variants which have *PASSED* filtering.
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
        Allele alternateAllele = variantContext.getAlternateAllele(altAlleleID);
        final int numSamples = variantContext.getNSamples();
        ImmutableList.Builder<Genotype> variantGenotypes = new ImmutableList.Builder<>();
        for (int i = 0; i < numSamples; ++i) {
            final String name = people.get(i).getName();
            final List<Allele> alleles = variantContext.getGenotype(name).getAlleles();
            if (alleles.size() != 2) {
                variantGenotypes.add(Genotype.NOT_OBSERVED);
                continue;
            }
            final boolean isAlt0 = alleles.get(0).basesMatch(alternateAllele);
            final boolean isAlt1 = alleles.get(1).basesMatch(alternateAllele);
            if (!isAlt0 && !isAlt1) {
                variantGenotypes.add(Genotype.HOMOZYGOUS_REF);
            } else if ((isAlt0 && !isAlt1) || (!isAlt0 && isAlt1)) {
                variantGenotypes.add(Genotype.HETEROZYGOUS);
            } else {
                variantGenotypes.add(Genotype.HOMOZYGOUS_ALT);
            }
        }
        return variantGenotypes.build();
    }

    private Set<ModeOfInheritance> getCompatibleInheritanceModes(Pedigree pedigree, GenotypeList genotypes) {

        Set<ModeOfInheritance> compatibleInheritanceModes = EnumSet.noneOf(ModeOfInheritance.class);

        // FIXME: there are more modes of inheritance implemented in Jannovar
        final Set<ModeOfInheritance> modesToCheck = EnumSet.of(
                ModeOfInheritance.AUTOSOMAL_RECESSIVE,
                ModeOfInheritance.AUTOSOMAL_DOMINANT,
                ModeOfInheritance.X_RECESSIVE);

        PedigreeDiseaseCompatibilityDecorator checker = new PedigreeDiseaseCompatibilityDecorator(pedigree);
        for (ModeOfInheritance mode : modesToCheck) {
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
