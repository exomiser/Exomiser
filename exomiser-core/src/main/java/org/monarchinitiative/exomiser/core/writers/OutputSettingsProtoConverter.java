/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

import com.google.common.collect.Sets;
import org.monarchinitiative.exomiser.api.v1.OutputProto;
import org.monarchinitiative.exomiser.core.proto.ProtoConverter;

import java.util.stream.Collectors;

/**
 * Class for inter-converting output settings/options between the domain and proto formats.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 * @since 13.0.0
 */
public class OutputSettingsProtoConverter implements ProtoConverter<OutputSettings, OutputProto.OutputOptions> {

    @Override
    public OutputProto.OutputOptions toProto(OutputSettings outputOptions) {
        return OutputProto.OutputOptions.newBuilder()
                .setOutputPrefix(outputOptions.getOutputPrefix())
                .setNumGenes(outputOptions.getNumberOfGenesToShow())
                .setMinExomiserGeneScore(outputOptions.getMinExomiserGeneScore())
                .setOutputContributingVariantsOnly(outputOptions.outputContributingVariantsOnly())
                .addAllOutputFormats(outputOptions
                        .getOutputFormats()
                        .stream()
                        .map(OutputFormat::toString)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public OutputSettings toDomain(OutputProto.OutputOptions outputOptions) {
        return OutputSettings.builder()
                .outputPrefix(outputOptions.getOutputPrefix())
                .numberOfGenesToShow(outputOptions.getNumGenes())
                .minExomiserGeneScore(outputOptions.getMinExomiserGeneScore())
                .outputContributingVariantsOnly(outputOptions.getOutputContributingVariantsOnly())
                .outputFormats(outputOptions
                        .getOutputFormatsList().stream()
                        .map(OutputFormat::parseFormat)
                        .collect(Sets.toImmutableEnumSet()))
                .build();
    }
}
