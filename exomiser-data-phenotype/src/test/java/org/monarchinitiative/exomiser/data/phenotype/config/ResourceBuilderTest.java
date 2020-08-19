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

package org.monarchinitiative.exomiser.data.phenotype.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ResourceBuilderTest {

    private static ReleaseFileSystem releaseFileSystem;
    private static ResourceBuilder resourceBuilder;

    @BeforeAll
    static void setUp(@TempDir Path tempDir) {
        releaseFileSystem = ReleaseFileSystem.create(tempDir, "2008");
        resourceBuilder = new ResourceBuilder(releaseFileSystem);
    }

    @Test
    void buildRemoteResource() throws MalformedURLException {
        ResourceProperties remoteResourceProperties = ResourceProperties.ofRemote("http://www.orphadata.org/data/xml/", "en_product1.xml");
        Resource product1Resource = resourceBuilder.buildResource(remoteResourceProperties);

        assertThat(product1Resource.getFileName(), equalTo("en_product1.xml"));
        assertThat(product1Resource.getResourcePath(), equalTo(releaseFileSystem.resourcesDir().resolve("en_product1.xml")));
        assertThat(product1Resource.getRemoteResourceUrl(), equalTo(new URL("http://www.orphadata.org/data/xml/en_product1.xml")));
    }

    @Test
    void buildLocalResource() {

        ResourceProperties localResourceProperties = ResourceProperties.ofLocal("hp-hp-phenodigm-cache.txt");
        Resource hphpResource = resourceBuilder.buildResource(localResourceProperties);

        System.out.println(hphpResource);
        assertThat(hphpResource.getFileName(), equalTo("hp-hp-phenodigm-cache.txt"));
        assertThat(hphpResource.getResourcePath(), equalTo(releaseFileSystem.resourcesDir().resolve("hp-hp-phenodigm-cache.txt")));
        assertThat(hphpResource.hasRemoteResource(), equalTo(false));
    }

    @Test
    void buildExternalResourceMissingNames() throws MalformedURLException {
        ResourceProperties malformedResourceProperties = new ResourceProperties();
        malformedResourceProperties.setUrl("http://www.orphadata.org/data/xml/");

        assertThrows(IllegalStateException.class, () -> resourceBuilder.buildResource(malformedResourceProperties));
    }
}