/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.charite.compbio.exomiser.parsers;

import de.charite.compbio.exomiser.core.VariantPathogenicity;
import de.charite.compbio.exomiser.resources.ResourceOperationStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jj8
 */
public class NSFP2SQLDumpParserTest {
    
    public NSFP2SQLDumpParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getTotalNsfpLines method, of class NSFP2SQLDumpParser.
     */
    @Test
    public void testGetTotalNsfpLines() {
        System.out.println("getTotalNsfpLines");
        NSFP2SQLDumpParser instance = new NSFP2SQLDumpParser();
        int expResult = 0;
        int result = instance.getTotalNsfpLines();
        assertEquals(expResult, result);
    }

    /**
     * Test of getVariantCount method, of class NSFP2SQLDumpParser.
     */
    @Test
    public void testGetVariantCount() {
        System.out.println("getVariantCount");
        NSFP2SQLDumpParser instance = new NSFP2SQLDumpParser();
        int expResult = 0;
        int result = instance.getVariantCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getGeneCount method, of class NSFP2SQLDumpParser.
     */
    @Test
    public void testGetGeneCount() {
        System.out.println("getGeneCount");
        NSFP2SQLDumpParser instance = new NSFP2SQLDumpParser();
        int expResult = 0;
        int result = instance.getGeneCount();
        assertEquals(expResult, result);

    }

    /**
     * Test of parse method, of class NSFP2SQLDumpParser.
     */
    @Test
    public void testParse() {
        System.out.println("parse");
        String inPath = "src/test/resources/data/dbNSFP2.3_test.zip";
        String outPath = "target/test-data/testVariant.pg";
        NSFP2SQLDumpParser instance = new NSFP2SQLDumpParser();
        ResourceOperationStatus expResult = ResourceOperationStatus.SUCCESS;
        ResourceOperationStatus result = instance.parse(inPath, outPath);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of parseLine method, of class NSFP2SQLDumpParser.
     */
    @Test
    public void testParseLine() {
        System.out.println("parseLine");
        String line = 
        //#chr	pos(1-coor)	ref	alt	aaref	aaalt	hg18_pos(1-coor)	genename	Uniprot_acc	Uniprot_id	Uniprot_aapos	Interpro_domain	cds_strand	refcodon	SLR_test_statistic 	codonpos	fold-degenerate	Ancestral_allele	Ensembl_geneid	Ensembl_transcriptid	aapos	aapos_SIFT	aapos_FATHMM	SIFT_score	SIFT_score_converted	SIFT_pred	Polyphen2_HDIV_score	Polyphen2_HDIV_pred	Polyphen2_HVAR_score	Polyphen2_HVAR_pred	LRT_score	LRT_score_converted	LRT_pred	MutationTaster_score	MutationTaster_score_converted	MutationTaster_pred	MutationAssessor_score	MutationAssessor_score_converted	MutationAssessor_pred	FATHMM_score	FATHMM_score_converted	FATHMM_pred	RadialSVM_score	RadialSVM_score_converted	RadialSVM_pred	LR_score	LR_pred	Reliability_index	GERP++_NR	GERP++_RS	phyloP	29way_pi	29way_logOdds	LRT_Omega	UniSNP_ids	1000Gp1_AC	1000Gp1_AF	1000Gp1_AFR_AC	1000Gp1_AFR_AF	1000Gp1_EUR_AC	1000Gp1_EUR_AF	1000Gp1_AMR_AC	1000Gp1_AMR_AF	1000Gp1_ASN_AC	1000Gp1_ASN_AF	ESP6500_AA_AF	ESP6500_EA_AF
        "Y	2655049	C	A	Q	H	2715049	SRY	.	.	.	.	-	CAG	.	3	2	.	ENSG00000184895	ENST00000525526	197	.	ENSP00000437575:Q197H	0.30	.	.	.	.	0.01	.	.	.	.	0.02	.	D	2.125	0.6657986111111112	M	-4.65	0.5712	D	0.6590	0.6084	D	0.8738	D	4	0.644	0.644	0.641000	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.";
        NSFP2SQLDumpParser instance = new NSFP2SQLDumpParser();
        VariantPathogenicity pathogenicity = instance.parseLine(line);
        System.out.println(pathogenicity);
        assertEquals(24, pathogenicity.getChromosome());
        assertEquals(2655049, pathogenicity.getPosition());
        assertEquals('C', pathogenicity.getRef());
        assertEquals('A', pathogenicity.getAlt());
        assertEquals('Q', pathogenicity.getAminoAcidRef());
        assertEquals('H', pathogenicity.getAminoAcidAlt());
        assertEquals(197, pathogenicity.getAminoAcidPosition());
        assertEquals(0.641, pathogenicity.getPhylopScore(), 0.001);
        //actual scores for the pathogenicity:
        assertEquals(0.30, pathogenicity.getSiftScore(), 0.001);
        assertEquals(0.01, pathogenicity.getPolyphenScore(), 0.001);
        assertEquals(0.02, pathogenicity.getMutationTasterScore(), 0.001);
        //this is calculated internally by the VariantPathogenicity itself so may
        //break in the future if the internal rules change.
        assertEquals(0.7, pathogenicity.maxPathogenicity(), 0.001);
    }
    
}
