/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome;

import org.junit.Test;
import org.monarchinitiative.exomiser.data.genome.archive.AlleleArchive;
import org.monarchinitiative.exomiser.data.genome.archive.TabixAlleleArchive;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.parsers.DbSnpAlleleParser;
import org.monarchinitiative.exomiser.data.genome.writers.AlleleWriter;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class AlleleArchiveProcessorTest {

    @Test
    public void process() throws Exception {
        AlleleArchive dbsnpArchive = new TabixAlleleArchive(Paths.get("src/test/resources/test_first_ten_dbsnp.vcf.gz"));
        AlleleArchiveProcessor instance = new AlleleArchiveProcessor(dbsnpArchive, new DbSnpAlleleParser());

        TestAlleleWriter testAlleleWriter = new TestAlleleWriter();
        instance.process(testAlleleWriter);

        assertThat(testAlleleWriter.count(), equalTo(10L));
        testAlleleWriter.getAlleles().forEach(System.out::println);
    }

    private class TestAlleleWriter implements AlleleWriter {

        private final List<Allele> alleles = new ArrayList<>();

        public List<Allele> getAlleles() {
            return alleles;
        }

        @Override
        public void write(Allele allele) {
            alleles.add(allele);
        }

        @Override
        public long count() {
            return alleles.size();
        }
    }
}