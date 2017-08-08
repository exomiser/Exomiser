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

import org.junit.Test;
import org.monarchinitiative.exomiser.db.resources.Resource;
import org.monarchinitiative.exomiser.db.resources.ResourceOperationStatus;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;


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
        testResource.setExtractedFileName("mim2gene.txt");
        testResource.setParsedFileName("testMim2Gene.out");

        Map<Integer, Set<Integer>> mim2geneMap = new HashMap<>();
        MimToGeneParser instance = new MimToGeneParser(mim2geneMap);
        instance.parseResource(testResource, Paths.get("src/test/resources/data"), Paths.get("target/test-data"));

        Map<Integer, Set<Integer>> expected = new HashMap<>();
        expected.put(123463, Collections.singleton(124));
        expected.put(123464, Collections.singleton(123));
        expected.put(123466, Collections.singleton(216));
        expected.put(123468, Collections.singleton(87));

        assertEquals(expected, mim2geneMap);
        assertEquals(ResourceOperationStatus.SUCCESS, testResource.getParseStatus());
    }
    
}
