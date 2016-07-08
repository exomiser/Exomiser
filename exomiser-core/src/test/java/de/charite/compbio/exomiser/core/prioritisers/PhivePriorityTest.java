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
import de.charite.compbio.exomiser.core.prioritisers.util.TestPriorityServiceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class PhivePriorityTest {

    private Logger logger = LoggerFactory.getLogger(PhivePriorityTest.class);

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
                .flatMap(gene -> gene.getPriorityResults().values()
                        .stream())
                .map(priorityResult -> (PhivePriorityResult) priorityResult)
                .sorted(Comparator.comparingDouble(PriorityResult::getScore).reversed())
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
//        List<PriorityResult> results = instance.prioritizeGenes(genes);

        List<PhivePriorityResult> results = getPriorityResultsOrderedByScore(genes);
        assertThat(results.size(), equalTo(genes.size()));
        results.forEach(result -> {
            System.out.println(result);
        });

//      Scores from flatfile (these will have suffered slight rounding errors compared to the database)
        List<PhivePriorityResult> expected = Lists.newArrayList(
                new PhivePriorityResult(2263, "FGFR2", 0.8278620340423056, "MGI:95523", "Fgfr2"),
                new PhivePriorityResult(4920, "ROR2", 0.6999088391144016, "MGI:1347521", "Ror2"),
                new PhivePriorityResult(341640, "FREM2", 0.6208762175615226, "MGI:2444465", "Frem2"),
                new PhivePriorityResult(148203, "ZNF738", 0.6000000238418579, null, null)
        );
        assertThat(results, equalTo(expected));
    }

}