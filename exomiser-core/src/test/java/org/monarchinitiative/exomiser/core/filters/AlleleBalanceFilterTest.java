package org.monarchinitiative.exomiser.core.filters;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monarchinitiative.exomiser.core.genome.TestVcfReader;
import org.monarchinitiative.exomiser.core.genome.VariantContextConverter;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.sequence.VariantTrimmer;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class AlleleBalanceFilterTest {

    @Test
    void filterType() {
        assertThat(new AlleleBalanceFilter().filterType(), equalTo(FilterType.ALLELE_BALANCE_FILTER));
    }

    @Test
    void minimumDepth() {
        assertThat(new AlleleBalanceFilter().minimumDepth(), equalTo(10));
    }

    @Test
    void minimumGenotypeQuality() {
        assertThat(new AlleleBalanceFilter().minimumGenotypeQuality(), equalTo(20));
    }

    @ParameterizedTest
    @CsvSource({
            "PASS, '1       47607851        rs2056899       A       T       1826.78 .      .;    NA     .   .       .       .",
            "PASS, '1       47607851        rs2056899       A       T       1826.78 .      .;    GT   1/1    0/1    0/1    1/1",
            "PASS, '1       47607851        rs2056899       A       T       1826.78 .      .;    GT:AD:DP:GQ:PL   1/1:0,45:45:99:1253,132,0    0/1:6,8:14:99:197,0,182    0/1:12,4:16:66:66,0,346    1/1:0,13:13:36:357,36,0'",
            "PASS, '1       47607851        rs2056899       A       T       1826.78 .      .;    GT:NR:NV:GQ:PL   1/1:45:45:99:1253,132,0    0/1:14:8:99:197,0,182    0/1:16:4:66:66,0,346    1/1:13:13:36:357,36,0'",
            "PASS, 'MT      13227           rs2056899       A       T       1826.78 .      .;    GT:NR:NV:GQ:PL   1/1:45:45:99:1253,132,0    0/1:14:8:99:197,0,182    0/1:16:4:66:66,0,346    1/1:13:13:36:357,36,0'",
            "FAIL, '1       63735   .       CCTA    C       193.23  .    AC=3;AF=0.5;AN=6;     GT:AD:DP:GQ:PL  ./.:.:.:.:.     1/1:6,2:4:6:116,6,0     0/1:8,4:8:99:128,0,301       0/0:18,2:15:30:0,30,666'",
            "FAIL, '1       47607851        rs2056899       A       T       1826.78 FAILS_DP      .;    GT:AD:DP:GQ:PL   1/1:0,45:9:99:1253,132,0    0/1:6,8:14:99:197,0,182    0/1:12,4:16:66:66,0,346    1/1:0,13:13:36:357,36,0'",
            "FAIL, '1       47123898        rs67089539      TAAA    TAAAA,T 382.01  .    AC=1,2;AF=0.125,0.25;AN=8;BaseQRankSum=-2.349;DB;DP=66       GT:AD:DP:GQ:PL  0/0:1,0,0:4:5:0,5,20,6,23,31    0/0:18,0,0:21:13:0,13,306,51,356,783    0/2:14,0,3:20:99:116,140,259,0,102,280  1/2:8,0,4:21:99:362,206,248,163,0,482'",
            "FAIL, '1       70904523        rs112004441     T       TTG,TTGTG       1285.02 .    AC=3,4;AF=0.375,0.5;AN=8     GT:AD:DP:GQ:PL  1/2:10,5,6:23:99:555,231,199,192,0,162    1/2:12,1,4:17:30:272,146,131,44,0,30    1/2:6,1,4:11:28:288,148,133,42,0,28    0/2:15,0,2:20:99:297,180,279,0,120,156'",
            "FAIL, 'MT      13227           .               C        CT        .        PASS     DP=20656;MQ=60 GT:AD:AF:DP:FT:LOD:F1R2:F2R1 0/1:7397,13:0.002:7410:PASS:17.11:3497,5:3900,8 0/0:7309,9:0.001:6772:PASS:.:.:. 0/0:7066,5:0.001:6351:PASS:.:.:. 0/0:7066,5:0.001:6351:PASS:.:.:.'",
            // Some GEL samples were decomposed from multi-allelic sites into mono-allelic sites, but the read information wasn't split, hence the NR=45,45:NV=45,45 where the numerical value of NV or NR is listed for each alternate allele. In this case the data is incorrect, but it will 'PASS' as were using it as a soft filter.
            "PASS, '1       47607851        rs2056899       A       T       1826.78 .      .;    GT:NR:NV:GQ:PL   1/1:45,45:45,45:99:1253,132,0    0/1:14,14:8,8:99:197,0,182    0/1:16,16:4,4:66:66,0,346    1/1:13,13:13,13:36:357,36,0'",
            // 0/0 genotype AB should be ignored as even cases where a single ALT read from a 30x WGS will fail, as in this case:
            "PASS, '1       47607851        rs2056899       A       T       1826.78 .      .;    GT:GQ:NR:NV   0/0:99:32:1   1/1:28:35:14    0/1:99:34:9    0/1:99:34:9'",
    })
    void runFilter(FilterResult.Status expectedStatus, String vcfRecord) {
        TestVcfReader testVcfReader = TestVcfReader.of(List.of("sample1", "sample2", "sample3", "sample4"));
        VariantContext variantContext = testVcfReader.readVariantContext(vcfRecord);
        VariantContextConverter variantContextConverter = VariantContextConverter.of(GenomicAssemblies.GRCh37p13(), VariantTrimmer.leftShiftingTrimmer(VariantTrimmer.retainingCommonBase()));
        List<Allele> alternateAlleles = variantContext.getAlternateAlleles();
        for (int i = 0; i < alternateAlleles.size(); i++) {
            Allele allele = alternateAlleles.get(i);
            var genomicVariant = variantContextConverter.convertToVariant(variantContext, allele);
            VariantEvaluation variantEvaluation = VariantEvaluation.builder()
                    .variant(genomicVariant)
                    .variantContext(variantContext)
                    .altAlleleId(i)
                    .build();
            AlleleBalanceFilter instance = new AlleleBalanceFilter();
            FilterResult.Status status = instance.runFilter(variantEvaluation).status();
            assertThat(status, equalTo(expectedStatus));
        }
    }
}