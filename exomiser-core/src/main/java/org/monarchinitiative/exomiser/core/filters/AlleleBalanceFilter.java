package org.monarchinitiative.exomiser.core.filters;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.svart.assembly.AssignedMoleculeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A {@link VariantFilter} which removes poor quality variants based on their allele balance after the criteria published
 * in <a href="https://doi.org/10.1038/s41525-021-00227-3">Pedersen, B.S., Brown, J.M., Dashnow, H. et al. Effective variant filtering and expected candidate variant yield in studies of rare human disease. npj Genom. Med. 6, 60 (2021).</a>
 * <p>
 * This filter chacks that ALL samples in a VCF have a minimum GQ of > 20, a minimum DP > 10 and allele balance between
 * 0.2 and 0.8 for heterozygous samples or <0.02 homozygous samples.
 */
public record AlleleBalanceFilter() implements VariantFilter {

    private static final Logger logger = LoggerFactory.getLogger(AlleleBalanceFilter.class);

    private static final FilterResult PASS = FilterResult.pass(FilterType.ALLELE_BALANCE_FILTER);
    private static final FilterResult FAIL = FilterResult.fail(FilterType.ALLELE_BALANCE_FILTER);

    private static final int MIN_GQ = 20;
    private static final int MIN_DP = 10;


    public int minimumGenotypeQuality() {
        return MIN_GQ;
    }

    public int minimumDepth() {
        return MIN_DP;
    }

    @Override
    public FilterType filterType() {
        return FilterType.ALLELE_BALANCE_FILTER;
    }

    @Override
    public FilterResult runFilter(VariantEvaluation variantEvaluation) {
        VariantContext variantContext = variantEvaluation.variantContext();
        if (variantContext.hasGenotypes()) {
            List<Allele> alleles = variantContext.getAlleles();
            GenotypesContext genotypes = variantContext.getGenotypes();
            boolean isMitochondrial = variantEvaluation.contig().assignedMoleculeType() == AssignedMoleculeType.MITOCHONDRION;
            for (Genotype genotype : genotypes) {
                if (genotype.hasGQ() && genotype.getGQ() < MIN_GQ) {
                    logger.debug("GQ {} < {} for {}", genotype.getGQ(), MIN_GQ, genotype);
                    return FAIL;
                }
                if (genotype.hasDP() && genotype.getDP() < MIN_DP) {
                    logger.debug("DP {} < {} for {}", genotype.getDP(), MIN_DP, genotype);
                    return FAIL;
                }
                if (!passesAlleleBalanceFilter(isMitochondrial, alleles, genotype)) {
                    return FAIL;
                }
            }
        }
        return PASS;
    }

    private boolean passesAlleleBalanceFilter(boolean isMitochondrial, List<Allele> allAlleles, Genotype genotype) {
        // AB (allele balance) = alternate reads / (alternate reads + reference reads)
        // AB = AD / DP (where AD needs to match the allele according to GT)
        // ##INFO=<ID=AF,Number=A,Type=Float,Description="Allele Frequency, for each ALT allele, in the same order as listed">
        double ab = calculateAlleleBalance(allAlleles, genotype);
        if (ab == -1.0) {
            // no genotype / read quality info available, so pass the variant.
            return true;
        }
        boolean passesABCheck = true;
        if (genotype.isHet() || genotype.isHetNonRef()) {
            if (isMitochondrial) {
                // GEL tiering heteroplasmy filter set to pass MT HET variants with ab >= 5% (0.05)
                passesABCheck = ab >= 0.05;
            } else {
                // fail if AF < 0.2 or AF > 0.8 (Quinlan recommended, but for GEL solved cases the UDN recommended 0.15-0.85 removed fewer diagnosed cases)
                passesABCheck = ab >= 0.15 && ab <= 0.85;
            }
        } else if (genotype.isHomRef()) {
            // fail if AF > 0.02 (Quinlan recommended, but for GEL solved cases where the NR was close to 30 a single read would remove the diagnosis)
            passesABCheck = ab <= 0.03;
        }
        logger.debug("{} AB {} for {}", (passesABCheck ? "PASS" : "FAIL"), ab, genotype);
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

}
