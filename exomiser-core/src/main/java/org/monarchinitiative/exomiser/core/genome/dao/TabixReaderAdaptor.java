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

package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;

/**
 * Wrapper for an HTSJDK TabixReader.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class TabixReaderAdaptor implements TabixDataSource {

    private final TabixReader tabixReader;

    public TabixReaderAdaptor(TabixReader tabixReader) {
        this.tabixReader = tabixReader;
    }

    @Override
    public TabixReader.Iterator query(String query) {
        return tabixReader.query(query);
    }

    @Override
    public TabixReader.Iterator query(String chromosome, int start, int end) {
        return tabixReader.query(chromosome, start, end);
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
