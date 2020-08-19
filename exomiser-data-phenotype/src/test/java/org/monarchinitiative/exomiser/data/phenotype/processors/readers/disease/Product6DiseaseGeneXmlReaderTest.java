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

import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.disease.DiseaseGene;

import java.nio.file.Path;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class Product6DiseaseGeneXmlReaderTest {

    private final Path inDir = Path.of("src/test/resources/data");

    private Resource buildResource(String fileName) {
        return Resource.of(inDir, fileName);
    }

    @Test
    void name() {
        Resource omimMim2geneResource = buildResource("test_multi_mappings_moi_mim2gene.txt");
        OmimMimToGeneReader mimToGeneReader = new OmimMimToGeneReader(omimMim2geneResource);

        Resource product6XmlResource = buildResource("en_product6_test.xml");

        Product6DiseaseGeneXmlReader instance = new Product6DiseaseGeneXmlReader(mimToGeneReader, product6XmlResource);
        ListMultimap<String, DiseaseGene> allDiseases = instance.read();
        allDiseases.forEach((diseaseId, gene) -> {
              System.out.println(diseaseId + " - " + gene);
        });
    }

    @Test
    void multipleMappings() {

        Resource omimMim2geneResource = buildResource("test_multi_mappings_moi_mim2gene.txt");
        OmimMimToGeneReader mimToGeneReader = new OmimMimToGeneReader(omimMim2geneResource);

        Resource product6XmlResource = buildResource("test_multi_mappings_moi_product6.xml");

        Product6DiseaseGeneXmlReader instance = new Product6DiseaseGeneXmlReader(mimToGeneReader, product6XmlResource);

        ListMultimap<String, DiseaseGene> allDiseases = instance.read();
        allDiseases.forEach((diseaseId, gene) -> {
            System.out.println(diseaseId + " - " + gene);
        });
    }

}