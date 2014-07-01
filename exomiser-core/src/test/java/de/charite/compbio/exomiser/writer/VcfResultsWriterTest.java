/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.writer;

import de.charite.compbio.exomiser.core.SampleData;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.Builder;
import jannovar.common.Genotype;
import jannovar.exome.Variant;
import jannovar.genotype.GenotypeCall;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VcfResultsWriterTest {

    Gene gene;
    VcfResultsWriter instance;

    public VcfResultsWriterTest() {
        instance = new VcfResultsWriter();
        
        GenotypeCall genotypeCall = new GenotypeCall(Genotype.HETEROZYGOUS, Integer.SIZE);
        byte chr = 1;
        Variant variant = new Variant(chr, 1, "A", "T", genotypeCall, 2.2f);
        VariantEvaluation variantEval = new VariantEvaluation(variant);
        gene = new Gene(variantEval);
    }

    @Test
    public void testWrite() {
        SampleData sampleData = new SampleData();
        sampleData.setGeneList(new ArrayList<Gene>());
        ExomiserSettings settings = new ExomiserSettings.Builder().outFileName("testWrite.vcf").build();
        List<Filter> filterList = null;
        List<Priority> priorityList = null;
        instance.write(sampleData, settings, filterList, priorityList);
        assertTrue(Paths.get("testWrite.vcf").toFile().exists());
        assertTrue(Paths.get("testWrite.vcf").toFile().delete());
    }

    @Test
    public void testBuildGeneVariantsString() {
        String expResult = "chr1	1	.	A	T	2.2	PASS	GENE=.;PHENO_SCORE=-10.0;VARIANT_SCORE=-10.0;COMBINED_SCORE=-10.0	GT	0/1\n";
        String result = instance.buildGeneVariantsString(gene);
        assertThat(result, equalTo(expResult));
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

}
