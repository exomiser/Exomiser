/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2015  Charite Universitätsmedizin Berlin and Genome Research Ltd.
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

package de.charite.compbio.exomiser.core.analysis.util;

import de.charite.compbio.exomiser.core.model.TopologicalDomain;
import de.charite.compbio.exomiser.core.model.VariantCoordinates;
import de.charite.compbio.jannovar.impl.intervals.IntervalArray;
import de.charite.compbio.jannovar.impl.intervals.IntervalEndExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Index of Topological Associated Domains (TADs). This is backed by an interval tree so should enable fast lookup
 * of TADs overlapping a given position.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 * @see TopologicalDomain
 * @see GeneReassigner
 */
public class TadIndex {

    private static final Logger logger = LoggerFactory.getLogger(TadIndex.class);

    private final Map<Integer, IntervalArray<TopologicalDomain>> index;

    public TadIndex(Collection<TopologicalDomain> tads) {
        this.index = populateIndex(tads);
    }

    private Map<Integer, IntervalArray<TopologicalDomain>> populateIndex(Collection<TopologicalDomain> tads) {
        Map<Integer, Set<TopologicalDomain>> tadIndex = createTadIndex(tads);
        Map<Integer, IntervalArray<TopologicalDomain>> index = createChromosomeIntervalTreeIndex(tadIndex);
        logger.info("Created index for {} chromosomes totalling {} TADs containing {} genes", index.keySet().size(), tads.size(), tads.stream().mapToInt(tad-> tad.getGenes().keySet().size()).sum());
        return index;
    }

    private Map<Integer, Set<TopologicalDomain>> createTadIndex(Collection<TopologicalDomain> tads) {
        Map<Integer, Set<TopologicalDomain>> tadIndex = new HashMap<>();
        for (TopologicalDomain tad : tads) {
            if (!tadIndex.containsKey(tad.getChromosome())) {
                Set<TopologicalDomain> tadsInChr = new LinkedHashSet<>();
                tadsInChr.add(tad);
                tadIndex.put(tad.getChromosome(), tadsInChr);
            } else {
                tadIndex.get(tad.getChromosome()).add(tad);
            }
        }
        return tadIndex;
    }

    private Map<Integer, IntervalArray<TopologicalDomain>> createChromosomeIntervalTreeIndex(Map<Integer, Set<TopologicalDomain>> tadIndex) {
        Map<Integer, IntervalArray<TopologicalDomain>> index = new HashMap<>();
        for (Integer chrId : tadIndex.keySet()) {
            IntervalArray<TopologicalDomain> intervalTree = new IntervalArray<>(tadIndex.get(chrId), new TopologicalDomainEndExtractor());
            logger.debug("Chr: {} - {} TADs", chrId, intervalTree.size());
            index.put(chrId, intervalTree);
        }
        return index;
    }


    public List<TopologicalDomain> getTadsContainingVariant(VariantCoordinates variantCoordinates) {
        IntervalArray<TopologicalDomain> intervalTree = index.get(variantCoordinates.getChromosome());
        if (intervalTree == null) {
            return Collections.emptyList();
        }
        IntervalArray.QueryResult queryResult = intervalTree.findOverlappingWithPoint(variantCoordinates.getPosition());
        return queryResult.getEntries();
    }

    private class TopologicalDomainEndExtractor implements IntervalEndExtractor<TopologicalDomain> {

        public int getBegin(TopologicalDomain topologicalDomain) {
            return topologicalDomain.getStart();
        }

        public int getEnd(TopologicalDomain topologicalDomain) {
            return topologicalDomain.getEnd();
        }
    }
}


