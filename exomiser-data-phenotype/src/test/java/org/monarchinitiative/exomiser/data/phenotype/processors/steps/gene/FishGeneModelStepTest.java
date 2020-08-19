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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.gene;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneModel;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.MonarchFishGeneLabelReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.MonarchFishGenePhenotypeReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.ZfinGeneOrthologReader;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class FishGeneModelStepTest {

    @Test
    void run(@TempDir Path tempDir) throws IOException {
        Path resourceDir = Path.of("src/test/resources/data/fish");

        // Fish-Human Orthologs
        ZfinGeneOrthologReader zfinGeneOrthologReader = new ZfinGeneOrthologReader(Resource.of(resourceDir, "human_orthos_test.txt"));

        Path fishOrthoPath = tempDir.resolve("fish_orthologs.pg");
        OutputLineWriter<GeneOrtholog> fishGeneOrthologOutputLineWriter = new OutputLineWriter<>(fishOrthoPath);

        // Gene-Phenotype models
        MonarchFishGeneLabelReader monarchFishGeneLabelReader = new MonarchFishGeneLabelReader(Resource.of(resourceDir, "Dr_gene_labels_test.txt"));
        MonarchFishGenePhenotypeReader monarchFishGenePhenotypeReader = new MonarchFishGenePhenotypeReader(Resource.of(resourceDir, "Dr_gene_phenotype_test.txt"));

        Path fishModelPath = tempDir.resolve("fish_models.pg");
        OutputLineWriter<GeneModel> fishGeneModelOutputLineWriter = new OutputLineWriter<>(fishModelPath);

        FishGeneModelStep instance = new FishGeneModelStep(zfinGeneOrthologReader, fishGeneOrthologOutputLineWriter, monarchFishGeneLabelReader, monarchFishGenePhenotypeReader, fishGeneModelOutputLineWriter);
        instance.run();

        assertTrue(Files.size(fishOrthoPath) > 0);
        assertTrue(Files.size(fishModelPath) > 0);
    }
}