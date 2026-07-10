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

package org.monarchinitiative.exomiser.data.phenotype.processors.readers.ontology;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.monarchinitiative.exomiser.data.phenotype.processors.model.ontology.OboOntologyTerm;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
class OboOntologyParserTest {

    private static final OboOntology OBO_ONTOLOGY = OboOntologyParser.parseOboFile(Path.of("src/test/resources/data/hp.obo"));

    @Test
    void throwsErrorForNonOboFile() {
        assertThrows(
                RuntimeException.class,
                () -> OboOntologyParser.parseOboFile(Path.of("src/test/resources/data/not_obo.obo")),
                "Not an OBO format file."
        );
    }

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
                .altIds(List.of("HP:0001453", "HP:0001461"))
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
                .altIds(List.of("HP:0004158", "HP:0004164", "HP:0004165"))
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

    // test for issue #649
    @Test
    void testReplacedByConsider(@TempDir Path temp) {
        String hpo = """
                format-version: 1.2
                data-version: hp/releases/2026-06-23
                subsetdef: hposlim_core "Core clinical terminology"
                subsetdef: secondary_consequence "Consequence of a disorder in another organ system."
                synonymtypedef: abbreviation "abbreviation"
                synonymtypedef: allelic_requirement "allelic_requirement"
                synonymtypedef: layperson "layperson term"
                synonymtypedef: obsolete_synonym "discarded/obsoleted synonym"
                synonymtypedef: plural_form "plural form"
                synonymtypedef: uk_spelling "UK spelling"
                default-namespace: human_phenotype
                idspace: dc http://purl.org/dc/elements/1.1/\s
                idspace: oboInOwl http://www.geneontology.org/formats/oboInOwl#\s
                idspace: terms http://purl.org/dc/terms/\s
                remark: Please see license of HPO at http://www.human-phenotype-ontology.org
                ontology: hp.obo
                
                [term]
                id: HP:0000535
                name: obsolete Sparse and thin eyebrow
                is_obsolete: true
                consider: HP:0045074
                consider: HP:0045075
                
                [Term]
                id: HP:0045074
                name: Thin eyebrow
                def: "Decreased diameter of eyebrow hairs." []
                synonym: "Thin eyebrow" EXACT layperson []
                synonym: "Thin eyebrows" EXACT layperson []
                is_a: HP:0100840 ! Aplasia/Hypoplasia of the eyebrow
                property_value: terms:creator https://orcid.org/0000-0002-5316-1399
                property_value: terms:date "2016-07-28T11:49:07Z" xsd:dateTime
                
                [Term]
                id: HP:0045075
                name: Sparse eyebrow
                alt_id: HP:0002222
                alt_id: HP:0002554
                alt_id: HP:0004520
                alt_id: HP:0004551
                def: "Decreased density/number of eyebrow hairs." [https://orcid.org/0000-0002-5316-1399, PMID:19125427]
                comment: Sparseness can be regional (medial, central, lateral) or total.
                subset: hposlim_core
                synonym: "Hypotrichosis of eyebrow" EXACT [https://orcid.org/0000-0002-5316-1399]
                synonym: "Sparse eyebrow" EXACT layperson []
                synonym: "Sparse eyebrows" EXACT layperson []
                is_a: HP:0100840 ! Aplasia/Hypoplasia of the eyebrow
                property_value: terms:date "2016-07-28T11:49:07Z" xsd:dateTime
                
                """;

        Path oboFile = temp.resolve("hp.obo");
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(oboFile)) {
            bufferedWriter.write(hpo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        OboOntologyTerm sparseAndThinEyebrow = OboOntologyTerm.builder()
                .id("HP:0000535")
                .label("obsolete Sparse and thin eyebrow")
                .obsolete(true)
                .addConsider("HP:0045074")
                .addConsider("HP:0045075")
                .build();

        OboOntologyTerm thinEyebrow = OboOntologyTerm.builder()
                .id("HP:0045074")
                .label("Thin eyebrow")
                .build();

        OboOntologyTerm sparseEyebrow = OboOntologyTerm.builder()
                .id("HP:0045075")
                .label("Sparse eyebrow")
                .altIds(List.of("HP:0002222", "HP:0002554", "HP:0004520", "HP:0004551"))
                .build();

        OboOntology ontology = OboOntologyParser.parseOboFile(oboFile);
        assertThat(ontology.getDataVersion(), equalTo("hp/releases/2026-06-23"));
        assertThat(ontology.getCurrentOntologyTerms(), equalTo(List.of(thinEyebrow, sparseEyebrow)));
        assertThat(ontology.getObsoleteOntologyTerms(), equalTo(List.of(sparseAndThinEyebrow)));
        assertThat(ontology.getIdToTerms().get("HP:0000535"), equalTo(thinEyebrow)); // the first 'consider' term
    }
}