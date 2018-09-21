/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2018 Queen Mary University of London.
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
import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author jj8
 */
public class MorbidMapParserTest {

    /**
     * Test of parseResource method, of class MorbidMapParser.
     */
    @Test
    @ExtendWith(TempDirectory.class)
    public void testParse(@TempDir Path tempDir) {
        System.out.println("parse");
        Path testResourceDir = Paths.get("src/test/resources/data");

        Resource testResource = new Resource("OMIM_morbidmap");
        testResource.setExtractedFileName("morbidmap");
        String parsedFileName = "testMorbidMap.txt";
        testResource.setParsedFileName(parsedFileName);

        Resource diseaseInheritanceResource = new Resource("HPO_phenotype_annotation_test");
        diseaseInheritanceResource.setExtractedFileName("phenotype_annotation_test.tab");

        DiseaseInheritanceCache cache = new DiseaseInheritanceCache();
        cache.parseResource(diseaseInheritanceResource, testResourceDir, tempDir);
        Map<Integer, Set<Integer>> mim2geneMap = new HashMap<>();
        //todo: add some stub data to stop the test failing...
        Set<Integer> geneIds = new HashSet<>();
        geneIds.add(2263);
        geneIds.add(2260);
        mim2geneMap.put(176943, geneIds);
        MorbidMapParser instance = new MorbidMapParser(cache, mim2geneMap);
        instance.parseResource(testResource, testResourceDir, tempDir);
        assertThat(testResource.getParseStatus(), equalTo(ResourceOperationStatus.SUCCESS));

        Path parsedFile = tempDir.resolve(parsedFileName);
        assertThat(parsedFile.toFile().exists(), is(true));
    }

}
