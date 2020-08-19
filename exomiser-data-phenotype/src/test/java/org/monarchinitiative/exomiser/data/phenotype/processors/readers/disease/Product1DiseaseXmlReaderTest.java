/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2020 Queen Mary University of London.
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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.monarchinitiative.exomiser.data.phenotype.processors.readers.disease.OrphaOmimMapping.MappingType.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class Product1DiseaseXmlReaderTest {

    private ListMultimap<String, OrphaOmimMapping> readMappings(String filePath) {
        Path resourcePath = Paths.get(filePath);
        Resource resource = new Resource.Builder()
                .fileDirectory(resourcePath.getParent())
                .fileName(resourcePath.getFileName().toString())
                .build();
        Product1DiseaseXmlReader instance = new Product1DiseaseXmlReader(resource);
        return instance.read();
    }

    @Test
    void name() {
        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = readMappings("src/test/resources/data/en_product1_test.xml");

        ListMultimap<String, OrphaOmimMapping> expected = ArrayListMultimap.create();
        expected.put("ORPHA:166024", mappingTo("OMIM:607131", EXACT));
        expected.put("ORPHA:58", mappingTo("OMIM:203450", EXACT));

        expected.put("ORPHA:254892", mappingTo("OMIM:609283", BTNT));
        expected.put("ORPHA:254892", mappingTo("OMIM:609286", BTNT));
        expected.put("ORPHA:254892", mappingTo("OMIM:610131", BTNT));
        expected.put("ORPHA:254892", mappingTo("OMIM:613077", BTNT));
        expected.put("ORPHA:254892", mappingTo("OMIM:157640", EXACT));

        expected.put("ORPHA:93260", mappingTo("OMIM:101600", NTBT));
        expected.put("ORPHA:93259", mappingTo("OMIM:101600", NTBT));
        expected.put("ORPHA:93258", mappingTo("OMIM:101600", NTBT));
        expected.put("ORPHA:710", mappingTo("OMIM:101600", EXACT));

        expected.put("ORPHA:314777", mappingTo("OMIM:102200", NTBT));
        expected.put("ORPHA:314777", mappingTo("OMIM:600634", BTNT));

        assertThat(orphaOmimMappings, equalTo(expected));
    }

    private OrphaOmimMapping mappingTo(String omimDisease, OrphaOmimMapping.MappingType type) {
        return new OrphaOmimMapping(omimDisease, type);
    }

    @Test
    void obsoleteEntriesNotReturned() {
        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = readMappings("src/test/resources/data/en_product1_test_obsolete.xml");
        assertTrue(orphaOmimMappings.isEmpty());
    }

    @Test
    void multipleMappings() {
        // ORPHA:314777
        ListMultimap<String, OrphaOmimMapping> orphaOmimMappings = readMappings("src/test/resources/data/test_multi_mappings_moi_product1.xml");

        ListMultimap<String, OrphaOmimMapping> expected = ArrayListMultimap.create();
        expected.put("ORPHA:55654", mappingTo("OMIM:618275", BTNT));
        expected.put("ORPHA:55654", mappingTo("OMIM:615059", BTNT));
        expected.put("ORPHA:55654", mappingTo("OMIM:615885", BTNT));
        expected.put("ORPHA:55654", mappingTo("OMIM:278150", BTNT));
        expected.put("ORPHA:55654", mappingTo("OMIM:604379", BTNT));
        expected.put("ORPHA:55654", mappingTo("OMIM:605389", EXACT));
        expected.put("ORPHA:55654", mappingTo("OMIM:607903", BTNT));
        expected.put("ORPHA:55654", mappingTo("OMIM:614237", BTNT));
        expected.put("ORPHA:55654", mappingTo("OMIM:614238", BTNT));

        assertThat(orphaOmimMappings, equalTo(expected));
    }
}