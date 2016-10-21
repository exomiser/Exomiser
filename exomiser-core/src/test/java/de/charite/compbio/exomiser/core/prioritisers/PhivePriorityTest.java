/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.prioritisers;

import com.google.common.collect.Lists;
import de.charite.compbio.exomiser.core.model.Gene;
import de.charite.compbio.exomiser.core.model.GeneModel;
import de.charite.compbio.exomiser.core.model.ModelPhenotypeMatch;
import de.charite.compbio.exomiser.core.model.Organism;
import de.charite.compbio.exomiser.core.prioritisers.util.TestPriorityServiceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhivePriorityTest {

    private Logger logger = LoggerFactory.getLogger(PhivePriorityTest.class);

    //TODO: add these to the TestService as GeneIdentifiers
    private List<Gene> getGenes() {
        return Lists.newArrayList(
                new Gene("FGFR2", 2263),
                new Gene("ROR2", 4920),
                new Gene("FREM2", 341640),
                new Gene("ZNF738", 148203)
        );
    }

    //TODO: this should be the output of a Prioritiser: Genes + HPO -> PrioritiserResults
    private List<PhivePriorityResult> getPriorityResultsOrderedByScore(List<Gene> genes) {
        return genes.stream()
                .flatMap(gene -> gene.getPriorityResults().values().stream())
                .map(priorityResult -> (PhivePriorityResult) priorityResult)
                .sorted()
                .collect(Collectors.toList());
    }

    @Test
    public void testGetPriorityType() {
        PhivePriority instance = new PhivePriority(Collections.emptyList(), TestPriorityServiceFactory.STUB_SERVICE);
        assertThat(instance.getPriorityType(), equalTo(PriorityType.PHIVE_PRIORITY));
    }

    @Test
    public void testPrioritizeGenes() {
        List<Gene> genes = getGenes();

        List<String> hpoIds= Lists.newArrayList("HP:0010055", "HP:0001363", "HP:0001156", "HP:0011304");
        PhivePriority phivePriority = new PhivePriority(hpoIds, TestPriorityServiceFactory.TEST_SERVICE);
        phivePriority.prioritizeGenes(genes);
//        List<PriorityResult> results = instance.prioritise(hpoIds, geneIdentifiers);

        List<PhivePriorityResult> results = getPriorityResultsOrderedByScore(genes);
        assertThat(results.size(), equalTo(genes.size()));

        results.forEach(System.out::println);

        GeneModel topFgfr2Model = new GeneModel("MGI:95523_25785", Organism.MOUSE, 2263, "FGFR2", "MGI:95523", "Fgfr2", Arrays.asList("MP:0000081", "MP:0000157", "MP:0000435", "MP:0000566", "MP:0001219", "MP:0001222", "MP:0001231", "MP:0001240", "MP:0001725", "MP:0001732", "MP:0001874", "MP:0002060", "MP:0003743", "MP:0009545", "MP:0009601", "MP:0009611", "MP:0011085", "MP:0011495"));
        GeneModel topRor2Model = new GeneModel("MGI:1347521_1478", Organism.MOUSE, 4920, "ROR2", "MGI:1347521", "Ror2", Arrays.asList("MP:0000081", "MP:0000157", "MP:0000435", "MP:0000566", "MP:0001219", "MP:0001222", "MP:0001231", "MP:0001240", "MP:0001725", "MP:0001732", "MP:0001874", "MP:0002060", "MP:0003743", "MP:0009545", "MP:0009601", "MP:0009611", "MP:0011085", "MP:0011495"));
        GeneModel topFrem2Model = new GeneModel("MGI:2444465_18183", Organism.MOUSE, 341640, "FREM2", "MGI:2444465", "Frem2", Arrays.asList("MP:0000081", "MP:0000157", "MP:0000435", "MP:0000566", "MP:0001219", "MP:0001222", "MP:0001231", "MP:0001240", "MP:0001725", "MP:0001732", "MP:0001874", "MP:0002060", "MP:0003743", "MP:0009545", "MP:0009601", "MP:0009611", "MP:0011085", "MP:0011495"));

        PriorityResult fgfr2Result = new PhivePriorityResult(2263, "FGFR2", 0.8278620340423056, new ModelPhenotypeMatch(0.8278620340423056, topFgfr2Model, Collections.emptyList()));//);
        PriorityResult ror2Result = new PhivePriorityResult(4920, "ROR2", 0.6999088391144015, new ModelPhenotypeMatch(0.6999088391144015, topRor2Model, Collections.emptyList()));
        PriorityResult frem2Result = new PhivePriorityResult(341640, "FREM2", 0.6208762175615226, new ModelPhenotypeMatch(0.6208762175615226, topFrem2Model, Collections.emptyList()));
        PriorityResult znf738Result = new PhivePriorityResult(148203, "ZNF738", 0.6000000238418579, null);

//      Scores from flatfile (these will have suffered slight rounding errors compared to the database)
        Map<String, Double> expectedScores = expectedMouseScores();

        Map<String, Double> actualScores = results.stream().collect(toMap(PhivePriorityResult::getGeneSymbol, PhivePriorityResult::getScore));

        assertThat(actualScores, equalTo(expectedScores));
    }

    @Test
    public void testPrioritise() {
        List<Gene> genes = getGenes();

        List<String> hpoIds= Lists.newArrayList("HP:0010055", "HP:0001363", "HP:0001156", "HP:0011304");
        PhivePriority phivePriority = new PhivePriority(hpoIds, TestPriorityServiceFactory.TEST_SERVICE);

        List<PhivePriorityResult> results = phivePriority.prioritise(genes)
                .sorted(Comparator.naturalOrder())
                .collect(toList());

        assertThat(results.size(), equalTo(genes.size()));

        results.forEach(System.out::println);

        Map<String, Double> expectedScores = expectedMouseScores();

        Map<String, Double> actualScores = results.stream().collect(toMap(PhivePriorityResult::getGeneSymbol, PhivePriorityResult::getScore));

        assertThat(actualScores, equalTo(expectedScores));
    }

    private Map<String, Double> expectedMouseScores() {
        Map<String, Double> expectedScores = new HashMap<>();
        expectedScores.put("FGFR2", 0.8278620340423056);
        expectedScores.put("ROR2", 0.6999088391144015);
        expectedScores.put("FREM2", 0.6208762175615226);
        expectedScores.put("ZNF738", 0.6000000238418579);
        return expectedScores;
    }

}