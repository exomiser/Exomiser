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

import de.charite.compbio.jannovar.UncheckedJannovarException;
import de.charite.compbio.jannovar.data.JannovarData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class JannovarDataSourceLoaderTest {

    @Test
    public void failsToLoadPreviousNativeJannovarVersionFormatData() {
        Path jannovarDataPath = Paths.get("src/test/resources/jannovar/1710_hg19_transcripts_ensembl_native_format.ser");
        Exception exception = assertThrows(UncheckedJannovarException.class, () -> JannovarDataSourceLoader.loadJannovarData(jannovarDataPath));
        assertThat(exception.getMessage(), equalTo("src/test/resources/jannovar/1710_hg19_transcripts_ensembl_native_format.ser was created by Jannovar 0.30 but we need at least 0.33-SNAPSHOT"));
    }

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/jannovar/2309_hg19_transcripts_ensembl_native_format.ser", // jannovar native format >= 0.33
            "src/test/resources/jannovar/1710_hg19_transcripts_ensembl_exomiser_format.ser", // jannovar exomiser format < 0.33
            "src/test/resources/jannovar/2309_hg19_transcripts_ensembl_exomiser_format.ser" // jannovar exomiser format >= 0.33
    })
    public void loadJannovarData(Path jannovarDataPath) {
        JannovarData jannovarData = JannovarDataSourceLoader.loadJannovarData(jannovarDataPath);
        assertThat(jannovarData, instanceOf(JannovarData.class));
    }

    @Test
    public void cannotLoadData() {
        Path jannovarDataPath = Paths.get("src/test/resources/data/1710_hg19/wibble.ser");
        assertThrows(JannovarException.class, () -> JannovarDataSourceLoader.loadJannovarData(jannovarDataPath));
    }
}