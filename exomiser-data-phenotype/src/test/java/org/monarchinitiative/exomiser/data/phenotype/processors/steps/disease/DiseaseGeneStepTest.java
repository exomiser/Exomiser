/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2022 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class DiseaseGeneStepTest {

    @Disabled
    @Test
    void testRun(@TempDir Path tempDir) {
        Path dataDir = Path.of("src/test/resources/data");
        Resource phenotypeAnnotationsResource = Resource.of(dataDir, "phenotype_annotation_test.tab");
        Resource geneMap2Resource = Resource.of(dataDir, "genemap2.txt");
        Resource mimToGeneResource = Resource.of(dataDir, "mim2gene_test.txt");
        Resource product1Resource = Resource.of(dataDir, "en_product1_test.xml");
        Resource product6Resource = Resource.of(dataDir, "en_product6_test.xml");
        Resource product9Resource = Resource.of(dataDir, "en_product9_ages.xml");

        Path diseasePg = tempDir.resolve("disease.pg");
        OutputLineWriter<DiseaseGene> diseaseGeneWriter = new OutputLineWriter<>(diseasePg);

        DiseaseGeneStep instance = DiseaseGeneStep.create(phenotypeAnnotationsResource, geneMap2Resource, mimToGeneResource, product1Resource, product6Resource, product9Resource, diseaseGeneWriter);
        instance.run();

        assertTrue(diseasePg.toFile().exists());
    }
}