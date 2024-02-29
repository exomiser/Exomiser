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

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.pathogenicity.ClinVarData;
import org.monarchinitiative.exomiser.data.genome.model.Allele;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;


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
                .primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC)
                .reviewStatus(ClinVarData.ReviewStatus.PRACTICE_GUIDELINE)
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
                .primaryInterpretation(ClinVarData.ClinSig.LIKELY_PATHOGENIC)
                .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_SINGLE_SUBMITTER)
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClnSig() {
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNSIG=Benign/Likely_benign";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setClinVarData(ClinVarData.builder()
                .primaryInterpretation(ClinVarData.ClinSig.BENIGN_OR_LIKELY_BENIGN)
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClnSigWithSecondary() {
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNSIG=Pathogenic/Likely_pathogenic,_other,_association";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setClinVarData(ClinVarData.builder()
                .primaryInterpretation(ClinVarData.ClinSig.PATHOGENIC_OR_LIKELY_PATHOGENIC)
                .secondaryInterpretations(EnumSet.of(ClinVarData.ClinSig.OTHER, ClinVarData.ClinSig.ASSOCIATION))
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
                .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClinSigIncl() {
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNSIGINCL=424752:Pathogenic";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setClinVarData(ClinVarData.builder()
                .includedAlleles(Map.of("424752", ClinVarData.ClinSig.PATHOGENIC))
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    @Test
    public void testParseClinSigInclMulti() {
        String line = "1	28590	.	T	TTGG	999	PASS	ALLELEID=12345;CLNSIGINCL=424752:Pathogenic|15612:other";
        Allele expected = new Allele(1, 28590, "T", "TTGG");
        expected.setClinVarData(ClinVarData.builder()
                .includedAlleles(Map.of(
                        "424752", ClinVarData.ClinSig.PATHOGENIC,
                        "15612", ClinVarData.ClinSig.OTHER
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
                .reviewStatus(ClinVarData.ReviewStatus.NO_INTERPRETATION_FOR_THE_SINGLE_VARIANT)
                .includedAlleles(Map.of(
                        "8152", ClinVarData.ClinSig.PATHOGENIC
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
                .primaryInterpretation(ClinVarData.ClinSig.CONFLICTING_PATHOGENICITY_INTERPRETATIONS)
                .reviewStatus(ClinVarData.ReviewStatus.CRITERIA_PROVIDED_CONFLICTING_INTERPRETATIONS)
                // CLNSIGCONF=Pathogenic(5)|Uncertain_significance(1)
                .conflictingInterpretationCounts(Map.of(ClinVarData.ClinSig.PATHOGENIC, 5, ClinVarData.ClinSig.UNCERTAIN_SIGNIFICANCE, 1))
                .build());

        assertParseLineEquals(line, List.of(expected));
    }

    //CLNSIGINCL=424752:Pathogenic
    //CLNSIGINCL=15127:other|15128:other|15334:Pathogenic|15335:Pathogenic|15336:Pathogenic|15337:Pathogenic|15610:Pathogenic|15612:other

    //Use Primary CLINSIG i.e. CLINSIG.split(",_")[0]
    //CLNSIG counts: Affects=99, Affects,_risk_factor=1, Benign=23891, Benign,_Affects=1, Benign,_association=8, Benign,_association,_protective=1, Benign,_association,_risk_factor=1, Benign,_drug_response=1, Benign,_drug_response,_risk_factor=1, Benign,_other=24, Benign,_protective=4, Benign,_protective,_risk_factor=1, Benign,_risk_factor=30, Benign/Likely_benign=10782, Benign/Likely_benign,_Affects=2, Benign/Likely_benign,_association=3, Benign/Likely_benign,_drug_response=1, Benign/Likely_benign,_drug_response,_risk_factor=1, Benign/Likely_benign,_other=9, Benign/Likely_benign,_protective=7, Benign/Likely_benign,_protective,_risk_factor=1, Benign/Likely_benign,_risk_factor=21, Conflicting_interpretations_of_pathogenicity=12703, Conflicting_interpretations_of_pathogenicity,_Affects=2, Conflicting_interpretations_of_pathogenicity,_Affects,_association,_risk_factor=2, Conflicting_interpretations_of_pathogenicity,_Affects,_other=1, Conflicting_interpretations_of_pathogenicity,_association=3, Conflicting_interpretations_of_pathogenicity,_association,_other,_risk_factor=2, Conflicting_interpretations_of_pathogenicity,_drug_response=4, Conflicting_interpretations_of_pathogenicity,_other=18, Conflicting_interpretations_of_pathogenicity,_other,_risk_factor=1, Conflicting_interpretations_of_pathogenicity,_protective=2, Conflicting_interpretations_of_pathogenicity,_risk_factor=46, Likely_benign=52024, Likely_benign,_Affects=1, Likely_benign,_association=2, Likely_benign,_drug_response=1, Likely_benign,_other=12, Likely_benign,_protective=2, Likely_benign,_risk_factor=22, Likely_pathogenic=15083, Likely_pathogenic,_Affects=1, Likely_pathogenic,_association=2, Likely_pathogenic,_drug_response=11, Likely_pathogenic,_other=6, Likely_pathogenic,_risk_factor=24, Pathogenic=46578, Pathogenic,_Affects=8, Pathogenic,_association=4, Pathogenic,_association,_protective=1, Pathogenic,_drug_response=26, Pathogenic,_other=96, Pathogenic,_other,_protective=2, Pathogenic,_protective=9, Pathogenic,_protective,_risk_factor=1, Pathogenic,_risk_factor=78, Pathogenic/Likely_pathogenic=3239, Pathogenic/Likely_pathogenic,_Affects,_risk_factor=1, Pathogenic/Likely_pathogenic,_drug_response=5, Pathogenic/Likely_pathogenic,_other=12, Pathogenic/Likely_pathogenic,_risk_factor=21, Uncertain_significance=120363, Uncertain_significance,_Affects=1, Uncertain_significance,_association=1, Uncertain_significance,_drug_response=17, Uncertain_significance,_other=17, Uncertain_significance,_protective=1, Uncertain_significance,_risk_factor=18, association=144, association,_protective=1, association,_risk_factor=3, drug_response=285, drug_response,_other=4, drug_response,_risk_factor=1, not_provided=10980, other=1796, protective=27, protective,_risk_factor=3, risk_factor=411
    //CLINSIG split(",_") counts: {Affects=120, Benign=23963, Benign/Likely_benign=10827, Conflicting_interpretations_of_pathogenicity=12784, Likely_benign=52064, Likely_pathogenic=15127, Pathogenic=46803, Pathogenic/Likely_pathogenic=3278, Uncertain_significance=120418, association=178, drug_response=358, not_provided=10980, other=2000, protective=63, risk_factor=691
    //CLINSIG split(",_")[0] counts: Affects=100, Benign=23963, Benign/Likely_benign=10827, Conflicting_interpretations_of_pathogenicity=12784, Likely_benign=52064, Likely_pathogenic=15127, Pathogenic=46803, Pathogenic/Likely_pathogenic=3278, Uncertain_significance=120418, association=148, drug_response=290, not_provided=10980, other=1796, protective=30, risk_factor=411

    //CLNVC counts: Deletion=22574, Duplication=9075, Indel=2162, Insertion=2358, Inversion=32, Microsatellite=15, Variation=9, copy_number_loss=4, single_nucleotide_variant=264009
    //CLNREVSTAT counts: criteria_provided,_conflicting_interpretations=12678, criteria_provided,_multiple_submitters,_no_conflicts=34967, criteria_provided,_single_submitter=197277, no_assertion_criteria_provided=34308, no_assertion_provided=10980, no_interpretation_for_the_single_variant=500, practice_guideline=23, reviewed_by_expert_panel=8786
}

