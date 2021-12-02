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

package org.monarchinitiative.exomiser.core.analysis.util;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AcmgAssignmentTest {

    @Test
    void testAutosomalDominant() {
        TranscriptAnnotation transcriptAnnotation = TranscriptAnnotation.builder().accession("ENST12345678").geneSymbol("GENE1").hgvsProtein("p.1234A>-").hgvsCdna("c.2346A>T").variantEffect(VariantEffect.STOP_GAINED).build();
        VariantEvaluation variantEvaluation = TestFactory.variantBuilder(1, 12335, "A", "T").annotations(List.of(transcriptAnnotation)).build();
        GeneIdentifier geneIdentifier = GeneIdentifier.builder().geneId("HGNC:1").geneSymbol("GENE1").build();
        Disease disease = Disease.builder().diseaseId("OMIM:12345").diseaseName("Disease").build();
        AcmgEvidence acmgEvidence = AcmgEvidence.builder().add(AcmgCriterion.PVS1).add(AcmgCriterion.PS1).add(AcmgCriterion.PP4, AcmgCriterion.Evidence.STRONG).build();
        AcmgAssignment instance = AcmgAssignment.of(variantEvaluation, geneIdentifier, ModeOfInheritance.AUTOSOMAL_DOMINANT, disease, acmgEvidence, AcmgClassification.PATHOGENIC);
        assertThat(instance.toDisplayString(), equalTo("1-12335-A-T, NC_000001.10:g.12335A>T, GENE1(ENST12345678):c.2346A>T:p.1234A>-, PATHOGENIC, [PVS1, PS1, PP4_Strong], Disease (OMIM:12345), AUTOSOMAL_DOMINANT"));
    }
}