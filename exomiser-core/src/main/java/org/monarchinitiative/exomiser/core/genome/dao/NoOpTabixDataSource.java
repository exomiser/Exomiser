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

import java.io.IOException;

/**
 * No-op implementation of the TabixDataSource. Will return an empty iterator which will always return 'null' for any
 * query.
 *
 * @since 13.0.0
 */
public class NoOpTabixDataSource implements TabixDataSource {

    private static final TabixReader.Iterator NO_OP_ITERATOR = new NoOpTabixIterator();

    private final String source;

    public NoOpTabixDataSource(String source) {
        this.source = source;
    }

    @Override
    public TabixReader.Iterator query(String query) {
        return NO_OP_ITERATOR;
    }

    @Override
    public TabixReader.Iterator query(String chromosome, int start, int end) {
        return NO_OP_ITERATOR;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public void close() throws IOException {
        // no-op
    }

    private static final class NoOpTabixIterator implements TabixReader.Iterator {
        @Override
        public String next() throws IOException {
            return null;
        }
    }
}
