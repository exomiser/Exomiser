/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.genome.jannovar;

import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarDataProtoSerialiserTest {

    private Path getTempFile() throws IOException {
        Path tempFile = Files.createTempFile("exomiser_test", ".tmp");
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }

    @Test
    public void roundTrip() throws Exception {
        JannovarData testData = TestFactory.buildDefaultJannovarData();

        Path protoJannovarPath = getTempFile();
        JannovarDataProtoSerialiser.save(protoJannovarPath, testData);
        JannovarData jannovarData = JannovarDataProtoSerialiser.load(protoJannovarPath);

        assertThat(jannovarData.getRefDict().getContigNameToID(), equalTo(testData.getRefDict().getContigNameToID()));
        assertThat(jannovarData.getRefDict().getContigIDToLength(), equalTo(testData.getRefDict().getContigIDToLength()));
        assertThat(jannovarData.getRefDict().getContigIDToName(), equalTo(testData.getRefDict().getContigIDToName()));

        assertThat(jannovarData.getTmByAccession(), equalTo(testData.getTmByAccession()));
        assertThat(jannovarData.getTmByGeneSymbol(), equalTo(testData.getTmByGeneSymbol()));
    }

    @Test
    public void incorrectFileFormatThrowsException() throws Exception {
        Path protoJannovarPath = getTempFile();
        Exception exception = assertThrows(InvalidFileFormatException.class, () -> JannovarDataProtoSerialiser.load(protoJannovarPath));
        assertThat(exception.getMessage(), equalTo(protoJannovarPath + " not an Exomiser format Jannovar transcript database."));
    }
}