package de.charite.compbio.exomiser.cli;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class BatchFileReaderTest {

    private BatchFileReader instance;

    @Before
    public void setUp() throws Exception {
        instance = new BatchFileReader();
    }

    private List<Path> getPaths(String fileName) {
        return instance.readPathsFromBatchFile(Paths.get(fileName));
    }

    @Test
    public void testReadPathsFromBatchFile_FileNotFound() throws Exception {
        assertThat(getPaths("wibble.txt").isEmpty(), is(true));
    }

    @Test
    public void testReadPathsFromBatchFile() throws Exception {
        assertThat(getPaths("src/test/resources/testBatchFiles.txt").size(), equalTo(3));
    }
}