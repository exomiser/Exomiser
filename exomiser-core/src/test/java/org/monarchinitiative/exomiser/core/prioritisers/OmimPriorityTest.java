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

package org.monarchinitiative.exomiser.core.prioritisers;

import com.google.common.collect.Lists;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.prioritisers.model.Disease;
import org.monarchinitiative.exomiser.core.prioritisers.service.PriorityService;
import org.monarchinitiative.exomiser.core.prioritisers.service.TestPriorityServiceFactory;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class OmimPriorityTest {

    private final PriorityService priorityService = TestPriorityServiceFactory.testPriorityService();

    private final OmimPriority instance = new OmimPriority(priorityService);

    private List<Gene> getGenes() {
        return Lists.newArrayList(
                new Gene("FGFR2", 2263),
                new Gene("ROR2", 4920),
                new Gene("FREM2", 341640),
                new Gene("ZNF738", 148203)
        );
    }

    private OmimPriorityResult omimResultForGene(Gene gene, double score, Map<ModeOfInheritance, Double> scoresByMode) {
        return new OmimPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), score, getDiseasesForGene(gene), scoresByMode);
    }

    private List<Disease> getDiseasesForGene(Gene gene) {
        return priorityService.getDiseaseDataAssociatedWithGeneId(gene.getEntrezGeneID());
    }

    private Map<ModeOfInheritance, Double> allModesScoreOne() {
        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 1.0);
        return scores;
    }

    @Test
    public void getPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.OMIM_PRIORITY));
    }

    @Test
    public void prioritizeGenesUnrecognisedGene() {
        List<Gene> genes = Lists.newArrayList(new Gene("Wibble", 999999999));

        instance.prioritizeGenes(Collections.emptyList(), genes);

        genes.forEach(gene -> {
            OmimPriorityResult result = (OmimPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);
//            System.out.printf("%s %s %s%n", gene.getGeneSymbol(), gene.getCompatibleInheritanceModes(), result);
            OmimPriorityResult expected = omimResultForGene(gene, 1.0, allModesScoreOne());
            assertThat(result, equalTo(expected));
        });
    }

    @Test
    public void prioritizeGenesNoInheritanceModes() {
        List<Gene> genes = getGenes();
        instance.prioritizeGenes(Collections.emptyList(), genes);
        genes.forEach(gene -> {
            OmimPriorityResult result = (OmimPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);
//            System.out.printf("%s %s %s%n", gene.getGeneSymbol(), gene.getCompatibleInheritanceModes(), result);
            OmimPriorityResult expected = omimResultForGene(gene, 1.0, allModesScoreOne());
            assertThat(result, equalTo(expected));
        });
    }

    @Test
    public void prioritizeGenesNoAssociatedDiseases() {
        //ZNF738 has no associated conditions.
        Gene gene = new Gene("ZNF738", 148203);
        List<Gene> genes = Lists.newArrayList(gene);

        instance.prioritizeGenes(Collections.emptyList(), genes);
        OmimPriorityResult result = (OmimPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);

        OmimPriorityResult expected = omimResultForGene(gene, 1.0, allModesScoreOne());
        assertThat(result, equalTo(expected));
    }

    @Test
    public void prioritizeGenesRECESSIVEModeIsCompatible() {
        //ROR2 has two associated conditions, one recessive, the other dominant.
        // We're going to simulate this matching the recessive one.
        Gene gene = new Gene("ROR2", 4920);
        gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        List<Gene> genes = Lists.newArrayList(gene);

        instance.prioritizeGenes(Collections.emptyList(), genes);
        OmimPriorityResult result = (OmimPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);

        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);

        OmimPriorityResult expected = omimResultForGene(gene, 1.0, scores);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void prioritizeGenesDOMINANTModeIsCompatible() {
        //ROR2 has two associated conditions, one recessive, the other dominant.
        // We're going to simulate this matching the dominant one.
        Gene gene = new Gene("ROR2", 4920);
        gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        List<Gene> genes = Lists.newArrayList(gene);

        instance.prioritizeGenes(Collections.emptyList(), genes);
        OmimPriorityResult result = (OmimPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);

        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);

        OmimPriorityResult expected = omimResultForGene(gene, 1.0, scores);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void prioritizeGenesMultipleModesCompatible() {
        //ROR2 has two associated conditions, one recessive, the other dominant.
        // We're going to simulate this matching the dominant one.
        Gene gene = new Gene("ROR2", 4920);
        gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));
        List<Gene> genes = Lists.newArrayList(gene);

        instance.prioritizeGenes(Collections.emptyList(), genes);
        OmimPriorityResult result = (OmimPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);

        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 1.0);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 1.0);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);

        OmimPriorityResult expected = omimResultForGene(gene, 1.0, scores);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void prioritizeGenesNoModeSet() {
        //ROR2 has two associated conditions, one recessive, the other dominant.
        // We're going to simulate this matching the dominant one.
        Gene gene = new Gene("ROR2", 4920);
        List<Gene> genes = Lists.newArrayList(gene);

        instance.prioritizeGenes(Collections.emptyList(), genes);

        OmimPriorityResult result = (OmimPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);

        Map<ModeOfInheritance, Double> scores = allModesScoreOne();

        OmimPriorityResult expected = omimResultForGene(gene, 1.0, scores);
        assertThat(result, equalTo(expected));
    }

    @Test
    public void prioritizeGenesInheritanceModeIsNotCompatible() {
        //FREM2 has only one associated condition (recessive). We're going to simulate this not matching.
        Gene gene = new Gene("FREM2", 341640);
        gene.setCompatibleInheritanceModes(EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        List<Gene> genes = Lists.newArrayList(gene);

        instance.prioritizeGenes(Collections.emptyList(), genes);

        OmimPriorityResult result = (OmimPriorityResult) gene.getPriorityResult(PriorityType.OMIM_PRIORITY);

        Map<ModeOfInheritance, Double> scores = new EnumMap<>(ModeOfInheritance.class);
        scores.put(ModeOfInheritance.AUTOSOMAL_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.AUTOSOMAL_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_RECESSIVE, 0.5);
        scores.put(ModeOfInheritance.X_DOMINANT, 0.5);
        scores.put(ModeOfInheritance.MITOCHONDRIAL, 0.5);

        OmimPriorityResult expected = omimResultForGene(gene, 0.5, scores);
        assertThat(result, equalTo(expected));
    }

}