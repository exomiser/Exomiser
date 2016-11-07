package org.monarchinitiative.exomiser.autoconfigure;

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import htsjdk.tribble.readers.TabixReader;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to provide a stub classes for exomiser beans which require on-disk files to operate on.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class ExomiserStubDataConfig {

    @Bean
    public JannovarData jannovarData() {
        return new JannovarData(HG19RefDictBuilder.build(), ImmutableList.of());
    }

    /**
     * Provides a mock TabixReader in place of a TabixReader for a specific tabix file.
     * @return a mock TabixReader
     */
    @Bean
    public TabixReader inDelTabixReader() {
        return Mockito.mock(TabixReader.class);
    }

    @Bean
    public TabixReader snvTabixReader() {
        return Mockito.mock(TabixReader.class);
    }

    @Bean
    public TabixReader remmTabixReader() {
        return Mockito.mock(TabixReader.class);
    }

}
