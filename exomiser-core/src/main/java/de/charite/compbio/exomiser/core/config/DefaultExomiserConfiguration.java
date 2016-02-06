/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.config;

import de.charite.compbio.exomiser.core.prioritisers.util.DataMatrix;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Configuration
@ComponentScan("de.charite.compbio.exomiser.core")
@Import({DataSourceConfig.class, CacheConfig.class})
@PropertySource(value = {"classpath:exomiser.version", "classpath:exomiser.properties"})
public class DefaultExomiserConfiguration {

    Logger logger = LoggerFactory.getLogger(DefaultExomiserConfiguration.class);

    @Autowired
    Environment env;

    @Bean
    public String buildVersion() {
        return env.getProperty("buildVersion");
    }

    @Bean
    public String buildTimestamp() {
        return env.getProperty("buildTimestamp");
    }

    /**
     * This is critical for the application to run as it points to the data
     * directory where all the required resources are found. Without this being
     * correctly set, the application will fail.
     *
     * @return
     */
    @Bean
    public Path dataPath() {
        Path dataPath = Paths.get(env.getProperty("dataDir"));
        logger.info("Root data source directory set to: {}", dataPath.toAbsolutePath());
        return dataPath;
    }

    @Bean
    public Path ucscFilePath() {
        String ucscFileNameValue = getValueOfProperty("ucscFileName");
        Path ucscFilePath = resolveRelativeToDataDir(ucscFileNameValue);
        logger.debug("UCSC data file: {}", ucscFilePath.toAbsolutePath());
        return ucscFilePath;
    }


    private Path resolveRelativeToDataDir(String fileName) {
        return dataPath().resolve(fileName);
    }
    /**
     * This takes a few seconds to de-serialise.
     */
    @Lazy
    @Bean
    public JannovarData jannovarData() {
        try {
            return new JannovarDataSerializer(ucscFilePath().toString()).load();
        } catch (SerializationException e) {
            throw new RuntimeException("Could not load Jannovar data from " + ucscFilePath(), e);
        }
    }

    @Bean
    public Path phenixDataDirectory() {
        String phenixDataDirValue = getValueOfProperty("phenomizerDataDir");
        Path phenixDataDirectory = resolveRelativeToDataDir(phenixDataDirValue);
        logger.debug("phenixDataDirectory: {}", phenixDataDirectory.toAbsolutePath());
        return phenixDataDirectory;
    }

    @Bean
    public Path hpoOntologyFilePath() {
        String hpoOntologyFileValue = getValueOfProperty("hpoOntologyFile");
        Path hpoOntologyFilePath = phenixDataDirectory().resolve(hpoOntologyFileValue);
        logger.debug("hpoOntologyFilePath: {}", hpoOntologyFilePath.toAbsolutePath());
        return hpoOntologyFilePath;
    }

    @Bean
    public Path hpoAnnotationFilePath() {
        String hpoAnnotationFileValue = getValueOfProperty("hpoAnnotationFile");
        Path hpoAnnotationFilePath = phenixDataDirectory().resolve(hpoAnnotationFileValue);
        logger.debug("hpoAnnotationFilePath: {}", hpoAnnotationFilePath.toAbsolutePath());
        return hpoAnnotationFilePath;
    }

    @Lazy
    @Bean
    public TabixReader inDelTabixReader() {
        String caddInDelPathValue = getValueOfProperty("caddInDelPath");
        if (caddInDelPathValue.isEmpty()) {
            caddInDelPathValue = dataPath().resolve("InDels.tsv.gz").toString();
        }
        try {
            return new TabixReader(caddInDelPathValue);
        } catch (IOException e) {
            throw new RuntimeException("CADD InDels.tsv.gz file not found.", e);
        }
    }

    @Lazy
    @Bean
    public TabixReader snvTabixReader() {
        String caddSnvPathValue = getValueOfProperty("caddSnvPath");
        if (caddSnvPathValue.isEmpty()) {
            caddSnvPathValue = resolveRelativeToDataDir("whole_genome_SNVs.tsv.gz").toString();
        }
        try {
            return new TabixReader(caddSnvPathValue);
        } catch (IOException e) {
            throw new RuntimeException("CADD whole_genome_SNVs.tsv.gz file not found.", e);
        }
    }

    @Lazy
    @Bean
    public TabixReader remmTabixReader() {
        String remmPath = getValueOfProperty("remmPath");
        String remmPathValue = resolveRelativeToDataDir(remmPath).toString();
        try {
            return new TabixReader(remmPathValue);
        } catch (IOException e) {
            throw new RuntimeException("REMM file not found ", e);
        }
    }

    /**
     * This needs a lot of RAM and is slow to create from the randomWalkFile, so
     * it's set as lazy use on the command-line.
     *
     * @return
     */
    @Lazy
    @Bean
    public DataMatrix randomWalkMatrix() {
        String randomWalkFileNameValue = getValueOfProperty("randomWalkFileName");
        Path randomWalkFilePath = resolveRelativeToDataDir(randomWalkFileNameValue);

        String randomWalkIndexFileNameValue = getValueOfProperty("randomWalkIndexFileName");
        Path randomWalkIndexFilePath = resolveRelativeToDataDir(randomWalkIndexFileNameValue);

        return new DataMatrix(randomWalkFilePath.toString(), randomWalkIndexFilePath.toString(), true);
    }

    //TODO: this conflicts with the SpringTemplateEngine in the exomiser-web package
    @Bean
    public TemplateEngine templateEngine() {
        TemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setPrefix("html/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(true);
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine;
    }

    private String getValueOfProperty(String property) throws PropertyNotFoundException {
        String value = env.getProperty(property);
        if (value == null) {
            throw new PropertyNotFoundException(String.format("Property '%s' not present in application.properties", property));
        }
        return value;
    }

public class PropertyNotFoundException extends RuntimeException {

    public PropertyNotFoundException(String message) {
        super(message);
    }
}
}
