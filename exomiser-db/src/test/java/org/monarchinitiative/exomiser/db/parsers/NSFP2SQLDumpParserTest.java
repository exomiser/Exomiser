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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.db.parsers;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class NSFP2SQLDumpParserTest {
    
    private NSFP2SQLDumpParser instance;
    
    @Before
    public void setUp() {
        instance = new NSFP2SQLDumpParser();
    }
    /**
     * Test of getTotalNsfpLines method, of class NSFP2SQLDumpParser.
     */
    @Test
    public void testGetTotalNsfpLines() {
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
        int expResult = 0;
        int result = instance.getGeneCount();
        assertEquals(expResult, result);

    }
    
    @Test
    public void parseDbNsfpFloatForNoValueReturnsNull() {
        assertThat(instance.valueOfField("."), equalTo(null));
    }
    
    @Test
    public void parseDbNsfpFloatForSingleValue() {
        assertThat(instance.valueOfField("1"), equalTo(1f));
    }
    
    @Test
    public void parseDbNsfpFloatForSingleNonNumericValueReturnsNull() {
        assertThat(instance.valueOfField("wibble"), equalTo(null));
    }

    /**
     * Test of parseResource method, of class NSFP2SQLDumpParser.
     */
//    @Test
//    public void testParse() {
//        System.out.println("parse");
//        
//        Path testResourceDir = Paths.get("src/test/resources/data");
//        Path testOutDir = Paths.get("target/test-data");
//        
//        Resource testResource = new Resource("dbNSFP2.3");
//        testResource.setExtractedFileName("dbNSFP2.3_test.zip");
//        testResource.setParsedFileName("testVariant.pg");
//        
//        ResourceOperationStatus expResult = ResourceOperationStatus.SUCCESS;
//        NSFP2SQLDumpParser instance = new NSFP2SQLDumpParser();
//        instance.parseResource(testResource, testResourceDir, testOutDir);
//        assertEquals(expResult, testResource.getParseStatus());
//    }
    
    /**
     * Test of parseLine method, of class NSFP2SQLDumpParser.
     */
//    @Test
//    public void testParseLine() {
//        String line = 
//        //#chr	pos(1-coor)	ref	alt	aaref	aaalt	hg18_pos(1-coor)	genename	Uniprot_acc	Uniprot_id	Uniprot_aapos	Interpro_domain	cds_strand	refcodon	SLR_test_statistic 	codonpos	fold-degenerate	Ancestral_allele	Ensembl_geneid	Ensembl_transcriptid	aapos	aapos_SIFT	aapos_FATHMM	SIFT_score	SIFT_score_converted	SIFT_pred	Polyphen2_HDIV_score	Polyphen2_HDIV_pred	Polyphen2_HVAR_score	Polyphen2_HVAR_pred	LRT_score	LRT_score_converted	LRT_pred	MutationTaster_score	MutationTaster_score_converted	MutationTaster_pred	MutationAssessor_score	MutationAssessor_score_converted	MutationAssessor_pred	FATHMM_score	FATHMM_score_converted	FATHMM_pred	RadialSVM_score	RadialSVM_score_converted	RadialSVM_pred	LR_score	LR_pred	Reliability_index	GERP++_NR	GERP++_RS	phyloP	29way_pi	29way_logOdds	LRT_Omega	UniSNP_ids	1000Gp1_AC	1000Gp1_AF	1000Gp1_AFR_AC	1000Gp1_AFR_AF	1000Gp1_EUR_AC	1000Gp1_EUR_AF	1000Gp1_AMR_AC	1000Gp1_AMR_AF	1000Gp1_ASN_AC	1000Gp1_ASN_AF	ESP6500_AA_AF	ESP6500_EA_AF
//        "Y	2655049	C	A	Q	H	2715049	SRY	.	.	.	.	-	CAG	.	3	2	.	ENSG00000184895	ENST00000525526	197	.	ENSP00000437575:Q197H	0.30	.	.	.	.	0.01	.	.	.	.	0.02	.	D	2.125	0.6657986111111112	M	-4.65	0.5712	D	0.6590	0.6084	D	0.8738	D	4	0.644	0.644	0.641000	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.      .	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.	.";
//        NSFP2SQLDumpParser instance = new NSFP2SQLDumpParser();
//        VariantPathogenicity pathogenicity = instance.parseLine(line);
//        System.out.println(pathogenicity);
//        assertEquals(24, pathogenicity.getChromosome());
//        assertEquals(2655049, pathogenicity.getPosition());
//        assertEquals('C', pathogenicity.getRef());
//        assertEquals('A', pathogenicity.getAlt());
//        assertEquals('Q', pathogenicity.getAminoAcidRef());
//        assertEquals('H', pathogenicity.getAminoAcidAlt());
//        assertEquals(197, pathogenicity.getAminoAcidPosition());
//        assertEquals(0.641, pathogenicity.getPhylopScore(), 0.001);
//        //actual scores for the pathogenicity:
//        assertEquals(0.30, pathogenicity.getSiftScore(), 0.001);
//        assertEquals(0.01, pathogenicity.getPolyphenScore(), 0.001);
//        assertEquals(0.02, pathogenicity.getMutationTasterScore(), 0.001);
//        //this is calculated internally by the VariantPathogenicity itself so may
//        //break in the future if the internal rules change.
//        assertEquals(0.7, pathogenicity.maxPathogenicity(), 0.001);
//    }
    
}
