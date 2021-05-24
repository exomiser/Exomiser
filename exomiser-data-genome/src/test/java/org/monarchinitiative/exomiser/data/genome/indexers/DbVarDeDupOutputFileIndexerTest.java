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

package org.monarchinitiative.exomiser.data.genome.indexers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.genome.model.SvFrequency;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.monarchinitiative.svart.VariantType.INS_ME_SVA;

public class DbVarDeDupOutputFileIndexerTest {

    @Test
    public void deDup(@TempDir Path tempDir) throws IOException {
        Path outputPath = tempDir.resolve("dbvar-sv.pg");

        DbVarDeDupOutputFileIndexer instance = new DbVarDeDupOutputFileIndexer(outputPath);

        List<SvFrequency> input = List.of(
                new SvFrequency(7, 5853374, 5853374, 1322, INS_ME_SVA, "esv3848073", "DBVAR", "essv20524319", 28, 5008),
                new SvFrequency(7, 5853374, 5853374, 1322, INS_ME_SVA, "esv3848073", "DBVAR", "essv20524320", 28, 5008),
                new SvFrequency(7, 5853374, 5853374, 1322, INS_ME_SVA, "esv3848073", "DBVAR", "essv20524321", 28, 5008),
                new SvFrequency(7, 5853374, 5853374, 1320, INS_ME_SVA, "esv3848073", "DBVAR", "essv20524322", 28, 5008),
                new SvFrequency(7, 5853374, 5853375, 1240, INS_ME_SVA, "nsv4566220", "DBVAR", "nssv16072270", 308, 21656),
                new SvFrequency(7, 5853374, 5853375, 2, INS_ME_SVA, "nsv4700870", "DBVAR", "nssv16218955", 308, 21656)
        );

        input.forEach(instance::write);
        instance.close();

        List<String> expected = List.of(
                "7|5853374|5853374|1322|INS_ME_SVA|esv3848073|DBVAR|essv20524319|28|5008",
                "7|5853374|5853374|1320|INS_ME_SVA|esv3848073|DBVAR|essv20524322|28|5008",
                "7|5853374|5853375|1240|INS_ME_SVA|nsv4566220|DBVAR|nssv16072270|308|21656",
                "7|5853374|5853375|2|INS_ME_SVA|nsv4700870|DBVAR|nssv16218955|308|21656"
        );

        assertThat(Files.exists(outputPath), equalTo(true));
        assertThat(Files.readAllLines(outputPath), equalTo(expected));
    }
}