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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.monarchinitiative.exomiser.core.model.Gene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import drseb.BoqaService;
import drseb.BoqaService.ResultEntry;
import ontologizer.association.AssociationParser.Type;
import ontologizer.go.Term;

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

        Map<Integer, Double> geneScores = calculateGenePhenotypeScores(hpoIds);

        return genes.stream().map(toBoqaPriorityResult(geneScores));
    }

    private Map<Integer, Double> calculateGenePhenotypeScores(List<String> hpoIds) {
    	HashSet<Term> ids =makeHpoQueryTerms(hpoIds);
    	 if (ids.isEmpty())
             throw new BoqaException("Please supply some HPO terms. BOQA is unable to prioritise genes without these.");
        return boqaService.scoreItems(ids).values().stream()
                .collect(ImmutableMap.toImmutableMap(getEntrezId(), ResultEntry::getScore));
    }
    
    private HashSet<Term> makeHpoQueryTerms(List<String> hpoIds) {
        return hpoIds.stream()
                .map(termIdString -> {
                    Term term = boqaService.getOntology().getTermIncludingAlternatives(termIdString);
                    if (term == null) {
                        logger.error("Unrecognised HPO input term {}. This will not be used in the analysis.", termIdString);
                    }
                    return term;
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Function<ResultEntry, Integer> getEntrezId() {
        return resultEntry -> {
            String key = resultEntry.getItemRealId();
            String entrezIdStr = key.replace("NCBIENTREZ:", "");
            return Integer.parseInt(entrezIdStr);
        };
    }

    private Function<Gene, BOQAPriorityResult> toBoqaPriorityResult(Map<Integer, Double> geneScores) {
        return gene -> {
            double boqaScore = geneScores.getOrDefault(gene.getEntrezGeneID(), DEFAULT_SCORE);
            return new BOQAPriorityResult(gene.getEntrezGeneID(), gene.getGeneSymbol(), boqaScore);
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

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private BoqaException(String message) {
            super(message);
        }
    }

}
