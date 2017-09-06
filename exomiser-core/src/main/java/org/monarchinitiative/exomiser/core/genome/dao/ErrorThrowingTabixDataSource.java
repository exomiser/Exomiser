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
 * Special implementation of the TabixDataSource to throw an error if called, but the datasource has not been configured
 * in the application.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ErrorThrowingTabixDataSource implements TabixDataSource {

    private final String message;

    public ErrorThrowingTabixDataSource(String message) {
        this.message = message;
    }

    @Override
    public TabixReader.Iterator query(String query) {
        throw new IllegalArgumentException(message);
    }

    @Override
    public TabixReader.Iterator query(String chromosome, int start, int end) {
        throw new IllegalArgumentException(message);
    }

    @Override
    public void close() {
        //empty implementation
    }

    @Override
    public String getSource() {
        return "No source";
    }
}
