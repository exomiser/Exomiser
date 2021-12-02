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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.genome.HgvsUtil;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;

import java.util.Objects;

public class AcmgAssignment {

    // MOI, Disease, Variant(s), transcript, AA change, AcmgCriteria...
    @JsonProperty
    private final VariantEvaluation variantEvaluation;
    @JsonProperty
    private final GeneIdentifier geneIdentifier;
    @JsonProperty
    private final ModeOfInheritance modeOfInheritance;
    @JsonProperty
    private final Disease disease;
    @JsonProperty
    private final AcmgEvidence acmgEvidence;
    @JsonProperty
    private final AcmgClassification acmgClassification;

    private AcmgAssignment(VariantEvaluation variantEvaluation, GeneIdentifier geneIdentifier, ModeOfInheritance modeOfInheritance, Disease disease, AcmgEvidence acmgEvidence, AcmgClassification acmgClassification) {
        this.variantEvaluation = variantEvaluation;
        this.geneIdentifier = geneIdentifier;
        this.modeOfInheritance = modeOfInheritance;
        this.disease = disease;
        this.acmgEvidence = acmgEvidence;
        this.acmgClassification = acmgClassification;
    }

    @JsonCreator
    public static AcmgAssignment of(VariantEvaluation variant, GeneIdentifier geneIdentifier, ModeOfInheritance modeOfInheritance, Disease disease, AcmgEvidence acmgEvidence, AcmgClassification acmgClassification) {
        Objects.requireNonNull(variant);
        Objects.requireNonNull(geneIdentifier);
        Objects.requireNonNull(disease);
        Objects.requireNonNull(modeOfInheritance);
        Objects.requireNonNull(acmgEvidence);
        Objects.requireNonNull(acmgClassification);
        return new AcmgAssignment(variant, geneIdentifier, modeOfInheritance, disease, acmgEvidence, acmgClassification);
    }

    public VariantEvaluation variantEvaluation() {
        return variantEvaluation;
    }

    public GeneIdentifier geneIdentifier() {
        return geneIdentifier;
    }

    public Disease disease() {
        return disease;
    }

    public ModeOfInheritance modeOfInheritance() {
        return modeOfInheritance;
    }

    public AcmgEvidence acmgEvidence() {
        return acmgEvidence;
    }

    public AcmgClassification acmgClassification() {
        return acmgClassification;
    }

    public String toDisplayString() {
        // report as HGVS, ACMG, Disease, MOI
        //  e.g. FGFR2(NM_014008.5):c.2T>G (p.M1T), likely pathogenic, Apert syndrome, autosomal dominant
        String transcriptinfo = transcriptDisplayString();

        String diseaseInfo = "";
        if (!disease.getDiseaseId().isEmpty()) {
            diseaseInfo = disease.getDiseaseName() + " (" + disease.getDiseaseId() + "), ";
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
            TranscriptAnnotation transcriptAnnotation = variantEvaluation.getTranscriptAnnotations().get(0);
            return transcriptAnnotation.getGeneSymbol() +
                    "(" + transcriptAnnotation.getAccession() + ")" +
                    ":" + transcriptAnnotation.getHgvsCdna() +
                    ":" + transcriptAnnotation.getHgvsProtein();
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcmgAssignment that = (AcmgAssignment) o;
        return variantEvaluation.equals(that.variantEvaluation) && geneIdentifier.equals(that.geneIdentifier) && disease.equals(that.disease) && modeOfInheritance == that.modeOfInheritance && acmgEvidence.equals(that.acmgEvidence) && acmgClassification == that.acmgClassification;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantEvaluation, geneIdentifier, disease, modeOfInheritance, acmgEvidence, acmgClassification);
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
