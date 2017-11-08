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

package org.monarchinitiative.exomiser.data.genome.parsers;

import org.junit.Test;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.AlleleProperty;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class DbSnpAlleleParserTest {

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
    public void testMultiAllelealleleNoCaf() {
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
    public void testMultiAllelealleleWithCaf() {
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
    public void testLotsOfMultiAllelealleleWithCaf() {
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