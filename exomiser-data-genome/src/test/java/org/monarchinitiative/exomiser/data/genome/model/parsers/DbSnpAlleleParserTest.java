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

package org.monarchinitiative.exomiser.data.genome.model.parsers;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.proto.AlleleData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.KG;
import static org.monarchinitiative.exomiser.core.proto.AlleleProto.FrequencySource.TOPMED;

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

        assertThat(alleles.size(), equalTo(0));
        // These are no longer produced as they have no TOPMED frequency information
//        Allele allele = alleles.get(0);
//        assertThat(allele.getChr(), equalTo(1));
//        assertThat(allele.getPos(), equalTo(9446333));
//        assertThat(allele.getRsId(), equalTo("rs761066172"));
//        assertThat(allele.getRef(), equalTo("G"));
//        assertThat(allele.getAlt(), equalTo("A"));
//        assertThat(allele.getFrequencies().isEmpty(), is(true));
    }

    @Test
    public void testSingleAlleleSnpMultiRsId() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t12345\t74640812;rs115693429\tG\tA\t.\t.\t.";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(0));
        // These are no longer produced as they have no TOPMED frequency information
//        Allele allele = alleles.get(0);
//        assertThat(allele.getChr(), equalTo(1));
//        assertThat(allele.getPos(), equalTo(12345));
////        assertThat(allele.getRsId(), equalTo("rs200118651"));
//        assertThat(allele.getRsId(), equalTo("74640812"));
//        assertThat(allele.getRef(), equalTo("G"));
//        assertThat(allele.getAlt(), equalTo("A"));
//        assertThat(allele.getFrequencies().isEmpty(), is(true));
    }


    @Test
    public void testSingleAlleleSnp() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "NC_000001.10\t8036291\trs72854879\tT\tC\t.\t.\tRS=72854879;dbSNPBuildID=130;SSR=0;GENEINFO=PARK7:11315;VC=SNV;INT;GNO;FREQ=1000Genomes:0.9391,0.0609|ALSPAC:0.9992,0.0007784|Estonian:0.9998,0.0002232|GnomAD:0.9427,0.0573|Qatari:0.9306,0.06944|SGDP_PRJ:0.4615,0.5385|TOPMED:0.939,0.061|TWINSUK:0.9995,0.0005394|dbGaP_PopFreq:0.9689,0.03107;COMMON";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(1));
        assertThat(allele.getPos(), equalTo(8036291));
        assertThat(allele.getRsId(), equalTo("rs72854879"));
        assertThat(allele.getRef(), equalTo("T"));
        assertThat(allele.getAlt(), equalTo("C"));
        // FREQ=1000Genomes:0.9391,0.0609 TOPMED:0.939,0.061
        List<AlleleProto.Frequency> expectedFreqs = List.of(
                AlleleData.frequencyOf(KG, 6.0899997f),
                AlleleData.frequencyOf(TOPMED, 6.1f)
        );
        assertThat(allele.getFrequencies(), equalTo(expectedFreqs));
    }

    @Test
    public void testSingleAlleleSnpBuild151() {
        String line = "1\t8036291\trs72854879\tT\tC\t.\t.\tRS=72854879;RSPOS=8036291;dbSNPBuildID=130;SSR=0;SAO=0;VP=0x05010008000515013e000100;GENEINFO=PARK7:11315;WGT=1;VC=SNV;SLO;INT;ASP;VLD;G5;GNO;KGPhase1;KGPhase3;CAF=0.9413,0.05871;COMMON=1;TOPMED=0.914829,0.0851707";

        Allele expected = new Allele(1, 8036291, "T", "C");
        expected.setRsId("rs72854879");
        expected.addFrequency(AlleleData.frequencyOf(KG, 5.8710003f));
        expected.addFrequency(AlleleData.frequencyOf(TOPMED, 8.51707f));

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testSingleAlleleSnpBuild155() {
        String line = "NC_000016.9	2150254	rs147967021	G	A	.	.	RS=147967021;dbSNPBuildID=134;SSR=0;GENEINFO=PKD1:5310;VC=SNV;NSM;R3;GNO;FREQ=1000Genomes:0.9998,0.0001997|ExAC:0.9999,9.404e-05|GnomAD:1,4.989e-05|GnomAD_exomes:0.9999,0.0001088|TOPMED:0.9999,7.934e-05|dbGaP_PopFreq:0.9999,8.681e-05";

        Allele expected = new Allele(16, 2150254, "G", "A");
        expected.setRsId("rs147967021");
        expected.addFrequency(AlleleData.frequencyOf(KG, 0.01997f));
        expected.addFrequency(AlleleData.frequencyOf(TOPMED, 0.007934f));

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testSingleAlleleDeletion() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t10353088\trs763778935\tTC\tT\t.\t.\tRS=763778935;RSPOS=10353089;dbSNPBuildID=144;SSR=0;SAO=0;VP=0x050000080005000002000200;GENEINFO=KIF1B:23095;WGT=1;VC=DIV;INT;ASP;TOPMED=0.99335818042813455,0.00664181957186544";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(1));
        assertThat(allele.getPos(), equalTo(10353088));
        assertThat(allele.getRsId(), equalTo("rs763778935"));
        assertThat(allele.getRef(), equalTo("TC"));
        assertThat(allele.getAlt(), equalTo("T"));
        assertThat(allele.getFrequencies(), equalTo(List.of(AlleleData.frequencyOf(TOPMED, 0.664181957186544f))));
    }

    @Test
    public void testMultiAlleleNoCaf() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t9633387\trs776815368\tG\tGT,GTT\t.\t.\tRS=776815368;RSPOS=9633387;dbSNPBuildID=144;SSR=0;SAO=0;VP=0x050000080005000002000200;GENEINFO=SLC25A33:84275;WGT=1;VC=DIV;INT;ASP";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(0));
        // These are no longer produced as they have no TOPMED frequency information
//        Allele allele1 = alleles.get(0);
//        System.out.println(allele1);
//        assertThat(allele1.getChr(), equalTo(1));
//        assertThat(allele1.getPos(), equalTo(9633387));
//        assertThat(allele1.getRsId(), equalTo("rs776815368"));
//        assertThat(allele1.getRef(), equalTo("G"));
//        assertThat(allele1.getAlt(), equalTo("GT"));
//        assertThat(allele1.getFrequencies().isEmpty(), is(true));
//
//        Allele allele2 = alleles.get(1);
//        System.out.println(allele2);
//        assertThat(allele2.getChr(), equalTo(1));
//        assertThat(allele2.getPos(), equalTo(9633387));
//        assertThat(allele2.getRsId(), equalTo("rs776815368"));
//        assertThat(allele2.getRef(), equalTo("G"));
//        assertThat(allele2.getAlt(), equalTo("GTT"));
//        assertThat(allele2.getFrequencies().isEmpty(), is(true));
    }

    @Test
    public void testMultiAlleleWithCaf() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "1\t9973965\trs555705142\tA\tAT,ATTT\t.\t.\tRS=555705142;RSPOS=9973965;dbSNPBuildID=142;SSR=0;SAO=0;VP=0x050000000005150026000200;WGT=1;VC=DIV;ASP;VLD;G5;KGPhase3;CAF=0.87,.,0.13;COMMON=1";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));

        // No longer produced as it has no TOPMED frequency information
//        Allele allele1 = alleles.get(0);
//        System.out.println(allele1);
//        assertThat(allele1.getChr(), equalTo(1));
//        assertThat(allele1.getPos(), equalTo(9973965));
//        assertThat(allele1.getRsId(), equalTo("rs555705142"));
//        assertThat(allele1.getRef(), equalTo("A"));
//        assertThat(allele1.getAlt(), equalTo("AT"));
//        assertThat(allele1.getFrequencies().isEmpty(), is(true));

        Allele allele2 = alleles.get(0);
        System.out.println(allele2);
        assertThat(allele2.getChr(), equalTo(1));
        assertThat(allele2.getPos(), equalTo(9973965));
        assertThat(allele2.getRsId(), equalTo("rs555705142"));
        assertThat(allele2.getRef(), equalTo("A"));
        assertThat(allele2.getAlt(), equalTo("ATTT"));
        assertThat(allele2.getFrequencies(), equalTo(List.of(AlleleData.frequencyOf(KG, 13.00f))));
    }

    @Test
    public void testLotsOfMultiAlleleWithCaf() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "3\t134153617\trs56011117\tG\tGT,GTT,GTTGT,GTTGTTTTTTTTTGTTT\t.\t.\tRS=56011117;RSPOS=134153617;dbSNPBuildID=129;SSR=0;SAO=0;VP=0x05000000000504002e000204;WGT=1;VC=DIV;ASP;VLD;KGPhase3;NOV;CAF=0.995,0.004992,.,.,.;COMMON=1";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele1 = alleles.get(0);

        assertThat(allele1.getChr(), equalTo(3));
        assertThat(allele1.getPos(), equalTo(134153617));
        assertThat(allele1.getRsId(), equalTo("rs56011117"));
        assertThat(allele1.getRef(), equalTo("G"));
        assertThat(allele1.getAlt(), equalTo("GT"));
        assertThat(allele1.getFrequencies(), equalTo(List.of(AlleleData.frequencyOf(KG, 0.4992f))));

        // These are no longer produced as they have no frequency information
//        Allele allele2 = alleles.get(1);
//        assertThat(allele2.getChr(), equalTo(3));
//        assertThat(allele2.getPos(), equalTo(134153617));
//        assertThat(allele2.getRsId(), equalTo("rs56011117"));
//        assertThat(allele2.getRef(), equalTo("G"));
//        assertThat(allele2.getAlt(), equalTo("GTT"));
//        assertThat(allele2.getFrequencies().isEmpty(), is(true));
//
//        Allele allele3 = alleles.get(2);
//
//        assertThat(allele3.getChr(), equalTo(3));
//        assertThat(allele3.getPos(), equalTo(134153617));
//        assertThat(allele3.getRsId(), equalTo("rs56011117"));
//        assertThat(allele3.getRef(), equalTo("G"));
//        assertThat(allele3.getAlt(), equalTo("GTTGT"));
//        assertThat(allele3.getFrequencies().isEmpty(), is(true));
//
//        Allele allele4 = alleles.get(3);
//        assertThat(allele4.getChr(), equalTo(3));
//        assertThat(allele4.getPos(), equalTo(134153617));
//        assertThat(allele4.getRsId(), equalTo("rs56011117"));
//        assertThat(allele4.getRef(), equalTo("G"));
//        assertThat(allele4.getAlt(), equalTo("GTTGTTTTTTTTTGTTT"));
//        assertThat(allele4.getFrequencies().isEmpty(), is(true));
    }

    /**
     * Build 151 has TOPMED allele frequencies in along with CAF from the Thousand Genomes.
     */
    @Test
    void testSingleAlleleWithTopMedNoCaf() {
        String line = "3\t134153617\trs796981196\tGGTTT\tG\t.\t.\tRS=796981196;RSPOS=134153618;dbSNPBuildID=146;SSR=0;SAO=0;VP=0x050000000005000002000200;WGT=1;VC=DIV;ASP;TOPMED=0.99335818042813455,0.00664181957186544";

        Allele expected = new Allele(3, 134153617, "GGTTT", "G");
        expected.setRsId("rs796981196");
        expected.addFrequency(AlleleData.frequencyOf(TOPMED, 0.664181957186544f));

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    void testMultiAlleleCafAndTopMed() {
        String line = "1\t9974103\trs527824753\tA\tC,T\t.\t.\tRS=527824753;RSPOS=9974103;dbSNPBuildID=142;SSR=0;SAO=0;VP=0x050000000005040026000100;WGT=1;VC=SNV;ASP;VLD;KGPhase3;CAF=0.9996,0.0003994,.;COMMON=1;TOPMED=0.999725,0.000274744,.";

        Allele allele1 = new Allele(1, 9974103, "A", "C");
        allele1.setRsId("rs527824753");
        allele1.addFrequency(AlleleData.frequencyOf(KG, 0.03994f));
        allele1.addFrequency(AlleleData.frequencyOf(TOPMED, 0.0274744f));

        // This is no longer expected as it has no TOPMED frequency information
//        Allele allele2 = new Allele(1, 9974103, "A", "T");
//        allele2.setRsId("rs527824753");

        assertParseLineEquals(line, List.of(allele1));
    }

    @Test
    void testMultiAlleleCafAndTopMedMixedRepresentation() {
        String line = "1\t9974103\trs527824753\tA\tC,T\t.\t.\tRS=527824753;RSPOS=9974103;dbSNPBuildID=142;SSR=0;SAO=0;VP=0x050000000005040026000100;WGT=1;VC=SNV;ASP;VLD;KGPhase3;CAF=0.9996,.,0.0003994;COMMON=1;TOPMED=0.999725,0.000274744,.";

        Allele allele1 = new Allele(1, 9974103, "A", "C");
        allele1.setRsId("rs527824753");
        allele1.addFrequency(AlleleData.frequencyOf(TOPMED, 0.0274744f));

        Allele allele2 = new Allele(1, 9974103, "A", "T");
        allele2.setRsId("rs527824753");
        allele2.addFrequency(AlleleData.frequencyOf(KG, 0.03994f));

        assertParseLineEquals(line, List.of(allele1, allele2));
    }

    @Test
    void testBuild152FormatMultiAlleleCafAndTopMedMixedRepresentation() {
        String line = "" +
                "NC_000001.10\t9974103\trs527824753\tA\tC,T\t.\t.\tRS=527824753;dbSNPBuildID=142;SSR=0;VC=SNV;GNO;FREQ=1000Genomes:0.9996,0.0003994,.|ALSPAC:0.9997,0.0002595,.|GnomAD:0.9999,0.0001279,.|TOPMED:0.9997,0.0002389,5.575e-05|TWINSUK:0.9989,0.001079,.";

        Allele allele1 = new Allele(1, 9974103, "A", "C");
        allele1.setRsId("rs527824753");
        allele1.addFrequency(AlleleData.frequencyOf(KG, 0.03994f));
        allele1.addFrequency(AlleleData.frequencyOf(TOPMED, 0.02389f));

        Allele allele2 = new Allele(1, 9974103, "A", "T");
        allele2.setRsId("rs527824753");
        allele2.addFrequency(AlleleData.frequencyOf(TOPMED, 0.005575f));

        assertParseLineEquals(line, List.of(allele1, allele2));
    }


    @Test
    public void testMitochondrialSnp() {
        DbSnpAlleleParser instance = new DbSnpAlleleParser();
        String line = "NC_012920.1\t15061\trs527236205\tA\tG\t.\t.\tRS=527236205;dbSNPBuildID=141;SSR=0;GENEINFO=MT-CYB:4519|MT-ND6:4541;VC=SNV;SYN;R5;GNO;FREQ=MGP:0.9963,0.003745|SGDP_PRJ:0,1|TOMMO:0.9984,0.001628|dbGaP_PopFreq:0.9987,0.001336;CLNVI=.,;CLNORIGIN=.,1073741824;CLNSIG=.,4;CLNDISDB=.,MONDO:MONDO:0021068/MeSH:D010051/MedGen:C0919267/OMIM:167000/Human_Phenotype_Ontology:HP:0100615;CLNDN=.,Neoplasm_of_ovary;CLNREVSTAT=.,no_criteria;CLNACC=.,RCV000133452.1;CLNHGVS=NC_012920.1:m.15061=,NC_012920.1:m.15061A>G";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(0));
        // These are no longer produced as they have no TOPMED frequency information
//        Allele allele = alleles.get(0);
//        assertThat(allele.getChr(), equalTo(25));
//        assertThat(allele.getPos(), equalTo(15061));
//        assertThat(allele.getRsId(), equalTo("rs527236205"));
//        assertThat(allele.getRef(), equalTo("A"));
//        assertThat(allele.getAlt(), equalTo("G"));
//        assertThat(allele.getFrequencies().isEmpty(), is(true));
    }
}