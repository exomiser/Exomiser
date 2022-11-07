/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.writers;

import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterReport;
import org.monarchinitiative.exomiser.core.filters.FilterReportFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ResultsWriterUtils {

    private static final Logger logger = LoggerFactory.getLogger(ResultsWriterUtils.class);

    private static final FilterReportFactory filterReportFactory = new FilterReportFactory();

    private static final String DEFAULT_OUTPUT_DIR = "results";

    private ResultsWriterUtils() {
        //Empty - this is a static class.
    }

    public static Path resolveOutputDir(String outputPrefix) {
        if (outputPrefix.isEmpty()) {
            return Path.of(DEFAULT_OUTPUT_DIR);
        }
        Path outputPrefixPath = Path.of(outputPrefix);
        if (Files.isDirectory(outputPrefixPath)) {
            return outputPrefixPath;
        }
        return Objects.requireNonNullElse(outputPrefixPath.getParent(), Path.of(""));
    }

    /**
     * Determines the correct file extension for a file given that was specified by the user, or a sensible default if not.
     *
     * @param vcfPath
     * @param outputPrefix
     * @param outputFormat
     * @return A filename based on either the user input, or one generated from the sample.
     */
    public static String makeOutputFilename(Path vcfPath, String outputPrefix, OutputFormat outputFormat) {
        String baseFileName = Path.of(outputPrefix).getFileName().toString();
        if (baseFileName.isEmpty() && vcfPath == null) {
            baseFileName = "exomiser";
        } else if (baseFileName.isEmpty()) {
            String vcfFileName = vcfPath.getFileName().toString().replace(".vcf", "").replace(".gz", "");
            baseFileName = vcfFileName + "-exomiser";
        }
        return resolveOutputDir(outputPrefix).normalize().resolve(baseFileName + '.' + outputFormat.getFileExtension()).toString();
    }

    /**
     * Make a {@code VariantTypeCounter} object from the list of
     * {@code VariantEvaluation}. We use this to print out a table of variant
     * class distribution.
     *
     * @param variantEvaluations
     * @return
     */
    public static List<VariantEffectCount> makeVariantEffectCounters(List<String> sampleNames, List<VariantEvaluation> variantEvaluations) {

        // all used Jannovar VariantEffects
        final Set<VariantEffect> variantEffects = ImmutableSet.of(
                VariantEffect.FRAMESHIFT_ELONGATION,
                VariantEffect.FRAMESHIFT_TRUNCATION, VariantEffect.FRAMESHIFT_VARIANT,
                VariantEffect.INTERNAL_FEATURE_ELONGATION, VariantEffect.FEATURE_TRUNCATION, VariantEffect.MNV,
                VariantEffect.STOP_GAINED, VariantEffect.STOP_LOST, VariantEffect.START_LOST,
                VariantEffect.SPLICE_ACCEPTOR_VARIANT, VariantEffect.SPLICE_DONOR_VARIANT,
                VariantEffect.MISSENSE_VARIANT,
                VariantEffect.INFRAME_INSERTION, VariantEffect.DISRUPTIVE_INFRAME_INSERTION,
                VariantEffect.INFRAME_DELETION, VariantEffect.DISRUPTIVE_INFRAME_DELETION,
                VariantEffect.SPLICE_REGION_VARIANT, VariantEffect.STOP_RETAINED_VARIANT,
                VariantEffect.INITIATOR_CODON_VARIANT, VariantEffect.SYNONYMOUS_VARIANT,
                VariantEffect.FIVE_PRIME_UTR_TRUNCATION,
                VariantEffect.FIVE_PRIME_UTR_INTRON_VARIANT,
                VariantEffect.FIVE_PRIME_UTR_EXON_VARIANT,
                VariantEffect.THREE_PRIME_UTR_TRUNCATION,
                VariantEffect.THREE_PRIME_UTR_INTRON_VARIANT,
                VariantEffect.THREE_PRIME_UTR_EXON_VARIANT,
                VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT, VariantEffect.NON_CODING_TRANSCRIPT_EXON_VARIANT,
                VariantEffect.NON_CODING_TRANSCRIPT_INTRON_VARIANT, VariantEffect.UPSTREAM_GENE_VARIANT,
                VariantEffect.DOWNSTREAM_GENE_VARIANT, VariantEffect.INTERGENIC_VARIANT,
                VariantEffect.REGULATORY_REGION_VARIANT);

        VariantEffectCounter variantEffectCounter = new VariantEffectCounter(sampleNames, variantEvaluations);
        return variantEffectCounter.getVariantEffectCounts(variantEffects);
    }

    public static List<FilterReport> makeFilterReports(Analysis analysis, AnalysisResults analysisResults) {
        return filterReportFactory.makeFilterReports(analysis, analysisResults);
    }

    public static List<Gene> getMaxPassedGenes(List<Gene> genes, int maxGenes) {
        List<Gene> passedGenes = getPassedGenes(genes);
        if (maxGenes == 0) {
            logger.debug("Maximum gene limit set to {} - Returning all {} genes which have passed filtering.", maxGenes, passedGenes.size());
            return passedGenes;
        }
        return getMaxGenes(passedGenes, maxGenes);
    }

    private static List<Gene> getPassedGenes(List<Gene> genes) {
        List<Gene> passedGenes = genes.stream()
                .filter(Gene::passedFilters)
                .collect(Collectors.toList());
        logger.debug("{} of {} genes passed filters", passedGenes.size(), genes.size());
        return passedGenes;
    }

    private static List<Gene> getMaxGenes(List<Gene> genes, int maxGenes) {
        List<Gene> passedGenes = genes.stream()
                .limit(maxGenes)
                .collect(Collectors.toList());
        logger.debug("Maximum gene limit set to {} - Returning first {} of {} genes which have passed filtering.", maxGenes, maxGenes, genes.size());
        return passedGenes;
    }

}
