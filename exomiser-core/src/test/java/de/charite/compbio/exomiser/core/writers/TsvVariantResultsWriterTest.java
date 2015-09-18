/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.writers;

import de.charite.compbio.exomiser.core.Analysis;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.EnumSet;

import de.charite.compbio.exomiser.core.model.pathogenicity.PathogenicityData;
import de.charite.compbio.exomiser.core.model.pathogenicity.PolyPhenScore;
import org.junit.Before;
import org.junit.Test;

import de.charite.compbio.exomiser.core.factories.TestVariantFactory;
import de.charite.compbio.exomiser.core.filters.FailFilterResult;
import de.charite.compbio.exomiser.core.filters.FilterType;
import de.charite.compbio.exomiser.core.filters.PassFilterResult;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.SampleData;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.exomiser.core.writers.OutputSettingsImp.OutputSettingsBuilder;
import de.charite.compbio.jannovar.pedigree.Genotype;
import java.util.Arrays;

/**
 *
 * @author Max Schubach <max.schubach@charite.de>
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class TsvVariantResultsWriterTest {

    private TsvVariantResultsWriter instance;
    
    private static final String VARIANT_DETAILS_HEADER = "#CHROM\tPOS\tREF\tALT\tQUAL\tFILTER\tGENOTYPE\tCOVERAGE\tFUNCTIONAL_CLASS\tHGVS\tEXOMISER_GENE\t";
    private static final String PATHOGENICITY_SCORES_HEADER = "CADD(>0.483)\tPOLYPHEN(>0.956|>0.446)\tMUTATIONTASTER(>0.94)\tSIFT(<0.06)\t";
    private static final String FREQUENCY_DATA_HEADER =  "DBSNP_ID\tMAX_FREQUENCY\tDBSNP_FREQUENCY\t"
            + "EVS_EA_FREQUENCY\tEVS_AA_FREQUENCY\t"
            + "EXAC_AFR_FREQ\tEXAC_AMR_FREQ\tEXAC_EAS_FREQ\tEXAC_FIN_FREQ\tEXAC_NFE_FREQ\tEXAC_SAS_FREQ\tEXAC_OTH_FREQ\t";
    private static final String EXOMISER_SCORES_HEADER = 
            "EXOMISER_VARIANT_SCORE\tEXOMISER_GENE_PHENO_SCORE\tEXOMISER_GENE_VARIANT_SCORE\tEXOMISER_GENE_COMBINED_SCORE\n";
    
    private static final String HEADER = VARIANT_DETAILS_HEADER + PATHOGENICITY_SCORES_HEADER + FREQUENCY_DATA_HEADER + EXOMISER_SCORES_HEADER;
    
    private static final String PASS_VARIANT_DETAILS = "chr10\t123353298\tG\tC\t2.2\tPASS\t0/1\t0\tmissense_variant\tFGFR2:uc021pzz.1:exon2:c.34C>G:p.(Leu12Val)\tFGFR2";
    private static final String FAIL_VARIANT_DETAILS = "chr7\t155604801\tC\tCTT\t1.0\tTarget\t0/1\t0\tframeshift_variant\tSHH:uc003wmk.1:exon1:c.16_17insAA:p.(Arg6Lysfs*6)\tSHH";
    private static final String NO_PATH_SCORES = "\t.\t.\t.\t.";
    private static final String NO_FREQUENCY_DATA = "\t.\t0.0\t.\t.\t.\t.\t.\t.\t.\t.\t.\t.";
    private static final String PASS_VARIANT_EXOMISER_SCORES = "\t1.0\t0.0\t0.0\t0.0\n";
    private static final String FAIL_VARIANT_EXOMISER_SCORES = "\t0.95\t0.0\t0.0\t0.0\n";

    private static final String PASS_VARIANT_LINE = PASS_VARIANT_DETAILS + "\t.\t1.0\t.\t." + NO_FREQUENCY_DATA + PASS_VARIANT_EXOMISER_SCORES;
    private static final String FAIL_VARIANT_LINE = FAIL_VARIANT_DETAILS + NO_PATH_SCORES + NO_FREQUENCY_DATA + FAIL_VARIANT_EXOMISER_SCORES;

    private OutputSettingsBuilder settingsBuilder;
    private Analysis analysis;
    private SampleData sampleData;
    private Gene gene;
    private VariantEvaluation passVariant;
    private VariantEvaluation failVariant;
    
    @Before
    public void before() {
        instance = new TsvVariantResultsWriter();
        settingsBuilder = new OutputSettingsBuilder().outputFormats(EnumSet.of(OutputFormat.TSV_VARIANT));
        
        TestVariantFactory varFactory = new TestVariantFactory();
        makePassVariant(varFactory);
        makeFailVariant(varFactory);
        
        gene = new Gene(passVariant.getGeneSymbol(), passVariant.getEntrezGeneId());
        gene.addVariant(passVariant);
        gene.addVariant(failVariant);

        sampleData = new SampleData();
        sampleData.setGenes(Arrays.asList(gene));
        
        analysis = new Analysis();
        analysis.setSampleData(sampleData);
    }

    private void makePassVariant(TestVariantFactory varFactory) {
        passVariant = varFactory.constructVariant(10, 123353297, "G", "C", Genotype.HETEROZYGOUS, 30, 0, 2.2);
        passVariant.addFilterResult(new PassFilterResult(FilterType.VARIANT_EFFECT_FILTER));
        passVariant.setPathogenicityData(new PathogenicityData(new PolyPhenScore(1f)));
    }
    
    private void makeFailVariant(TestVariantFactory varFactory) {
        failVariant = varFactory.constructVariant(7, 155604800, "C", "CTT", Genotype.HETEROZYGOUS, 30, 0, 1.0);
        failVariant.addFilterResult(new FailFilterResult(FilterType.VARIANT_EFFECT_FILTER));
    }

    @Test
    public void testWrite() {
        OutputSettings settings = settingsBuilder.outputPrefix("testWrite").build();
        instance.writeFile(analysis, settings);
        assertTrue(Paths.get("testWrite.variants.tsv").toFile().exists());
        assertTrue(Paths.get("testWrite.variants.tsv").toFile().delete());
    }

    @Test
    public void testWriteStringContainsAllVariants() {
        OutputSettings settings = settingsBuilder.build();
        String outString = instance.writeString(analysis, settings);
        String expected = HEADER
                + PASS_VARIANT_LINE
                + FAIL_VARIANT_LINE;
        assertThat(outString, equalTo(expected));
    }
    
    @Test
    public void testWritePassVariantsOnlyStringContainsOnlyPassedVariants() {
        OutputSettings settings = settingsBuilder.outputPassVariantsOnly(true).build();
        String outString = instance.writeString(analysis, settings);
        String expected = HEADER +
                PASS_VARIANT_LINE;
        assertThat(outString, equalTo(expected));
    }

}
