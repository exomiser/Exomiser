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

package org.monarchinitiative.exomiser.core.analysis.acmg;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.genome.HgvsUtil;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import java.util.Objects;

/**
 * @since 13.1.0
 */
public record AcmgAssignment(
    // MOI, Disease, Variant(s), transcript, AA change, AcmgCriteria...
    VariantEvaluation variantEvaluation,
    GeneIdentifier geneIdentifier,
    ModeOfInheritance modeOfInheritance,
    Disease disease,
    AcmgEvidence acmgEvidence,
    AcmgClassification acmgClassification) {

    public AcmgAssignment {
        Objects.requireNonNull(variantEvaluation);
        Objects.requireNonNull(geneIdentifier);
        Objects.requireNonNull(disease);
        Objects.requireNonNull(modeOfInheritance);
        Objects.requireNonNull(acmgEvidence);
        Objects.requireNonNull(acmgClassification);
    }

    public static AcmgAssignment of(VariantEvaluation variant, GeneIdentifier geneIdentifier, ModeOfInheritance modeOfInheritance, Disease disease, AcmgEvidence acmgEvidence, AcmgClassification acmgClassification) {
        return new AcmgAssignment(variant, geneIdentifier, modeOfInheritance, disease, acmgEvidence, acmgClassification);
    }

    public String toDisplayString() {
        // report as HGVS, ACMG, Disease, MOI
        //  e.g. FGFR2(NM_014008.5):c.2T>G (p.M1T), likely pathogenic, Apert syndrome, autosomal dominant
        String transcriptinfo = transcriptDisplayString();

        String diseaseInfo = "";
        if (!disease.diseaseId().isEmpty()) {
            diseaseInfo = disease.diseaseName() + " (" + disease.diseaseId() + "), ";
        }

        return variantEvaluation.toGnomad() +
                ", " + HgvsUtil.toHgvsGenomic(variantEvaluation) +
                ", " + transcriptinfo +
                ", " + acmgClassification +
                ", " + acmgEvidence +
                ", " + diseaseInfo
                + modeOfInheritance;
    }

    private String transcriptDisplayString() {
        if (variantEvaluation.hasTranscriptAnnotations()) {
            TranscriptAnnotation transcriptAnnotation = variantEvaluation.transcriptAnnotations().get(0);
            return transcriptAnnotation.geneSymbol() +
                    "(" + transcriptAnnotation.accession() + ")" +
                    ":" + transcriptAnnotation.hgvsCdna() +
                    ":" + transcriptAnnotation.hgvsProtein();
        }
        return "";
    }

    @Override
    public String toString() {
        return "AcmgAssignment{" +
                "variant=" + variantEvaluation +
                ", geneIdentifier=" + geneIdentifier +
                ", disease=" + disease +
                ", modeOfInheritance=" + modeOfInheritance +
                ", acmgEvidence=" + acmgEvidence +
                ", acmgClassification=" + acmgClassification +
                '}';
    }
}
