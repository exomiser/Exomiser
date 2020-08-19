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
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneModel;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.gene.GeneOrtholog;
import org.monarchinitiative.exomiser.data.phenotype.processors.readers.gene.GenePhenotype;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class MouseGeneModelFactoryTest {

    @Test
    void buildGeneModels() {
        List<GeneOrtholog> orthologs = List.of(
                new GeneOrtholog("MGI:12345", "Abcd", "ABCD", 12345),
                // duplicated MGI geneId due to N:M orthology with human.
                new GeneOrtholog("MGI:12345", "Abcd", "ABCD1", 123451),
                new GeneOrtholog("MGI:54321", "Dcba", "DCBA", 54321)
        );

        List<GenePhenotype> mgiModels = List.of(
                new GenePhenotype("MGI:model-1", "MGI:12345", List.of("MP:1", "MP:2")),
                new GenePhenotype("MGI:model-2", "MGI:12345", List.of("MP:1", "MP:3")),
                // this has a missing geneId
                new GenePhenotype("MGI:model-3", "MGI:45678", List.of("MP:7", "MP:8", "MP:9"))
                );

        List<GenePhenotype> impcModels = List.of(
                new GenePhenotype("IMPC:HAR-123", "MGI:12345", List.of("MP:1", "MP:3", "MP:4")),
                new GenePhenotype("IMPC:WTSIJ", "MGI:54321", List.of("MP:2345", "MP:9999", "MP:123"))
        );

        MouseGeneModelFactory instance = new MouseGeneModelFactory(orthologs, mgiModels, impcModels);

        List<GeneModel> expected = List.of(
                new GeneModel("IMPC:HAR-123", "MGI:12345", "Abcd", List.of("MP:1", "MP:3", "MP:4")),
                new GeneModel("MGI:model-1", "MGI:12345", "Abcd", List.of("MP:1", "MP:2")),
                new GeneModel("MGI:model-2", "MGI:12345", "Abcd", List.of("MP:1", "MP:3")),
                new GeneModel("IMPC:WTSIJ", "MGI:54321", "Dcba", List.of("MP:2345", "MP:9999", "MP:123"))
        );

        assertThat(instance.buildGeneModels(), equalTo(expected));
    }

    @Test
    void buildGeneModelsMissingGeneSymbol() {
        List<GeneOrtholog> orthologs = List.of(
                new GeneOrtholog("MGI:12345", "Abcd", "ABCD", 12345)
        );

        List<GenePhenotype> mgiModels = List.of(
                // this has a missing geneId from the geneOrthologs
                new GenePhenotype("MGI:model-3", "MGI:45678", List.of("MP:7", "MP:8", "MP:9"))
        );

        List<GenePhenotype> impcModels = List.of();

        MouseGeneModelFactory instance = new MouseGeneModelFactory(orthologs, mgiModels, impcModels);

        assertTrue(instance.buildGeneModels().isEmpty());
    }
}