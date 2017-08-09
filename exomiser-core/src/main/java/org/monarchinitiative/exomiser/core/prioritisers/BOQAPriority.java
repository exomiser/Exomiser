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

import ontologizer.association.AssociationParser.Type;
import org.monarchinitiative.exomiser.core.model.Gene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import drseb.BoqaService;
import drseb.BoqaService.ResultEntry;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

import java.io.File;

/**
 * Score variants by BOQA
 *
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 * @author Max Schubach <max.schubach@bihealth.de>

 * @version 0.01 (9 December, 2013)
 */
public class BOQAPriority implements Prioritiser {

    private static final Logger logger = LoggerFactory.getLogger(BOQAPriority.class);

    private static final PriorityType PRIORITY_TYPE = PriorityType.BOQA_PRIORITY;
    
    private BoqaService boqaService;

    private static final double DEFAULT_SCORE = 0;



    /**
     * Create a new instance of the BOQA-Priority.
     *
     */
    public BOQAPriority(String dataFolder) {

    	 if (!dataFolder.endsWith(File.separator)) {
    		 dataFolder += File.separator;
         }
        String hpoOboFile = String.format("%s%s", dataFolder, "hp.obo");
        String hpoAnnotationFile = String.format("%s%s", dataFolder, "ALL_SOURCES_ALL_FREQUENCIES_genes_to_phenotype.txt");
        
        boqaService = new BoqaService(hpoOboFile, hpoAnnotationFile, Type.GPAF);
        
    }


    /**
     * Flag to output results of filtering against Uberpheno data.
     */
    @Override
    public PriorityType getPriorityType() {
        return PRIORITY_TYPE;
    }

    @Override
    public Stream<BOQAPriorityResult> prioritise(List<String> hpoIds, List<Gene> genes) {

        if (hpoIds.isEmpty()) {
            throw new BoqaException("Please supply some HPO terms. BOQA is unable to prioritise genes without these.");
        }


        ArrayList<String> hpoIdsAl = new ArrayList<>(hpoIds);
        Map<Gene, Double> geneScores = genes.stream().collect(toMap(Function.identity(), scoreGene(hpoIdsAl)));


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
    	for (ResultEntry result : scoredGenesRaw.values()){
    		String key = result.getItemRealId();
    		String entrezIdStr = key.replaceAll("NCBIENTREZ:", "");
    		int entrezId = Integer.parseInt(entrezIdStr);
    		
    		scoredGenes.put(entrezId, result.getScore());
    	}
    	
        return gene -> {
            int entrezGeneId = gene.getEntrezGeneID();
            
            if (!scoredGenes.containsKey(entrezGeneId)) {
                return DEFAULT_SCORE;
            }

            return scoredGenes.get(entrezGeneId);
        };
    }

  

    @Override
    public int hashCode() {
        return Objects.hash(BOQAPriority.class.getName());
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
