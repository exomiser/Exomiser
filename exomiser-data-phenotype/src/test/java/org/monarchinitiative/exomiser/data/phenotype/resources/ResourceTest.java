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

package org.monarchinitiative.exomiser.data.phenotype.resources;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ResourceTest {

    @Test
    void remoteResource() throws MalformedURLException {
        Resource instance = Resource.builder()
                .fileName("en_product1_test.xml")
                .fileDirectory(Paths.get("src/test/resources/data"))
                .remoteFileUrl("http://www.orphadata.org/data/xml")
                .remoteFileName("en_product1.xml")
                .build();

        System.out.println(instance);
        assertThat(instance.getFileName(), equalTo("en_product1_test.xml"));
        assertThat(instance.getResourcePath(), equalTo(Paths.get("src/test/resources/data/en_product1_test.xml")));

        assertTrue(instance.hasRemoteResource());
        assertThat(instance.getRemoteResourceUrl(), equalTo(new URL("http://www.orphadata.org/data/xml/en_product1.xml")));
        assertThat(instance.getRemoteFileName(), equalTo("en_product1.xml"));
    }

    @Test
    void localResource() {
        Resource instance = Resource.builder()
                .fileName("hp-hp-phenodigm-cache.txt")
                .fileDirectory(Paths.get("src/test/resources/data"))
                .build();

        System.out.println(instance);
        assertThat(instance.getFileName(), equalTo("hp-hp-phenodigm-cache.txt"));
        assertThat(instance.getResourcePath(), equalTo(Paths.get("src/test/resources/data/hp-hp-phenodigm-cache.txt")));

        assertFalse(instance.hasRemoteResource());
        assertThat(instance.getRemoteResourceUrl(), equalTo(null));
        assertThat(instance.getRemoteFileName(), equalTo(""));
    }

    @Test
    void checkNotNullPathAndFileName() {
        assertThrows(NullPointerException.class, () -> Resource.builder()
                .fileDirectory(null)
                .fileName(null)
                .build());
    }

    @Test
    void wontBuildWithoutPathAndFileName() {
        assertThrows(NullPointerException.class, () -> Resource.builder().build());
    }

    @Test
    void throwsExceptionWithMalformedUrl() {
        assertThrows(RuntimeException.class, () -> {
            Resource.builder()
                    .fileName("wibble")
                    .fileDirectory(Paths.get(""))
                    .remoteFileUrl("htp://thinggov")
                    .remoteFileName("thing")
                    .build();
        });
    }

    @Test
    void acceptsNullsInExternalUrlDefaultsToEmpty() {
        Resource instance = Resource.builder()
                .fileName("hp-hp-phenodigm-cache.txt")
                .fileDirectory(Paths.get("src/test/resources/data"))
                .remoteFileUrl(null)
                .remoteFileName(null)
                .build();

        System.out.println(instance);
        assertThat(instance.getFileName(), equalTo("hp-hp-phenodigm-cache.txt"));
        assertThat(instance.getResourcePath(), equalTo(Paths.get("src/test/resources/data/hp-hp-phenodigm-cache.txt")));

        assertThat(instance.hasRemoteResource(), equalTo(false));
        assertThat(instance.getRemoteResourceUrl(), equalTo(null));
        assertThat(instance.getRemoteFileName(), equalTo(""));
    }
}