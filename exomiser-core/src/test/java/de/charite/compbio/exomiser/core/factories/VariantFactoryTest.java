/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.model.Variant;
import de.charite.compbio.exomiser.core.model.VariantEvaluation;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
import htsjdk.variant.variantcontext.VariantContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;
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

    private final JannovarData jannovarData;
    private final VariantContextAnnotator variantContextAnnotator;
    private final VariantAnnotator variantAnnotator;

    public VariantFactoryTest() {
        jannovarData = new TestJannovarDataFactory().getJannovarData();
        variantContextAnnotator = new VariantContextAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes());
        variantAnnotator = new VariantAnnotator(variantContextAnnotator);
    }

    @Before
    public void setUp() {
        instance = new VariantFactory(variantAnnotator);
    }

    private void printVariant(Variant variant) {
        System.out.printf("%s offExome=%s gene=%s%n", variant.getChromosomalVariant(), variant.isOffExome(), variant.getGeneSymbol());
    }

    @Test
    public void testCreateVariantContexts_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        List<VariantContext> variants = instance.createVariantContexts(vcfPath);
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(3));
    }

    @Test
    public void testStreamVariantContexts_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        List<VariantContext> variants = instance.streamVariantContexts(vcfPath)
                .filter(variantContext -> (variantContext.getChr().equals("1")))
                .collect(toList());

        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(3));
    }

    @Test
    public void testStreamCreateVariants_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        List<VariantEvaluation> variants = instance.streamVariantEvaluations(vcfPath).collect(toList());
        variants.forEach(this::printVariant);
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(3));

    }

    @Test
    public void testCreateVariantContexts_MultipleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/altAllele.vcf");
        List<VariantContext> variants = instance.createVariantContexts(vcfPath);
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(1));
    }

    @Test
    public void testCreateVariants_SingleAlleles() {
        Path vcfPath = Paths.get("src/test/resources/smallTest.vcf");
        List<VariantEvaluation> variants = instance.createVariantEvaluations(vcfPath);
        variants.forEach(this::printVariant);
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(3));

    }

    @Test
    public void testCreateVariants_MultipleAllelesProduceOneVariantPerAllele() {
        Path vcfPath = Paths.get("src/test/resources/altAllele.vcf");
        List<VariantEvaluation> variants = instance.createVariantEvaluations(vcfPath);
        variants.forEach(this::printVariant);
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(2));
    }

    @Test
    public void testCreateVariants_NoVariantAnnotationsProduceVariantEvaluationsWithNoAnnotations() {
        Path vcfPath = Paths.get("src/test/resources/noAnnotations.vcf");
        List<VariantEvaluation> variants = instance.createVariantEvaluations(vcfPath);
        assertThat(variants.isEmpty(), is(false));
        assertThat(variants.size(), equalTo(2));

        for (VariantEvaluation variant : variants) {
            System.out.println(variant.getChromosomeName() + " " + variant);
            assertThat(variant.hasAnnotations(), is(false));
        }
    }

}
