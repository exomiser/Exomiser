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

package org.monarchinitiative.exomiser.data.genome.config;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ResourcePropertiesTest {

    @Test
    void testAllTheThings() throws Exception {
        String name = "all_the_alleles.vcf.gz";
        Path filePath = Paths.get("build-dir/hg19/variants");
        String fileUrl = "ftp://alleles.org/get/your/alleles/here/";
        ResourceProperties instance = new ResourceProperties(name, filePath, fileUrl);

        assertThat(instance.getResourceUrl(), equalTo(new URL("ftp://alleles.org/get/your/alleles/here/all_the_alleles.vcf.gz")));
        assertThat(instance.getResourcePath(), equalTo(Paths.get("build-dir/hg19/variants/all_the_alleles.vcf.gz")));
    }

    @Test
    void testAllTheThingsUrlWithoutSeparatorEnding() throws Exception {
        String name = "all_the_alleles.vcf.gz";
        Path filePath = Paths.get("build-dir/hg19/variants");
        //this one has no path separator ending
        String fileUrl = "ftp://alleles.org/get/your/alleles/here";
        ResourceProperties instance = new ResourceProperties(name, filePath, fileUrl);

        assertThat(instance.getResourceUrl(), equalTo(new URL("ftp://alleles.org/get/your/alleles/here/all_the_alleles.vcf.gz")));
        assertThat(instance.getResourcePath(), equalTo(Paths.get("build-dir/hg19/variants/all_the_alleles.vcf.gz")));
    }
}