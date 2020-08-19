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

package org.monarchinitiative.exomiser.data.phenotype.processors.model.disease;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class DiseaseGeneMoiComparisonTest {

    @Test
    void isMissingHpoMoi() {
        DiseaseGene diseaseGene = DiseaseGene.builder()
                .diseaseId("OMIM:101600")
                .omimGeneId("OMIM:176943")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .build();

        InheritanceMode hpoaMoi = InheritanceMode.UNKNOWN;

        DiseaseGeneMoiComparison instance = DiseaseGeneMoiComparison.of(diseaseGene, hpoaMoi);
        assertThat(instance.isMissingHpoMoi(), is(true));
    }

    @Test
    void isMissingOmimMoi() {
        DiseaseGene diseaseGene = DiseaseGene.builder()
                .diseaseId("OMIM:101600")
                .omimGeneId("OMIM:176943")
                .inheritanceMode(InheritanceMode.UNKNOWN)
                .build();

        InheritanceMode hpoaMoi = InheritanceMode.AUTOSOMAL_DOMINANT;

        DiseaseGeneMoiComparison instance = DiseaseGeneMoiComparison.of(diseaseGene, hpoaMoi);
        assertThat(instance.isMissingOmimMoi(), is(true));
    }

    @Test
    void hasMismatchedMoi() {
        DiseaseGene diseaseGene = DiseaseGene.builder()
                .diseaseId("OMIM:101600")
                .omimGeneId("OMIM:176943")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .build();

        InheritanceMode hpoaMoi = InheritanceMode.AUTOSOMAL_RECESSIVE;

        DiseaseGeneMoiComparison instance = DiseaseGeneMoiComparison.of(diseaseGene, hpoaMoi);
        assertThat(instance.hasMismatchedMoi(), is(true));
    }

    @Test
    void hasMatchingMoi() {
        DiseaseGene diseaseGene = DiseaseGene.builder()
                .diseaseId("OMIM:101600")
                .omimGeneId("OMIM:176943")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .build();

        InheritanceMode hpoaMoi = InheritanceMode.AUTOSOMAL_DOMINANT;

        DiseaseGeneMoiComparison instance = DiseaseGeneMoiComparison.of(diseaseGene, hpoaMoi);
        assertThat(instance.hasMatchingMoi(), is(true));
    }

    @Test
    void toMarkdownLine() {
        DiseaseGene diseaseGene = DiseaseGene.builder()
                .diseaseId("OMIM:101600")
                .diseaseName("PFEIFFER SYNDROME")
                .omimGeneId("OMIM:176943")
                .geneSymbol("FGFR2")
                .inheritanceMode(InheritanceMode.AUTOSOMAL_DOMINANT)
                .build();

        InheritanceMode hpoaMoi = InheritanceMode.AUTOSOMAL_RECESSIVE;

        DiseaseGeneMoiComparison instance = DiseaseGeneMoiComparison.of(diseaseGene, hpoaMoi);
        assertThat(instance.toOutputLine(), equalTo("- [ ] PFEIFFER SYNDROME (OMIM:101600); FGFR2 (OMIM:176943); HPO_MOI: AUTOSOMAL_RECESSIVE, OMIM_MOI: AUTOSOMAL_DOMINANT"));
    }
}