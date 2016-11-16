package org.monarchinitiative.exomiser.cli.options;

import org.apache.commons.cli.Option;
import org.monarchinitiative.exomiser.core.analysis.Settings;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class ProbandSampleNameOptionMarshaller extends AbstractOptionMarshaller {

    public static final String PROBAND_SAMPLE_NAME_OPTION = "proband";

    public ProbandSampleNameOptionMarshaller() {
        option = Option.builder()
                .hasArg()
                .desc("Sample name of the proband. This should be present in both the ped and vcf files. Required if the vcf file is for a family.")
                .longOpt(PROBAND_SAMPLE_NAME_OPTION)
                .build();
    }

    @Override
    public void applyValuesToSettingsBuilder(String[] values, Settings.SettingsBuilder settingsBuilder) {
        settingsBuilder.probandSampleName(values[0]);
    }

}
