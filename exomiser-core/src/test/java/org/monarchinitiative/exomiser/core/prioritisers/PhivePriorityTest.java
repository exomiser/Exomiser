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

package org.monarchinitiative.exomiser.core.prioritisers;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhivePriorityTest {

    //TODO: add these to the TestService as GeneIdentifiers
    private List<Gene> getGenes() {
        return Lists.newArrayList(
                new Gene("FGFR2", 2263),
                new Gene("ROR2", 4920),
                new Gene("FREM2", 341640),
                new Gene("ZNF738", 148203)
        );
    }

    private void checkScores(Map<String, Double> actualScores, Map<String, Double> expectedScores) {
        assertThat(actualScores.size(), equalTo(expectedScores.size()));
        expectedScores.forEach(
                (expectedGeneSymbol, expectedScore) ->
                        assertThat(expectedGeneSymbol + " score not as expected", actualScores.get(expectedGeneSymbol), equalTo(expectedScore)));
    }

    private Map<String, Double> expectedMouseScores() {
        Map<String, Double> expectedScores = new HashMap<>();
        expectedScores.put("FGFR2", 0.8278620340423056);
        expectedScores.put("ROR2", 0.6999088391144016);
        expectedScores.put("FREM2", 0.6208762175615226);
        expectedScores.put("ZNF738", 0.6000000238418579);
        return expectedScores;
    }

    @Test
    public void testGetPriorityType() {
        PhivePriority instance = new PhivePriority(TestPriorityServiceFactory.stubPriorityService());
        assertThat(instance.getPriorityType(), equalTo(PriorityType.PHIVE_PRIORITY));
    }

    @Test
    public void testPrioritizeGenes() {
        List<Gene> genes = getGenes();

        List<String> hpoIds= Lists.newArrayList("HP:0010055", "HP:0001363", "HP:0001156", "HP:0011304");
        PhivePriority phivePriority = new PhivePriority(TestPriorityServiceFactory.testPriorityService());
        phivePriority.prioritizeGenes(hpoIds, genes);

        List<PhivePriorityResult> results = genes.stream()
                .flatMap(gene -> gene.getPriorityResults().values().stream())
                .map(priorityResult -> (PhivePriorityResult) priorityResult)
                .sorted()
                .collect(toList());

        results.forEach(System.out::println);
        assertThat(results.size(), equalTo(genes.size()));

        Map<String, Double> actualScores = results.stream().collect(toMap(PhivePriorityResult::getGeneSymbol, PhivePriorityResult::getScore));
        checkScores(actualScores, expectedMouseScores());
    }

    @Test
    public void testPrioritise() {
        List<Gene> genes = getGenes();

        List<String> hpoIds= Lists.newArrayList("HP:0010055", "HP:0001363", "HP:0001156", "HP:0011304");
        PhivePriority phivePriority = new PhivePriority(TestPriorityServiceFactory.testPriorityService());

        List<PhivePriorityResult> results = phivePriority.prioritise(hpoIds, genes)
                .sorted()
                .collect(toList());

        results.forEach(System.out::println);
        assertThat(results.size(), equalTo(genes.size()));

        Map<String, Double> actualScores = results.stream().collect(toMap(PhivePriorityResult::getGeneSymbol, PhivePriorityResult::getScore));
        checkScores(actualScores, expectedMouseScores());
    }

    @Test
    public void testHashCode() {
        PhivePriority phivePriority = new PhivePriority(TestPriorityServiceFactory.testPriorityService());
        PhivePriority other = new PhivePriority(TestPriorityServiceFactory.testPriorityService());
        System.out.println(phivePriority.hashCode());
        System.out.println(other.hashCode());
        assertThat(phivePriority, equalTo(other));
    }
}