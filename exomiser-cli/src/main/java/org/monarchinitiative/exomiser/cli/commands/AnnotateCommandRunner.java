package org.monarchinitiative.exomiser.cli.commands;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import org.apache.commons.csv.CSVPrinter;
import org.monarchinitiative.exomiser.cli.commands.annotate.AnnotationWriter;
import org.monarchinitiative.exomiser.core.analysis.AnalysisDurationFormatter;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraint;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraints;
import org.monarchinitiative.exomiser.core.analysis.util.acmg.*;
import org.monarchinitiative.exomiser.core.genome.*;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicityData;
import org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource;
import org.monarchinitiative.exomiser.core.prioritisers.dao.DiseaseDao;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.svart.GenomicRegion;
import org.monarchinitiative.svart.GenomicVariant;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.monarchinitiative.svart.sequence.VariantTrimmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.PathogenicitySource.*;

@Component
public class AnnotateCommandRunner implements CommandRunner<AnnotateCommand> {

    private static final Logger logger = LoggerFactory.getLogger(AnnotateCommandRunner.class);
    private static final Set<PathogenicitySource> PATHOGENICITY_SOURCES = EnumSet.of(REVEL, MVP, ALPHA_MISSENSE, SPLICE_AI);
    private final GenomeAnalysisServiceProvider genomeAnalysisServiceProvider;
    private final DiseaseDao diseaseDao;

    public AnnotateCommandRunner(GenomeAnalysisServiceProvider genomeAnalysisServiceProvider, DiseaseDao diseaseDao) {
        this.genomeAnalysisServiceProvider = genomeAnalysisServiceProvider;
        this.diseaseDao = diseaseDao;
    }

    public Integer run(AnnotateCommand annotateCommand) {
        logger.info("Running {}", annotateCommand);
        GenomeAssembly genomeAssembly = annotateCommand.genomeAssembly;
        GenomeAnalysisService genomeAnalysisService = genomeAnalysisServiceProvider.get(genomeAssembly);

        AnnotateCommand.VariantCoordinates variantCoordinates = annotateCommand.inputOption.variant;
        if (variantCoordinates != null) {
            GenomicVariant genomicVariant = variantCoordinates.toGenomicVariant(genomeAssembly.genomicAssembly());
            System.out.println("Annotating: " + genomeAssembly + " " + variantCoordinates);
            // encode as VariantKey (https://doi.org/10.1101/473744) == 8 bytes fixed size (long);
//        System.out.println("AlleleKey size (bytes): " + alleleKey.getSerializedSize()); // SNP = 13 bytes, 11 bases = 23
            VariantEvaluation variantEvaluation = buildAndAnnotateVariantEvaluation(genomeAnalysisService, genomicVariant);

            System.out.println();

            TranscriptAnnotation transcriptAnnotation = variantEvaluation.hasTranscriptAnnotations() ? variantEvaluation.transcriptAnnotations().getFirst() : null;
            Set<VariantEffect> varEffects = variantEvaluation.hasTranscriptAnnotations() ? variantEvaluation.transcriptAnnotations().stream().map(TranscriptAnnotation::variantEffect).collect(Collectors.toUnmodifiableSet()) : Set.of();
            FrequencyData frequencyData = variantEvaluation.frequencyData();
            System.out.println(variantCoordinates + " " + (frequencyData.getRsId().isEmpty() ? "-" : frequencyData.getRsId()) + " " + (variantEvaluation.geneSymbol() + (transcriptAnnotation == null ? "" : ":" + transcriptAnnotation.accession() + ":" + transcriptAnnotation.hgvsCdna() + ":" + transcriptAnnotation.hgvsProtein() + " " + transcriptAnnotation.rankType() + " " + transcriptAnnotation.rank() + "/" + transcriptAnnotation.rankTotal()) + " " + varEffects));
            String geneSymbol = variantEvaluation.geneSymbol();
            Map<String, GeneIdentifier> knownGeneIdentifiers = genomeAnalysisService.getKnownGeneIdentifiers().stream()
                    .collect(toMap(GeneIdentifier::geneSymbol, Function.identity()));
            GeneIdentifier EMPTY_GENE_IDENTIFIER = GeneIdentifier.builder().build();

            GeneIdentifier geneIdentifier = knownGeneIdentifiers.getOrDefault(variantEvaluation.geneSymbol(), EMPTY_GENE_IDENTIFIER);
            GeneConstraint geneConstraint = GeneConstraints.geneConstraint(geneSymbol);
            if (geneIdentifier.hasEntrezId()) {
                System.out.println(geneSymbol + " " + geneIdentifier.hgncId() + " " + (geneConstraint == null ? "" : geneConstraint + (geneConstraint.isLossOfFunctionIntolerant() ? (" (LOF INTOLERANT)") : "")));
            }
            PathogenicityData pathogenicityData = variantEvaluation.pathogenicityData();
            ClinVarData clinVarData = pathogenicityData.clinVarData();
            System.out.println("ClinVar: " + (clinVarData.isEmpty() ? "-" : formatClinVarData(clinVarData)));
            System.out.println("Variant score: " + variantEvaluation.variantScore() + " Frequency score: " + variantEvaluation.frequencyScore() + " Pathogenicity score: " + variantEvaluation.pathogenicityScore() + (variantEvaluation.isWhiteListed() ? " (WHITELIST VARIANT - all scores set to 1.0)" : ""));
            System.out.println("Frequency score: " + frequencyData.frequencyScore());
            if (frequencyData.isEmpty()) {
                System.out.println("\t(no frequency data)");
            } else {
                frequencyData.frequencies().forEach(freq -> System.out.println("\t" + freq.source() + "=" + freq.frequency() + "(" + freq.ac() + "|" + freq.an() + "|" + freq.homs() + ")"));
            }
            System.out.println("Pathogenicity score: " + pathogenicityData.pathogenicityScore());
            if (pathogenicityData.isEmpty()) {
                System.out.println("\t(no pathogenicity data)");
            } else {
                pathogenicityData.pathogenicityScores().forEach(path -> System.out.println("\t" + path));
            }
            // Exomiser ACMG
            AcmgEvidenceAssigner acmgEvidenceAssigner = new Acmg2015EvidenceAssigner("sample", Pedigree.justProband("sample"), genomeAnalysisService);
            Disease EMPTY_DISEASE = Disease.builder().build();
            List<Disease> diseases = diseaseDao.getDiseaseDataAssociatedWithGeneId(Integer.parseInt(geneIdentifier.entrezId()));
            ModeOfInheritance modeOfInheritance = findMoiForGene(diseases);
            AcmgEvidence acmgEvidence = acmgEvidenceAssigner.assignVariantAcmgEvidence(variantEvaluation, modeOfInheritance, List.of(variantEvaluation), diseases, List.of());
            AcmgEvidenceClassifier classifier = new Acmg2020PointsBasedClassifier();
            AcmgClassification acmgClassification = classifier.classify(acmgEvidence);
            // should use an AcmgAssignmentCalculator to figure out the correct disease-gene association to use
            System.out.println();
            System.out.println("ACMG Assignments:");
            acmgEvidence.evidence().forEach(
                    (acmgCriterion, evidence) -> System.out.println(acmgCriterion + (acmgCriterion.evidence().equals(evidence) ? "" : "_" + evidence.displayString()) + " : " + (acmgCriterion.isBenign() ? -evPoints(evidence) : evPoints(evidence)) + " \"" + acmgCriterion.description() + "\"")
            );
            System.out.println("--------");
            System.out.println("Classification: " + acmgClassification + " " + acmgEvidence);
            System.out.println("ACMG Score: " + acmgEvidence.points());
            System.out.println("Post Prob Path: " + acmgEvidence.postProbPath());

            //GeneStatistics geneStatistics = genomeAnalysisService.getGeneStatistics(geneSymbol);
            // pretty print these here?

            System.out.println();
            // ClinVar region data
            GenomicRegion clinVarSearchInterval = genomicVariant.withPadding(25, 25);
            String interval = clinVarSearchInterval.contig().name() + ":" + clinVarSearchInterval.startOneBased() + "-" + clinVarSearchInterval.end();
            System.out.println("ClinVar neighbourhood " + interval + " (" + genomicVariant.start() + " +/-25 bases):");
            Map<GenomicVariant, ClinVarData> clinVarRecordsOverlappingInterval = genomeAnalysisService.findClinVarRecordsOverlappingInterval(clinVarSearchInterval);
            clinVarRecordsOverlappingInterval.forEach((variant, data) -> System.out.println(toBroad(variant) + " : " + formatClinVarData(data)));
            System.out.println();
            // G2D associations
            if (geneIdentifier.hasEntrezId()) {
                System.out.println("Known diseases associated with " + variantEvaluation.geneSymbol() + ":");
                for (Disease disease1 : diseases) {
                    System.out.printf("%s\t%s\t%s%n", disease1.diseaseId(), formatMoi(disease1.inheritanceMode()), disease1.diseaseName());
                }
            }
            return 0;
        }

        AcmgEvidenceClassifier acmgClassifier = new Acmg2020PointsBasedClassifier();
        AcmgEvidenceAssigner acmgEvidenceAssigner = new Acmg2015EvidenceAssigner("sample", Pedigree.justProband("sample"), genomeAnalysisService);

        Path vcfPath = annotateCommand.inputOption.vcfPath;
        if (vcfPath != null) {
            Instant start = Instant.now();
            logger.info("Annotating {}", vcfPath);
            // iterate through VCF records
            GenomicAssembly genomicAssembly = genomeAssembly.genomicAssembly();
            VariantContextConverter variantContextConverter = VariantContextConverter.of(genomicAssembly, VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
            Map<String, GeneIdentifier> knownGeneIdentifiers = genomeAnalysisService.getKnownGeneIdentifiers().stream()
                    .collect(toMap(GeneIdentifier::geneSymbol, Function.identity()));
            GeneIdentifier EMPTY_GENE_IDENTIFIER = GeneIdentifier.builder().build();
            Disease EMPTY_DISEASE = Disease.builder().build();
            Locale.setDefault(Locale.UK);
            Path outPath = Path.of(vcfPath.getFileName().toString().replace(".gz", "").replace(".vcf", "-exomiser-annotations.tsv"));
            try (VCFFileReader vcfReader = new VCFFileReader(vcfPath, false);
                 CloseableIterator<VariantContext> it = vcfReader.iterator();
//                 VariantContextWriter vcfWriter = new VariantContextWriterBuilder().setReferenceDictionary(samSequenceDictionary)
//                         .setOutputPath(Path.of("."))
//                         .setOption(Options.INDEX_ON_THE_FLY)
//                         .setOption(Options.USE_ASYNC_IO)
//                         .build()
                 CSVPrinter printer = new CSVPrinter(Files.newBufferedWriter(outPath, StandardCharsets.UTF_8), AnnotationWriter.EXOMISER_VARIANTS_TSV_FORMAT)
            ) {

                int variantsRead = 0;
                while (it.hasNext()) {
                    VariantContext variantContext = it.next();
                    variantsRead++;
                    // decompose and trim alleles
//                    Map<String, String> attributes = new HashMap<>();
                    logger.debug("Read {}", variantContext);
                    for (Allele allele : variantContext.getAlternateAlleles()) {
                        GenomicVariant genomicVariant = variantContextConverter.convertToVariant(variantContext, allele);
                        VariantEvaluation variantEvaluation = buildAndAnnotateVariantEvaluation(genomeAnalysisService, genomicVariant);
                        GeneIdentifier geneIdentifier = knownGeneIdentifiers.getOrDefault(variantEvaluation.geneSymbol(), EMPTY_GENE_IDENTIFIER);
                        // Exomiser ACMG
                        List<Disease> diseases = diseaseDao.getDiseaseDataAssociatedWithGeneId(Integer.parseInt(geneIdentifier.entrezId()));
                        ModeOfInheritance modeOfInheritance = findMoiForGene(diseases);
                        AcmgEvidence acmgEvidence = acmgEvidenceAssigner.assignVariantAcmgEvidence(variantEvaluation, modeOfInheritance, List.of(variantEvaluation), diseases, List.of());
                        AcmgClassification acmgClassification = acmgClassifier.classify(acmgEvidence);
                        Disease disease = diseases.isEmpty() ? EMPTY_DISEASE : diseases.getFirst();
                        AcmgAssignment acmgAssignment = AcmgAssignment.of(variantEvaluation, geneIdentifier, modeOfInheritance, disease, acmgEvidence, acmgClassification);
                        logger.debug("{} {} points={} {}", acmgClassification, acmgEvidence, acmgEvidence.points(), variantEvaluation);
                        List<Object> annotations = AnnotationWriter.buildVariantRecord(variantEvaluation, acmgAssignment);
                        printer.printRecord(annotations);
                        printer.flush();
                    }
                }
                Duration runtime = Duration.between(start, Instant.now());
                long ms = runtime.toMillis();
                String formatted = AnalysisDurationFormatter.format(runtime);
                logger.info("Annotated {} variants in {} ({} ms)", variantsRead, formatted, ms);
                logger.info("Wrote results to {}", outPath.toAbsolutePath());
            } catch (IOException e) {
                logger.error("Unable to annotate file {}", vcfPath, e);
                return -1;
            }
        }
        return 0;
    }

    private int evPoints(AcmgCriterion.Evidence evidence) {
        return switch (evidence) {
            case STAND_ALONE, VERY_STRONG -> 8;
            case STRONG -> 4;
            case MODERATE -> 2;
            case SUPPORTING -> 1;
        };
    }

    private ModeOfInheritance findMoiForGene(List<Disease> diseases) {
        if (diseases.isEmpty()) {
            return ModeOfInheritance.ANY;
        }
        Set<ModeOfInheritance> mois = EnumSet.noneOf(ModeOfInheritance.class);
        for (Disease disease : diseases) {
            // diseases can be AD and AR
            InheritanceMode inheritanceMode = disease.inheritanceMode();
            switch (inheritanceMode) {
                case AUTOSOMAL_DOMINANT, X_DOMINANT -> mois.add(ModeOfInheritance.AUTOSOMAL_DOMINANT);
                case AUTOSOMAL_RECESSIVE, X_LINKED, X_RECESSIVE, AUTOSOMAL_DOMINANT_AND_RECESSIVE, Y_LINKED ->
                    //Y_LINKED is DOMINANT in males, however given gnomAD has male and female together in the counts, we'll expect a very low AF
                        mois.add(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
                case MITOCHONDRIAL -> mois.add(ModeOfInheritance.MITOCHONDRIAL);
                case UNKNOWN, SOMATIC, POLYGENIC -> mois.add(ModeOfInheritance.ANY);
            }
        }
        if (mois.isEmpty()) {
            return ModeOfInheritance.ANY;
        } else if (mois.size() == 1) {
            return mois.iterator().next();
            // if more than one, defer to recessive model as this usually has more permissive criteria
        } else if (mois.contains(ModeOfInheritance.AUTOSOMAL_RECESSIVE)) {
            return ModeOfInheritance.AUTOSOMAL_RECESSIVE;
        } else if (mois.contains(ModeOfInheritance.AUTOSOMAL_DOMINANT)) {
            return ModeOfInheritance.AUTOSOMAL_DOMINANT;
        } else if (mois.contains(ModeOfInheritance.MITOCHONDRIAL)) {
            return ModeOfInheritance.MITOCHONDRIAL;
        }
        return ModeOfInheritance.ANY;
    }

    private VariantEvaluation buildAndAnnotateVariantEvaluation(GenomeAnalysisService genomeAnalysisService, GenomicVariant genomicVariant) {
        VariantAnnotator variantAnnotator = genomeAnalysisService.getVariantAnnotator();
        List<VariantAnnotation> variantAnnotations = variantAnnotator.annotate(genomicVariant);
        VariantAnnotation variantAnnotation = variantAnnotations.isEmpty() ? null : variantAnnotations.get(0);
        VariantEvaluation.Builder variantEvaluationBuilder = VariantEvaluation.builder()
                .variant(genomicVariant);
        if (variantAnnotation != null) {
            variantEvaluationBuilder
                    .geneId(variantAnnotation.geneId())
                    .geneSymbol(variantAnnotation.geneSymbol())
                    .variantEffect(variantAnnotation.variantEffect())
                    .transcriptAnnotations(variantAnnotation.transcriptAnnotations());
        }
        var variantEvaluation = variantEvaluationBuilder.build();
        FrequencyData frequencyData = genomeAnalysisService.getVariantFrequencyData(variantEvaluation, FrequencySource.NON_FOUNDER_POPS);
        variantEvaluation.setFrequencyData(frequencyData);
        PathogenicityData pathogenicityData = genomeAnalysisService.getVariantPathogenicityData(variantEvaluation, PATHOGENICITY_SOURCES);
        variantEvaluation.setPathogenicityData(pathogenicityData);
        return variantEvaluation;
    }

    private String formatMoi(InheritanceMode inheritanceMode) {
        return switch (inheritanceMode) {
            case UNKNOWN -> "unknown";
            case AUTOSOMAL_RECESSIVE -> "AR";
            case AUTOSOMAL_DOMINANT -> "AD";
            case AUTOSOMAL_DOMINANT_AND_RECESSIVE -> "AD/AR";
            case X_LINKED -> "XD/XR";
            case X_RECESSIVE -> "XR";
            case X_DOMINANT -> "XD";
            case Y_LINKED -> "Y";
            case SOMATIC -> "Somatic";
            case MITOCHONDRIAL -> "M";
            case POLYGENIC -> "Polygenic";
        };
    }

    private String formatClinVarData(ClinVarData clinVarData) {
        return clinVarData.primaryInterpretation() + (clinVarData.primaryInterpretation() == ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS ? " " + clinVarData.conflictingInterpretationCounts() : "") + " (" + clinVarData.starRating() + "*) " + clinVarData.variationId() + " " + clinVarData.geneSymbol() + ":" + clinVarData.hgvsCdna() + ":" + clinVarData.hgvsProtein() + " " + clinVarData.variantEffect() + " (" + clinVarData.reviewStatus() + ")";
    }

    private String toBroad(GenomicVariant genomicVariant) {
        return genomicVariant.contig().name() + '-' + genomicVariant.start() + '-' + genomicVariant.ref() + '-' + genomicVariant.alt();
    }


}
