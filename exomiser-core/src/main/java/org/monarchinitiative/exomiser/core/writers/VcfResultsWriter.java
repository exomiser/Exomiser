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

package org.monarchinitiative.exomiser.core.writers;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.VariantContextComparator;
import htsjdk.variant.variantcontext.writer.Options;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.*;
import org.monarchinitiative.exomiser.core.analysis.AnalysisResults;
import org.monarchinitiative.exomiser.core.analysis.sample.Sample;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgAssignment;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgClassification;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgCriterion;
import org.monarchinitiative.exomiser.core.analysis.acmg.AcmgEvidence;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.VcfFiles;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.svart.Contig;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.joining;

/**
 * Generate results in VCF format using HTS-JDK.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @author Manuel Holtgrewe <manuel.holtgrewe@charite.de>
 * @see <a href="http://samtools.github.io/hts-specs/VCFv4.1.pdf">VCF Standard</a>
 * @since 13.1.0
 */
public class VcfResultsWriter implements ResultsWriter {
    private static final Logger logger = LoggerFactory.getLogger(VcfResultsWriter.class);

    private static final String EXOMISER_INFO_KEY = "Exomiser";
    private static final String INFO_FORMAT = "{RANK|ID|GENE_SYMBOL|ENTREZ_GENE_ID|MOI|P-VALUE|EXOMISER_GENE_COMBINED_SCORE|EXOMISER_GENE_PHENO_SCORE|EXOMISER_GENE_VARIANT_SCORE|EXOMISER_VARIANT_SCORE|CONTRIBUTING_VARIANT|WHITELIST_VARIANT|FUNCTIONAL_CLASS|HGVS|EXOMISER_ACMG_CLASSIFICATION|EXOMISER_ACMG_EVIDENCE|EXOMISER_ACMG_DISEASE_ID|EXOMISER_ACMG_DISEASE_NAME}";

    private static final VCFHeaderLine EXOMISER_VCF_HEADER_METADATA_LINE = new VCFInfoHeaderLine(EXOMISER_INFO_KEY,
            VCFHeaderLineCount.UNBOUNDED,
            VCFHeaderLineType.String,
            "A pipe-separated set of values for the proband allele(s) from the record with one per compatible MOI following the format: " + INFO_FORMAT
    );

    private static final OutputFormat OUTPUT_FORMAT = OutputFormat.VCF;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.0000");

    /**
     * Initialize the object, given the original {@link VCFFileReader} from the
     * input.
     */
    public VcfResultsWriter() {
        Locale.setDefault(Locale.UK);
    }

    @Override
    public void writeFile(AnalysisResults analysisResults, OutputSettings settings) {
        // create a VariantContextWriter writing to the output file path
        Sample sample = analysisResults.sample();
        Path vcfPath = sample.vcfPath();
        if (vcfPath == null) {
            logger.info("Skipping writing VCF results as no input VCF has been defined");
            return;
        }
        Path outFileName = settings.makeOutputFilePath(vcfPath, OUTPUT_FORMAT);
        Path outPath = Path.of(outFileName + ".gz");
        try (VariantContextWriter writer = variantContextWriterBuilder().setOutputPath(outPath).build()) {
            writeData(analysisResults, settings, vcfPath, writer);
        } catch (Exception e) {
            logger.error("Unable to write results to file {}", outPath, e);
            // The VCFWriter will start to write a compressed VCF file. "The gzip header includes an extra sub-field
            //  with identifier 'BC' and the length of the compressed block, including all headers."
            //  (see https://www.htslib.org/doc/bgzip.html#BGZF_FORMAT) but the file contents will otherwise be empty as
            //  it would have thrown an exception. Delete it so as not to cause too much user WTFery
            cleanUpEmptyFile(outPath);
            cleanUpEmptyFile(Path.of(outPath + ".tbi"));
        }
        logger.debug("{} results written to file {}.", OUTPUT_FORMAT, outFileName);
    }

    private static void cleanUpEmptyFile(Path outPath) {
        try {
            Files.deleteIfExists(outPath);
        } catch (IOException ex) {
            // swallow
        }
    }

    @Override
    public String writeString(AnalysisResults analysisResults, OutputSettings settings) {
        Sample sample = analysisResults.sample();
        Path vcfPath = sample.vcfPath();
        if (vcfPath == null) {
            logger.info("Skipping writing VCF results as no input VCF has been defined. Returning empty string.");
            return "";
        }
        // create a VariantContextWriter writing to a buffer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // don't try to write the string as a BGZipped output.
        try (VariantContextWriter writer = variantContextWriterBuilder().modifyOption(Options.INDEX_ON_THE_FLY, false).setOutputStream(baos).build()) {
            writeData(analysisResults, settings, vcfPath, writer);
        } catch (Exception e) {
            logger.error("Unable to write results to string", e);
        }
        logger.debug("{} results written to string buffer", OUTPUT_FORMAT);
        return baos.toString(StandardCharsets.UTF_8);
    }

    private VariantContextWriterBuilder variantContextWriterBuilder() {
        return new VariantContextWriterBuilder()
                .setOption(Options.ALLOW_MISSING_FIELDS_IN_HEADER);
    }

    private void writeData(AnalysisResults analysisResults, OutputSettings outputSettings, Path vcfPath, VariantContextWriter writer) {
        // n.b. identity is key here as VariantContext doesn't override equals() or hashCode() so don't change the implementation of this map
        Map<VariantContext, List<String>> variantContextAlleleInfoMap = new IdentityHashMap<>();

        GeneScoreRanker geneScoreRanker = new GeneScoreRanker(analysisResults, outputSettings);
        geneScoreRanker.rankedVariants().forEach(rankedVariant -> {
            VariantEvaluation ve = rankedVariant.variantEvaluation();
            String alleleInfo = this.buildVariantRecord(rankedVariant.rank(), ve, rankedVariant.geneScore());
            if (variantContextAlleleInfoMap.containsKey(ve.variantContext())) {
                variantContextAlleleInfoMap.get(ve.variantContext()).add(alleleInfo);
            } else {
                var alleleInfoList = new ArrayList<String>();
                alleleInfoList.add(alleleInfo);
                variantContextAlleleInfoMap.put(ve.variantContext(), alleleInfoList);
            }
        });

        VCFHeader vcfHeader = VcfFiles.readVcfHeader(vcfPath);
        vcfHeader.addMetaDataLine(EXOMISER_VCF_HEADER_METADATA_LINE);

        SAMSequenceDictionary samSequenceDictionary = vcfHeader.getSequenceDictionary();
        if (samSequenceDictionary == null) {
            Map<GeneIdentifier, Gene> genesById = geneScoreRanker.mapGenesByGeneIdentifier();
            var genomicAssembly = genesById.values().stream()
                    .filter(Gene::hasVariants)
                    .flatMap(gene -> gene.variantEvaluations().stream())
                    .findFirst()
                    .map(Variant::genomeAssembly)
                    .orElse(GenomeAssembly.UNKNOWN)
                    .genomicAssembly();
            samSequenceDictionary = createSamSequenceDictionary(genomicAssembly, variantContextAlleleInfoMap.keySet());
            vcfHeader.setSequenceDictionary(samSequenceDictionary);
        }
        writer.writeHeader(vcfHeader);

        if (variantContextAlleleInfoMap.isEmpty() || samSequenceDictionary.isEmpty()) {
            // don't try sorting and writing as the VariantContextComparator will throw an error
            // with no contigs in the samSequenceDictionary
            return;
        }

        variantContextAlleleInfoMap.entrySet().stream()
                .map(entry -> new VariantContextBuilder(entry.getKey())
                        .attribute(EXOMISER_INFO_KEY, String.join(",", entry.getValue()))
                        .make())
                .sorted(new VariantContextComparator(samSequenceDictionary))
                .forEach(writer::add);
    }

    private SAMSequenceDictionary createSamSequenceDictionary(GenomicAssembly genomicAssembly, Set<VariantContext> variantContexts) {
        var unknownContigId = new AtomicInteger(genomicAssembly.contigs().size());
        var contigs = variantContexts.stream()
                .map(VariantContext::getContig)
                .distinct()
                .map(contigName -> {
                    // it's possible that there are other non-canonical contigs, in which case we'll make some new ids for the sequence index
                    Contig contig = genomicAssembly.contigByName(contigName);
                    SAMSequenceRecord samSequenceRecord = new SAMSequenceRecord(contigName, contig.length());
                    samSequenceRecord.setSequenceIndex(contig.isUnknown() ? unknownContigId.incrementAndGet() : contig.id());
                    samSequenceRecord.setAssembly(genomicAssembly.name());
                    samSequenceRecord.setAlternativeSequenceName(contig.isUnknown() ? List.of() : List.of(contig.ucscName(), contig.name(), contig.genBankAccession()));
                    return samSequenceRecord;
                })
                .sorted(Comparator.comparingInt(SAMSequenceRecord::getSequenceIndex))
                .toList();
        return new SAMSequenceDictionary(contigs);
    }

    private String buildVariantRecord(int rank, VariantEvaluation ve, GeneScore geneScore) {
        List<String> fields = new ArrayList<>();
        GeneIdentifier geneIdentifier = geneScore.geneIdentifier();
        ModeOfInheritance modeOfInheritance = geneScore.modeOfInheritance();
        String moiAbbreviation = modeOfInheritance.getAbbreviation() == null ? "ANY" : modeOfInheritance.getAbbreviation();
        List<AcmgAssignment> acmgAssignments = geneScore.acmgAssignments();
        Optional<AcmgAssignment> assignment = acmgAssignments.stream().filter(acmgAssignment -> acmgAssignment.variantEvaluation().equals(ve)).findFirst();
        fields.add(String.valueOf(rank));
        String gnomadString = ve.toGnomad();
        fields.add(gnomadString + "_" + moiAbbreviation);
        fields.add(geneIdentifier.geneSymbol().replace(" ", "_"));
        fields.add(geneIdentifier.entrezId());
        fields.add(moiAbbreviation);
        fields.add(decimalFormat.format(geneScore.pValue()));
        fields.add(decimalFormat.format(geneScore.combinedScore()));
        fields.add(decimalFormat.format(geneScore.phenotypeScore()));
        fields.add(decimalFormat.format(geneScore.variantScore()));
        fields.add(decimalFormat.format(ve.variantScore()));
        fields.add(ve.contributesToGeneScoreUnderMode(modeOfInheritance) ? "1" : "0");
        fields.add(ve.isWhiteListed() ? "1" : "0");
        fields.add(ve.variantEffect().getSequenceOntologyTerm());
        fields.add(this.getRepresentativeAnnotation(ve.transcriptAnnotations()));
        fields.add(assignment.map(AcmgAssignment::acmgClassification).orElse(AcmgClassification.NOT_AVAILABLE).toString());
        fields.add(assignment.map(acmgAssignment -> toVcfAcmgInfo(acmgAssignment.acmgEvidence())).orElse(""));
        fields.add(assignment.map(acmgAssignment -> acmgAssignment.disease().diseaseId()).orElse(""));
        fields.add('"' + assignment.map(acmgAssignment -> acmgAssignment.disease().diseaseName().replace(" ", "_")).orElse("") + '"');
        return "{"+ String.join("|", fields) + "}";
    }

    private String toVcfAcmgInfo(AcmgEvidence acmgEvidence) {
        return acmgEvidence.evidence().entrySet().stream()
                .map(entry -> {
                    AcmgCriterion acmgCriterion = entry.getKey();
                    AcmgCriterion.Evidence evidence = entry.getValue();
                    return (acmgCriterion.evidence() == evidence) ? acmgCriterion.toString() : acmgCriterion + "_" + evidence.displayString();
                })
                .collect(joining(","));
    }

    private String getRepresentativeAnnotation(List<TranscriptAnnotation> annotations) {
        if (annotations.isEmpty()) {
            return "";
        } else {
            TranscriptAnnotation anno = annotations.get(0);
            StringJoiner stringJoiner = new StringJoiner(":");
            stringJoiner.add(anno.geneSymbol());
            stringJoiner.add(anno.accession());
            stringJoiner.add(anno.hgvsCdna());
            stringJoiner.add(anno.hgvsProtein());
            return stringJoiner.toString();
        }
    }
}
