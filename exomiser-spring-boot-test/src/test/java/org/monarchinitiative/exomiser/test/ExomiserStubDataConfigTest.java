package org.monarchinitiative.exomiser.test;

import de.charite.compbio.jannovar.data.JannovarData;
import htsjdk.tribble.readers.TabixReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ExomiserStubDataConfig.class)
public class ExomiserStubDataConfigTest {

    @Autowired
    private JannovarData jannovarData;

    @Autowired
    private TabixReader inDelTabixReader;

    @Autowired
    private TabixReader snvTabixReader;

    @Autowired
    private TabixReader remmTabixReader;

    @Autowired
    private TabixReader localFrequencyTabixReader;

    @Test
    public void testJannovarData() {
        assertThat(jannovarData.getChromosomes().size(), equalTo(25));
    }

    @Test
    public void testInDelTabixReader() {
        assertThat(inDelTabixReader, instanceOf(TabixReader.class));
    }

    @Test
    public void testSnvTabixReader() {
        assertThat(snvTabixReader, instanceOf(TabixReader.class));
    }

    @Test
    public void testRemmTabixReader() {
        assertThat(remmTabixReader, instanceOf(TabixReader.class));
    }

    @Test
    public void testLocalFrequencyTabixReader() {
        assertThat(localFrequencyTabixReader, instanceOf(TabixReader.class));
    }
}