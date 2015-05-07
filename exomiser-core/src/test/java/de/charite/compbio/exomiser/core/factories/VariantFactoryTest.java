/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.data.JannovarData;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantFactoryTest {
    
    private VariantFactory instance;
    
    private final JannovarData jannovarData = new TestJannovarDataFactory().getJannovarData();
    
    @Before
    public void setUp() {
        VariantAnnotationsFactory variantAnnotator = new VariantAnnotationsFactory(jannovarData);
        instance = new VariantFactory(variantAnnotator);
    }

    private VCFFileReader makeVcfFileReader(String vcfFilePath) {
        File vcfFile = new File(vcfFilePath);
        // open VCF file (will read header)
        VCFFileReader vcfFileReader = new VCFFileReader(vcfFile, false); // false => do not require index
        return vcfFileReader;
    }

    private void printVariant(Variant variant) {
        System.out.printf("%s offExome=%s gene=%s%n", variant.getChromosomalVariant(), variant.isOffExome(), variant.getGeneSymbol());
    }
    
    @Test
    public void testCreateVariantContexts_SingleAlleles() {
        VCFFileReader vcfFileReader = makeVcfFileReader("src/test/resources/smallTest.vcf");
        List<VariantContext> variants = instance.createVariantContexts(vcfFileReader);
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(3));
    }
    
    @Test
    public void testCreateVariantContexts_MultipleAlleles() {
        VCFFileReader vcfFileReader = makeVcfFileReader("src/test/resources/altAllele.vcf"); 
        List<VariantContext> variants = instance.createVariantContexts(vcfFileReader);
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(1));
    }

    @Test
    public void testCreateVariants_SingleAlleles() {
        VCFFileReader vcfFileReader = makeVcfFileReader("src/test/resources/smallTest.vcf");
        List<VariantEvaluation> variants = instance.createVariantEvaluations(vcfFileReader);
        for (Variant variant : variants) {
            printVariant(variant);
        }
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(3));
        
    }

    @Test
    public void testCreateVariants_MultipleAllelesProduceOneVariantPerAllele() {
        VCFFileReader vcfFileReader = makeVcfFileReader("src/test/resources/altAllele.vcf");
        List<VariantEvaluation> variants = instance.createVariantEvaluations(vcfFileReader);
        for (Variant variant : variants) {
            System.out.println(variant);
            printVariant(variant);
        }
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(2));
    }
    
    @Test
    public void testCreateVariants_NoVariantAnnotationsProduceNoVariantEvaluations() {
        VCFFileReader vcfFileReader = makeVcfFileReader("src/test/resources/noAnnotations.vcf");
        List<VariantEvaluation> variants = instance.createVariantEvaluations(vcfFileReader);
        assertThat(variants.isEmpty(), is(true));        
    }
    
}
