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
