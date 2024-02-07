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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.GeneScore;
import org.monarchinitiative.exomiser.core.model.SampleGenotype;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Max Schubach <max.schubach@charite.de>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvVariantResultsWriterTest {

    private final TsvVariantResultsWriter instance = new TsvVariantResultsWriter();

    private static final String HEADER = "#RANK\tID\tGENE_SYMBOL\tENTREZ_GENE_ID\tMOI\tP-VALUE\tEXOMISER_GENE_COMBINED_SCORE\tEXOMISER_GENE_PHENO_SCORE\tEXOMISER_GENE_VARIANT_SCORE\tEXOMISER_VARIANT_SCORE\tCONTRIBUTING_VARIANT\tWHITELIST_VARIANT\tVCF_ID\tRS_ID\tCONTIG\tSTART\tEND\tREF\tALT\tCHANGE_LENGTH\tQUAL\tFILTER\tGENOTYPE\tFUNCTIONAL_CLASS\tHGVS\tEXOMISER_ACMG_CLASSIFICATION\tEXOMISER_ACMG_EVIDENCE\tEXOMISER_ACMG_DISEASE_ID\tEXOMISER_ACMG_DISEASE_NAME\tCLINVAR_VARIATION_ID\tCLINVAR_PRIMARY_INTERPRETATION\tCLINVAR_STAR_RATING\tGENE_CONSTRAINT_LOEUF\tGENE_CONSTRAINT_LOEUF_LOWER\tGENE_CONSTRAINT_LOEUF_UPPER\tMAX_FREQ_SOURCE\tMAX_FREQ\tALL_FREQ\tMAX_PATH_SOURCE\tMAX_PATH\tALL_PATH\n";

    private AnalysisResults buildAnalysisResults() {

        VariantEvaluation fgfr2PassMissenseVariant = TestVariantFactory.buildVariant(10, 123256214, "A", "G", SampleGenotype.het(), 30, 2.2);
        fgfr2PassMissenseVariant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        fgfr2PassMissenseVariant.setFrequencyData(FrequencyData.of(Frequency.of(FrequencySource.GNOMAD_G_AFR, 0.05f)));
        fgfr2PassMissenseVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        VariantEvaluation fgfr2ContributingVariant = TestVariantFactory.buildVariant(10, 123256215, "T", "G", SampleGenotype.het(), 30, 2.2);
        fgfr2ContributingVariant.addFilterResult(FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER));
        fgfr2ContributingVariant.setPathogenicityData(PathogenicityData.of(PathogenicityScore.of(PathogenicitySource.REVEL, 0.95f)));
        fgfr2ContributingVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2ContributingVariant.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        Gene fgfr2Gene = TestFactory.newGeneFGFR2();
        fgfr2Gene.addVariant(fgfr2PassMissenseVariant);
        fgfr2Gene.addVariant(fgfr2ContributingVariant);
        fgfr2Gene.addPriorityResult(new OmimPriorityResult(fgfr2Gene.getEntrezGeneID(), fgfr2Gene.getGeneSymbol(), 1f, Collections.emptyList(), Collections.emptyMap()));
        GeneScore geneScore = GeneScore.builder()
                .combinedScore(1f)
                .phenotypeScore(1f)
                .variantScore(fgfr2ContributingVariant.getVariantScore())
                .geneIdentifier(fgfr2Gene.getGeneIdentifier())
                .contributingVariants(List.of(fgfr2ContributingVariant))
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .build();
        fgfr2Gene.addGeneScore(geneScore);
        fgfr2Gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        Gene shhGene = TestFactory.newGeneSHH();
        VariantEvaluation shhFailedVariant = TestVariantFactory.buildVariant(7, 155604800, "C", "CT", SampleGenotype.het(), 30, 1.0);
        shhFailedVariant.addFilterResult(FilterResult.fail(FilterType.VARIANT_EFFECT_FILTER));
        shhGene.addVariant(shhFailedVariant);

        return AnalysisResults.builder()
                .genes(List.of(fgfr2Gene, shhGene))
                .build();
    }


    @Test
    public void testWriteProducesFileWithCorrectName(@TempDir Path tempFolder) {
        AnalysisResults analysisResults = buildAnalysisResults();

        String outPrefix = tempFolder.resolve("testWrite").toString();
        OutputSettings settings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.TSV_VARIANT)).outputPrefix(outPrefix).build();

        instance.writeFile(ModeOfInheritance.ANY, analysisResults, settings);

        Path outputPath = tempFolder.resolve("testWrite.variants.tsv");
        assertThat(Files.exists(outputPath), is(true));
    }

    @Test
    public void testWriteStringContainsAllVariants() {
        AnalysisResults analysisResults = buildAnalysisResults();

        OutputSettings settings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.TSV_VARIANT))
                .build();

        String expected = HEADER +
                "1	10-123256215-T-G_AD	FGFR2	2263	AD	1.0000	1.0000	1.0000	0.9500	0.9500	1	0			10	123256215	123256215	T	G	0	2.2000	PASS	0/1	missense_variant	FGFR2:uc021pzz.1:c.1694A>C:p.(Glu565Ala)	NOT_AVAILABLE					NOT_PROVIDED	0	0.18759\t0.129\t0.278				REVEL	0.95	REVEL=0.95\n"+
                "1	10-123256214-A-G_AD	FGFR2	2263	AD	1.0000	1.0000	1.0000	0.9500	0.5958	0	0			10	123256214	123256214	A	G	0	2.2000	PASS	0/1	missense_variant	FGFR2:uc021pzz.1:c.1695G>C:p.(Glu565Asp)	NOT_AVAILABLE					NOT_PROVIDED	0	0.18759\t0.129\t0.278	GNOMAD_G_AFR	0.05	GNOMAD_G_AFR=0.05\t\t\t\n"+
                "2	7-155604800-C-CT_ANY	SHH	6469	ANY	1.0000	0.0000	0.0000	0.0000	1.0000	0	0			7	155604800	155604800	C	CT	1	1.0000	var-effect	0/1	frameshift_variant	SHH:uc003wmk.1:c.16dup:p.(Arg6Lysfs*58)	NOT_AVAILABLE					NOT_PROVIDED	0	0.21546\t0.105\t0.493\t\t\t\t\t\t\n"
                ;
        assertThat(instance.writeString(ModeOfInheritance.ANY, analysisResults, settings), equalTo(expected));
    }

    @Test
    public void testWriteContributingVariantsOnlyStringContainsOnlyContributingVariants() {
        AnalysisResults results = buildAnalysisResults();

        OutputSettings settings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.TSV_VARIANT))
                .outputContributingVariantsOnly(true)
                .build();

        String expected =
                HEADER +
                "1	10-123256215-T-G_AD	FGFR2	2263	AD	1.0000	1.0000	1.0000	0.9500	0.9500	1	0			10	123256215	123256215	T	G	0	2.2000	PASS	0/1	missense_variant	FGFR2:uc021pzz.1:c.1694A>C:p.(Glu565Ala)	NOT_AVAILABLE					NOT_PROVIDED	0	0.18759\t0.129\t0.278				REVEL	0.95	REVEL=0.95\n";
        assertThat(instance.writeString(ModeOfInheritance.ANY, results, settings), equalTo(expected));
    }
}
