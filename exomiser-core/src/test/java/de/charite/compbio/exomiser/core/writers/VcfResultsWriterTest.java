package de.charite.compbio.exomiser.core.writers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

import htsjdk.variant.vcf.VCFFileReader;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.dao.TestVariantFactory;
import de.charite.compbio.exomiser.core.filters.FilterResult;
import de.charite.compbio.exomiser.core.filters.FilterResultStatus;
import de.charite.compbio.exomiser.core.filters.FrequencyFilterResult;
import de.charite.compbio.exomiser.core.filters.TargetFilterResult;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.prioritisers.OMIMPriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PhivePriorityResult;
import de.charite.compbio.exomiser.core.prioritisers.PriorityType;
import de.charite.compbio.jannovar.pedigree.Genotype;

/**
 * Tests for the {@link VCFResultsWriter} class.
 * 
 * The {@link VCFResultsWriter} class needs a {@link VCFFileReader} for building its header. Thus, we base our output
 * {@link VCFResultsWriter} on the minimal.vcf file from the test resources.
 * 
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 */
@RunWith(MockitoJUnitRunner.class)
public class VcfResultsWriterTest {

    final static String EXPECTED_HEADER = "##fileformat=VCFv4.1\n"
            + "##FILTER=<ID=BED_FILTER,Description=\"Gene panel target region (Bed filter)\">\n"
            + "##FILTER=<ID=ENTREZ_GENE_ID_FILTER,Description=\"Genes to keep\">\n"
            + "##FILTER=<ID=FREQUENCY_FILTER,Description=\"Frequency\">\n"
            + "##FILTER=<ID=INHERITANCE_FILTER,Description=\"Inheritance\">\n"
            + "##FILTER=<ID=INTERVAL_FILTER,Description=\"Interval\">\n"
            + "##FILTER=<ID=PATHOGENICITY_FILTER,Description=\"Pathogenicity\">\n"
            + "##FILTER=<ID=QUALITY_FILTER,Description=\"Quality\">\n"
            + "##FILTER=<ID=TARGET_FILTER,Description=\"Target\">\n"
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

    /** VCF reader, initialized from minimal.vcf (that only contains a header). */
    private static VCFFileReader reader;

    /** The instance under test. */
    private VcfResultsWriter instance;

    /** The temporary folder to write files to, automatically removed after tests finish. */
    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    /** The currently used output path. */
    private Path outPath;

    /** The current settings to use. */
    private ExomiserSettings settings;

    /** Sample data used for testing. */
    private SampleData sampleData;

    /** VariantEvaluation objects used for testing (annotated ones). */
    private VariantEvaluation missenseVariantEvaluation;
    private VariantEvaluation indelVariantEvaluation;

    /** VariantEvaluation objects used for testing (unannotated ones). */
    private VariantEvaluation unAnnotatedVariantEvaluation1;
    private VariantEvaluation unAnnotatedVariantEvaluation2;

    /** Genes used for testing */
    private Gene gene1, gene2;

    /** Filters used for testing. */
    FilterResult passTargetResult, failTargetResult, failFrequencyResult;

    @BeforeClass
    public static void loadVCFHeader() throws URISyntaxException {
        final String inputFilePath = VcfResultsWriterTest.class.getResource("/minimal.vcf").toURI().getPath();
        reader = new VCFFileReader(new File(inputFilePath), false); // no index required
    }

    @Before
    public void setUp() throws IOException {
        outPath = tmpFolder.newFile().toPath();
        settings = new ExomiserSettings.SettingsBuilder()
                .vcfFilePath(outPath)
                .usePrioritiser(PriorityType.OMIM_PRIORITY)
                .outputFormats(EnumSet.of(OutputFormat.VCF))
                .outputPrefix("testWrite").build();

        instance = new VcfResultsWriter(reader.getFileHeader());

        setUpModel();
    }

    private void setUpModel() {
        sampleData = new SampleData();
        sampleData.setGenes(new ArrayList<Gene>());

        TestVariantFactory varFactory = new TestVariantFactory();

        passTargetResult = new TargetFilterResult(1f, FilterResultStatus.PASS);
        failTargetResult = new TargetFilterResult(0f, FilterResultStatus.FAIL);
        failFrequencyResult = new FrequencyFilterResult(0f, FilterResultStatus.FAIL);

        missenseVariantEvaluation = varFactory.constructVariant(10, 123353297, "G", "C", Genotype.HETEROZYGOUS, 30, 0, 2.2);
        indelVariantEvaluation = varFactory.constructVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);

        gene1 = new Gene(missenseVariantEvaluation.getGeneSymbol(), missenseVariantEvaluation.getEntrezGeneId());
        gene1.addVariant(missenseVariantEvaluation);
        
        gene2 = new Gene(indelVariantEvaluation.getGeneSymbol(), indelVariantEvaluation.getEntrezGeneId());
        gene2.addVariant(indelVariantEvaluation);

        gene1.addPriorityResult(new PhivePriorityResult("MGI:12345", "Gene1", 0.99f));
        gene2.addPriorityResult(new PhivePriorityResult("MGI:54321", "Gene2", 0.98f));

        OMIMPriorityResult gene1PriorityScore = new OMIMPriorityResult();
        gene1PriorityScore.addRow("OMIM:12345", "OMIM:67890", "Disease syndrome", 'D', 'D', 1f);
        gene1.addPriorityResult(gene1PriorityScore);
        gene2.addPriorityResult(new OMIMPriorityResult());

        unAnnotatedVariantEvaluation1 = new VariantEvaluation.VariantBuilder(5, 11, "AC", "AT").quality(1).build();
        unAnnotatedVariantEvaluation2 = new VariantEvaluation.VariantBuilder(5, 14, "T", "TG").quality(1).build();
    }

    /* test that the extended header is written out properly */
    @Test
    public void testWriteHeaderFile() {
        Assert.assertEquals(EXPECTED_HEADER, instance.writeString(sampleData, settings));
    }

    /* test writing out unannotated variants */
    @Test
    public void testWriteUnannotatedVariants() {       
        sampleData.setVariantEvaluations(Arrays.asList(unAnnotatedVariantEvaluation1, unAnnotatedVariantEvaluation2));
        
        String vcf = instance.writeString(sampleData, settings);
        final String EXPECTED = EXPECTED_HEADER
                + "chr5\t11\t.\tAC\tAT\t1\t.\tEXOMISER_WARNING=VARIANT_NOT_ANALYSED_NO_GENE_ANNOTATIONS\tGT\t0/1\n"
                + "chr5\t14\t.\tT\tTG\t1\t.\tEXOMISER_WARNING=VARIANT_NOT_ANALYSED_NO_GENE_ANNOTATIONS\tGT\t0/1\n";
        Assert.assertEquals(EXPECTED, vcf);
    }

    /* test writing out annotated variants in two genes */
    @Test
    public void testWriteAnnotatedVariants() {
        sampleData.setGenes(Arrays.asList(gene1, gene2));

        String vcf = instance.writeString(sampleData, settings);
        final String EXPECTED = EXPECTED_HEADER
                + "chr10\t123353298\t.\tG\tC\t2.20\t.\tEXOMISER_GENE=FGFR2;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=1.0;RD=30\tGT:RD\t0/1:30\n"
                + "chr7\t155604801\t.\tC\tCTT\t1\t.\tEXOMISER_GENE=SHH;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=1.0;RD=30\tGT:RD\t0/1:30\n";
        Assert.assertEquals(EXPECTED, vcf);
    }

    /* test writing out a variant PASSing all filters */
    @Test
    public void testWritePassVariant() {
        missenseVariantEvaluation.addFilterResult(passTargetResult);
        sampleData.setGenes(Arrays.asList(gene1));

        String vcf = instance.writeString(sampleData, settings);
        final String EXPECTED = EXPECTED_HEADER
                + "chr10\t123353298\t.\tG\tC\t2.20\tPASS\tEXOMISER_GENE=FGFR2;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=1.0;RD=30\tGT:RD\t0/1:30\n";
        Assert.assertEquals(EXPECTED, vcf);
    }

    /* test writing out a variant failing the frequency filter filters */
    @Test
    public void testWriteFailFrequencyVariant() {
        missenseVariantEvaluation.addFilterResult(failFrequencyResult);
        sampleData.setGenes(Arrays.asList(gene1));

        String vcf = instance.writeString(sampleData, settings);
        final String EXPECTED = EXPECTED_HEADER
                + "chr10\t123353298\t.\tG\tC\t2.20\tFrequency\tEXOMISER_GENE=FGFR2;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=0.0;RD=30\tGT:RD\t0/1:30\n";
        Assert.assertEquals(EXPECTED, vcf);
    }

    /* test writing out a variant failing the target filter */
    @Test
    public void testWriteFailTargetVariant() {
        missenseVariantEvaluation.addFilterResult(failTargetResult);
        sampleData.setGenes(Arrays.asList(gene1));

        String vcf = instance.writeString(sampleData, settings);
        final String EXPECTED = EXPECTED_HEADER
                + "chr10\t123353298\t.\tG\tC\t2.20\tTarget\tEXOMISER_GENE=FGFR2;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=0.0;RD=30\tGT:RD\t0/1:30\n";
        Assert.assertEquals(EXPECTED, vcf);
    }

    @Test
    public void testWritePassVariantsOnlyContainsOnlyPassedVariantLine() {
        missenseVariantEvaluation.addFilterResult(passTargetResult);
        indelVariantEvaluation.addFilterResult(failTargetResult);
        sampleData.setGenes(Arrays.asList(gene1, gene2));
        
        ExomiserSettings outputPassVariantsOnlySettings = new ExomiserSettings.SettingsBuilder()
                .vcfFilePath(outPath)
                .outputFormats(EnumSet.of(OutputFormat.VCF))
                .outputPassVariantsOnly(true)
                .outputPrefix("testWrite").build();
        
        String vcf = instance.writeString(sampleData, outputPassVariantsOnlySettings);
        final String EXPECTED = EXPECTED_HEADER
                + "chr10\t123353298\t.\tG\tC\t2.20\tPASS\tEXOMISER_GENE=FGFR2;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=1.0;RD=30\tGT:RD\t0/1:30\n";
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
        String vcf = instance.writeString(sampleData, settings);
        System.out.println(vcf);
        final String EXPECTED = EXPECTED_HEADER
                + "chr1\t120612041\t.\tT\tTCCGCCG\t258.62\t.\tEXOMISER_GENE=TEST;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=1.0;RD=30\tGT:RD\t0/1:30\n"
                + "chr1\t120612041\t.\tT\tTCCTCCGCCG\t258.62\t.\tEXOMISER_GENE=TEST;EXOMISER_GENE_COMBINED_SCORE=0.0;EXOMISER_GENE_PHENO_SCORE=0.0;EXOMISER_GENE_VARIANT_SCORE=0.0;EXOMISER_VARIANT_SCORE=1.0;RD=30\tGT:RD\t0/1:30\n";
        Assert.assertEquals(EXPECTED, vcf);
    }
}
