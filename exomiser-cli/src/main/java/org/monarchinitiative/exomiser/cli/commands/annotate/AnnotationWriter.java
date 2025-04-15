package org.monarchinitiative.exomiser.cli.commands.annotate;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.apache.commons.csv.CSVFormat;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraint;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraints;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgAssignment;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgClassification;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgEvidence;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;
import org.monarchinitiative.exomiser.core.model.TranscriptAnnotation;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityScore;

import java.text.DecimalFormat;
import java.util.*;

import static java.util.stream.Collectors.joining;

public class AnnotationWriter {

    public static final CSVFormat EXOMISER_VARIANTS_TSV_FORMAT = CSVFormat.newFormat('\t')
            .withSkipHeaderRecord()
            .withRecordSeparator("\n")
            .withIgnoreSurroundingSpaces(true)
            .withHeader("ID", "GENE_SYMBOL", "HGNC_GENE_ID", "ENTREZ_GENE_ID",  "EXOMISER_VARIANT_SCORE",
                    "WHITELIST_VARIANT", "VCF_ID", "RS_ID", "CONTIG", "START", "END", "REF", "ALT", "CHANGE_LENGTH", "QUAL",
                    "FILTER", "GENOTYPE", "FUNCTIONAL_CLASS", "HGVS",
                    "EXOMISER_ACMG_CLASSIFICATION", "EXOMISER_ACMG_EVIDENCE", "EXOMISER_ACMG_DISEASE_ID", "EXOMISER_ACMG_DISEASE_NAME",
                    "CLINVAR_VARIATION_ID", "CLINVAR_PRIMARY_INTERPRETATION", "CLINVAR_STAR_RATING",
                    "GENE_CONSTRAINT_LOEUF", "GENE_CONSTRAINT_LOEUF_LOWER", "GENE_CONSTRAINT_LOEUF_UPPER",
                    "MAX_FREQ_SOURCE", "MAX_FREQ", "ALL_FREQ", "MAX_PATH_SOURCE", "MAX_PATH", "ALL_PATH");

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.0000");

    public AnnotationWriter() {
        Locale.setDefault(Locale.UK);
    }


    public static List<Object> buildVariantRecord(VariantEvaluation ve, AcmgAssignment acmgAssignment) {
        List<Object> fields = new ArrayList<>(EXOMISER_VARIANTS_TSV_FORMAT.getHeader().length);
        GeneIdentifier geneIdentifier = acmgAssignment.geneIdentifier();
        ModeOfInheritance modeOfInheritance = acmgAssignment.modeOfInheritance();
        Optional<AcmgAssignment> assignment = Optional.of(acmgAssignment);
        fields.add(ve.toGnomad());
        fields.add(geneIdentifier.getGeneSymbol());
        fields.add(geneIdentifier.getHgncId());
        fields.add(geneIdentifier.getEntrezId());
        fields.add(decimalFormat.format(ve.getVariantScore()));
        fields.add(ve.isWhiteListed() ? "1" : "0");
        fields.add(ve.id());
        FrequencyData frequencyData = ve.getFrequencyData();
        fields.add(frequencyData.getRsId());
        fields.add(ve.contigName());
        fields.add(ve.start());
        fields.add(ve.end());
        fields.add(ve.ref());
        fields.add(ve.alt());
        fields.add(ve.changeLength());
        fields.add(decimalFormat.format(ve.getPhredScore()));
        fields.add(makeFiltersField(modeOfInheritance, ve));
        fields.add(ve.getGenotypeString());
        fields.add(ve.getVariantEffect().getSequenceOntologyTerm());
        fields.add(getRepresentativeAnnotation(ve.getTranscriptAnnotations()));
        fields.add(assignment.map(AcmgAssignment::acmgClassification).orElse(AcmgClassification.NOT_AVAILABLE));
        fields.add(assignment.map(acmg -> toVcfAcmgInfo(acmg.acmgEvidence())).orElse(""));
        fields.add(assignment.map(acmg -> acmg.disease().getDiseaseId()).orElse(""));
        fields.add(assignment.map(acmg -> acmg.disease().getDiseaseName()).orElse(""));
        PathogenicityData pathogenicityData = ve.getPathogenicityData();
        ClinVarData clinVarData = pathogenicityData.clinVarData();
        fields.add(clinVarData.getVariationId());
        fields.add(clinVarData.getPrimaryInterpretation());
        fields.add(clinVarData.starRating());
        GeneConstraint geneConstraint = GeneConstraints.geneConstraint(geneIdentifier.getGeneSymbol());
        fields.add(geneConstraint == null ? "" : geneConstraint.loeuf());
        fields.add(geneConstraint == null ? "" : geneConstraint.loeufLower());
        fields.add(geneConstraint == null ? "" : geneConstraint.loeufUpper());
        Frequency maxFreq = frequencyData.maxFrequency();
        fields.add(maxFreq == null ? "" : maxFreq.source());
        fields.add(maxFreq == null ? "" : maxFreq.frequency());
        fields.add(toVcfFreqInfo(frequencyData.frequencies()));
        PathogenicityScore maxPath = pathogenicityData.mostPathogenicScore();
        fields.add(maxPath == null ? "" : maxPath.getSource());
        fields.add(maxPath == null ? "" : maxPath.getScore());
        fields.add(toVcfPathInfo(pathogenicityData.pathogenicityScores()));
        return fields;
    }

    private static String toVcfAcmgInfo(AcmgEvidence acmgEvidence) {
        return acmgEvidence.evidence().entrySet().stream()
                .map(entry -> {
                    AcmgCriterion acmgCriterion = entry.getKey();
                    AcmgCriterion.Evidence evidence = entry.getValue();
                    return (acmgCriterion.evidence() == evidence) ? acmgCriterion.toString() : acmgCriterion + "_" + evidence.displayString();
                })
                .collect(joining(","));
    }
    private static String toVcfFreqInfo(List<Frequency> frequencies) {
        return frequencies.stream()
                .map(frequency -> frequency.source() + "=" + frequency.frequency())
                .collect(joining(","));
    }

    private static String toVcfPathInfo(List<PathogenicityScore> predictedPathogenicityScores) {
        return predictedPathogenicityScores.stream()
                .map(pathScore -> pathScore.getSource() + "=" + pathScore.getScore())
                .collect(joining(","));
    }

    private static String makeFiltersField(ModeOfInheritance modeOfInheritance, VariantEvaluation variantEvaluation) {
        //under some modes a variant should not pass, but others it will, so we need to check this here
        //otherwise when running FULL or SPARSE modes alleles will be reported as having passed under the wrong MOI
        return switch (variantEvaluation.getFilterStatusForMode(modeOfInheritance)) {
            case FAILED -> {
                Set<FilterType> failedFilterTypes = variantEvaluation.getFailedFilterTypesForMode(modeOfInheritance);
                yield formatFailedFilters(failedFilterTypes);
            }
            case PASSED -> "PASS";
            default -> ".";
        };
    }

    private static String formatFailedFilters(Set<FilterType> failedFilters) {
        StringJoiner stringJoiner = new StringJoiner(";");
        for (FilterType filterType : failedFilters) {
            stringJoiner.add(filterType.vcfValue());
        }
        return stringJoiner.toString();
    }

    /**
     * @return An annotation for a single transcript, representing one of the
     * annotations with the most pathogenic annotation.
     */
    private static String getRepresentativeAnnotation(List<TranscriptAnnotation> annotations) {
        if (annotations.isEmpty()) {
            return "";
        }
        TranscriptAnnotation anno = annotations.get(0);

        StringJoiner stringJoiner = new StringJoiner(":");
        stringJoiner.add(anno.getGeneSymbol());
        stringJoiner.add(anno.getAccession());
        stringJoiner.add(anno.getHgvsCdna());
        stringJoiner.add(anno.getHgvsProtein());
        return stringJoiner.toString();
    }

}
