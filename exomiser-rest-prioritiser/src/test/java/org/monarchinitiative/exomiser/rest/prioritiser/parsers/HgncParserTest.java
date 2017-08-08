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

package org.monarchinitiative.exomiser.rest.prioritiser.parsers;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.GeneIdentifier;

import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HgncParserTest {

    @Test
    public void testParseGeneIdentifiers() {
        HgncParser hgncParser = new HgncParser(Paths.get("src/test/resources/data/hgnc_complete_set.txt"));
        List<GeneIdentifier> geneIdentifiers = hgncParser.parseGeneIdentifiers().collect(toList());
        assertThat(geneIdentifiers.size(), equalTo(41054));

        GeneIdentifier fgfr2Identifier = geneIdentifiers.stream()
                .filter(geneIdentifier -> geneIdentifier.getGeneSymbol().equals("FGFR2"))
                .findFirst()
                .get();
        System.out.println(fgfr2Identifier);

        GeneIdentifier expectedFgfr2Identifier = GeneIdentifier.builder()
                .geneId("2263")
                .geneSymbol("FGFR2")
                .hgncId("HGNC:3689")
                .hgncSymbol("fibroblast growth factor receptor 2")
                .ensemblId("ENSG00000066468")
                .entrezId("2263")
                .ucscId("uc057wle.1")
                .build();

        assertThat(fgfr2Identifier, equalTo(expectedFgfr2Identifier));

        GeneIdentifier withdrawnIdentifier = geneIdentifiers.stream()
                .filter(geneIdentifier -> geneIdentifier.getHgncId().equals("HGNC:1"))
                .findFirst()
                .get();
        System.out.println(withdrawnIdentifier);

//        GeneIdentifier expectedWithdrawnIdentifier = GeneIdentifier.builder()
//                .hgncId("HGNC:1")
//                .geneSymbol("A12M1")
//                .withdrawn(true)
//                .build();
//
//        assertThat(withdrawnIdentifier.isWithdrawn(), is(true));
//        assertThat(withdrawnIdentifier, equalTo(expectedWithdrawnIdentifier));
//
//        System.out.println("getLocusGroups:");
//        geneIdentifiers.stream()
//                .collect(groupingBy(GeneIdentifier::getLocusGroup, counting()))
//                .entrySet()
//                .forEach(System.out::println);

        System.out.println();
//        geneIdentifiers.stream()
//                .filter(geneIdentifier -> geneIdentifier.getLocusGroup().isEmpty())
//                .forEach(System.out::println);
//        System.out.println();

//        System.out.println("getLocusTypes:");
//        geneIdentifiers.stream().map(GeneIdentifier::getLocusType).distinct().forEach(System.out::println);
//
//        geneIdentifiers.stream()
//                .filter(geneIdentifier -> geneIdentifier.getLocusGroup().equals("phenotype"))
//                .forEach(System.out::println);
//        System.out.println();

    }


}