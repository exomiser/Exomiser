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

package org.monarchinitiative.exomiser.data.phenotype.processors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.ontology.HpoResourceReader;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ResourceDownloaderTest {


    @Disabled
    @Test
    void download(@TempDir Path tempDir) {
        Resource hpoResource = Resource.builder()
                .fileDirectory(tempDir)
                .fileName("hpo.obo")
                .remoteFileUrl("https://purl.obolibrary.org/obo/hp/")
                .remoteFileName("hp.obo")
                .build();

        if (ResourceDownloader.downloadResource(hpoResource)) {
            HpoResourceReader hpoResourceReader = new HpoResourceReader(hpoResource);
            String dataVersion = hpoResourceReader.read().getDataVersion();
            System.out.println("HPO version: " + dataVersion);
            assertTrue(true);
        } else {
            System.out.println("Download failed");
            fail();
        }

    }
}