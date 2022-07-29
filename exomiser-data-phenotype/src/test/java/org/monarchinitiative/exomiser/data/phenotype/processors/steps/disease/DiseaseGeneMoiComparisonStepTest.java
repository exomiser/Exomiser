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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGeneMoiComparison;
import org.monarchinitiative.exomiser.data.phenotype.processors.writers.OutputLineWriter;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class DiseaseGeneMoiComparisonStepTest {

    @Test
    void testRun(@TempDir Path tempDir) {
        Path missingHpo = tempDir.resolve("missing_moi_hpo.md");
        OutputLineWriter<DiseaseGeneMoiComparison> missingInHpoMoiWriter = new OutputLineWriter<>(missingHpo);

        Path missingOmim = tempDir.resolve("missing_moi_omim.md");
        OutputLineWriter<DiseaseGeneMoiComparison> missingInOmimMoiWriter = new OutputLineWriter<>(missingOmim);

        Path mismatchedMoi = tempDir.resolve("mismatched_moi.md");
        OutputLineWriter<DiseaseGeneMoiComparison> mismatchedMoiWriter = new OutputLineWriter<>(mismatchedMoi);

        Path resourcePath = Path.of("src/test/resources/data/");
        Resource hpoPhenotypeAnnotations = Resource.of(resourcePath, "phenotype_annotation_test.tab");
        Resource omimGeneMap2 = Resource.of(resourcePath, "genemap2_test.txt");

        DiseaseGeneMoiComparisonStep instance = DiseaseGeneMoiComparisonStep.create(
                hpoPhenotypeAnnotations,
                omimGeneMap2,
                missingInHpoMoiWriter,
                missingInOmimMoiWriter,
                mismatchedMoiWriter
        );
        instance.run();

        assertTrue(missingHpo.toFile().exists());
        assertTrue(missingOmim.toFile().exists());
        assertTrue(mismatchedMoi.toFile().exists());
    }
}