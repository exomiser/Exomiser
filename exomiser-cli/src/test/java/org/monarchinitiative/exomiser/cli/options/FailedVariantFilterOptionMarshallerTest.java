package org.monarchinitiative.exomiser.cli.options;

import org.apache.commons.cli.Option;
import org.junit.Before;
import org.junit.Test;
import org.monarchinitiative.exomiser.core.analysis.Settings;
import org.monarchinitiative.exomiser.core.prioritisers.PriorityType;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class FailedVariantFilterOptionMarshallerTest {

    private FailedVariantFilterOptionMarshaller instance;
    private Option option;
    private Settings.SettingsBuilder settingsBuilder;

    @Before
    public void setUp() {
        instance = new FailedVariantFilterOptionMarshaller();
        option = instance.getOption();
        settingsBuilder = Settings.builder();
        settingsBuilder.vcfFilePath(Paths.get("test.vcf"));
        settingsBuilder.usePrioritiser(PriorityType.OMIM_PRIORITY);
    }

    @Test
    public void testOptionCommandLineParameter() {
        assertThat(instance.getCommandLineParameter(), equalTo("remove-failed"));
    }

    @Test
    public void testThatOptionHasNoOptionalArgument() {
        assertThat(option.hasOptionalArg(), is(false));
    }

    @Test
    public void testSettingsRemoveFailedVariantsIsFalseByDefault() {
        Settings settings = settingsBuilder.build();
        assertThat(settings.removeFailedVariants(), is(false));
    }

    @Test
    public void testSettingsBuilderAlwaysAppliesTrueWhenSetWithNullValue() {
        instance.applyValuesToSettingsBuilder(null, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.removeFailedVariants(), is(true));
    }

    @Test
    public void testSettingsBuilderAlwaysAppliesTrueArgFalse() {
        String[] args = {"false"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.removeFailedVariants(), is(true));
    }

    @Test
    public void testSettingsBuilderAlwaysAppliesTrueArgTrue() {
        String[] args = {"true"};
        instance.applyValuesToSettingsBuilder(args, settingsBuilder);
        Settings settings = settingsBuilder.build();
        assertThat(settings.removeFailedVariants(), is(true));
    }
}