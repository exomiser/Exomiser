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

import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode;
import org.monarchinitiative.exomiser.data.phenotype.processors.Resource;

import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.monarchinitiative.exomiser.core.prioritisers.model.InheritanceMode.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class Product9InheritanceXmlReaderTest {

    @Test
    void getDisease2inheritanceMultimap() {

        Resource product9Resource = Resource.builder()
                .fileDirectory(Paths.get("src/test/resources/data/"))
                .fileName("en_product9_ages.xml")
                .build();
        Product9InheritanceXmlReader instance = new Product9InheritanceXmlReader(product9Resource);

        Multimap<String, InheritanceMode> disease2inheritanceMultimap = instance.read();
        assertThat(disease2inheritanceMultimap.get("ORPHA:166024"), equalTo(List.of(AUTOSOMAL_RECESSIVE)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:58"), equalTo(List.of(AUTOSOMAL_DOMINANT)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:166084"), equalTo(List.of(AUTOSOMAL_DOMINANT, AUTOSOMAL_RECESSIVE)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:535"), equalTo(List.of(POLYGENIC)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:166119"), equalTo(List.of(AUTOSOMAL_DOMINANT)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:487"), equalTo(List.of(AUTOSOMAL_RECESSIVE)));

        assertThat(disease2inheritanceMultimap.get("ORPHA:461"), equalTo(List.of(X_RECESSIVE)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:586"), equalTo(List.of(AUTOSOMAL_RECESSIVE)));
        // Emery-Dreifuss muscular dystrophy (ORPHA:261) is a parent of three sub-types each with a specific MOI
        assertThat(disease2inheritanceMultimap.get("ORPHA:261"), equalTo(List.of(AUTOSOMAL_DOMINANT, AUTOSOMAL_RECESSIVE, X_RECESSIVE)));
        // Emery-Dreifuss subtypes:
        assertThat(disease2inheritanceMultimap.get("ORPHA:98853"), equalTo(List.of(AUTOSOMAL_DOMINANT)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:98855"), equalTo(List.of(AUTOSOMAL_RECESSIVE)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:98863"), equalTo(List.of(X_RECESSIVE)));

        assertThat(disease2inheritanceMultimap.get("ORPHA:550"), equalTo(List.of(MITOCHONDRIAL)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:480"), equalTo(List.of(AUTOSOMAL_RECESSIVE, MITOCHONDRIAL)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:163937"), equalTo(List.of(X_DOMINANT)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:163966"), equalTo(List.of(X_DOMINANT)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:1941"), equalTo(List.of(POLYGENIC)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:99771"), equalTo(List.of(POLYGENIC)));
        assertThat(disease2inheritanceMultimap.get("ORPHA:99792"), equalTo(List.of()));
        assertFalse(disease2inheritanceMultimap.containsKey("ORPHA:99792"));
    }
}