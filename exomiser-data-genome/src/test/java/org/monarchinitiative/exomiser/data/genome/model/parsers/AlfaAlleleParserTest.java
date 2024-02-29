package org.monarchinitiative.exomiser.data.genome.model.parsers;

import htsjdk.variant.vcf.*;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.*;

class AlfaAlleleParserTest {
    // Doh! They placed the allele counts in the genotypes field!
    // ##fileformat=VCFv4.0
    // ##build_id=20201027095038
    // ##Population=https://www.ncbi.nlm.nih.gov/biosample/?term=GRAF-pop
    // ##FORMAT=<ID=AN,Number=1,Type=Integer,Description="Total allele count for the population, including REF">
    // ##FORMAT=<ID=AC,Number=A,Type=Integer,Description="Allele count for each ALT allele for the population">
    // #CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  SAMN10492695    SAMN10492696    SAMN10492697    SAMN10492698    SAMN10492699    SAMN10492700    SAMN10492701    SAMN10492702    SAMN11605645    SAMN10492703    SAMN10492704    SAMN10492705
    // NC_000001.9     144135212       rs1553120241    G       A       .       .       .       AN:AC   8560:5387       8:8     256:224 336:288 32:24   170:117 32:24   18:13   20:15   344:296 288:248 9432:6100
    // NC_000001.9     144148243       rs2236566       G       T       .       .       .       AN:AC   5996:510        0:0     0:0     0:0     0:0     0:0     0:0     0:0     84:8    0:0     0:0     6080:518
    // NC_000001.9     146267105       rs1553119693    T       G       .       .       .       AN:AC   37168:28800     36:22   56:44   1378:839        18:14   70:60   10:9    4836:3639       452:322 1414:861        66:53   44024:33749
    // NC_000001.9     148488564       .       C       A       .       .       .       AN:AC   8552:0  8:0     256:0   338:0   32:0    170:0   32:0    16:0    20:0    346:0   288:0   9424:0
    // NC_000001.10    2701535 rs371068661     C       T       .       .       .       AN:AC   134:9   0:0     0:0     48:1    0:0     0:0     0:0     0:0     188:15  48:1    0:0     370:25
    // NC_000001.10    2701546 rs587702211     G       A       .       .       .       AN:AC   134:0   0:0     0:0     48:4    0:0     0:0     0:0     0:0     188:2   48:4    0:0     370:6
    // NC_000001.10    7426777 rs1553119850    GT      G       .       .       .       AN:AC   4473:4462       0:0     0:0     8:0     0:0     0:0     0:0     0:0     24:8    8:0     0:0     4505:4470
    // NC_000001.10    7426778 rs1553119849    T       C,G     .       .       .       AN:AC   4494:0,4483     0:0,0   2:0,2   32:0,24 8:0,8   6:0,6   2:0,2   0:0,0   304:0,288       32:0,24 4:0,4   4848:0,4813
    // NC_000001.10    12461010        rs762190215     T       TGC,TGCGCGCGC,TGCGCGC   .       .       .       AN:AC   4456:85,8,45    0:0,0,0 0:0,0,0 0:0,0,0 0:0,0,0 0:0,0,0 0:0,0,0 0:0,0,0 8:0,0,0 0:0,0,0 0:0,0,0 4464:85,8,45
    // NC_000001.11    10001   .       T       C       .       .       .       AN:AC   7618:0  108:0   84:0    2708:0  146:0   610:0   24:0    94:0    470:0   2816:0  108:0   11862:0


    private final String header = """
            ##fileformat=VCFv4.0
            ##build_id=20201027095038
            ##Population=https://www.ncbi.nlm.nih.gov/biosample/?term=GRAF-pop
            ##FORMAT=<ID=AN,Number=1,Type=Integer,Description="Total allele count for the population, including REF">
            ##FORMAT=<ID=AC,Number=A,Type=Integer,Description="Allele count for each ALT allele for the population">
            #CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tSAMN10492695\tSAMN10492696\tSAMN10492697\tSAMN10492698\tSAMN10492699\tSAMN10492700\tSAMN10492701\tSAMN10492702\tSAMN11605645\tSAMN10492703\tSAMN10492704\tSAMN10492705
            """;

    private final Set<VCFHeaderLine> headerLines = Set.of(
            new VCFHeaderLine("Population", "https://www.ncbi.nlm.nih.gov/biosample/?term=GRAF-pop"),
            new VCFFormatHeaderLine("AN", 1, VCFHeaderLineType.Integer, "Total allele count for the population, including REF"),
            new VCFFormatHeaderLine("AC", VCFHeaderLineCount.A, VCFHeaderLineType.Integer, "Allele count for each ALT allele for the population")
            );
    private final List<String> sampleNames = List.of(
            "SAMN10492695", "SAMN10492696", "SAMN10492697", "SAMN10492698",
            "SAMN10492699", "SAMN10492700", "SAMN10492701", "SAMN10492702",
            "SAMN11605645", "SAMN10492703", "SAMN10492704", "SAMN10492705");

    private final VCFHeader vcfHeader = new VCFHeader(headerLines, sampleNames);
    {
        vcfHeader.setVCFHeaderVersion(VCFHeaderVersion.VCF4_1);
    }

    private final AlfaAlleleParser instance = new AlfaAlleleParser(vcfHeader);


    @Test
    void parseCommonHg18() {
        // NC_000001.9 is Chr1 on hg18 which we don't support
        var result = instance.parseLine("NC_000001.9\t144135212\trs1553120241\tG\tA\t.\t.\t.\tAN:AC\t8560:5387\t8:8\t256:224\t336:288\t32:24\t170:117\t32:24\t18:13\t20:15\t344:296\t288:248\t9432:6100");
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void parseHomomorphicHg38() {
        // NC_000001.9 is Chr1 on hg18 which we don't support
        var result = instance.parseLine("NC_000001.11\t10001\t.\tT\tC\t.\t.\t.\tAN:AC\t7618:0\t108:0\t84:0\t2708:0\t146:0\t610:0\t24:0\t94:0\t470:0\t2816:0\t108:0\t11862:0");
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void parsePolymorphicSiteNoFrequencies() {
        //#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  SAMN10492695    SAMN10492696    SAMN10492697    SAMN10492698    SAMN10492699    SAMN10492700    SAMN10492701    SAMN10492702    SAMN11605645    SAMN10492703    SAMN10492704    SAMN10492705
        //NC_000001.11    10235   rs1035249121    T       A,C,G   .       .       .       AN:AC   7616:0,0,0      108:0,0,0       84:0,0,0        2706:0,0,0      146:0,0,0       610:0,0,0       24:0,0,0        94:0,0,0        470:0,0,0       2814:0,0,0      108:0,0,0       11858:0,0,0
        var result = instance.parseLine("NC_000001.11\t10235\trs1035249121\tT\tA,C,G\t.\t.\t.\tAN:AC\t7616:0,0,0\t108:0,0,0\t84:0,0,0\t2706:0,0,0\t146:0,0,0\t610:0,0,0\t24:0,0,0\t94:0,0,0\t470:0,0,0\t2814:0,0,0\t108:0,0,0\t11858:0,0,0");
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void parsePolymorphicSiteWithFrequencies() {
        //#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT  SAMN10492695    SAMN10492696    SAMN10492697    SAMN10492698    SAMN10492699    SAMN10492700    SAMN10492701    SAMN10492702    SAMN11605645    SAMN10492703    SAMN10492704    SAMN10492705
        //NC_000001.11    10291   rs145427775     C       G,T     .       .       .       AN:AC   7618:0,5        108:0,0 84:0,0  2708:0,1        146:0,0 610:0,1 24:0,0  94:0,0  470:0,0 2816:0,1        108:0,0 11862:0,7
        var result = instance.parseLine("NC_000001.11\t10291\trs145427775\tC\tG,T\t.\t.\t.\tAN:AC\t7618:0,5\t108:0,0\t84:0,0\t2708:0,1\t146:0,0\t610:0,1\t24:0,0\t94:0,0\t470:0,0\t2816:0,1\t108:0,0\t11862:0,7");
        Allele allele = new Allele(1, 10291, "C", "T");
        allele.setRsId("rs145427775");
        allele.addFrequency(AlleleData.frequencyOf(ALFA_EUR, 5, 7618)); //SAMN10492695
        allele.addFrequency(AlleleData.frequencyOf(ALFA_AFA, 1, 2708)); //SAMN10492698
        allele.addFrequency(AlleleData.frequencyOf(ALFA_LEN, 1, 610));  //SAMN10492700
        allele.addFrequency(AlleleData.frequencyOf(ALFA_AFR, 1, 2816)); //SAMN10492703
        allele.addFrequency(AlleleData.frequencyOf(ALFA_TOT, 7, 11862));//SAMN10492705
        List<Allele> expected = List.of(allele);
        assertThat(result, equalTo(expected));
    }

    @Test
    void ignoreAllelesThrowingHtsJdkExceptions() {
        var result = instance.parseLine("NC_000001.9\t144135212\trs1553120241\tG\tR\t.\t.\t.\tAN:AC\t8560:5387\t8:8\t256:224\t336:288\t32:24\t170:117\t32:24\t18:13\t20:15\t344:296\t288:248\t9432:6100");
        assertThat(result.isEmpty(), is(true));
    }
}