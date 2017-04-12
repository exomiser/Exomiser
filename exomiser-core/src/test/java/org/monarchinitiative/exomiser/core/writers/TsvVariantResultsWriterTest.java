/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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
package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.pedigree.Genotype;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PolyPhenScore;
import org.monarchinitiative.exomiser.core.writers.OutputSettingsImp.OutputSettingsBuilder;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Max Schubach <max.schubach@charite.de>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvVariantResultsWriterTest {

    private final TestVariantFactory varFactory = new TestVariantFactory();

    private final TsvVariantResultsWriter instance = new TsvVariantResultsWriter();
    
    private static final String VARIANT_DETAILS_HEADER = "#CHROM\tPOS\tREF\tALT\tQUAL\tFILTER\tGENOTYPE\tCOVERAGE\tFUNCTIONAL_CLASS\tHGVS\tEXOMISER_GENE\t";
    private static final String PATHOGENICITY_SCORES_HEADER = "CADD(>0.483)\tPOLYPHEN(>0.956|>0.446)\tMUTATIONTASTER(>0.94)\tSIFT(<0.06)\tREMM\t";
    private static final String FREQUENCY_DATA_HEADER =  "DBSNP_ID\tMAX_FREQUENCY\tDBSNP_FREQUENCY\t"
            + "EVS_EA_FREQUENCY\tEVS_AA_FREQUENCY\t"
            + "EXAC_AFR_FREQ\tEXAC_AMR_FREQ\tEXAC_EAS_FREQ\tEXAC_FIN_FREQ\tEXAC_NFE_FREQ\tEXAC_SAS_FREQ\tEXAC_OTH_FREQ\t";
    private static final String EXOMISER_SCORES_HEADER = 
            "EXOMISER_VARIANT_SCORE\tEXOMISER_GENE_PHENO_SCORE\tEXOMISER_GENE_VARIANT_SCORE\tEXOMISER_GENE_COMBINED_SCORE\tCONTRIBUTING_VARIANT\n";
    
    private static final String HEADER = VARIANT_DETAILS_HEADER + PATHOGENICITY_SCORES_HEADER + FREQUENCY_DATA_HEADER + EXOMISER_SCORES_HEADER;

    private static final String PASS_VARIANT_DETAILS = "chr10\t123256215\tT\tG\t2.2\tPASS\t0/1\t0\tmissense_variant\tFGFR2:uc021pzz.1:c.1694A>C:p.(Glu565Ala)\tFGFR2";
    private static final String FAIL_VARIANT_DETAILS = "chr7\t155604800\tC\tCTT\t1.0\tvar-effect\t0/1\t0\tframeshift_variant\tSHH:uc003wmk.1:c.16_17insAA:p.(Arg6Lysfs*6)\tSHH";
    private static final String NO_PATH_SCORES = "\t.\t.\t.\t.\t.";
    private static final String NO_FREQUENCY_DATA = "\t.\t0.0\t.\t.\t.\t.\t.\t.\t.\t.\t.\t.";
    private static final String PASS_VARIANT_EXOMISER_SCORES = "\t1.0\t0.0\t0.0\t0.0";
    private static final String FAIL_VARIANT_EXOMISER_SCORES = "\t0.95\t0.0\t0.0\t0.0";

    private static final String CONTRIBUTING_VARIANT_FIELD = "\tCONTRIBUTING_VARIANT";
    private static final String NON_CONTRIBUTING_VARIANT_FIELD = "\t.";

    private static final String PASS_VARIANT_LINE = PASS_VARIANT_DETAILS + "\t.\t1.0\t.\t.\t." + NO_FREQUENCY_DATA + PASS_VARIANT_EXOMISER_SCORES + NON_CONTRIBUTING_VARIANT_FIELD +"\n";
    private static final String FAIL_VARIANT_LINE = FAIL_VARIANT_DETAILS + NO_PATH_SCORES + NO_FREQUENCY_DATA + FAIL_VARIANT_EXOMISER_SCORES + NON_CONTRIBUTING_VARIANT_FIELD + "\n";

    private final OutputSettingsBuilder settingsBuilder = OutputSettings.builder().outputFormats(EnumSet.of(OutputFormat.TSV_VARIANT));
    private final Analysis analysis = Analysis.builder().build();
    private AnalysisResults analysisResults;

    @Before
    public void before() {
        Gene fgfr2 = TestFactory.newGeneFGFR2();
        fgfr2.addVariant(makePassVariant());

        Gene shh = TestFactory.newGeneSHH();
        shh.addVariant(makeFailVariant());

        analysisResults = AnalysisResults.builder()
                .genes(Arrays.asList(fgfr2, shh))
                .build();
    }

    private VariantEvaluation makePassVariant() {
        VariantEvaluation variant = varFactory.buildVariant(10, 123256215, "T", "G", Genotype.HETEROZYGOUS, 30, 0, 2.2);
        variant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        variant.setPathogenicityData(new PathogenicityData(PolyPhenScore.valueOf(1f)));
        return variant;
    }
    
    private VariantEvaluation makeFailVariant() {
        VariantEvaluation variant = varFactory.buildVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);
        variant.addFilterResult(FilterResult.fail(FilterType.VARIANT_EFFECT_FILTER));
        return variant;
    }

    @Test
    public void testWrite() {
        OutputSettings settings = settingsBuilder.outputPrefix("testWrite").build();
        instance.writeFile(analysis, analysisResults, settings);
        assertTrue(Paths.get("testWrite.variants.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.variants.tsv").toFile().delete());
    }

    @Test
    public void testWriteStringContainsAllVariants() {
        OutputSettings settings = settingsBuilder.build();
        String outString = instance.writeString(analysis, analysisResults, settings);
        String expected = HEADER
                + PASS_VARIANT_LINE
                + FAIL_VARIANT_LINE;
        assertThat(outString, equalTo(expected));
    }
    
    @Test
    public void testWritePassVariantsOnlyStringContainsOnlyPassedVariants() {
        OutputSettings settings = settingsBuilder.outputPassVariantsOnly(true).build();
        String outString = instance.writeString(analysis, analysisResults, settings);
        String expected = HEADER +
                PASS_VARIANT_LINE;
        assertThat(outString, equalTo(expected));
    }

    @Test
    public void testContributingVariantIsIndicated() {
        OutputSettings settings = settingsBuilder.build();
        Gene fgfr2 = TestFactory.newGeneFGFR2();

        VariantEvaluation passVariant = makePassVariant();
        passVariant.setAsContributingToGeneScore();
        fgfr2.addVariant(passVariant);

        AnalysisResults results = AnalysisResults.builder()
                .genes(Collections.singletonList(fgfr2))
                .build();

        String outString = instance.writeString(analysis, results, settings);
        String expected = HEADER +
                PASS_VARIANT_DETAILS + "\t.\t1.0\t.\t.\t." + NO_FREQUENCY_DATA + PASS_VARIANT_EXOMISER_SCORES + CONTRIBUTING_VARIANT_FIELD + "\n";
        assertThat(outString, equalTo(expected));
    }

}
