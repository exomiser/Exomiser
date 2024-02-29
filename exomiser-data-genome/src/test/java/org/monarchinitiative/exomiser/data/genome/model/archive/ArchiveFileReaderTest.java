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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class ArchiveFileReaderTest {

    @ParameterizedTest
    @CsvSource({
            "src/test/resources/test_empty.vcf.gz,  0", // empty
            "src/test/resources/test_first_ten_dbsnp.vcf.gz, 79", // gzipped vcf
            "src/test/resources/gnomad-test/chr1.vcf.bgz, 62" // bgzipped vcf
    })
    void readTabixArchive(Path archiveFile, long expectedLineCount) {
        ArchiveFileReader instance = new SimpleArchiveFileReader(new TabixArchive(archiveFile));
        long lineCount = instance.lines().count();
        assertThat(lineCount, equalTo(expectedLineCount));
    }

    @Test
    void readNonFolderArchive() {
        ArchiveFileReader instance = new SimpleArchiveFileReader(new FileArchive(Path.of("src/test/resources/genome/ensembl_enhancers.tsv")));
        assertThat(instance.lines().count(), equalTo(7L));
    }
}