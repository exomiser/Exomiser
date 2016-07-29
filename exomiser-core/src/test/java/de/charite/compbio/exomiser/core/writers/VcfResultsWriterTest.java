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

package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.analysis.Analysis;
import de.charite.compbio.exomiser.core.analysis.TestAnalysisBuilder;
import de.charite.compbio.exomiser.core.factories.TestVariantFactory;
import de.charite.compbio.exomiser.core.filters.FailFilterResult;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.PassFilterResult;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PhivePriorityResult;
import de.charite.compbio.exomiser.core.writers.OutputSettingsImp.OutputSettingsBuilder;
import de.charite.compbio.jannovar.pedigree.Genotype;
import htsjdk.variant.vcf.VCFFileReader;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

/**
 * Tests for the {@link VcfResultsWriter} class.
 * 
 * The {@link VcfResultsWriter} class needs a {@link VCFFileReader} for building its header. Thus, we base our output
 * {@link VcfResultsWriter} on the minimal.vcf file from the test resources.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
@RunWith(MockitoJUnitRunner.class)
public class VcfResultsWriterTest {

    final static String EXPECTED_HEADER = "##fileformat=VCFv4.2\n"
            + "##FILTER=<ID=BED_FILTER,Description=\"Gene panel target region (Bed filter)\">\n"
            + "##FILTER=<ID=ENTREZ_GENE_ID_FILTER,Description=\"Genes to keep\">\n"
            + "##FILTER=<ID=FREQUENCY_FILTER,Description=\"Frequency\">\n"
            + "##FILTER=<ID=INHERITANCE_FILTER,Description=\"Inheritance\">\n"
            + "##FILTER=<ID=INTERVAL_FILTER,Description=\"Interval\">\n"
            + "##FILTER=<ID=KNOWN_VARIANT_FILTER,Description=\"Known variant\">\n"
            + "##FILTER=<ID=PATHOGENICITY_FILTER,Description=\"Pathogenicity\">\n"
            + "##FILTER=<ID=PRIORITY_SCORE_FILTER,Description=\"Gene priority score\">\n" 
            + "##FILTER=<ID=QUALITY_FILTER,Description=\"Quality\">\n"
            + "##FILTER=<ID=REGULATORY_FEATURE_FILTER,Description=\"Regulatory Feature\">\n"
            + "##FILTER=<ID=VARIANT_EFFECT_FILTER,Description=\"Target\">\n"
            + "##INFO=<ID=ANN,Number=1,Type=String,Description=\"Functional annotations:'Allele|Annotation|Annotation_Impact|Gene_Name|Gene_ID|Feature_Type|Feature_ID|Transcript_BioType|Rank|HGVS.c|HGVS.p|cDNA.pos / cDNA.length|CDS.pos / CDS.length|AA.pos / AA.length|Distance|ERRORS / WARNINGS / INFO'\">\n"
            + "##INFO=<ID=EFFECT,Number=1,Type=String,Description=\"variant effect (UTR5,UTR3,intronic,splicing,missense,stoploss,stopgain,startloss,duplication,frameshift-insertion,frameshift-deletion,non-frameshift-deletion,non-frameshift-insertion,synonymous)\">\n"
            + "##INFO=<ID=EXOMISER_GENE,Number=1,Type=String,Description=\"Exomiser gene\">\n"
            + "##INFO=<ID=EXOMISER_GENE_COMBINED_SCORE,Number=1,Type=Float,Description=\"Exomiser gene combined\">\n"
            + "##INFO=<ID=EXOMISER_GENE_PHENO_SCORE,Number=1,Type=Float,Description=\"Exomiser gene phenotype score\">\n"
            + "##INFO=<ID=EXOMISER_GENE_VARIANT_SCORE,Number=1,Type=Float,Description=\"Exomiser gene variant score\">\n"
            + "##INFO=<ID=EXOMISER_VARIANT_SCORE,Number=1,Type=Float,Description=\"Exomiser variant score\">\n"
            + "##INFO=<ID=EXOMISER_WARNING,Number=1,Type=String,Description=\"Exomiser gene\">\n"
            + "##INFO=<ID=HGVS,Number=1,Type=String,Description=\"HGVS Nomenclature\">\n"
            + "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tsample\n";
    private static final String CHR10_FGFR2_PATHOGENIC_MISSENSE_VARIANT = "chr10\t123353298\t.\tG\tC\t2.20\tPASS\tEXOMISER_GENE=FGFR2;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=1.0;RD=30\tGT:RD\t0/1:30\n";

    private VcfResultsWriter instance;

    private static VCFFileReader reader;

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private Path outPath;
    private OutputSettings settings;
    private SampleData sampleData;

    private Analysis analysis; 
    /** VariantEvaluation objects used for testing (annotated ones). */
    private VariantEvaluation missenseVariantEvaluation;
    private VariantEvaluation indelVariantEvaluation;

    /** VariantEvaluation objects used for testing (unannotated ones). */
    private VariantEvaluation unAnnotatedVariantEvaluation1;
    private VariantEvaluation unAnnotatedVariantEvaluation2;

    private Gene gene1, gene2;
    private FilterResult passTargetResult, failTargetResult, failFrequencyResult;

    @BeforeClass
    public static void loadVCFHeader() throws URISyntaxException {
        final String inputFilePath = VcfResultsWriterTest.class.getResource("/minimal.vcf").toURI().getPath();
        reader = new VCFFileReader(new File(inputFilePath), false); // no index required
    }

    @Before
    public void setUp() throws IOException {
        outPath = tmpFolder.newFile().toPath();
        settings = new OutputSettingsBuilder()
                .outputFormats(EnumSet.of(OutputFormat.VCF))
                .outputPrefix(outPath + "testWrite").build();

        instance = new VcfResultsWriter();

        setUpModel();
    }

    private void setUpModel() {
        sampleData = new SampleData();
        sampleData.setVcfHeader(reader.getFileHeader());
        
        analysis = new TestAnalysisBuilder().sampleData(sampleData).build();
        
        TestVariantFactory varFactory = new TestVariantFactory();

        passTargetResult = new PassFilterResult(FilterType.VARIANT_EFFECT_FILTER);
        failTargetResult = new FailFilterResult(FilterType.VARIANT_EFFECT_FILTER);
        failFrequencyResult = new FailFilterResult(FilterType.FREQUENCY_FILTER);

        missenseVariantEvaluation = varFactory.constructVariant(10, 123353297, "G", "C", Genotype.HETEROZYGOUS, 30, 0, 2.2);
        missenseVariantEvaluation.setPathogenicityData(new PathogenicityData(new PolyPhenScore(1f)));
        indelVariantEvaluation = varFactory.constructVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);

        gene1 = new Gene(missenseVariantEvaluation.getGeneSymbol(), missenseVariantEvaluation.getEntrezGeneId());
        gene1.addVariant(missenseVariantEvaluation);
        
        gene2 = new Gene(indelVariantEvaluation.getGeneSymbol(), indelVariantEvaluation.getEntrezGeneId());
        gene2.addVariant(indelVariantEvaluation);

        gene1.addPriorityResult(new PhivePriorityResult(gene1.getEntrezGeneID(), gene1.getGeneSymbol(), 0.99f, "MGI:12345", "Gene1"));
        gene2.addPriorityResult(new PhivePriorityResult(gene2.getEntrezGeneID(), gene2.getGeneSymbol(), 0.98f, "MGI:54321", "Gene2"));

        gene1.addPriorityResult(new OMIMPriorityResult(gene1.getEntrezGeneID(), gene1.getGeneSymbol(), 1f, Collections.emptyList()));
        gene2.addPriorityResult(new OMIMPriorityResult(gene2.getEntrezGeneID(), gene2.getGeneSymbol(), 1f, Collections.emptyList()));

        unAnnotatedVariantEvaluation1 = new VariantEvaluation.VariantBuilder(5, 11, "AC", "AT").quality(1).build();
        unAnnotatedVariantEvaluation2 = new VariantEvaluation.VariantBuilder(5, 14, "T", "TG").quality(1).build();
    }

    /* test that the extended header is written out properly */
    @Test
    public void testWriteHeaderFile() {
        Assert.assertEquals(EXPECTED_HEADER, instance.writeString(analysis, settings));
    }

    /* test writing out unannotated variants */
    @Test
    public void testWriteUnannotatedVariants() {       
        sampleData.setVariantEvaluations(Arrays.asList(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2));
        
        String vcf = instance.writeString(analysis, settings);
        final String EXPECTED = EXPECTED_HEADER
                + "chr5\t11\t.\tAC\tAT\t1\t.\tEXOMISER_WARNING=VARIANT_NOT_ANALYSED_NO_GENE_ANNOTATIONS\tGT\t0/1\n"
                + "chr5\t14\t.\tT\tTG\t1\t.\tEXOMISER_WARNING=VARIANT_NOT_ANALYSED_NO_GENE_ANNOTATIONS\tGT\t0/1\n";
        Assert.assertEquals(EXPECTED, vcf);
    }

    /* test writing out annotated variants in two genes */
    @Test
    public void testWriteAnnotatedVariantsNoFiltersApplied() {
        sampleData.setGenes(Arrays.asList(gene1, gene2));

        String vcf = instance.writeString(analysis, settings);
        final String EXPECTED = EXPECTED_HEADER
                + "chr10\t123353298\t.\tG\tC\t2.20\t.\tEXOMISER_GENE=FGFR2;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=1.0;RD=30\tGT:RD\t0/1:30\n"
                + "chr7\t155604801\t.\tC\tCTT\t1\t.\tEXOMISER_GENE=SHH;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=0.95;RD=30\tGT:RD\t0/1:30\n";
        Assert.assertEquals(EXPECTED, vcf);
    }

    /* test writing out a variant PASSing all filters */
    @Test
    public void testWritePassVariant() {
        missenseVariantEvaluation.addFilterResult(passTargetResult);
        sampleData.setGenes(Arrays.asList(gene1));

        String vcf = instance.writeString(analysis, settings);
        final String EXPECTED = EXPECTED_HEADER
                + CHR10_FGFR2_PATHOGENIC_MISSENSE_VARIANT;
        Assert.assertEquals(EXPECTED, vcf);
    }

    /* test writing out a variant failing the target filter */
    @Test
    public void testWriteFailTargetVariant() {
        missenseVariantEvaluation.addFilterResult(failTargetResult);
        sampleData.setGenes(Arrays.asList(gene1));

        String vcf = instance.writeString(analysis, settings);
        final String EXPECTED = EXPECTED_HEADER
                + "chr10\t123353298\t.\tG\tC\t2.20\tTarget\tEXOMISER_GENE=FGFR2;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=1.0;RD=30\tGT:RD\t0/1:30\n";
        Assert.assertEquals(EXPECTED, vcf);
    }

    @Test
    public void testWritePassVariantsOnlyContainsOnlyPassedVariantLine() {
        missenseVariantEvaluation.addFilterResult(passTargetResult);
        indelVariantEvaluation.addFilterResult(failTargetResult);
        sampleData.setGenes(Arrays.asList(gene1, gene2));
        
        OutputSettings outputPassVariantsOnlySettings = new OutputSettingsBuilder()
                .outputFormats(EnumSet.of(OutputFormat.VCF))
                .outputPassVariantsOnly(true)
                .outputPrefix(outPath + "testWrite").build();
        
        String vcf = instance.writeString(analysis, outputPassVariantsOnlySettings);
        final String EXPECTED = EXPECTED_HEADER
                + CHR10_FGFR2_PATHOGENIC_MISSENSE_VARIANT;
        Assert.assertEquals(EXPECTED, vcf);
    }
    
    @Test
    public void testAlternativeAllelesAreWrittenOnSuccessiveLines() {
        TestVariantFactory varFactory = new TestVariantFactory();
        VariantEvaluation alt1 = varFactory.constructVariant(1, 120612040, "T", "TCCGCCG", Genotype.HETEROZYGOUS, 30, 0, 258.62);
        VariantEvaluation alt2 = varFactory.constructVariant(1, 120612040, "T", "TCCTCCGCCG", Genotype.HETEROZYGOUS, 30, 1, 258.62);
        Gene gene = new Gene("TEST", 12345);
        gene.addVariant(alt1);
        gene.addVariant(alt2);
        
        sampleData.setGenes(Arrays.asList(gene));
        String vcf = instance.writeString(analysis, settings);
        System.out.println(vcf);
        final String EXPECTED = EXPECTED_HEADER
                + "chr1\t120612041\t.\tT\tTCCGCCG\t258.62\t.\tEXOMISER_GENE=TEST;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=0.0;RD=30\tGT:RD\t0/1:30\n"
                + "chr1\t120612041\t.\tT\tTCCTCCGCCG\t258.62\t.\tEXOMISER_GENE=TEST;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=0.0;RD=30\tGT:RD\t0/1:30\n";
        Assert.assertEquals(EXPECTED, vcf);
    }
}
