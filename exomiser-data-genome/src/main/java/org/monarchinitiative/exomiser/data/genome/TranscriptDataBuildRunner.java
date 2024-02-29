/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome;

import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataFactory;
import org.monarchinitiative.exomiser.core.genome.jannovar.TranscriptSource;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TranscriptDataBuildRunner {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptDataBuildRunner.class);

    private final BuildInfo buildInfo;
    private final JannovarDataFactory jannovarDataFactory;
    private final Path outPath;
    private final List<TranscriptSource> transcriptSources;

    public TranscriptDataBuildRunner(BuildInfo buildInfo, JannovarDataFactory jannovarDataFactory, Path outPath, List<TranscriptSource> transcriptSources) {
        this.buildInfo = buildInfo;
        this.jannovarDataFactory = jannovarDataFactory;
        this.outPath = outPath;
        this.transcriptSources = transcriptSources;
    }

    public static String transcriptFileName(BuildInfo buildInfo, TranscriptSource transcriptSource) {
        return buildInfo.getBuildString() + "_transcripts_" + transcriptSource + ".ser";
    }

    public void run() {
        transcriptSources.forEach(transcriptSource -> {
            String outputName = transcriptFileName(buildInfo, transcriptSource);
            logger.info("Building {}", outputName);
            jannovarDataFactory.buildAndWrite(buildInfo.getAssembly(), transcriptSource, outPath.resolve(outputName));
        });
    }
}
