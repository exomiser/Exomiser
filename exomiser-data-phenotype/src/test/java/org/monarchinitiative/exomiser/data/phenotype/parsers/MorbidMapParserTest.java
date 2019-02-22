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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.monarchinitiative.exomiser.data.phenotype.parsers;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease.DiseaseType;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.data.phenotype.parsers.MorbidMapParser.OmimDisease;

/**
 * @author jj8
 */
@ExtendWith(TempDirectory.class)
public class MorbidMapParserTest {

    private final Path tempDir;
    private final Path testResourceDir = Paths.get("src/test/resources/data");

    private final MorbidMapParser instance;
    private final Resource morbidMapResource = new Resource("OMIM_morbidmap");

    public MorbidMapParserTest(@TempDir Path tempDir) {
        this.tempDir = tempDir;

        morbidMapResource.setExtractedFileName("morbidmap.txt");
        morbidMapResource.setParsedFileName("testMorbidMap.txt");

        Resource diseaseInheritanceResource = new Resource("HPO_phenotype_annotation_test");
        diseaseInheritanceResource.setExtractedFileName("phenotype_annotation.tab");

        DiseaseInheritanceCache cache = new DiseaseInheritanceCache();
        cache.parseResource(diseaseInheritanceResource, testResourceDir, tempDir);

        Resource mim2geneResource = new Resource("mim2gene");
        mim2geneResource.setExtractedFileName("mim2gene.txt");
        mim2geneResource.setParsedFileName("testMim2Gene.tsv");

        Map<Integer, Integer> mim2geneMap = new HashMap<>();
        MimToGeneParser mimToGeneParser = new MimToGeneParser(mim2geneMap);
        mimToGeneParser.parseResource(mim2geneResource, testResourceDir, tempDir);

        instance = new MorbidMapParser(cache, mim2geneMap);
    }


    /**
     * Test of parseResource method, of class MorbidMapParser.
     */
    @Test
    public void testParse() {
        System.out.println("parse");

        instance.parseResource(morbidMapResource, testResourceDir, tempDir);
        assertThat(morbidMapResource.getParseStatus(), equalTo(ResourceOperationStatus.SUCCESS));

        Path parsedFile = tempDir.resolve(morbidMapResource.getParsedFileName());
        assertThat(parsedFile.toFile().exists(), is(true));
        //Extracted 6122 OMIM terms from morbidmap
    }

    @Test
    void parseDiseaseWithKnownInheritance() {
        // Pfeiffer syndrome, 101600 (3)	FGFR1, FLT2, OGD, KAL2, HH2, HRTFDS, ECCL	136350	8p11.23
        // Pfeiffer syndrome, 101600 (3)	FGFR2, BEK, CFD1, JWS, TK14, BBDS	176943	10q26.13
        // 101600-176943-2263 is not unique! Skipping...
        OmimDisease output = instance.parseLine("Pfeiffer syndrome, 101600 (3)\tFGFR1, FLT2, OGD, KAL2, HH2, HRTFDS, ECCL\t136350\t8p11.23");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(101600, 136350, "Pfeiffer syndrome",2260, DiseaseType.DISEASE, InheritanceMode.AUTOSOMAL_DOMINANT);
        assertThat(output, equalTo(expected));
    }

    @Test
    void parseDiseaseNoDiseaseId() {
        OmimDisease output = instance.parseLine("Leukemia, acute T-cell (2)\tLMO2, RBTNL1, RHOM2, TTG2\t180385\t11p13");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(-10, 180385, "Leukemia, acute T-cell",4005, DiseaseType.DISEASE, InheritanceMode.UNKNOWN);
        assertThat(output, equalTo(expected));
    }

    @Test
    void parseDiseaseNotRealDiseaseId() {
        OmimDisease output = instance.parseLine("Myoclonic epilepsy, juvenile, 4 (2)\tEJM4\t611364\t5q12-q14");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(-10, 611364, "Myoclonic epilepsy, juvenile, 4",0, DiseaseType.DISEASE, InheritanceMode.UNKNOWN);
        assertThat(output, equalTo(expected));
    }

    @Test
    void parseUnconfirmedDiseaseWithKnownInheritance() {
        OmimDisease output = instance.parseLine("?Mental retardation, X-linked 100, 300923 (3)\tKIF4A, KIF4, MRX100\t300521\tXq13.1");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(300923, 300521, "?Mental retardation, X-linked 100",24137, DiseaseType.UNCONFIRMED, InheritanceMode.X_RECESSIVE);
        assertThat(output, equalTo(expected));
    }

    @Test
    void parseSusceptibilityWithDiseaseId() {
        OmimDisease output = instance.parseLine("{Bone mineral density QTL 12, osteoporosis}, 612560 (3)\tUGT2B17, BMND12\t601903\t4q13.2");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(612560, 601903, "Bone mineral density QTL 12, osteoporosis",7367, DiseaseType.SUSCEPTIBILITY, InheritanceMode.UNKNOWN);
        assertThat(output, equalTo(expected));
    }

    @Test
    void parseUnconfirmedSusceptibilityWithDiseaseId() {
        OmimDisease output = instance.parseLine("{?Breast cancer susceptibility}, 114480 (1)\tNQO2, NMOR2\t160998\t6p25.2");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(114480, 160998, "?Breast cancer susceptibility",4835, DiseaseType.SUSCEPTIBILITY, InheritanceMode.AUTOSOMAL_DOMINANT);
        assertThat(output, equalTo(expected));
    }

    @Test
    void parseNonDiseaseWithDiseaseId() {
        OmimDisease output = instance.parseLine("[Blood group, Scianna system], 111750 (3)\tERMAP, SC, RD\t609017\t1p34.2");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(111750, 609017, "Blood group, Scianna system",114625, DiseaseType.NON_DISEASE, InheritanceMode.UNKNOWN);
        assertThat(output, equalTo(expected));
    }

    @Test
    void parseUnconfirmedNonDiseaseWithDiseaseId() {
        OmimDisease output = instance.parseLine("[?Phosphohydroxylysinuria], 615011 (3)\tPHYKPL, AGXT2L2, PHLU\t614683\t5q35.3");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(615011, 614683, "?Phosphohydroxylysinuria",85007, DiseaseType.NON_DISEASE, InheritanceMode.UNKNOWN);
        assertThat(output, equalTo(expected));
    }

    @Test
    void parseNonDiseaseNoDiseaseId() {
        OmimDisease output = instance.parseLine("[Blood group, Rhesus] (3)\tRHCE, RHNA\t111700\t1p36.11");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(-10, 111700, "Blood group, Rhesus",6006, DiseaseType.NON_DISEASE, InheritanceMode.UNKNOWN);
        assertThat(output, equalTo(expected));
    }

    @Test
    void parseCnvWithDiseaseId() {
        OmimDisease output = instance.parseLine("Silver-Russell syndrome (4)\tSRS, RSS\t180860\t7p11.2");
        System.out.println(output);
        OmimDisease expected = new OmimDisease(-10, 180860, "Silver-Russell syndrome",0, DiseaseType.CNV, InheritanceMode.UNKNOWN);
        assertThat(output, equalTo(expected));
    }
}
