/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PolyPhenScore;
import org.monarchinitiative.exomiser.core.writers.OutputSettings.Builder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Max Schubach <max.schubach@charite.de>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvVariantResultsWriterTest {

    private final TsvVariantResultsWriter instance = new TsvVariantResultsWriter();

    private static final String VARIANT_DETAILS_HEADER = "#CHROM\tPOS\tREF\tALT\tQUAL\tFILTER\tGENOTYPE\tCOVERAGE\tFUNCTIONAL_CLASS\tHGVS\tEXOMISER_GENE\t";
    private static final String PATHOGENICITY_SCORES_HEADER = "CADD(>0.483)\tPOLYPHEN(>0.956|>0.446)\tMUTATIONTASTER(>0.94)\tSIFT(<0.06)\tREMM\t";
    private static final String FREQUENCY_DATA_HEADER = "DBSNP_ID\tMAX_FREQUENCY\tDBSNP_FREQUENCY\t"
            + "EVS_EA_FREQUENCY\tEVS_AA_FREQUENCY\t"
            + "EXAC_AFR_FREQ\tEXAC_AMR_FREQ\tEXAC_EAS_FREQ\tEXAC_FIN_FREQ\tEXAC_NFE_FREQ\tEXAC_SAS_FREQ\tEXAC_OTH_FREQ\t";
    private static final String EXOMISER_SCORES_HEADER =
            "EXOMISER_VARIANT_SCORE\tEXOMISER_GENE_PHENO_SCORE\tEXOMISER_GENE_VARIANT_SCORE\tEXOMISER_GENE_COMBINED_SCORE\tCONTRIBUTING_VARIANT\n";

    private static final String HEADER = "#RANK\tID\tGENE_SYMBOL\tENTREZ_GENE_ID\tMOI\tP-VALUE\tEXOMISER_GENE_COMBINED_SCORE\tEXOMISER_GENE_PHENO_SCORE\tEXOMISER_GENE_VARIANT_SCORE\tEXOMISER_VARIANT_SCORE\tCONTRIBUTING_VARIANT\tWHITELIST_VARIANT\tVCF_ID\tRS_ID\tCONTIG\tSTART\tEND\tREF\tALT\tCHANGE_LENGTH\tQUAL\tFILTER\tGENOTYPE\tFUNCTIONAL_CLASS\tHGVS\tEXOMISER_ACMG_CLASSIFICATION\tEXOMISER_ACMG_EVIDENCE\tEXOMISER_ACMG_DISEASE_ID\tEXOMISER_ACMG_DISEASE_NAME\tCLINVAR_ALLELE_ID\tCLINVAR_PRIMARY_INTERPRETATION\tCLINVAR_STAR_RATING\tGENE_CONSTRAINT_LOEUF\tGENE_CONSTRAINT_LOEUF_LOWER\tGENE_CONSTRAINT_LOEUF_UPPER\tMAX_FREQ_SOURCE\tMAX_FREQ\tALL_FREQ\tMAX_PATH_SOURCE\tMAX_PATH\tALL_PATH\\n";

    private static final String FAIL_VARIANT_DETAILS = "7\t155604800\tC\tCTT\t1.0\tvar-effect\t0/1\t0\tframeshift_variant\tSHH:uc003wmk.1:c.16_17insAA:p.(Arg6Lysfs*6)\tSHH";
    private static final String PASS_VARIANT_DETAILS = "10\t123256214\tA\tG\t2.2\tPASS\t0/1\t0\tmissense_variant\tFGFR2:uc021pzz.1:c.1695G>C:p.(Glu565Asp)\tFGFR2";
    private static final String CONTRIBUTING_VARIANT_DETAILS = "10\t123256215\tT\tG\t2.2\tPASS\t0/1\t0\tmissense_variant\tFGFR2:uc021pzz.1:c.1694A>C:p.(Glu565Ala)\tFGFR2";

    private static final String NO_PATH_SCORES = "\t.\t.\t.\t.\t.";
    private static final String NO_FREQUENCY_DATA = "\t.\t0.0\t.\t.\t.\t.\t.\t.\t.\t.\t.\t.";
    private static final String FAIL_VARIANT_EXOMISER_SCORES = "\t1.0\t0.0\t0.0\t0.0";
    private static final String PASS_VARIANT_EXOMISER_SCORES = "\t0.89\t0.0\t0.0\t0.0";
    private static final String CONTRIBUTING_VARIANT_EXOMISER_SCORES = "\t1.0\t0.0\t0.0\t0.0";

    private static final String CONTRIBUTING_VARIANT_FIELD = "\tCONTRIBUTING_VARIANT";
    private static final String NON_CONTRIBUTING_VARIANT_FIELD = "\t.";

    private static final String FAIL_VARIANT_LINE = FAIL_VARIANT_DETAILS + NO_PATH_SCORES + NO_FREQUENCY_DATA + FAIL_VARIANT_EXOMISER_SCORES + NON_CONTRIBUTING_VARIANT_FIELD + "\n";
    private static final String PASS_VARIANT_LINE = PASS_VARIANT_DETAILS + "\t.\t0.89\t.\t.\t." + NO_FREQUENCY_DATA + PASS_VARIANT_EXOMISER_SCORES + NON_CONTRIBUTING_VARIANT_FIELD + "\n";
    private static final String CONTRIBUTING_VARIANT_LINE = CONTRIBUTING_VARIANT_DETAILS + "\t.\t1.0\t.\t.\t." + NO_FREQUENCY_DATA + CONTRIBUTING_VARIANT_EXOMISER_SCORES + CONTRIBUTING_VARIANT_FIELD + "\n";

    private final Builder settingsBuilder = OutputSettings.builder()
            .outputFormats(EnumSet.of(OutputFormat.TSV_VARIANT));
    private final Analysis analysis = Analysis.builder().build();
    private final Sample sample = Sample.builder().build();
    private AnalysisResults analysisResults;

    @BeforeEach
    public void before() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        fgfr2.addVariant(makePassVariant());

        Gene shh = TestFactory.newGeneSHH();
        shh.addVariant(makeFailVariant());

        analysisResults = AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .genes(Arrays.asList(fgfr2, shh))
                .build();
    }

    private VariantEvaluation makeFailVariant() {
        VariantEvaluation variant = TestVariantFactory.buildVariant(7, 155604800, "C", "CTT", SampleGenotype.het(), 30, 1.0);
        variant.addFilterResult(FilterResult.fail(FilterType.VARIANT_EFFECT_FILTER));
        return variant;
    }

    private VariantEvaluation makePassVariant() {
        VariantEvaluation variant = TestVariantFactory.buildVariant(10, 123256214, "A", "G", SampleGenotype.het(), 30, 2.2);
        variant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        variant.setPathogenicityData(PathogenicityData.of(PolyPhenScore.of(0.89f)));
        return variant;
    }

    private VariantEvaluation makeContributingVariant() {
        VariantEvaluation variant = TestVariantFactory.buildVariant(10, 123256215, "T", "G", SampleGenotype.het(), 30, 2.2);
        variant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        variant.setPathogenicityData(PathogenicityData.of(PolyPhenScore.of(1f)));
        return variant;
    }

    @Test
    public void testWriteProducesFileWithCorrectName() throws Exception {
        Path tempFolder = Files.createTempDirectory("exomiser_test");
        String outPrefix = tempFolder.resolve("testWrite").toString();

        OutputSettings settings = settingsBuilder.outputPrefix(outPrefix).build();
        instance.writeFile(analysisResults, settings);
        Path arOutputPath = tempFolder.resolve("testWrite_AR.variants.tsv");
        assertThat(arOutputPath.toFile().exists(), is(true));
        assertThat(arOutputPath.toFile().delete(), is(true));

        instance.writeFile(analysisResults, settings);
        Path adOutputPath = tempFolder.resolve("testWrite_AD.variants.tsv");
        assertThat(adOutputPath.toFile().exists(), is(true));
        assertThat(adOutputPath.toFile().delete(), is(true));

        Files.delete(tempFolder);
    }

    @Test
    public void testWriteStringUnderAnyInheritanceModeContainsAllVariants() {
        OutputSettings settings = settingsBuilder.build();

        String outString = instance.writeString(analysisResults, settings);
        String expected = HEADER
                + PASS_VARIANT_LINE
                + FAIL_VARIANT_LINE;
        assertThat(outString, equalTo(expected));
    }

    @Test
    public void testWriteContributingVariantsOnlyStringContainsOnlyContributingVariants() {
        OutputSettings settings = settingsBuilder.outputContributingVariantsOnly(true).build();

        Gene fgfr2 = TestFactory.newGeneFGFR2();
        fgfr2.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        VariantEvaluation passVariant = makePassVariant();
        passVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2.addVariant(passVariant);

        VariantEvaluation contributingVariant = makeContributingVariant();
        contributingVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        contributingVariant.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        GeneScore geneScore = GeneScore.builder()
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .contributingVariants(List.of(contributingVariant))
                .build();

        fgfr2.addVariant(contributingVariant);
        fgfr2.addGeneScore(geneScore);

        AnalysisResults results = AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .genes(Collections.singletonList(fgfr2))
                .build();

        String outString = instance.writeString(results, settings);
        String expected = HEADER
                + CONTRIBUTING_VARIANT_LINE;
        assertThat(outString, equalTo(expected));
    }

    @Test
    public void testWriteContributingVariantsOnlyStringIsEmptyForIncompatibleMode() {
        OutputSettings settings = settingsBuilder.outputContributingVariantsOnly(true).build();

        Gene fgfr2 = TestFactory.newGeneFGFR2();
        fgfr2.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        VariantEvaluation passVariant = makePassVariant();
        passVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2.addVariant(passVariant);

        VariantEvaluation contributingVariant = makeContributingVariant();
        contributingVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        contributingVariant.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        GeneScore geneScore = GeneScore.builder()
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .geneIdentifier(fgfr2.getGeneIdentifier())
                .contributingVariants(List.of(contributingVariant))
                .build();

        fgfr2.addVariant(contributingVariant);
        fgfr2.addGeneScore(geneScore);

        AnalysisResults results = AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .genes(Collections.singletonList(fgfr2))
                .build();

        String outString = instance.writeString(results, settings);
        String expected = HEADER;
        assertThat(outString, equalTo(expected));
    }

    @Test
    public void testContributingVariantIsIndicated() {
        OutputSettings settings = settingsBuilder.build();
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        fgfr2.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        VariantEvaluation contributing = makeContributingVariant();
        contributing.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        contributing.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        fgfr2.addVariant(contributing);

        AnalysisResults results = AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .genes(Collections.singletonList(fgfr2))
                .build();

        String outString = instance.writeString(results, settings);
        String expected = HEADER +
                CONTRIBUTING_VARIANT_LINE;
        assertThat(outString, equalTo(expected));
    }

}
