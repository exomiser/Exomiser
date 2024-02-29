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

package org.monarchinitiative.exomiser.data.genome.model.resource.sv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.data.genome.indexers.OutputFileIndexer;
import org.monarchinitiative.exomiser.data.genome.model.archive.FileArchive;
import org.monarchinitiative.exomiser.data.genome.model.parsers.sv.ClinVarSvParser;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class ClinVarSvResourceTest {

    @Test
    public void name(@TempDir Path tempDir) throws Exception {

        Path outfilePath = tempDir.resolve("clinvar-sv-test.tsv");

        ClinVarSvResource clinVarSvResource = new ClinVarSvResource("hg19.clinvar-sv",
                new URL("https://"),
                new FileArchive(Path.of("src/test/resources/genome/clinvar_test.txt.gz")),
                new ClinVarSvParser(GenomeAssembly.HG19),
                new OutputFileIndexer<>(outfilePath)
        );

        // do the magic
        clinVarSvResource.indexResource();

        List<String> outputLines = Files.readAllLines(outfilePath);
        assertThat(outputLines, equalTo(List.of("20|25364147|25378237|-14091|DEL|nsv1067853|CLINVAR|RCV000000042|25|PATHOGENIC|NO_ASSERTION_CRITERIA_PROVIDED")));
    }

}