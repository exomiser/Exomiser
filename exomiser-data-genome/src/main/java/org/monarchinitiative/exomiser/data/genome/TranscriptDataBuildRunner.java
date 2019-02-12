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

import com.google.common.collect.ImmutableList;
import de.charite.compbio.jannovar.data.JannovarData;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataProtoSerialiser;
import org.monarchinitiative.exomiser.core.genome.jannovar.JannovarDataSourceLoader;
import org.monarchinitiative.exomiser.data.genome.model.BuildInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

//import de.charite.compbio.jannovar.Jannovar;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TranscriptDataBuildRunner {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptDataBuildRunner.class);

    private final BuildInfo buildInfo;
    private final Path outPath;

    public TranscriptDataBuildRunner(BuildInfo buildInfo, Path outPath) {
        this.buildInfo = buildInfo;
        this.outPath = outPath;
    }

    public void run() {
        // build jannovar - can this be done via an API?
        // zip build archive
        // this should work but there are logging conflicts and it simply fails without an error
        logger.info("Building Jannovar data... ");
        String[] arguments = new String[] {"download",  "-d", "hg19/refseq"};
        // or $ java -jar jannovar-cli-0.26.jar download -d hg19/refseq -d hg19/ucsc
//        Jannovar.main(arguments);
        // need to download then hack the hg19 data to update the Ensembl ids
        // see https://github.com/exomiser/Exomiser/issues/253
        // Finally, convert to new proto format:
        List<String> resourcesNames = ImmutableList.of("ensembl", "refseq", "ucsc");

        GenomeAssembly assembly = buildInfo.getAssembly();
        String buildString = buildInfo.getBuildString();

        resourcesNames.parallelStream().forEach(resourceName -> {
            logger.info("Converting {}_{}", assembly, resourceName);
            String inputName = String.format("%s_%s.ser", assembly, resourceName);
            JannovarData jannovarData = JannovarDataSourceLoader.loadJannovarData(Paths.get(inputName));
            String outputName = String.format("%s_transcripts_%s.ser", buildString, resourceName);
            JannovarDataProtoSerialiser.save(Paths.get(outputName), jannovarData);
        });
    }
}
