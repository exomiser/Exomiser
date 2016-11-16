package org.monarchinitiative.exomiser.cli.options;

import org.apache.commons.cli.Option;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.Settings;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ProbandSampleNameOptionMarshallerTest {

    ProbandSampleNameOptionMarshaller instance = new ProbandSampleNameOptionMarshaller();

    @Test
    public void testGetCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo("proband"));
    }

    @Test
    public void testOptionTakesAnArgument() {
        Option option = instance.getOption();
        assertThat(option.hasArg(), is(true));
    }

    @Test
    public void testApplyValuesToSettingsBuilderSetsVcfValue() {
        String probandSampleName = "AGENT-47";
        String[] values = {probandSampleName};

        Settings.SettingsBuilder settingsBuilder = Settings.builder();
        instance.applyValuesToSettingsBuilder(values, settingsBuilder);
        Settings settings = settingsBuilder.build();

        assertThat(settings.getProbandSampleName(), equalTo(probandSampleName));
    }
}
