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

import drseb.BoqaService;
import drseb.BoqaService.ResultEntry;
import ontologizer.association.AssociationParser.Type;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * Score genes by BOQA. This will return the probability of the gene matching the set of input phenotypes.
 *
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 * @author Max Schubach <max.schubach@bihealth.de>
 */
public class BOQAPriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(BOQAPriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.BOQA_PRIORITY;

    private static final double DEFAULT_SCORE = 0;

    private final BoqaService boqaService;

    /**
     * Create a new instance of the BOQA-Priority.
     */
    public BOQAPriority(String dataFolder) {
        this(Paths.get(dataFolder));
    }

    public BOQAPriority(Path dataDirectory) {
        Path dataDirAbsolutePath = dataDirectory.toAbsolutePath();

        Path hpoOboFile = dataDirAbsolutePath.resolve("hp.obo");
        Path hpoAnnotationFile = dataDirAbsolutePath.resolve("ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt");

        boqaService = new BoqaService(hpoOboFile.toString(), hpoAnnotationFile.toString(), Type.GPAF);
    }

    /**
     * STUB CONSTRUCTOR - ONLY USED FOR TESTING PURPOSES TO AVOID NULL POINTERS FROM ORIGINAL CONSTRUCTOR. DO NOT USE FOR PRODUCTION CODE - WILL THROW NPE!!!!
     */
    protected BOQAPriority() {
        boqaService = null;
    }

    @Override
    public Stream<BOQAPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {

        if (hpoIds.isEmpty()) {
            throw new BoqaException("Please supply some HPO terms. BOQA is unable to prioritise genes without these.");
        }

        Map<Gene, Double> geneScores = genes.stream()
                .collect(toMap(Function.identity(), scoreGene(new ArrayList<>(hpoIds))));

        return geneScores.entrySet().stream()
                .map(entry -> {
                    Gene gene = entry.getKey();
                    double boqaScore = entry.getValue();
                    return new BOQAPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), boqaScore);
                });
    }

    private Function<Gene, Double> scoreGene(ArrayList<String> queryTerms) {

        HashMap<String, ResultEntry> scoredGenesRaw = boqaService.scoreItems(queryTerms);
        HashMap<Integer, Double> scoredGenes = new HashMap<>();
        for (ResultEntry result : scoredGenesRaw.values()) {
            String key = result.getItemRealId();
            String entrezIdStr = key.replaceAll("NCBIENTREZ:", "");
            int entrezId = Integer.parseInt(entrezIdStr);

            scoredGenes.put(entrezId, result.getScore());
        }

        return gene -> {
            int entrezGeneId = gene.getEntrezGeneID();
            return scoredGenes.getOrDefault(entrezGeneId, DEFAULT_SCORE);
        };
    }

    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BOQAPriority that = (BOQAPriority) o;
        return Objects.equals(boqaService, that.boqaService);
    }

    //equals and hashCode should really be produced from the ontology and gene-phenotype associations
    @Override
    public int hashCode() {
        return Objects.hash(boqaService);
    }

    @Override
    public String toString() {
        return "BOQAPriority{}";
    }


    private static class BoqaException extends RuntimeException {

        private BoqaException(String message) {
            super(message);
        }
    }

}
