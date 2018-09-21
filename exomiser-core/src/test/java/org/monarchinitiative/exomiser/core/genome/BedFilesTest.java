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

package org.monarchinitiative.exomiser.core.genome;

import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.model.ChromosomalRegion;
import org.monarchinitiative.exomiser.core.model.GeneticInterval;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class BedFilesTest {

    @Test
    public void readChromosomalRegions() {
        List<ChromosomalRegion> expectedIntervals = new ArrayList<>();
        expectedIntervals.add(new GeneticInterval(7, 127471197, 127472363));
        expectedIntervals.add(new GeneticInterval(7, 127472364, 127473530));
        expectedIntervals.add(new GeneticInterval(7, 127475865, 127477031));
        expectedIntervals.add(new GeneticInterval(7, 127479366, 127480532));
        expectedIntervals.add(new GeneticInterval(7, 127480533, 127481699));

        Stream<ChromosomalRegion> regions = BedFiles.readChromosomalRegions(Paths.get("src/test/resources/intervals.bed"));

        assertThat(regions.collect(toList()), contains(expectedIntervals.toArray()));
    }
}