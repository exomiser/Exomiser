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

package org.monarchinitiative.exomiser.data.genome.indexers;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.parsers.ClinVarAlleleParser;

import java.io.BufferedWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClinVarWhiteListFileAlleleIndexerTest {

    private String indexLine(String line) {
        StringWriter out = new StringWriter();
        try (ClinVarWhiteListFileAlleleIndexer instance = new ClinVarWhiteListFileAlleleIndexer(new BufferedWriter(out), Set.of())) {
            ClinVarAlleleParser clinVarAlleleParser = new ClinVarAlleleParser();
            List<Allele> alleles = clinVarAlleleParser.parseLine(line);
            alleles.forEach(instance::write);
        }
        return out.toString();
    }

    @Test
    void testNonClinVarVcfLine() {
        assertThat(indexLine("line").isEmpty(), equalTo(true));
    }

    @Test
    void testCorruptedClinVarVcfLine() {
        assertThrows(Exception.class, () -> indexLine("1	155235058	991404	C	G	.	.	ALLELEID=97i949|synony552ucleotide_variCLNREVSTAT=c_l;CLNREVSTAT=criteria_proviP:aNRE;CLNVC=singlla;CLNREVSTAT=no_a2ingle_submitter;CLNSIG=Pathogenic;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=OMIM_Allelic_Variant:606463.0021|UsCLNSIG=Pathogenic;Cl_disorder_of_glycosylation_type_1O;CLNHGVS=NC_000001.11:g.155140056G>A;CLNREVSTAT=criteria_provided,_single_73;CLNMONDO:MONDO:0018150,MedGen:C0017205,Orphanet:ORPHA355|MONDO:MONDO:0018466,MedGen:CN237447,Orphanet:ORPHA411602|MedGen:CN517202;CLNDN=Lewy_body_dementia|Parkinson_R	PGINCL=424820:Likely_pathogenic"));
    }

    @Test
    void testNoAssertionCriteriaNotOnWhitelist() {
        String line = "1	198776931	427752	C	A	.	.	AF_TGP=0.47724;ALLELEID=417641;CLNDISDB=MONDO:MONDO:0020320,MedGen:C1879321,Orphanet:ORPHA98834;CLNDN=Acute_myeloid_leukemia_with_maturation;CLNHGVS=NC_000001.10:g.198776931C>A;CLNREVSTAT=no_assertion_criteria_provided;CLNSIG=Pathogenic;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;ORIGIN=1;RS=9660525";
        assertThat(indexLine(line).isEmpty(), equalTo(true));
    }

    @Test
    void testVusNotOnWhitelist() {
        String line = "1       861332  1019397 G       A       .       .       ALLELEID=1003021;CLNDISDB=MedGen:CN517202;CLNDN=not_provided;CLNHGVS=NC_000001.10:g.861332G>A;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Uncertain_significance;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;GENEINFO=SAMD11:148398;MC=SO:0001583|missense_variant;ORIGIN=1";
        assertThat(indexLine(line).isEmpty(), equalTo(true));
    }

    @Test
    void testLikelyBenignNotOnWhitelist() {
        String line = "1       865579  1095790 C       T       .       .       ALLELEID=1067609;CLNDISDB=MedGen:CN517202;CLNDN=not_provided;CLNHGVS=NC_000001.10:g.865579C>T;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Likely_benign;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;GENEINFO=SAMD11:148398;MC=SO:0001819|synonymous_variant;ORIGIN=1";
        assertThat(indexLine(line).isEmpty(), equalTo(true));
    }

    @Test
    void testDrugResponseNotOnWhitelist() {
        String line = "10	96612542	633935	C	G	.	.	AF_EXAC=0.00001;ALLELEID=622296;CLNDISDB=.;CLNDN=CYP2C19:_decreased_function;CLNHGVS=NC_000010.10:g.96612542C>G;CLNREVSTAT=practice_guideline;CLNSIG=drug_response;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=CPIC:1894aa78604f8eea2adc5348|CPIC:29e8be864a7951a5edabebb7|CPIC:5148fe3b90f46e67f305761b|CPIC:PA166128343|CPIC:ca488cacf462a44627d3106c;GENEINFO=CYP2C19:1557;MC=SO:0001583|missense_variant;ORIGIN=1;RS=118203759;CLNDISDBINCL=MedGen:CN077957|MedGen:CN221263|MedGen:CN221264|MedGen:CN221265;CLNDNINCL=Voriconazole_response|Citalopram_response|Escitalopram_response|Sertraline_response;CLNSIGINCL=633936:drug_response|633944:drug_response|633948:drug_response|633950:drug_response|633954:drug_response|633956:drug_response|633963:drug_response|633972:drug_response|633975:drug_response|633976:drug_response|633984:drug_response|633987:drug_response|633999:drug_response|634002:drug_response|634075:drug_response|634111:drug_response|634124:drug_response|634125:drug_response|634126:drug_response|634127:drug_response|634227:drug_response|634229:drug_response|634239:drug_response|634244:drug_response|634265:drug_response|634292:drug_response|634293:drug_response|634338:drug_response|634370:drug_response|634374:drug_response|634375:drug_response|634380:drug_response|634385:drug_response|638792:drug_response";
        assertThat(indexLine(line).isEmpty(), equalTo(true));
    }


    @Test
    void testBenignNotOnWhitelist() {
        String line = "1       865584  1170208 G       A       .       .       ALLELEID=1153702;CLNDISDB=MedGen:CN517202;CLNDN=not_provided;CLNHGVS=NC_000001.10:g.865584G>A;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Benign;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;GENEINFO=SAMD11:148398;MC=SO:0001583|missense_variant;ORIGIN=1";
        assertThat(indexLine(line).isEmpty(), equalTo(true));
    }

    @Test
    void testLikelyPathogenicWithAssertionCriteriaOnWhitelist() {
        String line = "1	2338019	555297	CA	C	.	.	ALLELEID=541178;CLNDISDB=MONDO:MONDO:0013936,MedGen:C3553947,OMIM:614870|MONDO:MONDO:0013937,MedGen:C3553948,OMIM:614871;CLNDN=Peroxisome_biogenesis_disorder_6A|Peroxisome_biogenesis_disorder_6B;CLNHGVS=NC_000001.10:g.2338020del;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Likely_pathogenic;CLNVC=Deletion;CLNVCSO=SO:0000159;GENEINFO=PEX10:5192;MC=SO:0001589|frameshift_variant;ORIGIN=0;RS=1553231783";
        assertThat(indexLine(line), equalTo("1	2338019	CA	C	VARIATIONID=555297;CLNSIG=LIKELY_PATHOGENIC;CLNREVSTAT=CRITERIA_PROVIDED_SINGLE_SUBMITTER;STARS=1\n"));
    }

    @Test
    void testPathogenicWithAssertionCriteriaOnWhitelist() {
        String line = "1	156106935	1072048	GC	G	.	.	ALLELEID=1058441;CLNDISDB=MONDO:MONDO:0018993,MedGen:C0270914,Orphanet:ORPHA64746;CLNDN=Charcot-Marie-Tooth_disease,_type_2;CLNHGVS=NC_000001.10:g.156106941del;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Pathogenic;CLNVC=Deletion;CLNVCSO=SO:0000159;CLNVI=Invitae:10194418;GENEINFO=LMNA:4000;MC=SO:0001589|frameshift_variant;ORIGIN=1";
        assertThat(indexLine(line), equalTo("1	156106935	GC	G	VARIATIONID=1072048;CLNSIG=PATHOGENIC;CLNREVSTAT=CRITERIA_PROVIDED_SINGLE_SUBMITTER;STARS=1\n"));
    }

    @Test
    void testPathogenicWithExpertPAnelReviewOnWhitelist() {
        String line = "11	76895760	179479	G	A	.	.	ALLELEID=178280;CLNDISDB=MONDO:MONDO:0010168,MedGen:C1568247,OMIM:276900,Orphanet:ORPHA231169|MONDO:MONDO:0010807,MedGen:C1838701,OMIM:600060|MONDO:MONDO:0019501,MeSH:D052245,MedGen:C0271097,OMIM:PS276900,Orphanet:ORPHA886|MeSH:D030342,MedGen:C0950123|MedGen:CN169374|MedGen:CN517202;CLNDN=Usher_syndrome_type_1|Deafness,_autosomal_recessive_2|Usher_syndrome|Inborn_genetic_diseases|not_specified|not_provided;CLNHGVS=NC_000011.9:g.76895760G>A;CLNREVSTAT=reviewed_by_expert_panel;CLNSIG=Pathogenic;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;GENEINFO=MYO7A:4647;MC=SO:0001583|missense_variant;ORIGIN=1;RS=797044516";
        assertThat(indexLine(line), equalTo("11	76895760	G	A	VARIATIONID=179479;CLNSIG=PATHOGENIC;CLNREVSTAT=REVIEWED_BY_EXPERT_PANEL;STARS=3\n"));
    }

    @Test
    void testPathogenicWithPracticeGuidelineOnWhitelist() {
        String line = "7	117180324	7110	G	C	.	.	ALLELEID=22149;CLNDISDB=MONDO:MONDO:0009061,MedGen:C0010674,OMIM:219700,Orphanet:ORPHA586,SNOMED_CT:190905008|MONDO:MONDO:0010178,MedGen:C0403814,OMIM:277180|MedGen:CN169374|MedGen:CN517202;CLNDN=Cystic_fibrosis|Congenital_bilateral_aplasia_of_vas_deferens_from_CFTR_mutation|not_specified|not_provided;CLNHGVS=NC_000007.13:g.117180324G>C;CLNREVSTAT=practice_guideline;CLNSIG=Pathogenic;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=ARUP_Laboratories,_Molecular_Genetics_and_Genomics,ARUP_Laboratories:145466|CFTR-France:c.1040G>C|CFTR2:R347P|OMIM_Allelic_Variant:602421.0006|UniProtKB_(protein):P13569#VAR_000155;GENEINFO=CFTR:1080;MC=SO:0001583|missense_variant;ORIGIN=1;RS=77932196";
        assertThat(indexLine(line), equalTo("7	117180324	G	C	VARIATIONID=7110;CLNSIG=PATHOGENIC;CLNREVSTAT=PRACTICE_GUIDELINE;STARS=4\n"));
    }
}