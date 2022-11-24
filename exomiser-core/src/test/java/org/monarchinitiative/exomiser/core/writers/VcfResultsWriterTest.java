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

package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import htsjdk.samtools.util.BlockCompressedInputStream;
import htsjdk.variant.vcf.VCFFileReader;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.analysis.Analysis;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeOptions;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.genome.TestVariantFactory;
import org.monarchinitiative.exomiser.core.genome.TestVcfReader;
import org.monarchinitiative.exomiser.core.genome.VariantFactory;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PolyPhenScore;
import org.monarchinitiative.exomiser.core.prioritisers.OmimPriorityResult;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for the {@link VcfResultsWriter} class.
 * 
 * The {@link VcfResultsWriter} class needs a {@link VCFFileReader} for building its header. Thus, we base our output
 * {@link VcfResultsWriter} on the minimal.vcf file from the test resources.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
public class VcfResultsWriterTest {

    private final static String METADATA_HEADER = """
            ##fileformat=VCFv4.2
            ##INFO=<ID=Exomiser,Number=.,Type=String,Description="A pipe-separated set of values for the proband allele(s) from the record with one per compatible MOI following the format: {RANK|ID|GENE_SYMBOL|ENTREZ_GENE_ID|MOI|P-VALUE|EXOMISER_GENE_COMBINED_SCORE|EXOMISER_GENE_PHENO_SCORE|EXOMISER_GENE_VARIANT_SCORE|EXOMISER_VARIANT_SCORE|CONTRIBUTING_VARIANT|WHITELIST_VARIANT|FUNCTIONAL_CLASS|HGVS|EXOMISER_ACMG_CLASSIFICATION|EXOMISER_ACMG_EVIDENCE|EXOMISER_ACMG_DISEASE_ID|EXOMISER_ACMG_DISEASE_NAME}">
            """;

    private static final String CHR_7_CONTIG_HEADER = "##contig=<ID=10,length=135534747,assembly=GRCh37.p13>\n";

    private static final String CHR_10_CONTIG_HEADER = "##contig=<ID=10,length=135534747,assembly=GRCh37.p13>\n";

    private static final String SAMPLE_HEADER = "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tsample\n";

    private static final String CHR10_FGFR2_PASS_VARIANT = "10\t123256214\t.\tA\tG\t2.20\tPASS\tExomiser={1|10-123256214-A-G_AD|FGFR2|2263|AD|1.0000|1.0000|0.0000|0.0000|0.8900|0|0|missense_variant|FGFR2:uc021pzz.1:c.1695G>C:p.(Glu565Asp)|NOT_AVAILABLE|||\"\"}\tGT:RD\t0/1:30\n";
    private static final String CHR10_FGFR2_CONTRIBUTING_VARIANT = "10\t123256215\t.\tT\tG\t2.20\tPASS\tExomiser={1|10-123256215-T-G_AD|FGFR2|2263|AD|1.0000|1.0000|0.0000|0.0000|1.0000|1|0|missense_variant|FGFR2:uc021pzz.1:c.1694A>C:p.(Glu565Ala)|NOT_AVAILABLE|||\"\"}\tGT:RD\t0/1:30\n";

    private static final FilterResult PASS_TARGET_RESULT = FilterResult.pass(FilterType.VARIANT_EFFECT_FILTER);
    private static final FilterResult FAIL_TARGET_RESULT = FilterResult.fail(FilterType.VARIANT_EFFECT_FILTER);
    private static final FilterResult FAIL_FREQUENCY_RESULT = FilterResult.fail(FilterType.FREQUENCY_FILTER);

    private final VcfResultsWriter instance = new VcfResultsWriter();

    private static VCFFileReader reader;

    //    private Path outPath;
    private OutputSettings settings = OutputSettings.builder()
            .outputFormats(EnumSet.of(OutputFormat.VCF))
            .outputPrefix("testWrite")
            .build();

    private final Sample sample = Sample.builder()
            .vcfPath(Paths.get("src/test/resources/minimal.vcf"))
            .build();
    private final Analysis analysis = Analysis.builder()
            .inheritanceModeOptions(InheritanceModeOptions.defaults())
            .build();
    /**
     * VariantEvaluation objects used for testing (annotated ones).
     */
    private VariantEvaluation fgfr2PassMissenseVariant;
    private VariantEvaluation fgfr2ContributingVariant;
    private VariantEvaluation shhIndelVariant;

    private Gene fgfr2Gene;
    private Gene shhGene;

    @BeforeEach
    public void setUp() throws IOException {
        setUpModel();
    }

    private void setUpModel() {
        setUpFgfr2Gene();
        setUpShhGene();
    }

    private void setUpShhGene() {
        shhIndelVariant = TestVariantFactory.buildVariant(7, 155604800, "C", "CTT", SampleGenotype.het(), 30, 1.0);

        shhGene = TestFactory.newGeneSHH();
        shhGene.addVariant(shhIndelVariant);
        shhGene.addPriorityResult(new OmimPriorityResult(shhGene.getEntrezGeneID(), shhGene.getGeneSymbol(), 1f, Collections.emptyList(), Collections.emptyMap()));
    }

    private Gene setUpFgfr2Gene() {
        fgfr2PassMissenseVariant = TestVariantFactory.buildVariant(10, 123256214, "A", "G", SampleGenotype.het(), 30, 2.2);
        fgfr2PassMissenseVariant.setPathogenicityData(PathogenicityData.of(PolyPhenScore.of(0.89f)));
        fgfr2PassMissenseVariant.contributesToGeneScoreUnderMode(ModeOfInheritance.ANY);
        fgfr2ContributingVariant = TestVariantFactory.buildVariant(10, 123256215, "T", "G", SampleGenotype.het(), 30, 2.2);
        fgfr2ContributingVariant.setPathogenicityData(PathogenicityData.of(PolyPhenScore.of(1f)));
        fgfr2ContributingVariant.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        fgfr2Gene = TestFactory.newGeneFGFR2();
        fgfr2Gene.addVariant(fgfr2PassMissenseVariant);
        fgfr2Gene.addPriorityResult(new OmimPriorityResult(fgfr2Gene.getEntrezGeneID(), fgfr2Gene.getGeneSymbol(), 1f, Collections.emptyList(), Collections.emptyMap()));
        GeneScore geneScore = GeneScore.builder().combinedScore(1f).phenotypeScore(1f).variantScore(fgfr2ContributingVariant.getVariantScore()).geneIdentifier(fgfr2Gene.getGeneIdentifier()).contributingVariants(List.of(fgfr2ContributingVariant)).modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT).build();
        fgfr2Gene.addGeneScore(geneScore);
    }

    private AnalysisResults buildAnalysisResults(Sample sample, Analysis analysis, Gene... genes) {
        return AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .genes(Arrays.asList(genes))
                .build();
    }

    /* test that the extended header is written out properly */
    @Test
    public void testWriteHeaderFile() {
        AnalysisResults analysisResults = AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .build();
        assertThat(instance.writeString(analysisResults, settings), equalTo(METADATA_HEADER + SAMPLE_HEADER));
    }

    /* test writing out unannotated variants */
    @Test
    public void testWriteUnannotatedVariants() {
        TestVcfReader testVcfReader = TestVcfReader.forSamples("Sample");
        VariantEvaluation unAnnotatedVariantEvaluation1 = TestFactory.variantBuilder(5, 11, "C", "T")
                .variantContext(testVcfReader.readVariantContext("5 11 . C T 1 PASS . GT 0/1"))
                .build();
        VariantEvaluation unAnnotatedVariantEvaluation2 = TestFactory.variantBuilder(5, 14, "T", "TG")
                .variantContext(testVcfReader.readVariantContext("5 14 . T TG 1 PASS . GT 0/1"))
                .build();

        AnalysisResults analysisResults = AnalysisResults.builder()
                .sample(sample)
                .analysis(analysis)
                .variantEvaluations(Arrays.asList(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2))
                .build();

        String vcf = instance.writeString(analysisResults, settings);
        final String expected = METADATA_HEADER + SAMPLE_HEADER
                + "5\t11\t.\tC\tT\t1\t.\tExWarn=VARIANT_NOT_ANALYSED_NO_GENE_ANNOTATIONS\tGT\t0/1\n"
                + "5\t14\t.\tT\tTG\t1\t.\tExWarn=VARIANT_NOT_ANALYSED_NO_GENE_ANNOTATIONS\tGT\t0/1\n";
        assertThat(vcf, equalTo(expected));
    }

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

        Gene shhGene = buildShhGene();

        Sample sample = Sample.builder()
                .vcfPath(Paths.get("src/test/resources/minimal.vcf"))
                .build();

        return AnalysisResults.builder()
                .genes(List.of(fgfr2Gene, shhGene))
                .sample(sample)
                .build();
    }

    private static Gene buildShhGene() {
        Gene shhGene = TestFactory.newGeneSHH();
        VariantEvaluation shhFailedVariant = TestVariantFactory.buildVariant(7, 155604800, "C", "CT", SampleGenotype.het(), 30, 1.0);
        shhFailedVariant.addFilterResult(FilterResult.fail(FilterType.VARIANT_EFFECT_FILTER));
        shhGene.addVariant(shhFailedVariant);
        return shhGene;
    }

    /* test writing out annotated variants in two genes */
    @Test
    public void testWriteAnnotatedVariantsNoFiltersApplied() {

        AnalysisResults analysisResults = buildAnalysisResults();

        String vcf = instance.writeString(analysisResults, settings);
        final String expected = METADATA_HEADER
                + """
                     ##contig=<ID=7,length=159138663,assembly=GRCh37.p13>
                     ##contig=<ID=10,length=135534747,assembly=GRCh37.p13>
                     #CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tsample
                     7\t155604800\t.\tC\tCT\t1\tPASS\tExomiser={2|7-155604800-C-CT_ANY|SHH|6469|ANY|1.0000|0.0000|0.0000|0.0000|1.0000|0|0|frameshift_variant|SHH:uc003wmk.1:c.16dup:p.(Arg6Lysfs*58)|NOT_AVAILABLE|||""}\tGT:RD\t0/1:30
                     10\t123256214\t.\tA\tG\t2.20\tPASS\tExomiser={1|10-123256214-A-G_AD|FGFR2|2263|AD|1.0000|1.0000|1.0000|0.9500|0.5958|0|0|missense_variant|FGFR2:uc021pzz.1:c.1695G>C:p.(Glu565Asp)|NOT_AVAILABLE|||""}\tGT:RD\t0/1:30
                     10\t123256215\t.\tT\tG\t2.20\tPASS\tExomiser={1|10-123256215-T-G_AD|FGFR2|2263|AD|1.0000|1.0000|1.0000|0.9500|0.9500|1|0|missense_variant|FGFR2:uc021pzz.1:c.1694A>C:p.(Glu565Ala)|NOT_AVAILABLE|||""}\tGT:RD\t0/1:30
                     """;
        assertThat(vcf, equalTo(expected));
    }

    @Test
    public void testAnnotatedVariantGeneSymbolWhitespaceIsReplacedWithUnderscore() {
        GeneIdentifier incorrectGeneSymbol = GeneIdentifier.builder()
                .geneId("6469")
                //this should not have spaces in the VCF file
                .geneSymbol("SHH alpha spaces")
                .hgncId("HGNC:10848")
                .hgncSymbol("SHH")
                .entrezId("6469")
                .ensemblId("ENSG00000164690")
                .ucscId("uc003wmk.2")
                .build();

        Gene gene = new Gene(incorrectGeneSymbol);
        VariantEvaluation shhFailedVariant = TestVariantFactory.buildVariant(7, 155604800, "C", "CT", SampleGenotype.het(), 30, 1.0);
        shhFailedVariant.addFilterResult(FilterResult.fail(FilterType.VARIANT_EFFECT_FILTER));
        gene.addVariant(shhFailedVariant);
        gene.addPriorityResult(new OmimPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), 1f, Collections.emptyList(), Collections.emptyMap()));
        gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, gene);

        String vcf = instance.writeString(analysisResults, settings);
        final String expected = METADATA_HEADER + CHR_7_CONTIG_HEADER + SAMPLE_HEADER
                + "7\t155604800\t.\tC\tCT\t1\tPASS\tExomiser={1|7-155604800-C-CT_ANY|SHH_alpha_spaces|6469|ANY|1.0000|0.0000|0.0000|0.0000|1.0000|0|0|frameshift_variant|SHH:uc003wmk.1:c.16dup:p.(Arg6Lysfs*58)|NOT_AVAILABLE|||\"\"}\tGT:RD\t0/1:30\n";
        assertThat(vcf, equalTo(expected));
    }

    /* test writing out a variant PASSing all filters */
    @Test
    public void testWritePassVariants() {
        fgfr2PassMissenseVariant.addFilterResult(PASS_TARGET_RESULT);
        fgfr2PassMissenseVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        fgfr2ContributingVariant.addFilterResult(PASS_TARGET_RESULT);
        fgfr2ContributingVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2ContributingVariant.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        GeneScore geneScore = GeneScore.builder()
                // combined score required for anything to be writen out!
                .combinedScore(1d)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .geneIdentifier(fgfr2Gene.getGeneIdentifier())
                .contributingVariants(List.of(fgfr2ContributingVariant))
                .build();

        fgfr2Gene.addVariant(fgfr2ContributingVariant);
        fgfr2Gene.addGeneScore(geneScore);
        fgfr2Gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, fgfr2Gene);

        String vcf = instance.writeString(analysisResults, settings);
        String expected = METADATA_HEADER + CHR_10_CONTIG_HEADER + SAMPLE_HEADER + CHR10_FGFR2_PASS_VARIANT + CHR10_FGFR2_CONTRIBUTING_VARIANT;
        assertThat(vcf, equalTo(expected));
    }

    @Test
    public void testWritePassVariantsToFile(@TempDir Path tempDir) throws IOException {
        fgfr2PassMissenseVariant.addFilterResult(PASS_TARGET_RESULT);
        fgfr2PassMissenseVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        fgfr2ContributingVariant.addFilterResult(PASS_TARGET_RESULT);
        fgfr2ContributingVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2ContributingVariant.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        GeneScore geneScore = GeneScore.builder()
                // combined score required for anything to be writen out!
                .combinedScore(1d)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .geneIdentifier(fgfr2Gene.getGeneIdentifier())
                .contributingVariants(List.of(fgfr2ContributingVariant))
                .build();

        fgfr2Gene.addVariant(fgfr2ContributingVariant);
        fgfr2Gene.addGeneScore(geneScore);
        fgfr2Gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, fgfr2Gene);

        Path vcfOutFilePrefix = tempDir.resolve("test-vcf-writer");
        OutputSettings outputSettings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.VCF))
                .outputPrefix(vcfOutFilePrefix.toString())
                .build();
        instance.writeFile(analysisResults, outputSettings);

        Path vcfOutFile = tempDir.resolve("test-vcf-writer.vcf.gz");
        assertThat(Files.exists(vcfOutFile), equalTo(true));

        List<String> expected = new ArrayList<>(List.of(METADATA_HEADER.split("\n")));
        expected.add(CHR_10_CONTIG_HEADER.trim());
        expected.add(SAMPLE_HEADER.trim());
        expected.add(CHR10_FGFR2_PASS_VARIANT.trim());
        expected.add(CHR10_FGFR2_CONTRIBUTING_VARIANT.trim());

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new BlockCompressedInputStream(new FileInputStream(vcfOutFile.toFile()))))) {
            List<String> actual = br.lines().toList();
            assertThat(actual, equalTo(expected));
        }
    }

    /* test writing out a variant failing the target filter */
    @Test
    public void testWriteFailTargetVariant() {
        EnumSet<ModeOfInheritance> autosomalDominantCompatible = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        fgfr2PassMissenseVariant.addFilterResult(FAIL_TARGET_RESULT);
        fgfr2PassMissenseVariant.setCompatibleInheritanceModes(autosomalDominantCompatible);
        fgfr2Gene.setCompatibleInheritanceModes(autosomalDominantCompatible);

        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, fgfr2Gene);

        String vcfAD = instance.writeString(analysisResults, settings);
        assertThat(vcfAD, equalTo(failedFgfr2VariantWithFilterField("var-effect")));

        String vcfAR = instance.writeString(analysisResults, settings);
        assertThat(vcfAR, equalTo(failedFgfr2VariantWithFilterField("inheritance;var-effect")));
    }

    private String failedFgfr2VariantWithFilterField(String filterFieldAD) {
        return METADATA_HEADER
                + "10\t123256214\t.\tA\tG\t2.20\t" + filterFieldAD + "\tExGeneSCombi=0.0;ExGeneSPheno=0.0;ExGeneSVar=0.0;ExGeneSymbId=2263;ExGeneSymbol=FGFR2;ExVarEff=missense_variant;ExVarScore=0.89\tGT:RD\t0/1:30\n";
    }

    @Test
    public void testWriteContributingVariantsOnlyContainsOnlyContributingVariantLine() {

        shhIndelVariant.addFilterResult(FAIL_TARGET_RESULT);
        shhIndelVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        fgfr2PassMissenseVariant.addFilterResult(PASS_TARGET_RESULT);
        fgfr2PassMissenseVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        fgfr2ContributingVariant.addFilterResult(PASS_TARGET_RESULT);
        fgfr2ContributingVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        fgfr2ContributingVariant.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        GeneScore geneScore = GeneScore.builder()
                // combined score required for anything to be writen out!
                .combinedScore(1d)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .geneIdentifier(fgfr2Gene.getGeneIdentifier())
                .contributingVariants(List.of(fgfr2ContributingVariant))
                .build();

        //TODO: check the logic here - are we dealing with GeneScores or Genes?
        // According to a helpful comment in GeneScoreRanker:
        // "a GeneScore only contains the contributing variants so can't be used directly to get the variants involved,
        //  hence the requirement for the Gene."
        // Perhaps it might be worth the GeneScorer assigning all other compatible variants to the GeneScore too?

        fgfr2Gene.addVariant(fgfr2ContributingVariant);
        fgfr2Gene.addGeneScore(geneScore);
        fgfr2Gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, fgfr2Gene, shhGene);

        OutputSettings outputContributingVariantsOnly = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.VCF))
                .outputContributingVariantsOnly(true)
                .build();

        String output = instance.writeString(analysisResults, outputContributingVariantsOnly);
        String expected = METADATA_HEADER + CHR_10_CONTIG_HEADER + SAMPLE_HEADER + CHR10_FGFR2_CONTRIBUTING_VARIANT;
        assertThat(output, equalTo(expected));
    }

    @Test
    public void testWritePassVariantsWithNoPassingVariants() {
        fgfr2PassMissenseVariant.addFilterResult(FAIL_TARGET_RESULT);
        fgfr2PassMissenseVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        shhIndelVariant.addFilterResult(FAIL_TARGET_RESULT);
        shhIndelVariant.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        fgfr2Gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        shhGene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, fgfr2Gene, shhGene);

        OutputSettings outputPassVariantsOnlySettings = OutputSettings.builder()
                .outputFormats(EnumSet.of(OutputFormat.VCF))
                .outputContributingVariantsOnly(true)
                .build();

        String output = instance.writeString(analysisResults, outputPassVariantsOnlySettings);
        assertThat(output, equalTo(METADATA_HEADER + SAMPLE_HEADER));
    }
    
    @Test
    public void testAlternativeAllelesAreWrittenOnSuccessiveLines() {
        VariantEvaluation alt1 = TestVariantFactory.buildVariant(1, 120612040, "T", "TCCGCCG", SampleGenotype.het(), 30, 258.62);
        alt1.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        alt1.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        VariantEvaluation alt2 = TestVariantFactory.buildVariant(1, 120612040, "T", "TCCTCCGCCG", SampleGenotype.homAlt(), 30, 258.62);
        alt2.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        alt2.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);

        Gene gene = new Gene("TEST", 12345);
        gene.addVariant(alt1);
        gene.addVariant(alt2);
        gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        GeneScore geneScore = GeneScore.builder()
                .combinedScore(1d)
                .modeOfInheritance(ModeOfInheritance.AUTOSOMAL_DOMINANT)
                .geneIdentifier(gene.getGeneIdentifier())
                .contributingVariants(List.of(alt1, alt2))
                .build();
        gene.addGeneScore(geneScore);

        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, gene);

        String output = instance.writeString(analysisResults, settings);
        String expected = METADATA_HEADER
                + "##contig=<ID=1,length=249250621,assembly=GRCh37.p13>\n"
                + SAMPLE_HEADER
                + "1\t120612040\t.\tT\tTCCGCCG\t258.62\tPASS\tExomiser={1|1-120612040-T-TCCGCCG_AD|TEST|12345|AD|1.0000|1.0000|0.0000|0.0000|0.0000|1|0|intergenic_variant|RBM8A:uc001ent.2::|NOT_AVAILABLE|||\"\"}\tGT:RD\t0/1:30\n"
                + "1\t120612040\t.\tT\tTCCTCCGCCG\t258.62\tPASS\tExomiser={1|1-120612040-T-TCCTCCGCCG_AD|TEST|12345|AD|1.0000|1.0000|0.0000|0.0000|0.0000|1|0|intergenic_variant|RBM8A:uc001ent.2::|NOT_AVAILABLE|||\"\"}\tGT:RD\t1/1:30\n"
                ;
//                + "1\t120612040\t.\tT\tTCCGCCG\t258.62\t.\tExGeneSCombi=0.0;ExGeneSPheno=0.0;ExGeneSVar=0.0;ExGeneSymbId=12345;ExGeneSymbol=TEST;ExVarEff=intergenic_variant;ExVarScore=0.0\tGT:RD\t0/1:30\n"
//                + "1\t120612040\t.\tT\tTCCTCCGCCG\t258.62\t.\tExGeneSCombi=0.0;ExGeneSPheno=0.0;ExGeneSVar=0.0;ExGeneSymbId=12345;ExGeneSymbol=TEST;ExVarEff=intergenic_variant;ExVarScore=0.0\tGT:RD\t1/1:30\n";
        assertThat(output, equalTo(expected));
    }

    @Test
    public void testHomozygousAltAlleleOutputVcfContainsConcatenatedVariantScoresOnOneLine() {
        Path vcfPath = Paths.get("src/test/resources/multiAlleleGenotypes.vcf");
        VariantFactory variantFactory = TestFactory.buildDefaultVariantFactory(vcfPath);
        List<VariantEvaluation> variants = variantFactory.createVariantEvaluations().toList();
        // 1/2 HETEROZYGOUS_ALT - needs to be written back out as a single line
        VariantEvaluation altAlleleOne = variants.get(3).toBuilder()
                //change the variant effect from MISSENSE so that the score is different and the order can be tested on the output line
                //variant score is 0.95 - contributes to score
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .contributingModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT))
                .build();

        //variant score is 0.6
        VariantEvaluation altAlleleTwo = variants.get(4);

        Gene gene = new Gene(GeneIdentifier.builder()
                .geneSymbol(altAlleleOne.getGeneSymbol())
                .geneId(altAlleleOne.getGeneId())
                .build());
        gene.addVariant(altAlleleOne);
        gene.addVariant(altAlleleTwo);
        gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        AnalysisResults analysisResults = buildAnalysisResults(sample, analysis, gene);

        String output = instance.writeString(analysisResults, settings);
        //expected should have concatenated variant score for multi-allele line: ExVarSCombi=0.85,0.6
        String expected = METADATA_HEADER + CHR_10_CONTIG_HEADER + SAMPLE_HEADER
                + "10\t123256215\t.\tT\tG,A\t100\t.\tExContribAltAllele=0;ExGeneSCombi=0.0;ExGeneSPheno=0.0;ExGeneSVar=0.0;ExGeneSymbId=2263;ExGeneSymbol=FGFR2;ExVarEff=frameshift_variant,missense_variant;ExVarScore=1.0,0.6;GENE=FGFR2;INHERITANCE=AD;MIM=101600\tGT\t1/2\n";
        assertThat(output, equalTo(expected));
    }
}
