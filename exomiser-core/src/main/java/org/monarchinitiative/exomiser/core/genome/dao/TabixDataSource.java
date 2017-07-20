package org.monarchinitiative.exomiser.core.genome.dao;

import htsjdk.tribble.readers.TabixReader;

import java.io.Closeable;

/**
 * Abstraction for querying Tabix files. The HTSJK TabixReader is not easy to test and provides no interfaceor
 * alternate implementations. This partially mitigates this issue as the Tabix.Iterator does not implement
 * java.util.Iterator.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public interface TabixDataSource extends Closeable {

    public TabixReader.Iterator query(String query);

    public TabixReader.Iterator query(String chromosome, int start, int end);

    public String getSource();

}