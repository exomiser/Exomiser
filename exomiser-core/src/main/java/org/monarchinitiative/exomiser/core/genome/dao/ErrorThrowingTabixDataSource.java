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
