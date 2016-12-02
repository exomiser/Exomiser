package org.monarchinitiative.exomiser.test;

import com.google.common.collect.ImmutableList;
import com.zaxxer.hikari.HikariConfig;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import htsjdk.tribble.readers.TabixReader;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to provide a stub classes for exomiser beans which require on-disk files to operate on.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class ExomiserStubDataConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExomiserStubDataConfig.class);

    @Bean
    public HikariConfig h2Config() {
        logger.info("Creating in memory H2 databasegit stash");
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:exomiser");
        config.setMaximumPoolSize(3);
        config.setPoolName("exomiser-H2-mem");
        return config;
    }

    @Bean
    public JannovarData jannovarData() {
        logger.info("Creating stub Jannovar data");
        return new JannovarData(HG19RefDictBuilder.build(), ImmutableList.of());
    }

    /**
     * Provides a mock TabixReader in place of a TabixReader for a specific tabix file.
     * @return a mock TabixReader
     */
    @Bean
    public TabixReader inDelTabixReader() {
        logger.info("Mocking inDelTabixReader");
        return Mockito.mock(TabixReader.class);
    }

    @Bean
    public TabixReader snvTabixReader() {
        logger.info("Mocking snvTabixReader");
        return Mockito.mock(TabixReader.class);
    }

    @Bean
    public TabixReader remmTabixReader() {
        logger.info("Mocking remmTabixReader");
        return Mockito.mock(TabixReader.class);
    }

}
