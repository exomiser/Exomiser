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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class EspAlleleParserTest {

    @Test
    public void testSingleAlleleSnp() {
        EspAlleleParser instance = new EspAlleleParser();
        String line = "17\t26942314\trs371278753\tC\tG\t.\tPASS\tDBSNP=dbSNP_138;EA_AC=1,8599;AA_AC=0,4406;TAC=1,13005;MAF=0.0116,0.0,0.0077;GTS=GG,GC,CC;EA_GTC=0,1,4299;AA_GTC=0,0,2203;GTC=0,1,6502;DP=24;GL=KIAA0100;CP=0.0;CG=0.2;AA=C;CA=.;EXOME_CHIP=no;GWAS_PUBMED=.;FG=NM_014680.3:intron;HGVS_CDNA_VAR=NM_014680.3:c.6526-50G>C;HGVS_PROTEIN_VAR=.;CDS_SIZES=NM_014680.3:6708;GS=.;PH=.;EA_AGE=.;AA_AGE=.;GRCh38_POSITION=17:28615296";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(17));
        assertThat(allele.getPos(), equalTo(26942314));
        assertThat(allele.getRsId(), equalTo("rs371278753"));
        assertThat(allele.getRef(), equalTo("C"));
        assertThat(allele.getAlt(), equalTo("G"));

        Map<AlleleProperty, Float> expectedFreqs = new EnumMap<>(AlleleProperty.class);
        expectedFreqs.put(AlleleProperty.ESP_EA, 0.0116f);
        expectedFreqs.put(AlleleProperty.ESP_ALL, 0.0077f);

        assertThat(allele.getValues(), equalTo(expectedFreqs));

    }

    @Test
    public void testSingleAlleleDeletion() {
        EspAlleleParser instance = new EspAlleleParser();
        String line = "17\t73725391\t.\tGA\tG\t.\tPASS\tDBSNP=.;EA_AC=0,8254;AA_AC=2,4262;TAC=2,12516;MAF=0.0,0.0469,0.016;GTS=A1A1,A1R,RR;EA_GTC=0,0,4127;AA_GTC=0,2,2130;GTC=0,2,6257;DP=92;GL=ITGB4;CP=1.0;CG=3.6;AA=.;CA=.;EXOME_CHIP=no;GWAS_PUBMED=.;FG=NM_001005731.1:frameshift,NM_001005619.1:frameshift,NM_000213.3:frameshift;HGVS_CDNA_VAR=NM_001005731.1:c.613del1,NM_001005619.1:c.613del1,NM_000213.3:c.613del1;HGVS_PROTEIN_VAR=NM_001005731.1:p.(N205Tfs*5),NM_001005619.1:p.(N205Tfs*5),NM_000213.3:p.(N205Tfs*5);CDS_SIZES=NM_001005731.1:5259,NM_001005619.1:5418,NM_000213.3:5469;GS=.,.,.;PH=.,.,.;EA_AGE=.;AA_AGE=.;GRCh38_POSITION=17:75729310";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(1));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(17));
        assertThat(allele.getPos(), equalTo(73725391));
        assertThat(allele.getRsId(), equalTo("."));
        assertThat(allele.getRef(), equalTo("GA"));
        assertThat(allele.getAlt(), equalTo("G"));

        Map<AlleleProperty, Float> expectedFreqs = new EnumMap<>(AlleleProperty.class);
        expectedFreqs.put(AlleleProperty.ESP_AA, 0.0469f);
        expectedFreqs.put(AlleleProperty.ESP_ALL, 0.016f);

        assertThat(allele.getValues(), equalTo(expectedFreqs));
    }

    @Test
    public void testMultiAllele() {
        EspAlleleParser instance = new EspAlleleParser();
        String line = "7\t107167661\t.\tTAA\tTAAA,TTAA,TAAAA,T,TA\t.\tPASS\tDBSNP=.;EA_AC=2461,15,376,36,596,4262;AA_AC=1215,23,182,39,450,2089;TAC=3676,38,558,75,1046,6351;MAF=44.9781,47.7489,45.9213;GTS=A1A1,A1A2,A1A3,A1A4,A1A5,A1R,A2A2,A2A3,A2A4,A2A5,A2R,A3A3,A3A4,A3A5,A3R,A4A4,A4A5,A4R,A5A5,A5R,RR;EA_GTC=276,3,128,5,97,1676,1,0,0,0,10,28,0,8,184,1,1,28,45,400,982;AA_GTC=134,9,59,8,82,789,4,0,0,0,6,16,1,8,82,1,4,24,30,296,446;GTC=410,12,187,13,179,2465,5,0,0,0,16,44,1,16,266,2,5,52,75,696,1428;DP=17;GL=COG5;CP=0.0;CG=.;AA=.;CA=.;EXOME_CHIP=no;GWAS_PUBMED=.;FG=NM_181733.2:intron,NM_181733.2:intron,NM_181733.2:intron,NM_181733.2:intron,NM_181733.2:intron,NM_006348.3:intron,NM_006348.3:intron,NM_006348.3:intron,NM_006348.3:intron,NM_006348.3:intron,NM_001161520.1:intron,NM_001161520.1:intron,NM_001161520.1:intron,NM_001161520.1:intron,NM_001161520.1:intron;HGVS_CDNA_VAR=NM_181733.2:c.631+20del1,NM_181733.2:c.631+19_631+20del2,NM_181733.2:c.631+20_631+21insTT,NM_181733.2:c.631+20_631+21insTTAA,NM_181733.2:c.631+20_631+21insT,NM_006348.3:c.631+20del1,NM_006348.3:c.631+19_631+20del2,NM_006348.3:c.631+20_631+21insTT,NM_006348.3:c.631+20_631+21insTTAA,NM_006348.3:c.631+20_631+21insT,NM_001161520.1:c.631+20del1,NM_001161520.1:c.631+19_631+20del2,NM_001161520.1:c.631+20_631+21insTT,NM_001161520.1:c.631+20_631+21insTTAA,NM_001161520.1:c.631+20_631+21insT;HGVS_PROTEIN_VAR=.,.,.,.,.,.,.,.,.,.,.,.,.,.,.;CDS_SIZES=NM_181733.2:2520,NM_181733.2:2520,NM_181733.2:2520,NM_181733.2:2520,NM_181733.2:2520,NM_006348.3:2583,NM_006348.3:2583,NM_006348.3:2583,NM_006348.3:2583,NM_006348.3:2583,NM_001161520.1:2472,NM_001161520.1:2472,NM_001161520.1:2472,NM_001161520.1:2472,NM_001161520.1:2472;GS=.,.,.,.,.,.,.,.,.,.,.,.,.,.,.;PH=.,.,.,.,.,.,.,.,.,.,.,.,.,.,.;EA_AGE=.;AA_AGE=.;GRCh38_POSITION=7:107527216";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(5));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(7));
        assertThat(allele.getPos(), equalTo(107167661));
        assertThat(allele.getRsId(), equalTo("."));
        assertThat(allele.getRef(), equalTo("T"));
        assertThat(allele.getAlt(), equalTo("TA"));

        Map<AlleleProperty, Float> expectedFreqs = new EnumMap<>(AlleleProperty.class);
        expectedFreqs.put(AlleleProperty.ESP_EA, 44.9781f);
        expectedFreqs.put(AlleleProperty.ESP_AA, 47.7489f);
        expectedFreqs.put(AlleleProperty.ESP_ALL, 45.9213f);

        assertThat(allele.getValues(), equalTo(expectedFreqs));
    }

    @Test
    public void testMultiAlleleWithTildaPrefixedRsId() {
        EspAlleleParser instance = new EspAlleleParser();
        String line = "1\t116234024\t~rs151052987\tTA\tTAAA,TAA,T\t.\tPASS\tDBSNP=dbSNP_134;EA_AC=17,55,130,7976;AA_AC=17,217,55,3829;TAC=34,272,185,11805;MAF=2.47,7.018,3.9932;GTS=A1A1,A1A2,A1A3,A1R,A2A2,A2A3,A2R,A3A3,A3R,RR;EA_GTC=0,0,0,17,0,0,55,0,130,3887;AA_GTC=1,0,0,15,13,0,191,4,47,1788;GTC=1,0,0,32,13,0,246,4,177,5675;DP=25;GL=VANGL1;CP=0.0;CG=-3.2;AA=.;CA=.;EXOME_CHIP=no;GWAS_PUBMED=.;FG=NM_138959.2:utr-3,NM_138959.2:utr-3,NM_138959.2:utr-3,NM_001172412.1:utr-3,NM_001172412.1:utr-3,NM_001172412.1:utr-3,NM_00117^C2411.1:utr-3,NM_001172411.1:utr-3,NM_001172411.1:utr-3;HGVS_CDNA_VAR=NM_138959.2:c.*25del1,NM_138959.2:c.*24_*25insA,NM_138959.2:c.*24_*25insAA,NM_001172412.1:c.*25del1,NM_001172412.1:c.*24_*25insA,NM_001172412.1:c.*24_*25insAA,NM_001172411.1:c.*25del1,NM_001172411.1:c.*24_*25insA,NM_001172411.1:c.*24_*25insAA;HGVS_PROTEIN_VAR=.,.,.,.,.,.,.,.,.;CDS_SIZES=NM_138959.2:1575,NM_138959.2:1575,NM_138959.2:1575,NM_001172412.1:1575,NM_001172412.1:1575,NM_001172412.1:1575,NM_001172411.1:1569,NM_001172411.1:1569,NM_0";
        List<Allele> alleles = instance.parseLine(line);

        assertThat(alleles.size(), equalTo(3));
        Allele allele = alleles.get(0);

        System.out.println(allele);
        assertThat(allele.getChr(), equalTo(1));
        assertThat(allele.getPos(), equalTo(116234024));
        assertThat(allele.getRsId(), equalTo("rs151052987"));
        assertThat(allele.getRef(), equalTo("T"));
        assertThat(allele.getAlt(), equalTo("TAA"));
    }
}