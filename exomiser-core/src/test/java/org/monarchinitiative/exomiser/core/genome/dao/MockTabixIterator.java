/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Mocks the TabixIterator returned from the TabixReader.query() method to
 * return a given set of values.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
class MockTabixIterator implements TabixReader.Iterator {

    private static final Logger logger = LoggerFactory.getLogger(MockTabixIterator.class);

    private List<String> values;
    private int pos;

    MockTabixIterator() {
        setUp(new ArrayList<>());
    }

    MockTabixIterator(List<String> values) {
        setUp(values);
    }

    private void setUp(List<String> values) {
        this.values = values;
        pos = 0;
    }

    void setValues(List<String> values) {
        setUp(values);
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
