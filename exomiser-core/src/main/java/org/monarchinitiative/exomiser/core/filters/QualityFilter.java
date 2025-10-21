/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.core.filters;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * VariantFilter Variants on the basis of the PHRED quality score for the
 * variant that was derived from the VCF file (QUAL field).
 *
 * @param mimimumQualityThreshold Threshold for filtering. Retain only those variants whose PHRED variant
 *                                call quality is at least as good. The default is 1.
 * @author Peter N Robinson
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @version 0.09 (18 December, 2013).
 */
public record QualityFilter(double mimimumQualityThreshold) implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(QualityFilter.class);

    private static final FilterType filterType = FilterType.QUALITY_FILTER;

    private static final FilterResult PASS = FilterResult.pass(filterType);
    private static final FilterResult FAIL = FilterResult.fail(filterType);

    /**
     * Constructs a VariantFilter for removing variants which do not pass the
     * defined PHRED score.
     * <p>
     * n.b. We are no longer filtering by requiring a minimum number of reads
     * for each DP4 field (alt/ref in both directions). Instead, we are just
     * filtering on the overall PHRED variant call quality.
     *
     * @param mimimumQualityThreshold The minimum PHRED quality threshold (e.g.
     *                                30) under which a variant will be filtered out.
     */
    public QualityFilter {
        if (mimimumQualityThreshold <= 0f) {
            throw new IllegalArgumentException(String.format("Illegal value for minimum quality threshold: %2f. Minimum quality threshold must be greater than 0.0", mimimumQualityThreshold));
        }
    }

    /**
     * Flag for output field representing the QUAL column of the VCF file.
     */
    @Override
    public FilterType filterType() {
        return filterType;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        return runQualityFilter(variantEvaluation.variantContext());
//        double phredScore = variantEvaluation.phredScore();
//        if (overQualityThreshold(phredScore)) {
//            return PASS;
//        }
//        // Variant is not of good quality
//        return FAIL;
    }

    FilterResult runQualityFilter(VariantContext variantContext) {
        if (variantContext.hasGenotypes()) {
            List<Allele> alleles = variantContext.getAlleles();
            GenotypesContext genotypes = variantContext.getGenotypes();
            for (Genotype genotype : genotypes) {
//                System.out.println("Checking " + genotype);
                if (genotype.hasGQ() && genotype.getGQ() < 20) {
//                    System.out.println("FAIL: " + genotype.getSampleName() + " GQ " + genotype.getGQ() + " < 20");
                    return FAIL;
                }
                if (genotype.hasDP() && genotype.getDP() < 10) {
//                    System.out.println("FAIL: " + genotype.getSampleName() + " DP " + genotype.getDP() + " < 10");
                    return FAIL;
                }
                if (!passesAlleleBalanceFilter(alleles, genotype)) {
//                    System.out.println("FAIL " + genotype.getSampleName() + " AB");
                    return FAIL;
                }
            }
        }

        return PASS;
    }
    private boolean passesAlleleBalanceFilter(List<Allele> allAlleles, Genotype genotype) {
//        AB (allele balance) = alternate reads / (alternate reads + reference reads)
        // AB = AD / DP (where AD needs to match the allele according to GT)
        // ##INFO=<ID=AF,Number=A,Type=Float,Description="Allele Frequency, for each ALT allele, in the same order as listed">
        double ab = calculateAlleleBalance(allAlleles, genotype);
        boolean passesABCheck = true;
        if (genotype.isHet() || genotype.isHetNonRef()) {
            // fail if AF < 0.2 or AF > 0.8
            passesABCheck = ab >= 0.2 && ab <= 0.8;
        } else if (genotype.isHomRef()) {
            // fail if AF > 0.02
            passesABCheck = ab <= 0.02;
        } else if (genotype.isHomVar()) {
            // fail if AF < 0.98
            passesABCheck = ab >= 0.98;
        }
        logger.debug("{} AB {} for {}", (passesABCheck ?  "PASS" : "FAIL"), ab, genotype);
        return passesABCheck;
    }

    private double calculateAlleleBalance(List<Allele> allAlleles, Genotype genotype) {
        if (genotype.hasAD()) {
            return calcABfromAD(allAlleles, genotype);
        }
        // Try using NV & NR (Platypus) AB = NV / NR
        // https://github.com/andyrimmer/Platypus/blob/master/misc/README.txt
        // NR Number of reads covering variant position in this sample
        // NV Number of reads at variant position which support the called variant in this sample
        Map<String, Object> extendedAttributes = genotype.getExtendedAttributes();
        if (extendedAttributes.containsKey("NR") && extendedAttributes.containsKey("NV")) {
            int nr = Integer.parseInt((String) extendedAttributes.get("NR"));
            int nv = Integer.parseInt((String) extendedAttributes.get("NV"));
            return nv / Math.max(nr, 1.0);
        }
        return -1.0;
    }

    private static double calcABfromAD(List<Allele> allAlleles, Genotype genotype) {
        int[] ad = genotype.getAD();
        // this is retarded, but the HTSJDK doesn't store the allele index of each allele, so the GT=0/1 1/1 0/2
        // indexes are lost and need to be recalculated so that the AD for a specific allele can be found for
        // multi-alleleic sites
        List<Allele> genotypeAlleles = genotype.getAlleles();
        // e.g. [1], [0, 0], [0, 1], [0, 2], [1, 1], [1, 2], [2, 2]
        int[] alleleIndexes = new int[genotypeAlleles.size()];
        for (int i = 0; i < genotypeAlleles.size(); i++) {
            alleleIndexes[i] = allAlleles.indexOf(genotypeAlleles.get(i));
        }
        // ensure 1/0 is ordered as 0, 1
        Arrays.sort(alleleIndexes);
        int numRefReads = ad[0];
        int numAltReads = ad[alleleIndexes[alleleIndexes.length - 1]];
        return numAltReads / Math.max(numAltReads + (double) numRefReads, 1.0);
    }

    boolean overQualityThreshold(double qualityScore) {
        return qualityScore >= mimimumQualityThreshold;
    }

    @Override
    public String toString() {
        return "QualityFilter{" + "mimimumQualityThreshold=" + mimimumQualityThreshold + '}';
    }

}
