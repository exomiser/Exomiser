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
import org.monarchinitiative.exomiser.data.genome.model.Allele;
import org.monarchinitiative.exomiser.data.genome.model.resource.DbNsfp4AlleleResource;

import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.monarchinitiative.exomiser.core.proto.AlleleProto.PathogenicitySource.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class DbNsfpAlleleParserTest {

    private List<Allele> parseLine(DbNsfpColumnIndex columnIndex, String line) {
        DbNsfpAlleleParser instance = new DbNsfpAlleleParser(columnIndex);
        instance.parseLine(LineBuilder.HEADER);
        return instance.parseLine(line);
    }

    private void assertParseLineEqualsExpected(DbNsfpColumnIndex columnIndex, String line, List<Allele> expectedAlleles) {
        List<Allele> alleles = parseLine(columnIndex, line);
        assertThat(alleles.size(), equalTo(expectedAlleles.size()));
        for (int i = 0; i < alleles.size(); i++) {
            Allele allele = alleles.get(i);
            Allele expected = expectedAlleles.get(i);
            assertThat(allele, equalTo(expected));
            assertThat(allele.getRsId(), equalTo(expected.getRsId()));
            assertThat(allele.getPathogenicityScores(), equalTo(expected.getPathogenicityScores()));
        }
    }

    @Test
    void parseChrZero() {
        String line = lineBuilder().chr19("0").pos19("11").ref("A").alt("T").build();
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of());
    }

    @Test
    void parseHg19NoScoresNoRsId() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11").build();
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of());
    }

    @Test
    void parseHg38NoScoresNoRsId() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11").build();
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG38, line, List.of());
    }

    @Test
    void parseHg19NoScoresRsId() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .rs("rs12345")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.setRsId("rs12345");
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of());
    }

    @Test
    void parseHg38NoScoresRsId() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .rs("rs12345")
                .build();
        Allele allele = new Allele(1, 10, "A", "T");
        allele.setRsId("rs12345");
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG38, line, List.of());
    }

    @Test
    void parseMultiRsId() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .rs("rs12345;rs54321")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.setRsId("rs12345");
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of());
    }

    @Test
    void parseSift() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .sift("0.0")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(SIFT, 0f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parseSiftMultipleValuesReturnsHighest() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .sift("0.0;1.0")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(SIFT, 0f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parsePolyPhen() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .polyPhen2Hvar("1.0")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(POLYPHEN, 1f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parsePolyPhenMultipleValuesReturnsHighest() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .polyPhen2Hvar("0.998;0.02;0.99")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(POLYPHEN, 0.998f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parseMutationTaster() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .mTasterScore("1")
                .mTasterPred("D")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(MUTATION_TASTER, 1f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parseMutationTasterMultipleValuesReturnsHighest() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .mTasterScore("0.880664;1")
                .mTasterPred("D;D")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(MUTATION_TASTER, 1f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parseRevel() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .revelScore("1")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(REVEL, 1f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parseMvpSingleTranscript() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .mvpScore("1.0")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(MVP, 1f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parseMvpMultiTranscript() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .mvpScore("1.0;0.5;0.1")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(MVP, 1f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parseAlphaMissense() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .alphaMissenseScore("1")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(ALPHA_MISSENSE, 1f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    @Test
    void parseEve() {
        String line = lineBuilder().chr("1").pos("10").ref("A").alt("T").chr19("1").pos19("11")
                .eveScore("1")
                .build();
        Allele allele = new Allele(1, 11, "A", "T");
        allele.addPathogenicityScore(AlleleData.pathogenicityScoreOf(EVE, 1f));
        assertParseLineEqualsExpected(DbNsfpColumnIndex.HG19, line, List.of(allele));
    }

    //##chr	pos(1-based)	ref	alt	aaref	aaalt	rs_dbSNP147	hg19_chr	hg19_pos(1-based)	hg18_chr	hg18_pos(1-based)	genename	cds_strand	refcodon	codonpos	codon_degeneracy	Ancestral_allele	AltaiNeandertal	Denisova	Ensembl_geneid	Ensembl_transcriptid	Ensembl_proteinid	aapos	SIFT_score	SIFT_converted_rankscore	SIFT_pred	Uniprot_acc_Polyphen2	Uniprot_id_Polyphen2	Uniprot_aapos_Polyphen2	Polyphen2_HDIV_score	Polyphen2_HDIV_rankscore	Polyphen2_HDIV_pred	Polyphen2_HVAR_score	Polyphen2_HVAR_rankscore	Polyphen2_HVAR_pred	LRT_score	LRT_converted_rankscore	LRT_pred	LRT_Omega	MutationTaster_score	MutationTaster_converted_rankscore	MutationTaster_pred	MutationTaster_model	MutationTaster_AAE	MutationAssessor_UniprotID	MutationAssessor_variant	MutationAssessor_score	MutationAssessor_score_rankscore	MutationAssessor_pred	FATHMM_score	FATHMM_converted_rankscore	FATHMM_pred	PROVEAN_score	PROVEAN_converted_rankscore	PROVEAN_pred	Transcript_id_VEST3	Transcript_var_VEST3	VEST3_score	VEST3_rankscore	MetaSVM_score	MetaSVM_rankscore	MetaSVM_pred	MetaLR_score	MetaLR_rankscore	MetaLR_pred	Reliability_index	M-CAP_score	M-CAP_rankscore	M-CAP_pred	CADD_raw	CADD_raw_rankscore	CADD_phred	DANN_score	DANN_rankscore	fathmm-MKL_coding_score	fathmm-MKL_coding_rankscore	fathmm-MKL_coding_pred	fathmm-MKL_coding_group	Eigen_coding_or_noncoding	Eigen-raw	Eigen-phred	Eigen-PC-raw	Eigen-PC-phred	Eigen-PC-raw_rankscore	GenoCanyon_score	GenoCanyon_score_rankscore	integrated_fitCons_score	integrated_fitCons_score_rankscore	integrated_confidence_value	GM12878_fitCons_score	GM12878_fitCons_score_rankscore	GM12878_confidence_value	H1-hESC_fitCons_score	H1-hESC_fitCons_score_rankscore	H1-hESC_confidence_value	HUVEC_fitCons_score	HUVEC_fitCons_score_rankscore	HUVEC_confidence_value	GERP++_NR	GERP++_RS	GERP++_RS_rankscore	phyloP100way_vertebrate	phyloP100way_vertebrate_rankscore	phyloP20way_mammalian	phyloP20way_mammalian_rankscore	phastCons100way_vertebrate	phastCons100way_vertebrate_rankscore	phastCons20way_mammalian	phastCons20way_mammalian_rankscore	SiPhy_29way_pi	SiPhy_29way_logOdds	SiPhy_29way_logOdds_rankscore	1000Gp3_AC	1000Gp3_AF	1000Gp3_AFR_AC	1000Gp3_AFR_AF	1000Gp3_EUR_AC	1000Gp3_EUR_AF	1000Gp3_AMR_AC	1000Gp3_AMR_AF	1000Gp3_EAS_AC	1000Gp3_EAS_AF	1000Gp3_SAS_AC	1000Gp3_SAS_AF	TWINSUK_AC	TWINSUK_AF	ALSPAC_AC	ALSPAC_AF	ESP6500_AA_AC	ESP6500_AA_AF	ESP6500_EA_AC	ESP6500_EA_AF	ExAC_AC	ExAC_AF	ExAC_Adj_AC	ExAC_Adj_AF	ExAC_AFR_AC	ExAC_AFR_AF	ExAC_AMR_AC	ExAC_AMR_AF	ExAC_EAS_AC	ExAC_EAS_AF	ExAC_FIN_AC	ExAC_FIN_AF	ExAC_NFE_AC	ExAC_NFE_AF	ExAC_SAS_AC	ExAC_SAS_AF	ExAC_nonTCGA_AC	ExAC_nonTCGA_AF	ExAC_nonTCGA_Adj_AC	ExAC_nonTCGA_Adj_AF	ExAC_nonTCGA_AFR_AC	ExAC_nonTCGA_AFR_AF	ExAC_nonTCGA_AMR_AC	ExAC_nonTCGA_AMR_AF	ExAC_nonTCGA_EAS_AC	ExAC_nonTCGA_EAS_AF	ExAC_nonTCGA_FIN_AC	ExAC_nonTCGA_FIN_AF	ExAC_nonTCGA_NFE_AC	ExAC_nonTCGA_NFE_AF	ExAC_nonTCGA_SAS_AC	ExAC_nonTCGA_SAS_AF	ExAC_nonpsych_AC	ExAC_nonpsych_AF	ExAC_nonpsych_Adj_AC	ExAC_nonpsych_Adj_AF	ExAC_nonpsych_AFR_AC	ExAC_nonpsych_AFR_AF	ExAC_nonpsych_AMR_AC	ExAC_nonpsych_AMR_AF	ExAC_nonpsych_EAS_AC	ExAC_nonpsych_EAS_AF	ExAC_nonpsych_FIN_AC	ExAC_nonpsych_FIN_AF	ExAC_nonpsych_NFE_AC	ExAC_nonpsych_NFE_AF	ExAC_nonpsych_SAS_AC	ExAC_nonpsych_SAS_AF	clinvar_rs	clinvar_clnsig	clinvar_trait	clinvar_golden_stars	Interpro_domain	GTEx_V6_gene	GTEx_V6_tissue

    //M	3307	A	C	I	L	.	M	3308	M	3308	MT-ND1	+	ATA	1	0	A	A/A	A/A	ENSG00000198888	ENST00000361390	ENSP00000354687	1	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1.011493	0.19087	10.73	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	2.68	0.30700	3.333000	0.51560	-0.276000	0.08517	0.981000	0.35144	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.
    //M	3307	A	G	I	V	.	M	3308	M	3308	MT-ND1	+	ATA	1	0	A	A/A	A/A	ENSG00000198888	ENST00000361390	ENSP00000354687	1	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	0.738215	0.16434	9.075	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	2.68	0.30700	3.333000	0.51560	-0.276000	0.08517	0.981000	0.35144	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.
    //M	3307	A	T	I	L	.	M	3308	M	3308	MT-ND1	+	ATA	1	0	A	A/A	A/A	ENSG00000198888	ENST00000361390	ENSP00000354687	1	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1.087687	0.19852	11.15	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	2.68	0.30700	3.333000	0.51560	-0.276000	0.08517	0.981000	0.35144	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.
    //M	3308	T	A	I	K	.	M	3309	M	3309	MT-ND1	+	ATA	2	0	t	T/T	T/T	ENSG00000198888	ENST00000361390	ENSP00000354687	1	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1	0.08979	N	complex_aae	first 2 AA missing	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	3.387294	0.46542	23.0	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	3.08	0.34428	4.722000	0.61414	-0.364000	0.06755	1.000000	0.71511	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.
    //M	3308	T	C	I	T	.	M	3309	M	3309	MT-ND1	+	ATA	2	0	t	T/T	T/T	ENSG00000198888	ENST00000361390	ENSP00000354687	1	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1	0.08979	N	complex_aae	first 2 AA missing	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	2.362247	0.34514	18.57	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	3.08	0.34428	4.722000	0.61414	-0.364000	0.06755	1.000000	0.71511	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	rs28358582	5|5|3	Carcinoma of colon|SUDDEN INFANT DEATH SYNDROME|not specified	0|0|1	.	.	.
    //M	3308	T	G	I	R	.	M	3309	M	3309	MT-ND1	+	ATA	2	0	t	T/T	T/T	ENSG00000198888	ENST00000361390	ENSP00000354687	1	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1	0.08979	N	complex_aae	first 2 AA missing	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	3.711150	0.50300	23.3	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	3.08	0.34428	4.722000	0.61414	-0.364000	0.06755	1.000000	0.71511	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	rs28358582	5	SUDDEN INFANT DEATH SYNDROME	0	.	.	.
    //M	3309	A	G	I	M	.	M	3310	M	3310	MT-ND1	+	ATA	3	3	A	A/A	A/A	ENSG00000198888	ENST00000361390	ENSP00000354687	1	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1	0.08979	N	complex_aae	first 2 AA missing	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	-0.196972	0.07623	1.114	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	1.4	0.21290	3.182000	0.50363	0.024000	0.14708	1.000000	0.71511	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.
    //M	3310	C	A	P	T	.	M	3311	M	3311	MT-ND1	+	CCC	1	0	C	C/C	C/C	ENSG00000198888	ENST00000361390	ENSP00000354687	2	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1	0.08979	N	complex_aae	P2T	.	.	.	.	.	2.88	0.10313	T	0.82	0.01820	N	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1.979908	0.29879	16.08	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	-2.38	0.06208	-2.215000	0.01297	-5.679000	0.00008	0.000000	0.06329	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.
    //M	3310	C	G	P	A	.	M	3311	M	3311	MT-ND1	+	CCC	1	0	C	C/C	C/C	ENSG00000198888	ENST00000361390	ENSP00000354687	2	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1	0.08979	N	complex_aae	P2A	.	.	.	.	.	2.88	0.10313	T	0.5	0.02953	N	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1.653600	0.26002	14.15	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	-2.38	0.06208	-2.215000	0.01297	-5.679000	0.00008	0.000000	0.06329	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.
    //M	3310	C	T	P	S	.	M	3311	M	3311	MT-ND1	+	CCC	1	0	C	C/C	C/C	ENSG00000198888	ENST00000361390	ENSP00000354687	2	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	1	0.08979	N	complex_aae	P2S	.	.	.	.	.	2.85	0.10599	T	0.74	0.02044	N	.	.	.	.	.	.	.	.	.	.	.	.	.	.	2.450449	0.35574	19.15	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	4.06	-2.38	0.06208	-2.215000	0.01297	-5.679000	0.00008	0.000000	0.06329	0.000000	0.01567	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.
    //13	28664538	C	T	P	L	.	13	29238675	13	28136675	POMP	+	CCT	2	0	C	C/C	C/C	ENSG00000132963	ENST00000380842	ENSP00000370222	44	0.173	0.22540	T	Q9Y244	POMP_HUMAN	44	0.998	0.71542	D	0.955	0.67890	D	0.000000	0.84324	D	0.000000	1	0.81033	D	simple_aae	P44L	POMP_HUMAN	P44L	2.455	0.71470	M	.	.	.	-3.15	0.64090	D	NM_015932.5	P44L	0.836	0.82343	-0.2990	0.75030	T	0.4270	0.77031	T	9	0.00730115482713	0.19535	T	4.998548	0.67259	25.1	0.99741724067215187	0.83418	0.98666	0.85225	D	AEFBI	c	0.805093972835176	8.880976	0.794489500351649	9.962374	0.89075	0.999999999999894	0.74713	0.706548	0.72963	0	0.662677	0.62754	0	0.724815	0.87840	0	0.714379	0.83238	0	5.75	5.75	0.90354	6.534000	0.73552	0.935000	0.48983	1.000000	0.71511	1.000000	0.88762	0.0:1.0:0.0:0.0	16.8601	0.85817	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.
    //13	32379787	T	A	Y	X	.	13	32953924	13	31851924	BRCA2	+	TAT	3	2	T	T/T	T/T	ENSG00000139618	ENST00000380152;ENST00000544455	ENSP00000369497;ENSP00000439902	2997	.	.	.	.	.	.	.	.	.	.	.	.	0.108175	0.19527	N	0.574777	1;1	0.81033	A;A	complex_aae;complex_aae	Y2997*;Y2997*	BRCA2_HUMAN	Y2997*	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	11.299675	0.97373	37	0.9818776762409972	0.38501	0.28578	0.23370	N	AEBI	c	0.121064704783902	2.970226	-0.157505361898804	1.891293	0.33017	0.0631945254690647	0.15300	0.6512	0.46512	0	0.708844	0.79302	0	0.658983	0.55576	0	0.683762	0.67193	0	5.66	0.453	0.15778	0.494000	0.22014	0.144000	0.23057	0.202000	0.24110	0.997000	0.65342	0.0:0.2084:0.1291:0.6625	5.868	0.17915	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	Nucleic acid-binding, OB-fold	.	.

//    Allele{chr=12, pos=300334, ref='G', alt='A', rsId='.', values={SIFT=0.616, MUT_TASTER=1.0, POLYPHEN=0.003}} sift=0.616 polyPhen=0.003 mTasterScore=1;1;1;1;1 mTasterPred=D;D;D;D;D
//Allele{chr=12, pos=300334, ref='G', alt='C', rsId='.', values={SIFT=0.559, MUT_TASTER=0.999977, POLYPHEN=0.003}} sift=0.559 polyPhen=0.003 mTasterScore=0.999977;0.999977;0.999977;0.999977;0.999977 mTasterPred=D;D;D;D;D
//Allele{chr=12, pos=300334, ref='G', alt='T', rsId='.', values={SIFT=0.096, MUT_TASTER=0.999956, POLYPHEN=0.022}} sift=0.096 polyPhen=0.022 mTasterScore=0.999956;0.999956;0.999956;0.999956;0.999956 mTasterPred=D;D;D;D;D
//Allele{chr=13, pos=20224248, ref='T', alt='C', rsId='.', values={SIFT=0.094}} sift=0.094 polyPhen=0.003;0.0;0.0 mTasterScore0.999998;0.999998 mTasterPredN;N
//    Allele{chr=13, pos=20224248, ref='T', alt='G', rsId='.', values={SIFT=0.227, POLYPHEN=0.0}} sift=0.227 polyPhen=0.0 mTasterScore0.999997;0.999997 mTasterPredN;N
//    Allele{chr=13, pos=20224383, ref='A', alt='T', rsId='.', values={SIFT=0.004}} sift=0.004 polyPhen=0.229;0.023;0.167 mTasterScore1;1 mTasterPredN;N
//    Allele{chr=13, pos=20233203, ref='C', alt='T', rsId='.', values={}} sift=. polyPhen=. mTasterScore1;1 mTasterPredA;A
//    Allele{chr=13, pos=20233253, ref='T', alt='G', rsId='.', values={SIFT=0.017, POLYPHEN=0.804}} sift=0.017 polyPhen=0.804 mTasterScore0.683266;0.683266 mTasterPredD;D
// Allele{chr=13, pos=20233254, ref='C', alt='A', rsId='.', values={SIFT=0.053}} sift=0.053 polyPhen=0.79;0.068 mTasterScore0.998999;0.998999 mTasterPredD;D
//Allele{chr=13, pos=20233254, ref='C', alt='G', rsId='.', values={SIFT=0.005}} sift=0.005 polyPhen=0.556;0.337 mTasterScore1;1 mTasterPredD;D
//    Allele{chr=13, pos=20233251, ref='A', alt='T', rsId='.', values={SIFT=0.011}} sift=0.011 polyPhen=0.648;0.663 mTasterScore0.957857;0.957857 mTasterPredN;N
//    Allele{chr=17, pos=10209848, ref='C', alt='A', rsId='.', values={MUT_TASTER=0.987356, POLYPHEN=0.25}} sift=.;0.003;. polyPhen=0.648;0.663 mTasterScore=0.987356;0.987356;0.987356 mTasterPred=D;D;D

    private static LineBuilder lineBuilder() {
        return new LineBuilder();
    }

    /**
     * Utility class to build lines in a type-safe way.
     */
    private static class LineBuilder {

        private static final String HEADER =
                "#chr\t" +
                "pos(1-based)\t" +
                "ref\t" +
                "alt\t" +
                "rs_dbSNP147\t" +
                "hg19_chr\t" +
                "hg19_pos(1-based)\t" +
                "SIFT_score\t" +
                "Polyphen2_HVAR_score\t" +
                "MutationTaster_score\t" +
                "MutationTaster_pred\t" +
                "REVEL_score\t" +
                "M-CAP_score\t" +
                "MPC_score\t" +
                "MVP_score\t" +
                "PrimateAI_score\t" +
                "AlphaMissense_score\t" +
                "EVE_score\t"
                ;


        private String chr = "0";
        private String pos = "0";

        private String chr19 = "0";
        private String pos19 = "0";

        private String rs = ".";
        private String ref = "";
        private String alt = "";

        private String sift = ".";
        private String polyPhen2Hvar = ".";
        private String mTasterScore = ".";
        private String mTasterPred = ".";
        private String revelScore = ".";

        private String mCapScore = ".";
        private String mpcScore = ".";
        private String mvpScore = ".";
        private String primateAiScore = ".";
        private String alphaMissenseScore = ".";
        private String eveScore = ".";

        public LineBuilder chr(String chr) {
            this.chr = chr;
            return this;
        }

        public LineBuilder pos(String pos) {
            this.pos = pos;
            return this;
        }

        public LineBuilder chr19(String chr19) {
            this.chr19 = chr19;
            return this;
        }

        public LineBuilder pos19(String pos19) {
            this.pos19 = pos19;
            return this;
        }

        public LineBuilder rs(String rs) {
            this.rs = rs;
            return this;
        }

        public LineBuilder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public LineBuilder alt(String alt) {
            this.alt = alt;
            return this;
        }

        public LineBuilder sift(String sift) {
            this.sift = sift;
            return this;
        }

        public LineBuilder polyPhen2Hvar(String polyPhen2Hvar) {
            this.polyPhen2Hvar = polyPhen2Hvar;
            return this;
        }

        public LineBuilder mTasterScore(String mTasterScore) {
            this.mTasterScore = mTasterScore;
            return this;
        }

        public LineBuilder mTasterPred(String mTasterPred) {
            this.mTasterPred = mTasterPred;
            return this;
        }

        public LineBuilder revelScore(String revelScore) {
            this.revelScore = revelScore;
            return this;
        }

        public LineBuilder mCapScore(String mCapScore) {
            this.mCapScore = mCapScore;
            return this;
        }

        public LineBuilder mpcScore(String mpcScore) {
            this.mpcScore = mpcScore;
            return this;
        }

        public LineBuilder mvpScore(String mvpScore) {
            this.mvpScore = mvpScore;
            return this;
        }

        public LineBuilder primateAiScore(String primateAiScore) {
            this.primateAiScore = primateAiScore;
            return this;
        }

        public LineBuilder alphaMissenseScore(String alphaMissenseScore) {
            this.alphaMissenseScore = alphaMissenseScore;
            return this;
        }

        public LineBuilder eveScore(String eveScore) {
            this.eveScore = eveScore;
            return this;
        }

        public String build() {
            StringJoiner stringJoiner = new StringJoiner("\t");
            // This order is critical for the tests to work properly.
            // If we add another field, it needs to match the order of the HEADER fields
            stringJoiner.add(chr);
            stringJoiner.add(pos);
            stringJoiner.add(ref);
            stringJoiner.add(alt);
            stringJoiner.add(rs);
            stringJoiner.add(chr19);
            stringJoiner.add(pos19);
            stringJoiner.add(sift);
            stringJoiner.add(polyPhen2Hvar);
            stringJoiner.add(mTasterScore);
            stringJoiner.add(mTasterPred);
            stringJoiner.add(revelScore);
            stringJoiner.add(mCapScore);
            stringJoiner.add(mpcScore);
            stringJoiner.add(mvpScore);
            stringJoiner.add(primateAiScore);
            stringJoiner.add(alphaMissenseScore);
            stringJoiner.add(eveScore);
            return stringJoiner.toString();
        }

    }

    @Test
    void realFileDebugTest() {
        var resource = new DbNsfp4AlleleResource("hg19.dbnsfp", null, Path.of("/home/hhx640/Documents/GitHub/Exomiser/exomiser-data-genome/build/hg19/variants/dbNSFP4.5a.zip"), DbNsfpColumnIndex.HG19);
        resource.parseResource().limit(1).forEach(System.out::println);
    }
}