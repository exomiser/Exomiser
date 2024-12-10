/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.AlleleProtoAdaptor;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.core.proto.AlleleProto;
import org.monarchinitiative.exomiser.data.genome.indexers.AlleleConverter;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ClinSig.*;
import static org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData.ReviewStatus.*;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ClinVarAlleleParserTest extends AbstractAlleleParserTester<ClinVarAlleleParser> {

    @Override
    public ClinVarAlleleParser newInstance() {
        return new ClinVarAlleleParser();
    }

    @Test
    public void testNoInfo() {
        String line = "1	28590	.	T	TTGG	999	PASS	";
        Allele expected = new Allele(1, 28590, "T", "TTGG");

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testNoAlleleIdReturnsNoClinVarData() {
        String line = "1	28590	.	T	TTGG	999	PASS	UNWANTED=0.003;FIELDS=these_should_not_produce_anything";
        Allele expected = new Allele(1, 28590, "T", "TTGG");

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testUnwantedInfoField() {
        String line = "1	28590	54321	T	TTGG	999	PASS	ALLELEID=12345;UNWANTED=0.003;FIELDS=these_should_not_produce_anything;RS=11111111";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setRsId("11111111");
        expected.setClinVarData(ClinVarData.builder()
                .variationId("54321")
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseVariationId() {
        String line = "1	28590	54321	T	TTGG	999	PASS	ALLELEID=12345";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setRsId("54321");
        expected.setClinVarData(ClinVarData.builder()
                .variationId("54321")
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClnSigPathogenic() {
        String line = "7	117199644	7105	ATCT	A	.	.	ALLELEID=22144;CLNREVSTAT=practice_guideline;CLNSIG=Pathogenic";
        Allele expected = new Allele(7, 117199644, "ATCT", "A");
        expected.setRsId("7105");
        expected.setClinVarData(ClinVarData.builder()
                .variationId("7105")
                .primaryInterpretation(PATHOGENIC)
                .reviewStatus(PRACTICE_GUIDELINE)
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClnSigLikelyPathogenic() {
        String line = "10	123256215	374823	T	C	.	.	ALLELEID=361707;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Likely_pathogenic;RS=121918506";
        Allele expected = new Allele(10, 123256215, "T", "C");
        expected.setRsId("121918506");
        expected.setClinVarData(ClinVarData.builder()
                .variationId("374823")
                .primaryInterpretation(LIKELY_PATHOGENIC)
                .reviewStatus(CRITERIA_PROVIDED_SINGLE_SUBMITTER)
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClnSig() {
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNSIG=Benign/Likely_benign";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setClinVarData(ClinVarData.builder()
                .primaryInterpretation(BENIGN_OR_LIKELY_BENIGN)
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClnSigWithSecondary() {
        // as of approx. Feb 2024 ClinVar changed their delimiters from ',_' to '|' so the parser will now only recognise
        // files released after this date.
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNSIG=Pathogenic/Likely_pathogenic|other|association";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setClinVarData(ClinVarData.builder()
                .primaryInterpretation(PATHOGENIC_OR_LIKELY_PATHOGENIC)
                .secondaryInterpretations(EnumSet.of(OTHER, ASSOCIATION))
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClnSigUnknownValue() {
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNSIG=WIBBLE!";
        Allele expected = new Allele(1, 28590, "T", "TTGG");

        assertParseLineEquals(line, List.of(expected));
    }


    @Test
    public void testParseClnRevStat() {
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNREVSTAT=criteria_provided,_conflicting_interpretations";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setClinVarData(ClinVarData.builder()
                .reviewStatus(CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClinSigIncl() {
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNSIGINCL=424752:Pathogenic";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setClinVarData(ClinVarData.builder()
                .includedAlleles(Map.of("424752", PATHOGENIC))
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClinSigInclMulti() {
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNSIGINCL=424752:Pathogenic|15612:other";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setClinVarData(ClinVarData.builder()
                .includedAlleles(Map.of(
                        "424752", PATHOGENIC,
                        "15612", OTHER
                ))
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClinSigNoNumbers() {
        String line = "3	39307162	25511	G	A	.	.	ALLELEID=36774;CLNHGVS=NC_000003.11:g.39307162G>A;CLNREVSTAT=no_interpretation_for_the_single_variant;CLNSIGINCL=8152:Pathogenic|_protective|_risk_factor";
        Allele expected = new Allele(3, 39307162, "G", "A");
        expected.setRsId("25511");
        expected.setClinVarData(ClinVarData.builder()
                .variationId("25511")
                .reviewStatus(NO_INTERPRETATION_FOR_THE_SINGLE_VARIANT)
                .includedAlleles(Map.of(
                        "8152", PATHOGENIC
                ))
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    void testParseClinSigConf() {
        String line = "13\t100515567\t218256\tG\tA\t.\t.\tCLNREVSTAT=criteria_provided,_conflicting_interpretations;CLNSIG=Conflicting_interpretations_of_pathogenicity;CLNSIGCONF=Pathogenic(5)|Uncertain_significance(1);RS=369982920";
        Allele expected = new Allele(13, 100515567, "G", "A");
        expected.setRsId("369982920");
        expected.setClinVarData(ClinVarData.builder()
                .variationId("218256")
                .primaryInterpretation(CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .reviewStatus(CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                // CLNSIGCONF=Pathogenic(5)|Uncertain_significance(1)
                .conflictingInterpretationCounts(Map.of(PATHOGENIC, 5, UNCERTAIN_SIGNIFICANCE, 1))
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    /**
     * Test for issue <a href="https://github.com/exomiser/Exomiser/issues/560">560</a>
     */
    @Test
    void testParseClinSigConfClassification() {
        String line = "11\t47352652\t506638\tA\tG\t.\t.\tALLELEID=503825;CLNDISDB=MedGen:CN230736|MedGen:CN169374;CLNDN=Cardiovascular_phenotype|not_specified;CLNHGVS=NC_000011.10:g.47352652A>G;CLNREVSTAT=criteria_provided,_conflicting_classifications;CLNSIG=Conflicting_classifications_of_pathogenicity;CLNSIGCONF=Uncertain_significance(1)|Likely_benign(1);CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=ClinGen:CA658797649;GENEINFO=MYBPC3:4607;ORIGIN=1;RS=886048383";
        Allele expected = new Allele(11, 47352652, "A", "G");
        expected.setRsId("886048383");
        expected.setClinVarData(ClinVarData.builder()
                .variationId("506638")
                .primaryInterpretation(CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .reviewStatus(CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                // CLNSIGCONF=Pathogenic(5)|Uncertain_significance(1)
                .conflictingInterpretationCounts(Map.of(UNCERTAIN_SIGNIFICANCE, 1, LIKELY_BENIGN, 1))
                .build());
        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    void unrecognisedClinSigCombo() {
        String line = "X\t153763492\t100055\tT\tC\t.\t.\tAF_TGP=0.09457;ALLELEID=25399;CLNDISDB=MONDO:MONDO:0005775,MedGen:C2939465|MONDO:MONDO:0010480,MedGen:C2720289,OMIM:300908,Orphanet:466026|MeSH:D030342,MedGen:C0950123|MedGen:C3661900|MedGen:CN169374|.|MONDO:MONDO:0021024,MedGen:C1970028,OMIM:611162,Orphanet:673|MedGen:C3806712,OMIM:300910,Orphanet:391330;CLNDN=G6PD_deficiency|Anemia,_nonspherocytic_hemolytic,_due_to_G6PD_deficiency|Inborn_genetic_diseases|not_provided|not_specified|G6PD_A+|Malaria,_susceptibility_to|Bone_mineral_density_quantitative_trait_locus_18;CLNHGVS=NC_000023.10:g.153763492T>C;CLNREVSTAT=criteria_provided,_conflicting_classifications;CLNSIG=Conflicting_classifications_of_pathogenicity;CLNSIGCONF=Pathogenic(10)|Likely_pathogenic(1)|Uncertain_significance(5)|Benign(1)|Likely_benign(3);CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=ClinGen:CA120939|Genetic_Testing_Registry_(GTR):GTR000167633|OMIM:305900.0001|OMIM:305900.0002|OMIM:305900.0023;GENEINFO=G6PD:2539;MC=SO:0001583|missense_variant;ORIGIN=17;RS=1050829;CLNDISDBINCL=MONDO:MONDO:0010480,MedGen:C2720289,OMIM:300908,Orphanet:466026|.|MONDO:MONDO:0005775,MedGen:C2939465;CLNDNINCL=Anemia,_nonspherocytic_hemolytic,_due_to_G6PD_deficiency|G6PD_SANTAMARIA|G6PD_deficiency;CLNSIGINCL=10361:Likely_pathogenic/Established_risk_allele|10382:Likely_pathogenic|1065168:Pathogenic|1722597:Likely_pathogenic|1722617:Likely_pathogenic|1722622:Likely_pathogenic|1722693:Uncertain_significance|1722697:Uncertain_significance|1722698:Uncertain_significance|1722726:Likely_pathogenic|1722747:Likely_pathogenic";
        Allele expected = new Allele(23, 153763492, "T", "C");
        expected.setRsId("1050829");
        Map<String, ClinVarData.ClinSig> includedAlleles = new HashMap<>();
        includedAlleles.put("1722747", LIKELY_PATHOGENIC);
        includedAlleles.put("1722726", LIKELY_PATHOGENIC);
        includedAlleles.put("1722617", LIKELY_PATHOGENIC);
        includedAlleles.put("1722622", LIKELY_PATHOGENIC);
        includedAlleles.put("10361", LIKELY_PATHOGENIC); // CLNSIGINCL=10361:Likely_pathogenic/Established_risk_allele
        includedAlleles.put("10382", LIKELY_PATHOGENIC);
        includedAlleles.put("1722597", LIKELY_PATHOGENIC);
        includedAlleles.put("1722697", UNCERTAIN_SIGNIFICANCE);
        includedAlleles.put("1722698", UNCERTAIN_SIGNIFICANCE);
        includedAlleles.put("1065168", PATHOGENIC);
        includedAlleles.put("1722693", UNCERTAIN_SIGNIFICANCE);
        expected.setClinVarData(ClinVarData.builder()
                .variationId("100055")
                .primaryInterpretation(CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .reviewStatus(CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                .conflictingInterpretationCounts(Map.of(PATHOGENIC, 10, LIKELY_PATHOGENIC, 1, UNCERTAIN_SIGNIFICANCE, 5, LIKELY_BENIGN, 3, BENIGN, 1))
                .includedAlleles(includedAlleles)
                .build());
        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    void clnSigLowPenetrance() {
        String line = "1\t94473807\t7888\tC\tT\t.\t.\tALLELEID=22927;RS=1800553;CLNREVSTAT=criteria_provided,_multiple_submitters,_no_conflicts;CLNSIG=Pathogenic/Likely_pathogenic/Pathogenic,_low_penetrance;";
        Allele expected = new Allele(1, 94473807, "C", "T");
        expected.setRsId("1800553");
        expected.setClinVarData(ClinVarData.builder()
                .variationId("7888")
                .primaryInterpretation(PATHOGENIC_OR_LIKELY_PATHOGENIC)
                .reviewStatus(CRITERIA_PROVIDED_MULTIPLE_SUBMITTERS_NO_CONFLICTS)
                .build());
        assertParseLineEquals(line, List.of(expected));
    }

    // 4	6301479	215392	G	C	.	.	ALLELEID=211061;CLNDISDB=MedGen:CN517202|MONDO:MONDO:0009101,MedGen:C4551693,OMIM:222300,Orphanet:3463;CLNDN=not_provided|Wolfram_syndrome_1;CLNHGVS=NC_000004.12:g.6301479G>C;CLNREVSTAT=criteria_provided,_multiple_submitters,_no_conflicts;CLNSIG=Likely_pathogenic/Likely_risk_allele;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=ClinGen:CA322818;GENEINFO=WFS1:7466;MC=SO:0001583|missense_variant;ORIGIN=1;RS=753237278


    //CLNSIGINCL=424752:Pathogenic
    //CLNSIGINCL=15127:other|15128:other|15334:Pathogenic|15335:Pathogenic|15336:Pathogenic|15337:Pathogenic|15610:Pathogenic|15612:other

    //Use Primary CLINSIG i.e. CLINSIG.split(",_")[0]
    //CLNSIG counts: Affects=99, Affects,_risk_factor=1, Benign=23891, Benign,_Affects=1, Benign,_association=8, Benign,_association,_protective=1, Benign,_association,_risk_factor=1, Benign,_drug_response=1, Benign,_drug_response,_risk_factor=1, Benign,_other=24, Benign,_protective=4, Benign,_protective,_risk_factor=1, Benign,_risk_factor=30, Benign/Likely_benign=10782, Benign/Likely_benign,_Affects=2, Benign/Likely_benign,_association=3, Benign/Likely_benign,_drug_response=1, Benign/Likely_benign,_drug_response,_risk_factor=1, Benign/Likely_benign,_other=9, Benign/Likely_benign,_protective=7, Benign/Likely_benign,_protective,_risk_factor=1, Benign/Likely_benign,_risk_factor=21, Conflicting_interpretations_of_pathogenicity=12703, Conflicting_interpretations_of_pathogenicity,_Affects=2, Conflicting_interpretations_of_pathogenicity,_Affects,_association,_risk_factor=2, Conflicting_interpretations_of_pathogenicity,_Affects,_other=1, Conflicting_interpretations_of_pathogenicity,_association=3, Conflicting_interpretations_of_pathogenicity,_association,_other,_risk_factor=2, Conflicting_interpretations_of_pathogenicity,_drug_response=4, Conflicting_interpretations_of_pathogenicity,_other=18, Conflicting_interpretations_of_pathogenicity,_other,_risk_factor=1, Conflicting_interpretations_of_pathogenicity,_protective=2, Conflicting_interpretations_of_pathogenicity,_risk_factor=46, Likely_benign=52024, Likely_benign,_Affects=1, Likely_benign,_association=2, Likely_benign,_drug_response=1, Likely_benign,_other=12, Likely_benign,_protective=2, Likely_benign,_risk_factor=22, Likely_pathogenic=15083, Likely_pathogenic,_Affects=1, Likely_pathogenic,_association=2, Likely_pathogenic,_drug_response=11, Likely_pathogenic,_other=6, Likely_pathogenic,_risk_factor=24, Pathogenic=46578, Pathogenic,_Affects=8, Pathogenic,_association=4, Pathogenic,_association,_protective=1, Pathogenic,_drug_response=26, Pathogenic,_other=96, Pathogenic,_other,_protective=2, Pathogenic,_protective=9, Pathogenic,_protective,_risk_factor=1, Pathogenic,_risk_factor=78, Pathogenic/Likely_pathogenic=3239, Pathogenic/Likely_pathogenic,_Affects,_risk_factor=1, Pathogenic/Likely_pathogenic,_drug_response=5, Pathogenic/Likely_pathogenic,_other=12, Pathogenic/Likely_pathogenic,_risk_factor=21, Uncertain_significance=120363, Uncertain_significance,_Affects=1, Uncertain_significance,_association=1, Uncertain_significance,_drug_response=17, Uncertain_significance,_other=17, Uncertain_significance,_protective=1, Uncertain_significance,_risk_factor=18, association=144, association,_protective=1, association,_risk_factor=3, drug_response=285, drug_response,_other=4, drug_response,_risk_factor=1, not_provided=10980, other=1796, protective=27, protective,_risk_factor=3, risk_factor=411
    //CLINSIG split(",_") counts: {Affects=120, Benign=23963, Benign/Likely_benign=10827, Conflicting_interpretations_of_pathogenicity=12784, Likely_benign=52064, Likely_pathogenic=15127, Pathogenic=46803, Pathogenic/Likely_pathogenic=3278, Uncertain_significance=120418, association=178, drug_response=358, not_provided=10980, other=2000, protective=63, risk_factor=691
    //CLINSIG split(",_")[0] counts: Affects=100, Benign=23963, Benign/Likely_benign=10827, Conflicting_interpretations_of_pathogenicity=12784, Likely_benign=52064, Likely_pathogenic=15127, Pathogenic=46803, Pathogenic/Likely_pathogenic=3278, Uncertain_significance=120418, association=148, drug_response=290, not_provided=10980, other=1796, protective=30, risk_factor=411

    //CLNVC counts: Deletion=22574, Duplication=9075, Indel=2162, Insertion=2358, Inversion=32, Microsatellite=15, Variation=9, copy_number_loss=4, single_nucleotide_variant=264009
    //CLNREVSTAT counts: criteria_provided,_conflicting_interpretations=12678, criteria_provided,_multiple_submitters,_no_conflicts=34967, criteria_provided,_single_submitter=197277, no_assertion_criteria_provided=34308, no_assertion_provided=10980, no_interpretation_for_the_single_variant=500, practice_guideline=23, reviewed_by_expert_panel=8786

    @Disabled("data exploration")
    @Test
    void testClnSigValues() {
        Path clinVarVcfFile = Path.of("clinvar_hg38.vcf.gz");
        var infoFieldCounts = getInfoFieldCounts(clinVarVcfFile, "CLNSIG");
        for (Map.Entry<String, Long> entry : infoFieldCounts.entrySet()) {
            System.out.println(entry);
        }
        //##fileDate=2024-05-09
        //##INFO=<ID=CLNSIG,Number=.,Type=String,Description="Aggregate germline classification for this single variant; multiple values are separated by a vertical bar">
        //Affects=142
        //Affects|risk_factor=1
        //Benign=202927
        //Benign/Likely_benign=48319
        //Benign/Likely_benign|association=1
        //Benign/Likely_benign|drug_response=2
        //Benign/Likely_benign|drug_response|other=3
        //Benign/Likely_benign|other=9
        //Benign/Likely_benign|other|risk_factor=1
        //Benign/Likely_benign|risk_factor=2
        //Benign|Affects=1
        //Benign|association=5
        //Benign|confers_sensitivity=2
        //Benign|drug_response=2
        //Benign|other=9
        //Benign|risk_factor=3
        //Conflicting_classifications_of_pathogenicity=125784
        //Conflicting_classifications_of_pathogenicity|Affects=2
        //Conflicting_classifications_of_pathogenicity|association=8
        //Conflicting_classifications_of_pathogenicity|association|risk_factor=2
        //Conflicting_classifications_of_pathogenicity|drug_response=2
        //Conflicting_classifications_of_pathogenicity|drug_response|other=1
        //Conflicting_classifications_of_pathogenicity|other=25
        //Conflicting_classifications_of_pathogenicity|other|risk_factor=2
        //Conflicting_classifications_of_pathogenicity|protective=1
        //Conflicting_classifications_of_pathogenicity|risk_factor=15
        //Likely_benign=874860
        //Likely_benign|association=1
        //Likely_benign|drug_response|other=4
        //Likely_benign|other=12
        //Likely_benign|risk_factor=1
        //Likely_pathogenic=82058
        //Likely_pathogenic,_low_penetrance=6
        //Likely_pathogenic/Likely_risk_allele=17
        //Likely_pathogenic|Affects=1
        //Likely_pathogenic|association=5
        //Likely_pathogenic|drug_response=21
        //Likely_pathogenic|other=2
        //Likely_pathogenic|protective=1
        //Likely_pathogenic|risk_factor=10
        //Likely_risk_allele=97
        //Pathogenic=157595
        //Pathogenic/Likely_pathogenic=26459
        //Pathogenic/Likely_pathogenic/Likely_risk_allele=4
        //Pathogenic/Likely_pathogenic/Pathogenic,_low_penetrance=6
        //Pathogenic/Likely_pathogenic/Pathogenic,_low_penetrance/Established_risk_allele=1
        //Pathogenic/Likely_pathogenic/Pathogenic,_low_penetrance|other=1
        //Pathogenic/Likely_pathogenic|drug_response=3
        //Pathogenic/Likely_pathogenic|other=11
        //Pathogenic/Likely_pathogenic|risk_factor=6
        //Pathogenic/Likely_risk_allele=9
        //Pathogenic/Likely_risk_allele|risk_factor=1
        //Pathogenic/Pathogenic,_low_penetrance|other=1
        //Pathogenic/Pathogenic,_low_penetrance|other|risk_factor=1
        //Pathogenic|Affects=6
        //Pathogenic|association=6
        //Pathogenic|association|protective=1
        //Pathogenic|confers_sensitivity=1
        //Pathogenic|drug_response=54
        //Pathogenic|other=71
        //Pathogenic|protective=2
        //Pathogenic|risk_factor=22
        //Uncertain_risk_allele=35
        //Uncertain_risk_allele|risk_factor=1
        //Uncertain_significance=1358995
        //Uncertain_significance/Uncertain_risk_allele=153
        //Uncertain_significance|association=8
        //Uncertain_significance|drug_response=11
        //Uncertain_significance|other=4
        //Uncertain_significance|risk_factor=7
        //association=341
        //association_not_found=4
        //association|drug_response|risk_factor=1
        //association|risk_factor=1
        //confers_sensitivity=11
        //confers_sensitivity|other=1
        //drug_response=1869
        //drug_response|other=1
        //drug_response|risk_factor=3
        //no_classification_for_the_single_variant=683
        //no_classifications_from_unflagged_records=7
        //not_provided=9808
        //other=1588
        //other|risk_factor=1
        //protective=39
        //protective|risk_factor=4
        //risk_factor=407
    }

    @Disabled("data exploration")
    @Test
    void testClnRevStatValues() {
        Path clinVarVcfFile = Path.of("clinvar.vcf.gz");
        var infoFieldCounts = getInfoFieldCounts(clinVarVcfFile, "CLNREVSTAT");
        for (Map.Entry<String, Long> entry : infoFieldCounts.entrySet()) {
            System.out.println(entry);
        }
        //##fileDate=2024-05-09
        //##INFO=<ID=CLNREVSTAT,Number=.,Type=String,Description="ClinVar review status of germline classification for the Variation ID">
        //criteria_provided,_conflicting_classifications=125583
        //criteria_provided,_multiple_submitters,_no_conflicts=425991
        //criteria_provided,_single_submitter=2266963
        //no_assertion_criteria_provided=48407
        //no_classification_for_the_single_variant=683
        //no_classification_provided=9806
        //no_classifications_from_unflagged_records=13
        //practice_guideline=51
        //reviewed_by_expert_panel=16215
    }

    private Map<String, Long> getInfoFieldCounts(Path clinVarVcfFile, String infoKey) {
        try (Stream<String> lines = new BufferedReader(new InputStreamReader(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(clinVarVcfFile))))).lines()) {
            var result = lines
                    .peek(line -> {if (line.startsWith("#")) {System.out.println(line);}})
                    .filter(line -> !line.startsWith("#"))
                    .map(findInfoField(infoKey))
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            return new TreeMap<>(result);
        } catch (IOException e) {
            System.err.println(e);
        }
        return Map.of();
    }

    private Function<String, String> findInfoField(String infoKey) {
        return line -> {
            var fields = line.split("\t");
            var info= fields[7];
            var infoFields = info.split(";");
            for (String field : infoFields) {
                if (field.startsWith(infoKey)) {
                    return field.split("=")[1];
                }
            }
            return "";
        };
    }
}

