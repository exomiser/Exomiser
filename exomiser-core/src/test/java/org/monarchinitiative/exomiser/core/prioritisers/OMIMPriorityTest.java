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

package org.monarchinitiative.exomiser.core.prioritisers;

import com.google.common.collect.Lists;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.util.EnumSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OMIMPriorityTest {

    private OMIMPriority instance;

    @Before
    public void setUp() {
        instance = new OMIMPriority(TestPriorityServiceFactory.TEST_SERVICE);
    }

    private List<Gene> getGenes() {
        return Lists.newArrayList(
                new Gene("FGFR2", 2263),
                new Gene("ROR2", 4920),
                new Gene("FREM2", 341640),
                new Gene("ZNF738", 148203)
        );
    }

    @Test
    public void getPriorityType() throws Exception {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.OMIM_PRIORITY));
    }

    @Test
    public void prioritizeGenes_unrecognisedGene() throws Exception {
        List<Gene> genes = Lists.newArrayList(new Gene("Wibble", 999999999));

        instance.prioritizeGenes(genes);

        genes.forEach(gene -> {
            OMIMPriorityResult result = (OMIMPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);
            System.out.printf("%s %s %s%n", gene.getGeneSymbol(), gene.getInheritanceModes(), result);
            assertThat(result.getScore(), equalTo(1d));
            assertThat(result.getAssociatedDiseases().isEmpty(), is(true));
        });
    }

    @Test
    public void prioritizeGenes_NoInheritanceModes() throws Exception {
        List<Gene> genes = getGenes();
        instance.prioritizeGenes(genes);
        genes.forEach(gene -> {
            OMIMPriorityResult result = (OMIMPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);
            System.out.printf("%s %s %s%n", gene.getGeneSymbol(), gene.getInheritanceModes(), result);
            assertThat(result.getScore(), equalTo(1d));
        });
    }

    @Test
    public void prioritizeGenes_NoAssociatedDiseases() throws Exception {
        //ZNF738 has no associated conditions.
        Gene znf738 = new Gene("ZNF738", 148203);
        List<Gene> genes = Lists.newArrayList(znf738);

        instance.prioritise(genes).forEach(result -> {
            System.out.println(result);
            assertThat(result.getScore(), equalTo(1d));
            assertThat(result.getAssociatedDiseases().isEmpty(), is(true));
        });
    }

    @Test
    public void prioritizeGenes_RECESSIVE_ModeIsCompatible() throws Exception {
        //ROR2 has two associated conditions, one recessive, the other dominant.
        // We're going to simulate this matching the recessive one.
        Gene ror2 = new Gene("ROR2", 4920);
        ror2.setInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        List<Gene> genes = Lists.newArrayList(ror2);

        instance.prioritizeGenes(genes);

        genes.forEach(gene -> {
            checkOmimScoreAndHasAssociatedDiseases(gene, 1d, false);
        });
    }

    @Test
    public void prioritizeGenes_DOMINANT_ModeIsCompatible() throws Exception {
        //ROR2 has two associated conditions, one recessive, the other dominant.
        // We're going to simulate this matching the dominant one.
        Gene ror2 = new Gene("ROR2", 4920);
        ror2.setInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        List<Gene> genes = Lists.newArrayList(ror2);

        instance.prioritizeGenes(genes);

        genes.forEach(gene -> {
            checkOmimScoreAndHasAssociatedDiseases(gene, 1d, false);
        });
    }

    @Test
    public void prioritizeGenes_NoModeSet() throws Exception {
        //ROR2 has two associated conditions, one recessive, the other dominant.
        // We're going to simulate this matching the dominant one.
        Gene ror2 = new Gene("ROR2", 4920);
        List<Gene> genes = Lists.newArrayList(ror2);

        instance.prioritizeGenes(genes);

        genes.forEach(gene -> {
            checkOmimScoreAndHasAssociatedDiseases(gene, 1d, false);
        });
    }

    private void checkOmimScoreAndHasAssociatedDiseases(Gene gene, double score, boolean associatedDiseasesIsEmpty) {
        OMIMPriorityResult result = (OMIMPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);
        System.out.printf("%s %s %s%n", gene.getGeneSymbol(), gene.getInheritanceModes(), result);
        assertThat(result.getScore(), equalTo(score));
        assertThat(result.getAssociatedDiseases().isEmpty(), is(associatedDiseasesIsEmpty));
    }

    @Test
    public void prioritizeGenes_InheritanceModeIsNotCompatible() throws Exception {
        //FREM2 has only one associated condition (recessive). We're going to simulate this not matching.
        Gene frem2 = new Gene("FREM2", 341640);
        frem2.setInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        List<Gene> genes = Lists.newArrayList(frem2);

        instance.prioritizeGenes(genes);

        genes.forEach(gene -> {
            checkOmimScoreAndHasAssociatedDiseases(gene, 0.5d, false);
        });


    }

}