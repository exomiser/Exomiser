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

package org.monarchinitiative.exomiser.data.genome.archive;

import org.apache.commons.vfs2.FileObject;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ArchiveFileReaderTest {

    private final TabixAlleleArchive archive = new TabixAlleleArchive(Paths.get("src/test/resources/test_empty.vcf.gz"));

    @Test
    public void getFileObjects() throws Exception {
        ArchiveFileReader instance = new ArchiveFileReader(archive);
        List<FileObject> vcfFiles = instance.getFileObjects();
        assertThat(vcfFiles.size(), equalTo(1));
    }

    @Test
    public void readFileObject() throws Exception {
        ArchiveFileReader instance = new ArchiveFileReader(archive);
        List<FileObject> vcfFiles = instance.getFileObjects();
        FileObject vcfFile = vcfFiles.get(0);
        Stream<String> stringStream = new BufferedReader(new InputStreamReader(instance.readFileObject(vcfFile))).lines();
        assertThat(stringStream.count(), equalTo(0L));
    }

}