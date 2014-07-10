/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.SampleData;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.util.OutputFormat;
import jannovar.common.Genotype;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VcfResultsWriterTest {

    private final Gene gene;
    private final VcfResultsWriter instance;
    private static final String VARIANT_STRING = "chr1	1	.	A	T	2.2	PASS	GENE=.;PHENO_SCORE=-10.0;VARIANT_SCORE=-10.0;COMBINED_SCORE=-10.0	GT	0/1\n";
    private SampleData sampleData;
    
    public VcfResultsWriterTest() {
        instance = new VcfResultsWriter();
        
        GenotypeCall genotypeCall = new GenotypeCall(Genotype.HETEROZYGOUS, Integer.SIZE);
        byte chr = 1;
        Variant variant = new Variant(chr, 1, "A", "T", genotypeCall, 2.2f);
        VariantEvaluation variantEval = new VariantEvaluation(variant);
        gene = new Gene(variantEval);
    }
    
    @Before
    public void before() {
        sampleData = new SampleData();
        sampleData.setGeneList(new ArrayList<Gene>());
    }
    

    @Test
    public void testWriteFile() {
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outFileName("testWrite").outputFormat(OutputFormat.VCF).build();
        instance.writeFile(sampleData, settings, null, null);
        assertTrue(Paths.get("testWrite.vcf").toFile().exists());
        assertTrue(Paths.get("testWrite.vcf").toFile().delete());
    }

    @Test
    public void testBuildGeneVariantsString() {
        String result = instance.buildGeneVariantsString(gene);
        assertThat(result, equalTo(VARIANT_STRING));
    }

    @Test
    public void testAddColumnField() {
        StringBuilder sb = new StringBuilder();
        String value = "WIBBLE";
        VcfResultsWriter instance = new VcfResultsWriter();
        instance.addColumnField(sb, value);
        String result = sb.toString();
        assertThat(result, equalTo(value + "\t"));
    }

    @Test
    public void testWriteString() {
        List<Gene> geneList = new ArrayList();
        geneList.add(gene);
        sampleData.setGeneList(geneList);
        ExomiserSettings settings = new ExomiserSettings.SettingsBuilder().outputFormat(OutputFormat.VCF).build();
        String result = instance.writeString(sampleData, settings, null, null);
        //there will be a header portion which we're not interested in for this test.
        System.out.println(result);
        assertThat(result, endsWith(VARIANT_STRING));
    }

}
