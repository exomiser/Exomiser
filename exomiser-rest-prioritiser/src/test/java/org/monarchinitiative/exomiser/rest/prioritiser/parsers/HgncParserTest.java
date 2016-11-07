package org.monarchinitiative.exomiser.rest.prioritiser.parsers;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.HumanGeneIdentifier;

import java.nio.file.Paths;
import java.util.List;

import static java.util.stream.Collectors.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class HgncParserTest {

    @Test
    public void testParseGeneIdentifiers() {
        HgncParser hgncParser = new HgncParser(Paths.get("src/test/resources/data/hgnc_complete_set.txt"));
        List<HumanGeneIdentifier> humanGeneIdentifiers = hgncParser.parseGeneIdentifiers().collect(toList());
        assertThat(humanGeneIdentifiers.size(), equalTo(41054));

        HumanGeneIdentifier fgfr2Identifier = humanGeneIdentifiers.stream()
                .filter(geneIdentifier -> geneIdentifier.getGeneSymbol().equals("FGFR2"))
                .findFirst()
                .get();
        System.out.println(fgfr2Identifier);

        HumanGeneIdentifier expectedFgfr2Identifier = HumanGeneIdentifier.builder()
                .hgncId("HGNC:3689")
                .geneSymbol("FGFR2")
                .geneName("fibroblast growth factor receptor 2")
                .location("10q26.13")
                .ensemblId("ENSG00000066468")
                .entrezId("2263")
                .ucscId("uc057wle.1")
                .build();

        assertThat(fgfr2Identifier, equalTo(expectedFgfr2Identifier));

        HumanGeneIdentifier withdrawnIdentifier = humanGeneIdentifiers.stream()
                .filter(geneIdentifier -> geneIdentifier.getHgncId().equals("HGNC:1"))
                .findFirst()
                .get();
        System.out.println(withdrawnIdentifier);

        HumanGeneIdentifier expectedWithdrawnIdentifier = HumanGeneIdentifier.builder()
                .hgncId("HGNC:1")
                .geneSymbol("A12M1")
                .withdrawn(true)
                .build();

        assertThat(withdrawnIdentifier.isWithdrawn(), is(true));
        assertThat(withdrawnIdentifier, equalTo(expectedWithdrawnIdentifier));

        System.out.println("getLocusGroups:");
        humanGeneIdentifiers.stream()
                .collect(groupingBy(HumanGeneIdentifier::getLocusGroup, counting()))
                .entrySet()
                .forEach(System.out::println);

        System.out.println();
//        humanGeneIdentifiers.stream()
//                .filter(geneIdentifier -> geneIdentifier.getLocusGroup().isEmpty())
//                .forEach(System.out::println);
//        System.out.println();

        System.out.println("getLocusTypes:");
        humanGeneIdentifiers.stream().map(HumanGeneIdentifier::getLocusType).distinct().forEach(System.out::println);

        humanGeneIdentifiers.stream()
                .filter(geneIdentifier -> geneIdentifier.getLocusGroup().equals("phenotype"))
                .forEach(System.out::println);
        System.out.println();

    }


}