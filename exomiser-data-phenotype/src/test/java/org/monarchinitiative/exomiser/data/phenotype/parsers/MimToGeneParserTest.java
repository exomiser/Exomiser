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
import org.monarchinitiative.exomiser.data.phenotype.resources.Resource;
import org.monarchinitiative.exomiser.data.phenotype.resources.ResourceOperationStatus;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Test of the <code>MimToGeneParser</code>.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class MimToGeneParserTest {

    /**
     * Test of parseResource method, of class MimToGeneParser.
     */
    @Test
    public void testParse() {
        Resource testResource = new Resource("MIM2GENE");
        testResource.setExtractedFileName("mim2gene_test.txt");
        testResource.setParsedFileName("testMim2Gene.out");

        Map<Integer, Integer> mim2geneMap = new HashMap<>();
        MimToGeneParser instance = new MimToGeneParser(mim2geneMap);
        instance.parseResource(testResource, Paths.get("src/test/resources/data"), Paths.get("target/test-data"));

        Map<Integer, Integer> expected = new HashMap<>();
        expected.put(123463, 124);
        expected.put(123464, 123);
        expected.put(123466, 216);
        expected.put(123468, 87);

        assertThat(mim2geneMap, equalTo(expected));
        assertThat(testResource.getParseStatus(), equalTo(ResourceOperationStatus.SUCCESS));
    }

}
