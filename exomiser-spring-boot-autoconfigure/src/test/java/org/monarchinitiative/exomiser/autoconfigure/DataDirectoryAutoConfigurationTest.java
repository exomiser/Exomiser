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

package org.monarchinitiative.exomiser.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DataDirectoryAutoConfigurationTest extends AbstractAutoConfigurationTest {

    @Test
    public void testUndefinedDataPath() {
        Throwable thrown = assertThrows(BeanCreationException.class, () -> load(DataDirectoryAutoConfiguration.class));
        assertThat(thrown.getMessage(), containsString("Exomiser data directory not defined. Please provide a valid path."));
    }

    @Test
    public void testEmptyDataPath() {
        Throwable thrown = assertThrows(BeanCreationException.class, () ->
                load(DataDirectoryAutoConfiguration.class, "exomiser.data-directory=")
        );
        assertThat(thrown.getMessage(), containsString("Exomiser data directory not defined. Please provide a valid path."));
    }

    @Test
    public void testDataPath() {
        load(DataDirectoryAutoConfiguration.class, "exomiser.data-directory=" + TEST_DATA);
        Path exomiserDataDirectory = (Path) this.context.getBean("exomiserDataDirectory");
        assertThat(exomiserDataDirectory, equalTo(TEST_DATA));
    }

    @Test
    public void testWorkingDirectoryPathDefaultIsTempDir() {
        load(DataDirectoryAutoConfiguration.class, TEST_DATA_ENV);
        Path workingDirectory = (Path) this.context.getBean("exomiserWorkingDirectory");
        assertThat(workingDirectory.getFileName(), equalTo(Paths.get("exomiser")));
        assertThat(workingDirectory.getParent(), equalTo(Paths.get(System.getProperty("java.io.tmpdir"))));
    }

    @Test
    public void testCanSpecifyWorkingDirectory() {
        load(DataDirectoryAutoConfiguration.class, TEST_DATA_ENV, "exomiser.working-directory=" + TEST_DATA + "/wibble");
        Path workingDirectory = (Path) this.context.getBean("exomiserWorkingDirectory");
        assertThat(workingDirectory.getFileName(), equalTo(Paths.get("wibble")));
        assertThat(workingDirectory.getParent(), equalTo(TEST_DATA));
    }

}