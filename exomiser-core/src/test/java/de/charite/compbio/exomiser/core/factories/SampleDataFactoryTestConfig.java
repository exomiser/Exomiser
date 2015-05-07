/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.exomiser.core.dao.TestJannovarDataFactory;
import de.charite.compbio.jannovar.data.JannovarData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jj8
 */
@Configuration
public class SampleDataFactoryTestConfig {

    @Bean
    public SampleDataFactory SampleDataFactory() {
        return new SampleDataFactory();
    }

    @Bean
    public VariantAnnotationsFactory variantAnnotator() {
        final JannovarData jannovarData = new TestJannovarDataFactory().getJannovarData();

//        VariantAnnotationsFactory mockVariantAnnotator = Mockito.mock(VariantAnnotationsFactory.class);
//        final TestVariantFactory testVariantFactory = new TestVariantFactory();
//        // variantAnnotator to return dummy Variant objects
//        Mockito.when(mockVariantAnnotator.buildVariantAnnotations(isNotNull(VariantContext.class))).thenAnswer(
//                new Answer<List<Variant>>() {
//                    @Override
//                    public List<Variant> answer(InvocationOnMock invocation) {
//                        return Arrays.asList(testVariantFactory.constructVariant(10, 123256213, "CA", "CT", Genotype.HETEROZYGOUS, 22, 0));
//                    }
//                });

        return new VariantAnnotationsFactory(jannovarData);
    }

    @Bean
    public VariantFactory variantFactory() {
        return new VariantFactory(variantAnnotator());
    }

}
