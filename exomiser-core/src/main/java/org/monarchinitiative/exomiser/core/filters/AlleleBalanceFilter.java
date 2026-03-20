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
        double ab = calculateGenotypeAlleleBalance(genotype, allAlleles);
        if (ab == -1.0) {
            // no genotype / read quality info available, so pass the variant.
            logger.debug("FAIL AB {} for {}", ab, genotype);
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
        }
        // ordinarily fail a homRef (0/0) if AF > 0.02 (Quinlan recommended, but for GEL solved cases where the NR was close to 30 a single read would remove the diagnosis)
        logger.debug("{} AB {} for {}", (passesABCheck ? "PASS" : "FAIL"), ab, genotype);
        return passesABCheck;
    }

    private double calculateGenotypeAlleleBalance(Genotype genotype, List<Allele> allAlleles) {
        // this is retarded, but the HTSJDK doesn't store the allele index of each allele, so the GT=0/1 1/1 0/2
        // indexes are lost and need to be recalculated so that the AD for a specific allele can be found for
        // multi-alleleic sites.
        // create an ordered array of the alleles present in the genotype, as these are not stored
        // e.g. 0/0 = [0,0], 1/0 or 0/1 = [0,1], 1/1 = [1,1], 1/2 = [1,2]...
        int[] genotypeAlleleIndex = buildGenotypeAlleleIndex(genotype, allAlleles);

        if (genotype.hasAD()) {
            return calcABfromAD(genotype, genotypeAlleleIndex);
        }
        // Try using NV & NR (Platypus) AB = NV / NR
        // https://github.com/andyrimmer/Platypus/blob/master/misc/README.txt
        // NR Number of reads covering variant position in this sample
        // NV Number of reads at variant position which support the called variant in this sample

        // See also: https://github.com/andyrimmer/Platypus/issues/61#issuecomment-279744487 for multi-allelic sites
        // "The NR and NV values are given for each allele, in the order which the alleles are listed in the ALT column.
        // So the NR values will always be the same (number of reads supporting the reference), and the NV values will
        // be different for each variant allele. These numbers are harder to interpret when you have e.g. repetitive
        // indels because some reads could support either variant allele."
        Map<String, Object> extendedAttributes = genotype.getExtendedAttributes();
        if (extendedAttributes.containsKey("NR") && extendedAttributes.containsKey("NV")) {
            String nrString = (String) extendedAttributes.get("NR");
            String nvString = (String) extendedAttributes.get("NV");

            var nvStrings = nvString.contains(",") ? nvString.split(",") : new String[]{nvString};
            if (allAlleles.size() - 1 != (nvStrings.length)) {
                logger.debug("FAIL allele count {} != NV count {}", allAlleles.size(), nvStrings.length);
                // This case should FAIL as the number of ALT alleles doesn't match the genotype info, but we're using
                //  this as a soft filter where we'll ignore incorrect or absent data to maximise recall at the
                //  expense of some precision
                return -1.0;
            }
            // make a new AD-like array
            int[] alleleDepths = new int[nvStrings.length + 1];
            int nr = Integer.parseInt(nrString.contains(",") ? nrString.substring(0, nrString.indexOf(",")) : nrString);
            alleleDepths[0] = nr;
            for (int i = 0; i < nvStrings.length; i++) {
                alleleDepths[i + 1] = Integer.parseInt(nvStrings[i]);
            }

//            int nv = alleleDepths[genotypeAlleleIndex[genotypeAlleleIndex.length - 1]];
            int nv = 0;
            for (int i = 1; i < alleleDepths.length; i++) {
                nv += alleleDepths[i];
            }
            double ab = nv / Math.max(nr, 1.0);
            logger.debug("NR={}, NV={}, AB={} for {}", nr, nv, ab, genotype);
            return ab;
        }
        return -1.0;
    }

    private static double calcABfromAD(Genotype genotype, int[] genotypeAlleleIndexes) {
        int[] ad = genotype.getAD();
        int numRefReads = ad[0];
        // should this not be the sum of all alternate alleles?
        int numAltReads = 0;
        for (int i = 1; i < ad.length; i++) {
            numAltReads += ad[i];
        }
        double ab = numAltReads / Math.max(numAltReads + (double) numRefReads, 1.0);
        logger.debug("AD REF={}, AD ALT={}, AB={} for {}", numRefReads, numAltReads, ab, genotype);
        return ab;
    }

    /**
     * Builds an array representing indexes of alleles from the genotype in the list of all alleles.
     * The resulting array is sorted to ensure a consistent order. For example, 0/1 or 1/0 or 1|0 will return the array
     * [0, 1], a 1/2 or 2/1 genotype will return [1, 2].
     *
     * @param genotype the genotype containing the alleles to be indexed
     * @param allAlleles the complete list of alleles from which the indexes are derived
     * @return an array of indexes for each allele in the genotype, sorted in ascending order
     */
    private static int[] buildGenotypeAlleleIndex(Genotype genotype, List<Allele> allAlleles) {
        List<Allele> genotypeAlleles = genotype.getAlleles();
        // e.g. [1], [0, 0], [0, 1], [0, 2], [1, 1], [1, 2], [2, 2]
        int[] alleleIndexes = new int[genotypeAlleles.size()];
        for (int i = 0; i < genotypeAlleles.size(); i++) {
            alleleIndexes[i] = allAlleles.indexOf(genotypeAlleles.get(i));
        }
        // ensure 1/0 is ordered as 0, 1
        Arrays.sort(alleleIndexes);
        return alleleIndexes;
    }

}
