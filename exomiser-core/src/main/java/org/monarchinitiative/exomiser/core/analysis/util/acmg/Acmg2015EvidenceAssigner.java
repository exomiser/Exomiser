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

package org.monarchinitiative.exomiser.core.analysis.util.acmg;

import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraint;
import org.monarchinitiative.exomiser.core.analysis.util.GeneConstraints;
import org.monarchinitiative.exomiser.core.analysis.util.InheritanceModeAnalyser;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.genome.dao.ClinVarDao;
import org.monarchinitiative.exomiser.core.model.*;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;
import org.monarchinitiative.exomiser.core.phenotype.ModelPhenotypeMatch;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.svart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.monarchinitiative.exomiser.core.analysis.util.acmg.AcmgCriterion.*;

/**
 * @since 13.1.0
 */
public class Acmg2015EvidenceAssigner implements AcmgEvidenceAssigner {

    private static final Logger logger = LoggerFactory.getLogger(Acmg2015EvidenceAssigner.class);

    // Variants to be excluded from being assigned BA1 as specified by the ClinGen SVI working group in:
    //   https://www.clinicalgenome.org/site/assets/files/3460/ba1_exception_list_07_30_2018.pdf
    private static final Set<AlleleProto.AlleleKey> HG19_BA1_EXCLUSION_VARIANTS = Set.of(
            // ClinVar 1018 - 3: 128598490 (GRCh37) 128879647 (GRCh38) (SPDI: NC_000003.12:128879647:TAAG:TAAGTAAG)
            AlleleProtoAdaptor.toAlleleKey(3, 128_598_490, "C", "CTAAG"),
            // ClinVar 17023 - 13: 20763612 (GRCh37) 20189473 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(13, 20_763_612, "C", "T"),
            // ClinVar 10 - 6: 26091179 (GRCh37) 26090951 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(6, 26_091_179, "C", "G"),
            // ClinVar 9 - 6: 26093141 (GRCh37) 26092913 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(6, 26_093_141, "G", "A"),
            // ClinVar 2551 - 16: 3299586 (GRCh37) 3249586 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(16, 3_299_586, "G", "A"),
            // ClinVar 2552 - 16: 3299468 (GRCh37) 3249468 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(16, 3_299_468, "C", "T"),
            // ClinVar 217689 - 13: 73409497 (GRCh37) 72835359 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(13, 73_409_497, "G", "A"),
            // ClinVar 3830 - 12: 121175678 (GRCh37) 120737875 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(12, 121_175_678, "C", "T"),
            // ClinVar 1900 - 3: 15686693 (GRCh37) 15645186 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(3, 15_686_693, "G", "C")
    );
    private static final Set<AlleleProto.AlleleKey> HG38_BA1_EXCLUSION_VARIANTS = Set.of(
            // ClinVar 1018 - 3: 128598490 (GRCh37) 128879647 (GRCh38) (SPDI: NC_000003.12:128879647:TAAG:TAAGTAAG)
            AlleleProtoAdaptor.toAlleleKey(3, 128_879_647, "C", "CTAAG"),
            // ClinVar 17023 - 13: 20763612 (GRCh37) 20189473 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(13, 20_189_473, "C", "T"),
            // ClinVar 10 - 6: 26091179 (GRCh37) 26090951 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(6, 26_090_951, "C", "G"),
            // ClinVar 9 - 6: 26093141 (GRCh37) 26092913 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(6, 26_092_913, "G", "A"),
            // ClinVar 2551 - 16: 3299586 (GRCh37) 3249586 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(16, 3_249_586, "G", "A"),
            // ClinVar 2552 - 16: 3299468 (GRCh37) 3249468 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(16, 3_249_468, "C", "T"),
            // ClinVar 217689 - 13: 73409497 (GRCh37) 72835359 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(13, 72_835_359, "G", "A"),
            // ClinVar 3830 - 12: 121175678 (GRCh37) 120737875 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(12, 120_737_875, "C", "T"),
            // ClinVar 1900 - 3: 15686693 (GRCh37) 15645186 (GRCh38)
            AlleleProtoAdaptor.toAlleleKey(3, 15_645_186, "G", "C")
    );

    // e.g. p.(Lys567Thr)
    private static final Pattern MISSENSE_HGVS_P = Pattern.compile("p\\.\\((?<refPos>[A-Z][a-z]{2}\\d+)(?<alt>[A-Z][a-z]{2})\\)");
    private final ClinVarDao clinVarDao;


    private final String probandId;
    private final Individual.Sex probandSex;
    private final Pedigree pedigree;

    public Acmg2015EvidenceAssigner(String probandId, Pedigree pedigree, ClinVarDao clinVarDao) {
        this.probandId = Objects.requireNonNull(probandId);
        this.pedigree = pedigree == null || pedigree.isEmpty() ? Pedigree.justProband(probandId) : pedigree;
        Individual proband = this.pedigree.getIndividualById(probandId);
        if (proband == null) {
            throw new IllegalArgumentException("Proband '" + probandId + "' not found in pedigree " + pedigree);
        }
        this.probandSex = proband.getSex();
        this.clinVarDao = clinVarDao;
    }

    /**
     * Assesses the provided {@link VariantEvaluation} against the ACMG criteria (Richards et al. 2015 doi:10.1038/gim.2015.30).
     * This class will assign the criteria codes and evidence strength based on the current {@link ModeOfInheritance}
     * being tested, the known diseases for this MOI, other contributing variants, the patients pedigree, and the phenotypic
     * similarity of the patient to the compatible diseases.
     *
     * <b>CAUTION! This method expects the variant evaluation to be compatible with the given mode of inheritance.</b> This
     * class is expected to be run downstream of an {@link InheritanceModeAnalyser} and accessed via the {@link AcmgAssignmentCalculator}. Failure to do this will result in
     * incorrect assignments.
     *
     * @param variantEvaluation
     * @param modeOfInheritance
     * @param contributingVariants
     * @param knownDiseases
     * @param compatibleDiseaseMatches
     * @return
     */
    // https://www.ncbi.nlm.nih.gov/clinvar/variation/464/ - check in ClinVar VCF if there is MOI information for a classification
    public AcmgEvidence assignVariantAcmgEvidence(VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<VariantEvaluation> contributingVariants, List<Disease> knownDiseases, List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches) {
        AcmgEvidence.Builder acmgEvidenceBuilder = AcmgEvidence.builder();

        FrequencyData frequencyData = variantEvaluation.getFrequencyData();
        // BA1 "Allele frequency is >5% in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
        // Updated recommendation: "Allele frequency is >0.05 in any general continental population dataset of at least
        // 2,000 observed alleles and found in a gene without a gene- or variant-specific BA1 modification." i.e. ExAC
        // African, East Asian, European [non-Finnish], Latino, and South Asian
        AlleleProto.AlleleKey alleleKey = variantEvaluation.alleleKey();
        boolean isBa1ExcludedVariant = variantEvaluation.getGenomeAssembly() == GenomeAssembly.HG19 ? HG19_BA1_EXCLUSION_VARIANTS.contains(alleleKey) : HG38_BA1_EXCLUSION_VARIANTS.contains(alleleKey);
        if (!isBa1ExcludedVariant && frequencyData.maxFreqForPopulation(FrequencySource.NON_FOUNDER_POPS) >= 5.0) {
            acmgEvidenceBuilder.add(BA1);
            // BA1 is supposed to be used as a filtering criterion where no other evidence need be considered.
            return acmgEvidenceBuilder.build();
        }
        // PM2 "Absent from controls (or at extremely low frequency if recessive) in Exome Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium"
        assignPM2(acmgEvidenceBuilder, frequencyData, modeOfInheritance);


        boolean hasCompatibleDiseaseMatches = !compatibleDiseaseMatches.isEmpty();

        // PVS1 "null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease"
        assignPVS1(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, knownDiseases);

        // ignore non-missense, truncating, splice or mitochondrial variants
        if (isMissenseOrInframeIndel(variantEvaluation.getVariantEffect()) && variantEvaluation.contigId() != 25) {
            // ensure region is within contig bounds
            Contig contig = variantEvaluation.contig();
            var upStream = Math.max(1, variantEvaluation.start() - 25);
            var downStream = Math.min(contig.length(), variantEvaluation.start() + 25);
            GenomicInterval genomicInterval = GenomicInterval.of(contig, Strand.POSITIVE, Coordinates.oneBased(upStream, downStream));
            var localClinVarData = clinVarDao.findClinVarRecordsOverlappingInterval(genomicInterval);
            // PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
            // PM5 "Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before"
            assignPS1PM5(acmgEvidenceBuilder, variantEvaluation, localClinVarData);
            // PM1 "Located in a mutational hot spot and/or critical and well-established functional domain (e.g., active site of an enzyme) without benign variation"
            assignPM1(acmgEvidenceBuilder, variantEvaluation, localClinVarData);
            // TODO: PM1/BP3 "In-frame deletions/insertions in a repetitive region without a known function" - requires domain information.
        }

        if (pedigree.containsId(probandId)) {
            Individual proband = pedigree.getIndividualById(probandId);
            // PS2 "De novo (both maternity and paternity confirmed) in a patient with the disease and no family history"
            assignPS2(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, contributingVariants, hasCompatibleDiseaseMatches, proband);
            // PM6 "Assumed de novo, but without confirmation of paternity and maternity"
//            assignPM6(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, contributingVariants, hasCompatibleDiseaseMatches);
            // BS4 "Lack of segregation in affected members of a family"
            assignBS4(acmgEvidenceBuilder, variantEvaluation, proband);
        }

        // PM3 "For recessive disorders, detected in trans with a pathogenic variant"
        assignPM3orBP2(acmgEvidenceBuilder, variantEvaluation, modeOfInheritance, contributingVariants, hasCompatibleDiseaseMatches);
        // PM4 Protein length changes as a result of in-frame deletions/insertions in a nonrepeat region or stop-loss variants
        assignPM4(acmgEvidenceBuilder, variantEvaluation);

        // PP4 "Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology"
        assignPP4(acmgEvidenceBuilder, compatibleDiseaseMatches);

        PathogenicityData pathogenicityData = variantEvaluation.getPathogenicityData();
        ClinVarData clinVarData = variantEvaluation.getPathogenicityData().clinVarData();
        if (!clinVarData.isEmpty()) {
            // PP5 "Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation"
            assignPP5(acmgEvidenceBuilder, clinVarData);
            // BP6 "Reputable source recently reports variant as benign, but the evidence is not available to the laboratory to perform an independent evaluation"
            assignBP6(acmgEvidenceBuilder, clinVarData);
        }
        // PP3 "Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc.)"
        // BP4 "Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc.)"
        // TODO: internalise in PathogenicityData and create proper cutoff values adding isPath/isbenign to PathogenicityScore
        assignPP3orBP4(acmgEvidenceBuilder, pathogenicityData);

        return acmgEvidenceBuilder.build();
    }

    /**
     * PVS1 "null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease"
     */
    private void assignPVS1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<Disease> knownDiseases) {
        // TODO: add new method - gene.getAssociatedDiseases()
        //  also need ClinvarCache.getClinvarDataForGene(GeneIdentifier geneIdentifier)

        // Certain types of variants (e.g., nonsense, frameshift, canonical ±1 or 2 splice sites, initiation codon, single exon or multiexon
        // deletion) can often be assumed to disrupt gene function by leading to a complete absence of the gene product by lack of tran-
        // scription or nonsense-mediated decay of an altered transcript. One  must,  however,  exercise  caution  when  classifying  these
        // variants as pathogenic by considering the following principles:
        //  (i)  When  classifying  such  variants  as  pathogenic,  one  must ensure  that  null  variants  are  a  known  mechanism  of
        //  pathogenicity consistent with the established inheritance pattern  for  the  disease.  For  example,  there  are  genes  for
        //  which only heterozygous missense variants cause disease and null variants are benign in a heterozygous state (e.g.,
        //  many hypertrophic cardiomyopathy genes). A novel heterozygous  nonsense  variant  in  the  MYH7  gene  would
        //  not be considered pathogenic for dominant hypertrophic cardiomyopathy based solely on this evidence, whereas a
        //  novel  heterozygous  nonsense  variant  in  the  CFTR  gene would likely be considered a recessive pathogenic variant.
        // Caveats:
        // •  Beware of genes where LOF is not a known disease mechanism (e.g., GFAP, MYH7)
        // •  Use caution interpreting LOF variants at the extreme 3′ end of a gene
        // •  Use caution with splice variants that are predicted to lead to exon skipping but leave the remainder of the protein intact
        // •  Use caution in the presence of multiple transcripts

        GeneConstraint geneContraint = GeneConstraints.geneConstraint(variantEvaluation.getGeneSymbol());
        // Should this be using the hasCompatibleDiseaseMatches variable?
        boolean inGeneWithKnownDiseaseAssociations = !knownDiseases.isEmpty();
        if (inGeneWithKnownDiseaseAssociations && isLossOfFunctionEffect(variantEvaluation.getVariantEffect())
            && (geneContraint != null && geneContraint.isLossOfFunctionIntolerant())
        ) {
            if (variantEvaluation.hasTranscriptAnnotations()) {
                TranscriptAnnotation transcriptAnnotation = variantEvaluation.getTranscriptAnnotations().get(0);
                if (predictedToLeadToNmd(transcriptAnnotation)) {
                    acmgEvidenceBuilder.add(PVS1);
                } else {
                    // Not predicted to lead to NMD? Downgrade to STRONG
                    acmgEvidenceBuilder.add(PVS1, Evidence.STRONG);
                }
            } else {
                // shouldn't happen that there are no transcript annotations, but just in case...
                acmgEvidenceBuilder.add(PVS1, Evidence.STRONG);
            }
        }
    }

    private boolean isLossOfFunctionEffect(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.START_LOST
               || variantEffect == VariantEffect.STOP_LOST
               || variantEffect == VariantEffect.STOP_GAINED
               || variantEffect == VariantEffect.FRAMESHIFT_ELONGATION
               || variantEffect == VariantEffect.FRAMESHIFT_TRUNCATION
               || variantEffect == VariantEffect.FRAMESHIFT_VARIANT
               || variantEffect == VariantEffect.SPLICE_ACCEPTOR_VARIANT
               || variantEffect == VariantEffect.SPLICE_DONOR_VARIANT
               || variantEffect == VariantEffect.EXON_LOSS_VARIANT;
    }

    private boolean predictedToLeadToNmd(TranscriptAnnotation transcriptAnnotation) {
        // predicted to lead to NMD if in last exon or last 50bp of penultimate exon, or in single exon transcript
        boolean notInLastExon = transcriptAnnotation.getRank() < transcriptAnnotation.getRankTotal();
        boolean isSingleExonTranscript = transcriptAnnotation.getRankTotal() == 1;
        return transcriptAnnotation.getRankType() == TranscriptAnnotation.RankType.EXON && (notInLastExon || isSingleExonTranscript);
    }

    private boolean compatibleWithRecessive(ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
            return true;
        }
        return probandSex == Individual.Sex.FEMALE && modeOfInheritance == ModeOfInheritance.X_RECESSIVE;
    }

    private boolean compatibleWithDominant(ModeOfInheritance modeOfInheritance) {
        if (modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT) {
            return true;
        }
        if (probandSex == Individual.Sex.MALE && (modeOfInheritance == ModeOfInheritance.X_RECESSIVE || modeOfInheritance == ModeOfInheritance.X_DOMINANT)) {
            return true;
        }
        return (probandSex == Individual.Sex.FEMALE || probandSex == Individual.Sex.UNKNOWN) && modeOfInheritance == ModeOfInheritance.X_DOMINANT;
    }

    /**
     * PM3 "For recessive disorders, detected in trans with a pathogenic variant"
     * Note: This requires testing of parents (or offspring) to determine phase
     */
    private void assignPM3orBP2(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<VariantEvaluation> contributingVariants, boolean hasCompatibleDiseaseMatches) {
        if (hasCompatibleDiseaseMatches) {
            SampleGenotype thisVariantGenotype = variantEvaluation.getSampleGenotype(probandId);
            if (contributingVariants.size() >= 2 && contributingVariants.contains(variantEvaluation)) {
                for (VariantEvaluation otherVariant : contributingVariants) {
                    SampleGenotype otherVariantGenotype = otherVariant.getSampleGenotype(probandId);
                    ClinVarData otherClinVarData = otherVariant.getPathogenicityData().clinVarData();
                    if (otherClinVarData.getPrimaryInterpretation() == ClinVarData.ClinSig.PATHOGENIC && !otherVariant.equals(variantEvaluation)) {
                        // X = this variant, P = other pathogenic variant
                        boolean inTrans = inTrans(thisVariantGenotype, otherVariantGenotype);
                        boolean inCis = inCis(thisVariantGenotype, otherVariantGenotype);
                        if (inTrans && modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
                            //     -------P- (AR)
                            //     ---X------
                            acmgEvidenceBuilder.add(PM3);
                        } else if (inCis && modeOfInheritance == ModeOfInheritance.AUTOSOMAL_RECESSIVE) {
                            //     ---X---P- (AR)
                            //     ---------
                            acmgEvidenceBuilder.add(BP2);
                        } else if (inTrans && modeOfInheritance == ModeOfInheritance.AUTOSOMAL_DOMINANT) {
                            //     -------P-  (AD)
                            //     ---X------
                            acmgEvidenceBuilder.add(BP2);
                        }
                    }
                }
            }
        }
    }

    private boolean inTrans(SampleGenotype thisVariantGenotype, SampleGenotype otherVariantGenotype) {
        // GT in trans = (1|0 && 0|1) (variant in both copies of gene)
        // GT in cis   = (1|0 && 1|0) or (0|1 && 0|1) (variant in same copy of gene)
        return thisVariantGenotype.isPhased() && thisVariantGenotype.isHet()
               && otherVariantGenotype.isPhased() && otherVariantGenotype.isHet()
               && !thisVariantGenotype.equals(otherVariantGenotype);
    }

    private boolean inCis(SampleGenotype thisVariantGenotype, SampleGenotype otherVariantGenotype) {
        // GT in trans = (1|0 && 0|1) (variant in both copies of gene)
        // GT in cis   = (1|0 && 1|0) or (0|1 && 0|1) (variant in same copy of gene)
        return thisVariantGenotype.isPhased() && thisVariantGenotype.isHet()
               && otherVariantGenotype.isPhased() && otherVariantGenotype.isHet()
               && thisVariantGenotype.equals(otherVariantGenotype);
    }


    // PM1 "Located in a mutational hot spot and/or critical and well-established functional domain (e.g., active site of an enzyme) without benign variation"
    private void assignPM1(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, Map<GenomicVariant, ClinVarData> localClinVarData) {
        // TODO - need UniProt domain / site info and clinvar counts
        //  can upgrade to STRONG
        // https://www.cell.com/ajhg/fulltext/S0002-9297(22)00461-X suggests to limit the combined evidence from PM1 and PP3 to strong
        int pathCount = 0;
        int vusCount = 0;
        int benignCount = 0;
        for (var entry : localClinVarData.entrySet()) {
            ClinVarData clinVarData = entry.getValue();
            ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
            if (isMissenseOrInframeIndel(clinVarData.getVariantEffect())) {
                if (isPathOrLikelyPath(primaryInterpretation)) {
                    pathCount++;
                }
                if (primaryInterpretation == ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE) {
                    vusCount++;
                }
                if (isBenignOrLikelyBenign(primaryInterpretation)) {
                    benignCount++;
                }
            }
        }
        logger.debug("PM1 evidence for region {} {}:{}-{} Paths: {} VUSs: {} Benigns: {}", variantEvaluation.getGenomeAssembly(), variantEvaluation.contigId(), variantEvaluation.start() - 25, variantEvaluation.end() + 25, pathCount, vusCount, benignCount);
        if (pathCount >= 4 && benignCount == 0) {
            // could do funkier thing to score other path variants by severity/star rating and distance to variant
            if (pathCount > vusCount) {
                acmgEvidenceBuilder.add(PM1);
                logger.debug("{} -> {}", variantEvaluation, PM1);
            } else {
                acmgEvidenceBuilder.add(PM1, Evidence.SUPPORTING);
                logger.debug("{} -> {}_Supporting", variantEvaluation, PM1);
            }
        }
    }

    private void assignPS1PM5(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, Map<GenomicVariant, ClinVarData> localClinVarData) {
        if (variantEvaluation.getTranscriptAnnotations().isEmpty()) {
            return;
        }
        String variantProteinChange = variantEvaluation.getTranscriptAnnotations().get(0).getHgvsProtein();
        Matcher queryVariantMatcher = MISSENSE_HGVS_P.matcher(variantProteinChange);
        boolean matches = queryVariantMatcher.matches();
        String aaRefPos = matches ? queryVariantMatcher.group("refPos") : "";
        String aaAlt = matches ? queryVariantMatcher.group("alt") : "";
        for (var entry : localClinVarData.entrySet()) {
            ClinVarData clinVarData = entry.getValue();
            ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
            if (isMissenseOrInframeIndel(clinVarData.getVariantEffect()) && isPathOrLikelyPath(primaryInterpretation)) {
                GenomicVariant clinVarVariant = entry.getKey();
                if (Math.abs(clinVarVariant.distanceTo(variantEvaluation)) <= 2 && GenomicVariant.compare(clinVarVariant, variantEvaluation) != 0) {
                    // within codon so check for same AA change or different AA change
                    Matcher clinvarMatcher = MISSENSE_HGVS_P.matcher(clinVarData.getHgvsProtein());
                    if (clinvarMatcher.matches()) {
                        String clnAaRefPos = clinvarMatcher.group("refPos");
                        String clnAaAlt = clinvarMatcher.group("alt");
                        if (aaRefPos.equals(clnAaRefPos)) {
                            if (aaAlt.equals(clnAaAlt)) {
                                // PS1 "Same amino acid change as a previously established pathogenic variant regardless of nucleotide change"
                                Evidence evidence;
                                if (clinVarData.starRating() >= 2) {
                                    evidence = Evidence.STRONG;
                                } else if (clinVarData.starRating() == 1) {
                                    evidence = Evidence.MODERATE;
                                } else {
                                    evidence = Evidence.SUPPORTING;
                                }
                                logger.debug("{} -> {}_{}", clinVarData.getHgvsProtein(), PS1, evidence);
                                acmgEvidenceBuilder.add(PS1, evidence);
                            } else {
                                Evidence evidence = clinVarData.starRating() >= 2 ? Evidence.MODERATE : Evidence.SUPPORTING;
                                logger.debug("{} -> {}_{}", clinVarData.getHgvsProtein(), PM5, evidence);
                                // PM5 "Novel missense change at an amino acid residue where a different missense change determined to be pathogenic has been seen before"
                                acmgEvidenceBuilder.add(PM5, evidence);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isMissenseOrInframeIndel(VariantEffect variantEffect) {
        return variantEffect == VariantEffect.MISSENSE_VARIANT || variantEffect == VariantEffect.INFRAME_DELETION || variantEffect == VariantEffect.INFRAME_INSERTION;
    }

    /**
     * PP5 "Reputable source recently reports variant as pathogenic, but the evidence is not available to the laboratory to perform an independent evaluation"
     */
    private void assignPP5(AcmgEvidence.Builder acmgEvidenceBuilder, ClinVarData clinVarData) {
        ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
        boolean pathOrLikelyPath = isPathOrLikelyPath(primaryInterpretation);
        if (pathOrLikelyPath && clinVarData.starRating() == 1) {
            acmgEvidenceBuilder.add(PP5);
        } else if (pathOrLikelyPath && clinVarData.starRating() == 2) {
            // multiple submitters, no conflicts
            acmgEvidenceBuilder.add(PP5, Evidence.STRONG);
        } else if (pathOrLikelyPath && clinVarData.starRating() >= 3) {
            // expert panel or practice guidelines
            acmgEvidenceBuilder.add(PP5, Evidence.VERY_STRONG);
        }
    }

    private boolean isPathOrLikelyPath(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.PATHOGENIC || clinSig == ClinVarData.ClinSig.LIKELY_PATHOGENIC || clinSig == ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC;
    }

    /**
     * BP6 "Reputable source recently reports variant as benign, but the evidence is not available to the laboratory to perform an independent evaluation"
     */
    private void assignBP6(AcmgEvidence.Builder acmgEvidenceBuilder, ClinVarData clinVarData) {
        // these are likely never to be triggered as the variants will have been filtered out already
        ClinVarData.ClinSig primaryInterpretation = clinVarData.getPrimaryInterpretation();
        boolean benignOrLikelyBenign = isBenignOrLikelyBenign(primaryInterpretation);
        if (benignOrLikelyBenign && clinVarData.starRating() == 1) {
            acmgEvidenceBuilder.add(BP6);
        } else if (benignOrLikelyBenign && clinVarData.starRating() >= 2) {
            acmgEvidenceBuilder.add(BP6, Evidence.STRONG);
        }
    }

    private boolean isBenignOrLikelyBenign(ClinVarData.ClinSig clinSig) {
        return clinSig == ClinVarData.ClinSig.BENIGN || clinSig == ClinVarData.ClinSig.LIKELY_BENIGN || clinSig == ClinVarData.ClinSig.BENIGN_OR_LIKELY_BENIGN;
    }

    /**
     * PS2 "De novo (both maternity and paternity confirmed) in a patient with the disease and no family history"
     * see also PM6.
     */
    private void assignPS2(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<VariantEvaluation> contributingVariants, boolean hasCompatibleDiseaseMatches, Individual proband) {
        if (modeOfInheritance.isDominant() && hasCompatibleDiseaseMatches && contributingVariants.contains(variantEvaluation)) {
            List<Individual> ancestors = pedigree.anscestorsOf(proband);
            boolean noFamilyHistoryOfDisease = true;
            boolean noFamilyHistoryOfAllele = true;
            if (ancestors.size() >= 2) {
                SampleGenotype probandGenotype = variantEvaluation.getSampleGenotype(probandId);
                for (Individual ancestor : ancestors) {
                    SampleGenotype ancestorGenotype = variantEvaluation.getSampleGenotype(ancestor.getId());
                    // It may be the case that the pedigree contains a relative but the relative was not sequenced,
                    // so these are considered independently
                    if (ancestor.isAffected()) {
                        noFamilyHistoryOfDisease = false;
                    }
                    if ((ancestorGenotype.isHet() || ancestorGenotype.isHomAlt()) && (probandGenotype.isHet() || probandGenotype.isHomAlt())) {
                        // See comment below about possible de novo
                        noFamilyHistoryOfAllele = false;
                    }
                }
                if (noFamilyHistoryOfDisease && noFamilyHistoryOfAllele) {
                    acmgEvidenceBuilder.add(PS2);
                }
            }
        }
    }

    // n.b. this method is a little more obvious about what it expects compared to the test for noFamilyHistoryOfAllele in the PS2 method
    // plus it doesn't assign a 0/1 or ./. genotypes in a parent as being a possible de novo in a 0/0 proband. This case shouldn't occur
    // however, as Exomiser removes 0/0 proband variants.
    private boolean possibleDeNovo(SampleGenotype ancestorGenotype, SampleGenotype probandGenotype) {
        return (ancestorGenotype.isNoCall() || ancestorGenotype.isHomRef()) && (probandGenotype.isHet() || probandGenotype.isHomAlt());
    }


    /**
     * PM2 "Absent from controls (or at extremely low frequency if recessive) in Exome Sequencing Project, 1000 Genomes
     * Project, or Exome Aggregation Consortium"
     * See <a href=https://clinicalgenome.org/site/assets/files/5182/pm2_-_svi_recommendation_-_approved_sept2020.pdf>ClinGen SVI updated recommendations</a>.
     * This method uses the recommended frequency filtering populations from gnomAD for their <a href=https://gnomad.broadinstitute.org/help/faf>Filtering allele frequency</a>
     * which excludes the bottle-necked populations Ashkenazi Jewish (ASJ), European Finnish (FIN), Other (OTH), Amish (AMI)
     * and Middle Eastern (MID).
     */
    private void assignPM2(AcmgEvidence.Builder acmgEvidenceBuilder, FrequencyData frequencyData, ModeOfInheritance modeOfInheritance) {
        // allow local frequency occurrences as these are unverifiable as to their size or content. Also do not use isRepresentedInDatabase()
        // as this will exclude anything with an rsID which could be a ClinVar variant not seen in any population database.
        boolean absentFromDatabase = frequencyData.isEmpty() || (frequencyData.size() == 1 && frequencyData.containsFrequencySource(FrequencySource.LOCAL));
        boolean atVeryLowFrequencyIfRecessive = modeOfInheritance.isRecessive() && frequencyData.maxFreqForPopulation(FrequencySource.NON_FOUNDER_POPS) < 0.01f;
        if (absentFromDatabase || atVeryLowFrequencyIfRecessive) {
            acmgEvidenceBuilder.add(PM2, Evidence.SUPPORTING);
        }
    }

    /**
     * PM4 Protein length changes as a result of in-frame deletions/insertions in a nonrepeat region or stop-loss variants
     */
    private void assignPM4(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation) {
        boolean isInFrameInDel = variantEvaluation.getVariantEffect() == VariantEffect.INFRAME_INSERTION || variantEvaluation.getVariantEffect() == VariantEffect.INFRAME_DELETION;
        // avoid double-counting same affects if PVS1 already applied
        if (!acmgEvidenceBuilder.contains(PVS1) && variantEvaluation.getVariantEffect() == VariantEffect.STOP_LOST || isInFrameInDel) {
            acmgEvidenceBuilder.add(PM4);
        }
        // TODO if can assess whether the variant is an inframe indel in an unconserved or repetitive region then can assign BP3
    }

    /**
     * PM6 "Assumed de novo, but without confirmation of paternity and maternity"
     * See also PS2.
     */
    private void assignPM6(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, ModeOfInheritance modeOfInheritance, List<VariantEvaluation> contributingVariants, boolean hasCompatibleDiseaseMatches) {
//        (iii) The phenotype in the patient matches the gene’s disease
//        association with reasonable specificity. For example, this
//        argument is strong for a patient with a de novo variant
//        in the NIPBL gene who has distinctive facial features,
//         hirsutism, and upper-limb defects (i.e., Cornelia de Lange syndrome),
//         whereas it would be weaker for a de
//        novo variant found by exome sequencing in a child with
//        nonspecific features such as developmental delay.
        if (hasCompatibleDiseaseMatches && modeOfInheritance.isDominant()
            && contributingVariants.contains(variantEvaluation)
            && variantEvaluation.getSampleGenotypes().size() == 1 && variantEvaluation.getSampleGenotype(probandId).isHet()) {
            // making an assumption that this could be a de novo
            acmgEvidenceBuilder.add(PM6);
        }
    }

    /**
     * PP3 "Multiple lines of computational evidence support a deleterious effect on the gene or gene product (conservation, evolutionary, splicing impact, etc.)"
     * BP4 "Multiple lines of computational evidence suggest no impact on gene or gene product (conservation, evolutionary, splicing impact, etc.)"
     */
    private void assignPP3orBP4(AcmgEvidence.Builder acmgEvidenceBuilder, PathogenicityData pathogenicityData) {
        // These approaches are broadly similar in recommending just using one predictor and REVEL is consistently
        // seen to out-perform other predictors. In our testing we also found that the REVEL-only approach worked best.

        // updated from "Evidence-based calibration of computational tools for missense variant pathogenicity
        //   classification and ClinGen recommendations for clinical use of PP3/BP4 criteria"
        //   https://www.biorxiv.org/content/10.1101/2022.03.17.484479v1
        //
        var revelScore = pathogenicityData.pathogenicityScore(PathogenicitySource.REVEL);
        if (revelScore != null) {
            assignRevelBasedPP3BP4Classification(acmgEvidenceBuilder, revelScore);
        } else {
            // See "Assessing performance of pathogenicity predictors using clinically relevant variant datasets"
            // http://dx.doi.org/10.1136/jmedgenet-2020-107003
            assignEnsembleBasedPP3BP4Classification(acmgEvidenceBuilder, pathogenicityData);
        }
    }

    /*
     * Updated classification from "Evidence-based calibration of computational tools for missense variant pathogenicity
     * classification and ClinGen recommendations for clinical use of PP3/BP4 criteria"
     * https://www.biorxiv.org/content/10.1101/2022.03.17.484479v1
     *
     * This method provided much better
     */
    private void assignRevelBasedPP3BP4Classification(AcmgEvidence.Builder acmgEvidenceBuilder, PathogenicityScore revelScore) {
        var revel = revelScore.getRawScore();
        // Taken from table 2 of https://www.biorxiv.org/content/10.1101/2022.03.17.484479v1
        // P_Strong   P_Moderate   P_Supporting       B_Supporting   B_Moderate     B_Strong      B_Very Strong
        // ≥ 0.932 [0.773, 0.932) [0.644, 0.773)    (0.183, 0.290] (0.016, 0.183] (0.003, 0.016] ≤ 0.003
        // PATHOGENIC categories

        if (revel >= 0.932f) {
            acmgEvidenceBuilder.add(PP3, Evidence.STRONG);
        } else if (revel < 0.932f && revel >= 0.773f) {
            acmgEvidenceBuilder.add(PP3, Evidence.MODERATE);
        } else if (revel < 0.773f && revel >= 0.644f) {
            acmgEvidenceBuilder.add(PP3, Evidence.SUPPORTING);
        }
        // BENIGN categories
        else if (revel > 0.183f && revel <= 0.290f) {
            acmgEvidenceBuilder.add(BP4, Evidence.SUPPORTING);
        } else if (revel > 0.016f && revel <= 0.183f) {
            acmgEvidenceBuilder.add(BP4, Evidence.MODERATE);
        } else if (revel > 0.003f && revel <= 0.016f) {
            acmgEvidenceBuilder.add(BP4, Evidence.STRONG);
        } else if (revel <= 0.003f) {
            acmgEvidenceBuilder.add(BP4, Evidence.VERY_STRONG);
        }
    }

    /*
     * Ensemble-based approach suggested in
     * See "Assessing performance of pathogenicity predictors using clinically relevant variant datasets"
     * http://dx.doi.org/10.1136/jmedgenet-2020-107003
     */
    private void assignEnsembleBasedPP3BP4Classification(AcmgEvidence.Builder acmgEvidenceBuilder, PathogenicityData pathogenicityData) {
        int numBenign = 0;
        int numPathogenic = 0;
        List<PathogenicityScore> pathogenicityScores = pathogenicityData.pathogenicityScores();
        for (PathogenicityScore pathogenicityScore : pathogenicityScores) {
            if (isPathogenic(pathogenicityScore)) {
                numPathogenic++;
            } else {
                numBenign++;
            }
        }
        if (pathogenicityScores.size() > 1 && numPathogenic > numBenign) {
            acmgEvidenceBuilder.add(PP3);
        }
        if (pathogenicityScores.size() > 1 && numBenign > numPathogenic) {
            acmgEvidenceBuilder.add(BP4);
        }
    }

    private boolean isPathogenic(PathogenicityScore pathogenicityScore) {
        if (pathogenicityScore instanceof SiftScore score) {
            return score.getRawScore() < SiftScore.SIFT_THRESHOLD;
        }
        if (pathogenicityScore instanceof MutationTasterScore score) {
            return score.getScore() > MutationTasterScore.MTASTER_THRESHOLD;
        }
        if (pathogenicityScore instanceof PolyPhenScore score) {
            return score.getScore() > PolyPhenScore.POLYPHEN_PROB_DAMAGING_THRESHOLD;
        }
        if (pathogenicityScore instanceof CaddScore score) {
            // 95-99% most deleterious.
            return score.getRawScore() >= 13.0f;
        }
        return pathogenicityScore.getScore() > 0.5f;
    }

    /**
     * PP4 "Patient’s phenotype or family history is highly specific for a disease with a single genetic etiology"
     */
    private void assignPP4(AcmgEvidence.Builder acmgEvidenceBuilder, List<ModelPhenotypeMatch<Disease>> compatibleDiseaseMatches) {
        double humanGenePhenotypeScoreForMoi = 0;
        for (ModelPhenotypeMatch<Disease> diseaseModelPhenotypeMatch : compatibleDiseaseMatches) {
            humanGenePhenotypeScoreForMoi = Math.max(humanGenePhenotypeScoreForMoi, diseaseModelPhenotypeMatch.getScore());
        }
        if (humanGenePhenotypeScoreForMoi >= 0.6) {
            acmgEvidenceBuilder.add(PP4);
        }
    }

    /**
     * BS4 "Lack of segregation in affected members of a family"
     */
    //TODO: implement PP1 - Cosegregation with disease in multiple affected family members in a gene definitively known to cause the disease
    private void assignBS4(AcmgEvidence.Builder acmgEvidenceBuilder, VariantEvaluation variantEvaluation, Individual proband) {
        if (pedigree.size() >= 2) {
            List<Individual> affectedFamilyMembers = pedigree.getIndividuals().stream()
                    .filter(Individual::isAffected)
                    .filter(individual -> !individual.getId().equals(probandId))
                    .filter(individual -> individual.getFamilyId().equals(proband.getFamilyId()))
                    .toList();
            boolean segregatesWithAffectedInFamily = true;
            SampleGenotype probandGenotype = variantEvaluation.getSampleGenotype(probandId);
            if (!affectedFamilyMembers.isEmpty()) {
                for (Individual affected : affectedFamilyMembers) {
                    SampleGenotype affectedGenotype = variantEvaluation.getSampleGenotype(affected.getId());
                    if ((affectedGenotype.isHomRef() || affectedGenotype.isNoCall() || affectedGenotype.isEmpty()) && (probandGenotype.isHet() || probandGenotype.isHomAlt())) {
                        segregatesWithAffectedInFamily = false;
                    }
                }
            }
            if (!segregatesWithAffectedInFamily) {
                acmgEvidenceBuilder.add(BS4);
            }
        }
    }

}
