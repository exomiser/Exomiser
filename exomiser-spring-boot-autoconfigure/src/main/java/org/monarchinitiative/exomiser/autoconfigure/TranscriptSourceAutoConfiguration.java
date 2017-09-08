/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.autoconfigure;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
@Import(DataDirectoryAutoConfiguration.class)
@EnableConfigurationProperties(GenomeProperties.class)
public class TranscriptSourceAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptSourceAutoConfiguration.class);

    private final Path exomiserDataDirectory;
    private final ExomiserProperties properties;

    public TranscriptSourceAutoConfiguration(ExomiserProperties properties, Path exomiserDataDirectory) {
        this.properties = properties;
        this.exomiserDataDirectory = exomiserDataDirectory;
    }

    @Bean
    public Path transcriptFilePath() {
        String transcriptFileNameValue = properties.getTranscriptDataFileName();
        Path transcriptFilePath = exomiserDataDirectory.resolve(transcriptFileNameValue);
        logger.debug("Transcript data file: {}", transcriptFilePath.toAbsolutePath());
        return transcriptFilePath;
    }

    /**
     * This takes a few seconds to de-serialise. Can be overridden by defining your own bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public JannovarData jannovarData(Path transcriptFilePath) {
        try {
            return new JannovarDataSerializer(transcriptFilePath.toString()).load();
        } catch (SerializationException e) {
            throw new ExomiserAutoConfigurationException("Could not load Jannovar data from " + transcriptFilePath, e);
        }
    }

}
