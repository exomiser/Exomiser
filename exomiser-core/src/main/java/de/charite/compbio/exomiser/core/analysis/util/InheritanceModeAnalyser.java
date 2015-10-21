/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.analysis.util;

import com.google.common.collect.ArrayListMultimap;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.pedigree.*;
import de.charite.compbio.jannovar.pedigree.Genotype;
import htsjdk.variant.variantcontext.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import de.charite.compbio.jannovar.pedigree.compatibilitychecker.InheritanceCompatibilityChecker;
import de.charite.compbio.jannovar.pedigree.compatibilitychecker.InheritanceCompatibilityCheckerException;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.stream.Collectors.toSet;

/**
 * This class allows us to do segregation analysis for the variants supplied to
 * it i.e., to determine if they are compatible with autosomal recessive,
 * autosomal dominant, or X-linked recessive inheritance.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class InheritanceModeAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(InheritanceModeAnalyser.class);

    private final ModeOfInheritance modeOfInheritance;
    private final InheritanceCompatibilityChecker inheritanceCompatibilityChecker;

    public InheritanceModeAnalyser(Pedigree pedigree, ModeOfInheritance modeOfInheritance) {
        this.modeOfInheritance = modeOfInheritance;
        inheritanceCompatibilityChecker = new InheritanceCompatibilityChecker.Builder().pedigree(pedigree).addMode(modeOfInheritance).build();
    }

    /**
     * Analyses the compatibility of a list of {@link Gene} with the {@link ModeOfInheritance} used in the constructor
     * of this class according to the observed pattern of inheritance in the {@link Pedigree}. This will only be applied
     * to genes and the variants in the gene which have *PASSED* filtering.
     */
    public void analyseInheritanceModes(Collection<Gene> genes) {
        genes.stream().filter(Gene::passedFilters).forEach(this::analyseInheritanceModes);
    }

    /**
     * Analyses the compatibility of a {@link Gene} with the {@link ModeOfInheritance} used in the constructor
     * of this class according to the observed pattern of inheritance in the {@link Pedigree}. This will only be applied
     * to the variants in the gene which have *PASSED* filtering.
     */
    public boolean analyseInheritanceModes(Gene gene) {
        if (gene.passedFilters()) {
            checkInheritanceCompatibilityOfPassedVariants(gene);
        }
        return gene.isCompatibleWith(modeOfInheritance);
    }

    private void checkInheritanceCompatibilityOfPassedVariants(Gene gene) {
        if (modeOfInheritance == ModeOfInheritance.UNINITIALIZED) {
            return;
        }
        //it is *CRITICAL* that only the PASSED variantEvaluations are taken into account here.
        List<VariantEvaluation> passedVariantEvaluations = gene.getPassedVariantEvaluations();

        Multimap<String, VariantEvaluation> geneVariants = mapVariantEvaluationsToVariantContextString(passedVariantEvaluations);
        List<VariantContext> compatibleVariants = getCompatibleVariantContexts(passedVariantEvaluations);

        if (!compatibleVariants.isEmpty()) {
            logger.debug("Gene {} has {} variants compatible with {}:", gene.getGeneSymbol(), compatibleVariants.size(), modeOfInheritance);
            gene.setInheritanceModes(inheritanceCompatibilityChecker.getInheritanceModes());
            setVariantEvaluationInheritanceModes(geneVariants, compatibleVariants);
        }
    }

    private Multimap<String, VariantEvaluation> mapVariantEvaluationsToVariantContextString(List<VariantEvaluation> passedVariantEvaluations) {
        Multimap<String, VariantEvaluation> geneVariants = ArrayListMultimap.create();
        for (VariantEvaluation variantEvaluation : passedVariantEvaluations) {
            geneVariants.put(toKeyValue(variantEvaluation.getVariantContext()), variantEvaluation);
        }
        return geneVariants;
    }

    /**
     * A {@link VariantContext} cannot be used directly as a key in a Map or put into a Set as it does not override equals or hashCode.
     * Also simply using toString isn't an option as the compatible variants returned from the {@link #inheritanceCompatibilityChecker}
     * are different instances and have had their genotype strings changed. This method solves these problems.
     */
    private String toKeyValue(VariantContext variantContext) {
        return variantContext.toStringWithoutGenotypes();
    }

    private List<VariantContext> getCompatibleVariantContexts(List<VariantEvaluation> passedVariantEvaluations) {
        List<VariantContext> compatibleVariants = new ArrayList<>();
        //This needs to be done using all the variants in the gene in order to be able to check for compound heterozygous variations
        //otherwise it would be simpler to just call this on each variant in turn
        try {
            //Make sure only ONE variantContext is added if there are multiple alleles as there will be one VariantEvaluation per allele.
            //Having multiple copies of a VariantContext might cause problems with the comp het calculations 
            Set<VariantContext> geneVariants = passedVariantEvaluations.stream().map(VariantEvaluation::getVariantContext).collect(toSet());
            compatibleVariants = inheritanceCompatibilityChecker.getCompatibleWith(new ArrayList<>(geneVariants));
        } catch (InheritanceCompatibilityCheckerException ex) {
            logger.error(null, ex);
        }
        return compatibleVariants;
    }

    private void setVariantEvaluationInheritanceModes(Multimap<String, VariantEvaluation> geneVariants, List<VariantContext> compatibleVariants) {
        for (VariantContext variantContext : compatibleVariants) {
            //using toStringWithoutGenotypes as the genotype string gets changed and VariantContext does not override equals or hashcode so this cannot be used as a key
            Collection<VariantEvaluation> variants = geneVariants.get(toKeyValue(variantContext));
            for (VariantEvaluation variant : variants) {
                variant.setInheritanceModes(EnumSet.of(modeOfInheritance));
                logger.debug("{}: {}", variant.getInheritanceModes(), variant);
            }
        }
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
        } else {
            return Genotype.HETEROZYGOUS;
        }
    }

}
