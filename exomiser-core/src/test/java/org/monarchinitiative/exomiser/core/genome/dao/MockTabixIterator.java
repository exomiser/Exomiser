/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mocks the TabixIterator returned from the TabixReader.query() method to
 * return a given set of values.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class MockTabixIterator implements TabixReader.Iterator {

    private static final Logger logger = LoggerFactory.getLogger(MockTabixIterator.class);

    private static final MockTabixIterator EMPTY = new MockTabixIterator(Collections.emptyList());

    private final List<String> values;
    private int pos;

    private MockTabixIterator(List<String> values) {
        this.values = values;
        pos = 0;
    }

    public static MockTabixIterator of(String... values) {
        return new MockTabixIterator(Arrays.asList(values));
    }

    public static MockTabixIterator of(List<String> values) {
        return new MockTabixIterator(values);
    }

    public static MockTabixIterator empty() {
        return EMPTY;
    }

    @Override
    public String next() {
        if (pos < values.size()) {
            return values.get(pos++);
        }
        return null;
    }

    @Override
    public String toString() {
        return "MockTabixIterator{" + "values=" + values + '}';
    }

}
