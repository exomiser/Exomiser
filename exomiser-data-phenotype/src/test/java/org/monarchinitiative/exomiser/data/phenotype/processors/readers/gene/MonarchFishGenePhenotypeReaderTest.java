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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class MonarchFishGenePhenotypeReaderTest {

    @Test
    void read() {
        Resource drGenePhenotypeResource = Resource.of("src/test/resources/data/fish/Dr_gene_phenotype_test.txt");
        MonarchFishGenePhenotypeReader instance = new MonarchFishGenePhenotypeReader(drGenePhenotypeResource);
        SetMultimap<String, String> genePhenotypes = instance.read();

        SetMultimap<String, String> expected = LinkedHashMultimap.create();
        expected.putAll("ZFIN:ZDB-GENE-000125-4", ImmutableSet.of("ZP:0000002", "ZP:0000003", "ZP:0000004", "ZP:0000005", "ZP:0000006", "ZP:0000007", "ZP:0000008", "ZP:0000009", "ZP:0000010", "ZP:0000011", "ZP:0000012", "ZP:0000013", "ZP:0000014", "ZP:0000015", "ZP:0000016", "ZP:0000017", "ZP:0000018", "ZP:0000019", "ZP:0000020", "ZP:0000021", "ZP:0000022", "ZP:0012657", "ZP:0012658"));
        expected.putAll("ZFIN:ZDB-GENE-000201-18", ImmutableSet.of("ZP:0000028", "ZP:0000031", "ZP:0000032", "ZP:0000033", "ZP:0000034", "ZP:0000035", "ZP:0000036", "ZP:0000038", "ZP:0000039", "ZP:0000040", "ZP:0000041", "ZP:0000043", "ZP:0000044", "ZP:0000045", "ZP:0000046", "ZP:0000049", "ZP:0000050", "ZP:0000053", "ZP:0000054", "ZP:0000055", "ZP:0000056", "ZP:0000057", "ZP:0000058", "ZP:0000059", "ZP:0000060", "ZP:0000061", "ZP:0000062", "ZP:0000063", "ZP:0000064", "ZP:0000065", "ZP:0000066", "ZP:0000067", "ZP:0000068", "ZP:0000191", "ZP:0000194", "ZP:0000475", "ZP:0000675", "ZP:0000686", "ZP:0000688", "ZP:0000752", "ZP:0000826", "ZP:0001450", "ZP:0001536", "ZP:0001707", "ZP:0003253", "ZP:0004644", "ZP:0005232", "ZP:0005415", "ZP:0011363", "ZP:0011756", "ZP:0013065", "ZP:0014597", "ZP:0018576", "ZP:0018599", "ZP:0019211", "ZP:0019290", "ZP:0019295", "ZP:0020367", "ZP:0020368", "ZP:0020369", "ZP:0020370", "ZP:0020371", "ZP:0020372", "ZP:0020373", "ZP:0020374", "ZP:0020375", "ZP:0020376", "ZP:0100920", "ZP:0105671"));
        expected.putAll("ZFIN:ZDB-SNORNAG-120309-4", ImmutableSet.of("ZP:0000797"));
        expected.putAll("ZFIN:ZDB-SNORNAG-120314-8", ImmutableSet.of("ZP:0000038", "ZP:0000235", "ZP:0001616", "ZP:0002944", "ZP:0003946", "ZP:0006555", "ZP:0007179"));

        assertThat(genePhenotypes, equalTo(expected));
   }
}