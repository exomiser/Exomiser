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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ErrorThrowingTabixDataSourceTest {

    @Test(expected = IllegalArgumentException.class)
    public void testQueryString() throws Exception {
        TabixDataSource instance = new ErrorThrowingTabixDataSource("LOCAL");
        instance.query("X:12345-12345");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyQueryString() throws Exception {
        TabixDataSource instance = new ErrorThrowingTabixDataSource("LOCAL");
        instance.query("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyQuery() throws Exception {
        TabixDataSource instance = new ErrorThrowingTabixDataSource("LOCAL");
        instance.query("X", 12345, 12345);
    }

    @Test
    public void testGetSourceReturnsEmptyString() throws Exception {
        TabixDataSource instance = new ErrorThrowingTabixDataSource("LOCAL");
        assertThat(instance.getSource(), equalTo("No source"));
    }
}