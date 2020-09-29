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

package org.monarchinitiative.exomiser.data.genome.model.archive;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ArchiveFileReaderTest {

    @Test
    void readEmptyLines() {
        ArchiveFileReader instance = new SimpleArchiveFileReader(new TabixAlleleArchive(Paths.get("src/test/resources/test_empty.vcf.gz")));
        long lineCount = instance.lines().count();
        assertThat(lineCount, equalTo(0L));
    }

    @Test
    void readLines() {
        ArchiveFileReader instance = new SimpleArchiveFileReader(new TabixAlleleArchive(Paths.get("src/test/resources/test_first_ten_dbsnp.vcf.gz")));
        long lineCount = instance.lines().count();
        // 57 header + 10 allele = 67 lines total in the file
        assertThat(lineCount, equalTo(67L));
    }
}