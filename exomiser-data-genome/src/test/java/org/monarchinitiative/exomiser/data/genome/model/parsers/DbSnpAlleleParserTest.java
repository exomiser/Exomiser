/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbSnpAlleleParserTest extends AbstractAlleleParserTester<DbSnpAlleleParser> {

    @Override
    public DbSnpAlleleParser newInstance() {
        return new DbSnpAlleleParser();
    }

    @Test
    public void testSingleAlleleSnpNoCaf() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t9446333\trs761066172\tG\tA\t.\t.\tRS=761066172;RSPOS=9446333;dbSNPBuildID=144;SSR=0;SAO=0;VP=0x050000000005000002000100;WGT=1;VC=SNV;ASP";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(1));
        assertThat(allele.getPos(), equalTo(9446333));
        assertThat(allele.getRsId(), equalTo("rs761066172"));
        assertThat(allele.getRef(), equalTo("G"));
        assertThat(allele.getAlt(), equalTo("A"));
        assertThat(allele.getValues().isEmpty(), is(true));
    }

    @Test
    public void testSingleAlleleSnpMultiRsId() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t12345\t74640812;rs115693429\tG\tA\t.\t.\t.";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(1));
        assertThat(allele.getPos(), equalTo(12345));
//        assertThat(allele.getRsId(), equalTo("rs200118651"));
        assertThat(allele.getRsId(), equalTo("74640812"));
        assertThat(allele.getRef(), equalTo("G"));
        assertThat(allele.getAlt(), equalTo("A"));
        assertThat(allele.getValues().isEmpty(), is(true));
    }


    @Test
    public void testSingleAlleleSnp() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t8036291\trs72854879\tT\tC\t.\t.\tRS=72854879;RSPOS=8036291;dbSNPBuildID=130;SSR=0;SAO=0;VP=0x05010008000515013e000100;GENEINFO=PARK7:11315;WGT=1;VC=SNV;SLO;INT;ASP;VLD;G5;GNO;KGPhase1;KGPhase3;CAF=0.9413,0.05871;COMMON=1";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(1));
        assertThat(allele.getPos(), equalTo(8036291));
        assertThat(allele.getRsId(), equalTo("rs72854879"));
        assertThat(allele.getRef(), equalTo("T"));
        assertThat(allele.getAlt(), equalTo("C"));
        assertThat(allele.getValue(AlleleProperty.KG), equalTo(5.8710003f));
    }

    @Test
    public void testSingleAlleleSnpBuild151() {
        String line = "1\t8036291\trs72854879\tT\tC\t.\t.\tRS=72854879;RSPOS=8036291;dbSNPBuildID=130;SSR=0;SAO=0;VP=0x05010008000515013e000100;GENEINFO=PARK7:11315;WGT=1;VC=SNV;SLO;INT;ASP;VLD;G5;GNO;KGPhase1;KGPhase3;CAF=0.9413,0.05871;COMMON=1;TOPMED=0.914829,0.0851707";

        Allele expected = new Allele(1, 8036291, "T", "C");
        expected.setRsId("rs72854879");
        expected.addValue(AlleleProperty.KG, 5.8710003f);
        expected.addValue(AlleleProperty.TOPMED, 8.51707f);

        assertParseLineEquals(line, Collections.singletonList(expected));
    }

    @Test
    public void testSingleAlleleDeletion() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t10353088\trs763778935\tTC\tT\t.\t.\tRS=763778935;RSPOS=10353089;dbSNPBuildID=144;SSR=0;SAO=0;VP=0x050000080005000002000200;GENEINFO=KIF1B:23095;WGT=1;VC=DIV;INT;ASP";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(1));
        assertThat(allele.getPos(), equalTo(10353088));
        assertThat(allele.getRsId(), equalTo("rs763778935"));
        assertThat(allele.getRef(), equalTo("TC"));
        assertThat(allele.getAlt(), equalTo("T"));
        assertThat(allele.getValues().isEmpty(), is(true));
    }

    @Test
    public void testMultiAlleleNoCaf() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t9633387\trs776815368\tG\tGT,GTT\t.\t.\tRS=776815368;RSPOS=9633387;dbSNPBuildID=144;SSR=0;SAO=0;VP=0x050000080005000002000200;GENEINFO=SLC25A33:84275;WGT=1;VC=DIV;INT;ASP";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(2));
        Allele allele1 = alleles.get(0);

        System.out.println(allele1);
        assertThat(allele1.getChr(), equalTo(1));
        assertThat(allele1.getPos(), equalTo(9633387));
        assertThat(allele1.getRsId(), equalTo("rs776815368"));
        assertThat(allele1.getRef(), equalTo("G"));
        assertThat(allele1.getAlt(), equalTo("GT"));
        assertThat(allele1.getValues().isEmpty(), is(true));

        Allele allele2 = alleles.get(1);
        System.out.println(allele2);
        assertThat(allele2.getChr(), equalTo(1));
        assertThat(allele2.getPos(), equalTo(9633387));
        assertThat(allele2.getRsId(), equalTo("rs776815368"));
        assertThat(allele2.getRef(), equalTo("G"));
        assertThat(allele2.getAlt(), equalTo("GTT"));
        assertThat(allele2.getValues().isEmpty(), is(true));
    }

    @Test
    public void testMultiAlleleWithCaf() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t9973965\trs555705142\tA\tAT,ATTT\t.\t.\tRS=555705142;RSPOS=9973965;dbSNPBuildID=142;SSR=0;SAO=0;VP=0x050000000005150026000200;WGT=1;VC=DIV;ASP;VLD;G5;KGPhase3;CAF=0.87,.,0.13;COMMON=1";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(2));
        Allele allele1 = alleles.get(0);

        System.out.println(allele1);
        assertThat(allele1.getChr(), equalTo(1));
        assertThat(allele1.getPos(), equalTo(9973965));
        assertThat(allele1.getRsId(), equalTo("rs555705142"));
        assertThat(allele1.getRef(), equalTo("A"));
        assertThat(allele1.getAlt(), equalTo("AT"));
        assertThat(allele1.getValues().isEmpty(), is(true));

        Allele allele2 = alleles.get(1);
        System.out.println(allele2);
        assertThat(allele2.getChr(), equalTo(1));
        assertThat(allele2.getPos(), equalTo(9973965));
        assertThat(allele2.getRsId(), equalTo("rs555705142"));
        assertThat(allele2.getRef(), equalTo("A"));
        assertThat(allele2.getAlt(), equalTo("ATTT"));
        assertThat(allele2.getValue(AlleleProperty.KG), equalTo(13.00f));
    }

    @Test
    public void testLotsOfMultiAlleleWithCaf() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "3\t134153617\trs56011117\tG\tGT,GTT,GTTGT,GTTGTTTTTTTTTGTTT\t.\t.\tRS=56011117;RSPOS=134153617;dbSNPBuildID=129;SSR=0;SAO=0;VP=0x05000000000504002e000204;WGT=1;VC=DIV;ASP;VLD;KGPhase3;NOV;CAF=0.995,0.004992,.,.,.;COMMON=1";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(4));
        Allele allele1 = alleles.get(0);

        System.out.println(allele1);
        assertThat(allele1.getChr(), equalTo(3));
        assertThat(allele1.getPos(), equalTo(134153617));
        assertThat(allele1.getRsId(), equalTo("rs56011117"));
        assertThat(allele1.getRef(), equalTo("G"));
        assertThat(allele1.getAlt(), equalTo("GT"));
        assertThat(allele1.getValue(AlleleProperty.KG), equalTo(0.4992f));

        Allele allele2 = alleles.get(1);
        System.out.println(allele2);
        assertThat(allele2.getChr(), equalTo(3));
        assertThat(allele2.getPos(), equalTo(134153617));
        assertThat(allele2.getRsId(), equalTo("rs56011117"));
        assertThat(allele2.getRef(), equalTo("G"));
        assertThat(allele2.getAlt(), equalTo("GTT"));
        assertThat(allele2.getValues().isEmpty(), is(true));

        Allele allele3 = alleles.get(2);

        System.out.println(allele3);
        assertThat(allele3.getChr(), equalTo(3));
        assertThat(allele3.getPos(), equalTo(134153617));
        assertThat(allele3.getRsId(), equalTo("rs56011117"));
        assertThat(allele3.getRef(), equalTo("G"));
        assertThat(allele3.getAlt(), equalTo("GTTGT"));
        assertThat(allele3.getValues().isEmpty(), is(true));

        Allele allele4 = alleles.get(3);
        System.out.println(allele4);
        assertThat(allele4.getChr(), equalTo(3));
        assertThat(allele4.getPos(), equalTo(134153617));
        assertThat(allele4.getRsId(), equalTo("rs56011117"));
        assertThat(allele4.getRef(), equalTo("G"));
        assertThat(allele4.getAlt(), equalTo("GTTGTTTTTTTTTGTTT"));
        assertThat(allele4.getValues().isEmpty(), is(true));
    }

    /**
     * Build 151 has TOPMED allele frequencies in along with CAF from the Thousand Genomes.
     */
    @Test
    void testSingleAlleleWithTopMedNoCaf() {
        String line = "3\t134153617\trs796981196\tGGTTT\tG\t.\t.\tRS=796981196;RSPOS=134153618;dbSNPBuildID=146;SSR=0;SAO=0;VP=0x050000000005000002000200;WGT=1;VC=DIV;ASP;TOPMED=0.99335818042813455,0.00664181957186544";

        Allele expected = new Allele(3, 134153617, "GGTTT", "G");
        expected.setRsId("rs796981196");
        expected.addValue(AlleleProperty.TOPMED, 0.664181957186544f);

        assertParseLineEquals(line, Collections.singletonList(expected));
    }

    @Test
    void testMultiAlleleCafAndTopMed() {
        String line = "1\t9974103\trs527824753\tA\tC,T\t.\t.\tRS=527824753;RSPOS=9974103;dbSNPBuildID=142;SSR=0;SAO=0;VP=0x050000000005040026000100;WGT=1;VC=SNV;ASP;VLD;KGPhase3;CAF=0.9996,0.0003994,.;COMMON=1;TOPMED=0.999725,0.000274744,.";

        Allele allele1 = new Allele(1, 9974103, "A", "C");
        allele1.setRsId("rs527824753");
        allele1.addValue(AlleleProperty.KG, 0.03994f);
        allele1.addValue(AlleleProperty.TOPMED, 0.0274744f);

        Allele allele2 = new Allele(1, 9974103, "A", "T");
        allele2.setRsId("rs527824753");

        assertParseLineEquals(line, ImmutableList.of(allele1, allele2));
    }

    @Test
    void testMultiAlleleCafAndTopMedMixedRepresentation() {
        String line = "1\t9974103\trs527824753\tA\tC,T\t.\t.\tRS=527824753;RSPOS=9974103;dbSNPBuildID=142;SSR=0;SAO=0;VP=0x050000000005040026000100;WGT=1;VC=SNV;ASP;VLD;KGPhase3;CAF=0.9996,.,0.0003994;COMMON=1;TOPMED=0.999725,0.000274744,.";

        Allele allele1 = new Allele(1, 9974103, "A", "C");
        allele1.setRsId("rs527824753");
        allele1.addValue(AlleleProperty.TOPMED, 0.0274744f);

        Allele allele2 = new Allele(1, 9974103, "A", "T");
        allele2.setRsId("rs527824753");
        allele2.addValue(AlleleProperty.KG, 0.03994f);

        assertParseLineEquals(line, ImmutableList.of(allele1, allele2));
    }

    @Test
    void testBuild152FormatMultiAlleleCafAndTopMedMixedRepresentation() {
        String line = "" +
                "NC_000001.10\t9974103\trs527824753\tA\tC,T\t.\t.\tRS=527824753;dbSNPBuildID=142;SSR=0;VC=SNV;GNO;FREQ=1000Genomes:0.9996,0.0003994,.|ALSPAC:0.9997,0.0002595,.|GnomAD:0.9999,0.0001279,.|TOPMED:0.9997,0.0002389,5.575e-05|TWINSUK:0.9989,0.001079,.";

        Allele allele1 = new Allele(1, 9974103, "A", "C");
        allele1.setRsId("rs527824753");
        allele1.addValue(AlleleProperty.KG, 0.03994f);
        allele1.addValue(AlleleProperty.TOPMED, 0.02389f);
//        allele1.addValue(AlleleProperty.ALSPAC, 0.02595f);
//        allele1.addValue(AlleleProperty.TWINSUK, 0.10789999f);

        Allele allele2 = new Allele(1, 9974103, "A", "T");
        allele2.setRsId("rs527824753");
        allele2.addValue(AlleleProperty.TOPMED, 0.005575f);

        assertParseLineEquals(line, ImmutableList.of(allele1, allele2));
    }


    @Test
    public void testMitochondrialSnp() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "MT\t15061\trs527236205\tA\tG\t.\t.\tRS=527236205;RSPOS=15061;dbSNPBuildID=141;SSR=0;SAO=1;VP=0x050060000305000002110100;GENEINFO=CYTB:4519;WGT=1;VC=SNV;PM;REF;SYN;ASP;LSD;OM";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(25));
        assertThat(allele.getPos(), equalTo(15061));
        assertThat(allele.getRsId(), equalTo("rs527236205"));
        assertThat(allele.getRef(), equalTo("A"));
        assertThat(allele.getAlt(), equalTo("G"));
        assertThat(allele.getValues().isEmpty(), is(true));
    }
}