/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.core.factories;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.htsjdk.VariantContextAnnotator;
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
    public JannovarData jannovarData() {
        return new TestJannovarDataFactory().getJannovarData();
    }

    @Bean
    public VariantAnnotator variantAnnotator() {
        JannovarData jannovarData = jannovarData();
        VariantContextAnnotator variantContextAnnotator = new VariantContextAnnotator(jannovarData.getRefDict(), jannovarData.getChromosomes());
        return new VariantAnnotator(variantContextAnnotator);
    }

    @Bean
    public VariantFactory variantFactory() {
        return new VariantFactory(variantAnnotator());
    }

}
