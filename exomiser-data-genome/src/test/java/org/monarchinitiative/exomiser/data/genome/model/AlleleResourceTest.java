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

package org.monarchinitiative.exomiser.data.genome.model;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.genome.model.parsers.DbNsfpColumnIndex;
import org.monarchinitiative.exomiser.data.genome.model.resource.DbNsfp3AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.resource.DbNsfp4AlleleResource;
import org.monarchinitiative.exomiser.data.genome.model.resource.DbSnpAlleleResource;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class AlleleResourceTest {

    @Test
    void dbNsfp3ResourceTest() {
        Path alleleArchivePath = Paths.get("src/test/resources/dbNSFPv3.4a_test.zip");

        AlleleResource instance = new DbNsfp3AlleleResource("dbNSFP3test", null, alleleArchivePath, DbNsfpColumnIndex.HG19);

        long alleles = instance.parseResource()
//                .peek(System.out::println)
                .count();
        //there are 40 lines in total, but only 22  of them have any relevant information in them, as defined by the DbNsfpColumnIndex.
        assertThat(alleles, equalTo(22L));
    }

    @Test
    void dbNsfp4Hg19ResourceTest() {
        Path alleleArchivePath = Paths.get("src/test/resources/dbNSFP4.0_test.zip");

        AlleleResource instance = new DbNsfp4AlleleResource("dbNSFP4test", null, alleleArchivePath, DbNsfpColumnIndex.HG19);

        long alleles = instance.parseResource()
//                .peek(System.out::println)
                .count();
        //there are 30 lines in total, but only 5 of them have any relevant information in them, as defined by the DbNsfpColumnIndex. 1 has only an rsID.
        assertThat(alleles, equalTo(5L));
    }

    @Test
    void dbNsfp4Hg38ResourceTest() {
        Path alleleArchivePath = Paths.get("src/test/resources/dbNSFP4.0_test.zip");

        AlleleResource instance = new DbNsfp4AlleleResource("dbNSFP4test", null, alleleArchivePath, DbNsfpColumnIndex.HG38);

        long alleles = instance.parseResource()
//                .peek(System.out::println)
                .count();
        //there are 30 lines in total, but only 5 of them have any relevant information in them, as defined by the DbNsfpColumnIndex. 1 has only an rsID.
        assertThat(alleles, equalTo(5L));
    }

    @Test
    void dbSnpResourceTest() {
        Path alleleArchivePath = Paths.get("src/test/resources/test_first_ten_dbsnp.vcf.gz");

        AlleleResource instance = new DbSnpAlleleResource("dbSNP", null, alleleArchivePath);

        long alleles = instance.parseResource()
//                .peek(System.out::println)
                .count();
        //there are 30 lines in total, but only 6  of them have any relevant information in them, as defined by the DbNsfpColumnIndex.
        assertThat(alleles,equalTo(10L));
    }
}