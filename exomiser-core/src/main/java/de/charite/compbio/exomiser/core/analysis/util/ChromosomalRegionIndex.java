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

import de.charite.compbio.exomiser.core.model.ChromosomalRegion;
import de.charite.compbio.exomiser.core.model.VariantCoordinates;
import de.charite.compbio.jannovar.impl.intervals.IntervalArray;
import de.charite.compbio.jannovar.impl.intervals.IntervalEndExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Interval tree-backed index for chromosomal regions. It enables extremely fast in-memory lookups to find the regions
 * in which a variant can be found.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class ChromosomalRegionIndex<T extends ChromosomalRegion> {

    private static final Logger logger = LoggerFactory.getLogger(ChromosomalRegionIndex.class);

    private final Map<Integer, IntervalArray<T>> index;

    public ChromosomalRegionIndex(Collection<T> chromosomalRegions) {
        this.index = populateIndex(chromosomalRegions);
    }

    private Map<Integer, IntervalArray<T>> populateIndex(Collection<T> chromosomalRegions) {
        Map<Integer, Set<T>> regionIndex = createRegionIndex(chromosomalRegions);
        Map<Integer, IntervalArray<T>> index = createChromosomeIntervalTreeIndex(regionIndex);
        logger.debug("Created index for {} chromosomes totalling {} regions", index.keySet().size(), chromosomalRegions.size());
        return index;
    }

    private Map<Integer, Set<T>> createRegionIndex(Collection<T> chromosomalRegions) {
        Map<Integer, Set<T>> regionIndex = new HashMap<>();
        for (T region : chromosomalRegions) {
            if (!regionIndex.containsKey(region.getChromosome())) {
                Set<T> regionsInChr = new LinkedHashSet<>();
                regionsInChr.add(region);
                regionIndex.put(region.getChromosome(), regionsInChr);
            } else {
                regionIndex.get(region.getChromosome()).add(region);
            }
        }
        return regionIndex;
    }

    private Map<Integer, IntervalArray<T>> createChromosomeIntervalTreeIndex(Map<Integer, Set<T>> regionIndex) {
        Map<Integer, IntervalArray<T>> index = new HashMap<>();
        for (Integer chrId : regionIndex.keySet()) {
            IntervalArray<T> intervalTree = new IntervalArray<>(regionIndex.get(chrId), new ChromosomalRegionEndExtractor());
            logger.debug("Chr: {} - {} regions", chrId, intervalTree.size());
            index.put(chrId, intervalTree);
        }
        return index;
    }

    public List<T> getRegionsContainingVariant(VariantCoordinates variantCoordinates) {
        int chromosome = variantCoordinates.getChromosome();
        int position = variantCoordinates.getPosition();
        return getRegionsOverlappingPosition(chromosome, position);
    }

    /**
     * Use one-based co-ordinates for this method.
     * @param chromosome
     * @param position
     * @return
     */
    public List<T> getRegionsOverlappingPosition(int chromosome, int position) {
        IntervalArray<T> intervalTree = index.get(chromosome);
            if (intervalTree == null) {
                return Collections.emptyList();
            }
            IntervalArray.QueryResult queryResult = intervalTree.findOverlappingWithPoint(position - 1);
            return queryResult.getEntries();
    }

    private class ChromosomalRegionEndExtractor implements IntervalEndExtractor<T> {

        public int getBegin(T region) {
            return region.getStart() - 1;
        }

        public int getEnd(T region) {
            return region.getEnd();
        }
    }

}
