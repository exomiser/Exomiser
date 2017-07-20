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