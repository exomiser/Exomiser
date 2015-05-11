/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.jannovar.data.JannovarData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
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
        return new VariantAnnotationsFactory(jannovarData);
    }

    @Bean
    public VariantFactory variantFactory() {
        return new VariantFactory(variantAnnotator());
    }

}
