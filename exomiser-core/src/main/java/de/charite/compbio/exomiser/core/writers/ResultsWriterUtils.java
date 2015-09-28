/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.filters.FilterReport;
import de.charite.compbio.exomiser.core.filters.FilterReportFactory;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.annotation.VariantEffect;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import de.charite.compbio.exomiser.core.Analysis;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Map;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResultsWriterUtils {

    private static final Logger logger = LoggerFactory.getLogger(ResultsWriterUtils.class);

    private static final FilterReportFactory filterReportFactory = new FilterReportFactory();

    private static final String DEFAULT_OUTPUT_DIR = "results";
    
    /**
     * Determines the correct file extension for a file given what was specified
     * in the {@link de.charite.compbio.exomiser.core.ExomiserSettings}.
     *
     * @param vcfPath
     * @param outputPrefix
     * @param outputFormat
     * @return
     */
    public static String makeOutputFilename(Path vcfPath, String outputPrefix, OutputFormat outputFormat) {
        if (outputPrefix.isEmpty()) {
            String defaultOutputPrefix = String.format("%s/%s-exomiser-results", ResultsWriterUtils.DEFAULT_OUTPUT_DIR, vcfPath.getFileName());
            logger.debug("Output prefix was unspecified. Will write out to: {}", defaultOutputPrefix);
            outputPrefix = defaultOutputPrefix;
        }
        return String.format("%s.%s", outputPrefix, outputFormat.getFileExtension());
    }

    /**
     * Make a {@code VariantTypeCounter} object from the list of
     * {@code VariantEvaluation}. We use this to print out a table of variant
     * class distribution.
     *
     * @param variantEvaluations
     * @return
     */
    public static List<VariantEffectCount> makeVariantEffectCounters(List<VariantEvaluation> variantEvaluations) {

        // all used Jannovar VariantEffects
        final Set<VariantEffect> variantEffects = ImmutableSet.of(VariantEffect.FRAMESHIFT_ELONGATION,
                VariantEffect.FRAMESHIFT_TRUNCATION, VariantEffect.FRAMESHIFT_VARIANT,
                VariantEffect.INTERNAL_FEATURE_ELONGATION, VariantEffect.FEATURE_TRUNCATION, VariantEffect.MNV,
                VariantEffect.STOP_GAINED, VariantEffect.STOP_LOST, VariantEffect.START_LOST,
                VariantEffect.SPLICE_ACCEPTOR_VARIANT, VariantEffect.SPLICE_DONOR_VARIANT,
                VariantEffect.MISSENSE_VARIANT, VariantEffect.INFRAME_INSERTION,
                VariantEffect.DISRUPTIVE_INFRAME_INSERTION, VariantEffect.INFRAME_DELETION,
                VariantEffect.DISRUPTIVE_INFRAME_DELETION, VariantEffect.THREE_PRIME_UTR_TRUNCATION,
                VariantEffect.SPLICE_REGION_VARIANT, VariantEffect.STOP_RETAINED_VARIANT,
                VariantEffect.INITIATOR_CODON_VARIANT, VariantEffect.SYNONYMOUS_VARIANT,
                VariantEffect.FIVE_PRIME_UTR_VARIANT, VariantEffect.THREE_PRIME_UTR_VARIANT,
                VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT, VariantEffect.NON_CODING_TRANSCRIPT_EXON_VARIANT,
                VariantEffect.NON_CODING_TRANSCRIPT_INTRON_VARIANT, VariantEffect.UPSTREAM_GENE_VARIANT,
                VariantEffect.DOWNSTREAM_GENE_VARIANT, VariantEffect.INTERGENIC_VARIANT);

        VariantEffectCounter variantTypeCounter = makeVariantEffectCounter(variantEvaluations);
        final List<Map<VariantEffect, Integer>> freqMaps = variantTypeCounter.getFrequencyMap(variantEffects);

        int numIndividuals = 0;
        if (!variantEvaluations.isEmpty()) {
            numIndividuals = variantEvaluations.get(0).getNumberOfIndividuals();
        }

        List<VariantEffectCount> result = new ArrayList<>();
        Set<VariantEffect> effects = EnumSet.noneOf(VariantEffect.class);
        for (int sampleIdx = 0; sampleIdx < numIndividuals; ++sampleIdx) {
            effects.addAll(freqMaps.get(sampleIdx).keySet());
        }
        if (variantEvaluations.isEmpty()) {
            effects.addAll(variantEffects);
        }

        for (VariantEffect effect : effects) {
            List<Integer> typeSpecificCounts = new ArrayList<>();
            for (int sampleIdx = 0; sampleIdx < numIndividuals; ++sampleIdx) {
                typeSpecificCounts.add(freqMaps.get(sampleIdx).get(effect));
            }
            result.add(new VariantEffectCount(effect, typeSpecificCounts));
        }

        return result;
    }

    protected static VariantEffectCounter makeVariantEffectCounter(List<VariantEvaluation> variantEvaluations) {
        if (variantEvaluations.isEmpty()) {
            return new VariantEffectCounter(0);
        }

        int numIndividuals = variantEvaluations.get(0).getNumberOfIndividuals();
        VariantEffectCounter effectCounter = new VariantEffectCounter(numIndividuals);

        for (VariantEvaluation variantEvaluation : variantEvaluations) {
            effectCounter.put(variantEvaluation);
        }
        return effectCounter;
    }

    public static List<FilterReport> makeFilterReports(Analysis analysis) {
        return filterReportFactory.makeFilterReports(analysis);
    }

    public static List<Gene> getMaxPassedGenes(List<Gene> genes, int maxGenes) {
        List<Gene> passedGenes = getPassedGenes(genes);
        if (maxGenes == 0) {
            logger.info("Maximum gene limit set to {} - Returning all {} genes which have passed filtering.", maxGenes, passedGenes.size());
            return passedGenes;
        }
        return getMaxGenes(passedGenes, maxGenes);
    }

    public static List<Gene> getPassedGenes(List<Gene> genes) {
        List<Gene> passedGenes = new ArrayList<>();
        for (Gene gene : genes) {
            if (gene.passedFilters()) {
                passedGenes.add(gene);
            }
        }
        logger.info("{} of {} genes passed filters", passedGenes.size(), genes.size());
        return passedGenes;
    }

    private static List<Gene> getMaxGenes(List<Gene> genes, int maxGenes) {
        List<Gene> passedGenes = new ArrayList<>();
        int genesShown = 0;
        for (Gene gene : genes) {
            if (genesShown < maxGenes) {
                passedGenes.add(gene);
                genesShown++;
            }
        }
        logger.info("Maximum gene limit set to {} - Returning first {} of {} genes which have passed filtering.", maxGenes, maxGenes, genes.size());
        return passedGenes;
    }

}
