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

package org.monarchinitiative.exomiser.data.phenotype.processors.steps.disease;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGeneMoiComparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class DiseaseGeneMoiComparisonFactoryTest {

    @Test
    void sameMoi() {
        List<DiseaseGene> omimDiseaseGenes = new ArrayList<>();
        DiseaseGene diseaseGene = DiseaseGene.builder()
                .diseaseId("OMIM:101600")
                .omimGeneId("OMIM:176943")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .build();
        omimDiseaseGenes.add(diseaseGene);

        HashMap<String, InheritanceMode> inheritanceModeMap = new HashMap<>();
        InheritanceMode hpoaMoi = InheritanceMode.AUTOSOMAL_DOMINANT;
        inheritanceModeMap.put("OMIM:101600", hpoaMoi);

        List<DiseaseGeneMoiComparison> expected = new ArrayList<>();
        expected.add(DiseaseGeneMoiComparison.of(diseaseGene, hpoaMoi));

        DiseaseGeneMoiComparisonFactory instance = new DiseaseGeneMoiComparisonFactory(omimDiseaseGenes, inheritanceModeMap);
        assertThat(instance.buildComparisons(), equalTo(expected));
    }

    @Test
    void knownMoiInHpo() {
        List<DiseaseGene> omimDiseaseGenes = new ArrayList<>();
        DiseaseGene diseaseGene = DiseaseGene.builder()
                .diseaseId("OMIM:101600")
                .omimGeneId("OMIM:176943")
                .inheritanceMode(InheritanceMode.UNKNOWN)
                .build();
        omimDiseaseGenes.add(diseaseGene);

        HashMap<String, InheritanceMode> inheritanceModeMap = new HashMap<>();
        InheritanceMode hpoaMoi = InheritanceMode.AUTOSOMAL_DOMINANT;
        inheritanceModeMap.put("OMIM:101600", hpoaMoi);

        List<DiseaseGeneMoiComparison> expected = new ArrayList<>();
        expected.add(DiseaseGeneMoiComparison.of(diseaseGene, hpoaMoi));

        DiseaseGeneMoiComparisonFactory instance = new DiseaseGeneMoiComparisonFactory(omimDiseaseGenes, inheritanceModeMap);
        assertThat(instance.buildComparisons(), equalTo(expected));
    }

    @Test
    void knownMoiInOmim() {
        List<DiseaseGene> omimDiseaseGenes = new ArrayList<>();
        DiseaseGene diseaseGene = DiseaseGene.builder()
                .diseaseId("OMIM:101600")
                .omimGeneId("OMIM:176943")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .build();
        omimDiseaseGenes.add(diseaseGene);

        HashMap<String, InheritanceMode> inheritanceModeMap = new HashMap<>();
        InheritanceMode hpoaMoi = InheritanceMode.UNKNOWN;
        inheritanceModeMap.put("OMIM:101600", hpoaMoi);

        List<DiseaseGeneMoiComparison> expected = new ArrayList<>();
        expected.add(DiseaseGeneMoiComparison.of(diseaseGene, hpoaMoi));

        DiseaseGeneMoiComparisonFactory instance = new DiseaseGeneMoiComparisonFactory(omimDiseaseGenes, inheritanceModeMap);
        assertThat(instance.buildComparisons(), equalTo(expected));
    }

    @Test
    void differentMoi() {
        List<DiseaseGene> omimDiseaseGenes = new ArrayList<>();
        DiseaseGene diseaseGene = DiseaseGene.builder()
                .diseaseId("OMIM:101600")
                .omimGeneId("OMIM:176943")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .build();
        omimDiseaseGenes.add(diseaseGene);

        HashMap<String, InheritanceMode> inheritanceModeMap = new HashMap<>();
        InheritanceMode hpoaMoi = InheritanceMode.AUTOSOMAL_RECESSIVE;
        inheritanceModeMap.put("OMIM:101600", hpoaMoi);

        List<DiseaseGeneMoiComparison> expected = new ArrayList<>();
        expected.add(DiseaseGeneMoiComparison.of(diseaseGene, hpoaMoi));

        DiseaseGeneMoiComparisonFactory instance = new DiseaseGeneMoiComparisonFactory(omimDiseaseGenes, inheritanceModeMap);
        assertThat(instance.buildComparisons(), equalTo(expected));
    }
}