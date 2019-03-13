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

package org.monarchinitiative.exomiser.data.phenotype.parsers;


import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OboOntologyParserTest {

    private static final OboOntology OBO_ONTOLOGY = OboOntologyParser.parseOboFile(Paths.get("src/test/resources/data/hp.obo"));

    @Test
    void parseOntologyDataVersion() {
        assertThat(OBO_ONTOLOGY.getDataVersion(), equalTo("2014-03-18"));
    }

    @Test
    void rootTerm() {
        List<OboOntologyTerm> currentOntologyTerms = OBO_ONTOLOGY.getCurrentOntologyTerms();
        OboOntologyTerm firstTerm = currentOntologyTerms.get(0);
        assertThat(firstTerm, equalTo(OboOntologyTerm.builder().id("HP:0000001").label("All").build()));
    }

    @Test
    void lastTerm() {
        List<OboOntologyTerm> currentOntologyTerms = OBO_ONTOLOGY.getCurrentOntologyTerms();
        OboOntologyTerm lastTerm = currentOntologyTerms.get(currentOntologyTerms.size() - 1);
        assertThat(lastTerm, equalTo(OboOntologyTerm.builder().id("HP:0200151").label("Cutaneous mastocytosis").build()));
    }

    @Test
    void singleAltId() {
        Map<String, OboOntologyTerm> termIds = OBO_ONTOLOGY.getIdToTerms();
        OboOntologyTerm currentTerm = OboOntologyTerm.builder()
                .id("HP:0000003")
                .label("Multicystic kidney dysplasia")
                .addAltId("HP:0004715")
                .build();
        currentTerm.getAltIds().forEach(altId -> assertThat(termIds.get(altId), equalTo(currentTerm)));
    }

    @Test
    void multipleAltIds() {
        Map<String, OboOntologyTerm> termIds = OBO_ONTOLOGY.getIdToTerms();
        OboOntologyTerm currentTerm = OboOntologyTerm.builder()
                .id("HP:0000005")
                .label("Mode of inheritance")
                .altIds(ImmutableList.of("HP:0001453", "HP:0001461"))
                .build();
        currentTerm.getAltIds().forEach(altId -> assertThat(termIds.get(altId), equalTo(currentTerm)));
    }

    @Test
    void obsoleteReplacedBy() {
        Map<String, OboOntologyTerm> termIds = OBO_ONTOLOGY.getIdToTerms();
        OboOntologyTerm obsoleteTerm = OboOntologyTerm.builder()
                .id("HP:0001113")
                .label("Early cataracts")
                .obsolete(true)
                .replacedBy("HP:0000518")
                .build();
        assertTrue(OBO_ONTOLOGY.getObsoleteOntologyTerms().contains(obsoleteTerm));

        OboOntologyTerm currentTerm = OboOntologyTerm.builder()
                .id("HP:0000518")
                .label("Cataract")
                .build();
        assertThat(termIds.get(obsoleteTerm.getId()), equalTo(currentTerm));
    }

    @Test
    void obsoleteReplacedByWithAltIds() {
        Map<String, OboOntologyTerm> termIds = OBO_ONTOLOGY.getIdToTerms();
        OboOntologyTerm obsoleteTerm = OboOntologyTerm.builder()
                .id("HP:0009449")
                .label("Hypoplastic/small phalanges of the 3rd finger")
                .obsolete(true)
                .altIds(ImmutableList.of("HP:0004158", "HP:0004164", "HP:0004165"))
                .replacedBy("HP:0009447")
                .build();
        assertTrue(OBO_ONTOLOGY.getObsoleteOntologyTerms().contains(obsoleteTerm));

        OboOntologyTerm currentTerm = OboOntologyTerm.builder()
                .id("HP:0009447")
                .label("Aplasia/Hypoplasia of the phalanges of the 3rd finger")
                .build();

        assertThat(termIds.get(obsoleteTerm.getId()), equalTo(currentTerm));
        obsoleteTerm.getAltIds().forEach(altId -> assertThat(termIds.get(altId), equalTo(currentTerm)));
    }
}