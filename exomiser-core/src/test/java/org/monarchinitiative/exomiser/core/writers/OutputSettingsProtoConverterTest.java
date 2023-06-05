/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.writers;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.api.v1.OutputProto;

import java.nio.file.Path;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OutputSettingsProtoConverterTest {

    private final OutputSettingsProtoConverter instance = new OutputSettingsProtoConverter();

    private final OutputProto.OutputOptions proto = OutputProto.OutputOptions.newBuilder()
            .setOutputContributingVariantsOnly(true)
            .setNumGenes(10)
            .setOutputFileName("frood")
            .setOutputDirectory("hoopy")
            .addOutputFormats(OutputFormat.HTML.toString())
            .addOutputFormats(OutputFormat.JSON.toString())
            .build();

    private final OutputSettings domain = OutputSettings.builder()
            .outputContributingVariantsOnly(true)
            .numberOfGenesToShow(10)
            .outputPrefix("hoopy/frood")
            .outputFileName("frood")
            .outputDirectory(Path.of("hoopy"))
            .outputFormats(Set.of(OutputFormat.HTML, OutputFormat.JSON))
            .build();

    @Test
    void toProto() {
        assertThat(instance.toProto(domain), equalTo(proto));
    }

    @Test
    void toDomain() {
        assertThat(instance.toDomain(proto), equalTo(domain));
    }

    @Test
    void toDomainWhereNoDirectoryIsSetReturnsDefaultDir() {
        OutputProto.OutputOptions emptyOutputDir = OutputProto.OutputOptions.newBuilder()
                .setOutputContributingVariantsOnly(true)
                .setNumGenes(10)
                .setOutputDirectory("")
                .setOutputFileName("frood")
                .addOutputFormats(OutputFormat.HTML.toString())
                .addOutputFormats(OutputFormat.JSON.toString())
                .build();

        OutputSettings domain = OutputSettings.builder()
                .outputContributingVariantsOnly(true)
                .numberOfGenesToShow(10)
                .outputDirectory(OutputSettings.DEFAULT_OUTPUT_DIR)
                .outputFileName("frood")
                .outputFormats(Set.of(OutputFormat.HTML, OutputFormat.JSON))
                .build();
        assertThat(instance.toDomain(emptyOutputDir), equalTo(domain));
    }
}