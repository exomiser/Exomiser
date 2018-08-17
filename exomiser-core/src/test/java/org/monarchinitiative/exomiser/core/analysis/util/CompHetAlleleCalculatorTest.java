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

package org.monarchinitiative.exomiser.core.analysis.util;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypeType;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.model.Pedigree;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Sex;
import org.monarchinitiative.exomiser.core.model.Pedigree.Individual.Status;
import org.monarchinitiative.exomiser.core.model.VariantEvaluation;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.monarchinitiative.exomiser.core.analysis.util.TestAlleleFactory.*;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class CompHetAlleleCalculatorTest {


    @Test
    public void testFindCompHetCompatibleAllelesTwoAffectedSibsUnaffectedMotherhMissingFather() {
        Gene gene = new Gene("ABC", 123);

        //1 98518687 . T A 733 . GT 1|0 1|0 1|0
        List<Allele> alleles = buildAlleles("T", "A");

        Genotype proband = buildPhasedSampleGenotype("Cain", alleles.get(0), alleles.get(1));
        assertThat(proband.getType(), equalTo(GenotypeType.HET));

        Genotype brother = buildPhasedSampleGenotype("Abel", alleles.get(0), alleles.get(1));
        assertThat(brother.getType(), equalTo(GenotypeType.HET));

        Genotype mother = buildPhasedSampleGenotype("Eve", alleles.get(0), alleles.get(1));
        assertThat(mother.getType(), equalTo(GenotypeType.HET));

        VariantContext vc98518687 = buildVariantContext(1, 98518687, alleles, proband, brother, mother);
        VariantEvaluation var98518687 = filteredVariant(1, 98518687, "T", "A", FilterResult.pass(FilterType.FREQUENCY_FILTER), vc98518687);
        System.out.println("Built allele " + var98518687);
        gene.addVariant(var98518687);

        //1 98518683 . T A 733 . GT 1|0 1|0 1|0
        List<Allele> alleles98518683 = buildAlleles("T", "A");

        Genotype proband98518683 = buildPhasedSampleGenotype("Cain", alleles98518683.get(0), alleles98518683.get(1));
        assertThat(proband98518683.getType(), equalTo(GenotypeType.HET));

        Genotype brother98518683 = buildPhasedSampleGenotype("Abel", alleles98518683.get(0), alleles98518683.get(1));
        assertThat(brother98518683.getType(), equalTo(GenotypeType.HET));

        Genotype mother98518683 = buildPhasedSampleGenotype("Eve", alleles98518683.get(0), alleles98518683.get(1));
        assertThat(mother98518683.getType(), equalTo(GenotypeType.HET));

        VariantContext vc98518683 = buildVariantContext(1, 98518683, alleles98518683, proband98518683, brother98518683, mother98518683);
        VariantEvaluation var98518683 = filteredVariant(1, 98518683, "T", "A", FilterResult.pass(FilterType.FREQUENCY_FILTER), vc98518683);
        System.out.println("Built allele " + var98518683);
        gene.addVariant(var98518683);

        //1 97723020 . A G 1141 . GT 1/0 0/0 1/0
        List<Allele> alleles97723020 = buildAlleles("A", "G");

        Genotype proband97723020 = buildPhasedSampleGenotype("Cain", alleles97723020.get(0), alleles97723020.get(1));
        assertThat(proband97723020.getType(), equalTo(GenotypeType.HET));

        Genotype brother97723020 = buildPhasedSampleGenotype("Abel", alleles97723020.get(0), alleles97723020.get(1));
        assertThat(brother97723020.getType(), equalTo(GenotypeType.HET));

        Genotype mother97723020 = buildPhasedSampleGenotype("Eve", alleles97723020.get(0), alleles97723020.get(0));
        assertThat(mother97723020.getType(), equalTo(GenotypeType.HOM_REF));

        VariantContext vc97723020 = buildVariantContext(1, 97723020, alleles97723020, proband97723020, brother97723020, mother97723020);
        VariantEvaluation var97723020 = filteredVariant(1, 97723020, "A", "G", FilterResult.pass(FilterType.FREQUENCY_FILTER), vc97723020);
        System.out.println("Built allele " + var97723020);
        gene.addVariant(var97723020);

        Individual probandIndividual = Individual.builder().id("Cain").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual brotherIndividual = Individual.builder().id("Abel").motherId("Eve").sex(Sex.MALE).status(Status.AFFECTED).build();
        Individual motherIndividual = Individual.builder().id("Eve").sex(Sex.FEMALE).status(Status.UNAFFECTED).build();

        Pedigree pedigree = Pedigree.of(probandIndividual, motherIndividual, brotherIndividual);

        CompHetAlleleCalculator instance = new CompHetAlleleCalculator(new InheritanceModeAnnotator(pedigree, InheritanceModeOptions.defaults()));
        List<List<VariantEvaluation>> compHetAlleles = instance.findCompatibleCompHetAlleles(gene.getPassedVariantEvaluations());

        assertThat(compHetAlleles.size(), equalTo(2));
        assertThat(compHetAlleles.get(0), equalTo(Arrays.asList(var98518687, var97723020)));
        assertThat(compHetAlleles.get(1), equalTo(Arrays.asList(var98518683, var97723020)));
    }

}