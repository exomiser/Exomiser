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

package org.monarchinitiative.exomiser.core.prioritisers;

import org.junit.Test;
import org.monarchinitiative.exomiser.core.genome.TestFactory;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.monarchinitiative.exomiser.core.phenotype.PhenotypeTerm;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class BOQAPriorityTest {

    private static final Path boqaResourcesFolder = Paths.get("src/test/resources/prioritisers/boqa").toAbsolutePath();
    private static final Path geneToPhenotypesPath = boqaResourcesFolder.resolve("ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt");
    private static final BOQAPriority instance = new BOQAPriority(boqaResourcesFolder);

    private final GenePhenotypes genePhenotypes = new GenePhenotypes(geneToPhenotypesPath);

    @Test
    public void testConstructorWithString() {
        new BOQAPriority(boqaResourcesFolder.toAbsolutePath().toString());
    }

    @Test
    public void testPriorityType() {
        assertThat(instance.getPriorityType(), equalTo(PriorityType.BOQA_PRIORITY));
    }

    @Test(expected = RuntimeException.class)
    public void testPrioritise_WithoutPhenotypesShouldThrowException() {
        List<String> phenotypes = Collections.emptyList();
        List<Gene> genes = TestFactory.buildGenes();

        instance.prioritise(phenotypes, genes);
    }

    @Test
    public void testPrioritise_PerfectMatch() {
        Gene FGFR2 = TestFactory.newGeneFGFR2();
        //here we're using all the phenotypes associated with the FGFR2 gene, which should give us a perfect match with probability of 1
        List<String> phenotypes = genePhenotypes.getHpoIdsForGene(FGFR2);
        List<Gene> genes = TestFactory.buildGenes();

        List<BOQAPriorityResult> results = instance.prioritise(phenotypes, genes).sorted().collect(toList());
        results.forEach(System.out::println);

        BOQAPriorityResult topScoringResult = results.get(0);
        assertThat(topScoringResult.getGeneId(), equalTo(FGFR2.getEntrezGeneID()));
    }

    @Test
    public void testPrioritise_ImperfectMatch() {
        //This is a list of partial matches to FGFR2
        List<String> phenotypes = Arrays.asList("HP:0001156", "HP:0001363", "HP:0011304", "HP:0010055", "HP:0001770");
        List<Gene> genes = TestFactory.buildGenes();

        instance.prioritise(phenotypes, genes).forEach(result -> {
            System.out.println(result);
            //GNRHR2 is not in the genes_to_phenotypes file so will have a score of zero.
            if (result.geneSymbol.equals("GNRHR2")) {
                assertThat(result.score, equalTo(0d));
            } else {
                //not going to specify these as they could change depending on the algorithm internals, but they should not be zero
                assertThat(result.score > 0d, is(true));
            }
        });
    }

    private class GenePhenotypes {
        Map<Integer, List<PhenotypeTerm>> genePhenotypes;

        protected GenePhenotypes(Path genesToPhenotypes) {
            genePhenotypes = parse(genesToPhenotypes);
        }

        private Map<Integer, List<PhenotypeTerm>> parse(Path genesToPhenotypes) {
            Map<Integer, List<PhenotypeTerm>> geneIdentifierToPhenotypes = new HashMap<>();
            try (BufferedReader br = Files.newBufferedReader(genesToPhenotypes)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    String[] tokens = line.split("\t");

                    //2261	FGFR3	Strabismus	HP:0000486
                    //2261	FGFR3	Short middle phalanx of finger	HP:0005819
                    //2263	FGFR2	Symphalangism affecting the phalanges of the hand	HP:0009773
                    //2263	FGFR2	Autosomal dominant inheritance	HP:0000006
                    //2263	FGFR2	Variable expressivity	HP:0003828

                    Integer geneIdentifier = Integer.parseInt(tokens[0]);

                    PhenotypeTerm phenotypeTerm = PhenotypeTerm.of(tokens[3], tokens[2]);
                    if (geneIdentifierToPhenotypes.containsKey(geneIdentifier)) {
                        geneIdentifierToPhenotypes.get(geneIdentifier).add(phenotypeTerm);
                    } else {
                        List<PhenotypeTerm> phenotypes = new ArrayList<>();
                        phenotypes.add(phenotypeTerm);
                        geneIdentifierToPhenotypes.put(geneIdentifier, phenotypes);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error opening file " + e);
            }

            return geneIdentifierToPhenotypes;
        }

        protected List<String> getHpoIdsForGene(Gene gene) {
            return genePhenotypes.getOrDefault(gene.getEntrezGeneID(), Collections.emptyList())
                    .stream()
                    .map(PhenotypeTerm::getId)
                    .collect(toList());
        }
    }
}