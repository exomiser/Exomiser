/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2021 Queen Mary University of London.
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.filters;

import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.FilterStatus;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;
import org.monarchinitiative.exomiser.core.prioritisers.MockPriorityResult;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.filters.FilterType.*;

/**
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class SimpleGeneFilterRunnerTest {

    private SimpleGeneFilterRunner instance;

    private InheritanceFilter inheritanceFilter;
    private final ModeOfInheritance PASS_MODE = ModeOfInheritance.AUTOSOMAL_DOMINANT;
    private final ModeOfInheritance FAIL_MODE = ModeOfInheritance.AUTOSOMAL_RECESSIVE;

    private List<GeneFilter> filters;

    private List<Gene> genes;
    private Gene passGene;
    private Gene failGene;

    @BeforeEach
    public void setUp() {
        instance = new SimpleGeneFilterRunner();
        inheritanceFilter = new InheritanceFilter(PASS_MODE);

        passGene = makeGeneWithVariants("GENE1", 12345, EnumSet.of(PASS_MODE));

        failGene = makeGeneWithVariants("GENE2", 56789, EnumSet.of(FAIL_MODE));

        filters = new ArrayList<>();
        filters.add(inheritanceFilter);

        genes = new ArrayList<>();
        genes.add(passGene);
        genes.add(failGene);
    }

    private Gene makeGeneWithVariants(String geneSymbol, int geneId, Set<ModeOfInheritance> inheritanceModes) {
        Gene gene = new Gene(geneSymbol, geneId);
        gene.setCompatibleInheritanceModes(inheritanceModes);
        //Add some variants. For the purposes of this test these are required to
        //have the same inheritance mode as the gene to satisfy the unique bahaviour of the Inheritance filter. 
        //TODO: change this - mock filter required? We're not trying to test the functionality of the InheritanceFilter here.
        gene.addVariant(TestFactory.variantBuilder(1, 1, "A", "T").build());
        gene.addVariant(TestFactory.variantBuilder(1, 2, "G", "T").build());
        for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
            variantEvaluation.setCompatibleInheritanceModes(inheritanceModes);
        }
        return gene;
    }

    private void assertVariantsUnfilteredAndDoNotPassFilter(List<Gene> genes, List<GeneFilter> geneFilters) {
        for (Gene gene : genes) {
            assertThat(gene.passedFilters(), is(true));
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                for (GeneFilter filter : geneFilters) {
                    assertThat(variantEvaluation.passedFilter(filter.getFilterType()), is(false));
                }
                assertThat(variantEvaluation.getFilterStatus(), equalTo(FilterStatus.UNFILTERED));
            }
        }
        assertThat(instance.filterCounts(), equalTo(List.of()));
    }

    private void assertFilterStatus(Gene gene, List<GeneFilter> filters, FilterStatus filterStatus) {
        boolean hasPassed = true;
        if (filterStatus == FilterStatus.FAILED) {
            hasPassed = false;
        }
//        System.out.println(gene);
//        for (VariantEvaluation variantEvaluation :  gene.getVariantEvaluations()) {
//            System.out.println(variantEvaluation);
//        }

        assertThat(gene.passedFilters(), equalTo(hasPassed));
        for (GeneFilter filter : filters) {
            FilterType filterType = filter.getFilterType();
            assertThat(gene.passedFilter(filterType), equalTo(hasPassed));
            for (VariantEvaluation variantEvaluation : gene.getVariantEvaluations()) {
                assertThat(variantEvaluation.passedFilter(filterType), equalTo(hasPassed));
                assertThat(variantEvaluation.getFilterStatus(), equalTo(filterStatus));
            }
        }
    }

    @Test
    public void testRunMultipleFiltersOverGenes() {
        assertVariantsUnfilteredAndDoNotPassFilter(genes, filters);

        filters.forEach(filter -> instance.run(filter, genes));

        assertFilterStatus(passGene, filters, FilterStatus.PASSED);
        assertFilterStatus(failGene, filters, FilterStatus.FAILED);

        FilterResultCount inheritanceFilter = new FilterResultCount(INHERITANCE_FILTER, 2, 2);
        assertThat(instance.filterCounts(), equalTo(List.of(inheritanceFilter)));
    }

    @Test
    public void testRunSingleFilterOverGenes() {
        assertVariantsUnfilteredAndDoNotPassFilter(genes, filters);

        instance.run(inheritanceFilter, genes);

        assertFilterStatus(passGene, filters, FilterStatus.PASSED);
        assertFilterStatus(failGene, filters, FilterStatus.FAILED);

        FilterResultCount inheritanceFilter = new FilterResultCount(INHERITANCE_FILTER, 2, 2);
        assertThat(instance.filterCounts(), equalTo(List.of(inheritanceFilter)));
    }

    @Test
    public void testRunMultipleFiltersOverGenesTestGeneOnlyFilter() {
        filters = List.of(
                // these are only run in this order as the inheritance filter is ensured to always run last by the analysis runner
                new PriorityScoreFilter(PriorityType.HIPHIVE_PRIORITY, 0.7f),
                inheritanceFilter
        );
        passGene.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, passGene.getEntrezGeneID(), passGene.getGeneSymbol(), 0.8f));
        failGene.addPriorityResult(new MockPriorityResult(PriorityType.HIPHIVE_PRIORITY, failGene.getEntrezGeneID(), failGene.getGeneSymbol(), 0.6f));

        assertVariantsUnfilteredAndDoNotPassFilter(genes, filters);

        filters.forEach(filter -> instance.run(filter, genes));
        assertFilterStatus(passGene, filters, FilterStatus.PASSED);
        assertFilterStatus(failGene, filters, FilterStatus.FAILED);

        FilterResultCount priorityScoreCount = new FilterResultCount(PRIORITY_SCORE_FILTER, 1, 1);
        FilterResultCount inheritanceCount = new FilterResultCount(INHERITANCE_FILTER, 2, 0);
        assertThat(instance.filterCounts(), equalTo(List.of(priorityScoreCount, inheritanceCount)));
    }
}
