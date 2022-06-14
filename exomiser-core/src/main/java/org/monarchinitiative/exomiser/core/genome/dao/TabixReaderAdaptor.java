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

package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Wrapper for an HTSJDK TabixReader.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TabixReaderAdaptor implements TabixDataSource {

    private final TabixReader tabixReader;
    private final Map<String, String> chrIndex;

    public TabixReaderAdaptor(TabixReader tabixReader) {
        this.tabixReader = tabixReader;
        this.chrIndex = createChrIndex(tabixReader.getChromosomes());
    }

    private Map<String, String> createChrIndex(Set<String> tabixChromosomes) {
        // create a map of what Exomiser calls a chromosome with what the tabix file uses to identify the chromosome
        // e.g. chr1 or 1, chrY or Y
        GenomeAssembly genomeAssembly = GenomeAssembly.defaultBuild();
        return tabixChromosomes.stream().collect(Collectors.toMap(tabixChr -> genomeAssembly.getContigByName(tabixChr).name(), Function.identity()));
    }

    /**
     * @deprecated Unless you're 100% certain, use the method query(String chromosome, int start, int end) which will
     * automatically translate the chromosome name to the internal tabix identifier. USING THIS METHOD MAY RESULT IN NO
     * DATA BEING RETURNED DUE TO CHROMOSOME NAMING DIFFERENCES.
     */
    @Override
    public synchronized TabixReader.Iterator query(String query) {
        return tabixReader.query(query);
    }

    /**
     * @implNote This method will automatically map from the Exomiser chromosome name (1-22,X,Y,MT) to the value used in
     * the tabix file. Requires fully-closed start and end coordinates.
     */
    @Override
    public synchronized TabixReader.Iterator query(String chromosome, int start, int end) {
        String chr = chrIndex.get(chromosome);
        return tabixReader.query(tabixReader.chr2tid(chr), start - 1, end);
    }

    @Override
    public void close() {
        tabixReader.close();
    }

    @Override
    public String getSource() {
        return tabixReader.getSource();
    }
}
